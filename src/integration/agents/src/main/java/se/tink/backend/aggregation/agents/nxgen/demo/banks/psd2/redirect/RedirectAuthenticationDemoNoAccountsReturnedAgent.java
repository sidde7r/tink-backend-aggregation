package se.tink.backend.aggregation.agents.nxgen.demo.banks.psd2.redirect;

import java.util.Collections;
import se.tink.backend.aggregation.agents.FetchAccountsResponse;
import se.tink.backend.aggregation.agents.FetchInvestmentAccountsResponse;
import se.tink.backend.aggregation.agents.FetchLoanAccountsResponse;
import se.tink.backend.aggregation.agents.FetchTransactionsResponse;
import se.tink.backend.aggregation.agents.contexts.agent.AgentContext;
import se.tink.backend.aggregation.configuration.signaturekeypair.SignatureKeyPair;
import se.tink.libraries.credentials.service.CredentialsRequest;

public class RedirectAuthenticationDemoNoAccountsReturnedAgent
        extends RedirectAuthenticationDemoAgent {
    public RedirectAuthenticationDemoNoAccountsReturnedAgent(
            CredentialsRequest request, AgentContext context, SignatureKeyPair signatureKeyPair) {
        super(request, context, signatureKeyPair);
    }

    @Override
    public FetchAccountsResponse fetchCheckingAccounts() {
        return emptyAccountsResponse();
    }

    @Override
    public FetchTransactionsResponse fetchCheckingTransactions() {
        return emptyTransactionsResponse();
    }

    @Override
    public FetchAccountsResponse fetchSavingsAccounts() {
        return emptyAccountsResponse();
    }

    @Override
    public FetchTransactionsResponse fetchSavingsTransactions() {
        return emptyTransactionsResponse();
    }

    @Override
    public FetchAccountsResponse fetchCreditCardAccounts() {
        return emptyAccountsResponse();
    }

    @Override
    public FetchTransactionsResponse fetchCreditCardTransactions() {
        return emptyTransactionsResponse();
    }

    @Override
    public FetchInvestmentAccountsResponse fetchInvestmentAccounts() {
        return new FetchInvestmentAccountsResponse(Collections.emptyMap());
    }

    @Override
    public FetchTransactionsResponse fetchInvestmentTransactions() {
        return emptyTransactionsResponse();
    }

    @Override
    public FetchLoanAccountsResponse fetchLoanAccounts() {
        return new FetchLoanAccountsResponse(Collections.emptyMap());
    }

    @Override
    public FetchTransactionsResponse fetchLoanTransactions() {
        return emptyTransactionsResponse();
    }

    private FetchTransactionsResponse emptyTransactionsResponse() {
        return new FetchTransactionsResponse(Collections.emptyMap());
    }

    private FetchAccountsResponse emptyAccountsResponse() {
        return new FetchAccountsResponse(Collections.emptyList());
    }
}
