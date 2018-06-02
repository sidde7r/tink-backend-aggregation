package se.tink.backend.system.document.file.pdf;

import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.pdmodel.PDPage;
import org.apache.pdfbox.pdmodel.font.PDType1Font;
import se.tink.backend.system.document.file.Document;
import se.tink.backend.system.document.file.utils.PdfboxDocument;
import se.tink.backend.system.document.file.utils.PdfboxPagePoint;

abstract public class PowerOfAttorneyDocument extends PdfboxDocument implements Document {

    private static final Integer FONT_SIZE = 8;

    private String fullName;
    private String personalNumber;
    private String address;
    private String postalCode;
    private String city;
    private String clause;
    private String expirationDate;
    private String signingLocation;
    private String signingDate;
    private byte[] signature;
    private String printedName;
    private String pdfPath;

    public PowerOfAttorneyDocument(
            String prefix, String documentName, String trustorFullName, String trustorNationalNumber,
            String trustorAddress, String trustorPostNumber,
            String trustorLocation, String clause, String revocationDate, String signingLocation,
            String signingDate, byte[] signature, String printedName,
            String pdfPath) {
        super(prefix, documentName, PDType1Font.HELVETICA, FONT_SIZE);
        this.fullName = trustorFullName;
        this.personalNumber = trustorNationalNumber;
        this.address = trustorAddress;
        this.postalCode = trustorPostNumber;
        this.city = trustorLocation;
        this.clause = clause;
        this.expirationDate = revocationDate;
        this.signingLocation = signingLocation;
        this.signingDate = signingDate;
        this.signature = signature;
        this.printedName = printedName;
        this.pdfPath = pdfPath;
    }

    public byte[] getSignature() {
        return signature;
    }

    public void setFullName(PDDocument document) throws Exception {
        PdfboxPagePoint p = getPaths().get(Field.POA_TRUSTOR_FULL_NAME);
        PDPage page = document.getDocumentCatalog().getPages().get(p.page);
        addTextToPage(document, page, p.x, p.y, fullName);
    }

    public void setPersonalNumber(PDDocument document) throws Exception {
        PdfboxPagePoint p = getPaths().get(Field.POA_TRUSTOR_PERSONAL_NUMBER);
        PDPage page = document.getDocumentCatalog().getPages().get(p.page);
        addTextToPage(document, page, p.x, p.y, personalNumber);
    }

    public void setAddress(PDDocument document) throws Exception {
        PdfboxPagePoint p = getPaths().get(Field.POA_TRUSTOR_ADDRESS);
        PDPage page = document.getDocumentCatalog().getPages().get(p.page);
        addTextToPage(document, page, p.x, p.y, address);
    }

    public void setPostalCode(PDDocument document) throws Exception {
        PdfboxPagePoint p = getPaths().get(Field.POA_TRUSTOR_POSTAL_CODE);
        PDPage page = document.getDocumentCatalog().getPages().get(p.page);
        addTextToPage(document, page, p.x, p.y, postalCode);
    }

    public void setCity(PDDocument document) throws Exception {
        PdfboxPagePoint p = getPaths().get(Field.POA_TRUSTOR_LOCATION);
        PDPage page = document.getDocumentCatalog().getPages().get(p.page);
        addTextToPage(document, page, p.x, p.y, city);
    }

    public void setClause(PDDocument document) throws Exception {
        PdfboxPagePoint p = getPaths().get(Field.POA_CLAUSE);
        PDPage page = document.getDocumentCatalog().getPages().get(p.page);
        addMultilineTextToPage(document, page, p.x, p.y, clause);
    }

    public void setSigningLocation(PDDocument document) throws Exception {
        PdfboxPagePoint p = getPaths().get(Field.POA_SIGNING_LOCATION);
        PDPage page = document.getDocumentCatalog().getPages().get(p.page);
        addTextToPage(document, page, p.x, p.y, signingLocation);
    }

    public void setExpirationDate(PDDocument document) throws Exception {
        PdfboxPagePoint p = getPaths().get(Field.POA_REVOCATION_DATE);
        PDPage page = document.getDocumentCatalog().getPages().get(p.page);
        addTextToPage(document, page, p.x, p.y, expirationDate);
    }

    public void setSigningDate(PDDocument document) throws Exception {
        PdfboxPagePoint p = getPaths().get(Field.POA_SIGNING_DATE);
        PDPage page = document.getDocumentCatalog().getPages().get(p.page);
        addTextToPage(document, page, p.x, p.y, signingDate);
    }

    public void setSignature(PDDocument document) throws Exception {
        PdfboxPagePoint p = getPaths().get(Field.POA_SIGNATURE);
        PDPage page = document.getDocumentCatalog().getPages().get(p.page);
        addPngToPage(document, page, p.x, p.y, signature, 30);
    }

    public void setPrintedName(PDDocument document) throws Exception {
        PdfboxPagePoint p = getPaths().get(Field.POA_PRINTED_NAME);
        PDPage page = document.getDocumentCatalog().getPages().get(p.page);
        addTextToPage(document, page, p.x, p.y, printedName);
    }

    public String getPdfPath() {
        return pdfPath;
    }

}
