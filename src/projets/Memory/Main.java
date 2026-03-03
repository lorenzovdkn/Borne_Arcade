import java.awt.Color;
import java.awt.Font;
import java.awt.FontMetrics;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.GraphicsDevice;
import java.awt.GraphicsEnvironment;
import java.awt.RenderingHints;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import javax.swing.JFrame;
import javax.swing.JPanel;
import javax.swing.WindowConstants;

public class Main {
    public static void main(String[] args) {
        MemoryGame game = new MemoryGame();
        game.start();
    }
}

class MemoryGame extends JPanel {
    private static final int COLS = 4;
    private static final int ROWS = 4;
    private static final int CARD_COUNT = COLS * ROWS;

    private enum GameState {
        MENU, PLAYING, GAME_OVER
    }

    private enum PlayerState {
        SELECTING, WAITING_SECOND, CHECKING
    }

    private GameState state = GameState.MENU;
    private int menuSelection = 0;

    private int currentPlayer = 0;
    private int[] cursors = {0, 1};
    private int[] firstSelected = {-1, -1};
    private int[] secondSelected = {-1, -1};
    private PlayerState[] playerStates = {PlayerState.SELECTING, PlayerState.SELECTING};
    private long[] checkTimes = {0L, 0L};
    private int[] scores = {0, 0};
    private boolean changingPlayer = false;
    private long changePlayerTime = 0L;

    private int[] values = new int[CARD_COUNT];
    private boolean[] revealed = new boolean[CARD_COUNT];
    private boolean[] matched = new boolean[CARD_COUNT];

    private final ClavierBorneArcade keyboard = new ClavierBorneArcade();

    private boolean j1ActionTape = false;
    private boolean j2ActionTape = false;

    private int screenWidth;
    private int screenHeight;

    public MemoryGame() {
        setBackground(new Color(20, 20, 24));
        setFocusable(true);
        setDoubleBuffered(true);
    }

    public void start() {
        JFrame frame = new JFrame("Memory");
        frame.setDefaultCloseOperation(WindowConstants.EXIT_ON_CLOSE);
        frame.addKeyListener(keyboard);
        addKeyListener(keyboard);
        
        KeyAdapter customKeys = new KeyAdapter() {
            @Override
            public void keyReleased(KeyEvent e) {
                if (e.getKeyCode() == KeyEvent.VK_AMPERSAND || e.getKeyCode() == KeyEvent.VK_1) {
                    j1ActionTape = true;
                }
                if (e.getKeyCode() == KeyEvent.VK_Q) {
                    j2ActionTape = true;
                }
            }
        };
        frame.addKeyListener(customKeys);
        addKeyListener(customKeys);
        
        frame.setContentPane(this);

        GraphicsDevice device = GraphicsEnvironment.getLocalGraphicsEnvironment().getDefaultScreenDevice();
        if (device.isFullScreenSupported()) {
            frame.setUndecorated(true);
            device.setFullScreenWindow(frame);
            screenWidth = device.getDisplayMode().getWidth();
            screenHeight = device.getDisplayMode().getHeight();
        } else {
            screenWidth = 1280;
            screenHeight = 1024;
            frame.setSize(screenWidth, screenHeight);
            frame.setVisible(true);
        }

        requestFocusInWindow();

        while (true) {
            updateGame();
            repaint();
            try {
                Thread.sleep(16);
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
                return;
            }
        }
    }

    private void updateGame() {
        if (keyboard.getBoutonJ1ZTape()) {
            System.exit(0);
        }

        switch (state) {
            case MENU:
                updateMenu();
                break;
            case PLAYING:
                updatePlaying();
                break;
            case GAME_OVER:
                updateGameOver();
                break;
        }
    }

    private void updateMenu() {
        if (keyboard.getJoyJ1HautTape() || keyboard.getJoyJ2HautTape()) {
            menuSelection = (menuSelection - 1 + 2) % 2;
        }
        if (keyboard.getJoyJ1BasTape() || keyboard.getJoyJ2BasTape()) {
            menuSelection = (menuSelection + 1) % 2;
        }

        boolean menuAction = j1ActionTape || j2ActionTape;
        j1ActionTape = false;
        j2ActionTape = false;

        if (menuAction) {
            if (menuSelection == 0) {
                initGame();
                state = GameState.PLAYING;
            } else {
                System.exit(0);
            }
        }
    }

    private void initGame() {
        List<Integer> deck = new ArrayList<>();
        for (int value = 0; value < CARD_COUNT / 2; value++) {
            deck.add(value);
            deck.add(value);
        }
        Collections.shuffle(deck);

        for (int i = 0; i < CARD_COUNT; i++) {
            values[i] = deck.get(i);
            revealed[i] = false;
            matched[i] = false;
        }

        currentPlayer = 0;
        cursors[0] = 0;
        cursors[1] = 1;
        firstSelected[0] = -1;
        firstSelected[1] = -1;
        secondSelected[0] = -1;
        secondSelected[1] = -1;
        playerStates[0] = PlayerState.SELECTING;
        playerStates[1] = PlayerState.SELECTING;
        scores[0] = 0;
        scores[1] = 0;
        changingPlayer = false;
    }

