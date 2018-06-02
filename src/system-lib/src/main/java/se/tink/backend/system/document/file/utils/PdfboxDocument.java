package se.tink.backend.system.document.file.utils;

import com.google.common.base.Strings;
import com.google.common.collect.ImmutableMap;
import com.google.common.collect.Lists;
import java.awt.Point;
import java.util.List;
import java.util.Map;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.pdmodel.PDPage;
import org.apache.pdfbox.pdmodel.PDPageContentStream;
import org.apache.pdfbox.pdmodel.common.PDRectangle;
import org.apache.pdfbox.pdmodel.font.PDFont;
import org.apache.pdfbox.pdmodel.graphics.image.PDImageXObject;
import se.tink.backend.utils.LogUtils;

public class PdfboxDocument {

    private String prefix;
    private String documentName;
    private PDFont font;
    private Integer fontSize;
    private static final LogUtils log = new LogUtils(PdfboxDocument.class);

    private static final ImmutableMap<Field, PdfboxPagePoint> paths = new ImmutableMap.Builder<Field, PdfboxPagePoint>()
            .put(Field.POA_TRUSTOR_FULL_NAME, new PdfboxPagePoint(0, 80, 680))
            .put(Field.POA_TRUSTOR_PERSONAL_NUMBER, new PdfboxPagePoint(0, 420, 680))
            .put(Field.POA_TRUSTOR_ADDRESS, new PdfboxPagePoint(0, 80, 620))
            .put(Field.POA_TRUSTOR_POSTAL_CODE, new PdfboxPagePoint(0, 320, 620))
            .put(Field.POA_TRUSTOR_LOCATION, new PdfboxPagePoint(0, 420, 620))
            .put(Field.POA_CLAUSE, new PdfboxPagePoint(0, 80, 330))
            .put(Field.POA_SIGNING_LOCATION, new PdfboxPagePoint(0, 80, 195))
            .put(Field.POA_REVOCATION_DATE, new PdfboxPagePoint(0, 300, 235))
            .put(Field.POA_SIGNING_DATE, new PdfboxPagePoint(0, 320, 195))
            .put(Field.POA_SIGNATURE, new PdfboxPagePoint(0, 80, 120))
            .put(Field.POA_PRINTED_NAME, new PdfboxPagePoint(0, 320, 130))
            .put(Field.SALARY_FULL_NAME, new PdfboxPagePoint(0, 80, 680))
            .put(Field.SALARY_PERSONAL_NUMBER, new PdfboxPagePoint(0, 420, 680))
            .put(Field.SALARY_SALARIES, new PdfboxPagePoint(0, 70, 610))
            .put(Field.OTHER_INFORMATION_FULL_NAME, new PdfboxPagePoint(0, 80, 680))
            .put(Field.OTHER_INFORMATION_PERSONAL_NUMBER, new PdfboxPagePoint(0, 420, 680))
            .put(Field.OTHER_INFORMATION_PAYLOAD, new PdfboxPagePoint(0, 70, 610))
            .build();

    public enum Field {
        POA_TRUSTOR_FULL_NAME,
        POA_TRUSTOR_PERSONAL_NUMBER,
        POA_TRUSTOR_ADDRESS,
        POA_TRUSTOR_POSTAL_CODE,
        POA_TRUSTOR_LOCATION,
        POA_CLAUSE,
        POA_SIGNING_LOCATION,
        POA_REVOCATION_DATE,
        POA_SIGNING_DATE,
        POA_SIGNATURE,
        POA_PRINTED_NAME,
        SALARY_FULL_NAME,
        SALARY_PERSONAL_NUMBER,
        SALARY_SALARIES,
        OTHER_INFORMATION_FULL_NAME,
        OTHER_INFORMATION_PERSONAL_NUMBER,
        OTHER_INFORMATION_PAYLOAD
    }

    public ImmutableMap<Field, PdfboxPagePoint> getPaths() {
        return paths;
    }

    public PdfboxDocument(String prefix, String documentName, PDFont font, Integer fontSize) {
        this.prefix = prefix;
        this.documentName = documentName;
        this.font = font;
        this.fontSize = fontSize;
    }

    public PdfboxDocument(String documentName, PDFont font, Integer fontSize) {
        this(null, documentName, font, fontSize);
    }

    public String getDocumentName() {
        return documentName;
    }

    public String getDocumentNameWithExtension() {
        if (Strings.isNullOrEmpty(prefix)) {
            return getDocumentName() + ".pdf";
        }
        return prefix + "_" + getDocumentName() + ".pdf";
    }

