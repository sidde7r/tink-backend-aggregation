package se.tink.backend.aggregation.agents.nxgen.no.openbanking.nordea.payment;

import java.util.Collection;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.nordeabase.NordeaBaseApiClient;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.nordeabase.executor.payment.NordeaBasePaymentExecutor;
import se.tink.backend.aggregation.nxgen.controllers.authentication.utils.StrongAuthenticationState;
import se.tink.backend.aggregation.nxgen.controllers.payment.PaymentRequest;
import se.tink.backend.aggregation.nxgen.controllers.signing.Signer;
import se.tink.backend.aggregation.nxgen.controllers.utils.SupplementalInformationHelper;
import se.tink.libraries.payment.enums.PaymentType;

public class NordeaNoStandardPaymentExecutor extends NordeaBasePaymentExecutor {

    private final NordeaNoSigningController nordeaNoSigningController;
    SupplementalInformationHelper supplementalInformationHelper;

    public NordeaNoStandardPaymentExecutor(
            NordeaBaseApiClient apiClient,
            NordeaNoSigningController nordeaNoSigningController,
            SupplementalInformationHelper supplementalInformationHelper,
            StrongAuthenticationState strongAuthenticationState) {

        super(apiClient, supplementalInformationHelper, strongAuthenticationState);
        this.nordeaNoSigningController = nordeaNoSigningController;
        this.supplementalInformationHelper = supplementalInformationHelper;
    }

    @Override
    protected PaymentType getPaymentType(PaymentRequest paymentRequest) {
        return PaymentType.DOMESTIC;
    }

    @Override
    protected Collection<PaymentType> getSupportedPaymentTypes() {
        return null;
    }

    @Override
    protected Signer getSigner() {
        return nordeaNoSigningController;
    }
}
