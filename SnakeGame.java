import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import java.util.Random;
import java.io.*;
import javax.sound.sampled.*;

public class SnakeGame extends JPanel implements ActionListener, KeyListener {

    enum GameState { MENU, RUNNING, GAME_OVER }
    GameState state = GameState.MENU;

    final int WIDTH = 600, HEIGHT = 600, UNIT_SIZE = 25;

    int[] x = new int[600];
    int[] y = new int[600];

    int bodyParts = 3, applesEaten, highScore = 0;
    int appleX, appleY;

    char direction = 'R';
    boolean paused = false;

    Timer timer;
    Random random;

    int speed = 120;

    Clip bgClip;

    SnakeGame() {
        random = new Random();
        this.setPreferredSize(new Dimension(WIDTH, HEIGHT));
        this.setBackground(Color.black);
        this.setFocusable(true);
        this.addKeyListener(this);
        loadHighScore();
    }

    // 🔊 SOUND (FIXED PATH)
    public void playSound(String file) {
        try {
            java.net.URL url = getClass().getResource("/" + file);
            javax.sound.sampled.AudioInputStream audio = javax.sound.sampled.AudioSystem.getAudioInputStream(url);
            javax.sound.sampled.Clip clip = javax.sound.sampled.AudioSystem.getClip();
            clip.open(audio);
            clip.start();
        } catch (Exception e) {
            System.out.println("Sound error: " + file);
        }
    }



    public void playBG(String file) {
        stopBG();
        try {
            java.net.URL url = getClass().getResource("/" + file);
            javax.sound.sampled.AudioInputStream audio = javax.sound.sampled.AudioSystem.getAudioInputStream(url);
            bgClip = javax.sound.sampled.AudioSystem.getClip();
            bgClip.open(audio);
            bgClip.loop(javax.sound.sampled.Clip.LOOP_CONTINUOUSLY);
        } catch (Exception e) {
            System.out.println("BG error");
        }
    }
    public void stopBG() {
        if (bgClip != null) bgClip.stop();
    }

    // 🎮 START GAME (FULL RESET FIX)
    public void startGame() {
state =   GameState.RUNNING;
        for (int i = 0; i < 600; i++) {
            x[i] = 300;
            y[i] = 300;
        }

        bodyParts = 3;
        applesEaten = 0;
        direction = 'R';
        paused = false;

        newApple();

        if (timer != null) timer.stop();
        timer = new Timer(speed, this);
        timer.start();

        playBG("bg.wav");

        state = GameState.RUNNING;
    }

    public void paintComponent(Graphics g) {
        super.paintComponent(g);

        if (state == GameState.MENU) drawMenu(g);
        else if (state == GameState.RUNNING) drawGame(g);
        else drawGameOver(g);
    }

    // 🎯 MENU WITH DIFFICULTY
    public void drawMenu(Graphics g) {
        g.setColor(Color.white);
        g.setFont(new Font("Arial", Font.BOLD, 30));
        g.drawString("SNAKE GAME", 180, 200);

        g.setFont(new Font("Arial", Font.PLAIN, 20));
        g.drawString("Press 1 - Easy", 220, 260);
        g.drawString("Press 2 - Medium", 220, 300);
        g.drawString("Press 3 - Hard", 220, 340);
    }

    public void drawGame(Graphics g) {

        g.setColor(Color.darkGray);
        for (int i = 0; i < HEIGHT / UNIT_SIZE; i++) {
            g.drawLine(i * UNIT_SIZE, 0, i * UNIT_SIZE, HEIGHT);
            g.drawLine(0, i * UNIT_SIZE, WIDTH, i * UNIT_SIZE);
        }

        // 🌈 NEON FOOD
        g.setColor(new Color(255, 0, 150));
        g.fillOval(appleX, appleY, UNIT_SIZE, UNIT_SIZE);
        g.setColor(Color.pink);
        g.drawOval(appleX - 2, appleY - 2, UNIT_SIZE + 4, UNIT_SIZE + 4);

        // 🐍 ANIMATED SNAKE
        // 🐍 REALISTIC SNAKE
        for (int i = 0; i < bodyParts; i++) {

            if (i == 0) {
                // HEAD
                g.setColor(new Color(0, 180, 0));
                g.fillOval(x[i], y[i], UNIT_SIZE, UNIT_SIZE);

                // Eyes
                g.setColor(Color.white);
                g.fillOval(x[i] + 5, y[i] + 5, 5, 5);
                g.fillOval(x[i] + UNIT_SIZE - 10, y[i] + 5, 5, 5);

                g.setColor(Color.black);
                g.fillOval(x[i] + 6, y[i] + 6, 2, 2);
                g.fillOval(x[i] + UNIT_SIZE - 9, y[i] + 6, 2, 2);

            } else {
                // BODY
                g.setColor(new Color(34, 139, 34)); // snake green
                g.fillOval(x[i], y[i], UNIT_SIZE, UNIT_SIZE);

                // scales effect
                g.setColor(new Color(0, 100, 0));
                g.drawOval(x[i], y[i], UNIT_SIZE, UNIT_SIZE);
            }
        }

        g.setColor(Color.white);
        g.drawString("Score: " + applesEaten, 10, 20);
        g.drawString("High: " + highScore, 10, 40);

        if (paused) {
            g.setFont(new Font("Arial", Font.BOLD, 40));
            g.drawString("PAUSED", 220, 300);
        }
    }

