import com.googlecode.lanterna.TextColor;
import com.googlecode.lanterna.input.KeyStroke;
import com.googlecode.lanterna.input.KeyType;
import com.googlecode.lanterna.terminal.DefaultTerminalFactory;
import com.googlecode.lanterna.terminal.Terminal;

import static com.googlecode.lanterna.TextColor.ANSI.*;

public class TheGame {

    public static void main(String[] args) throws Exception {
        DefaultTerminalFactory terminalFactory = new DefaultTerminalFactory();
        Terminal terminal = terminalFactory.createTerminal();
        terminal.setCursorVisible(false);

        int cols = terminal.getTerminalSize().getColumns();
        int rows = terminal.getTerminalSize().getRows();
        int startCol = 0, startRow = 0;
        drawWall(startCol, startRow, cols, rows, terminal);
        int timer = 0;

        Player player = new Player(10, 10);
        int oldPPosX = player.getPlayerX();
        int oldPPosY = player.getPlayerY();

        Monster monster = new Monster(50, 20);
        int oldMPosX = monster.getMonsterX();
        int oldMPosY = monster.getMonsterY();

        KeyStroke keyStroke = null;
        KeyType type;
        boolean continueReadingInput = true;

        terminal.setCursorPosition(player.getPlayerX(), player.getPlayerY());
        terminal.setForegroundColor(CYAN);
        terminal.putCharacter(player.getPlayerChar());

        terminal.setCursorPosition(monster.getMonsterX(), monster.getMonsterY());
        terminal.setForegroundColor(RED);
        terminal.putCharacter(monster.getMonsterChar());

        terminal.flush();
        while (continueReadingInput) {

            oldMPosX = monster.getMonsterX();
            oldMPosY = monster.getMonsterY();

            do {
                Thread.sleep(5);
                keyStroke = terminal.pollInput();
                //monsternas rörelser - två gånger i sekunden?

                //move enemy at interval
                if (monster.getTimer() % 80 == 0) {
                    monsterMovement(player, monster, terminal);
                    monster.setTimer(1);
                }
                monster.setTimer(monster.getTimer() + 1);

                //}
//                //Kollar om det börjar närma sig tid för väggen att växa - förslag att något ljud varnar då
//                for(int i = 50; i > 0; i = i - 10) {
//                    if ((timer + i) % 1000 == 0){
//                        //makeSound
//                    }
//                }
                timer++;
                if (timer % 1000 == 0 && timer < 10000) {
                    if (monster.getMonsterX() == startCol + 1) monster.setMonsterX(monster.getMonsterX() + 1);
                    if (monster.getMonsterX() == cols - 2) monster.setMonsterX(monster.getMonsterX() - 1);
                    if (monster.getMonsterY() == startRow + 1) monster.setMonsterY(monster.getMonsterY() + 1);
                    if (monster.getMonsterY() == rows - 2) monster.setMonsterY(monster.getMonsterY() - 1);
                    terminal.setCursorPosition(oldPPosX, oldPPosY);
                    terminal.putCharacter(' ');
                    terminal.setCursorPosition(monster.getMonsterX(), monster.getMonsterY());
                    terminal.setForegroundColor(RED);
                    terminal.putCharacter(monster.getMonsterChar());
                    terminal.flush();
                    terminal.resetColorAndSGR();
                    drawWall(++startCol, ++startRow, --cols, --rows, terminal);
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

            if (isWall(player.getPlayerX(), player.getPlayerY(), startCol, startRow) || isWall(player.getPlayerX(), player.getPlayerY(), cols - 1, rows - 1)) {
                //Game over - text över skärmen!!! + Spräng-ljud
            }
//            // Is player alive?
//            for (Position monster : monsters) {
//                if (monster.x == player.x && monster.y == player.y) {
//                    continueReadingInput = false;
//                    terminal.bell(); // primitive death sound!
//                    System.out.println("Game Over!");
//                }
//            }

        }

    }

    public static void drawWall(int startCol, int startRow, int cols, int rows, Terminal terminal) throws Exception {
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

        //for (int i = 0 ; i < wallBits.size() ; i++) System.out.println(wallBits.get(i).toString());

        //for(int i = 0; i < wallBits.size(); i++) {
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
        if ((Math.abs(pX - mX)) > (Math.abs(pY - mY))) {
            if (pX - mX > 0) monster.setMonsterX(monster.getMonsterX() + 1);
            else if (pX - mX < 0) monster.setMonsterX(monster.getMonsterX() - 1);
            else return;
        } else if ((Math.abs(pX - mX)) < (Math.abs(pY - mY))) {
            if (pY - mY > 0) monster.setMonsterY(monster.getMonsterY() + 1);
            else if (pY - mY < 0) monster.setMonsterY(monster.getMonsterY() - 1);
            else return;
        }
        terminal.setCursorPosition(mX,mY);
        terminal.setForegroundColor(BLACK);
        terminal.putCharacter('X');
        terminal.setCursorPosition(monster.getMonsterX(), monster.getMonsterY());
        terminal.setForegroundColor(RED);
        terminal.putCharacter(monster.getMonsterChar());
        terminal.flush();

    }

}

