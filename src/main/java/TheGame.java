import com.googlecode.lanterna.input.KeyStroke;
import com.googlecode.lanterna.input.KeyType;
import com.googlecode.lanterna.terminal.DefaultTerminalFactory;
import com.googlecode.lanterna.terminal.Terminal;

import static com.googlecode.lanterna.TextColor.ANSI.*;

import java.time.Duration;
import java.time.LocalTime;

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
        LocalTime time = LocalTime.now().plus(Duration.ofSeconds(4));

        Player player = new Player(10, 10);
        int oldPPosX;
        int oldPPosY;

        Monster monster = new Monster(50, 20);
        Item star = new Item('\u2605');

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

            do {
                Thread.sleep(5);
                keyStroke = terminal.pollInput();

                //move monster at interval
                if (monster.getTimer() % monster.getSpeedTimer() == 0) {
                    monsterMovement(player, monster, terminal);
                    hitPlayer(player, monster);
                    monster.setTimer(1);
                }
                monster.setTimer(monster.getTimer() + 1);

                timer++;
                if (timer % 1000 == 0 && timer < 10000) {
                    prepareForWall(player, monster, startCol, startRow, cols, rows, terminal);
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
            hitPlayer(player, monster);
            terminal.setCursorPosition(oldPPosX, oldPPosY);
            terminal.setForegroundColor(BLACK);
            terminal.putCharacter(' ');
            terminal.setCursorPosition(player.getPlayerX(), player.getPlayerY());
            terminal.setForegroundColor(CYAN);
            terminal.putCharacter(player.getPlayerChar());
            terminal.flush();

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
        terminal.setForegroundColor(RED);
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

    public static void hitPlayer(Player p, Monster m) {
        if (p.getPlayerX() == m.getMonsterX() && p.getPlayerY() == m.getMonsterY()) {
            long iTime = Duration.between(p.getHitTime(), LocalTime.now()).getSeconds();
            if (iTime > 5) {
                p.setLives(p.getLives() - 1);
                System.out.print("DEAD! Lives left: " + p.getLives());
                p.setHitTime(LocalTime.now());
            }
        }
    }

}

