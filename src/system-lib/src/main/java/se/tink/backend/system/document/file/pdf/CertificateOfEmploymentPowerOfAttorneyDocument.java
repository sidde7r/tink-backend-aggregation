package se.tink.backend.system.document.file.pdf;

import java.io.ByteArrayOutputStream;
import java.io.File;
import org.apache.pdfbox.pdmodel.PDDocument;
import se.tink.libraries.i18n.Catalog;
import se.tink.backend.system.document.file.Document;
import se.tink.backend.utils.LogUtils;

public class CertificateOfEmploymentPowerOfAttorneyDocument extends PowerOfAttorneyDocument
        implements Document {

    private static final LogUtils log = new LogUtils(CertificateOfEmploymentPowerOfAttorneyDocument.class);
    private final static String documentName = "POA_CERTIFICATE_OF_EMPLOYMENT";
    private final static String clauseTemplate =
            "- Ifrån min arbetsgivare {0}, begära inlämning av Arbetsgivarintyg via en av Tink tillhandahållen säker hemsidelänk. "
                    + "- Ifall ovan ej är möjligt, Ifrån min arbetsgivare {0}, begära sänding av Arbetsgivarintyg till backoffice@tink.se. "
                    + "- Ifall ovan ej är möjligt, ifrån min arbetsgivare {0}, begära sänding av Arbetsgivarintyg till fullmaktshavarens adress. ";

    public CertificateOfEmploymentPowerOfAttorneyDocument(
            String prefix,
            String fullName,
            String personalNumber,
            String address,
            String postalCode,
            String city,
            String employer,
            String expirationDate,
            String signingLocation,
            String signingDate,
            byte[] signature,
            String printedName,
            String pdfPath) {
        super(
                prefix,
                documentName,
                fullName,
                personalNumber,
                address,
                postalCode,
                city,
                Catalog.format(clauseTemplate, employer),
                expirationDate,
                signingLocation,
                signingDate,
                signature,
                printedName,
                pdfPath);
    }

    public byte[] generateInMemoryDocument() {
        try {
            ByteArrayOutputStream output = new ByteArrayOutputStream();
            String pdfFilePath = getPdfPath();
            PDDocument document = PDDocument.load(new File(pdfFilePath));
            setFullName(document);
            setPersonalNumber(document);
            setAddress(document);
            setPostalCode(document);
            setCity(document);
            setClause(document);
            setSigningLocation(document);
            setExpirationDate(document);
            setSigningDate(document);
            setPrintedName(document);

            if (getSignature() != null) {
                setSignature(document);
            }

            document.save(output);
            document.close();

            log.debug(String.format("Generated PDF from template %s.", pdfFilePath));
            return output.toByteArray();
        } catch (Exception e) {
            log.error("Could not create Certificate of Employment POA pdf document.", e);
        }
        return null;
    }

}
