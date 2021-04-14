package se.tink.backend.aggregation.rpc;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.UUID;
import lombok.Getter;
import lombok.Setter;
import se.tink.backend.agents.rpc.Credentials;
import se.tink.backend.agents.rpc.Provider;
import se.tink.backend.aggregationcontroller.v1.rpc.enums.CredentialsRequestType;
import se.tink.libraries.account.enums.AccountIdentifierType;
import se.tink.libraries.payments.common.model.PaymentScheme;
import se.tink.libraries.signableoperation.rpc.SignableOperation;
import se.tink.libraries.transfer.rpc.ExecutionRule;
import se.tink.libraries.transfer.rpc.Frequency;
import se.tink.libraries.transfer.rpc.RecurringPayment;
import se.tink.libraries.transfer.rpc.RemittanceInformation;
import se.tink.libraries.user.rpc.User;
import se.tink.libraries.uuid.UUIDDeserializer;

@Setter
@Getter
@JsonIgnoreProperties(ignoreUnknown = true)
public class RecurringPaymentRequest extends TransferRequest {

    private UUID id;

    @JsonDeserialize(using = UUIDDeserializer.class)
    private UUID credentialsId;

    private UUID userId;
    private AccountIdentifierType creditorType;
    private String creditorId;
    private AccountIdentifierType debtorType;
    private String debtorId;
    private BigDecimal amount;
    private String currency;
    private RemittanceInformation remittanceInformation;
    private PaymentScheme paymentScheme;
    private String originatingUserIp;
    private Frequency frequency;
    private LocalDate startDate;
    private LocalDate endDate;
    private ExecutionRule executionRule;
    private int dayOfExecution;

    public RecurringPaymentRequest() {}

    public RecurringPaymentRequest(
            User user,
            Provider provider,
            Credentials credentials,
            SignableOperation signableOperation,
            boolean update) {
        super(user, provider, credentials, signableOperation, update);
    }

    @Override
    public CredentialsRequestType getType() {
        return CredentialsRequestType.RECURRING_PAYMENT;
    }

    public RecurringPayment getRecurringPayment() {
        RecurringPayment recurringPayment = new RecurringPayment();
        recurringPayment.setId(id);
        recurringPayment.setUserId(userId);
        recurringPayment.setCredentialsId(credentialsId);
        recurringPayment.setDestination(creditorType, creditorId);
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
}
