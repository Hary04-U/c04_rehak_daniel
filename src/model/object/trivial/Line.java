package model.object.trivial;

import model.object.transforms.Point3D;

public class Line {
    private Point3D startPoint3D;
    private Point3D endPoint3D;
    public Line(Point3D startPoint3D, Point3D endPoint3D) {
        this.startPoint3D = startPoint3D;
        this.endPoint3D = endPoint3D;
    }

    public Point3D getStartPoint3D() {
        return startPoint3D;
    }

    public Point3D getEndPoint3D() {
        return endPoint3D;
    }


}