    public void drawGameOver(Graphics g) {
        g.setColor(Color.red);
        g.setFont(new Font("Arial", Font.BOLD, 40));
        g.drawString("GAME OVER", 180, 250);

        g.setFont(new Font("Arial", Font.BOLD, 20));
        g.drawString("Press R to Restart", 200, 320);
    }

    public void newApple() {
        appleX = random.nextInt(WIDTH / UNIT_SIZE) * UNIT_SIZE;
        appleY = random.nextInt(HEIGHT / UNIT_SIZE) * UNIT_SIZE;
    }

    public void move() {
        for (int i = bodyParts; i > 0; i--) {
            x[i] = x[i - 1];
            y[i] = y[i - 1];
        }

        switch (direction) {
            case 'U':
                y[0] -= UNIT_SIZE;
                break;

            case 'D':
                y[0] += UNIT_SIZE;
                break;

            case 'L':
                x[0] -= UNIT_SIZE;
                break;

            case 'R':
                x[0] += UNIT_SIZE;
                break;
        }
    }

    public void checkApple() {
        if (x[0] == appleX && y[0] == appleY) {
            bodyParts++;
            applesEaten++;
            playSound("eat.wav");

            if (applesEaten > highScore) highScore = applesEaten;

            newApple();
        }
    }

    public void checkCollisions() {

        for (int i = bodyParts; i > 0; i--) {
            if (x[0] == x[i] && y[0] == y[i]) gameOver();
        }

        if (x[0] < 0 || x[0] >= WIDTH || y[0] < 0 || y[0] >= HEIGHT)
            gameOver();
    }

    public void gameOver() {
        state = GameState.GAME_OVER;
        timer.stop();
        playSound("gameover.wav");
        stopBG();
        saveHighScore();
    }

    public void actionPerformed(ActionEvent e) {
        if (state == GameState.RUNNING && !paused) {
            move();
            checkApple();
            checkCollisions();
        }
        repaint();
    }

    public void saveHighScore() {
        try {
            FileWriter fw = new FileWriter("highscore.txt");
            fw.write("" + highScore);
            fw.close();
        } catch (Exception e) {}
    }

    public void loadHighScore() {
        try {
            BufferedReader br = new BufferedReader(new FileReader("highscore.txt"));
            highScore = Integer.parseInt(br.readLine());
            br.close();
        } catch (Exception e) {
            highScore = 0;
        }
    }

    @Override
    public void keyPressed(KeyEvent e) {

        int key = e.getKeyCode();

        // 🎮 MENU CONTROLS
        if (state == GameState.MENU) {
            if (key == KeyEvent.VK_1) { speed = 120; startGame(); }
            if (key == KeyEvent.VK_2) { speed = 80; startGame(); }
            if (key == KeyEvent.VK_3) { speed = 50; startGame(); }
            return; // ⚠️ IMPORTANT
        }

        // 🐍 GAME CONTROLS (only when running)
        if (state == GameState.RUNNING) {

            if (key == KeyEvent.VK_LEFT && direction != 'R') direction = 'L';
            if (key == KeyEvent.VK_RIGHT && direction != 'L') direction = 'R';
            if (key == KeyEvent.VK_UP && direction != 'D') direction = 'U';
            if (key == KeyEvent.VK_DOWN && direction != 'U') direction = 'D';
        }

        // ⏸️ PAUSE
        if (key == KeyEvent.VK_P) paused = !paused;

        // 🔁 RESTART (only after game over)
        if (key == KeyEvent.VK_R && state == GameState.GAME_OVER) {
            startGame();
        }
    }

    @Override public void keyReleased(KeyEvent e) {}
    @Override public void keyTyped(KeyEvent e) {}

    public static void main(String[] args) {
        JFrame frame = new JFrame("Snake Game FINAL");
        SnakeGame game = new SnakeGame();

        frame.add(game);
        frame.pack();
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.setLocationRelativeTo(null);
        frame.setVisible(true);
    }
}

