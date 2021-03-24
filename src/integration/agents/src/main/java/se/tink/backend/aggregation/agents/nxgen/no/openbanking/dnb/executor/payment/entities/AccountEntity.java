package se.tink.backend.aggregation.agents.nxgen.no.openbanking.dnb.executor.payment.entities;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonInclude.Include;
import com.google.common.base.Strings;
import se.tink.backend.aggregation.annotations.JsonObject;
import se.tink.backend.aggregation.nxgen.controllers.payment.PaymentRequest;
import se.tink.libraries.account.AccountIdentifier;
import se.tink.libraries.account.enums.AccountIdentifierType;
import se.tink.libraries.payment.rpc.Creditor;
import se.tink.libraries.payment.rpc.Debtor;

@JsonObject
@JsonInclude(Include.NON_NULL)
public class AccountEntity {
    private String iban;
    private String bban;

    public AccountEntity() {}

    public AccountEntity(AccountIdentifierType accountType, String accountNumber) {
        switch (accountType) {
            case IBAN:
                this.iban = accountNumber;
                break;
            case NO:
                this.bban = accountNumber;
                break;
            default:
                throw new IllegalStateException("Unknown account type: " + accountType.toString());
        }
    }

    public static AccountEntity creditorOf(PaymentRequest paymentRequest) {
        Creditor creditor = paymentRequest.getPayment().getCreditor();
        return new AccountEntity(creditor.getAccountIdentifierType(), creditor.getAccountNumber());
    }

    public static AccountEntity debtorOf(PaymentRequest paymentRequest) {
        Debtor debtor = paymentRequest.getPayment().getDebtor();
        return new AccountEntity(debtor.getAccountIdentifierType(), debtor.getAccountNumber());
    }

    public Creditor toTinkCreditor() {
        return new Creditor(AccountIdentifier.create(getAccountType(), getAccountNumber()));
    }

    public Debtor toTinkDebtor() {
        return new Debtor(AccountIdentifier.create(getAccountType(), getAccountNumber()));
    }

    @JsonIgnore
    public String getAccountNumber() {
        return Strings.isNullOrEmpty(bban) ? iban : bban;
    }

    @JsonIgnore
    public AccountIdentifierType getAccountType() {
        return Strings.isNullOrEmpty(bban) ? AccountIdentifierType.IBAN : AccountIdentifierType.NO;
    }
}
