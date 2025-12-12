package model.object.mesh;

import model.object.mesh.axis.AxisX;
import model.object.mesh.axis.AxisY;
import model.object.mesh.axis.AxisZ;
import model.object.trivial.Point2D;
import model.object.transforms.*;


import java.awt.*;
import java.awt.image.BufferedImage;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import static model.renderer.Renderer3D.transformToWindow;

public class Mesh {
    protected List<Point3D> vertexBuffer = new ArrayList<>();
    protected List<Integer> indexBuffer = new ArrayList<>();
    protected List<Face> faces = new ArrayList<>();
    protected Mat4 modelMat = new Mat4Identity();
    protected Mat4 defaultModelMat = new Mat4Identity();
    protected Color color = Color.PINK;
    protected Color defaultColor = Color.PINK;

    public void addIndices(Integer... indices) {
        indexBuffer.addAll(Arrays.asList(indices));
    }

    public List<Point3D> getVertexBuffer() {
        return vertexBuffer;
    }

    public List<Integer> getIndexBuffer() {
        return indexBuffer;
    }

    public List<Face> getFaces() {
        return faces;
    }

    public void addFace(Integer... points) {
        faces.add(new Face(points));
    }

    public Color getColor() {
        return color;
    }

    public void setColor(Color color) {
        this.color = color;
    }

    public Color getDefaultColor() {
        return defaultColor;
    }

    public void setDefaultColor() {
        this.defaultColor = this.color;
    }

    public static boolean isInside(Point2D point, Mesh mesh, BufferedImage raster,
                                   Mat4 view, Mat4 projection) {
        int x = point.getX();
        int y = point.getY();

        Mat4 mvp = mesh.getModelMat().mul(view).mul(projection);

        for (Face face : mesh.getFaces()) {

            List<Vec3D> poly2D = new ArrayList<>();

            for (int idx : face.getFacePoints()) {
                Point3D p = mesh.getVertexBuffer().get(idx).mul(mvp);

                if (p.dehomog().isEmpty()) {
                    poly2D.clear();
                    break;
                }

                Vec3D ndc = p.dehomog().get();
                Vec3D win = transformToWindow(ndc, raster);

                poly2D.add(win);
            }

            if (poly2D.size() < 3) continue;

            if (pointInPolygon(x, y, poly2D)) {
                return true;
            }
        }

        return false;
    }

    private static boolean pointInPolygon(int x, int y, List<Vec3D> poly) {
        boolean inside = false;

        for (int i = 0, j = poly.size() - 1; i < poly.size(); j = i++) {
            double xi = poly.get(i).getX();
            double yi = poly.get(i).getY();
            double xj = poly.get(j).getX();
            double yj = poly.get(j).getY();

            boolean intersect = ((yi > y) != (yj > y)) &&
                    (x < (xj - xi) * (y - yi) / (yj - yi) + xi);

            if (intersect) inside = !inside;
        }

        return inside;
    }

    public Mat4 getModelMat() {
        return modelMat;
    }

    public void setModelMat(Mat4 modelMat) {
        this.modelMat = modelMat;
    }

    public Mat4 getDefaultModelMat() {
        return defaultModelMat;
    }

    public void setDefaultModelMat() {
        this.defaultModelMat = this.modelMat;
    }

    public void setDefaultModelMat(Mat4 defaultModelMat) {
        this.defaultModelMat = defaultModelMat;
    }
}
