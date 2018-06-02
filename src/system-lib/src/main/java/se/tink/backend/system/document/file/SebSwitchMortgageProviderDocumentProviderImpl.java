package se.tink.backend.system.document.file;

import se.tink.backend.common.repository.cassandra.DocumentRepository;
import se.tink.backend.system.document.core.SwitchMortgageProvider;
import se.tink.backend.system.document.file.pdf.SebOtherInformationDocument;
import se.tink.backend.system.document.file.template.PdfDocumentTemplate;

public class SebSwitchMortgageProviderDocumentProviderImpl extends SwitchMortgageProviderDocumentProviderImpl {

    public SebSwitchMortgageProviderDocumentProviderImpl(DocumentRepository documentRepository) {
        super(documentRepository);
    }

    @Override
    public void generateInMemoryPdfDocumentsMap(SwitchMortgageProvider switchMortgageProvider) {
        super.generateInMemoryPdfDocumentsMap(switchMortgageProvider);

        Document otherInformationDocument = getOtherInformationPdfDocument(switchMortgageProvider);
        addToMapIfNotNull(getOtherPdfDocumentsMap(), otherInformationDocument);

    }

    public Document getOtherInformationPdfDocument(SwitchMortgageProvider switchMortgageProvider) {
        Document doc = new SebOtherInformationDocument(
                switchMortgageProvider.getExternalApplicationId(),
                switchMortgageProvider.getApplicant().getFullName(),
                switchMortgageProvider.getApplicant().getNationalId(),
                PdfDocumentTemplate.OTHER_INFORMATION_TEMPLATE.getFilePath(),
                switchMortgageProvider.getSebAdditionalInformation()
        );
        return doc;
    }
}