    private void updatePlaying() {
        if (changingPlayer) {
            long elapsed = System.currentTimeMillis() - changePlayerTime;
            if (elapsed >= 1200) {
                changingPlayer = false;
                j1ActionTape = false;
                j2ActionTape = false;
            }
            return;
        }

        if (currentPlayer == 0) {
            boolean action = j1ActionTape;
            j1ActionTape = false;
            updatePlayerInput(0, keyboard.getJoyJ1HautTape(), keyboard.getJoyJ1BasTape(),
                    keyboard.getJoyJ1GaucheTape(), keyboard.getJoyJ1DroiteTape(), action);
        } else {
            boolean action = j2ActionTape;
            j2ActionTape = false;
            updatePlayerInput(1, keyboard.getJoyJ2HautTape(), keyboard.getJoyJ2BasTape(),
                    keyboard.getJoyJ2GaucheTape(), keyboard.getJoyJ2DroiteTape(), action);
        }

        boolean allMatched = true;
        for (boolean m : matched) {
            if (!m) {
                allMatched = false;
                break;
            }
        }
        if (allMatched) {
            state = GameState.GAME_OVER;
        }
    }

    private void updatePlayerInput(int player, boolean up, boolean down, boolean left, boolean right, boolean action) {
        PlayerState pState = playerStates[player];

        if (pState == PlayerState.CHECKING) {
            long elapsed = System.currentTimeMillis() - checkTimes[player];
            if (elapsed >= 850) {
                boolean isMatch = values[firstSelected[player]] == values[secondSelected[player]];
                
                if (isMatch) {
                    matched[firstSelected[player]] = true;
                    matched[secondSelected[player]] = true;
                    scores[player]++;
                } else {
                    revealed[firstSelected[player]] = false;
                    revealed[secondSelected[player]] = false;
                    currentPlayer = 1 - currentPlayer;
                    changingPlayer = true;
                    changePlayerTime = System.currentTimeMillis();
                }
                
                firstSelected[player] = -1;
                secondSelected[player] = -1;
                playerStates[player] = PlayerState.SELECTING;
            }
            return;
        }

        if (up && cursors[player] >= COLS) {
            cursors[player] -= COLS;
        }
        if (down && cursors[player] < CARD_COUNT - COLS) {
            cursors[player] += COLS;
        }
        if (left && cursors[player] % COLS > 0) {
            cursors[player]--;
        }
        if (right && cursors[player] % COLS < COLS - 1) {
            cursors[player]++;
        }

        if (action) {
            if (pState == PlayerState.SELECTING) {
                if (!matched[cursors[player]] && !revealed[cursors[player]]) {
                    revealed[cursors[player]] = true;
                    firstSelected[player] = cursors[player];
                    playerStates[player] = PlayerState.WAITING_SECOND;
                }
            } else if (pState == PlayerState.WAITING_SECOND) {
                if (!matched[cursors[player]] && !revealed[cursors[player]]) {
                    revealed[cursors[player]] = true;
                    secondSelected[player] = cursors[player];
                    playerStates[player] = PlayerState.CHECKING;
                    checkTimes[player] = System.currentTimeMillis();
                } else if (cursors[player] == firstSelected[player]) {
                    revealed[firstSelected[player]] = false;
                    firstSelected[player] = -1;
                    playerStates[player] = PlayerState.SELECTING;
                }
            }
        }
    }

    private void updateGameOver() {
        boolean restartAction = j1ActionTape || j2ActionTape;
        j1ActionTape = false;
        j2ActionTape = false;
        
        if (restartAction) {
            menuSelection = 0;
            state = GameState.MENU;
        }
    }

