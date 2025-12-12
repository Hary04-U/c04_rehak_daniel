package model.object.mesh.curve;

import model.object.mesh.Mesh;
import model.object.transforms.*;

import java.awt.*;

public class CurvePoint extends Mesh {

    public CurvePoint(Point3D center) {
        int latSegments = 12;
        int lonSegments = 24;

        for (int i = 0; i <= latSegments; i++) {
            double theta = Math.PI * i / latSegments;
            for (int j = 0; j <= lonSegments; j++) {
                double phi = 2.0 * Math.PI * j / lonSegments;

                double x = 0.02 * Math.sin(theta) * Math.cos(phi);
                double y = 0.02 * Math.sin(theta) * Math.sin(phi);
                double z = 0.02 * Math.cos(theta);

                vertexBuffer.add(new Point3D(x, y, z, 1.0));
            }
        }

        int vertsPerRow = lonSegments + 1;
        for (int i = 0; i < latSegments; i++) {
            for (int j = 0; j < lonSegments; j++) {
                int first = i * vertsPerRow + j;
                int second = first + vertsPerRow;

                this.indexBuffer.add(first);
                this.indexBuffer.add(second);
                this.indexBuffer.add(first + 1);

                this.indexBuffer.add(second);
                this.indexBuffer.add(second + 1);
                this.indexBuffer.add(first + 1);

                addFace(first, second, first + 1);
                addFace(second, second + 1, first + 1);
            }
        }

        this.color = Color.RED;
        setDefaultColor();

        this.modelMat = new Mat4Transl(center.getX(), center.getY(), center.getZ());
        setDefaultModelMat();
    }
}
