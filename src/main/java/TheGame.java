import com.googlecode.lanterna.input.KeyStroke;
import com.googlecode.lanterna.input.KeyType;
import com.googlecode.lanterna.terminal.DefaultTerminalFactory;
import com.googlecode.lanterna.terminal.Terminal;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ThreadLocalRandom;

import static com.googlecode.lanterna.TextColor.ANSI.*;

import java.time.Duration;
import java.time.LocalTime;

public class TheGame {
    private static boolean continueReadingInput = true;

    public static void main(String[] args) throws Exception {
        DefaultTerminalFactory terminalFactory = new DefaultTerminalFactory();
        Terminal terminal = terminalFactory.createTerminal();
        terminal.setCursorVisible(false);

        int oldPPosX, oldPPosY, timer = 0;

        List<Monster> monsterList = new ArrayList<>();
        Player player = new Player();

        Item item = new Item();

        Sign[] signs = new Sign[2];
        Sign levelClearSign = new Sign("LevelClearSign.txt");
        Sign gameOverSign = new Sign("GameOverSign.txt");
        signs[0] = levelClearSign;
        signs[1] = gameOverSign;

        Level lev = new Level();
        newLevel(monsterList, terminal, player, item, lev);

        Thread bm = new Thread(new Music());
        bm.start();

        statusBar(terminal, player, lev);

        KeyStroke keyStroke;
        KeyType type;
        continueReadingInput = true;


        while (continueReadingInput) {
            statusBar(terminal, player, lev);
            do {
                Thread.sleep(5);
                keyStroke = terminal.pollInput();
                timer++;
                statusBar(terminal, player, lev);

                //move monster at interval
                boolean monsterHitItem = false;
                do {
                    for (Monster monster : monsterList) {
                        if (monster.getTimer() % monster.getSpeedTimer() == 0) {
                            monsterMovement(player, monster, item, terminal);
                            hitPlayer(player, monsterList);
                            if (hitItem(item, monster)) {
                                item.setItemX(1000);
                                item.setItemY(1000);
                                monsterHitItem = true;
                                monsterList.add(new Monster());
                                monsterList.get(monsterList.size() - 1).setMonsterX(lev.getStartCol() + 1 + ThreadLocalRandom.current().nextInt(lev.getCols() - lev.getStartCol() - 2));
                                monsterList.get(monsterList.size() - 1).setMonsterY(lev.getStartRow() + 1 + ThreadLocalRandom.current().nextInt(lev.getRows() - lev.getStartRow() - 2));
                                item.setItemX(lev.getStartCol() + 1 + ThreadLocalRandom.current().nextInt(lev.getCols() - lev.getStartCol() - 2));
                                item.setItemY(lev.getStartRow() + 1 + ThreadLocalRandom.current().nextInt(lev.getRows() - lev.getStartRow() - 2));
                                break;
                            } else {
                                monsterHitItem = false;
                            }
                            monster.setTimer(1);
                        }
                        monster.setTimer(monster.getTimer() + 1);
                    }
                } while (monsterHitItem);

                if (timer % 1000 == 0 && timer < 9000) {
                    prepareForWall(item, player, monsterList, lev.getStartCol(), lev.getStartRow(), lev.getCols(), lev.getRows(), terminal);
                    lev.setStartCol(lev.getStartCol() + 1);
                    lev.setStartRow(lev.getStartRow() + 1);
                    lev.setRows(lev.getRows() - 1);
                    lev.setCols(lev.getCols() - 1);
                    drawWall(lev, terminal);
                }
            }

            while (keyStroke == null);
            type = keyStroke.getKeyType();

            if (type == KeyType.Escape) {
                continueReadingInput = false;
                bm.stop();
                terminal.close();
            }

            oldPPosX = player.getPlayerX();
            oldPPosY = player.getPlayerY();


            switch (type) {
                case ArrowUp:
                    if (!isWall(player.getPlayerX(), player.getPlayerY() - 1, lev.getStartCol(), lev.getStartRow()))
                        player.setPlayerY(oldPPosY - 1);
                    break;
                case ArrowDown:
                    if (!isWall(player.getPlayerX(), player.getPlayerY() + 1, lev.getCols() - 1, lev.getRows() - 1))
                        player.setPlayerY(oldPPosY + 1);
                    break;
                case ArrowRight:
                    if (!isWall(player.getPlayerX() + 1, player.getPlayerY(), lev.getCols() - 1, lev.getRows() - 1))
                        player.setPlayerX(oldPPosX + 1);
                    break;
                case ArrowLeft:
                    if (!isWall(player.getPlayerX() - 1, player.getPlayerY(), lev.getStartCol(), lev.getStartRow()))
                        player.setPlayerX(oldPPosX - 1);
                    break;
            }
            hitPlayer(player, monsterList);
            if (player.getLives() == 0) {
                gameOver(player, terminal, lev, item, bm, signs, monsterList);
            }
            terminal.setCursorPosition(item.getItemX(), item.getItemY());
            terminal.setForegroundColor(RED);
            terminal.putCharacter(item.getItemChar());
            terminal.setCursorPosition(oldPPosX, oldPPosY);
            terminal.setForegroundColor(BLACK);
            terminal.putCharacter(' ');
            terminal.setCursorPosition(player.getPlayerX(), player.getPlayerY());
            terminal.setForegroundColor(CYAN);
            terminal.putCharacter(player.getPlayerChar());
            terminal.flush();

            if (player.getPlayerX() == item.getItemX() && player.getPlayerY() == item.getItemY()) {
                terminal.setForegroundColor(BLACK);
                terminal.setCursorPosition(monsterList.get(monsterList.size() - 1).getMonsterX(), monsterList.get(monsterList.size() - 1).getMonsterY());
                terminal.putCharacter(' ');
                terminal.flush();
                monsterList.remove(monsterList.size() - 1);
                item.setItemX(lev.getStartCol() + 1 + ThreadLocalRandom.current().nextInt(lev.getCols() - lev.getStartCol() - 2));
                item.setItemY(lev.getStartRow() + 1 + ThreadLocalRandom.current().nextInt(lev.getRows() - lev.getStartRow() - 2));
            }

            if (monsterList.size() < 1) {
                displayMessage(signs[0].getSignDesign(), terminal, 0);
                newLevel(monsterList, terminal, player, item, lev);
                timer = 0;
            }

        }

    }

