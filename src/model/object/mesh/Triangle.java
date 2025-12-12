package model.object.mesh;

import model.object.transforms.*;

public class Triangle extends Mesh {

    public Triangle() {
        Point3D p1 = new Point3D(0.63, 0.38, 0);
        Point3D p2 = new Point3D(0.70, 0.45, 0);
        Point3D p3 = new Point3D(0.52, 0.5, 0);

        vertexBuffer.add(p1);
        vertexBuffer.add(p2);
        vertexBuffer.add(p3);

        addIndices(0, 1,
                   1, 2,
                   2, 0
        );
    }

}
