import javax.swing.*;
import java.awt.*;

public class Item {
    private int itemX;
    private int itemY;
    private char itemChar;

    public Item(int itemX, int itemY) {
        itemChar = '\u2665';
        this.itemX = itemX;
        this.itemY = itemY;
    }

    public int getItemX() {
        return itemX;
    }

    public int getItemY() {
        return itemY;
    }

    public char getItemChar() {
        return itemChar;
    }
}
