package se.tink.backend.common.workers.activity.renderers.themes;

import java.awt.Color;

public class SectionColor {
    private Color color;
    private double from;
    private double to;
    
    public SectionColor() {
        
    }

    public SectionColor(double from, double to, Color color) {
        setFrom(from);
        setTo(to);
        setColor(color);
    }
    
    public Color getColor() {
        return color;
    }
    
    public double getFrom() {
        return from;
    }
    
    public double getTo() {
        return to;
    }
    
    public void setColor(Color color) {
        this.color = color;
    }
    
    public void setFrom(double from) {
        this.from = from;
    }
    
    public void setTo(double to) {
        this.to = to;
    }
}
