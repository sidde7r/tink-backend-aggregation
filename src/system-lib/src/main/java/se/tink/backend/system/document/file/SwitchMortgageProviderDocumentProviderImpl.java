package se.tink.backend.system.document.file;

import com.google.common.collect.Maps;
import java.awt.Point;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;
import se.tink.backend.common.repository.cassandra.DocumentRepository;
import se.tink.backend.core.CompressedDocument;
import se.tink.backend.core.DocumentIdentifier;
import se.tink.backend.system.document.core.ResidenceType;
import se.tink.backend.system.document.core.SwitchMortgageProvider;
import se.tink.backend.system.document.file.pdf.AmortizationPowerOfAttorneyDocument;
import se.tink.backend.system.document.file.pdf.CertificateOfEmploymentPowerOfAttorneyDocument;
import se.tink.backend.system.document.file.pdf.HousingCooperativePowerOfAttorneyDocument;
import se.tink.backend.system.document.file.pdf.SalaryDocument;
import se.tink.backend.system.document.file.svg.SignatureSVG;
import se.tink.backend.system.document.file.template.PdfDocumentTemplate;
import se.tink.backend.utils.LogUtils;
import se.tink.libraries.uuid.UUIDUtils;

public class SwitchMortgageProviderDocumentProviderImpl implements DocumentProvider {

    private static final LogUtils log = new LogUtils(SwitchMortgageProviderDocumentProviderImpl.class);
    private Map<String, byte[]> signedPoaPdfDocumentsMap;
    private Map<String, byte[]> unsignedPoaPdfDocumentsMap;
    private Map<String, byte[]> otherPdfDocumentsMap;

    private DocumentRepository documentRepository;

    public SwitchMortgageProviderDocumentProviderImpl(DocumentRepository documentRepository) {
        this.documentRepository = documentRepository;
        signedPoaPdfDocumentsMap = Maps.newHashMap();
        unsignedPoaPdfDocumentsMap = Maps.newHashMap();
        otherPdfDocumentsMap = Maps.newHashMap();
    }

    @Override
    public void generateInMemoryPdfDocumentsMap(SwitchMortgageProvider switchMortgageProvider) {

        Document amortizationPowerOfAttorneyDocument = getAmortizationPowerOfAttorneyPdfDocument(
                switchMortgageProvider);
        addToMapIfNotNull(getSignedPoaPdfDocumentsMap(), amortizationPowerOfAttorneyDocument);

        Document certificateOfEmploymentPowerOfAttorneyDocument = getCertificateOfEmploymentPowerOfAttorneyPdfDocument(
                switchMortgageProvider);
        addToMapIfNotNull(getSignedPoaPdfDocumentsMap(), certificateOfEmploymentPowerOfAttorneyDocument);

        Document housingCooperativePowerOfAttorneyDocument = getHousingCooperativePowerOfAttorneyPdfDocumentIfEligible(
                switchMortgageProvider);
        addToMapIfNotNull(getSignedPoaPdfDocumentsMap(), housingCooperativePowerOfAttorneyDocument);

        Document unsignedAmortizationPowerOfAttorneyDocument = getUnsignedAmortizationPowerOfAttorneyPdfDocument(
                switchMortgageProvider);
        addToMapIfNotNull(getUnsignedPoaPdfDocumentsMap(), unsignedAmortizationPowerOfAttorneyDocument);

        Document unsignedCertificateOfEmploymentPowerOfAttorneyDocument = getUnsignedCertificateOfEmploymentPowerOfAttorneyPdfDocument(
                switchMortgageProvider);
        addToMapIfNotNull(getUnsignedPoaPdfDocumentsMap(), unsignedCertificateOfEmploymentPowerOfAttorneyDocument);

        Document unsignedHousingCooperativePowerOfAttorneyDocument = getUnsignedHousingCooperativePowerOfAttorneyPdfDocumentIfEligible(
                switchMortgageProvider);
        addToMapIfNotNull(getUnsignedPoaPdfDocumentsMap(), unsignedHousingCooperativePowerOfAttorneyDocument);

        Document salaryDocument = getSalaryPdf(switchMortgageProvider);
        addToMapIfNotNull(getOtherPdfDocumentsMap(), salaryDocument);

        Document signatureSvg = getSignatureSVG(switchMortgageProvider);
        addToMapIfNotNull(getUnsignedPoaPdfDocumentsMap(), signatureSvg);
    }

