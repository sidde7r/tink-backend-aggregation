package se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.ukopenbanking.pis.entity.international;

import com.fasterxml.jackson.databind.PropertyNamingStrategy;
import com.fasterxml.jackson.databind.annotation.JsonNaming;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.ukopenbanking.pis.UkOpenBankingV31PaymentConstants;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.ukopenbanking.pis.entity.domestic.CreditorAccount;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.ukopenbanking.pis.entity.domestic.RemittanceInformation;
import se.tink.backend.aggregation.annotations.JsonObject;
import se.tink.backend.aggregation.nxgen.controllers.payment.PaymentResponse;
import se.tink.backend.aggregation.nxgen.storage.Storage;
import se.tink.libraries.payment.rpc.Payment;
import se.tink.libraries.payment.rpc.Payment.Builder;
import se.tink.libraries.transfer.enums.RemittanceInformationType;

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
                RemittanceInformation.ofUnstructured(payment.getRemittanceInformation().getValue());
        this.currencyOfTransfer = payment.getCurrency();
        this.endToEndIdentification = endToEndIdentification;
        this.instructionIdentification = instructionIdentification;
        this.creditorAccount = new CreditorAccount(payment.getCreditor());
        this.instructedAmount = new InstructedAmountV2(payment.getExactCurrencyAmountFromField());
    }

    public PaymentResponse toPaymentResponse(String status, String consentId) {
        se.tink.libraries.transfer.rpc.RemittanceInformation transferRemittanceInformation =
                new se.tink.libraries.transfer.rpc.RemittanceInformation();
        transferRemittanceInformation.setType(RemittanceInformationType.UNSTRUCTURED);
        transferRemittanceInformation.setValue(remittanceInformation.getUnstructured());
        Payment payment =
                new Builder()
                        .withStatus(UkOpenBankingV31PaymentConstants.toPaymentStatus(status))
                        .withExactCurrencyAmount(instructedAmount.toTinkAmount())
                        .withRemittanceInformation(transferRemittanceInformation)
                        .withCreditor(creditorAccount.toCreditor())
                        .withCurrency(currencyOfTransfer)
                        .build();

        Storage storage = new Storage();
        storage.put(UkOpenBankingV31PaymentConstants.Storage.CONSENT_ID, consentId);

        return new PaymentResponse(payment, storage);
    }
}
