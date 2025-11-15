package model.filler;

import model.object.Point;
import model.object.Polygon;

import java.awt.*;
import java.awt.image.BufferedImage;
import java.util.ArrayList;

import static model.rasterizer.InterpolateColor.interpolateColor;

public class ScanLineFill {
    public static void scanFill(Polygon polygon, BufferedImage raster) {

        int invertedRed = 255 - polygon.getBarva().getRed();
        int invertedGreen = 255 - polygon.getBarva().getGreen();
        int invertedBlue = 255 - polygon.getBarva().getBlue();
        Color opacnaBarva = new Color(invertedRed, invertedGreen, invertedBlue);

        if (polygon.getBarvaG() != null) {
            opacnaBarva = interpolateColor(polygon.getBarva(), polygon.getBarvaG(), 0.4f);
        }

        ArrayList<Integer> prusecikyX = new ArrayList<>();

        int bottomY = polygon.getPoints().getFirst().getY();
        int topY = polygon.getPoints().getFirst().getY();

        Point p1;
        int polygonX1;
        int polygonY1;
        Point p2;
        int polygonX2;
        int polygonY2;
        Point tmp;

        for (Point p : polygon.getPoints()) {
            if (p.getY() > bottomY) {
                bottomY = p.getY();
            }
            if (p.getY() < topY) {
                topY = p.getY();
            }
        }

        for (int y = bottomY; y >= topY; y--) {
            prusecikyX.clear();

            for (int i = 0; i < polygon.getPoints().size(); i++) {
                p1 = polygon.getPoints().get(i);
                if (i == polygon.getPoints().size() - 1) {
                    p2 = polygon.getPoints().getFirst();
                } else {
                    p2 = polygon.getPoints().get(i + 1);
                }

                if (p2.getY() < p1.getY()) {
                    tmp = p1;
                    p1 = p2;
                    p2 = tmp;
                }

                polygonY1 = p1.getY();
                polygonY2 = p2.getY();
                polygonX1 = p1.getX();
                polygonX2 = p2.getX();

                if (y >= polygonY1 && y < polygonY2) {
                    double prusecikX = polygonX1 + (double) (y - polygonY1) * (polygonX2 - polygonX1) / (polygonY2 - polygonY1);
                    prusecikyX.add((int) Math.round(prusecikX));
                }
            }

            prusecikyX.sort(Integer::compareTo);

            int barvaPozadi = Color.BLACK.getRGB();
            for (int i = 0; i < prusecikyX.size(); i += 2) {
                for (int x = prusecikyX.get(i); x <= prusecikyX.get(i + 1); x++) {
                    if (raster.getRGB(x, y) == barvaPozadi) {
                        raster.setRGB(x, y, opacnaBarva.getRGB());
                    }
                }
            }
        }
    }
}
