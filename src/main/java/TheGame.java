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

    public static void main(String[] args) throws Exception {
        DefaultTerminalFactory terminalFactory = new DefaultTerminalFactory();
        Terminal terminal = terminalFactory.createTerminal();
        terminal.setCursorVisible(false);

        int oldPPosX, oldPPosY, timer = 0;

        List<Monster> monsterList = new ArrayList<Monster>();
        Player player = new Player();

        Item item = new Item();

        Level lev = new Level();
        newLevel(monsterList, terminal, player, item, lev);

        Thread bm = new Thread(new Music());
        bm.start();

        KeyStroke keyStroke = null;
        KeyType type;
        boolean continueReadingInput = true;


        while (continueReadingInput) {
            do {
                Thread.sleep(5);
                keyStroke = terminal.pollInput();
                timer++;

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
                                break;
                            } else {
                                monsterHitItem = false;
                            }
                            monster.setTimer(1);
                        }
                        monster.setTimer(monster.getTimer() + 1);
                    }
                } while (monsterHitItem);

                for (Monster monster : monsterList) {
                    if (timer % 1000 == 0 && timer < 10000) {
                        prepareForWall(item, player, monster, lev.getStartCol(), lev.getStartRow(), lev.getCols(), lev.getRows(), terminal);
                        lev.setStartCol(lev.getStartCol() + 1);
                        lev.setStartRow(lev.getStartRow() + 1);
                        lev.setRows(lev.getRows() - 1);
                        lev.setCols(lev.getCols() - 1);
                        drawWall(lev.getStartCol(), lev.getStartRow(), lev.getCols(), lev.getRows(), terminal);
                    }
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
            terminal.setCursorPosition(oldPPosX, oldPPosY);
            terminal.setForegroundColor(BLACK);
            terminal.putCharacter(' ');
            terminal.setCursorPosition(player.getPlayerX(), player.getPlayerY());
            terminal.setForegroundColor(CYAN);
            terminal.putCharacter(player.getPlayerChar());
            terminal.flush();

            if (player.getPlayerX() == item.getItemX() && player.getPlayerY() == item.getItemY()) {
                monsterList.remove(monsterList.size() - 1);
                item.setItemX(lev.getStartCol() + 1 + ThreadLocalRandom.current().nextInt(lev.getCols() - lev.getStartCol() - 2));
                item.setItemY(lev.getStartRow() + 1 + ThreadLocalRandom.current().nextInt(lev.getRows() - lev.getStartRow() - 2));
            }

            if (monsterList.size() < 1) {
                newLevel(monsterList, terminal, player, item, lev);
                timer = 0;
            }

        }

    }

    public static void drawWall(Level lev,/*int startCol, int startRow, int cols, int rows, */Terminal terminal) throws Exception {
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

    public static boolean isWall(int x, int y, int wallX, int wallY) {
        boolean isWall = false;
        if (x == wallX || y == wallY) {
            isWall = true;
        } else {
            isWall = false;
            //}
        }
        return isWall;
    }

    public static void monsterMovement(Player player, Monster monster, Item item, Terminal terminal) throws Exception {
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

    public static void prepareForWall(Item item, Player player, List<Monster> mList, int startCol, int startRow, int cols, int rows, Terminal terminal) throws Exception {
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

    public static void hitPlayer(Player p, List<Monster> mList) {
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

    public static boolean hitItem(Item i, Monster monster) {
        if (i.getItemX() == monster.getMonsterX() && i.getItemY() == monster.getMonsterY()) {
            return true;
        }
        return false;
    }

    public static void newLevel(List<Monster> monsterList, Terminal terminal, Player player, Item item, Level lev) throws Exception {
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

    public static boolean isMonsterCloserToPlayer(Monster monster, Player player, Item item) {
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
}
