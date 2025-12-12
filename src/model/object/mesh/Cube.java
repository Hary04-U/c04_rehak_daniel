package model.object.mesh;
import model.object.transforms.*;


public class Cube extends Mesh {
    public Cube() {
        Point3D p1 = new Point3D(-0.2, +0.2, 0);
        Point3D p2 = new Point3D(+0.2, +0.2, 0);
        Point3D p3 = new Point3D(+0.2, -0.2, 0);
        Point3D p4 = new Point3D(-0.2, -0.2, 0);

        Point3D p5 = new Point3D(-0.2, +0.2, 0.4);
        Point3D p6 = new Point3D(+0.2, +0.2, 0.4);
        Point3D p7 = new Point3D(+0.2, -0.2, 0.4);
        Point3D p8 = new Point3D(-0.2, -0.2, 0.4);

        vertexBuffer.add(p1);
        vertexBuffer.add(p2);
        vertexBuffer.add(p3);
        vertexBuffer.add(p4);
        vertexBuffer.add(p5);
        vertexBuffer.add(p6);
        vertexBuffer.add(p7);
        vertexBuffer.add(p8);

        addIndices(
                0,1, 1,2, 2,3, 3,0,
                4,5, 5,6, 6,7, 7,4,
                0,4, 1,5, 2,6, 3,7
        );

        addFace(0,1,2,3);
        addFace(4,5,6,7);
        addFace(0,1,5,4);
        addFace(2,3,7,6);
        addFace(1,2,6,5);
        addFace(0,3,7,4);

        this.modelMat = new Mat4Transl(0.5, 1, 0);
        setDefaultModelMat();
    }
}
