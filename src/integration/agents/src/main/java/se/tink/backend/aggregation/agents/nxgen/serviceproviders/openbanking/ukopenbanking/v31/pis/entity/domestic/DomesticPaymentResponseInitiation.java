package se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.ukopenbanking.v31.pis.entity.domestic;

import com.fasterxml.jackson.databind.PropertyNamingStrategy;
import com.fasterxml.jackson.databind.annotation.JsonNaming;
import java.util.List;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.ukopenbanking.v31.UkOpenBankingV31Constants;
import se.tink.backend.aggregation.annotations.JsonObject;
import se.tink.libraries.payment.rpc.Payment;

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

    public Payment toTinkPayment(String consentId, String domesticPaymentId) {
        return new Payment.Builder()
                .withReference(remittanceInformation.getReference())
                .withCreditor(creditorAccount.toCreditor())
                .withAmount(instructedAmount.toTinkAmount())
                .putInTemporaryStorage(
                        UkOpenBankingV31Constants.Storage.CONSENT_ID, consentId)
                .putInTemporaryStorage(
                        UkOpenBankingV31Constants.Storage.PAYMENT_ID, domesticPaymentId)
                .build();
    }
}
