package model.rasterizer;

import model.object.Line;
import model.object.Point;

import java.awt.*;
import java.awt.image.BufferedImage;

import static model.rasterizer.InterpolateColor.interpolateColor;

public class LineRasterizer {
    BufferedImage raster;

    public LineRasterizer(BufferedImage raster){
        this.raster = raster;
    }

    public void vykresliLineTrivial (Point startPoint, Point endPoint, BufferedImage raster, Color c) {
        int rozdilX = endPoint.getX() - startPoint.getX();
        int rozdilY = endPoint.getY() - startPoint.getY();

        if (rozdilX == 0 && rozdilY == 0) {
            raster.setRGB(startPoint.getX(), startPoint.getY(), c.getRGB());
        }

        if (Math.abs(rozdilX) >= Math.abs(rozdilY)) {
            double k = (double) rozdilY / rozdilX;
            double q = startPoint.getY() - k*startPoint.getX();
            int startX = Math.min(startPoint.getX(), endPoint.getX());
            int endX = Math.max(startPoint.getX(), endPoint.getX());
            for (int x = startX; x <= endX; x++) {
                int y = (int) Math.round(k * x + q);
                raster.setRGB(x, y, c.getRGB());
            }
        } else {
            double k = (double) rozdilX / rozdilY;
            double q = startPoint.getX() - k*startPoint.getY();
            int startY = Math.min(startPoint.getY(), endPoint.getY());
            int endY = Math.max(startPoint.getY(), endPoint.getY());
            for (int y = startY; y <= endY; y++) {
                int x = (int) Math.round(k * y + q);
                raster.setRGB(x, y, c.getRGB());
            }
        }
    }


    /**
     * Algoritmus DDA (Digital Differential Analyzer) pro rasterizaci úsečky, který postupně přidává malé přírůstky v ose X nebo Y podle sklonu úsečky.
     * Pokud je úsečka více vodorovná (rozdilX >= rozdilY), inkrementujeme X 0 1 a dopočítáváme Y pomocí k.
     * Pokud je úsečka více svislá (rozdilX < rozdilY), inkrementujeme Y o 1 a dopočítáváme X pomocí k.
     * Dále přepisuje hodnoty prvního bodu a koncového bodu, tak aby úsečka se vždy vykreslovala zleva doprava.
     * Pokud má úsečka nastavené dvě barvy (barva a barvaG), je mezi nimi vytvářen gradient, jinak pokud barvaG = null je úsečka vykreslována aktuální barvou.
     * Gradient:
     * Pokud je barvaG != null, každému bodu se přiřadí barva podle pozice t ∈ <0,1>.
     * Výsledná barva je c = c1 + t * (c2 - c1).
     * Tak vznikne plynulý barevný přechod (gradient).
     * Ošetření hranic:
     * Každý bod (x, y) se před zápisem kontroluje, zda leží uvnitř rastru.
     * Pokud by byl mimo, pixel se nepíše (zabraňuje chybám IndexOutOfBounds).
     * @param line
     * @param raster
     */
    public void vykresliLineDDA(Line line, BufferedImage raster) {
        int rozdilX = line.getEndPoint().getX() - line.getStartPoint().getX();
        int rozdilY = line.getEndPoint().getY() - line.getStartPoint().getY();
        if (rozdilX == 0 && rozdilY == 0) {
            raster.setRGB(line.getStartPoint().getX(), line.getStartPoint().getY(), line.getBarva().getRGB());
        }
        int x1 = line.getStartPoint().getX();
        int y1 = line.getStartPoint().getY();
        int x2 = line.getEndPoint().getX();
        int y2 = line.getEndPoint().getY();
        int tmp;
        double k;

        if (Math.abs(rozdilX) >= Math.abs(rozdilY)) {
            k = (double) rozdilY / rozdilX;

            if (x1 > x2) {
                tmp = x1;
                x1 = x2;
                x2 = tmp;

                tmp = y1;
                y1 = y2;
                y2 = tmp;
            }
            double y = y1;
            if (line.getBarvaG() == null) {
                for (int x = x1; x <= x2; x++) {
                    if (x1 < raster.getWidth() && x1 >= 0 && x2 < raster.getWidth() &&
                            x2 >= 0 && y1 < raster.getHeight() && y1 >= 0 && y2 < raster.getHeight() && y2 >= 0) {
                        raster.setRGB(x, (int) Math.round(y), line.getBarva().getRGB());
                    }
                    y += k;
                }
            } else {
                for (int i = 0; i <= Math.abs(rozdilX); i++) {
                    float t = i / (float) Math.abs(rozdilX);
                    Color c = interpolateColor(line.getBarva(), line.getBarvaG(), t);
                    int xx = x1 + i;
                    int yy = (int) Math.round(y);
                    if (xx >= 0 && xx < raster.getWidth() && yy >= 0 && yy < raster.getHeight()) {
                        raster.setRGB(xx, yy, c.getRGB());
                    }
                    y += k;
                }
            }
        } else {
            k = (double) rozdilX / rozdilY;
            if (y1 > y2) {
                tmp = y1;
                y1 = y2;
                y2 = tmp;

                tmp = x1;
                x1 = x2;
                x2 = tmp;
            }
            double x = x1;
            if (line.getBarvaG() == null) {
                for (int y = y1; y <= y2; y++) {
                    if (x1 < raster.getWidth() && x1 >= 0 && x2 < raster.getWidth() && x2 >= 0 &&
                            y1 < raster.getHeight() && y1 >= 0 && y2 < raster.getHeight() && y2 >= 0) {
                        raster.setRGB((int) Math.round(x), y, line.getBarva().getRGB());
                    }
                    x += k;
                }
            } else {
                for (int i = 0; i <= Math.abs(rozdilY); i++) {
                    float t = i / (float) Math.abs(rozdilY);
                    Color c = interpolateColor(line.getBarva(), line.getBarvaG(), t);
                    int xx = (int)Math.round(x);
                    int yy = y1 + i;
                    if (xx >= 0 && xx < raster.getWidth() && yy >= 0 && yy < raster.getHeight()) {
                        raster.setRGB(xx, yy, c.getRGB());
                    }
                    x += k;
                }
            }
        }
    }
}

