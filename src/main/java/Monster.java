public class Monster {
    private int monsterX;
    private int monsterY;
    private char monsterChar = '\u2620';
    //ta bort!!
    private String bla;

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
}
