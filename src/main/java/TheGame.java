import com.googlecode.lanterna.TextColor;
import com.googlecode.lanterna.input.KeyStroke;
import com.googlecode.lanterna.input.KeyType;
import com.googlecode.lanterna.terminal.DefaultTerminalFactory;
import com.googlecode.lanterna.terminal.Terminal;

import static com.googlecode.lanterna.TextColor.ANSI.CYAN;

public class TheGame {

    public static void main(String[] args) throws Exception {
        DefaultTerminalFactory terminalFactory = new DefaultTerminalFactory();
        Terminal terminal = terminalFactory.createTerminal();
        terminal.setCursorVisible(false);

        int timer = 0;

        int cols = terminal.getTerminalSize().getColumns();
        int rows = terminal.getTerminalSize().getRows();
        int startCol = 0, startRow = 0;
        drawWall(startCol, startRow, cols, rows, terminal);

        Player player = new Player(10, 10);
        int oldPPosX = player.getPlayerX();
        int oldPPosY = player.getPlayerY();

        KeyStroke keyStroke = null;
        KeyType type;
        boolean continueReadingInput = true;

        terminal.setCursorPosition(player.getPlayerX(), player.getPlayerY());
        terminal.setForegroundColor(CYAN);
        terminal.putCharacter(player.getPlayerChar());
        terminal.flush();
        while (continueReadingInput) {

            do {
                Thread.sleep(5);
                keyStroke = terminal.pollInput();
                timer++;
//                for(int i = 50; i > 0; i = i - 10) {
//                    if ((timer + i) % 1000 == 0){
//                        //makeSound
//                    }
//                }
                if (timer % 1000 == 0 && timer < 9000) {
                    terminal.resetColorAndSGR();
                    drawWall(++startCol,++startRow,--cols,--rows,terminal);
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
                    if (!isWall(player.getPlayerX(),player.getPlayerY()-1,startCol, startRow))
                        player.setPlayerY(oldPPosY-1);
                    break;
                case ArrowDown:
                    if (!isWall(player.getPlayerX(),player.getPlayerY()+1,cols-1, rows-1))
                        player.setPlayerY(oldPPosY+1);
                    break;
                case ArrowRight:
                    if (!isWall(player.getPlayerX()+1,player.getPlayerY(),cols-1, rows-1))
                        player.setPlayerX(oldPPosX+1);
                    break;
                case ArrowLeft:
                    if (!isWall(player.getPlayerX()-1,player.getPlayerY(),startCol, startRow))
                        player.setPlayerX(oldPPosX-1);
                    break;
            }

            terminal.setCursorPosition(oldPPosX, oldPPosY);
            terminal.putCharacter(' ');
            terminal.setCursorPosition(player.getPlayerX(), player.getPlayerY());
            terminal.setForegroundColor(CYAN);
            terminal.putCharacter(player.getPlayerChar());
            terminal.flush();

            if(isWall(player.getPlayerX(), player.getPlayerY(), startCol, startRow) || isWall(player.getPlayerX(), player.getPlayerY(), cols - 1, rows - 1)){
                //Game over - text över skärmen!!! + Spräng-ljud
            }
//            // Handle monsters
//            for (Position monster : monsters) {
//                terminal.setCursorPosition(monster.x, monster.y);
//                terminal.putCharacter(' ');
//
//                if (player.x > monster.x) {
//                    monster.x++;
//                }
//                else if (player.x < monster.x) {
//                    monster.x--;
//                }
//                if (player.y > monster.y) {
//                    monster.y++;
//                }
//                else if (player.y < monster.y) {
//                    monster.y--;
//                }
//
//                terminal.setCursorPosition(monster.x, monster.y);
//                terminal.putCharacter('X');
//            }
//
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

}