    private static void drawWall(Level lev,/*int startCol, int startRow, int cols, int rows, */Terminal terminal) throws Exception {
        int startCol = lev.getStartCol();
        int startRow = lev.getStartRow();
        int cols = lev.getCols();
        int rows = lev.getRows();
        terminal.resetColorAndSGR();
        for (int i = startCol; i < cols; i++) {
            terminal.setCursorPosition(i, startRow);
            terminal.putCharacter('\u2588');
        }
        for (int i = startRow; i < rows; i++) {
            terminal.setCursorPosition(startCol, i);
            terminal.putCharacter('\u2588');
        }
        for (int i = 0; i < cols; i++) {
            terminal.setCursorPosition(i, rows - 1);
            terminal.putCharacter('\u2588');
        }
        for (int i = 0; i < rows; i++) {
            terminal.setCursorPosition(cols - 1, i);
            terminal.putCharacter('\u2588');
        }
        terminal.flush();
    }

    private static boolean isWall(int x, int y, int wallX, int wallY) {
        if (x == wallX || y == wallY) {
            return true;
        } else {
            return false;
        }
    }

    private static void monsterMovement(Player player, Monster monster, Item item, Terminal terminal) throws Exception {
        int pX = player.getPlayerX(), pY = player.getPlayerY();
        int mX = monster.getMonsterX(), mY = monster.getMonsterY();
        int iX = item.getItemX(), iY = item.getItemY();

        if (isMonsterCloserToPlayer(monster, player, item)) {
            if ((Math.abs(pX - mX)) >= (Math.abs(pY - mY))) {
                if (pX - mX > 0) monster.setMonsterX(monster.getMonsterX() + 1);
                else if (pX - mX < 0) monster.setMonsterX(monster.getMonsterX() - 1);
                else return;
            } else if ((Math.abs(pX - mX)) < (Math.abs(pY - mY))) {
                if (pY - mY > 0) monster.setMonsterY(monster.getMonsterY() + 1);
                else if (pY - mY < 0) monster.setMonsterY(monster.getMonsterY() - 1);
                else return;
            }
        } else {
            if ((Math.abs(iX - mX)) >= (Math.abs(iY - mY))) {
                if (iX - mX > 0) monster.setMonsterX(monster.getMonsterX() + 1);
                else if (iX - mX < 0) monster.setMonsterX(monster.getMonsterX() - 1);
                else return;
            } else if ((Math.abs(iX - mX)) < (Math.abs(iY - mY))) {
                if (iY - mY > 0) monster.setMonsterY(monster.getMonsterY() + 1);
                else if (iY - mY < 0) monster.setMonsterY(monster.getMonsterY() - 1);
                else return;
            }
        }

        terminal.setCursorPosition(mX, mY);
        terminal.setForegroundColor(BLACK);
        terminal.putCharacter(' ');
        terminal.setCursorPosition(monster.getMonsterX(), monster.getMonsterY());
        terminal.setForegroundColor(GREEN);
        terminal.putCharacter(monster.getMonsterChar());
        terminal.flush();
    }

