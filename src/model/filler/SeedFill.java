package model.filler;

import model.object.Point;
import model.object.Polygon;

import java.awt.*;
import java.awt.image.BufferedImage;
import java.util.Stack;

import static model.rasterizer.InterpolateColor.interpolateColor;

public class SeedFill {
    public static void seedFill (Point point, Polygon polygon, BufferedImage raster) {
        int seedX = point.getX();
        int seedY = point.getY();

        int invertedRed = 255 - polygon.getBarva().getRed();
        int invertedGreen = 255 - polygon.getBarva().getGreen();
        int invertedBlue = 255 - polygon.getBarva().getBlue();
        Color opacnaBarva = new Color(invertedRed, invertedGreen, invertedBlue);
        int barvaVyplne = opacnaBarva.getRGB();

        if (polygon.getBarvaG() != null) {
            barvaVyplne = interpolateColor(polygon.getBarva(), polygon.getBarvaG(), 0.4f).getRGB();
        }

        if (raster.getRGB(seedX, seedY) == barvaVyplne) {
            return;
        }

        int barvaPozadi = raster.getRGB(point.getX(), point.getY());
        Stack<Point> zasobnik = new Stack<>();
        zasobnik.push(new Point(seedX, seedY));
        int x;
        int y;
        while (!zasobnik.isEmpty()) {
            Point p = zasobnik.pop();
            x = p.getX();
            y = p.getY();
            int currentColor = raster.getRGB(x, y);
            if (currentColor != barvaPozadi || currentColor == barvaVyplne) continue;
            raster.setRGB(x, y, barvaVyplne);
            zasobnik.push(new Point(x + 1, y));
            zasobnik.push(new Point(x - 1, y));
            zasobnik.push(new Point(x, y + 1));
            zasobnik.push(new Point(x, y - 1));
        }
    }
}
