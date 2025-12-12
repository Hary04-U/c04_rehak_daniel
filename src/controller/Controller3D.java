package controller;

import model.object.mesh.axis.*;
import model.object.mesh.curve.CubicCurve;
import model.object.mesh.curve.CurvePoint;
import model.object.transforms.*;
import model.object.trivial.Point2D;
import model.object.mesh.*;
import model.rasterizer.LineRasterizer;
import model.renderer.Renderer3D;
import view.Pane;

import java.awt.*;
import java.awt.event.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.HashMap;

import static model.object.mesh.Mesh.isInside;

public class Controller3D implements Controller {
    private final Pane panel;
    private Renderer3D renderer;
    private LineRasterizer lineRasterizer;

    private AxisX axisX;
    private AxisY axisY;
    private AxisZ axisZ;

    private Cube cube;
    private Cylinder cylinder;
    private CubicCurve cubicCurveBezier;
    private CubicCurve cubicCurveFerguson;
    private CubicCurve cubicCurveCoons;

    private final List<Mesh> meshList = new ArrayList<>();
    private Mesh selectedMesh;

    private Camera view;
    private Mat4 projection;

    private final double speedMove = 0.01;
    private final double zScale = 0.01;
    private final double speedRot = 0.01;
    private final double angleRot = 15.d;
    private final double scaleInc = 1.1;
    private final double scaleDec = 0.9;
    private final double speedZoom = 0.2;
    private final double speedCamera = 0.2;
    private final double fov = Math.toRadians(90);

    private Mat4 matRot;
    private Mat4 matTransl;
    private Mat4 matTranslInv;
    private Mat4 matScale;

    private Vec3D position;

    private int lastX;
    private int lastY;

    private boolean projectionSwap = false;

    private final Map<CurvePoint, Mat4> cpDefaultMats = new HashMap<>();

    public Controller3D(Pane panel) {
        this.panel = panel;

        initObjects();
        listeners();
        renderScene();
    }

    @Override
    public void initObjects() {
        axisX = new AxisX();
        axisY = new AxisY();
        axisZ = new AxisZ();

        view = new Camera(new Vec3D(1.5, 1.5, 1.5), Math.PI * 1.25, Math.toRadians(-30), 3, true);

        projection = new Mat4PerspRH(fov, (double) panel.getHeight() / panel.getWidth(), 0.1, 100);

        lineRasterizer = new LineRasterizer(panel.getRaster());
        renderer = new Renderer3D(lineRasterizer, panel.getRaster());
        renderer.setView(view.getViewMatrix());
        renderer.setProj(projection);

        cube = new Cube();
        cylinder = new Cylinder();

        cubicCurveBezier = new CubicCurve(CubicCurve.Type.BEZIER);
        meshList.add(cubicCurveBezier);
        meshList.addAll(cubicCurveBezier.getCurvePoints());

        cubicCurveFerguson = new CubicCurve(CubicCurve.Type.FERGUSON);
        meshList.add(cubicCurveFerguson);
        meshList.addAll(cubicCurveFerguson.getCurvePoints());

        cubicCurveCoons = new CubicCurve(CubicCurve.Type.COONS);
        meshList.add(cubicCurveCoons);
        meshList.addAll(cubicCurveCoons.getCurvePoints());

        for (Mesh mesh : meshList) {
            if (mesh instanceof CubicCurve curve) {
                for (CurvePoint cp : curve.getCurvePoints()) {
                    cpDefaultMats.put(cp, cp.getDefaultModelMat());
                }
            }
        }

        meshList.add(cylinder);
        meshList.add(cube);
    }

