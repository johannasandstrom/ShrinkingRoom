public class Monster {
    private int monsterX;
    private int monsterY;
    private int timer=1;
    private int speedTimer = 60;
//    private char monsterChar = '\u2620';
    private char monsterChar = '\u2622';

    public Monster(int x, int y) {
        monsterX = x;
        monsterY = y;
    }

    public int getMonsterX() {
        return monsterX;
    }

    public void setMonsterX(int monsterX) {
        this.monsterX = monsterX;
    }

    public int getMonsterY() {
        return monsterY;
    }

    public void setMonsterY(int monsterY) {
        this.monsterY = monsterY;
    }

    public char getMonsterChar() {
        return monsterChar;
    }

    public int getTimer() {
        return timer;
    }

    public void setTimer(int timer) {
        this.timer = timer;
    }

    public int getSpeedTimer() {
        return speedTimer;
    }

    public void setSpeedTimer(int speedTimer) {
        this.speedTimer = speedTimer;
    }
}