    @Override
    public Map<String, byte[]> getFiles(String userId, String namePrefix, List<String> loanNumbers) {
        Map<String, byte[]> mapOfFiles = Maps.newHashMap();

        mapOfFiles.put(namePrefix + "_SIGNED_POA.zip", zipMapOfFiles(getSignedPoaPdfDocumentsMap()));
        mapOfFiles.put(namePrefix + "_UNSIGNED_POA.zip", zipMapOfFiles(getUnsignedPoaPdfDocumentsMap()));
        mapOfFiles.put(namePrefix + "_SUPPLEMENTALS.zip", zipMapOfFiles(getOtherPdfDocumentsMap()));

        if (loanNumbers.size() > 0) {
            for (String loanNumber : loanNumbers) {
                String identifier = DocumentIdentifier.AMORTIZATION_DOCUMENTATION + "-" + loanNumber;
                Optional<CompressedDocument> doc = documentRepository.findOneByUserIdAndIdentifier(
                        UUIDUtils.fromTinkUUID(userId),
                        identifier);
                if (doc.isPresent()) {
                    try {
                        mapOfFiles.put(namePrefix + "_AMORTIZATION_DOCUMENTATION_" + loanNumber + ".pdf",
                                doc.get().getUncompressed());
                    } catch (IOException e) {
                        log.error(userId, "Retrieval of amortization documents failed", e);
                    }
                }
            }
        }

        return mapOfFiles;
    }

    public byte[] zipMapOfFiles(Map<String, byte[]> mapOfFiles) {
        if (mapOfFiles == null) {
            log.error("Map of files to be zipped is null.");
            return null;
        }
        ByteArrayOutputStream bos = new ByteArrayOutputStream();
        try (ZipOutputStream zos = new ZipOutputStream(bos)) {
            for (Map.Entry<String, byte[]> entry : mapOfFiles.entrySet()) {
                String name = entry.getKey();
                byte[] pdf = entry.getValue();
                ZipEntry zipEntry = new ZipEntry(name);
                zos.putNextEntry(zipEntry);
                zos.write(pdf);
                zos.closeEntry();
            }
            zos.close();
            return bos.toByteArray();
        } catch (Exception e) {
            log.error("Could not zip pdf document.", e);
        }
        return null;
    }

    public Map<String, byte[]> getSignedPoaPdfDocumentsMap() {
        return signedPoaPdfDocumentsMap;
    }

    public Map<String, byte[]> getUnsignedPoaPdfDocumentsMap() {
        return unsignedPoaPdfDocumentsMap;
    }

    public Map<String, byte[]> getOtherPdfDocumentsMap() {
        return otherPdfDocumentsMap;
    }

    public void addToMapIfNotNull(Map<String, byte[]> map, Document doc) {
        if (doc == null) {
            return;
        }

        byte[] document = doc.generateInMemoryDocument();
        if (document == null) {
            return;
        }

        map.put(doc.getDocumentNameWithExtension(), document);
    }

    public Document getSignatureSVG(SwitchMortgageProvider switchMortgageProvider) {
        List<List<Point>> signature = switchMortgageProvider.getApplicant().getSignature();

        if (signature == null || signature.isEmpty()) {
            log.error("Signature should not be empty");
            return null;
        }

        return new SignatureSVG(signature);
    }

