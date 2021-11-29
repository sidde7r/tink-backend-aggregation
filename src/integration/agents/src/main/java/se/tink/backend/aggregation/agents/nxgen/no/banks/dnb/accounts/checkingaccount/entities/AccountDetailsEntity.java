package se.tink.backend.aggregation.agents.nxgen.no.banks.dnb.accounts.checkingaccount.entities;

import com.fasterxml.jackson.annotation.JsonAutoDetect;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import java.util.Optional;
import lombok.Getter;
import se.tink.backend.aggregation.nxgen.core.account.nxbuilders.modules.balance.BalanceModule;
import se.tink.backend.aggregation.nxgen.core.account.nxbuilders.modules.id.IdModule;
import se.tink.backend.aggregation.nxgen.core.account.transactional.TransactionalAccount;
import se.tink.backend.aggregation.nxgen.core.account.transactional.TransactionalAccountType;
import se.tink.libraries.account.identifiers.BbanIdentifier;
import se.tink.libraries.amount.ExactCurrencyAmount;

@JsonAutoDetect(fieldVisibility = JsonAutoDetect.Visibility.ANY)
@JsonIgnoreProperties(ignoreUnknown = true)
@Getter
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

    public Optional<TransactionalAccount> toTransactionalAccount() {
        BalanceModule balance =
                BalanceModule.builder()
                        .withBalance(ExactCurrencyAmount.of(bookBalance, currency))
                        .setAvailableBalance(ExactCurrencyAmount.of(availableBalance, currency))
                        .build();

        IdModule id =
                IdModule.builder()
                        .withUniqueIdentifier(number)
                        .withAccountNumber(number)
                        .withAccountName(getTinkAccountName())
                        .addIdentifier(new BbanIdentifier(number))
                        .build();
        return TransactionalAccount.nxBuilder()
                .withType(getType())
                .withInferredAccountFlags()
                .withBalance(balance)
                .withId(id)
                .addHolderName(ownerName)
                .build();
    }

    private TransactionalAccountType getType() {
        return savings ? TransactionalAccountType.SAVINGS : TransactionalAccountType.CHECKING;
    }

    private String getTinkAccountName() {
        return alias != null ? alias : name;
    }
}
