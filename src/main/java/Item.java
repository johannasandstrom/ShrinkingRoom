import javax.swing.*;
import java.awt.*;

public class Item {
    private int itemX;
    private int itemY;
    private Image apple;

    public Item() {
        ImageIcon iia = new ImageIcon("src/resources/apple.png");
        apple = iia.getImage();
    }

    public int getItemX() {
        return itemX;
    }

    public void setItemX(int itemX) {
        this.itemX = itemX;
    }

    public int getItemY() {
        return itemY;
    }

    public void setItemY(int itemY) {
        this.itemY = itemY;
    }
}
