package se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.redsys.agents;

import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
import se.tink.backend.aggregation.agents.AgentContext;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.redsys.RedsysAgent;
import se.tink.backend.aggregation.configuration.SignatureKeyPair;
import se.tink.libraries.credentials.service.CredentialsRequest;

public class EvobancoAgent extends RedsysAgent {

    public EvobancoAgent(
            CredentialsRequest request, AgentContext context, SignatureKeyPair signatureKeyPair) {
        super(request, context, signatureKeyPair);
    }

    @Override
    public String getAspspCode() {
        return "EVOBANCO";
    }

    @Override
    public boolean shouldRequestAccountsWithBalance() {
        return true;
    }

    @Override
    public boolean supportsPendingTransactions() {
        return false;
    }

    @Override
    public LocalDate transactionsFromDate(String accountId, String consentId) {
        if (consentStorage.consentIsNewerThan(1, ChronoUnit.DAYS)) {
            // FIXME: not sure of this logic
            return LocalDate.now().minusYears(5);
        } else {
            return LocalDate.now().minusDays(90);
        }
    }
}
