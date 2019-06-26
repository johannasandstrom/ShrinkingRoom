import com.googlecode.lanterna.input.KeyStroke;
import com.googlecode.lanterna.input.KeyType;
import com.googlecode.lanterna.terminal.DefaultTerminalFactory;
import com.googlecode.lanterna.terminal.Terminal;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ThreadLocalRandom;

import static com.googlecode.lanterna.TextColor.ANSI.*;

public class TheGame {

    public static void main(String[] args) throws Exception {
        DefaultTerminalFactory terminalFactory = new DefaultTerminalFactory();
        Terminal terminal = terminalFactory.createTerminal();
        terminal.setCursorVisible(false);

        int cols = 0, rows = 0, startCol = 0, startRow = 0;
        int timer = 0, oldPPosX = 0, oldPPosY = 0;

        int lives = 3, level = 0;

        List<Monster> monsterList = new ArrayList<>();
        Player player = new Player();
        Item item = new Item();
        Level lev = new Level();

        newLevel(level, monsterList, terminal, player, item, lev);

        KeyStroke keyStroke = null;
        KeyType type;
        boolean continueReadingInput = true;

        
        while (continueReadingInput) {
            do {
                Thread.sleep(5);
                keyStroke = terminal.pollInput();
                timer++;

                //move monster at interval
                for (Monster monster : monsterList) {
                    if (monster.getTimer() % monster.getSpeedTimer() == 0) {
                        monsterMovement(player, monster, terminal);
                        monster.setTimer(1);
                    }
                    monster.setTimer(monster.getTimer() + 1);


                    if (timer % 1000 == 0 && timer < 10000) {
                        prepareForWall(player, monster, startCol, startRow, cols, rows, terminal);
                        drawWall(++startCol, ++startRow, --cols, --rows, terminal);
                    }
                }
            } while (keyStroke == null);
            type = keyStroke.getKeyType();

            if (type == KeyType.Escape) {
                continueReadingInput = false;
                terminal.close();
            }

            oldPPosX = player.getPlayerX();
            oldPPosY = player.getPlayerY();

            switch (type) {
                case ArrowUp:
                    if (!isWall(player.getPlayerX(), player.getPlayerY() - 1, startCol, startRow))
                        player.setPlayerY(oldPPosY - 1);
                    break;
                case ArrowDown:
                    if (!isWall(player.getPlayerX(), player.getPlayerY() + 1, cols - 1, rows - 1))
                        player.setPlayerY(oldPPosY + 1);
                    break;
                case ArrowRight:
                    if (!isWall(player.getPlayerX() + 1, player.getPlayerY(), cols - 1, rows - 1))
                        player.setPlayerX(oldPPosX + 1);
                    break;
                case ArrowLeft:
                    if (!isWall(player.getPlayerX() - 1, player.getPlayerY(), startCol, startRow))
                        player.setPlayerX(oldPPosX - 1);
                    break;
            }

            terminal.setCursorPosition(oldPPosX, oldPPosY);
            terminal.setForegroundColor(BLACK);
            terminal.putCharacter(' ');
            terminal.setCursorPosition(player.getPlayerX(), player.getPlayerY());
            terminal.setForegroundColor(CYAN);
            terminal.putCharacter(player.getPlayerChar());
            terminal.flush();

            if (player.getPlayerX() == item.getItemX() && player.getPlayerY() == item.getItemY()) {
                monsterList.remove(monsterList.size() - 1);
            }

            if (monsterList.size() < 1) {
                newLevel(level, monsterList, terminal, player, item, lev);
            }

        }

    }

    public static void drawWall(int startCol, int startRow, int cols, int rows, Terminal terminal) throws Exception {
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

    public static void monsterMovement(Player player, Monster monster, Terminal terminal) throws Exception {
        int pX = player.getPlayerX(), pY = player.getPlayerY();
        int mX = monster.getMonsterX(), mY = monster.getMonsterY();
        if ((Math.abs(pX - mX)) >= (Math.abs(pY - mY))) {
            if (pX - mX > 0) monster.setMonsterX(monster.getMonsterX() + 1);
            else if (pX - mX < 0) monster.setMonsterX(monster.getMonsterX() - 1);
            else return;
        } else if ((Math.abs(pX - mX)) < (Math.abs(pY - mY))) {
            if (pY - mY > 0) monster.setMonsterY(monster.getMonsterY() + 1);
            else if (pY - mY < 0) monster.setMonsterY(monster.getMonsterY() - 1);
            else return;
        }
        terminal.setCursorPosition(mX, mY);
        terminal.setForegroundColor(BLACK);
        terminal.putCharacter('X');
        terminal.setCursorPosition(monster.getMonsterX(), monster.getMonsterY());
        terminal.setForegroundColor(GREEN);
        terminal.putCharacter(monster.getMonsterChar());
        terminal.flush();

    }

    public static void prepareForWall(Player player, Monster monster, int startCol, int startRow, int cols, int rows, Terminal terminal) throws Exception {
        int oldPX = player.getPlayerX(), oldPY = player.getPlayerY();
        int oldMX = monster.getMonsterX(), oldMY = monster.getMonsterY();
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
        terminal.flush();
        //************move monster when room shrinks****************
        if (monster.getMonsterX() == startCol + 1) monster.setMonsterX(monster.getMonsterX() + 1);
        if (monster.getMonsterX() == cols - 2) monster.setMonsterX(monster.getMonsterX() - 1);
        if (monster.getMonsterY() == startRow + 1) monster.setMonsterY(monster.getMonsterY() + 1);
        if (monster.getMonsterY() == rows - 2) monster.setMonsterY(monster.getMonsterY() - 1);
        terminal.setCursorPosition(oldMX, oldMY);
        terminal.putCharacter(' ');
        terminal.setCursorPosition(monster.getMonsterX(), monster.getMonsterY());
        terminal.setForegroundColor(RED);
        terminal.putCharacter(monster.getMonsterChar());
        terminal.flush();
    }

    public static void newLevel(int level, List<Monster> monsterList, Terminal terminal, Player player, Item item, Level lev) throws Exception {
        level++;
        terminal.clearScreen();
        lev.setCols(terminal.getTerminalSize().getColumns());
        lev.setRows(terminal.getTerminalSize().getRows());
        lev.setStartCol(0);
        lev.setStartRow(0);
        drawWall(lev.getStartCol(), lev.getStartRow(), lev.getCols(), lev.getRows(), terminal);
        for (int i = 0; i < level; i++) {
            monsterList.add(new Monster());
        }

        for(Monster monster : monsterList){
            monster.setMonsterX(lev.getStartCol() + ThreadLocalRandom.current().nextInt(lev.getCols() - lev.getStartCol()));
            monster.setMonsterY(lev.getStartRow() + ThreadLocalRandom.current().nextInt(lev.getRows() - lev.getStartRow()));
        }

        player.setPlayerX(lev.getStartCol() + ThreadLocalRandom.current().nextInt(lev.getCols() - lev.getStartCol()));
        player.setPlayerY(lev.getStartRow() + ThreadLocalRandom.current().nextInt(lev.getRows() - lev.getStartRow()));

        item.setItemX(lev.getStartCol() + ThreadLocalRandom.current().nextInt(lev.getCols() - lev.getStartCol()));
        item.setItemY(lev.getStartRow() + ThreadLocalRandom.current().nextInt(lev.getRows() - lev.getStartRow()));

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
    }

}