    @Override
    public void listeners() {
        panel.addMouseListener(new MouseAdapter() {
            public void mousePressed(MouseEvent e) {
                selectedMesh = null;
                lastX = e.getX();
                lastY = e.getY();
                Point2D click = new Point2D(e.getX(), e.getY());
                for (Mesh mesh : meshList) {
                    if (isInside(click, mesh, panel.getRaster(), view.getViewMatrix(), projection)) {
                        if (selectedMesh == null) {
                            selectedMesh = mesh;
                            mesh.setColor(Color.YELLOW);
                        } else {
                            mesh.setColor(mesh.getDefaultColor());
                        }
                    } else {
                        mesh.setColor(mesh.getDefaultColor());
                    }
                }
                renderScene();
            }
        });

        panel.addMouseMotionListener(new MouseMotionAdapter() {
            @Override
            public void mouseDragged(MouseEvent e) {
                int dx = e.getX() - lastX;
                int dy = e.getY() - lastY;

                lastX = e.getX();
                lastY = e.getY();

                if (selectedMesh != null) {
                    if ((e.getModifiersEx() & MouseEvent.SHIFT_DOWN_MASK) != 0 && !(selectedMesh instanceof CurvePoint)) {
                        matRot = new Mat4Rot(dx * speedRot, new Vec3D(0, 0, 1));
                        Vec3D pos = selectedMesh.getModelMat().getTranslate();
                        matTransl = new Mat4Transl(-pos.getX(), -pos.getY(), -pos.getZ());
                        matTranslInv = new Mat4Transl(pos.getX(), pos.getY(), pos.getZ());
                        matTransform(matTransl, matRot, matTranslInv);
                    } else if ((e.getModifiersEx() & MouseEvent.CTRL_DOWN_MASK) != 0) {
                        double z = -dy * zScale;
                        matTransl = new Mat4Transl(0, 0, z);
                        matTransform(matTransl);
                    } else {
                        Mat4 viewMat = view.getViewMatrix();
                        Vec3D forwardCam = new Vec3D(
                                -viewMat.get(0, 2),
                                -viewMat.get(1, 2),
                                -viewMat.get(2, 2)
                        );

                        Vec3D forward = forwardCam.normalized().orElse(new Vec3D(0, 0, -1));

                        Vec3D worldUp = new Vec3D(0, 0, 1);

                        Vec3D right = forward.cross(worldUp).normalized().orElse(new Vec3D(1, 0, 0));
                        Vec3D forwardHoriz = worldUp.cross(right).normalized().orElse(new Vec3D(0, 1, 0));

                        Vec3D worldMove = right.mul(dx * speedMove).add(forwardHoriz.mul(-dy * speedMove));
                        worldMove = new Vec3D(worldMove.getX(), worldMove.getY(), 0.0);

                        matTransl = new Mat4Transl(worldMove.getX(), worldMove.getY(), worldMove.getZ());
                        matTransform(matTransl);
                    }
                } else {
                    view = view.addAzimuth(-dx * speedRot).addZenith(-dy * speedRot);
                }

                renderScene();
            }
        });

        panel.addMouseWheelListener(e -> {
            int rot = e.getWheelRotation();
            if (rot < 0) view = view.forward(speedZoom);
            else view = view.backward(speedZoom);

            renderScene();
        });

        panel.addKeyListener(new KeyAdapter() {
            @Override
            public void keyPressed(KeyEvent e) {
                if (e.getKeyCode() == KeyEvent.VK_C) {
                    hardClean();
                }

                if (e.getKeyCode() == KeyEvent.VK_R) {
                    for (Mesh mesh : meshList) {
                        if (mesh instanceof CubicCurve curve) {
                            curve.setModelMat(curve.getDefaultModelMat());
                            for (CurvePoint cp : curve.getCurvePoints()) {
                                Mat4 defaultMat = cpDefaultMats.get(cp);
                                if (defaultMat != null) {
                                    cp.setDefaultModelMat(defaultMat);
                                } else {
                                    cp.setDefaultModelMat(cp.getModelMat());
                                }
                                cp.setModelMat(curve.getModelMat().mul(cp.getDefaultModelMat()));
                            }
                            curve.rebuild();
                        } else {
                            mesh.setModelMat(mesh.getDefaultModelMat());
                        }
                        mesh.setColor(mesh.getDefaultColor());
                    }
                    selectedMesh = null;
                }

                if (e.getKeyCode() == KeyEvent.VK_P) {

                    if (projectionSwap) {
                        projection = new Mat4PerspRH(
                                Math.toRadians(90),
                                (double) panel.getHeight() / panel.getWidth(),
                                0.1,
                                100
                        );
                    } else {
                        projection = new Mat4OrthoRH(
                                panel.getWidth() / 100.0,
                                panel.getHeight() / 100.0,
                                0.1,
                                100
                        );
                    }

                    projectionSwap = !projectionSwap;

                    renderer.setProj(projection);
                }

                switch (e.getKeyCode()) {
                    case KeyEvent.VK_W:
                        view = view.forward(speedCamera);
                        break;

                    case KeyEvent.VK_S:
                        view = view.backward(speedCamera);
                        break;

                    case KeyEvent.VK_A:
                        view = view.left(speedCamera);
                        break;

                    case KeyEvent.VK_D:
                        view = view.right(speedCamera);
                        break;
                }

                if (selectedMesh == null) {
                    switch (e.getKeyCode()) {

                        case KeyEvent.VK_Q:
                            matRot = new Mat4Rot(Math.toRadians(angleRot), new Vec3D(0, 0, 1));
                            matTransform(matRot);
                            break;

                        case KeyEvent.VK_E:
                            matRot = new Mat4Rot(Math.toRadians(-angleRot), new Vec3D(0, 0, 1));
                            matTransform(matRot);
                            break;

                        case KeyEvent.VK_SHIFT:
                            view = view.up(speedCamera);
                            break;

                        case KeyEvent.VK_CONTROL:
                            view = view.down(speedCamera);
                            break;

                    }
                } else {
                    position = selectedMesh.getModelMat().getTranslate();
                    matTransl = new Mat4Transl(-position.getX(), -position.getY(), -position.getZ());
                    matTranslInv = new Mat4Transl(position.getX(), position.getY(), position.getZ());
                    if (!(selectedMesh instanceof CurvePoint)) {
                        switch (e.getKeyCode()) {

                            case KeyEvent.VK_ESCAPE:
                                for (Mesh mesh : meshList) {
                                    mesh.setColor(mesh.getDefaultColor());
                                }
                                selectedMesh = null;
                                break;

                            case KeyEvent.VK_Q:
                                matRot = new Mat4Rot(Math.toRadians(angleRot), new Vec3D(0, 0, 1));
                                matTransform(matTransl, matRot, matTranslInv);
                                break;

                            case KeyEvent.VK_E:
                                matRot = new Mat4Rot(Math.toRadians(-angleRot), new Vec3D(0, 0, 1));
                                matTransform(matTransl, matRot, matTranslInv);
                                break;

                            case KeyEvent.VK_Y:
                                matScale = new Mat4Scale(scaleDec, scaleDec, scaleDec);
                                matTransform(matTransl, matScale, matTranslInv);
                                break;

                            case KeyEvent.VK_X:
                                matScale = new Mat4Scale(scaleInc, scaleInc, scaleInc);
                                matTransform(matTransl, matScale, matTranslInv);
                                break;
                        }
                    }
                }

                renderScene();
            }
        });
    }

