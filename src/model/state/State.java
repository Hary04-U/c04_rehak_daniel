package model.state;

import model.enumModes.ColorMode;
import model.enumModes.FillTypeMode;
import model.enumModes.PolygonMode;
import model.object.Line;
import model.object.Point;
import model.object.Polygon;

import java.awt.*;
import java.util.ArrayList;

import static java.awt.Color.CYAN;

public class State {
    private final ArrayList<Point> points = new ArrayList<>();
    private final ArrayList<Polygon> polygons = new ArrayList<>();
    private final ArrayList<Line> lines = new ArrayList<>();
    private Point startPoint;
    private Point endPoint = null;
    private Point helpPoint = null;
    private Point editPoint = null;
    private Line line;
    private PolygonMode polygonMode = PolygonMode.NONE;
    private FillTypeMode fillTypeMode = FillTypeMode.NONE;
    private ColorMode colorMode = ColorMode.DEFAULT;
    private boolean drag = false;
    private boolean edit = false;
    private int pointIndex;
    private Color barva = CYAN;
    private Color barvaG = null;
    private final Color vychoziBarva = barva;
    private int souradniceX;
    private int souradniceY;
    private Point rectC;
    private Point rectD;

    public Point getStartPoint() {
        return startPoint;
    }

    public Point getEndPoint() {
        return endPoint;
    }

    public Point getHelpPoint() {
        return helpPoint;
    }

    public Point getEditPoint() {
        return editPoint;
    }

    public Line getLine() {
        return line;
    }

    public boolean isDrag() {
        return drag;
    }

    public boolean isEdit() {
        return edit;
    }

    public int getPointIndex() {
        return pointIndex;
    }

    public Color getBarva() {
        return barva;
    }

    public Color getBarvaG() {
        return barvaG;
    }

    public Color getVychoziBarva() {
        return vychoziBarva;
    }

    public int getPixelRadius() {
        return 3;
    }

    public ArrayList<Point> getPoints() {
        return points;
    }

    public ArrayList<Polygon> getPolygons() {
        return polygons;
    }

    public ArrayList<Line> getLines() {
        return lines;
    }

    public int getSouradniceX() {
        return souradniceX;
    }

    public int getSouradniceY() {
        return souradniceY;
    }

    public Point getRectC() {
        return rectC;
    }

    public Point getRectD() {
        return rectD;
    }


    public void setSouradniceX(int souradniceX) {
        this.souradniceX = souradniceX;
    }

    public void setSouradniceY(int souradniceY) {
        this.souradniceY = souradniceY;
    }

    public void setStartPoint(Point startPoint) {
        this.startPoint = startPoint;
    }

    public void setEndPoint(Point endPoint) {
        this.endPoint = endPoint;
    }

    public void setHelpPoint(Point helpPoint) {
        this.helpPoint = helpPoint;
    }

    public void setEditPoint(Point editPoint) {
        this.editPoint = editPoint;
    }

    public void setLine(Line line) {
        this.line = line;
    }

    public void setDrag(boolean drag) {
        this.drag = drag;
    }

    public void setEdit(boolean edit) {
        this.edit = edit;
    }

    public void setPointIndex(int pointIndex) {
        this.pointIndex = pointIndex;
    }

    public void setBarva(Color barva) {
        this.barva = barva;
    }

    public void setBarvaG(Color barvaG) {
        this.barvaG = barvaG;
    }

    public void setRectC(Point rectC) {
        this.rectC = rectC;
    }

    public void setRectD(Point rectD) {
        this.rectD = rectD;
    }

    public void setFillTypeMode(FillTypeMode fillTypeMode) {
        this.fillTypeMode = fillTypeMode;
    }

    public FillTypeMode getFillTypeMode() {
        return fillTypeMode;
    }

    public void setColorMode(ColorMode colorMode) {
        this.colorMode = colorMode;
    }

    public ColorMode getColorMode() {
        return colorMode;
    }

    public void setPolygonMode(PolygonMode polygonMode) {
        this.polygonMode = polygonMode;
    }

    public PolygonMode getPolygonMode() {
        return polygonMode;
    }

    public boolean isPolygon () {
        return polygonMode == PolygonMode.POLYGON;
    }

    public boolean isRectangle () {
        return polygonMode == PolygonMode.RECTANGLE;
    }

    public boolean isInterpolace() {
        return colorMode == ColorMode.INTERPOLACE;
    }

    public boolean isGradient() {
        return colorMode == ColorMode.GRADIENT;
    }

    public boolean isSeed () {
        return fillTypeMode == FillTypeMode.SEED;
    }

    public boolean isScanLine () {
        return fillTypeMode == FillTypeMode.SCANLINE;
    }

    public boolean isNone () {
        return fillTypeMode == FillTypeMode.NONE;
    }
}
