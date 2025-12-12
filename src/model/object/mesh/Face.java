package model.object.mesh;

import java.util.Arrays;
import java.util.List;

public class Face {
    private List<Integer> points;

    public Face(Integer... face) {
        this.points = Arrays.asList(face);
    }

    public List<Integer> getFacePoints() {
        return points;
    }
}
