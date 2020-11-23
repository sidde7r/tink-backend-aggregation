package se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.ukopenbanking.pis.converter.domestic;

import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.ukopenbanking.pis.UkOpenBankingV31PaymentConstants;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.ukopenbanking.pis.converter.common.PaymentConverterBase;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.ukopenbanking.pis.dto.domestic.DomesticPaymentConsentResponse;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.ukopenbanking.pis.dto.domestic.DomesticPaymentConsentResponseData;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.ukopenbanking.pis.dto.domestic.DomesticPaymentInitiation;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.ukopenbanking.pis.dto.domestic.DomesticPaymentResponse;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.ukopenbanking.pis.dto.domestic.DomesticPaymentResponseData;
import se.tink.backend.aggregation.nxgen.controllers.payment.PaymentResponse;
import se.tink.backend.aggregation.nxgen.storage.Storage;
import se.tink.libraries.amount.ExactCurrencyAmount;
import se.tink.libraries.payment.enums.PaymentStatus;
import se.tink.libraries.payment.rpc.Payment;
import se.tink.libraries.transfer.rpc.RemittanceInformation;

public class DomesticPaymentConverter extends PaymentConverterBase {

    public PaymentResponse convertConsentResponseDtoToTinkPaymentResponse(
            DomesticPaymentConsentResponse response) {
        final DomesticPaymentConsentResponseData responseData = response.getData();
        final DomesticPaymentInitiation initiation = responseData.getInitiation();
        final PaymentStatus paymentStatus =
                convertResponseStatusToPaymentStatus(responseData.getStatus());

        final Payment payment = createPayment(initiation, paymentStatus);

        final Storage storage = new Storage();
        storage.put(
                UkOpenBankingV31PaymentConstants.Storage.CONSENT_ID, responseData.getConsentId());

        return new PaymentResponse(payment, storage);
    }

    public PaymentResponse convertResponseDtoToPaymentResponse(DomesticPaymentResponse response) {
        final DomesticPaymentResponseData responseData = response.getData();
        final DomesticPaymentInitiation initiation = responseData.getInitiation();
        final PaymentStatus paymentStatus =
                convertResponseStatusToPaymentStatus(responseData.getStatus());

        final Payment payment = createPayment(initiation, paymentStatus);

        final Storage storage = new Storage();
        storage.put(
                UkOpenBankingV31PaymentConstants.Storage.CONSENT_ID, responseData.getConsentId());
        storage.put(
                UkOpenBankingV31PaymentConstants.Storage.PAYMENT_ID,
                responseData.getDomesticPaymentId());

        return new PaymentResponse(payment, storage);
    }

    private Payment createPayment(
            DomesticPaymentInitiation initiation, PaymentStatus paymentStatus) {
        final RemittanceInformation remittanceInformation =
                createUnstructuredRemittanceInformation(
                        initiation.getRemittanceInformation().getUnstructured());
        final ExactCurrencyAmount amount =
                convertInstructedAmountToExactCurrencyAmount(initiation.getInstructedAmount());

        return new Payment.Builder()
                .withExactCurrencyAmount(amount)
                .withStatus(paymentStatus)
                .withDebtor(convertDebtorAccountToDebtor(initiation.getDebtorAccount()))
                .withCreditor(convertCreditorAccountToCreditor(initiation.getCreditorAccount()))
                .withCurrency(amount.getCurrencyCode())
                .withRemittanceInformation(remittanceInformation)
                .withUniqueId(initiation.getInstructionIdentification())
                .build();
    }
}
