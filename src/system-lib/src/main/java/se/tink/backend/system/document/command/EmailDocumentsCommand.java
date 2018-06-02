package se.tink.backend.system.document.command;

import se.tink.backend.system.document.core.DocumentModeratorDispatchDetails;
import se.tink.backend.system.document.core.SwitchMortgageProvider;

public class EmailDocumentsCommand {

    private final SwitchMortgageProvider switchMortgageProvider;
    private final DocumentModeratorDispatchDetails documentModeratorDispatchDetails;
    private final String userId;

    public EmailDocumentsCommand(SwitchMortgageProvider switchMortgageProvider,
            DocumentModeratorDispatchDetails documentModeratorDispatchDetails, String userId) {
        this.switchMortgageProvider = switchMortgageProvider;
        this.documentModeratorDispatchDetails = documentModeratorDispatchDetails;
        this.userId = userId;
    }

    public SwitchMortgageProvider getSwitchMortgageProvider() {
        return switchMortgageProvider;
    }

    public DocumentModeratorDispatchDetails getDocumentModeratorDispatchDetails() {
        return documentModeratorDispatchDetails;
    }

    public String getUserId() {
        return userId;
    }
}
