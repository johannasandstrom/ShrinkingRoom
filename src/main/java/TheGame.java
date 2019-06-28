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
        //Skapar spelfönster
        DefaultTerminalFactory terminalFactory = new DefaultTerminalFactory();
        Terminal terminal = terminalFactory.createTerminal();
        terminal.setCursorVisible(false);

        //old.Pos. används för att rita över inaktuella chars
        int oldPPosX, oldPPosY, timer = 0;

        //skapar objekt monster, spelare och item
        List<Monster> monsterList = new ArrayList<>();
        Player player = new Player();
        Item item = new Item();

        //skapar de skyltar som visas upp vid ny level, samt game over
        Sign[] signs = new Sign[2];
        Sign levelClearSign = new Sign("LevelClearSign.txt");
        Sign gameOverSign = new Sign("GameOverSign.txt");
        signs[0] = levelClearSign;
        signs[1] = gameOverSign;

        //skapar ljudeffekterna
        Sfx clearLevel = new Sfx("clearLevel.wav");
        Sfx pHeart = new Sfx("pHeart.wav");
        Sfx eHeart = new Sfx("eHeart.wav");
        Sfx lifeLost = new Sfx("lifeLost.wav");
        Sfx gameOver = new Sfx("gameOver.wav");

        //Thread hanterar bakgrundsmusiken
        Music backgroundmusic = new Music("backgroundmusic.wav");
        Thread bm = new Thread(backgroundmusic);
        bm.start();

        //initiera första level
        Level lev = new Level();
        newLevel(monsterList, terminal, player, item, lev, clearLevel);

        statusBar(terminal, player, lev);

        //objekt som hanterar user input
        KeyStroke keyStroke;
        KeyType type;
        continueReadingInput = true;

        //körs tills man väljer att avsluta spelet
        while (continueReadingInput) {
            statusBar(terminal, player, lev);
            do {
                //låter processorn vila, kontroll av input endast var 5:e millisekund
                Thread.sleep(5);
                hitPlayer(player, monsterList, lifeLost);
                keyStroke = terminal.pollInput();
                timer++;
                statusBar(terminal, player, lev);

                //flyttar monstren, hanterar om monster når item eller player via resp. metod
                //set timer gör det möjligt att öka hastigheten hos monstren
                boolean monsterHitItem = false;
                int checkDeath = 0;
                do {
                    for (Monster monster : monsterList) {
                        if (monster.getTimer() % monster.getSpeedTimer() == 0) {
                            monsterMovement(player, monster, item, terminal);
                            if (!hitPlayer(player, monsterList, lifeLost)) checkDeath=1;
                            if (hitItemE(item, monster)) {
                                Thread sfx = new Thread(eHeart);
                                sfx.start();
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
                    if (checkDeath==1)gameOver(gameOver, player, terminal, lev, item, bm, signs, monsterList, clearLevel);
                } while (monsterHitItem);

                //stoppar muren från att växa efter x antal omgångar
                if (timer % 1000 == 0 && timer < 9000) {
                    prepareForWall(item, player, monsterList, lev.getStartCol(), lev.getStartRow(), lev.getCols(), lev.getRows(), terminal);
                    lev.setStartCol(lev.getStartCol() + 1);
                    lev.setStartRow(lev.getStartRow() + 1);
                    lev.setRows(lev.getRows() - 1);
                    lev.setCols(lev.getCols() - 1);

                    drawWall(lev, terminal);

                    //kollar om spelaren nått item - minskar i så fall antalet monster med 1
                    if (hitItemP(item, player)) {
                        pHitHeart(pHeart, terminal, monsterList, item, lev);
                    }
                    //om antalet monster kvar är 0 går man vidare till nästa level
                    if (monsterList.size() < 1) {
                        displayMessage(signs[0].getSignDesign(), terminal, 0);
                        newLevel(monsterList, terminal, player, item, lev, clearLevel);
                        timer = 0;
                    }

                }
            }

            //bestämmer vad som händer baserat på vilken tangent användaren trycker på - styr med piltangenter, avslutar med esc
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
                    if (isNotWall(player.getPlayerX(), player.getPlayerY() - 1, lev.getStartCol(), lev.getStartRow()))
                        player.setPlayerY(oldPPosY - 1);
                    break;
                case ArrowDown:
                    if (isNotWall(player.getPlayerX(), player.getPlayerY() + 1, lev.getCols() - 1, lev.getRows() - 1))
                        player.setPlayerY(oldPPosY + 1);
                    break;
                case ArrowRight:
                    if (isNotWall(player.getPlayerX() + 1, player.getPlayerY(), lev.getCols() - 1, lev.getRows() - 1))
                        player.setPlayerX(oldPPosX + 1);
                    break;
                case ArrowLeft:
                    if (isNotWall(player.getPlayerX() - 1, player.getPlayerY(), lev.getStartCol(), lev.getStartRow()))
                        player.setPlayerX(oldPPosX - 1);
                    break;
            }

            //kollar om spelaren träffats av monstret, och om det i så fall är game over
            hitPlayer(player, monsterList, lifeLost);
            if (player.getLives() == 0) {
                gameOver(gameOver, player, terminal, lev, item, bm, signs, monsterList, clearLevel);
            }

            //ritar ut item och player - suddar gammalt
            terminal.setCursorPosition(oldPPosX, oldPPosY);
            terminal.setForegroundColor(BLACK);
            terminal.putCharacter(' ');
            terminal.setCursorPosition(item.getItemX(), item.getItemY());
            terminal.setForegroundColor(RED);
            terminal.putCharacter(item.getItemChar());
            terminal.setCursorPosition(player.getPlayerX(), player.getPlayerY());
            if (Duration.between(player.getHitTime(), LocalTime.now()).getSeconds() < 5) {
                terminal.setForegroundColor(WHITE);
            } else {
                terminal.setForegroundColor(CYAN);
            }
            terminal.putCharacter(player.getPlayerChar());
            terminal.flush();

            //kollar om spelaren nått item - minskar i så fall antalet monster med 1
            if (hitItemP(item, player)) {
                pHitHeart(pHeart, terminal, monsterList, item, lev);
            }

            //om antalet monster kvar är 0 går man vidare till nästa level
            if (monsterList.size() < 1) {
                displayMessage(signs[0].getSignDesign(), terminal, 0);
                newLevel(monsterList, terminal, player, item, lev, clearLevel);
                timer = 0;
            } //end if

        } //end while reading Input

    } //end main

    //metod som ritar upp väggen allt eftersom den krymper
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

    //kollar om en viss position är upptagen av väggen
    private static boolean isNotWall(int x, int y, int wallX, int wallY) {
        return !(x == wallX || y == wallY);
    }

    //hanterar monsternas rörelser - ska de röra sig mot item eller spelare? + ritar upp monstrets nya pos, tar bort den gamla
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

    //flyttar på spelare, monster och item om de är i vägen när väggen ska växa
    private static void prepareForWall(Item item, Player player, List<Monster> mList, int startCol, int startRow, int cols, int rows, Terminal terminal) throws Exception {
        int oldPX = player.getPlayerX(), oldPY = player.getPlayerY();
        int oldIX = item.getItemX(), oldIY = item.getItemY();
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
        //************move player when room shrinks******************
        if (player.getPlayerX() == startCol + 1) player.setPlayerX(player.getPlayerX() + 1);
        if (player.getPlayerX() == cols - 2) player.setPlayerX(player.getPlayerX() - 1);
        if (player.getPlayerY() == startRow + 1) player.setPlayerY(player.getPlayerY() + 1);
        if (player.getPlayerY() == rows - 2) player.setPlayerY(player.getPlayerY() - 1);
        terminal.setCursorPosition(oldPX, oldPY);
        terminal.putCharacter(' ');
        terminal.setCursorPosition(player.getPlayerX(), player.getPlayerY());
        if (Duration.between(player.getHitTime(), LocalTime.now()).getSeconds() < 5) {
            terminal.setForegroundColor(WHITE);
        } else {
            terminal.setForegroundColor(CYAN);
        }
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
        terminal.flush();
    }

    //kollar om spelaren är på samma plats som monstret - minskar i så fall liv med 1
    private static boolean hitPlayer(Player p, List<Monster> mList, Sfx lifeLost) {
        for (Monster m : mList) {
            if (p.getPlayerX() == m.getMonsterX() && p.getPlayerY() == m.getMonsterY()) {
                long iTime = Duration.between(p.getHitTime(), LocalTime.now()).getSeconds();
                if (iTime > 5) {
                    p.setLives(p.getLives() - 1);
                    p.setHitTime(LocalTime.now());
                    if (p.getLives() > 0) {
                        Thread sfx = new Thread(lifeLost);
                        sfx.start();
                    } else return false;
                }
            }
        }
        return true;
    }

    //när man dör
    private static void gameOver(Sfx gameOver, Player p, Terminal terminal, Level lev, Item item, Thread bm, Sign[] signs, List<Monster> monsterList, Sfx clearLevel) throws Exception {
        displayMessage(signs[1].getSignDesign(), terminal, 1);
        statusBar(terminal, p, lev);
        Thread sfx = new Thread(gameOver);
        sfx.start();
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
            newLevel(monsterList, terminal, p, item, lev, clearLevel);
        } else {
            continueReadingInput = false;
            terminal.close();
            bm.stop();
        }
    }

    //kollar om monstret nått item
    private static boolean hitItemE(Item i, Monster monster) {
        return (i.getItemX() == monster.getMonsterX() && i.getItemY() == monster.getMonsterY());
    }

    //kollar om spelaren nått item
    private static boolean hitItemP(Item i, Player player) {
        return (i.getItemX() == player.getPlayerX() && i.getItemY() == player.getPlayerY());
    }

    //hanterar när spelaren når hjärtat
    private static void pHitHeart(Sfx pHeart, Terminal terminal, List<Monster> monsterList, Item item, Level lev) throws Exception {
        Thread sfx = new Thread(pHeart);
        sfx.start();
        terminal.setForegroundColor(BLACK);
        terminal.setCursorPosition(monsterList.get(monsterList.size() - 1).getMonsterX(), monsterList.get(monsterList.size() - 1).getMonsterY());
        terminal.putCharacter(' ');
        terminal.flush();
        monsterList.remove(monsterList.size() - 1);
        item.setItemX(lev.getStartCol() + 1 + ThreadLocalRandom.current().nextInt(lev.getCols() - lev.getStartCol() - 2));
        item.setItemY(lev.getStartRow() + 1 + ThreadLocalRandom.current().nextInt(lev.getRows() - lev.getStartRow() - 2));
    }

    //initierar ny nivå
    private static void newLevel(List<Monster> monsterList, Terminal terminal, Player player, Item item, Level lev, Sfx clearLevel) throws Exception {
        lev.level++;
        terminal.clearScreen();
        lev.setCols(terminal.getTerminalSize().getColumns());
        lev.setRows(terminal.getTerminalSize().getRows());
        lev.setStartCol(0);
        lev.setStartRow(0);
        drawWall(lev, terminal);
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

        terminal.setCursorPosition(item.getItemX(), item.getItemY());
        terminal.setForegroundColor(RED);
        terminal.putCharacter(item.getItemChar());

        terminal.setCursorPosition(player.getPlayerX(), player.getPlayerY());
        if (lev.level == 1) terminal.setForegroundColor(WHITE);
        else terminal.setForegroundColor(CYAN);
        terminal.putCharacter(player.getPlayerChar());

        for (Monster monster : monsterList) {
            terminal.setCursorPosition(monster.getMonsterX(), monster.getMonsterY());
            terminal.setForegroundColor(GREEN);
            terminal.putCharacter(monster.getMonsterChar());
        }

        terminal.flush();
        Thread sfxClear = new Thread(clearLevel);
        sfxClear.start();
    }

    //kollar om item eller player är närmast ett monster
    private static boolean isMonsterCloserToPlayer(Monster monster, Player player, Item item) {
        int xDistToPlayer = Math.abs(monster.getMonsterX() - player.getPlayerX());
        int yDistToPlayer = Math.abs(monster.getMonsterY() - player.getPlayerY());
        int xDistToItem = Math.abs(monster.getMonsterX() - item.getItemX());
        int yDistToItem = Math.abs(monster.getMonsterY() - item.getItemY());
        double distToPlayer = Math.sqrt(xDistToPlayer * xDistToPlayer + yDistToPlayer * yDistToPlayer);
        double distToItem = Math.sqrt(xDistToItem * xDistToItem + yDistToItem * yDistToItem);

        return (distToPlayer < distToItem);
    }

    //hanterar vad som ska skrivas på status bar
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

    //hanterar vilket meddelande som skrivs ut på skärmen (ny level? game over?)
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
