package se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.sparebank.executor.payment.entities;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.google.common.base.Strings;
import lombok.AllArgsConstructor;
import lombok.NoArgsConstructor;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.sparebank.fetcher.transactionalaccount.rpc.AccountResponse;
import se.tink.backend.aggregation.annotations.JsonObject;
import se.tink.libraries.account.AccountIdentifier;
import se.tink.libraries.account.enums.AccountIdentifierType;
import se.tink.libraries.payment.rpc.Creditor;
import se.tink.libraries.payment.rpc.Debtor;
import se.tink.libraries.payment.rpc.Payment;

@JsonInclude(JsonInclude.Include.NON_NULL)
@AllArgsConstructor
@NoArgsConstructor
@JsonObject
public class AccountEntity {
    private String bban;
    private String iban;
    private String currency;

    public AccountEntity(AccountIdentifierType accountType, String accountNumber) {
        switch (accountType) {
            case IBAN:
                this.iban = accountNumber;
                break;
            case NO:
            case BBAN:
                this.bban = accountNumber;
                this.currency = "NOK";
                break;
            default:
                throw new IllegalStateException("Unknown account type: " + accountType);
        }
    }

    @JsonIgnore
    public static AccountEntity debtorOf(AccountResponse accountResponse) {
        String accountNumber = accountResponse.getAccounts().stream().findFirst().get().getBban();
        return new AccountEntity(AccountIdentifierType.NO, accountNumber);
    }

    @JsonIgnore
    public static AccountEntity creditorOf(Payment payment) {
        Creditor creditor = payment.getCreditor();
        return new AccountEntity(creditor.getAccountIdentifierType(), creditor.getAccountNumber());
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

    public AccountEntity toBban() {
        return Strings.isNullOrEmpty(bban)
                ? new AccountEntity(AccountIdentifierType.NO, iban.substring(4))
                : new AccountEntity(AccountIdentifierType.NO, bban);
    }
}
