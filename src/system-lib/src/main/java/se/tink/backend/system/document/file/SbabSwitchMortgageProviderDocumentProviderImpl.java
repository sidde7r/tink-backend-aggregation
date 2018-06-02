package se.tink.backend.system.document.file;

import se.tink.backend.common.repository.cassandra.DocumentRepository;
import se.tink.backend.system.document.core.SwitchMortgageProvider;

public class SbabSwitchMortgageProviderDocumentProviderImpl extends SwitchMortgageProviderDocumentProviderImpl {

    public SbabSwitchMortgageProviderDocumentProviderImpl(DocumentRepository documentRepository) {
        super(documentRepository);
    }

    @Override
    public void generateInMemoryPdfDocumentsMap(SwitchMortgageProvider switchMortgageProvider) {
        super.generateInMemoryPdfDocumentsMap(switchMortgageProvider);
    }
}
