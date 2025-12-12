package model.rasterizer;
import model.object.trivial.Line;

import java.awt.*;
import java.awt.image.BufferedImage;

public class LineRasterizer {
    BufferedImage raster;

    public LineRasterizer(BufferedImage raster) {
        this.raster = raster;
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
     *
     * @param line
     * @param raster
     */
    public void vykresliLineDDA(Line line, BufferedImage raster, Color barva) {
        int rozdilX = (int) (line.getEndPoint3D().getX() - line.getStartPoint3D().getX());
        int rozdilY = (int) (line.getEndPoint3D().getY() - line.getStartPoint3D().getY());
        if (rozdilX == 0 && rozdilY == 0) {
            raster.setRGB((int) line.getStartPoint3D().getX(), (int) line.getStartPoint3D().getY(), barva.getRGB());
        }
        int x1 = (int) line.getStartPoint3D().getX();
        int y1 = (int) line.getStartPoint3D().getY();
        int x2 = (int) line.getEndPoint3D().getX();
        int y2 = (int) line.getEndPoint3D().getY();
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
            for (int x = x1; x <= x2; x++) {
                int yy = (int) Math.round(y);
                if (x >= 0 && x < raster.getWidth() &&
                        yy >= 0 && yy < raster.getHeight()) {
                    raster.setRGB(x, yy, barva.getRGB());
                }
                y += k;
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
            for (int y = y1; y <= y2; y++) {
                int xx = (int) Math.round(x);
                if (xx >= 0 && xx < raster.getWidth() &&
                        y >= 0 && y < raster.getHeight()) {
                    raster.setRGB(xx, y, barva.getRGB());
                }
                x += k;
            }
        }
    }
}

