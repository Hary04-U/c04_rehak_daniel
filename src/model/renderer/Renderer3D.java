package model.renderer;

import model.object.mesh.curve.CurvePoint;
import model.object.trivial.Line;
import model.object.mesh.Mesh;
import model.rasterizer.LineRasterizer;
import model.object.transforms.*;

import java.awt.image.BufferedImage;
import java.util.Optional;

public class Renderer3D {
    private final LineRasterizer lineRasterizer;
    private final BufferedImage raster;
    private Mat4 view;
    private Mat4 projection;

    public Renderer3D(LineRasterizer lineRasterizer, BufferedImage raster) {
        this.lineRasterizer = lineRasterizer;
        this.raster = raster;
    }

    public void render(Mesh mesh) {
        Mat4 mvp = mesh.getModelMat().mul(view).mul(projection);

        if (mesh.getIndexBuffer().size() % 3 == 0 && mesh instanceof CurvePoint) {
            for (int i = 0; i < mesh.getIndexBuffer().size(); i += 3) {
                int index1 = mesh.getIndexBuffer().get(i);
                int index2 = mesh.getIndexBuffer().get(i + 1);
                int index3 = mesh.getIndexBuffer().get(i + 2);

                Point3D a = mesh.getVertexBuffer().get(index1).mul(mvp);
                Point3D b = mesh.getVertexBuffer().get(index2).mul(mvp);
                Point3D c = mesh.getVertexBuffer().get(index3).mul(mvp);

                if (!isInView(a, b, c)) continue;

                Optional<Vec3D> optAV = tryDehomog(a);
                if (optAV.isEmpty()) continue;
                Vec3D aV = optAV.get();

                Optional<Vec3D> optBV = tryDehomog(b);
                if (optBV.isEmpty()) continue;
                Vec3D bV = optBV.get();

                Optional<Vec3D> optCV = tryDehomog(c);
                if (optCV.isEmpty()) continue;
                Vec3D cV = optCV.get();

                if (Math.abs(aV.getX()) > 1 || Math.abs(aV.getY()) > 1 || Math.abs(aV.getZ()) > 1) continue;
                if (Math.abs(bV.getX()) > 1 || Math.abs(bV.getY()) > 1 || Math.abs(bV.getZ()) > 1) continue;
                if (Math.abs(cV.getX()) > 1 || Math.abs(cV.getY()) > 1 || Math.abs(cV.getZ()) > 1) continue;

                Vec3D aToWindow = transformToWindow(aV, raster);
                Vec3D bToWindow = transformToWindow(bV, raster);
                Vec3D cToWindow = transformToWindow(cV, raster);

                rasterize(aToWindow, bToWindow, mesh);
                rasterize(aToWindow, cToWindow, mesh);
                rasterize(bToWindow, cToWindow, mesh);
            }
        } else {
            for (int i = 0; i < mesh.getIndexBuffer().size(); i += 2) {
                int index1 = mesh.getIndexBuffer().get(i);
                int index2 = mesh.getIndexBuffer().get(i + 1);

                Point3D a = mesh.getVertexBuffer().get(index1).mul(mvp);
                Point3D b = mesh.getVertexBuffer().get(index2).mul(mvp);

                if (!isInView(a, b)) continue;

                Optional<Vec3D> optAV = tryDehomog(a);
                if (optAV.isEmpty()) continue;
                Vec3D aV = optAV.get();

                Optional<Vec3D> optBV = tryDehomog(b);
                if (optBV.isEmpty()) continue;
                Vec3D bV = optBV.get();

                if (Math.abs(aV.getX()) > 1 || Math.abs(aV.getY()) > 1 || Math.abs(aV.getZ()) > 1) continue;
                if (Math.abs(bV.getX()) > 1 || Math.abs(bV.getY()) > 1 || Math.abs(bV.getZ()) > 1) continue;

                Vec3D aToWindow = transformToWindow(aV, raster);
                Vec3D bToWindow = transformToWindow(bV, raster);

                rasterize(aToWindow, bToWindow, mesh);
            }
        }
    }

    private boolean isInView(Point3D a, Point3D b) {

        if (a.getX() < -a.getW() && b.getX() < -b.getW()) return false;
        if (a.getX() > a.getW() && b.getX() > b.getW()) return false;

        if (a.getY() < -a.getW() && b.getY() < -b.getW()) return false;
        if (a.getY() > a.getW() && b.getY() > b.getW()) return false;

        if (a.getZ() < -a.getW() && b.getZ() < -b.getW()) return false;
        if (a.getZ() > a.getW() && b.getZ() > b.getW()) return false;

        return true;
    }

    private boolean isInView(Point3D a, Point3D b, Point3D c) {

        if (a.getX() < -a.getW() && b.getX() < -b.getW() && c.getX() < -c.getW()) return false;
        if (a.getX() > a.getW() && b.getX() > b.getW() && c.getX() > c.getW()) return false;

        if (a.getY() < -a.getW() && b.getY() < -b.getW() && c.getY() < -c.getW()) return false;
        if (a.getY() > a.getW() && b.getY() > b.getW() && c.getY() > c.getW()) return false;

        if (a.getZ() < -a.getW() && b.getZ() < -b.getW() && c.getZ() < -c.getW()) return false;
        if (a.getZ() > a.getW() && b.getZ() > b.getW() && c.getZ() > c.getW()) return false;

        return true;
    }

    private Optional<Vec3D> tryDehomog(Point3D p) {
        if (p == null) return Optional.empty();
        return p.dehomog();
    }

    public static Vec3D transformToWindow(Vec3D v, BufferedImage raster) {
        int width = raster.getWidth();
        int height = raster.getHeight();
        double x = (v.getX() + 1) * 0.5 * width;
        double y = (1 - v.getY()) * 0.5 * height;
        double z = v.getZ();
        return new Vec3D(x, y, z);
    }

    public void rasterize(Vec3D v1, Vec3D v2, Mesh mesh) {
        Line line = new Line(new Point3D(v1.getX(), v1.getY(), v1.getZ()), new Point3D(v2.getX(), v2.getY(), v2.getZ()));
        lineRasterizer.vykresliLineDDA(line, raster, mesh.getColor());
    }

    public void setView(Mat4 view) {
        this.view = view;
    }

    public void setProj(Mat4 projection) {
        this.projection = projection;
    }
}
