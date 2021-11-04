package se.tink.backend.aggregation.agents.nxgen.fi.openbanking.nordea.payment;

import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.nordeabase.NordeaBaseApiClient;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.nordeabase.executor.payment.NordeaBaseSingleScaPaymentExecutor;
import se.tink.backend.aggregation.nxgen.controllers.payment.PaymentRequest;
import se.tink.backend.aggregation.nxgen.controllers.signing.Signer;
import se.tink.backend.aggregation.nxgen.controllers.utils.SupplementalInformationController;
import se.tink.backend.aggregation.nxgen.storage.SessionStorage;
import se.tink.libraries.payment.enums.PaymentType;

public class NordeaFiStandardPaymentExecutor extends NordeaBaseSingleScaPaymentExecutor {
    private final NordeaFiSigningController nordeaFiSigningController;

    public NordeaFiStandardPaymentExecutor(
            NordeaBaseApiClient apiClient,
            SupplementalInformationController supplementalInformationController,
            SessionStorage sessionStorage,
            NordeaFiSigningController nordeaFiSigningController) {
        super(apiClient, sessionStorage, supplementalInformationController);
        this.nordeaFiSigningController = nordeaFiSigningController;
    }

    @Override
    protected PaymentType getPaymentType(PaymentRequest paymentRequest) {
        return PaymentType.SEPA;
    }

    @Override
    protected Signer<PaymentRequest> getSigner() {
        return nordeaFiSigningController;
    }
}
