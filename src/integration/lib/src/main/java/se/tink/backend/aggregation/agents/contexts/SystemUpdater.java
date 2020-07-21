package se.tink.backend.aggregation.agents.contexts;

import java.util.List;
import java.util.Map;
import se.tink.backend.agents.rpc.Account;
import se.tink.backend.agents.rpc.AccountHolder;
import se.tink.backend.agents.rpc.Credentials;
import se.tink.backend.aggregation.agents.models.TransferDestinationPattern;
import se.tink.backend.aggregation.agents.models.fraud.FraudDetailsContent;
import se.tink.libraries.transfer.rpc.Transfer;

public interface SystemUpdater {

    void processTransactions();

    Account sendAccountToUpdateService(String bankAccountId);

    AccountHolder sendAccountHolderToUpdateService(Account processedAccount);

    Account updateAccount(String uniqueId);

    void updateTransferDestinationPatterns(Map<Account, List<TransferDestinationPattern>> map);

    void updateCredentialsExcludingSensitiveInformation(
            Credentials credentials, boolean doStatusUpdate);

    void updateCredentialsExcludingSensitiveInformation(
            Credentials credentials, boolean doStatusUpdate, boolean isMigrationUpdate);

    void updateFraudDetailsContent(List<FraudDetailsContent> detailsContent);

    List<Account> getUpdatedAccounts();

    void updateEinvoices(List<Transfer> transfers);

    boolean isWaitingOnConnectorTransactions();

    void setWaitingOnConnectorTransactions(boolean waitingOnConnectorTransactions);

    void sendIdentityToIdentityAggregatorService();
}
