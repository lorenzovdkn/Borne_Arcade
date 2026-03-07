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
import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
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
    private static final int MAX_HIGHSCORES = 10;

    // Couleurs pré-définies pour optimisation (évite création d'objets à chaque frame)
    private static final Color BG_COLOR = new Color(20, 20, 24);
    private static final Color WHITE = Color.WHITE;
    private static final Color GRAY = Color.GRAY;
    private static final Color BLACK = Color.BLACK;
    private static final Color YELLOW = new Color(255, 220, 70);
    private static final Color BLUE_LIGHT = new Color(100, 200, 255);
    private static final Color GREEN_MATCHED = new Color(70, 180, 70);
    private static final Color BLUE_MATCHED = new Color(70, 120, 200);
    private static final Color CARD_FACE = new Color(230, 230, 230);
    private static final Color CARD_BACK = new Color(70, 90, 210);
    private static final Color CARD_BORDER = new Color(18, 18, 18);
    private static final Color CARD_TEXT = new Color(20, 20, 20);

    // Polices pré-définies
    private static final Font TITLE_FONT = new Font("Arial", Font.BOLD, 80);
    private static final Font MENU_FONT = new Font("Arial", Font.PLAIN, 40);
    private static final Font GAME_TITLE_FONT = new Font("Arial", Font.BOLD, 36);
    private static final Font SCORE_FONT = new Font("Arial", Font.BOLD, 32);
    private static final Font CARD_FONT = new Font("Arial", Font.BOLD, 42);
    private static final Font GAMEOVER_TITLE_FONT = new Font("Arial", Font.BOLD, 60);
    private static final Font GAMEOVER_SCORE_FONT = new Font("Arial", Font.PLAIN, 48);
    private static final Font GAMEOVER_INFO_FONT = new Font("Arial", Font.PLAIN, 32);
    private static final Font INITIAL_FONT = new Font("Arial", Font.BOLD, 72);

    // Symboles pré-définis pour éviter création d'objets à chaque frame
    private static final String[] CARD_SYMBOLS = {"★", "☻", "♠", "♥", "♦", "♣", "☺", "♪"};

    private enum GameState {
        MENU, PLAYING, GAME_OVER, ENTER_NAME
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
    private int[] matchedBy = new int[CARD_COUNT]; // 0 = J1, 1 = J2, -1 = non matché

    // Système de saisie des initiales
    private int winner = -1;
    private char[] initials = {'A', 'A', 'A'};
    private int initialCursor = 0;
    private List<String[]> highscores = new ArrayList<>();

    private final ClavierBorneArcade keyboard = new ClavierBorneArcade();

    private boolean j1ActionTape = false;
    private boolean j2ActionTape = false;
    private boolean needsRepaint = true;

    private int screenWidth;
    private int screenHeight;

    public MemoryGame() {
        setBackground(BG_COLOR);
        setFocusable(true);
        setDoubleBuffered(true);
        loadHighscores();
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
            if (needsRepaint) {
                repaint();
                needsRepaint = false;
            }
            try {
                Thread.sleep(33); // ~30fps pour réduire la charge CPU sur Raspberry Pi
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
                return;
            }
        }
    }

    private void loadHighscores() {
        highscores.clear();
        try (BufferedReader reader = new BufferedReader(new FileReader("highscore"))) {
            String line;
            while ((line = reader.readLine()) != null && highscores.size() < MAX_HIGHSCORES) {
                String[] parts = line.split("-");
                if (parts.length == 2) {
                    highscores.add(new String[]{parts[0], parts[1]});
                }
            }
        } catch (IOException e) {
            // Fichier inexistant, pas de highscores
        }
    }

    private void saveHighscores() {
        try (BufferedWriter writer = new BufferedWriter(new FileWriter("highscore"))) {
            for (String[] hs : highscores) {
                writer.write(hs[0] + "-" + hs[1]);
                writer.newLine();
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void addHighscore(String name, int score) {
        highscores.add(new String[]{name, String.valueOf(score)});
        highscores.sort((a, b) -> Integer.parseInt(b[1]) - Integer.parseInt(a[1]));
        if (highscores.size() > MAX_HIGHSCORES) {
            highscores.remove(highscores.size() - 1);
        }
        saveHighscores();
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
            case ENTER_NAME:
                updateEnterName();
                break;
        }
    }

    private void updateMenu() {
        if (keyboard.getJoyJ1HautTape() || keyboard.getJoyJ2HautTape()) {
            menuSelection = (menuSelection - 1 + 2) % 2;
            needsRepaint = true;
        }
        if (keyboard.getJoyJ1BasTape() || keyboard.getJoyJ2BasTape()) {
            menuSelection = (menuSelection + 1) % 2;
            needsRepaint = true;
        }

        boolean menuAction = j1ActionTape || j2ActionTape;
        j1ActionTape = false;
        j2ActionTape = false;

        if (menuAction) {
            if (menuSelection == 0) {
                initGame();
                state = GameState.PLAYING;
                needsRepaint = true;
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
            matchedBy[i] = -1;
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
        winner = -1;
        initials[0] = 'A';
        initials[1] = 'A';
        initials[2] = 'A';
        initialCursor = 0;
    }

    private void updatePlaying() {
        if (changingPlayer) {
            long elapsed = System.currentTimeMillis() - changePlayerTime;
            if (elapsed >= 1200) {
                changingPlayer = false;
                j1ActionTape = false;
                j2ActionTape = false;
                needsRepaint = true;
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
            needsRepaint = true;
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
                    matchedBy[firstSelected[player]] = player;
                    matchedBy[secondSelected[player]] = player;
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
                needsRepaint = true;
            }
            return;
        }

        int oldCursor = cursors[player];
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
        if (oldCursor != cursors[player]) {
            needsRepaint = true;
        }

        if (action) {
            if (pState == PlayerState.SELECTING) {
                if (!matched[cursors[player]] && !revealed[cursors[player]]) {
                    revealed[cursors[player]] = true;
                    firstSelected[player] = cursors[player];
                    playerStates[player] = PlayerState.WAITING_SECOND;
                    needsRepaint = true;
                }
            } else if (pState == PlayerState.WAITING_SECOND) {
                // On ne peut sélectionner que des cartes non matchées, non révélées,
                // et différentes de la première carte sélectionnée
                if (!matched[cursors[player]] && !revealed[cursors[player]] && cursors[player] != firstSelected[player]) {
                    revealed[cursors[player]] = true;
                    secondSelected[player] = cursors[player];
                    playerStates[player] = PlayerState.CHECKING;
                    checkTimes[player] = System.currentTimeMillis();
                    needsRepaint = true;
                }
            }
        }
    }

    private void updateGameOver() {
        boolean restartAction = j1ActionTape || j2ActionTape;
        j1ActionTape = false;
        j2ActionTape = false;
        
        if (restartAction) {
            // Déterminer le gagnant
            if (scores[0] > scores[1]) {
                winner = 0;
            } else if (scores[1] > scores[0]) {
                winner = 1;
            } else {
                // En cas d'égalité, retour au menu
                menuSelection = 0;
                state = GameState.MENU;
                needsRepaint = true;
                return;
            }
            // Passer à l'écran de saisie des initiales
            initials[0] = 'A';
            initials[1] = 'A';
            initials[2] = 'A';
            initialCursor = 0;
            state = GameState.ENTER_NAME;
            needsRepaint = true;
        }
    }

    private void updateEnterName() {
        boolean up = winner == 0 ? keyboard.getJoyJ1HautTape() : keyboard.getJoyJ2HautTape();
        boolean down = winner == 0 ? keyboard.getJoyJ1BasTape() : keyboard.getJoyJ2BasTape();
        boolean left = winner == 0 ? keyboard.getJoyJ1GaucheTape() : keyboard.getJoyJ2GaucheTape();
        boolean right = winner == 0 ? keyboard.getJoyJ1DroiteTape() : keyboard.getJoyJ2DroiteTape();
        boolean action = winner == 0 ? j1ActionTape : j2ActionTape;
        j1ActionTape = false;
        j2ActionTape = false;

        // Changer de lettre avec haut/bas
        if (up) {
            initials[initialCursor] = (char) ((initials[initialCursor] - 'A' + 1) % 26 + 'A');
            needsRepaint = true;
        }
        if (down) {
            initials[initialCursor] = (char) ((initials[initialCursor] - 'A' + 25) % 26 + 'A');
            needsRepaint = true;
        }

        // Naviguer entre les lettres
        if (left && initialCursor > 0) {
            initialCursor--;
            needsRepaint = true;
        }
        if (right && initialCursor < 2) {
            initialCursor++;
            needsRepaint = true;
        }

        // Valider
        if (action) {
            String name = new String(initials);
            addHighscore(name, scores[winner]);
            menuSelection = 0;
            state = GameState.MENU;
            needsRepaint = true;
        }
    }

    private String symbolFor(int value) {
        return CARD_SYMBOLS[value % CARD_SYMBOLS.length];
    }

    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);
        Graphics2D g2 = (Graphics2D) g;
        // Antialiasing désactivé pour meilleures performances sur Raspberry Pi
        g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_OFF);

        screenWidth = getWidth();
        screenHeight = getHeight();

        if (state == GameState.MENU) {
            paintMenu(g2);
        } else if (state == GameState.PLAYING) {
            paintGame(g2);
        } else if (state == GameState.GAME_OVER) {
            paintGameOver(g2);
        } else if (state == GameState.ENTER_NAME) {
            paintEnterName(g2);
        }
    }

    private void paintMenu(Graphics2D g2) {
        g2.setColor(WHITE);
        g2.setFont(TITLE_FONT);
        String title = "MEMORY";
        FontMetrics fm = g2.getFontMetrics();
        int x = (screenWidth - fm.stringWidth(title)) / 2;
        g2.drawString(title, x, screenHeight / 3);

        g2.setFont(MENU_FONT);
        String[] options = {"JOUER", "QUITTER"};
        int startY = screenHeight / 2;
        int gap = 100;

        for (int i = 0; i < options.length; i++) {
            if (i == menuSelection) {
                g2.setColor(YELLOW);
                g2.fillRoundRect(screenWidth / 2 - 200, startY + i * gap - 30, 400, 60, 15, 15);
                g2.setColor(BLACK);
            } else {
                g2.setColor(WHITE);
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
        int startY = (screenHeight - gridHeight) / 2 + 60;

        // Titre centré en haut
        g2.setColor(WHITE);
        g2.setFont(GAME_TITLE_FONT);
        FontMetrics fmTitle = g2.getFontMetrics();
        g2.drawString("MEMORY", (screenWidth - fmTitle.stringWidth("MEMORY")) / 2, 45);

        // Affichage des scores sur les côtés (en dehors de la zone des cartes)
        g2.setFont(SCORE_FONT);
        
        int leftScoreX = 30;
        int rightScoreX = screenWidth - 150;
        int scoreY = startY + gridHeight / 2;

        // Score Joueur 1 à gauche
        if (currentPlayer == 0) {
            g2.setColor(YELLOW);
            g2.fillRoundRect(leftScoreX - 10, scoreY - 80, 130, 110, 15, 15);
            g2.setColor(BLACK);
        } else {
            g2.setColor(GREEN_MATCHED);
        }
        g2.drawString("J1", leftScoreX + 35, scoreY - 45);
        g2.drawString(String.valueOf(scores[0]), leftScoreX + 40, scoreY);
        if (currentPlayer == 0) {
            g2.drawString("▶", leftScoreX, scoreY - 20);
        }

        // Score Joueur 2 à droite
        if (currentPlayer == 1) {
            g2.setColor(BLUE_LIGHT);
            g2.fillRoundRect(rightScoreX - 10, scoreY - 80, 130, 110, 15, 15);
            g2.setColor(BLACK);
        } else {
            g2.setColor(BLUE_MATCHED);
        }
        g2.drawString("J2", rightScoreX + 35, scoreY - 45);
        g2.drawString(String.valueOf(scores[1]), rightScoreX + 40, scoreY);
        if (currentPlayer == 1) {
            g2.drawString("◀", rightScoreX + 95, scoreY - 20);
        }

        g2.setFont(CARD_FONT);
        FontMetrics fm = g2.getFontMetrics();

        for (int i = 0; i < CARD_COUNT; i++) {
            int row = i / COLS;
            int col = i % COLS;

            int x = startX + col * (cardWidth + cardGap);
            int y = startY + row * (cardHeight + cardGap);

            boolean showFace = revealed[i] || matched[i];

            if (matched[i]) {
                // Couleur selon le joueur qui a trouvé la paire
                if (matchedBy[i] == 0) {
                    g2.setColor(GREEN_MATCHED);
                } else {
                    g2.setColor(BLUE_MATCHED);
                }
            } else if (showFace) {
                g2.setColor(CARD_FACE);
            } else {
                g2.setColor(CARD_BACK);
            }

            g2.fillRoundRect(x, y, cardWidth, cardHeight, 18, 18);
            g2.setColor(CARD_BORDER);
            g2.drawRoundRect(x, y, cardWidth, cardHeight, 18, 18);

            if (showFace) {
                String text = symbolFor(values[i]);
                int tx = x + (cardWidth - fm.stringWidth(text)) / 2;
                int ty = y + (cardHeight + fm.getAscent() - fm.getDescent()) / 2;
                g2.setColor(CARD_TEXT);
                g2.drawString(text, tx, ty);
            }

            if (currentPlayer == 0 && i == cursors[0]) {
                g2.setColor(YELLOW);
                g2.drawRoundRect(x - 3, y - 3, cardWidth + 6, cardHeight + 6, 20, 20);
                g2.drawRoundRect(x - 4, y - 4, cardWidth + 8, cardHeight + 8, 20, 20);
            }

            if (currentPlayer == 1 && i == cursors[1]) {
                g2.setColor(BLUE_LIGHT);
                g2.drawRoundRect(x - 3, y - 3, cardWidth + 6, cardHeight + 6, 20, 20);
                g2.drawRoundRect(x - 4, y - 4, cardWidth + 8, cardHeight + 8, 20, 20);
            }
        }
    }

    private void paintGameOver(Graphics2D g2) {
        g2.setColor(WHITE);
        g2.setFont(GAMEOVER_TITLE_FONT);
        String title = "FIN DE JEU";
        FontMetrics fm = g2.getFontMetrics();
        int x = (screenWidth - fm.stringWidth(title)) / 2;
        g2.drawString(title, x, 150);

        g2.setFont(GAMEOVER_SCORE_FONT);
        
        // Score J1 en vert
        g2.setColor(GREEN_MATCHED);
        String j1Score = "Joueur 1: " + scores[0];
        fm = g2.getFontMetrics();
        g2.drawString(j1Score, (screenWidth - fm.stringWidth(j1Score)) / 2, 350);
        
        // Score J2 en bleu
        g2.setColor(BLUE_MATCHED);
        String j2Score = "Joueur 2: " + scores[1];
        g2.drawString(j2Score, (screenWidth - fm.stringWidth(j2Score)) / 2, 420);

        String result;
        if (scores[0] > scores[1]) {
            result = "Joueur 1 gagne!";
            g2.setColor(GREEN_MATCHED);
        } else if (scores[1] > scores[0]) {
            result = "Joueur 2 gagne!";
            g2.setColor(BLUE_MATCHED);
        } else {
            result = "Egalité!";
            g2.setColor(YELLOW);
        }

        g2.drawString(result, (screenWidth - fm.stringWidth(result)) / 2, 550);

        g2.setFont(GAMEOVER_INFO_FONT);
        g2.setColor(WHITE);
        String info = "Appuyez sur A pour enregistrer votre score";
        fm = g2.getFontMetrics();
        g2.drawString(info, (screenWidth - fm.stringWidth(info)) / 2, screenHeight - 80);
    }

    private void paintEnterName(Graphics2D g2) {
        g2.setColor(WHITE);
        g2.setFont(GAMEOVER_TITLE_FONT);
        
        String title = (winner == 0) ? "JOUEUR 1 GAGNE!" : "JOUEUR 2 GAGNE!";
        FontMetrics fm = g2.getFontMetrics();
        int x = (screenWidth - fm.stringWidth(title)) / 2;
        
        g2.setColor(winner == 0 ? GREEN_MATCHED : BLUE_MATCHED);
        g2.drawString(title, x, 120);

        g2.setColor(WHITE);
        g2.setFont(MENU_FONT);
        String subtitle = "Entrez vos initiales:";
        fm = g2.getFontMetrics();
        g2.drawString(subtitle, (screenWidth - fm.stringWidth(subtitle)) / 2, 220);

        // Affichage des 3 lettres
        g2.setFont(INITIAL_FONT);
        fm = g2.getFontMetrics();
        int letterWidth = fm.charWidth('A') + 30;
        int totalWidth = letterWidth * 3;
        int startLetterX = (screenWidth - totalWidth) / 2;
        int letterY = 380;

        for (int i = 0; i < 3; i++) {
            int lx = startLetterX + i * letterWidth;
            
            // Fond de la lettre
            if (i == initialCursor) {
                g2.setColor(winner == 0 ? GREEN_MATCHED : BLUE_MATCHED);
                g2.fillRoundRect(lx - 10, letterY - 70, letterWidth - 10, 100, 15, 15);
                g2.setColor(WHITE);
            } else {
                g2.setColor(GRAY);
            }
            
            // La lettre
            String letter = String.valueOf(initials[i]);
            int letterDrawX = lx + (letterWidth - 10 - fm.stringWidth(letter)) / 2 - 10;
            g2.drawString(letter, letterDrawX, letterY);

            // Flèches pour la lettre active
            if (i == initialCursor) {
                g2.setFont(MENU_FONT);
                g2.setColor(YELLOW);
                int arrowX = lx + (letterWidth - 30) / 2 - 10;
                g2.drawString("▲", arrowX, letterY - 85);
                g2.drawString("▼", arrowX, letterY + 50);
                g2.setFont(INITIAL_FONT);
            }
        }

        g2.setFont(GAMEOVER_INFO_FONT);
        g2.setColor(WHITE);
        String info1 = "Haut/Bas: changer lettre | Gauche/Droite: déplacer";
        String info2 = "Appuyez sur A pour valider";
        fm = g2.getFontMetrics();
        g2.drawString(info1, (screenWidth - fm.stringWidth(info1)) / 2, screenHeight - 120);
        g2.drawString(info2, (screenWidth - fm.stringWidth(info2)) / 2, screenHeight - 70);

        // Affichage du score
        g2.setFont(SCORE_FONT);
        String scoreText = "Score: " + scores[winner];
        fm = g2.getFontMetrics();
        g2.setColor(YELLOW);
        g2.drawString(scoreText, (screenWidth - fm.stringWidth(scoreText)) / 2, 500);
    }
}
