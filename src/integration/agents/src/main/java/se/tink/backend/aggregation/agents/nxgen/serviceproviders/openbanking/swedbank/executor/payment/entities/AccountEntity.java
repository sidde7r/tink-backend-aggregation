package se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.swedbank.executor.payment.entities;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonInclude.Include;
import se.tink.backend.aggregation.annotations.JsonObject;
import se.tink.backend.aggregation.nxgen.controllers.payment.PaymentRequest;
import se.tink.libraries.account.AccountIdentifier;
import se.tink.libraries.account.enums.AccountIdentifierType;
import se.tink.libraries.payment.rpc.Creditor;
import se.tink.libraries.payment.rpc.Debtor;

@JsonObject
@JsonInclude(Include.NON_NULL)
public class AccountEntity {
    protected String iban;
    protected String bban;

    @JsonIgnore
    public static AccountEntity creditorOf(PaymentRequest paymentRequest) {
        return new CreditorAccountEntity(
                paymentRequest.getPayment().getCreditor().getAccountNumber(),
                paymentRequest.getPayment().getCreditor().getAccountIdentifierType());
    }

    @JsonIgnore
    public static AccountEntity debtorOf(PaymentRequest paymentRequest) {
        return new DebtorAccountEntity(
                paymentRequest.getPayment().getDebtor().getAccountNumber(),
                paymentRequest.getPayment().getDebtor().getAccountIdentifierType());
    }

    @JsonIgnore
    public Creditor toTinkCreditor(AccountIdentifierType accountIdentifierType) {
        return new Creditor(
                AccountIdentifier.create(
                        accountIdentifierType, getAccountNumber(accountIdentifierType)));
    }

    @JsonIgnore
    public Debtor toTinkDebtor(AccountIdentifierType accountIdentifierType) {
        return new Debtor(
                AccountIdentifier.create(
                        accountIdentifierType, getAccountNumber(accountIdentifierType)));
    }

    private String getAccountNumber(AccountIdentifierType accountIdentifierType) {
        return accountIdentifierType.equals(AccountIdentifierType.IBAN) ? iban : bban;
    }
}
