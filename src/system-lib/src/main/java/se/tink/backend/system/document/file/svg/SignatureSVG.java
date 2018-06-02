package se.tink.backend.system.document.file.svg;

import java.awt.Point;
import java.util.List;
import se.tink.backend.system.document.file.Document;
import se.tink.backend.utils.LogUtils;

public class SignatureSVG  implements Document {

    private List<List<Point>> signature;
    private final static String documentName = "SIGNATURE";
    private static final LogUtils log = new LogUtils(SignatureSVG.class);

    public SignatureSVG(List<List<Point>> signature) {
        this.signature = signature;
    }

    @Override
    public String getDocumentName() {
        return documentName;
    }

    @Override
    public String getDocumentNameWithExtension() {
        return getDocumentName() + ".svg";
    }

    @Override
    public byte[] generateInMemoryDocument() {

        if (signature == null || signature.isEmpty()) {
            log.error("Signature is null or empty");
            return null;
        }

        StringBuilder paths = new StringBuilder();
        for (List<Point> line : signature) {
            if (line.size() == 1) {
                Point point = line.get(0);
                paths.append(getCircleSVG(point));
            } else {
                paths.append(getPathSVG(line));
            }
        }

        int width = 0;
        int height = 0;
        for (List<Point> points : signature) {
            for (Point point : points) {
                if (point.x > width) {
                    width = point.x;
                }
                if (point.y > height) {
                    height = point.y;
                }
            }
        }
        String header = "<svg width=\"" + ceilBasedOnMultiplier(width,10)+ "\" height=\"" + ceilBasedOnMultiplier(height ,10)+ "\">";
        String footer = "</svg>";
        String svg = header + paths.toString() + footer;

        return svg.getBytes();
    }

    private String getCircleSVG(Point point) {
        return "<circle cx=\"" + point.x + "\" cy=\"" + point.y
                + "\" r=\"1\" stroke=\"blue\" fill=\"blue\" stroke-width=\"2\" /> ";
    }

    private String getPathSVG(List<Point> line) {
        StringBuilder b = new StringBuilder("<path stroke=\"blue\" fill=\"none\" d=\"");
        Boolean first = true;
        for (Point point : line) {
            String svgCoordinates;
            if (first) {
                svgCoordinates = "M" + point.x + " " + point.y + " ";
                first = false;
            } else {
                svgCoordinates = "L" + point.x + " " + point.y + " ";
            }
            b.append(svgCoordinates);
        }
        b.append("\" /> ");
        return b.toString();
    }

    private int ceilBasedOnMultiplier(int number, int multiple) {
        int result = number;

        if (number % multiple != 0) {
            int division = (number / multiple) + 1;
            result = division * multiple;
        }
        return result;
    }
}
