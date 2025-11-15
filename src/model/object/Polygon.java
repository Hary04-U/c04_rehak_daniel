package model.object;

import model.enumModes.FillTypeMode;

import java.awt.*;
import java.awt.image.BufferedImage;
import java.util.ArrayList;

public class Polygon {
    private ArrayList<Point> points;
    private Color barva;
    private Color barvaG;
    private Boolean filled = false;
    private ArrayList<Point> seedPoints = new ArrayList<>();
    private FillTypeMode fillType = FillTypeMode.NONE;

    public Polygon(ArrayList<Point> points, Color barva, Color barvaG) {
        this.points = points;
        this.barva = barva;
        this.barvaG = barvaG;
    }

    public ArrayList<Point> getPoints() {
        return points;
    }

    public Color getBarva() {
        return barva;
    }

    public Color getBarvaG() {
        return barvaG;
    }

    public Boolean isFilled() {
        return filled;
    }

    public void setFilled(Boolean filled) {
        this.filled = filled;
    }

    public FillTypeMode getFillType() {
        return fillType;
    }

    public void setFillType(FillTypeMode fillType) {
        this.fillType = fillType;
    }

    public ArrayList<Point> getSeedPoints() {
        return seedPoints;
    }

    public static Boolean isInside(Point point, Polygon polygon, BufferedImage raster) {
        boolean inside = false;
        if (point.getX() >= raster.getWidth() || point.getX() < 0 || point.getY() >= raster.getHeight() || point.getY() < 0) {
            return inside;
        }
        if (raster.getRGB(point.getX(), point.getY()) == polygon.getBarva().getRGB()) {
            return inside;
        }
        int counter = 0;
        int x1 = point.getX();
        int y1 = point.getY();

        Point p1;
        int polygonX1;
        int polygonY1;
        Point p2;
        int polygonX2;
        int polygonY2;
        Point tmp;

        double prusecikX;
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

            if (polygonY1 < y1 && polygonY2 > y1) {
                prusecikX = polygonX1 + (double) (y1 - polygonY1) * (polygonX2 - polygonX1) / (polygonY2 - polygonY1);

                if (prusecikX > x1) {
                    counter++;
                }
            }
        }
        if (counter % 2 == 0) {
            inside = false;
        } else {
            inside = true;
        }
        return inside;
    }

    public static Polygon orez(Polygon pOld, Polygon pCut) {
        if (pOld == null || pCut == null) return null;

        ArrayList<Point> pOldPoints = new ArrayList<>(pOld.getPoints());
        ArrayList<Point> pCutPoints = new ArrayList<>(pCut.getPoints());
        if (pOldPoints.size() < 3 || pCutPoints.size() < 3) return null;

        boolean cutCCW = isCCW(pCutPoints);

        for (int i = 0; i < pCutPoints.size(); i++) {
            Point p1 = pCutPoints.get(i);
            Point p2;
            if (i == pCutPoints.size() - 1) {
                p2 = pCutPoints.getFirst();
            } else {
                p2 = pCutPoints.get(i + 1);
            }

            ArrayList<Point> pointsFinal = new ArrayList<>();
            Point oldLast = pOldPoints.getLast();

            for (Point oldP : pOldPoints) {
                boolean aktual = isInsidePoly(oldP, p1, p2, cutCCW);
                boolean predchozi = isInsidePoly(oldLast, p1, p2, cutCCW);

                if (aktual) {
                    if (!predchozi) {
                        Point prusecik = getPruseciky(oldLast, oldP, p1, p2);
                        if (prusecik != null) pointsFinal.add(prusecik);
                    }
                    pointsFinal.add(oldP);
                } else if (predchozi) {
                    Point prusecik = getPruseciky(oldLast, oldP, p1, p2);
                    if (prusecik != null) pointsFinal.add(prusecik);
                }
                oldLast = oldP;
            }
            pOldPoints = pointsFinal;
            if (pOldPoints.isEmpty()) break;
        }

        if (pOldPoints.isEmpty()) return null;
        return new Polygon(pOldPoints, pOld.getBarva(), pOld.getBarvaG());
    }

    private static boolean isInsidePoly(Point p, Point A, Point B, boolean clipCCW) {
        double cross = (B.getX() - A.getX()) * (p.getY() - A.getY()) -
                (B.getY() - A.getY()) * (p.getX() - A.getX());
        return clipCCW ? cross >= 0 : cross <= 0;
    }

    private static Point getPruseciky(Point S, Point E, Point A, Point B) {
        double x1 = S.getX(), y1 = S.getY();
        double x2 = E.getX(), y2 = E.getY();
        double x3 = A.getX(), y3 = A.getY();
        double x4 = B.getX(), y4 = B.getY();

        double denom = (x1 - x2) * (y3 - y4) - (y1 - y2) * (x3 - x4);
        if (Math.abs(denom) < 1e-6) return null;

        double px = ((x1 * y2 - y1 * x2) * (x3 - x4)
                - (x1 - x2) * (x3 * y4 - y3 * x4)) / denom;
        double py = ((x1 * y2 - y1 * x2) * (y3 - y4)
                - (y1 - y2) * (x3 * y4 - y3 * x4)) / denom;

        return new Point((int) Math.round(px), (int) Math.round(py));
    }

    private static boolean isCCW(ArrayList<Point> points) {
        double area = 0;
        for (int i = 0; i < points.size(); i++) {
            Point p1 = points.get(i);
            Point p2;
            if (i == points.size() - 1) {
                p2 = points.getFirst();
            } else {
                p2 = points.get(i + 1);
            }
            area += (p2.getX() - p1.getX()) * (p2.getY() + p1.getY());
        }
        return area < 0;
    }
}
