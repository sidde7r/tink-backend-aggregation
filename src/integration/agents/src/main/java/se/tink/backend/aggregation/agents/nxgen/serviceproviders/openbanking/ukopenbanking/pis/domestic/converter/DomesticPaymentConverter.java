package se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.ukopenbanking.pis.domestic.converter;

import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.ukopenbanking.pis.UkOpenBankingPaymentConstants;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.ukopenbanking.pis.common.converter.PaymentConverterBase;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.ukopenbanking.pis.domestic.dto.DomesticPaymentConsentResponse;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.ukopenbanking.pis.domestic.dto.DomesticPaymentConsentResponseData;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.ukopenbanking.pis.domestic.dto.DomesticPaymentInitiation;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.ukopenbanking.pis.domestic.dto.DomesticPaymentResponse;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.ukopenbanking.pis.domestic.dto.DomesticPaymentResponseData;
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
        storage.put(UkOpenBankingPaymentConstants.CONSENT_ID_KEY, responseData.getConsentId());

        return new PaymentResponse(payment, storage);
    }

    public PaymentResponse convertResponseDtoToPaymentResponse(DomesticPaymentResponse response) {
        final DomesticPaymentResponseData responseData = response.getData();
        final DomesticPaymentInitiation initiation = responseData.getInitiation();
        final PaymentStatus paymentStatus =
                convertResponseStatusToPaymentStatus(responseData.getStatus());

        final Payment payment = createPayment(initiation, paymentStatus);

        final Storage storage = new Storage();
        storage.put(UkOpenBankingPaymentConstants.CONSENT_ID_KEY, responseData.getConsentId());
        storage.put(
                UkOpenBankingPaymentConstants.PAYMENT_ID_KEY, responseData.getDomesticPaymentId());

        return new PaymentResponse(payment, storage);
    }

    private Payment createPayment(
            DomesticPaymentInitiation initiation, PaymentStatus paymentStatus) {
        final RemittanceInformation remittanceInformation =
                createRemittanceInformation(initiation.getRemittanceInformation());
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
