package se.tink.backend.system.document.file.utils;

public class PdfboxPagePoint {
    public final Integer page;
    public final Integer x;
    public final Integer y;

    public PdfboxPagePoint(Integer page, Integer x, Integer y) {
        this.page = page;
        this.x = x;
        this.y = y;
    }
}
