package se.tink.backend.aggregation.agents.nxgen.se.openbanking.swedbank.executor.payment.entities;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonInclude.Include;
import se.tink.backend.aggregation.agents.nxgen.se.openbanking.swedbank.SwedbankConstants.ErrorMessages;
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

    private AccountEntity(String accountNumber, Type accountIdentifierType) {
        switch (accountIdentifierType) {
            case IBAN:
                this.iban = accountNumber;
                break;
            case SE:
            case SE_BG:
            case SE_PG:
                this.bban = accountNumber;
                break;
            default:
                throw new IllegalStateException(
                        String.format(
                                ErrorMessages.INVALID_ACCOUNT_TYPE,
                                accountIdentifierType.toString()));
        }
    }

    public AccountEntity() {}

    @JsonIgnore
    public static AccountEntity creditorOf(PaymentRequest paymentRequest) {
        return new AccountEntity(
                paymentRequest.getPayment().getCreditor().getAccountNumber(),
                paymentRequest.getPayment().getCreditor().getAccountIdentifierType());
    }

    @JsonIgnore
    public static AccountEntity debtorOf(PaymentRequest paymentRequest) {
        return new AccountEntity(
                paymentRequest.getPayment().getDebtor().getAccountNumber(),
                paymentRequest.getPayment().getDebtor().getAccountIdentifierType());
    }

    @JsonIgnore
    public Creditor toTinkCreditor(Type accountIdentifierType) {
        return new Creditor(
                AccountIdentifier.create(
                        accountIdentifierType, getAccountNumber(accountIdentifierType)));
    }

    @JsonIgnore
    public Debtor toTinkDebtor(Type accountIdentifierType) {
        return new Debtor(
                AccountIdentifier.create(
                        accountIdentifierType, getAccountNumber(accountIdentifierType)));
    }

    private String getAccountNumber(Type accountIdentifierType) {
        return accountIdentifierType.equals(Type.IBAN) ? iban : bban;
    }
}
