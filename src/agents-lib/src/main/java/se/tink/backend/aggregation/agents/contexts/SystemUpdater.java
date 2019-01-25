package se.tink.backend.aggregation.agents.contexts;

import java.util.List;
import java.util.Map;
import se.tink.backend.aggregation.rpc.Account;
import se.tink.backend.aggregation.rpc.Credentials;
import se.tink.backend.core.DocumentContainer;
import se.tink.backend.core.FraudDetailsContent;
import se.tink.backend.aggregation.agents.models.TransferDestinationPattern;
import se.tink.backend.core.transfer.Transfer;

public interface SystemUpdater {

    void processTransactions();

    Account sendAccountToUpdateService(String uniqueId);

    void updateTransferDestinationPatterns(Map<Account, List<TransferDestinationPattern>> map);

    void updateCredentialsExcludingSensitiveInformation(Credentials credentials,
            boolean doStatusUpdate);

    void updateFraudDetailsContent(List<FraudDetailsContent> detailsContent);

    void updateDocument(DocumentContainer container);

    List<Account> getUpdatedAccounts();

    void updateEinvoices(List<Transfer> transfers);

    boolean isWaitingOnConnectorTransactions();

    void setWaitingOnConnectorTransactions(boolean waitingOnConnectorTransactions);
}
