package controller;

import model.enumModes.ColorMode;
import model.enumModes.FillTypeMode;
import model.enumModes.PolygonMode;
import model.object.Line;
import model.object.Point;
import model.object.Polygon;
import model.object.Rectangle;
import model.state.State;
import view.Pane;

import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import java.util.ArrayList;

import static model.object.Polygon.isInside;
import static model.rasterizer.InterpolateColor.interpolateColor;

public class Controller2D implements Controller {
    private final Pane panel;
    private final State state;

    public Controller2D(Pane panel) {
        this.panel = panel;
        this.state = panel.getState();
        listeners();
    }

    @Override
    public void listeners() {
        panel.addMouseListener(new MouseAdapter() {
            @Override
            public void mousePressed(MouseEvent e) {
                if (state.isPolygon()) {
                    Point editpoint = new Point(e.getX(), e.getY());
                    for (int i = 0; i < state.getPoints().size(); i++) {
                        if (Math.abs(editpoint.getX() - state.getPoints().get(i).getX()) <= state.getPixelRadius() && Math.abs(editpoint.getY() - state.getPoints().get(i).getY()) <= state.getPixelRadius()) {
                            state.setEdit(true);
                            state.setPointIndex(i);
                        }
                    }
                }
                state.setStartPoint(new Point(e.getX(), e.getY()));
            }

            @Override
            public void mouseReleased(MouseEvent e) {
                boolean isInsideRaster;
                Point releasePoint = new Point(e.getX(), e.getY());
                boolean isInside = false;

                if (!state.getPolygons().isEmpty()) {
                    for (Polygon polygon : state.getPolygons()) {
                        if (isInside(releasePoint, polygon, panel.getRaster()) && (state.isScanLine() || state.isSeed())) {
                            polygon.getSeedPoints().add(releasePoint);
                            polygon.setFilled(true);
                            polygon.setFillType(state.getFillTypeMode());
                            state.setHelpPoint(null);
                            isInside = true;
                            break;
                        }
                    }
                }

                if (!isInside) {
                    if (state.isRectangle()) {
                        if (state.isDrag() && state.getPoints().isEmpty()) {
                            state.getPoints().add(state.getStartPoint());
                            state.getPoints().add(state.getEndPoint());
                        } else if (state.getPoints().size() == 2) {
                            if (state.getRectC() == null || state.getRectD() == null) {
                                Point A = state.getPoints().getFirst();
                                Point B = state.getPoints().getLast();
                                Point V = new Point(e.getX(), e.getY());
                                vypocitejObdelnik(A, B, V);
                            }
                            if ((state.getRectC().getX() < panel.getRaster().getWidth() && state.getRectC().getX() >= 0 && state.getRectC().getY() < panel.getRaster().getHeight() && state.getRectC().getY() >= 0) &&
                                    state.getRectD().getX() < panel.getRaster().getWidth() && state.getRectD().getX() >= 0 && state.getRectD().getY() < panel.getRaster().getHeight() && state.getRectD().getY() >= 0) {
                                state.getPoints().add(state.getRectC());
                                state.getPoints().add(state.getRectD());
                            } else {
                                JOptionPane.showMessageDialog(null, "Body obdélníku jsou mimo oblast kreslení!", "Upozornění", JOptionPane.WARNING_MESSAGE);
                            }
                            Polygon aktualRectangle = new Rectangle(new ArrayList<>(state.getPoints()), state.getBarva(), state.getBarvaG());
                            vytvorPolygon(aktualRectangle);
                            state.getPoints().clear();
                            state.setRectC(null);
                            state.setRectD(null);
                        } else {
                            state.getPoints().add(new Point(e.getX(), e.getY()));
                        }
                    } else if (state.isPolygon()) {
                        if (state.isDrag()) {
                            if (state.isEdit() && state.getEditPoint() != null) {
                                isInsideRaster = isInsideRaster(state.getEditPoint().getX(), state.getEditPoint().getY());
                                if (isInsideRaster) {
                                    state.getPoints().set(state.getPointIndex(), state.getEditPoint());
                                }
                                state.setEdit(false);
                                state.setEndPoint(null);
                            } else {
                                if (state.getPoints().isEmpty()) {
                                    state.getPoints().add(state.getStartPoint());
                                    state.getPoints().add(state.getEndPoint());
                                } else {
                                    isInsideRaster = isInsideRaster(e.getX(), e.getY());
                                    if (isInsideRaster) {
                                        state.getPoints().add(new Point(e.getX(), e.getY()));
                                    }
                                }
                            }
                        } else {
                            state.getPoints().add(new Point(e.getX(), e.getY()));
                        }
                    } else {
                        if (state.isDrag()) {
                            if (state.getHelpPoint() != null) {
                                isInsideRaster = isInsideRaster(state.getEndPoint().getX(),  state.getEndPoint().getY());
                                if (isInsideRaster) {
                                    state.setLine(new Line(state.getHelpPoint(), state.getEndPoint(), state.getBarva(), state.getBarvaG()));
                                    state.getLines().add(state.getLine());
                                    state.setHelpPoint(null);
                                }
                            } else {
                                isInsideRaster = isInsideRaster(state.getEndPoint().getX(),  state.getEndPoint().getY());
                                if (isInsideRaster) {
                                    state.setLine(new Line(state.getStartPoint(), state.getEndPoint(), state.getBarva(), state.getBarvaG()));
                                    state.getLines().add(state.getLine());
                                }
                            }
                        } else {
                            state.setEndPoint(new Point(e.getX(), e.getY()));
                            if (state.getHelpPoint() != null) {
                                state.setLine(new Line(state.getHelpPoint(), state.getEndPoint(), state.getBarva(), state.getBarvaG()));
                                state.getLines().add(state.getLine());
                                state.setHelpPoint(null);
                            } else {
                                state.setHelpPoint(state.getStartPoint());
                            }
                        }
                    }
                }

                state.setDrag(false);
                panel.repaint();
            }
        });

        panel.addMouseMotionListener(new MouseMotionAdapter() {
            @Override
            public void mouseDragged(MouseEvent e) {
                if (state.isRectangle() && state.getPoints().size() > 1) {
                    Point A = state.getPoints().getFirst();
                    Point B = state.getPoints().getLast();
                    Point V = new Point(e.getX(), e.getY());
                    vypocitejObdelnik(A, B, V);
                }
                if (state.isEdit()) {
                    state.setEditPoint(new Point(e.getX(), e.getY()));
                } else {
                    if ((e.getModifiersEx() & InputEvent.SHIFT_DOWN_MASK) != 0 && !(state.isPolygon())) {
                        int dx = e.getX() - state.getStartPoint().getX();
                        int dy = e.getY() - state.getStartPoint().getY();

                        double angle = Math.atan2(dy, dx);
                        double step = Math.toRadians(45);
                        double snapped = Math.round(angle / step) * step;

                        double length = Math.sqrt(dx * dx + dy * dy);
                        int newX = state.getStartPoint().getX() + (int) (length * Math.cos(snapped));
                        int newY = state.getStartPoint().getY() + (int) (length * Math.sin(snapped));

                        state.setEndPoint(new Point(newX, newY));
                    } else {
                        state.setEndPoint(new Point(e.getX(), e.getY()));
                    }
                }
                state.setDrag(true);
                panel.repaint();
            }

            @Override
            public void mouseMoved(MouseEvent e) {
                state.setSouradniceX(e.getX());
                state.setSouradniceY(e.getY());
                panel.repaint();
            }
        });

        panel.addKeyListener(new KeyAdapter() {
            public void keyPressed(KeyEvent e) {
                if (e.getKeyCode() == KeyEvent.VK_P) {
                    if (!(state.isPolygon())) {
                        if (!state.getPoints().isEmpty()) {
                            Polygon aktualRectangle = new Rectangle(new ArrayList<>(state.getPoints()), state.getBarva(), state.getBarvaG());
                            state.getPolygons().add(aktualRectangle);
                            state.getPoints().clear();
                        }
                        state.setPolygonMode(PolygonMode.POLYGON);
                        state.setHelpPoint(null);
                    } else {
                        state.setPolygonMode(PolygonMode.NONE);

                        Polygon aktualPolygon = new Polygon(new ArrayList<>(state.getPoints()), state.getBarva(), state.getBarvaG());
                        vytvorPolygon(aktualPolygon);

                        state.getPoints().clear();
                        panel.repaint();
                    }
                }
                if (e.getKeyCode() == KeyEvent.VK_R) {
                    if (!(state.isRectangle())) {
                        if (!state.getPoints().isEmpty()) {
                            Polygon aktualPolygon = new Polygon(new ArrayList<>(state.getPoints()), state.getBarva(), state.getBarvaG());
                            vytvorPolygon(aktualPolygon);
                            state.getPoints().clear();
                        }
                        state.setPolygonMode(PolygonMode.RECTANGLE);
                        state.setHelpPoint(null);
                    } else {
                        state.setPolygonMode(PolygonMode.NONE);

                    }
                }
                if (e.getKeyCode() == KeyEvent.VK_I) {
                    if (!(state.isInterpolace())) {
                        state.setColorMode(ColorMode.INTERPOLACE);
                        state.setBarva(interpolateColor(state.getVychoziBarva(), Color.RED, 0.4f));
                        state.setBarvaG(null);
                    } else {
                        state.setColorMode(ColorMode.DEFAULT);
                        state.setBarva(state.getVychoziBarva());
                    }
                }
                if (e.getKeyCode() == KeyEvent.VK_G) {
                    if (!(state.isGradient())) {
                        state.setColorMode(ColorMode.GRADIENT);
                        state.setBarva(state.getVychoziBarva());
                        state.setBarvaG(Color.RED);
                    } else {
                        state.setColorMode(ColorMode.DEFAULT);
                        state.setBarvaG(null);
                    }
                }
                if (e.getKeyCode() == KeyEvent.VK_V) {
                    if (state.isNone()) {
                        state.setFillTypeMode(FillTypeMode.SEED);
                    } else if (state.isSeed()) {
                        state.setFillTypeMode(FillTypeMode.SCANLINE);
                    } else {
                        state.setFillTypeMode(FillTypeMode.NONE);
                    }
                }
                if (e.getKeyCode() == KeyEvent.VK_C) {
                    clean();
                }
                panel.repaint();
            }
        });
    }

