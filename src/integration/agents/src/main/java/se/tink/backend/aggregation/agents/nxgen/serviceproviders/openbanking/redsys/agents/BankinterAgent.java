package se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.redsys.agents;

import java.time.LocalDate;
import se.tink.backend.aggregation.agents.AgentContext;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.redsys.RedsysAgent;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.redsys.RedsysConstants.StorageKeys;
import se.tink.backend.aggregation.configuration.SignatureKeyPair;
import se.tink.libraries.credentials.service.CredentialsRequest;

public class BankinterAgent extends RedsysAgent {

    public BankinterAgent(
            CredentialsRequest request, AgentContext context, SignatureKeyPair signatureKeyPair) {
        super(request, context, signatureKeyPair);
    }

    @Override
    public String getAspspCode() {
        return "bankinter";
    }

    @Override
    public boolean shouldRequestAccountsWithBalance() {
        return true;
    }

    @Override
    public boolean supportsPendingTransactions() {
        return false;
    }

    private boolean hasUsedConsentForAccount(String accountId, String consentId) {
        final String fetchedConsentId =
                persistentStorage.get(StorageKeys.FETCHED_INITIAL_TRANSACTIONS + accountId);
        return consentId.equals(fetchedConsentId);
    }

    @Override
    public LocalDate transactionsFromDate(String accountId, String consentId) {
        // 18 months are allowed on the first use of the consent, regardless of date
        if (hasUsedConsentForAccount(accountId, consentId)) {
            return LocalDate.now().minusDays(90);
        } else {
            return LocalDate.now().minusMonths(18);
        }
    }
}