    private static void prepareForWall(Item item, Player player, List<Monster> mList, int startCol, int startRow, int cols, int rows, Terminal terminal) throws Exception {
        int oldPX = player.getPlayerX(), oldPY = player.getPlayerY();
        int oldIX = item.getItemX(), oldIY = item.getItemY();
        //************move player when room shrinks******************
        if (player.getPlayerX() == startCol + 1) player.setPlayerX(player.getPlayerX() + 1);
        if (player.getPlayerX() == cols - 2) player.setPlayerX(player.getPlayerX() - 1);
        if (player.getPlayerY() == startRow + 1) player.setPlayerY(player.getPlayerY() + 1);
        if (player.getPlayerY() == rows - 2) player.setPlayerY(player.getPlayerY() - 1);
        terminal.setCursorPosition(oldPX, oldPY);
        terminal.putCharacter(' ');
        terminal.setCursorPosition(player.getPlayerX(), player.getPlayerY());
        terminal.setForegroundColor(CYAN);
        terminal.putCharacter(player.getPlayerChar());
        //************move monster when room shrinks****************
        for (Monster monster : mList) {
            int oldMX = monster.getMonsterX(), oldMY = monster.getMonsterY();
            if (monster.getMonsterX() == startCol + 1) monster.setMonsterX(monster.getMonsterX() + 1);
            if (monster.getMonsterX() == cols - 2) monster.setMonsterX(monster.getMonsterX() - 1);
            if (monster.getMonsterY() == startRow + 1) monster.setMonsterY(monster.getMonsterY() + 1);
            if (monster.getMonsterY() == rows - 2) monster.setMonsterY(monster.getMonsterY() - 1);
            terminal.setCursorPosition(oldMX, oldMY);
            terminal.putCharacter(' ');
            terminal.setCursorPosition(monster.getMonsterX(), monster.getMonsterY());
            terminal.setForegroundColor(GREEN);
            terminal.putCharacter(monster.getMonsterChar());
        }
        //************move item when room shrinks****************
        if (item.getItemX() == startCol + 1) item.setItemX(item.getItemX() + 1);
        if (item.getItemX() == cols - 2) item.setItemX(item.getItemX() - 1);
        if (item.getItemY() == startRow + 1) item.setItemY(item.getItemY() + 1);
        if (item.getItemY() == rows - 2) item.setItemY(item.getItemY() - 1);
        terminal.setCursorPosition(oldIX, oldIY);
        terminal.putCharacter(' ');
        terminal.setCursorPosition(item.getItemX(), item.getItemY());
        terminal.setForegroundColor(RED);
        terminal.putCharacter(item.getItemChar());
        terminal.flush();
    }

    private static void hitPlayer(Player p, List<Monster> mList) {
        for (Monster m : mList) {
            if (p.getPlayerX() == m.getMonsterX() && p.getPlayerY() == m.getMonsterY()) {
                long iTime = Duration.between(p.getHitTime(), LocalTime.now()).getSeconds();
                if (iTime > 5) {
                    p.setLives(p.getLives() - 1);
                    p.setHitTime(LocalTime.now());

                }
            }
        }
    }

    private static void gameOver(Player p, Terminal terminal, Level lev, Item item, Thread bm, Sign[] signs, List<Monster> monsterList) throws Exception {
        displayMessage(signs[1].getSignDesign(), terminal, 1);
        boolean hasResponded = false;
        KeyStroke keyStroke;
        char c;
        do {
            do {
                Thread.sleep(5);
                keyStroke = terminal.pollInput();
            } while (keyStroke == null);
            switch (keyStroke.getKeyType()) {
                case Character:
                    if (keyStroke.getCharacter() == 'y' || keyStroke.getCharacter() == 'n') hasResponded = true;
                    break;
                default:
                    break;
            }
        } while (!hasResponded);
        c = keyStroke.getCharacter();
        if (c == 'y') {
            monsterList.clear();
            p.setLives(3);
            lev.level = 0;
            newLevel(monsterList, terminal, p, item, lev);
        } else {
            continueReadingInput = false;
            terminal.close();
            bm.stop();
        }
    }

    private static boolean hitItem(Item i, Monster monster) {
        if (i.getItemX() == monster.getMonsterX() && i.getItemY() == monster.getMonsterY()) {
            return true;
        }
        return false;
    }