    public void clean() {
        if (!state.getPoints().isEmpty()) {
            state.getPoints().clear();
        }
        if (!state.getPolygons().isEmpty()) {
            state.getPolygons().clear();
        }
        if (!state.getLines().isEmpty()) {
            state.getLines().clear();
        }
        state.setHelpPoint(null);
    }

    public void vypocitejObdelnik(Point A, Point B, Point V) {
        double ux = B.getX() - A.getX();
        double uy = B.getY() - A.getY();
        double delkaV = Math.sqrt(ux * ux + uy * uy);
        double nx = -uy / delkaV;
        double ny = ux / delkaV;
        double orient = (B.getX() - A.getX()) * (V.getY() - A.getY()) - (B.getY() - A.getY()) * (V.getX() - A.getX());
        if (orient < 0) {
            nx = -nx;
            ny = -ny;
        }
        double height = Math.abs((B.getX() - A.getX()) * (A.getY() - V.getY()) - (A.getX() - V.getX()) * (B.getY() - A.getY())) / delkaV;
        Point C = new Point((int) Math.round(B.getX() + nx * height), (int) Math.round(B.getY() + ny * height));
        Point D = new Point((int) Math.round(A.getX() + nx * height), (int) Math.round(A.getY() + ny * height));
        state.setRectC(C);
        state.setRectD(D);
    }

    public void vytvorPolygon(Polygon polygon) {
        Polygon orez = null;
        int staryIndex = -1;
        boolean orezano = false;

        for (int i = 0; i < state.getPolygons().size(); i++) {
            Polygon stary = state.getPolygons().get(i);
            orez = Polygon.orez(stary, polygon);

            if (orez != null && !orez.getPoints().isEmpty()) {
                staryIndex = i;
                orezano = true;
                break;
            }
        }
        if (!orezano) {
            state.getPolygons().add(polygon);
        } else {
            orez.setFilled(true);
            orez.setFillType(FillTypeMode.SCANLINE);
            state.getPolygons().remove(staryIndex);
            state.getPolygons().add(orez);
        }
    }

    public boolean isInsideRaster(int x, int y) {
        return x < panel.getRaster().getWidth() && x >= 0 && y < panel.getRaster().getHeight() && y >= 0;
    }
}

