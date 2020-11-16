package se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.ukopenbanking.base.pis.entity.international;

import com.fasterxml.jackson.databind.PropertyNamingStrategy;
import com.fasterxml.jackson.databind.annotation.JsonNaming;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.ukopenbanking.base.UkOpenBankingV31Constants;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.ukopenbanking.base.pis.entity.domestic.CreditorAccount;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.ukopenbanking.base.pis.entity.domestic.InstructedAmount;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.ukopenbanking.base.pis.entity.domestic.RemittanceInformation;
import se.tink.backend.aggregation.annotations.JsonObject;
import se.tink.backend.aggregation.nxgen.controllers.payment.PaymentResponse;
import se.tink.backend.aggregation.nxgen.storage.Storage;
import se.tink.libraries.payment.rpc.Payment;

@JsonObject
@JsonNaming(PropertyNamingStrategy.UpperCamelCaseStrategy.class)
public class PaymentInitiationExtended {
    private SupplementaryData supplementaryData;
    private DebtorAccount debtorAccount;
    private String endToEndIdentification;
    private String instructionIdentification;
    private String currencyOfTransfer;
    private CreditorAccount creditorAccount;
    private String purpose;
    private String chargeBearer;
    private String instructionPriority;
    private String localInstrument;
    private RemittanceInformation remittanceInformation;
    private ExchangeRateInformation exchangeRateInformation;
    private Creditor creditor;
    private InstructedAmount instructedAmount;

    public PaymentResponse toTinkPaymentResponse(
            String status, String expectedExecutionDateTime, String internationalPaymentId) {
        Payment payment =
                new Payment.Builder()
                        .withStatus(UkOpenBankingV31Constants.toPaymentStatus(status))
                        .withCreditor(creditorAccount.toCreditor())
                        .withExactCurrencyAmount(instructedAmount.toTinkAmount())
                        .withExecutionDate(parseDate(expectedExecutionDateTime))
                        .build();

        Storage storage = new Storage();
        storage.put(UkOpenBankingV31Constants.Storage.PAYMENT_ID, internationalPaymentId);

        return new PaymentResponse(payment, storage);
    }

    private LocalDate parseDate(String expectedExecutionDateTime) {
        return LocalDate.parse(expectedExecutionDateTime, DateTimeFormatter.ISO_DATE_TIME);
    }
}
