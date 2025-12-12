package model.object.mesh.axis;

import model.object.mesh.Mesh;
import model.object.transforms.Point3D;

import java.awt.*;

public class AxisX extends Mesh {
    public AxisX() {
        Point3D p1 = new Point3D(-1.5, 0, 0);
        Point3D p2 = new Point3D(1.5, 0, 0);

        vertexBuffer.add(p1);
        vertexBuffer.add(p2);

        addIndices(0, 1);
        setColor(Color.RED);
    }
}
