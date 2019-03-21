package se.tink.backend.aggregation.agents.contexts;

import java.util.List;
import java.util.Map;
import se.tink.backend.agents.rpc.Account;
import se.tink.backend.agents.rpc.Credentials;
import se.tink.libraries.documentcontainer.DocumentContainer;
import se.tink.backend.aggregation.agents.models.fraud.FraudDetailsContent;
import se.tink.backend.aggregation.agents.models.TransferDestinationPattern;
import se.tink.libraries.transfer.rpc.Transfer;

public interface SystemUpdater {

    void processTransactions();

    Account sendAccountToUpdateService(String uniqueId);

    Account updateAccount(String uniqueId);

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