    public Document getAmortizationPowerOfAttorneyPdfDocument(SwitchMortgageProvider switchMortgageProvider) {
        AmortizationPowerOfAttorneyDocument doc = new AmortizationPowerOfAttorneyDocument(
                switchMortgageProvider.getExternalApplicationId(),
                switchMortgageProvider.getApplicant().getFullName(),
                switchMortgageProvider.getApplicant().getNationalId(),
                switchMortgageProvider.getApplicant().getAddress(),
                switchMortgageProvider.getApplicant().getPostalCode(),
                switchMortgageProvider.getApplicant().getTown(),
                switchMortgageProvider.getResidence().getMortgageProvider(),
                switchMortgageProvider.getApplicant().getPoaDetails().getExpirationDate(),
                switchMortgageProvider.getApplicant().getPoaDetails().getSigningLocation(),
                switchMortgageProvider.getApplicant().getPoaDetails().getSigningDate(),
                switchMortgageProvider.getApplicant().getPoaDetails().getSignature(),
                switchMortgageProvider.getApplicant().getFullName(),
                PdfDocumentTemplate.POA_AMORTIZATION_TEMPLATE.getFilePath()
        );
        return doc;
    }

    public Document getCertificateOfEmploymentPowerOfAttorneyPdfDocument(
            SwitchMortgageProvider switchMortgageProvider) {
        if (!switchMortgageProvider.getApplicant().getEmployment().isPresent()) {
            return null;
        }

        CertificateOfEmploymentPowerOfAttorneyDocument doc = new CertificateOfEmploymentPowerOfAttorneyDocument(
                switchMortgageProvider.getExternalApplicationId(),
                switchMortgageProvider.getApplicant().getFullName(),
                switchMortgageProvider.getApplicant().getNationalId(),
                switchMortgageProvider.getApplicant().getAddress(),
                switchMortgageProvider.getApplicant().getPostalCode(),
                switchMortgageProvider.getApplicant().getTown(),
                switchMortgageProvider.getApplicant().getEmployment().get().getEmployer(),
                switchMortgageProvider.getApplicant().getPoaDetails().getExpirationDate(),
                switchMortgageProvider.getApplicant().getPoaDetails().getSigningLocation(),
                switchMortgageProvider.getApplicant().getPoaDetails().getSigningDate(),
                switchMortgageProvider.getApplicant().getPoaDetails().getSignature(),
                switchMortgageProvider.getApplicant().getFullName(),
                PdfDocumentTemplate.POA_CERTIFICATE_OF_EMPLOYMENT_TEMPLATE.getFilePath()
        );
        return doc;
    }

    public Document getHousingCooperativePowerOfAttorneyPdfDocumentIfEligible(
            SwitchMortgageProvider switchMortgageProvider) {
        if (!Objects.equals(switchMortgageProvider.getResidence().getType(), ResidenceType.APARTMENT)) {
            log.info("Not an apartment. Housing cooperative not applicable.");
            return null;
        }

        if (!switchMortgageProvider.getResidence().getHousingCooperative().isPresent()) {
            log.error("Missing Housing Cooperative for ResidenceType.APARTMENT");
            return null;
        }

        HousingCooperativePowerOfAttorneyDocument doc = new HousingCooperativePowerOfAttorneyDocument(
                switchMortgageProvider.getExternalApplicationId(),
                switchMortgageProvider.getApplicant().getFullName(),
                switchMortgageProvider.getApplicant().getNationalId(),
                switchMortgageProvider.getApplicant().getAddress(),
                switchMortgageProvider.getApplicant().getPostalCode(),
                switchMortgageProvider.getApplicant().getTown(),
                switchMortgageProvider.getResidence().getHousingCooperative().get(),
                switchMortgageProvider.getApplicant().getPoaDetails().getExpirationDate(),
                switchMortgageProvider.getApplicant().getPoaDetails().getSigningLocation(),
                switchMortgageProvider.getApplicant().getPoaDetails().getSigningDate(),
                switchMortgageProvider.getApplicant().getPoaDetails().getSignature(),
                switchMortgageProvider.getApplicant().getFullName(),
                PdfDocumentTemplate.POA_HOUSING_COOPERATIVE_TEMPLATE.getFilePath()
        );
        return doc;
    }

    public Document getSalaryPdf(SwitchMortgageProvider switchMortgageProvider) {
        SalaryDocument doc = new SalaryDocument(
                switchMortgageProvider.getExternalApplicationId(),
                switchMortgageProvider.getApplicant().getFullName(),
                switchMortgageProvider.getApplicant().getNationalId(),
                switchMortgageProvider.getApplicant().getSalaries(),
                PdfDocumentTemplate.SALARY_TEMPLATE.getFilePath()
        );
        return doc;
    }

