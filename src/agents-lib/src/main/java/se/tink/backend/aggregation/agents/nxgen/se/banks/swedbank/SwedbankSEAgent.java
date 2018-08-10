package se.tink.backend.aggregation.agents.nxgen.se.banks.swedbank;

import java.util.Optional;
import java.util.concurrent.TimeUnit;
import se.tink.backend.aggregation.agents.AgentContext;
import se.tink.backend.aggregation.agents.nxgen.se.banks.swedbank.loan.SwedbankSELoanFetcher;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.banks.swedbank.SwedbankAbstractAgent;
import se.tink.backend.aggregation.nxgen.controllers.refresh.loan.LoanRefreshController;
import se.tink.backend.aggregation.nxgen.http.TinkHttpClient;
import se.tink.backend.aggregation.rpc.CredentialsRequest;
import se.tink.backend.aggregation.rpc.Field;
import se.tink.backend.common.config.SignatureKeyPair;

public class SwedbankSEAgent extends SwedbankAbstractAgent {

    private final SwedbankSEApiClient apiClient;

    public SwedbankSEAgent(CredentialsRequest request, AgentContext context,
            SignatureKeyPair signatureKeyPair) {
        super(request, context, signatureKeyPair, new SwedbankSEConfiguration(request.getProvider().getPayload()));
        this.apiClient = new SwedbankSEApiClient(client, configuration,
                credentials.getField(Field.Key.USERNAME),
                sessionStorage);
    }

    @Override
    protected void configureHttpClient(TinkHttpClient client) {
        super.configureHttpClient(client);
    }

    @Override
    protected Optional<LoanRefreshController> constructLoanRefreshController() {
        return Optional.of(new LoanRefreshController(metricRefreshController, updateController,
                new SwedbankSELoanFetcher(apiClient)));
    }
}
