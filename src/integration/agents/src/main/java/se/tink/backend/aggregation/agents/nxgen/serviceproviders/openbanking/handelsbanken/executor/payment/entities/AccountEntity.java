package se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.handelsbanken.executor.payment.entities;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;
import se.tink.backend.aggregation.annotations.JsonObject;
import se.tink.backend.aggregation.nxgen.controllers.payment.PaymentRequest;
import se.tink.libraries.payment.rpc.Creditor;
import se.tink.libraries.payment.rpc.Debtor;

@JsonObject
public class AccountEntity {

    @JsonProperty("value")
    private String accountNumber;

    private String accountType;

    private AccountEntity(String accountNumber, String accountType) {
        this.accountNumber = accountNumber;
        this.accountType = accountType;
    }

    @JsonIgnore
    public static AccountEntity creditorOf(PaymentRequest paymentRequest) {
        Creditor creditor = paymentRequest.getPayment().getCreditor();
        return new AccountEntity(
                creditor.getAccountNumber(), creditor.getAccountIdentifierType().name());
    }

    @JsonIgnore
    public static AccountEntity debtorOf(PaymentRequest paymentRequest) {
        Debtor debtor = paymentRequest.getPayment().getDebtor();
        return new AccountEntity(
                debtor.getAccountNumber(), debtor.getAccountIdentifierType().name());
    }
}
