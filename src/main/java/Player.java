public class Player extends Thread{
    private int playerX;
    private int playerY;
    private char playerChar = '\u263B';

    public Player(int x, int y) {
        playerX = x;
        playerY = y;
    }

    public int getPlayerX() {
        return playerX;
    }

    public void setPlayerX(int playerX) {
        this.playerX = playerX;
    }

    public int getPlayerY() {
        return playerY;
    }

    public void setPlayerY(int playerY) {
        this.playerY = playerY;
    }

    public char getPlayerChar() {
        return playerChar;
    }
}