    private String symbolFor(int value) {
        String[] symbols = {"★", "☻", "♠", "♥", "♦", "♣", "☺", "♪"};
        return symbols[value % symbols.length];
    }

    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);
        Graphics2D g2 = (Graphics2D) g;
        g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

        screenWidth = getWidth();
        screenHeight = getHeight();

        if (state == GameState.MENU) {
            paintMenu(g2);
        } else if (state == GameState.PLAYING) {
            paintGame(g2);
        } else if (state == GameState.GAME_OVER) {
            paintGameOver(g2);
        }
    }

    private void paintMenu(Graphics2D g2) {
        g2.setColor(Color.WHITE);
        g2.setFont(new Font("Arial", Font.BOLD, 80));
        String title = "MEMORY";
        FontMetrics fm = g2.getFontMetrics();
        int x = (screenWidth - fm.stringWidth(title)) / 2;
        g2.drawString(title, x, screenHeight / 3);

        g2.setFont(new Font("Arial", Font.PLAIN, 40));
        String[] options = {"JOUER", "QUITTER"};
        int startY = screenHeight / 2;
        int gap = 100;

        for (int i = 0; i < options.length; i++) {
            if (i == menuSelection) {
                g2.setColor(new Color(255, 220, 70));
                g2.fillRoundRect(screenWidth / 2 - 200, startY + i * gap - 30, 400, 60, 15, 15);
                g2.setColor(Color.BLACK);
            } else {
                g2.setColor(Color.WHITE);
            }
            fm = g2.getFontMetrics();
            int ox = (screenWidth - fm.stringWidth(options[i])) / 2;
            int oy = startY + i * gap;
            g2.drawString(options[i], ox, oy);
        }
    }

    private void paintGame(Graphics2D g2) {
        int gridWidth = Math.min(900, screenWidth - 120);
        int cardGap = 12;
        int cardWidth = (gridWidth - (COLS - 1) * cardGap) / COLS;
        int cardHeight = (int) (cardWidth * 1.2);

        int gridHeight = ROWS * cardHeight + (ROWS - 1) * cardGap;
        int startX = (screenWidth - gridWidth) / 2;
        int startY = (screenHeight - gridHeight) / 2 + 80;

        g2.setColor(Color.WHITE);
        g2.setFont(new Font("Arial", Font.BOLD, 36));
        g2.drawString("MEMORY", 40, 50);

        g2.setFont(new Font("Arial", Font.BOLD, 28));
        if (currentPlayer == 0) {
            g2.setColor(new Color(255, 220, 70));
            g2.drawString("▶ J1: " + scores[0], 40, 90);
            g2.setColor(Color.GRAY);
            g2.drawString("J2: " + scores[1], 180, 90);
        } else {
            g2.setColor(Color.GRAY);
            g2.drawString("J1: " + scores[0], 40, 90);
            g2.setColor(new Color(100, 200, 255));
            g2.drawString("▶ J2: " + scores[1], 180, 90);
        }
        g2.setColor(Color.WHITE);

        Font cardFont = new Font("Arial", Font.BOLD, 42);
        g2.setFont(cardFont);
        FontMetrics fm = g2.getFontMetrics();

        for (int i = 0; i < CARD_COUNT; i++) {
            int row = i / COLS;
            int col = i % COLS;

            int x = startX + col * (cardWidth + cardGap);
            int y = startY + row * (cardHeight + cardGap);

            boolean showFace = revealed[i] || matched[i];

            if (matched[i]) {
                g2.setColor(new Color(70, 150, 70));
            } else if (showFace) {
                g2.setColor(new Color(230, 230, 230));
            } else {
                g2.setColor(new Color(70, 90, 210));
            }

            g2.fillRoundRect(x, y, cardWidth, cardHeight, 18, 18);
            g2.setColor(new Color(18, 18, 18));
            g2.drawRoundRect(x, y, cardWidth, cardHeight, 18, 18);

            if (showFace) {
                String text = symbolFor(values[i]);
                int tx = x + (cardWidth - fm.stringWidth(text)) / 2;
                int ty = y + (cardHeight + fm.getAscent() - fm.getDescent()) / 2;
                g2.setColor(new Color(20, 20, 20));
                g2.drawString(text, tx, ty);
            }

            if (currentPlayer == 0 && i == cursors[0]) {
                g2.setColor(new Color(255, 220, 70));
                g2.drawRoundRect(x - 3, y - 3, cardWidth + 6, cardHeight + 6, 20, 20);
                g2.drawRoundRect(x - 4, y - 4, cardWidth + 8, cardHeight + 8, 20, 20);
            }

            if (currentPlayer == 1 && i == cursors[1]) {
                g2.setColor(new Color(100, 200, 255));
                g2.drawRoundRect(x - 3, y - 3, cardWidth + 6, cardHeight + 6, 20, 20);
                g2.drawRoundRect(x - 4, y - 4, cardWidth + 8, cardHeight + 8, 20, 20);
            }
        }
    }

    private void paintGameOver(Graphics2D g2) {
        g2.setColor(Color.WHITE);
        g2.setFont(new Font("Arial", Font.BOLD, 60));
        String title = "FIN DE JEU";
        FontMetrics fm = g2.getFontMetrics();
        int x = (screenWidth - fm.stringWidth(title)) / 2;
        g2.drawString(title, x, 150);

        g2.setFont(new Font("Arial", Font.PLAIN, 48));
        String j1Score = "Joueur 1: " + scores[0];
        String j2Score = "Joueur 2: " + scores[1];
        String result;

        if (scores[0] > scores[1]) {
            result = "Joueur 1 gagne!";
        } else if (scores[1] > scores[0]) {
            result = "Joueur 2 gagne!";
        } else {
            result = "Egalité!";
        }

        fm = g2.getFontMetrics();
        g2.drawString(j1Score, (screenWidth - fm.stringWidth(j1Score)) / 2, 350);
        g2.drawString(j2Score, (screenWidth - fm.stringWidth(j2Score)) / 2, 420);
        g2.drawString(result, (screenWidth - fm.stringWidth(result)) / 2, 550);

        g2.setFont(new Font("Arial", Font.PLAIN, 32));
        String info = "Appuyez sur A pour rejouer ou Z pour quitter";
        fm = g2.getFontMetrics();
        g2.drawString(info, (screenWidth - fm.stringWidth(info)) / 2, screenHeight - 80);
    }
}
