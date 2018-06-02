package se.tink.backend.system.document.file.pdf;

import java.io.ByteArrayOutputStream;
import java.io.File;
import org.apache.pdfbox.pdmodel.PDDocument;
import se.tink.libraries.i18n.Catalog;
import se.tink.backend.system.document.file.Document;
import se.tink.backend.utils.LogUtils;

public class HousingCooperativePowerOfAttorneyDocument extends PowerOfAttorneyDocument implements Document {

    private static final LogUtils log = new LogUtils(HousingCooperativePowerOfAttorneyDocument.class);
    private final static String documentName = "POA_HOUSING_COOPERATIVE";
    private final static String clauseTemplate =
            "- Ifrån min bostadsrättsförening {0} begära sändning av Utdrag ur Lägenhetsföreckning gällande min bostad till backoffice@tink.se. "
                    + "- Om ovan ej är möjligt, ifrån min bostadsrättsförening {0} begära sändning av Utdrag ur Lägenhetsföreckning gällande min bostad till Fullmakthavarens adress. ";

    public HousingCooperativePowerOfAttorneyDocument(
            String prefix,
            String fullName,
            String personalNumber,
            String address,
            String postalCode,
            String city,
            String housingCommunityName,
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
                Catalog.format(clauseTemplate, housingCommunityName),
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
            setSigningDate(document);
            setSigningLocation(document);
            setPrintedName(document);
            if (getSignature() != null) {
                setSignature(document);
            }
            document.save(output);
            document.close();

            log.debug(String.format("Generated PDF from template %s.", pdfFilePath));
            return output.toByteArray();
        } catch (Exception e) {
            log.error("Could not create Housing Cooperative POA pdf document.", e);
        }
        return null;
    }

}

