package model.object.mesh.curve;

import model.object.mesh.Mesh;
import model.object.transforms.*;

import java.awt.*;
import java.util.ArrayList;
import java.util.List;

public class CubicCurve extends Mesh {

    public enum Type {
        BEZIER,
        FERGUSON,
        COONS
    }

    private Type type;

    private CurvePoint p0, p1, p2, p3;
    private final int segments = 100;
    private final List<CurvePoint> curvePoints = new ArrayList<>();

    private final Mesh controlPolygon = new Mesh();

    private static final double[][] M_BEZIER = {
            {-1, 3, -3, 1},
            {3, -6, 3, 0},
            {-3, 3, 0, 0},
            {1, 0, 0, 0}
    };

    private static final double[][] M_HERMITE = {
            {2, -2, 1, 1},
            {-3, 3, -2, -1},
            {0, 0, 1, 0},
            {1, 0, 0, 0}
    };

    public CubicCurve(Type type) {
        this.type = type;

        switch (type) {
            case BEZIER:
                this.color = Color.MAGENTA;
                p0 = new CurvePoint(new Point3D(0, 0, 0));
                p1 = new CurvePoint(new Point3D(0, 0.5, 1));
                p2 = new CurvePoint(new Point3D(1, 0.5, 0));
                p3 = new CurvePoint(new Point3D(1, 1, 1));
                this.modelMat = new Mat4Transl(0.5, -0.8, 0);
                setDefaultModelMat();
                break;
            case FERGUSON:
                this.color = Color.CYAN;
                p0 = new CurvePoint(new Point3D(0, 0, 0));
                p1 = new CurvePoint(new Point3D(0, 1, 0.5));
                p2 = new CurvePoint(new Point3D(0.5, 0, 0.5));
                p3 = new CurvePoint(new Point3D(0.5, 0.5, 1));
                this.modelMat = new Mat4Transl(-0.7, -1, 0);
                setDefaultModelMat();
                break;
            case COONS:
                this.color = Color.ORANGE;
                p0 = new CurvePoint(new Point3D(0, 0, 0));
                p1 = new CurvePoint(new Point3D(0.5, 0, 1));
                p2 = new CurvePoint(new Point3D(0.5, 1, 0));
                p3 = new CurvePoint(new Point3D(1, 1, 1));
                this.modelMat = new Mat4Transl(-0.8, 0.5, 0);
                setDefaultModelMat();
                break;
        }

        curvePoints.add(p0);
        curvePoints.add(p1);
        curvePoints.add(p2);
        curvePoints.add(p3);

        setDefaultColor();
        controlPolygon.setColor(Color.GRAY);
        controlPolygon.setDefaultColor();

        for (CurvePoint cp : curvePoints) {
            cp.setModelMat(getModelMat().mul(cp.getDefaultModelMat()));
            cp.setDefaultModelMat(cp.getModelMat());
        }

        this.modelMat = new Mat4Identity();
        setDefaultModelMat();

        controlPolygon.setModelMat(this.modelMat);

        rebuild();
    }

    @Override
    public void setModelMat(Mat4 m) {
        super.setModelMat(m);

        controlPolygon.setModelMat(m);

        for (CurvePoint cp : curvePoints) {
            cp.setModelMat(m.mul(cp.getDefaultModelMat()));
        }

        rebuild();
    }

    public void rebuild() {
        vertexBuffer.clear();
        indexBuffer.clear();

        for (int i = 0; i <= segments; i++) {
            double t = i / (double) segments;
            vertexBuffer.add(evalPoint(t));
        }

        for (int i = 0; i < segments; i++) {
            addIndices(i, i + 1);
        }

        controlPolygon.getVertexBuffer().clear();
        controlPolygon.getIndexBuffer().clear();

        controlPolygon.getVertexBuffer().add(new Point3D(p0.getModelMat().getTranslate()));
        controlPolygon.getVertexBuffer().add(new Point3D(p1.getModelMat().getTranslate()));
        controlPolygon.getVertexBuffer().add(new Point3D(p2.getModelMat().getTranslate()));
        controlPolygon.getVertexBuffer().add(new Point3D(p3.getModelMat().getTranslate()));

        controlPolygon.getIndexBuffer().add(0);
        controlPolygon.getIndexBuffer().add(1);
        controlPolygon.getIndexBuffer().add(1);
        controlPolygon.getIndexBuffer().add(2);
        controlPolygon.getIndexBuffer().add(2);
        controlPolygon.getIndexBuffer().add(3);
    }

    private Point3D evalPoint(double t) {
        return switch (type) {
            case BEZIER -> bezierPoint(t);
            case FERGUSON -> fergusonPoint(t);
            case COONS -> coonsPoint(t);
        };
    }

    private Point3D evalMatrix(double t, double[][] M, Vec3D g0, Vec3D g1, Vec3D g2, Vec3D g3) {

        double t3 = t * t * t;
        double t2 = t * t;

        double[] T = {t3, t2, t, 1};
        double[] U = new double[4];

        for (int j = 0; j < 4; j++) {
            U[j] = T[0] * M[0][j] + T[1] * M[1][j] + T[2] * M[2][j] + T[3] * M[3][j];
        }

        Vec3D P =
                g0.mul(U[0])
                        .add(g1.mul(U[1]))
                        .add(g2.mul(U[2]))
                        .add(g3.mul(U[3]));

        return new Point3D(P.getX(), P.getY(), P.getZ());
    }

    private Point3D bezierPoint(double t) {
        return evalMatrix(t, M_BEZIER,
                p0.getModelMat().getTranslate(),
                p1.getModelMat().getTranslate(),
                p2.getModelMat().getTranslate(),
                p3.getModelMat().getTranslate()
        );
    }

    private Point3D fergusonPoint(double t) {

        Vec3D A = p0.getModelMat().getTranslate();
        Vec3D B = p1.getModelMat().getTranslate();
        Vec3D C = p2.getModelMat().getTranslate();
        Vec3D D = p3.getModelMat().getTranslate();

        Vec3D T0 = C.sub(A).mul(0.5);
        Vec3D T1 = D.sub(B).mul(0.5);

        return evalMatrix(t, M_HERMITE, B, C, T0, T1);
    }

    private Point3D coonsPoint(double t) {

        Vec3D A = p0.getModelMat().getTranslate();
        Vec3D B = p1.getModelMat().getTranslate();
        Vec3D C = p2.getModelMat().getTranslate();
        Vec3D D = p3.getModelMat().getTranslate();

        Vec3D T0 = B.sub(A).add(C.sub(B)).mul(0.5);
        Vec3D T1 = C.sub(B).add(D.sub(C)).mul(0.5);

        return evalMatrix(t, M_HERMITE, B, C, T0, T1);
    }

    public List<CurvePoint> getCurvePoints() {
        return curvePoints;
    }

    public Mesh getControlPolygonMesh() {
        return controlPolygon;
    }
}

