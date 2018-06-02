package se.tink.backend.common.utils;

import java.util.ArrayList;
import java.util.List;

import se.tink.backend.common.workers.activity.renderers.svg.charts.PointF;

public class GeometryUtils {

	public static List<PointF> CatmullRomSpline (List<PointF> points)
	{
		int granularity = 4;

        if (points.size() < 4) {
            return new ArrayList<PointF>(points);
        }

		ArrayList<PointF> smoothed = new ArrayList<PointF> ();

		smoothed.add (points.get(0));

		for (int i = 1; i < points.size() - 2; i++)
		{
			PointF p0 = points.get (i - 1);
			PointF p1 = points.get (i);
			PointF p2 = points.get (i + 1);
			PointF p3 = points.get (i + 2);

			for (int g = 1; g < granularity; g++)
			{
				float t = (float) g * (1.0f / (float) granularity);
				float tt = t * t;
				float ttt = tt * t;

//				PointF pi = new PointF(); // intermediate point
				float x = (float) 0.5 * (2*p1.x+(p2.x-p0.x)*t + (2*p0.x-5*p1.x+4*p2.x-p3.x)*tt + (3*p1.x-p0.x-3*p2.x+p3.x)*ttt);
				float y = (float) 0.5 * (2*p1.y+(p2.y-p0.y)*t + (2*p0.y-5*p1.y+4*p2.y-p3.y)*tt + (3*p1.y-p0.y-3*p2.y+p3.y)*ttt);

				
				smoothed.add (new PointF(x, y));
			}
			smoothed.add (p2);
		}

		smoothed.add (points.get(points.size() - 1));

		return smoothed;
	}
}
