package se.tink.backend.common.utils;

import java.awt.geom.Path2D;
import java.util.List;
import se.tink.backend.common.workers.activity.renderers.svg.charts.Path;
import se.tink.backend.common.workers.activity.renderers.svg.charts.PointF;

/**
 * Code copied from https://github.com/tink-ab/tink-frontend-mobile project
 */
public class BezierSpline {

    public static Path2D.Float getBezierSpline(Path input) {
        List<PointF> points = input.getPoints();

        Path2D.Float path = new Path2D.Float();

        if (points != null && points.size() > 0) {
            PointF firstPoint = points.get(0);

            path.moveTo(firstPoint.getX(), firstPoint.getY());

            PointF p1 = new PointF(points.get(0).x, points.get(0).y);

            for (int i = 1; i < points.size(); i++) {
                PointF p2 = points.get(i);
                PointF m = middleBetweenPoints(p1, p2);

                PointF c1 = controlPointForPoints(m, p1);
                path.quadTo(c1.x, c1.y, m.x, m.y);

                PointF c2 = controlPointForPoints(m, p2);
                path.quadTo(c2.x, c2.y, p2.x, p2.y);

                p1 = p2;
            }
        }
        return path;
    }

    private static PointF middleBetweenPoints(PointF p1, PointF p2) {
        return new PointF((p1.x + p2.x) * 0.5f, (p1.y + p2.y) * 0.5f);
    }

    private static PointF controlPointForPoints(PointF p1, PointF p2) {
        PointF controlPoint = middleBetweenPoints(p1, p2);

        double diff = Math.abs(p2.y - controlPoint.y);
        if (p1.y < p2.y) {
            controlPoint.y += diff;
        } else if (p1.y > p2.y) {
            controlPoint.y -= diff;
        }
        return controlPoint;
    }
}