    private void renderScene() {
        panel.clean();

        renderer.setView(view.getViewMatrix());

        renderer.render(axisX);
        renderer.render(axisY);
        renderer.render(axisZ);

        for (Mesh mesh : meshList) {
            if (mesh instanceof CubicCurve curve) {
                renderer.render(curve);
                renderer.render(curve.getControlPolygonMesh());
            } else {
                renderer.render(mesh);
            }
        }

        panel.repaint();
    }

    @Override
    public void hardClean() {
        meshList.clear();
        panel.repaint();
    }

    public void matTransform(Mat4... mats) {
        if (selectedMesh != null) {
            if (selectedMesh instanceof CurvePoint cp) {
                for (Mat4 m : mats) {
                    cp.setDefaultModelMat(cp.getDefaultModelMat().mul(m));
                }
                for (Mesh mesh : meshList) {
                    if (mesh instanceof CubicCurve curve && curve.getCurvePoints().contains(cp)) {
                        cp.setModelMat(curve.getModelMat().mul(cp.getDefaultModelMat()));
                        curve.rebuild();
                        break;
                    }
                }
            } else {
                for (Mat4 m : mats) {
                    selectedMesh.setModelMat(selectedMesh.getModelMat().mul(m));
                }
            }

            return;
        }
        for (Mesh mesh : meshList) {
            for (Mat4 m : mats) {
                mesh.setModelMat(mesh.getModelMat().mul(m));
            }
        }
    }
}

