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
import se.tink.backend.aggregation.agents.utils.random.RandomUtils;
import se.tink.backend.aggregation.nxgen.controllers.payment.PaymentRequest;
import se.tink.backend.aggregation.nxgen.controllers.payment.PaymentResponse;
import se.tink.libraries.account.AccountIdentifier;
import se.tink.libraries.amount.Amount;
import se.tink.libraries.payment.rpc.Creditor;
import se.tink.libraries.payment.rpc.Debtor;
import se.tink.libraries.payment.rpc.Payment;
import se.tink.libraries.payment.rpc.Reference;

public class UkOpenBankingV31Pis implements UkOpenBankingPis {

    private final UkOpenBankingPisConfig ukOpenBankingPisConfig;
    private final boolean mustNotHaveSourceAccountSpecified;
    private final String internalTransferId;

    public UkOpenBankingV31Pis(UkOpenBankingPisConfig ukOpenBankingPisConfig) {
        this.ukOpenBankingPisConfig = ukOpenBankingPisConfig;
        this.mustNotHaveSourceAccountSpecified = false;
        this.internalTransferId = RandomUtils.generateRandomHexEncoded(15);
    }

    @Override
    public boolean mustNotHaveSourceAccountSpecified() {
        return mustNotHaveSourceAccountSpecified;
    }

    @Override
    public PaymentResponse getBankTransferIntentId(
            UkOpenBankingApiClient apiClient, PaymentRequest paymentRequest)
            throws TransferExecutionException {

        // TODO: Currently we only support domestic transfers. In the future, we need to detect if
        // the payment is domestic or international and support both of them.

        // TODO: What to do with commented-out fields below?

        DomesticPaymentConsentRequest consentRequest =
                new DomesticPaymentConsentRequest(paymentRequest.getPayment());

        DomesticPaymentConsentResponse consentResponse =
                apiClient.createDomesticPaymentConsent(
                        ukOpenBankingPisConfig,
                        consentRequest,
                        DomesticPaymentConsentResponse.class);

        return consentResponse.toTinkPaymentResponse();

        // TODO: Do we need to go check the status of the consent (see below)?

        /*
           Step 6: Get Payment-Order/Consent Status

           The PISP can check the status of the payment-order consent
           (with the ConsentId) or payment-order resource (with the
           payment-order resource identifier). This is carried out by making a
           GET request to the payment-order consent or payment-order resource.
        */
    }

    @Override
    public void executeBankTransfer(
            UkOpenBankingApiClient apiClient,
            String intentId,
            @Nullable AccountIdentifier sourceIdentifier,
            AccountIdentifier destinationIdentifier,
            Amount amount,
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
                        .withAmount(amount)
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
