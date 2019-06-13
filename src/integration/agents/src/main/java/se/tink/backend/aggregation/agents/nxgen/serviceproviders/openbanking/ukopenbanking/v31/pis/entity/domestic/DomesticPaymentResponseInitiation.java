package se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.ukopenbanking.v31.pis.entity.domestic;

import com.fasterxml.jackson.databind.PropertyNamingStrategy;
import com.fasterxml.jackson.databind.annotation.JsonNaming;
import java.util.List;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.ukopenbanking.v31.UkOpenBankingV31Constants;
import se.tink.backend.aggregation.annotations.JsonObject;
import se.tink.backend.aggregation.nxgen.controllers.payment.PaymentResponse;
import se.tink.backend.aggregation.nxgen.storage.Storage;
import se.tink.libraries.payment.rpc.Payment;
import se.tink.libraries.payment.rpc.Payment.Builder;

@JsonObject
@JsonNaming(PropertyNamingStrategy.UpperCamelCaseStrategy.class)
public class DomesticPaymentResponseInitiation {
    private SupplementaryData supplementaryData;
    private List<String> localInstrument;
    private DebtorAccount debtorAccount;
    private RemittanceInformation remittanceInformation;
    private String endToEndIdentification;
    private String instructionIdentification;
    private CreditorAccount creditorAccount;
    private InstructedAmount instructedAmount;
    private CreditorPostalAddress creditorPostalAddress;

    public PaymentResponse toTinkPaymentResponse(String consentId, String domesticPaymentId) {
        Payment payment =
                new Builder()
                        .withReference(remittanceInformation.getReference())
                        .withCreditor(creditorAccount.toCreditor())
                        .withAmount(instructedAmount.toTinkAmount())
                        .build();

        Storage storage = new Storage();
        storage.put(UkOpenBankingV31Constants.Storage.CONSENT_ID, consentId);
        storage.put(UkOpenBankingV31Constants.Storage.PAYMENT_ID, domesticPaymentId);

        return new PaymentResponse(payment, storage);
    }
}
