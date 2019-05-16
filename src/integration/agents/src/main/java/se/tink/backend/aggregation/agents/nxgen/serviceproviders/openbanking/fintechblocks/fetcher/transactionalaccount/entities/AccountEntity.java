package se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.fintechblocks.fetcher.transactionalaccount.entities;

import com.fasterxml.jackson.annotation.JsonProperty;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import se.tink.backend.agents.rpc.AccountTypes;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.fintechblocks.FintechblocksConstants;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.fintechblocks.FintechblocksConstants.ErrorMessages;
import se.tink.backend.aggregation.annotations.JsonObject;
import se.tink.backend.aggregation.nxgen.core.account.transactional.CheckingAccount;
import se.tink.backend.aggregation.nxgen.core.account.transactional.SavingsAccount;
import se.tink.backend.aggregation.nxgen.core.account.transactional.TransactionalAccount;
import se.tink.libraries.account.AccountIdentifier;
import se.tink.libraries.account.AccountIdentifier.Type;
import se.tink.libraries.amount.Amount;

@JsonObject
public class AccountEntity {

    @JsonProperty("Account")
    private List<AccountInfoEntity> accountInfos;

    @JsonProperty("AccountId")
    private String accountId;

    @JsonProperty("AccountSubType")
    private String accountSubType;

    @JsonProperty("AccountType")
    private String accountType;

    @JsonProperty("Currency")
    private String currency;

    @JsonProperty("Nickname")
    private String nickname;

    @JsonProperty("Servicer")
    private ServicerEntity servicer;

    private List<BalanceEntity> balances;

    public TransactionalAccount toTinkAccount(List<BalanceEntity> balances) {
        this.balances = balances;
        AccountTypes type =
                FintechblocksConstants.ACCOUNT_TYPE_MAPPER
                        .translate(accountSubType)
                        .orElse(AccountTypes.OTHER);

        switch (type) {
            case CHECKING:
                return toCheckingAccount();
            case SAVINGS:
                return toSavingsAccount();
            case OTHER:
            default:
                throw new IllegalStateException(ErrorMessages.INVALID_ACCOUNT_TYPE);
        }
    }

    private TransactionalAccount toSavingsAccount() {
        return SavingsAccount.builder()
            .setUniqueIdentifier(getIdentifier())
            .setAccountNumber(getBban())
            .setBalance(getAvailableBalance())
            .setAlias(nickname)
            .addAccountIdentifier(getAccountIdentifier())
            .setApiIdentifier(accountId)
            .build();
    }

    private TransactionalAccount toCheckingAccount() {
        return CheckingAccount.builder()
                .setUniqueIdentifier(getIdentifier())
                .setAccountNumber(getBban())
                .setBalance(getAvailableBalance())
                .setAlias(nickname)
                .addAccountIdentifier(getAccountIdentifier())
                .setApiIdentifier(accountId)
                .build();
    }

    private Amount getAvailableBalance() {
        return Optional.ofNullable(balances).orElse(Collections.emptyList()).stream()
                .filter(BalanceEntity::isAvailableBalance)
                .map(BalanceEntity::getAmount)
                .findFirst()
                .orElse(BalanceEntity.DEFAULT);
    }

    private AccountIdentifier getAccountIdentifier() {
        return Optional.ofNullable(getIban())
                .filter(iban -> !iban.equalsIgnoreCase(""))
                .map(iban -> AccountIdentifier.create(Type.IBAN, iban))
                .orElse(AccountIdentifier.create(Type.BE, getBban()));
    }

    private String getIdentifier() {
        return Optional.ofNullable(getIban())
                .filter(iban -> !iban.equalsIgnoreCase(""))
                .orElse(getBban());
    }

    private String getIban() {
        return Optional.ofNullable(accountInfos).orElse(Collections.emptyList()).stream()
                .filter(AccountInfoEntity::isIban)
                .map(AccountInfoEntity::getIdentification)
                .findFirst()
                .orElse("");
    }

    private String getBban() {
        return Optional.ofNullable(accountInfos).orElse(Collections.emptyList()).stream()
                .filter(AccountInfoEntity::isBban)
                .map(AccountInfoEntity::getIdentification)
                .findFirst()
                .orElse("");
    }

    public String getAccountId() {
        return accountId;
    }
}