    public void addTextToPage(PDDocument document, PDPage page, Integer x, Integer y, String text) {
        try {
            PDPageContentStream contentStream = new PDPageContentStream(
                    document,
                    page,
                    PDPageContentStream.AppendMode.APPEND,
                    true);

            contentStream.beginText();
            contentStream.setFont(font, fontSize);
            contentStream.newLineAtOffset(x, y);
            contentStream.showText(text);
            contentStream.endText();
            contentStream.close();

        } catch (Exception e) {
            log.error("Could not add text to document.", e);
        }
    }

    public void addPngToPage(PDDocument document, PDPage page, Integer x, Integer y, byte[] image, Integer height) {

        try {
            PDImageXObject pdImage = PDImageXObject.createFromByteArray(document, image, "image");
            Integer originalHeight = pdImage.getHeight();
            Integer width = pdImage.getWidth() / (originalHeight / height);
            PDPageContentStream contentStream = new PDPageContentStream(document, page,
                    PDPageContentStream.AppendMode.APPEND, true);
            contentStream.drawImage(pdImage, x, y, width, height);
            contentStream.close();
        } catch (Exception e) {
            log.error("Could not add image to document.", e);
        }
    }

    /**
     * Adds text to the pdf by calculating the text width and determining where it should break line.
     */
    public Point addMultilineTextToPage(PDDocument document, PDPage page, Integer x, Integer y, String text) {
        try {
            float leading = 1.5f * fontSize;
            PDRectangle mediabox = page.getMediaBox();
            float margin = x;
            float width = mediabox.getWidth() - 2 * margin;

            List<String> lines = Lists.newArrayList();
            int lastSpace = -1;
            while (text.length() > 0) {
                int spaceIndex = text.indexOf(' ', lastSpace + 1);
                if (spaceIndex < 0) {
                    spaceIndex = text.length();
                }
                String subString = text.substring(0, spaceIndex);
                float size = fontSize * font.getStringWidth(subString) / 1000;
                if (size > width) {
                    if (lastSpace < 0) {
                        lastSpace = spaceIndex;
                    }
                    String newLine = text.substring(0, lastSpace);
                    lines.add(newLine);
                    text = text.substring(lastSpace).trim();
                    lastSpace = -1;
                } else if (subString.charAt(subString.length() - 1) == '.') {
                    lines.add(subString);
                    text = text.substring(spaceIndex).trim();
                    lastSpace = -1;
                } else if (spaceIndex == text.length()) {
                    lines.add(text);
                    text = "";
                } else {
                    lastSpace = spaceIndex;
                }
            }

            PDPageContentStream contentStream = new PDPageContentStream(document, page,
                    PDPageContentStream.AppendMode.APPEND,
                    true);
            contentStream.beginText();
            contentStream.setFont(font, fontSize);
            contentStream.newLineAtOffset(x, y);
            for (String line : lines) {
                contentStream.showText(line);
                contentStream.newLineAtOffset(0, -leading);
                y = y - (int) leading;
            }
            contentStream.endText();
            contentStream.close();

            return new Point(x, y);

        } catch (Exception e) {
            log.error("Could not write a multiline text to pdf.", e);
            return null;
        }
    }

    public void addMultilineTextWithHeaders(PDDocument document, PDPage page, Integer x, Integer y,
            Map<String, String> texts) {
        float leading = 1.1f * fontSize;
        Point p = new Point(x, y);
        int xMargin = 30;

        for (Map.Entry<String, String> e : texts.entrySet()) {
            p = addMultilineTextToPage(document, page, p.x, p.y, e.getKey());
            p = addMultilineTextToPage(document, page, p.x + xMargin, p.y, e.getValue());
            p = new Point(p.x - xMargin, p.y - (int) (leading * 1.8));
        }
    }

    public void drawTable(PDDocument document, PDPage page, int x, int y, String[][] content) {
        try {
            final float rowHeight = 1.5f * fontSize;
            final float colWidth = 90;
            final float cellMargin = 2f;

            PDPageContentStream contentStream = new PDPageContentStream(document, page,
                    PDPageContentStream.AppendMode.APPEND,
                    true);

            contentStream.setFont(font, fontSize);
            float textx = x + cellMargin;
            float texty = y - 15;
            for (int i = 0; i < content.length; i++) {
                for (int j = 0; j < content[i].length; j++) {
                    String text = content[i][j];
                    contentStream.beginText();
                    contentStream.newLineAtOffset(textx, texty);
                    contentStream.showText(text);
                    contentStream.endText();
                    textx += colWidth;
                }
                texty -= rowHeight;
                textx = x + cellMargin;
            }

            contentStream.close();

        } catch (Exception e) {
            log.error("Could not draw table on document.", e);
        }
    }

}
