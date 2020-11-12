package se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.ukopenbanking.base.pis.entity.international;

import com.fasterxml.jackson.databind.PropertyNamingStrategy;
import com.fasterxml.jackson.databind.annotation.JsonNaming;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.ukopenbanking.base.UkOpenBankingV31Constants;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.ukopenbanking.base.pis.entity.domestic.CreditorAccount;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.ukopenbanking.base.pis.entity.domestic.RemittanceInformation;
import se.tink.backend.aggregation.annotations.JsonObject;
import se.tink.backend.aggregation.nxgen.controllers.payment.PaymentResponse;
import se.tink.backend.aggregation.nxgen.storage.Storage;
import se.tink.libraries.payment.rpc.Payment;
import se.tink.libraries.transfer.enums.RemittanceInformationType;

@JsonObject
@JsonNaming(PropertyNamingStrategy.UpperCamelCaseStrategy.class)
public class InternationalPaymentConsentInitiationReq {
    private RemittanceInformation remittanceInformation;
    private String endToEndIdentification;
    private String instructionIdentification;
    private String currencyOfTransfer;
    private CreditorAccount creditorAccount;
    private InstructedAmount instructedAmount;

    // Used in serialization unit tests
    protected InternationalPaymentConsentInitiationReq() {}

    public InternationalPaymentConsentInitiationReq(
            Payment payment, String endToEndIdentification, String instructionIdentification) {
        this.remittanceInformation =
                RemittanceInformation.ofUnstructured(payment.getRemittanceInformation().getValue());
        this.currencyOfTransfer = payment.getCurrency();
        this.endToEndIdentification = endToEndIdentification;
        this.instructionIdentification = instructionIdentification;
        this.creditorAccount = new CreditorAccount(payment.getCreditor());
        this.instructedAmount = new InstructedAmount(payment.getExactCurrencyAmountFromField());
    }

    public PaymentResponse toPaymentResponse(String status, String consentId) {
        se.tink.libraries.transfer.rpc.RemittanceInformation transferRemittanceInformation =
                new se.tink.libraries.transfer.rpc.RemittanceInformation();
        transferRemittanceInformation.setType(RemittanceInformationType.UNSTRUCTURED);
        transferRemittanceInformation.setValue(remittanceInformation.getUnstructured());
        Payment payment =
                new Payment.Builder()
                        .withStatus(UkOpenBankingV31Constants.toPaymentStatus(status))
                        .withExactCurrencyAmount(instructedAmount.toTinkAmount())
                        .withRemittanceInformation(transferRemittanceInformation)
                        .withCreditor(creditorAccount.toCreditor())
                        .withCurrency(currencyOfTransfer)
                        .build();

        Storage storage = new Storage();
        storage.put(UkOpenBankingV31Constants.Storage.CONSENT_ID, consentId);

        return new PaymentResponse(payment, storage);
    }
}
