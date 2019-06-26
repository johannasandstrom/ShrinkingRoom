import javax.swing.*;
import java.awt.*;

public class Item {
    private int itemX;
    private int itemY;
    private char itemChar = '\u2665';

    public Item() {
    }

    public int getItemX() {
        return itemX;
    }

    public void setItemX(int x) {
        this.itemX = x;
    }

    public int getItemY() {
        return itemY;
    }

    public void setItemY(int y) {
        this.itemY = y;
    }

    public char getItemChar() {
        return itemChar;
    }
}
