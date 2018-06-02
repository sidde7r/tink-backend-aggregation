package se.tink.backend.common.workers.activity.renderers.svg.charts;

import java.awt.Rectangle;

@SuppressWarnings("serial")
public class RectF extends Rectangle.Float {

    public RectF(Rectangle rectangle) {
        super((float) rectangle.getX(), (float) rectangle.getY(), (float) rectangle.getWidth(), (float) rectangle
                .getHeight());
    }

    public RectF(RectF rectangle) {
        super((float) rectangle.getX(), (float) rectangle.getY(), (float) rectangle.getWidth(), (float) rectangle
                .getHeight());
    }
}
