package view;

import model.enumModes.FillTypeMode;
import model.object.Line;
import model.object.Point;
import model.object.Polygon;
import model.rasterizer.LineRasterizer;
import model.rasterizer.PolygonRasterizer;
import model.state.State;

import javax.swing.*;
import java.awt.*;
import java.awt.image.BufferedImage;

import static java.awt.Color.*;
import static model.filler.ScanLineFill.scanFill;
import static model.filler.SeedFill.seedFill;

public class Pane extends JPanel {

    private final BufferedImage raster;
    private final BufferedImage previewRaster;
    private final LineRasterizer lineRasterizer;
    private final PolygonRasterizer polygonRasterizer;
    private final State state;

    public Pane(int width, int height) {
        setPreferredSize(new Dimension(width, height));
        raster = new BufferedImage(width, height, BufferedImage.TYPE_INT_RGB);
        state = new State();
        previewRaster = new BufferedImage(width, height, BufferedImage.TYPE_INT_ARGB);
        lineRasterizer = new LineRasterizer(raster);
        polygonRasterizer = new PolygonRasterizer(lineRasterizer);
    }

    @Override
    public void paintComponent(Graphics g) {
        super.paintComponent(g);

        Graphics2D g2 = raster.createGraphics();
        g2.setColor(BLACK);
        g2.fillRect(0, 0, raster.getWidth(), raster.getHeight());
        g2.dispose();

        if (state.getHelpPoint() != null) {
            raster.setRGB(state.getHelpPoint().getX(), state.getHelpPoint().getY(), state.getBarva().getRGB());
        }

        for (Polygon poly : state.getPolygons()) {
            polygonRasterizer.vykresliPolygon(raster, poly);
            if (poly.isFilled()) {
                if (poly.getFillType() == FillTypeMode.SEED) {
                    if (!poly.getSeedPoints().isEmpty()) {
                        for (Point point : poly.getSeedPoints()) {
                            seedFill(point, poly, raster);
                        }
                    }
                } else if (poly.getFillType() == FillTypeMode.SCANLINE) {
                    scanFill(poly, raster);
                }
            }
        }

        if (!state.getLines().isEmpty()) {
            for (Line line : state.getLines()) {
                lineRasterizer.vykresliLineDDA(line, raster);
            }
        }

        g.drawImage(raster, 0, 0, null);

        Graphics2D gPreview = previewRaster.createGraphics();
        gPreview.setComposite(AlphaComposite.Clear);
        gPreview.fillRect(0, 0, previewRaster.getWidth(), previewRaster.getHeight());
        gPreview.dispose();

        g.setColor(WHITE);
        g.drawString("X: " + state.getSouradniceX() + ", Y: " + state.getSouradniceY(), 10, 20);
        if (state.isSeed() || state.isScanLine()) {
            g.drawString(String.valueOf(state.getFillTypeMode()), 10, 40);
            if (state.isInterpolace() || state.isGradient()) {
                g.drawString(String.valueOf(state.getColorMode()), 10, 60);
                if (state.isPolygon() || state.isRectangle()) {
                    g.drawString(String.valueOf(state.getPolygonMode()), 10, 80);
                }
            } else {
                if (state.isPolygon() || state.isRectangle()) {
                    g.drawString(String.valueOf(state.getPolygonMode()), 10, 60);
                }
            }
        } else {
            if (state.isInterpolace() || state.isGradient()) {
                g.drawString(String.valueOf(state.getColorMode()), 10, 40);
                if (state.isPolygon() || state.isRectangle()) {
                    g.drawString(String.valueOf(state.getPolygonMode()), 10, 60);
                }
            } else {
                if (state.isPolygon() || state.isRectangle()) {
                    g.drawString(String.valueOf(state.getPolygonMode()), 10, 40);
                }
            }
        }

        Line line;
        if (state.isDrag()) {
            if (state.isPolygon() || state.isRectangle()) {
                if (state.isEdit()) {
                    if (state.getPoints().size() == 1) {
                        previewRaster.setRGB(state.getEditPoint().getX(), state.getEditPoint().getY(), RED.getRGB());
                    } else if (state.getPoints().size() == 2) {
                        if (state.getPointIndex() == 0) {
                            line = new Line(state.getEditPoint(), state.getPoints().getLast(), RED, state.getBarvaG());
                            lineRasterizer.vykresliLineDDA(line, previewRaster);
                        } else if (state.getPointIndex() == 1) {
                            line = new Line(state.getEditPoint(), state.getPoints().getFirst(), RED, state.getBarvaG());
                            lineRasterizer.vykresliLineDDA(line, previewRaster);
                        }
                    } else if (state.getPoints().size() > 2) {
                        if (state.getPointIndex() == state.getPoints().size() - 1) {
                            line = new Line(state.getEditPoint(), state.getPoints().get(state.getPointIndex() - 1), RED, state.getBarvaG());
                            lineRasterizer.vykresliLineDDA(line, previewRaster);
                            line = new Line(state.getEditPoint(), state.getPoints().getFirst(), RED, state.getBarvaG());
                            lineRasterizer.vykresliLineDDA(line, previewRaster);
                        } else if (state.getPointIndex() == 0) {
                            line = new Line(state.getEditPoint(), state.getPoints().get(state.getPointIndex() + 1), RED, state.getBarvaG());
                            lineRasterizer.vykresliLineDDA(line, previewRaster);
                            line = new Line(state.getEditPoint(), state.getPoints().getLast(), RED, state.getBarvaG());
                            lineRasterizer.vykresliLineDDA(line, previewRaster);
                        } else {
                            line = new Line(state.getEditPoint(), state.getPoints().get(state.getPointIndex() - 1), RED, state.getBarvaG());
                            lineRasterizer.vykresliLineDDA(line, previewRaster);
                            line = new Line(state.getEditPoint(), state.getPoints().get(state.getPointIndex() + 1), RED, state.getBarvaG());
                            lineRasterizer.vykresliLineDDA(line, previewRaster);
                        }
                    }
                } else {
                    if (state.getEndPoint() != null) {
                        if (state.getPoints().size() > 1) {
                            if (state.isRectangle()) {
                                line = new Line(state.getPoints().getFirst(), state.getRectD(), RED, state.getBarvaG());
                                lineRasterizer.vykresliLineDDA(line, previewRaster);
                                line = new Line(state.getPoints().get(1), state.getRectC(), RED, state.getBarvaG());
                                lineRasterizer.vykresliLineDDA(line, previewRaster);
                                line = new Line(state.getRectC(), state.getRectD(), RED, state.getBarvaG());
                                lineRasterizer.vykresliLineDDA(line, previewRaster);
                            } else if (state.isPolygon()) {
                                line = new Line(state.getPoints().getLast(), state.getEndPoint(), RED, state.getBarvaG());
                                lineRasterizer.vykresliLineDDA(line, previewRaster);
                                line = new Line(state.getPoints().getFirst(), state.getEndPoint(), RED, state.getBarvaG());
                                lineRasterizer.vykresliLineDDA(line, previewRaster);
                            }
                        } else if (state.getPoints().size() == 1) {
                            line = new Line(state.getPoints().getFirst(), state.getEndPoint(), RED, state.getBarvaG());
                            lineRasterizer.vykresliLineDDA(line, previewRaster);
                        } else {
                            if (state.getHelpPoint() == null) {
                                line = new Line(state.getStartPoint(), state.getEndPoint(), RED, state.getBarvaG());
                                lineRasterizer.vykresliLineDDA(line, previewRaster);
                            } else {
                                previewRaster.setRGB(state.getHelpPoint().getX(), state.getHelpPoint().getY(), RED.getRGB());
                            }
                        }
                    }
                }
            } else {
                if (state.getHelpPoint() == null) {
                    line = new Line(state.getStartPoint(), state.getEndPoint(), RED, state.getBarvaG());
                    lineRasterizer.vykresliLineDDA(line, previewRaster);
                } else {
                    line = new Line(state.getHelpPoint(), state.getEndPoint(), RED, state.getBarvaG());
                    lineRasterizer.vykresliLineDDA(line, previewRaster);
                }
            }
            g.drawImage(previewRaster, 0, 0, null);
        }


        if (state.isPolygon() || state.isRectangle()) {
            Color barvaPolygonG = null;
            Color barvaPolygon = YELLOW;
            if (state.getPoints().size() == 1) {
                if (state.getBarvaG() == null) {
                    previewRaster.setRGB(state.getPoints().getFirst().getX(), state.getPoints().getFirst().getY(), barvaPolygon.getRGB());
                } else {
                    previewRaster.setRGB(state.getPoints().getFirst().getX(), state.getPoints().getFirst().getY(), state.getBarvaG().getRGB());
                }
            }
            if (state.getPoints().size() > 1) {
                for (int i = 1; i < state.getPoints().size(); i++) {
                    line = new Line(state.getPoints().get(i - 1), state.getPoints().get(i), barvaPolygon, barvaPolygonG);
                    lineRasterizer.vykresliLineDDA(line, previewRaster);
                }
            }
            if (state.getPoints().size() > 2) {
                if (state.isDrag() && !state.isEdit()) {
                    barvaPolygon = GRAY;
                }
                line = new Line(state.getPoints().getFirst(), state.getPoints().getLast(), barvaPolygon, barvaPolygonG);
                lineRasterizer.vykresliLineDDA(line, previewRaster);
            }

            if (state.isEdit()) {
                if (state.isDrag()) {
                    barvaPolygon = GRAY;
                } else {
                    barvaPolygon = YELLOW;
                }
                if (state.getPoints().size() > 2) {
                    if (state.getPointIndex() == state.getPoints().size() - 1) {
                        line = new Line(state.getPoints().getLast(), state.getPoints().get(state.getPointIndex() - 1), barvaPolygon, barvaPolygonG);
                        lineRasterizer.vykresliLineDDA(line, previewRaster);
                        line = new Line(state.getPoints().getLast(), state.getPoints().getFirst(), barvaPolygon, barvaPolygonG);
                        lineRasterizer.vykresliLineDDA(line, previewRaster);
                    } else if (state.getPointIndex() == 0) {
                        line = new Line(state.getPoints().getFirst(), state.getPoints().get(state.getPointIndex() + 1), barvaPolygon, barvaPolygonG);
                        lineRasterizer.vykresliLineDDA(line, previewRaster);
                        line = new Line(state.getPoints().getFirst(), state.getPoints().getLast(), barvaPolygon, barvaPolygonG);
                        lineRasterizer.vykresliLineDDA(line, previewRaster);
                    } else {
                        line = new Line(state.getPoints().get(state.getPointIndex()), state.getPoints().get(state.getPointIndex() - 1), barvaPolygon, barvaPolygonG);
                        lineRasterizer.vykresliLineDDA(line, previewRaster);
                        line = new Line(state.getPoints().get(state.getPointIndex()), state.getPoints().get(state.getPointIndex() + 1), barvaPolygon, barvaPolygonG);
                        lineRasterizer.vykresliLineDDA(line, previewRaster);
                    }
                }
            }
            g.drawImage(previewRaster, 0, 0, null);
        }
    }

    public BufferedImage getRaster() {
        return raster;
    }

    public State getState() {
        return state;
    }
}
