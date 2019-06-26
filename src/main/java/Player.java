import java.time.LocalTime;

public class Player extends Thread{
    private int playerX;
    private int playerY;
    private char playerChar = '\u263B';
    private int lives;
    private LocalTime hitTime;


    public Player(int x, int y) {
        playerX = x;
        playerY = y;
        lives=3;
        hitTime=LocalTime.now();
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

    public int getLives() {
        return lives;
    }

    public void setLives(int lives) {
        this.lives = lives;
    }

    public LocalTime getHitTime() {
        return hitTime;
    }

    public void setHitTime(LocalTime hitTime) {
        this.hitTime = hitTime;
    }
}