    private static void newLevel(List<Monster> monsterList, Terminal terminal, Player player, Item item, Level lev) throws Exception {
        lev.level++;
        terminal.clearScreen();
        lev.setCols(terminal.getTerminalSize().getColumns());
        lev.setRows(terminal.getTerminalSize().getRows());
        lev.setStartCol(0);
        lev.setStartRow(0);
        drawWall(lev,/*lev.getStartCol(), lev.getStartRow(), lev.getCols(), lev.getRows(),*/ terminal);
        for (int i = 0; i < lev.level; i++) {
            monsterList.add(new Monster());
        }

        for (Monster monster : monsterList) {
            monster.setMonsterX(lev.getStartCol() + 1 + ThreadLocalRandom.current().nextInt(lev.getCols() - lev.getStartCol() - 2));
            monster.setMonsterY(lev.getStartRow() + 1 + ThreadLocalRandom.current().nextInt(lev.getRows() - lev.getStartRow() - 2));
        }

        player.setPlayerX(lev.getStartCol() + 1 + ThreadLocalRandom.current().nextInt(lev.getCols() - lev.getStartCol() - 2));
        player.setPlayerY(lev.getStartRow() + 1 + ThreadLocalRandom.current().nextInt(lev.getRows() - lev.getStartRow() - 2));

        item.setItemX(lev.getStartCol() + 1 + ThreadLocalRandom.current().nextInt(lev.getCols() - lev.getStartCol() - 2));
        item.setItemY(lev.getStartRow() + 1 + ThreadLocalRandom.current().nextInt(lev.getRows() - lev.getStartRow() - 2));

        terminal.setCursorPosition(player.getPlayerX(), player.getPlayerY());
        terminal.setForegroundColor(CYAN);
        terminal.putCharacter(player.getPlayerChar());

        for (Monster monster : monsterList) {
            terminal.setCursorPosition(monster.getMonsterX(), monster.getMonsterY());
            terminal.setForegroundColor(GREEN);
            terminal.putCharacter(monster.getMonsterChar());
        }

        terminal.setCursorPosition(item.getItemX(), item.getItemY());
        terminal.setForegroundColor(RED);
        terminal.putCharacter(item.getItemChar());

        terminal.flush();
        System.out.println(monsterList.size());
    }

    private static boolean isMonsterCloserToPlayer(Monster monster, Player player, Item item) {
        int xDistToPlayer = Math.abs(monster.getMonsterX() - player.getPlayerX());
        int yDistToPlayer = Math.abs(monster.getMonsterY() - player.getPlayerY());
        int xDistToItem = Math.abs(monster.getMonsterX() - item.getItemX());
        int yDistToItem = Math.abs(monster.getMonsterY() - item.getItemY());
        double distToPlayer = Math.sqrt(xDistToPlayer * xDistToPlayer + yDistToPlayer * yDistToPlayer);
        double distToItem = Math.sqrt(xDistToItem * xDistToItem + yDistToItem * yDistToItem);

        if (distToPlayer < distToItem) {
            return true;
        }
        return false;
    }

    private static void statusBar(Terminal terminal, Player player, Level lev) throws Exception {
        terminal.setForegroundColor(WHITE);
        terminal.setBackgroundColor(BLUE);
        String statusBar = "SHRINKING ROOM - LEVEL:" + lev.level + " - LIVES LEFT:" + player.getLives();
        for (int i = 0; i < statusBar.length(); i++) {
            terminal.setCursorPosition(i + 5, 0);
            terminal.putCharacter(statusBar.charAt(i));
        }
        terminal.resetColorAndSGR();
        terminal.flush();
    }

    private static void displayMessage(char[][] signC, Terminal terminal, int endGame) throws Exception {
        for (int i = 0; i < signC.length; i++) {
            for (int j = 0; j < signC[i].length; j++) {
                char blockType;
                switch (signC[i][j]) {
                    case '0':
                        terminal.setBackgroundColor(WHITE);
                        blockType = ' ';
                        break;
                    case '1':
                        terminal.setForegroundColor(BLUE);
                        blockType = '\u2588';
                        break;
                    case '2':
                        terminal.setForegroundColor(RED);
                        blockType = '\u2588';
                        break;
                    default:
                        terminal.setForegroundColor(BLACK);
                        blockType = signC[i][j];
                        break;
                }
                terminal.setCursorPosition(28 + i, 5 + j);
                terminal.putCharacter(blockType);
            }
        }
        terminal.resetColorAndSGR();
        terminal.flush();
        if (endGame != 1) {
            boolean hasPressedC = false;
            KeyStroke keyStroke;
            char c;
            do {
                do {
                    Thread.sleep(5);
                    keyStroke = terminal.pollInput();
                } while (keyStroke == null);
                switch (keyStroke.getKeyType()) {
                    case Character:
                        if (keyStroke.getCharacter() == 'c') hasPressedC = true;
                        break;
                    default:
                        break;
                }
            } while (!hasPressedC);
        }
    }
}
