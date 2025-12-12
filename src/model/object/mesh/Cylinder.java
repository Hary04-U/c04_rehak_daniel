package model.object.mesh;

import model.object.transforms.*;

import java.awt.*;

public class Cylinder extends Mesh {
    public Cylinder() {
        int segments = 12;
        double radius = 0.2;
        double height = 0.4;

        for (int i = 0; i < segments; i++) {
            double angle = 2 * Math.PI * i / segments;
            double x = radius * Math.cos(angle);
            double y = radius * Math.sin(angle);

            vertexBuffer.add(new Point3D(x, y, height / 2));
            vertexBuffer.add(new Point3D(x, y, -height / 2));
        }

        for (int i = 0; i < segments; i++) {
            int next = (i + 1) % segments;
            addIndices(i * 2, next * 2);
        }

        for (int i = 0; i < segments; i++) {
            int next = (i + 1) % segments;
            addIndices(i * 2 + 1, next * 2 + 1);
        }

        for (int i = 0; i < segments; i++) {
            addIndices(i * 2, i * 2 + 1);
        }

        for (int i = 0; i < segments; i++) {

            int top1 = i * 2;
            int bot1 = i * 2 + 1;

            int top2 = (i == segments - 1) ? 0 : top1 + 2;
            int bot2 = (i == segments - 1) ? 1 : bot1 + 2;

            addFace(top1, top2, bot2, bot1);
        }

        {
            Integer[] topFace = new Integer[segments];
            for (int i = 0; i < segments; i++) {
                topFace[i] = i * 2;
            }
            addFace(topFace);
        }

        {
            Integer[] bottomFace = new Integer[segments];
            for (int i = 0; i < segments; i++) {
                bottomFace[i] = i * 2 + 1;
            }
            addFace(bottomFace);
        }

        this.color = Color.WHITE;
        setDefaultColor();
    }
}
