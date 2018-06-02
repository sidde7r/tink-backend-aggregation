package se.tink.backend.system.document.file.pdf;

import java.io.ByteArrayOutputStream;
import java.io.File;
import org.apache.pdfbox.pdmodel.PDDocument;
import se.tink.libraries.i18n.Catalog;
import se.tink.backend.system.document.file.Document;
import se.tink.backend.utils.LogUtils;

public class AmortizationPowerOfAttorneyDocument extends PowerOfAttorneyDocument implements Document {

    private static final LogUtils log = new LogUtils(AmortizationPowerOfAttorneyDocument.class);

    private final static String documentName = "POA_AMORTIZATION";
    private final static String clauseTemplate =
            "- Ifrån min bank {0}, begära sändning av amorteringsunderlag till amorteringsunderlag@tink.se. "
                    + "- Om ovan ej är möjligt, ifrån min bank {0}, begära sändning av amorteringsunderlag till Fullmakthavarens adress. "
                    + "- Om ovan ej är möjligt, ifrån min bank {0}, begära utlämning av fysisk kopia av Amorteringsunderlag på kontor. ";

    public AmortizationPowerOfAttorneyDocument(
            String prefix,
            String fullName,
            String personalNumber,
            String address,
            String postalCode,
            String city,
            String currentMortgageProvider,
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
                Catalog.format(clauseTemplate, currentMortgageProvider),
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
            setExpirationDate(document);
            setSigningLocation(document);
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
            log.error("Could not create Amortization pdf document.", e);
        }
        return null;
    }

}
