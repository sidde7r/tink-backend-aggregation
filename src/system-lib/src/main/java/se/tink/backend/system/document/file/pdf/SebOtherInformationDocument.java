package se.tink.backend.system.document.file.pdf;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.util.Map;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.pdmodel.PDPage;
import org.apache.pdfbox.pdmodel.font.PDType1Font;
import se.tink.backend.system.document.file.Document;
import se.tink.backend.system.document.file.utils.PdfboxDocument;
import se.tink.backend.system.document.file.utils.PdfboxPagePoint;
import se.tink.backend.utils.LogUtils;

public class SebOtherInformationDocument extends PdfboxDocument implements Document {

    private static final LogUtils log = new LogUtils(SebOtherInformationDocument.class);

    private final String fullName;
    private final String personalNumber;
    private final static String documentName = "SEB_OTHER_INFORMATION";
    private final String pdfFilePath;
    private final static int FONT_SIZE = 10;

    private final Map<String, String> additionalInformation;

    public SebOtherInformationDocument(String prefix, String fullName, String personalNumber, String pdfFilePath,
            Map<String, String> additionalInformation) {
        super(prefix, documentName, PDType1Font.HELVETICA, FONT_SIZE);
        this.fullName = fullName;
        this.personalNumber = personalNumber;
        this.pdfFilePath = pdfFilePath;
        this.additionalInformation = additionalInformation;
    }

    @Override
    public byte[] generateInMemoryDocument() {
        try {
            ByteArrayOutputStream output = new ByteArrayOutputStream();
            PDDocument document = PDDocument.load(new File(pdfFilePath));
            setFullName(document);
            setPersonalNumber(document);
            setAdditionalInformation(document);
            document.save(output);
            document.close();

            log.debug(String.format("Generated PDF from template %s.", pdfFilePath));
            return output.toByteArray();
        } catch (Exception e) {
            log.error("Could not create SEB other information pdf document.", e);
        }
        return null;
    }

    public void setFullName(PDDocument document) throws Exception {
        PdfboxPagePoint p = getPaths().get(Field.OTHER_INFORMATION_FULL_NAME);
        PDPage page = document.getDocumentCatalog().getPages().get(p.page);
        addTextToPage(document, page, p.x, p.y, fullName);

    }

    public void setPersonalNumber(PDDocument document) throws Exception {
        PdfboxPagePoint p = getPaths().get(Field.OTHER_INFORMATION_PERSONAL_NUMBER);
        PDPage page = document.getDocumentCatalog().getPages().get(p.page);
        addTextToPage(document, page, p.x, p.y, personalNumber);
    }

    public void setAdditionalInformation(PDDocument document) throws Exception {
        PdfboxPagePoint p = getPaths().get(Field.OTHER_INFORMATION_PAYLOAD);
        PDPage page = document.getDocumentCatalog().getPages().get(p.page);
        addMultilineTextWithHeaders(document, page, p.x, p.y, additionalInformation);
    }

}
