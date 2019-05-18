package se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.ukopenbanking.v31.pis.entity.international;

import com.fasterxml.jackson.databind.PropertyNamingStrategy;
import com.fasterxml.jackson.databind.annotation.JsonNaming;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.ukopenbanking.v31.UkOpenBankingV31Constants;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.ukopenbanking.v31.pis.entity.domestic.CreditorAccount;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.ukopenbanking.v31.pis.entity.domestic.RemittanceInformation;
import se.tink.backend.aggregation.annotations.JsonObject;
import se.tink.backend.aggregation.nxgen.controllers.payment.PaymentResponse;
import se.tink.libraries.payment.rpc.Payment;

@JsonObject
@JsonNaming(PropertyNamingStrategy.UpperCamelCaseStrategy.class)
public class InternationalPaymentConsentInitiationReq {
    private RemittanceInformation remittanceInformation;
    private String endToEndIdentification;
    private String instructionIdentification;
    private String currencyOfTransfer;
    private CreditorAccount creditorAccount;
    private InstructedAmount instructedAmount;

    public InternationalPaymentConsentInitiationReq() {}

    public InternationalPaymentConsentInitiationReq(
            Payment payment, String endToEndIdentification, String instructionIdentification) {
        this.remittanceInformation =
                new RemittanceInformation(payment.getId().toString(), payment.getReference());
        this.currencyOfTransfer = payment.getCurrency();
        this.endToEndIdentification = endToEndIdentification;
        this.instructionIdentification = instructionIdentification;
        this.creditorAccount = new CreditorAccount(payment.getCreditor());
        this.instructedAmount = new InstructedAmount(payment.getAmount());
    }

    public PaymentResponse toPaymentResponse(String status, String consentId) {
        return new PaymentResponse(
                new Payment.Builder()
                        .withStatus(UkOpenBankingV31Constants.toPaymentStatus(status))
                        .withAmount(instructedAmount.toTinkAmount())
                        .withReference(remittanceInformation.getReference())
                        .withCreditor(creditorAccount.toCreditor())
                        .withCurrency(currencyOfTransfer)
                        .putInTemporaryStorage(
                                UkOpenBankingV31Constants.Storage.CONSENT_ID, consentId)
                        .build());
    }
}