    public Document getUnsignedAmortizationPowerOfAttorneyPdfDocument(SwitchMortgageProvider switchMortgageProvider) {
        AmortizationPowerOfAttorneyDocument doc = new AmortizationPowerOfAttorneyDocument(
                switchMortgageProvider.getExternalApplicationId(),
                switchMortgageProvider.getApplicant().getFullName(),
                switchMortgageProvider.getApplicant().getNationalId(),
                switchMortgageProvider.getApplicant().getAddress(),
                switchMortgageProvider.getApplicant().getPostalCode(),
                switchMortgageProvider.getApplicant().getTown(),
                switchMortgageProvider.getResidence().getMortgageProvider(),
                switchMortgageProvider.getApplicant().getPoaDetails().getExpirationDate(),
                switchMortgageProvider.getApplicant().getPoaDetails().getSigningLocation(),
                switchMortgageProvider.getApplicant().getPoaDetails().getSigningDate(),
                null,
                switchMortgageProvider.getApplicant().getFullName(),
                PdfDocumentTemplate.POA_AMORTIZATION_TEMPLATE.getFilePath()
        );
        return doc;
    }

    public Document getUnsignedCertificateOfEmploymentPowerOfAttorneyPdfDocument(
            SwitchMortgageProvider switchMortgageProvider) {
        if (!switchMortgageProvider.getApplicant().getEmployment().isPresent()) {
            return null;
        }

        CertificateOfEmploymentPowerOfAttorneyDocument doc = new CertificateOfEmploymentPowerOfAttorneyDocument(
                switchMortgageProvider.getExternalApplicationId(),
                switchMortgageProvider.getApplicant().getFullName(),
                switchMortgageProvider.getApplicant().getNationalId(),
                switchMortgageProvider.getApplicant().getAddress(),
                switchMortgageProvider.getApplicant().getPostalCode(),
                switchMortgageProvider.getApplicant().getTown(),
                switchMortgageProvider.getApplicant().getEmployment().get().getEmployer(),
                switchMortgageProvider.getApplicant().getPoaDetails().getExpirationDate(),
                switchMortgageProvider.getApplicant().getPoaDetails().getSigningLocation(),
                switchMortgageProvider.getApplicant().getPoaDetails().getSigningDate(),
                null,
                switchMortgageProvider.getApplicant().getFullName(),
                PdfDocumentTemplate.POA_CERTIFICATE_OF_EMPLOYMENT_TEMPLATE.getFilePath()
        );
        return doc;
    }

    public Document getUnsignedHousingCooperativePowerOfAttorneyPdfDocumentIfEligible(
            SwitchMortgageProvider switchMortgageProvider) {
        if (!Objects.equals(switchMortgageProvider.getResidence().getType(), ResidenceType.APARTMENT)) {
            return null;
        }

        HousingCooperativePowerOfAttorneyDocument doc = new HousingCooperativePowerOfAttorneyDocument(
                switchMortgageProvider.getExternalApplicationId(),
                switchMortgageProvider.getApplicant().getFullName(),
                switchMortgageProvider.getApplicant().getNationalId(),
                switchMortgageProvider.getApplicant().getAddress(),
                switchMortgageProvider.getApplicant().getPostalCode(),
                switchMortgageProvider.getApplicant().getTown(),
                switchMortgageProvider.getResidence().getHousingCooperative().get(),
                switchMortgageProvider.getApplicant().getPoaDetails().getExpirationDate(),
                switchMortgageProvider.getApplicant().getPoaDetails().getSigningLocation(),
                switchMortgageProvider.getApplicant().getPoaDetails().getSigningDate(),
                null,
                switchMortgageProvider.getApplicant().getFullName(),
                PdfDocumentTemplate.POA_HOUSING_COOPERATIVE_TEMPLATE.getFilePath()
        );
        return doc;
    }
}
