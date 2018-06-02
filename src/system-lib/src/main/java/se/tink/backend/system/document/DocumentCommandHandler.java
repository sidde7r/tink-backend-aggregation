package se.tink.backend.system.document;

import java.util.Map;
import se.tink.backend.common.mail.MailSender;
import se.tink.backend.common.repository.cassandra.DocumentRepository;
import se.tink.backend.system.document.command.EmailDocumentsCommand;
import se.tink.backend.system.document.core.SwitchMortgageProvider;
import se.tink.backend.system.document.file.DocumentProvider;
import se.tink.backend.system.document.file.SbabSwitchMortgageProviderDocumentProviderImpl;
import se.tink.backend.system.document.file.SebSwitchMortgageProviderDocumentProviderImpl;
import se.tink.backend.utils.LogUtils;

public class DocumentCommandHandler {

    private static final LogUtils log = new LogUtils(DocumentCommandHandler.class);
    private MailSender mailSender;
    private DocumentRepository documentRepository;

    public DocumentCommandHandler(MailSender mailSender, DocumentRepository documentRepository) {
        this.mailSender = mailSender;
        this.documentRepository = documentRepository;
    }

    public void on(EmailDocumentsCommand command) {

        DocumentProvider documentProvider = getDocumentProvider(command.getSwitchMortgageProvider());
        if (documentProvider == null) {
            log.error("No available pdf document provider.");
            return;
        }

        documentProvider
                .generateInMemoryPdfDocumentsMap(command.getSwitchMortgageProvider());
        Map<String, byte[]> mapOfFiles = documentProvider
                .getFiles(command.getUserId(), command.getSwitchMortgageProvider().getExternalApplicationId(), command.getSwitchMortgageProvider().getMortgage().getLoanNumbers());
        if (mapOfFiles == null || mapOfFiles.isEmpty()) {
            log.error("No available pdf documents.");
            return;
        }

        log.info("Documents successfully generated.");

        final String subject = String.format("%s %s", command.getSwitchMortgageProvider().getName(),
                command.getSwitchMortgageProvider().getExternalApplicationId());
        final String body = command.getSwitchMortgageProvider().getMessageBody(12, 0.3);

        boolean emailWasSent = mailSender.sendMessage(
                command.getDocumentModeratorDispatchDetails().getToAddress(),
                subject,
                command.getDocumentModeratorDispatchDetails().getFromAddress(),
                command.getDocumentModeratorDispatchDetails().getFromName(),
                body,
                true,
                mapOfFiles);

        if (emailWasSent) {
            log.info(String.format("Email was sent to %s.",
                    command.getDocumentModeratorDispatchDetails().getToAddress()));
        } else {
            log.error("Email could not be sent.");
        }
    }

    private DocumentProvider getDocumentProvider(SwitchMortgageProvider switchMortgageProvider) {
        switch (switchMortgageProvider.getName()) {
        case ("seb-bankid"):
            return new SebSwitchMortgageProviderDocumentProviderImpl(documentRepository);
        case ("sbab-bankid"):
            return new SbabSwitchMortgageProviderDocumentProviderImpl(documentRepository);
        default:
            return null;
        }
    }

}
