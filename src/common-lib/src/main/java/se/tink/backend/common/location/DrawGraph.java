package se.tink.backend.common.location;

import java.awt.Color;
import java.awt.Font;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.awt.font.FontRenderContext;
import java.awt.font.LineMetrics;
import java.awt.geom.Ellipse2D;
import java.awt.geom.Line2D;

import javax.swing.JFrame;
import javax.swing.JPanel;

@SuppressWarnings("serial")
public class DrawGraph extends JPanel {

    private double[] xData;
    private double[] yData;
    private double[] xCoord;
    private double[] yCoord;
    private String yName;
    private final int PAD = 20;

    public DrawGraph(String yName, double[] x, double[] y, double[] xCoordinates, double[] yCoordinates) {
        this.yName = yName;
        this.xData = x;
        this.yData = y;
        this.xCoord = xCoordinates;
        this.yCoord = yCoordinates;
    }

    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);
        Graphics2D g2 = (Graphics2D) g;
        g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING,
                RenderingHints.VALUE_ANTIALIAS_ON);
        int w = getWidth();
        int h = getHeight();

        // Draw ordinate.

        g2.draw(new Line2D.Double(PAD, PAD, PAD, h - PAD));

        // Draw abcissa.

        g2.draw(new Line2D.Double(PAD, h - PAD, w - PAD, h - PAD));

        // Draw labels.
        Font font = g2.getFont();
        FontRenderContext frc = g2.getFontRenderContext();
        LineMetrics lm = font.getLineMetrics("0", frc);
        float sh = lm.getAscent() + lm.getDescent();

        // Ordinate label.

        String s = yName;
        float sy = PAD + ((h - 2 * PAD) - s.length() * sh) / 2 + lm.getAscent();
        for (int i = 0; i < s.length(); i++) {
            String letter = String.valueOf(s.charAt(i));
            float sw = (float) font.getStringBounds(letter, frc).getWidth();
            float sx = (PAD - sw) / 2;
            g2.drawString(letter, sx, sy);
            sy += sh;
        }

        // Abcissa label.

        s = "time";
        sy = h - PAD + (PAD - sh) / 2 + lm.getAscent();
        float sw = (float) font.getStringBounds(s, frc).getWidth();
        float sx = (w - sw) / 2;
        g2.drawString(s, sx, sy);

        // Draw lines.

        double xInc = (double) (w - 2 * PAD) / (xData.length - 1);
        double scale = (double) (h - 2 * PAD) / getMax();
        g2.setPaint(Color.green.darker());
        for (int i = 0; i < xData.length - 1; i++) {
            double x1 = PAD + xData[i] * xInc;
            double y1 = h - PAD - scale * yData[i];
            double x2 = PAD + xData[i + 1] * xInc;
            double y2 = h - PAD - scale * yData[i + 1];
            g2.draw(new Line2D.Double(x1, y1, x2, y2));
        }

        // Mark data points.

        g2.setPaint(Color.red);
        for (int i = 0; i < xCoord.length; i++) {
            double x = PAD + xCoord[i] * xInc;
            double y = h - PAD - scale * yCoord[i];
            g2.fill(new Ellipse2D.Double(x - 2, y - 2, 4, 4));
        }
    }

    private double getMax() {
        double max = -Double.MAX_VALUE;
        for (int i = 0; i < yData.length; i++) {
            if (yData[i] > max) {
                max = yData[i];
            }
        }
        return max;
    }
    
    private double getMin() {
        double min = Double.MAX_VALUE;
        for (int i = 0; i < yData.length; i++) {
            if (yData[i] < min) {
                min = yData[i];
            }
        }
        return min;
    }

    public static void draw(String yName, double[] xData, double[] yData, double[] xCoordinates, double[] yCoordinates) {
        JFrame f = new JFrame();
        f.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        f.add(new DrawGraph(yName, xData, yData, xCoordinates, yCoordinates));
        f.setSize(600, 600);
        f.setLocation(200, 200);
        f.setVisible(true);
    }
    
    public static void main(String[] args) {
        double[] xData = {
                1, 2, 3, 4, 5, 6, 7, 8, 9, 10,
                11, 12, 13, 14, 15, 16, 17, 18, 19, 20
        };
        double[] yData = {
                21, 14, 18, 03, 86, 88, 74, 87, 54, 77,
                61, 55, 48, 60, 49, 36, 38, 27, 20, 18
        };
        double[] xCoordinates = {
                1, 4, 6, 8, 
                10, 14, 15, 18, 19
        };
        double[] yCoordinates = {
                21, 03, 74, 54, 
                61, 49, 36, 20, 18
        };

        JFrame f = new JFrame();
        f.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        f.add(new DrawGraph("y", xData, yData, xCoordinates, yCoordinates));
        f.setSize(600, 600);
        f.setLocation(200, 200);
        f.setVisible(true);
    }
}
