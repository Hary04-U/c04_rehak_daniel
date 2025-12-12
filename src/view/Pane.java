package view;

import javax.swing.*;
import java.awt.*;
import java.awt.image.BufferedImage;

public class Pane extends JPanel {

    private final BufferedImage raster;

    public Pane(int width, int height) {
        setPreferredSize(new Dimension(width, height));
        raster = new BufferedImage(width, height, BufferedImage.TYPE_INT_RGB);
    }

    public void clean() {
        Graphics2D g = raster.createGraphics();
        g.setColor(Color.BLACK);
        g.fillRect(0, 0, raster.getWidth(), raster.getHeight());
        g.dispose();
    }

    @Override
    public void paintComponent(Graphics g) {
        super.paintComponent(g);
        g.drawImage(raster, 0, 0, null);
    }

    public BufferedImage getRaster() {
        return raster;
    }

}
