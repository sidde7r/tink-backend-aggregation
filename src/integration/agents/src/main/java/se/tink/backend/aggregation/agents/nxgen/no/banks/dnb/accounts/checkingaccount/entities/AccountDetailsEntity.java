package se.tink.backend.aggregation.agents.nxgen.no.banks.dnb.accounts.checkingaccount.entities;

import com.fasterxml.jackson.annotation.JsonAutoDetect;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import java.util.Optional;
import se.tink.backend.aggregation.nxgen.core.account.nxbuilders.modules.balance.BalanceModule;
import se.tink.backend.aggregation.nxgen.core.account.nxbuilders.modules.id.IdModule;
import se.tink.backend.aggregation.nxgen.core.account.transactional.TransactionalAccount;
import se.tink.backend.aggregation.nxgen.core.account.transactional.TransactionalAccountType;
import se.tink.libraries.account.AccountIdentifier;
import se.tink.libraries.account.enums.AccountIdentifierType;
import se.tink.libraries.amount.ExactCurrencyAmount;

@JsonAutoDetect(fieldVisibility = JsonAutoDetect.Visibility.ANY)
@JsonIgnoreProperties(ignoreUnknown = true)
public class AccountDetailsEntity {
    private String number;
    private String currency;
    private String name;
    private String alias;
    private String productNumber;
    private String owner;
    private String ownerName;
    private String property;
    private double availableBalance;

    @JsonProperty("availableBalanceNOK")
    private double availableBalanceNok;

    private double bookBalance;

    @JsonProperty("bookBalanceNOK")
    private double bookBalanceNok;

    private double granting;

    @JsonProperty("grantingNOK")
    private double grantingNok;

    private boolean payFrom;
    private boolean delinquent;
    private boolean loan;
    private boolean transferFrom;
    private boolean transferTo;
    private boolean savings;
    private boolean hidden;
    private boolean taxes;
    private boolean primary;
    private boolean own;
    private boolean bsu;
    private boolean bma;
    private boolean ipa;

    public String getNumber() {
        return number;
    }

    public String getCurrency() {
        return currency;
    }

    public String getName() {
        return name;
    }

    public String getAlias() {
        return alias;
    }

    public String getProductNumber() {
        return productNumber;
    }

    public String getOwner() {
        return owner;
    }

    public String getOwnerName() {
        return ownerName;
    }

    public String getProperty() {
        return property;
    }

    public double getAvailableBalance() {
        return availableBalance;
    }

    public double getAvailableBalanceNok() {
        return availableBalanceNok;
    }

    public double getBookBalance() {
        return bookBalance;
    }

    public double getBookBalanceNok() {
        return bookBalanceNok;
    }

    public double getGranting() {
        return granting;
    }

    public double getGrantingNok() {
        return grantingNok;
    }

    public boolean isPayFrom() {
        return payFrom;
    }

    public boolean isDelinquent() {
        return delinquent;
    }

    public boolean isLoan() {
        return loan;
    }

    public boolean isTransferFrom() {
        return transferFrom;
    }

    public boolean isTransferTo() {
        return transferTo;
    }

    public boolean isSavings() {
        return savings;
    }

    public boolean isHidden() {
        return hidden;
    }

    public boolean isTaxes() {
        return taxes;
    }

    public boolean isPrimary() {
        return primary;
    }

    public boolean isOwn() {
        return own;
    }

    public boolean isBsu() {
        return bsu;
    }

    public boolean isBma() {
        return bma;
    }

    public boolean isIpa() {
        return ipa;
    }

    public Optional<TransactionalAccount> toTransactionalAccount() {
        BalanceModule balance =
                BalanceModule.of(ExactCurrencyAmount.of(availableBalanceNok, currency));
        IdModule id =
                IdModule.builder()
                        .withUniqueIdentifier(number)
                        .withAccountNumber(number)
                        .withAccountName(getTinkAccountName())
                        .addIdentifier(AccountIdentifier.create(AccountIdentifierType.NO, number))
                        .build();
        return TransactionalAccount.nxBuilder()
                .withType(getType())
                .withInferredAccountFlags()
                .withBalance(balance)
                .withId(id)
                .build();
    }

    private TransactionalAccountType getType() {
        return savings ? TransactionalAccountType.SAVINGS : TransactionalAccountType.CHECKING;
    }

    private String getTinkAccountName() {
        return alias != null ? alias : name;
    }
}
