package se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.ukopenbanking.v31;

import javax.annotation.Nullable;
import se.tink.backend.aggregation.agents.TransferExecutionException;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.ukopenbanking.base.UkOpenBankingApiClient;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.ukopenbanking.base.interfaces.UkOpenBankingPis;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.ukopenbanking.base.interfaces.UkOpenBankingPisConfig;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.ukopenbanking.v31.pis.rpc.domestic.DomesticPaymentConsentRequest;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.ukopenbanking.v31.pis.rpc.domestic.DomesticPaymentConsentResponse;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.ukopenbanking.v31.pis.rpc.domestic.DomesticPaymentRequest;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.ukopenbanking.v31.pis.rpc.domestic.DomesticPaymentResponse;
import se.tink.backend.aggregation.nxgen.agents.componentproviders.generated.randomness.RandomValueGenerator;
import se.tink.backend.aggregation.nxgen.controllers.payment.PaymentRequest;
import se.tink.backend.aggregation.nxgen.controllers.payment.PaymentResponse;
import se.tink.libraries.account.AccountIdentifier;
import se.tink.libraries.amount.ExactCurrencyAmount;
import se.tink.libraries.payment.rpc.Creditor;
import se.tink.libraries.payment.rpc.Debtor;
import se.tink.libraries.payment.rpc.Payment;
import se.tink.libraries.payment.rpc.Reference;

public class UkOpenBankingV31Pis implements UkOpenBankingPis {

    private final UkOpenBankingPisConfig ukOpenBankingPisConfig;
    private final boolean mustNotHaveSourceAccountSpecified;
    private final String internalTransferId;
    private final RandomValueGenerator randomValueGenerator;

    public UkOpenBankingV31Pis(
            UkOpenBankingPisConfig ukOpenBankingPisConfig,
            RandomValueGenerator randomValueGenerator) {
        this.ukOpenBankingPisConfig = ukOpenBankingPisConfig;
        this.mustNotHaveSourceAccountSpecified = false;
        this.randomValueGenerator = randomValueGenerator;
        this.internalTransferId = randomValueGenerator.generateRandomHexEncoded(15);
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

    @Override
    public void executeBankTransfer(
            UkOpenBankingApiClient apiClient,
            String intentId,
            @Nullable AccountIdentifier sourceIdentifier,
            AccountIdentifier destinationIdentifier,
            ExactCurrencyAmount amount,
            String referenceText)
            throws TransferExecutionException {
        /*
            Step 4: Confirm Funds (Domestic and International Single Immediate Payments Only)

            Once the PSU is authenticated and authorised the payment-order-consent,
            the PISP can check whether funds are available to make the payment.
            This is carried out by making a GET request, calling the funds-confirmation
            operator on the payment-order-consent resource.

            TODO: Should we implement that step?
        */

        // TODO: Currently we only support domestic transfers. In the future, we need to detect if
        // the payment is domestic or international and support both of them.

        Payment payment =
                new Payment.Builder()
                        .withCreditor(
                                new Creditor(
                                        destinationIdentifier,
                                        destinationIdentifier.getName().orElse("Unknown Person")))
                        .withDebtor(new Debtor(sourceIdentifier))
                        .withExactCurrencyAmount(amount)
                        .withReference(new Reference("TRANSFER", referenceText))
                        .withUniqueId(internalTransferId)
                        // .withExecutionDate()
                        // .withStatus()
                        // .withType()
                        .build();

        DomesticPaymentRequest request =
                new DomesticPaymentRequest(
                        payment, intentId, internalTransferId, internalTransferId);

        DomesticPaymentResponse response =
                apiClient.executeDomesticPayment(
                        ukOpenBankingPisConfig, request, DomesticPaymentResponse.class);

        PaymentResponse tinkResponse = response.toTinkPaymentResponse();

        // TODO: Do we need to go to //domestic-payments/{DomesticPaymentId} and check the status?
    }
}
