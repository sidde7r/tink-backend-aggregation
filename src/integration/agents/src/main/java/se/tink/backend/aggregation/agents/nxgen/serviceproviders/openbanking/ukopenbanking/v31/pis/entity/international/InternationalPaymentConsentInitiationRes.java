package se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.ukopenbanking.v31.pis.entity.international;

import com.fasterxml.jackson.databind.PropertyNamingStrategy;
import com.fasterxml.jackson.databind.annotation.JsonNaming;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.ukopenbanking.v31.UkOpenBankingV31Constants;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.ukopenbanking.v31.pis.entity.domestic.CreditorAccount;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.ukopenbanking.v31.pis.entity.domestic.RemittanceInformation;
import se.tink.backend.aggregation.annotations.JsonObject;
import se.tink.backend.aggregation.nxgen.controllers.payment.PaymentResponse;
import se.tink.backend.aggregation.nxgen.storage.Storage;
import se.tink.libraries.payment.rpc.Payment;
import se.tink.libraries.payment.rpc.Payment.Builder;

@JsonObject
@JsonNaming(PropertyNamingStrategy.UpperCamelCaseStrategy.class)
public class InternationalPaymentConsentInitiationRes {
    private RemittanceInformation remittanceInformation;
    private String endToEndIdentification;
    private String instructionIdentification;
    private String currencyOfTransfer;
    private CreditorAccount creditorAccount;
    private InstructedAmountV2 instructedAmount;

    public InternationalPaymentConsentInitiationRes() {}

    public InternationalPaymentConsentInitiationRes(
            Payment payment, String endToEndIdentification, String instructionIdentification) {
        this.remittanceInformation =
                new RemittanceInformation(payment.getId().toString(), payment.getReference());
        this.currencyOfTransfer = payment.getCurrency();
        this.endToEndIdentification = endToEndIdentification;
        this.instructionIdentification = instructionIdentification;
        this.creditorAccount = new CreditorAccount(payment.getCreditor());
        this.instructedAmount = new InstructedAmountV2(payment.getAmount());
    }

    public PaymentResponse toPaymentResponse(String status, String consentId) {
        Payment payment =
                new Builder()
                        .withStatus(UkOpenBankingV31Constants.toPaymentStatus(status))
                        .withAmount(instructedAmount.toTinkAmount())
                        .withReference(remittanceInformation.getReference())
                        .withCreditor(creditorAccount.toCreditor())
                        .withCurrency(currencyOfTransfer)
                        .build();

        Storage storage = new Storage();
        storage.put(UkOpenBankingV31Constants.Storage.CONSENT_ID, consentId);

        return new PaymentResponse(payment, storage);
    }
}
