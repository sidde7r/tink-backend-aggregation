package se.tink.backend.aggregation.agents.nxgen.se.openbanking.swedbank.executor.payment.entities;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonInclude.Include;
import se.tink.backend.aggregation.annotations.JsonObject;
import se.tink.backend.aggregation.nxgen.controllers.payment.PaymentRequest;
import se.tink.libraries.account.AccountIdentifier;
import se.tink.libraries.account.AccountIdentifier.Type;
import se.tink.libraries.payment.rpc.Creditor;
import se.tink.libraries.payment.rpc.Debtor;

@JsonObject
@JsonInclude(Include.NON_NULL)
public class AccountEntity {
    private String iban;
    private String bban;

    public AccountEntity(String accountIdentifierValue, Type accountIdentifierType) {
        switch (accountIdentifierType) {
            case IBAN:
                this.iban = accountIdentifierValue;
                break;
            default:
                this.bban = accountIdentifierValue;
                break;
        }
    }

    public AccountEntity() {}

    public static AccountEntity creditorOf(PaymentRequest paymentRequest) {
        return new AccountEntity(
                paymentRequest.getPayment().getCreditor().getAccountNumber(),
                paymentRequest.getPayment().getCreditor().getAccountIdentifierType());
    }

    public static AccountEntity debtorOf(PaymentRequest paymentRequest) {
        return new AccountEntity(
                paymentRequest.getPayment().getDebtor().getAccountNumber(),
                paymentRequest.getPayment().getDebtor().getAccountIdentifierType());
    }

    public Creditor toTinkCreditor(Type accountIdentifierType) {
        return new Creditor(
                AccountIdentifier.create(
                        accountIdentifierType, getAccountIdentifierValue(accountIdentifierType)));
    }

    public Debtor toTinkDebotor(Type accountIdentifierType) {
        return new Debtor(
                AccountIdentifier.create(
                        accountIdentifierType, getAccountIdentifierValue(accountIdentifierType)));
    }

    private String getAccountIdentifierValue(Type accountIdentifierType) {
        return accountIdentifierType.equals(Type.IBAN) ? iban : bban;
    }
}
