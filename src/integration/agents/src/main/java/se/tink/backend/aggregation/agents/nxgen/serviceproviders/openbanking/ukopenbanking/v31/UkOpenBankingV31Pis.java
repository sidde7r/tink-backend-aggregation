package se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.ukopenbanking.v31;

import se.tink.backend.aggregation.agents.exceptions.transfer.TransferExecutionException;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.ukopenbanking.base.UkOpenBankingApiClient;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.ukopenbanking.base.interfaces.UkOpenBankingPis;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.ukopenbanking.base.interfaces.UkOpenBankingPisConfig;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.ukopenbanking.v31.pis.rpc.domestic.DomesticPaymentConsentRequest;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.ukopenbanking.v31.pis.rpc.domestic.DomesticPaymentConsentResponse;
import se.tink.backend.aggregation.nxgen.agents.componentproviders.generated.randomness.RandomValueGenerator;
import se.tink.backend.aggregation.nxgen.controllers.payment.PaymentRequest;
import se.tink.backend.aggregation.nxgen.controllers.payment.PaymentResponse;

public class UkOpenBankingV31Pis implements UkOpenBankingPis {

    private final UkOpenBankingPisConfig ukOpenBankingPisConfig;
    private final boolean mustNotHaveSourceAccountSpecified;
    private final RandomValueGenerator randomValueGenerator;

    public UkOpenBankingV31Pis(
            UkOpenBankingPisConfig ukOpenBankingPisConfig,
            RandomValueGenerator randomValueGenerator) {
        this.ukOpenBankingPisConfig = ukOpenBankingPisConfig;
        this.mustNotHaveSourceAccountSpecified = false;
        this.randomValueGenerator = randomValueGenerator;
    }

    @Override
    public boolean mustNotHaveSourceAccountSpecified() {
        return mustNotHaveSourceAccountSpecified;
    }

    @Override
    public PaymentResponse setupPaymentOrderConsent(
            UkOpenBankingApiClient apiClient, PaymentRequest paymentRequest)
            throws TransferExecutionException {

        DomesticPaymentConsentRequest consentRequest =
                new DomesticPaymentConsentRequest(paymentRequest.getPayment());

        DomesticPaymentConsentResponse consentResponse =
                apiClient.createDomesticPaymentConsent(
                        ukOpenBankingPisConfig,
                        consentRequest,
                        DomesticPaymentConsentResponse.class);

        // Our flow has hardcoded a SCA redirect after this request so we can only continue if
        // the status is AwaitingAuthorisation.
        if (!consentResponse.hasStatusAwaitingAuthorisation()) {
            throw new IllegalStateException(
                    String.format(
                            "Consent resource status was %s, expected status AwaitingAuthorisation.",
                            consentResponse.getData().getStatus()));
        }

        return consentResponse.toTinkPaymentResponse();
    }
}
