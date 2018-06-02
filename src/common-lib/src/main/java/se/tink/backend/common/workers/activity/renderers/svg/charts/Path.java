package se.tink.backend.common.workers.activity.renderers.svg.charts;

import java.util.ArrayList;
import java.util.List;

public class Path {

    private final ArrayList<PointF> points = new ArrayList<PointF>();
    private float maxY = Float.MIN_VALUE;
    private float minY = Float.MAX_VALUE;

    public List<PointF> getPoints() {
        return points;
    }

    public void moveTo(float x, float y) {
        lineTo(x, y);
    }

    public void lineTo(float x, float y) {
        maxY = Math.max(maxY, y);
        minY = Math.min(minY, y);
        points.add(new PointF(x, y));
    }

    public PointF firstPoint()
    {
        if (points.size() > 0) {
            return points.get(0);
        }
        return null;
    }

    public PointF lastPoint()
    {
        if (points.size() > 0) {
            return points.get(points.size() - 1);
        }
        return null;
    }

    public float getMaxY()
    {
        return maxY;
    }

    public float getMinY()
    {
        return minY;
    }
}
