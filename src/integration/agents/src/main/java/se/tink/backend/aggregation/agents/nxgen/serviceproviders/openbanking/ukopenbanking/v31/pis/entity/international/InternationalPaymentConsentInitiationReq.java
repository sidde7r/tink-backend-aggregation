package se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.ukopenbanking.v31.pis.entity.international;

import com.fasterxml.jackson.databind.PropertyNamingStrategy;
import com.fasterxml.jackson.databind.annotation.JsonNaming;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.ukopenbanking.base.UkOpenBankingV31Constants;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.ukopenbanking.v31.pis.UkOpenBankingV31PisUtils;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.ukopenbanking.v31.pis.entity.domestic.CreditorAccount;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.ukopenbanking.v31.pis.entity.domestic.RemittanceInformation;
import se.tink.backend.aggregation.annotations.JsonObject;
import se.tink.backend.aggregation.nxgen.controllers.payment.PaymentResponse;
import se.tink.backend.aggregation.nxgen.storage.Storage;
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

    // Used in serialization unit tests
    protected InternationalPaymentConsentInitiationReq() {}

    public InternationalPaymentConsentInitiationReq(
            Payment payment, String endToEndIdentification, String instructionIdentification) {
        this.remittanceInformation = new RemittanceInformation(payment.getReference());
        this.currencyOfTransfer = payment.getCurrency();
        this.endToEndIdentification = endToEndIdentification;
        this.instructionIdentification = instructionIdentification;
        this.creditorAccount = new CreditorAccount(payment.getCreditor());
        this.instructedAmount = new InstructedAmount(payment.getExactCurrencyAmountFromField());
    }

    public PaymentResponse toPaymentResponse(String status, String consentId) {
        Payment payment =
                new Payment.Builder()
                        .withStatus(UkOpenBankingV31Constants.toPaymentStatus(status))
                        .withExactCurrencyAmount(instructedAmount.toTinkAmount())
                        .withReference(
                                UkOpenBankingV31PisUtils.createTinkReference(
                                        remittanceInformation.getReference()))
                        .withCreditor(creditorAccount.toCreditor())
                        .withCurrency(currencyOfTransfer)
                        .build();

        Storage storage = new Storage();
        storage.put(UkOpenBankingV31Constants.Storage.CONSENT_ID, consentId);

        return new PaymentResponse(payment, storage);
    }
}
