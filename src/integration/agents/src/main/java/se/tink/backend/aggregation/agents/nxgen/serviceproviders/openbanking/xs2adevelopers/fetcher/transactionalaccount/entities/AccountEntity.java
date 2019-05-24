package se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.xs2adevelopers.fetcher.transactionalaccount.entities;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import se.tink.backend.agents.rpc.AccountTypes;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.xs2adevelopers.Xs2aDevelopersConstants;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.xs2adevelopers.Xs2aDevelopersConstants.ErrorMessages;
import se.tink.backend.aggregation.annotations.JsonObject;
import se.tink.backend.aggregation.nxgen.core.account.transactional.CheckingAccount;
import se.tink.backend.aggregation.nxgen.core.account.transactional.SavingsAccount;
import se.tink.backend.aggregation.nxgen.core.account.transactional.TransactionalAccount;
import se.tink.libraries.account.AccountIdentifier;
import se.tink.libraries.amount.Amount;

@JsonObject
public class AccountEntity {

    @JsonProperty("_links")
    private LinksEntity links;

    private String accountType;
    private String bban;
    private String bic;
    private String cashAccountType;
    private String currency;
    private String iban;
    private String id;
    private String maskedPan;
    private String msisdn;
    private String name;
    private List<BalanceEntity> balances;

    @JsonIgnore
    public TransactionalAccount toTinkAccount() {
        final AccountTypes type =
                Xs2aDevelopersConstants.ACCOUNT_TYPE_MAPPER
                        .translate(accountType)
                        .orElse(AccountTypes.OTHER);

        switch (type) {
            case CHECKING:
                return toCheckingAccount();
            case SAVINGS:
                return toSavingsAccount();
            case OTHER:
            default:
                throw new IllegalStateException(ErrorMessages.UNKNOWN_ACCOUNT_TYPE);
        }
    }

    @JsonIgnore
    private TransactionalAccount toCheckingAccount() {
        return CheckingAccount.builder()
                .setUniqueIdentifier(bban)
                .setAccountNumber(bban)
                .setBalance(getAvailableBalance())
                .setAlias(name)
                .addAccountIdentifier(AccountIdentifier.create(AccountIdentifier.Type.IBAN, iban))
                .setApiIdentifier(id)
                .build();
    }

    @JsonIgnore
    private TransactionalAccount toSavingsAccount() {
        return SavingsAccount.builder()
                .setUniqueIdentifier(iban)
                .setAccountNumber(bban)
                .setBalance(getAvailableBalance())
                .setAlias(name)
                .addAccountIdentifier(AccountIdentifier.create(AccountIdentifier.Type.IBAN, iban))
                .setApiIdentifier(id)
                .build();
    }

    @JsonIgnore
    private Amount getAvailableBalance() {
        return Optional.ofNullable(balances).orElse(Collections.emptyList()).stream()
                .filter(BalanceEntity::isAvailableBalance)
                .map(BalanceEntity::toAmount)
                .findFirst()
                .orElse(BalanceEntity.DEFAULT);
    }

    public String getId() {
        return id;
    }

    public void setBalance(List<BalanceEntity> balances) {
        this.balances = balances;
    }
}
