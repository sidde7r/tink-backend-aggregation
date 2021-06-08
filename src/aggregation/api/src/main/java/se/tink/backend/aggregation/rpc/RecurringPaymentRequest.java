package se.tink.backend.aggregation.rpc;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.UUID;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;
import se.tink.backend.aggregationcontroller.v1.rpc.enums.CredentialsRequestType;
import se.tink.libraries.account.enums.AccountIdentifierType;
import se.tink.libraries.payments.common.model.PaymentScheme;
import se.tink.libraries.transfer.rpc.ExecutionRule;
import se.tink.libraries.transfer.rpc.Frequency;
import se.tink.libraries.transfer.rpc.RecurringPayment;
import se.tink.libraries.transfer.rpc.RemittanceInformation;
import se.tink.libraries.transfer.rpc.Transfer;
import se.tink.libraries.uuid.UUIDUtils;

@Setter
@Getter
@NoArgsConstructor
@ToString
@JsonIgnoreProperties(ignoreUnknown = true)
public class RecurringPaymentRequest extends TransferRequest {

    private UUID id;
    private String credentialsId;
    private String userId;
    private AccountIdentifierType creditorType;
    @ToString.Exclude private String creditorId;
    @ToString.Exclude private String creditorName;
    private AccountIdentifierType debtorType;
    @ToString.Exclude private String debtorId;
    private BigDecimal amount;
    private String currency;
    @ToString.Exclude private RemittanceInformation remittanceInformation;
    private PaymentScheme paymentScheme;
    @ToString.Exclude private String originatingUserIp;
    private Frequency frequency;
    private LocalDate startDate;
    private LocalDate endDate;
    private ExecutionRule executionRule;
    private int dayOfExecution;

    @Override
    public CredentialsRequestType getType() {
        return CredentialsRequestType.RECURRING_PAYMENT;
    }

    @JsonIgnore
    public RecurringPayment getRecurringPayment() {
        RecurringPayment recurringPayment = new RecurringPayment();
        recurringPayment.setId(id);
        recurringPayment.setUserId(UUIDUtils.fromTinkUUID(userId));
        recurringPayment.setCredentialsId(UUIDUtils.fromTinkUUID(credentialsId));
        recurringPayment.setDestination(creditorType, creditorId, creditorName);
        recurringPayment.setSource(debtorType, debtorId);
        recurringPayment.setAmount(amount, currency);
        recurringPayment.setRemittanceInformation(remittanceInformation);
        recurringPayment.setPaymentScheme(paymentScheme);
        recurringPayment.setOriginatingUserIp(originatingUserIp);
        recurringPayment.setFrequency(frequency);
        recurringPayment.setStartDate(startDate);
        recurringPayment.setEndDate(endDate);
        recurringPayment.setExecutionRule(executionRule);
        recurringPayment.setDayOfExecution(dayOfExecution);

        return recurringPayment;
    }

    // temp overriding unless we restructure code in better way
    @JsonIgnore
    public Transfer getTransfer() {
        return getRecurringPayment();
    }

    public boolean isSkipRefresh() {
        return true; // No need to refresh after PIS in case of Recurring Payments.
    }
}
