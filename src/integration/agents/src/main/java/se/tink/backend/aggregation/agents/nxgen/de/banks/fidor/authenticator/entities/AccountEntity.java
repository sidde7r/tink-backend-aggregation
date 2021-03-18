package se.tink.backend.aggregation.agents.nxgen.de.banks.fidor.authenticator.entities;

import com.fasterxml.jackson.annotation.JsonProperty;
import se.tink.backend.agents.rpc.AccountTypes;
import se.tink.backend.aggregation.annotations.JsonObject;
import se.tink.backend.aggregation.nxgen.core.account.transactional.TransactionalAccount;
import se.tink.libraries.account.AccountIdentifier;
import se.tink.libraries.account.enums.AccountIdentifierType;
import se.tink.libraries.amount.ExactCurrencyAmount;

@JsonObject
public class AccountEntity {
    private String id;

    @JsonProperty("account_number")
    private String accountNumber;

    private String iban;
    private double balance;

    @JsonProperty("balance_available")
    private double availableBalance;

    private String bic;

    @JsonProperty("preauth_amount")
    private double preauthAmount;

    @JsonProperty("cash_flow_per_year")
    private double cashFlowPerYear;

    @JsonProperty("is_locked")
    private boolean isLocked;

    private String currency;

    @JsonProperty("created_at")
    private String createdAt;

    @JsonProperty("updated_at")
    private String updatedAt;

    private String nick;

    public String getId() {
        return id;
    }

    public String getAccountNumber() {
        return accountNumber;
    }

    public String getIban() {
        return iban;
    }

    public double getBalance() {
        return balance;
    }

    public double getAvailableBalance() {
        return availableBalance;
    }

    public String getBic() {
        return bic;
    }

    public double getPreauthAmount() {
        return preauthAmount;
    }

    public double getCashFlowPerYear() {
        return cashFlowPerYear;
    }

    public boolean isLocked() {
        return isLocked;
    }

    public String getCurrency() {
        return currency;
    }

    public String getCreatedAt() {
        return createdAt;
    }

    public String getUpdatedAt() {
        return updatedAt;
    }

    public String getNick() {
        return nick;
    }

    public AccountTypes getType() {
        return AccountTypes.CHECKING;
    }

    public ExactCurrencyAmount getTinkBalance() {
        return ExactCurrencyAmount.inEUR(availableBalance);
    }

    public TransactionalAccount toTransactionalAccount() {
        return TransactionalAccount.builder(getType(), getId(), getTinkBalance())
                .setAccountNumber(getId())
                .addIdentifier(AccountIdentifier.create(AccountIdentifierType.IBAN, iban))
                .build();
    }
}
