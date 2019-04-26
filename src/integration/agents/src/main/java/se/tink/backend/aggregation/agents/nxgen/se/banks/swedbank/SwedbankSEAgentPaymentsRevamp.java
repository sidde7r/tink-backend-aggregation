package se.tink.backend.aggregation.agents.nxgen.se.banks.swedbank;

import java.util.Optional;
import se.tink.backend.aggregation.agents.AgentContext;
import se.tink.backend.aggregation.agents.nxgen.se.banks.swedbank.loan.SwedbankSELoanFetcher;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.banks.swedbank.SwedbankAbstractAgentPaymentsRevamp;
import se.tink.backend.aggregation.configuration.SignatureKeyPair;
import se.tink.backend.aggregation.nxgen.controllers.refresh.loan.LoanRefreshController;
import se.tink.libraries.credentials.service.CredentialsRequest;

public class SwedbankSEAgentPaymentsRevamp extends SwedbankAbstractAgentPaymentsRevamp {

    public SwedbankSEAgentPaymentsRevamp(
            CredentialsRequest request, AgentContext context, SignatureKeyPair signatureKeyPair) {
        super(
                request,
                context,
                signatureKeyPair,
                new SwedbankSEConfiguration(request.getProvider().getPayload()),
                new SwedbankSEApiClientProvider());
    }

    @Override
    protected Optional<LoanRefreshController> constructLoanRefreshController() {
        return Optional.of(
                new LoanRefreshController(
                        metricRefreshController,
                        updateController,
                        new SwedbankSELoanFetcher((SwedbankSEApiClient) apiClient)));
    }
}
