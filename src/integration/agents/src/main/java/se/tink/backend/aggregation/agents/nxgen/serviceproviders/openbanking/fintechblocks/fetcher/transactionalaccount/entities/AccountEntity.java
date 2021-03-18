package se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.fintechblocks.fetcher.transactionalaccount.entities;

import com.fasterxml.jackson.annotation.JsonProperty;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.fintechblocks.FintechblocksConstants;
import se.tink.backend.aggregation.annotations.JsonObject;
import se.tink.backend.aggregation.nxgen.core.account.nxbuilders.modules.balance.BalanceModule;
import se.tink.backend.aggregation.nxgen.core.account.nxbuilders.modules.id.IdModule;
import se.tink.backend.aggregation.nxgen.core.account.transactional.TransactionalAccount;
import se.tink.backend.aggregation.nxgen.core.account.transactional.TransactionalAccountType;
import se.tink.libraries.account.AccountIdentifier;
import se.tink.libraries.account.enums.AccountIdentifierType;
import se.tink.libraries.amount.ExactCurrencyAmount;

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

    public Optional<TransactionalAccount> toTinkAccount(List<BalanceEntity> balances) {
        this.balances = balances;
        Optional<TransactionalAccountType> type =
                FintechblocksConstants.ACCOUNT_TYPE_MAPPER.translate(accountSubType);
        return type.flatMap(this::transformAccount);
    }

    private Optional<TransactionalAccount> transformAccount(TransactionalAccountType type) {
        return TransactionalAccount.nxBuilder()
                .withType(type)
                .withPaymentAccountFlag()
                .withBalance(BalanceModule.of(getAvailableBalance()))
                .withId(
                        IdModule.builder()
                                .withUniqueIdentifier(getIdentifier())
                                .withAccountNumber(getBban())
                                .withAccountName(nickname)
                                .addIdentifier(getAccountIdentifier())
                                .build())
                .setApiIdentifier(accountId)
                .build();
    }

    private ExactCurrencyAmount getAvailableBalance() {
        return Optional.ofNullable(balances).orElse(Collections.emptyList()).stream()
                .filter(BalanceEntity::isAvailableBalance)
                .map(BalanceEntity::getAmount)
                .findFirst()
                .orElse(BalanceEntity.DEFAULT);
    }

    private AccountIdentifier getAccountIdentifier() {
        return Optional.ofNullable(getIban())
                .filter(iban -> !iban.equalsIgnoreCase(""))
                .map(iban -> AccountIdentifier.create(AccountIdentifierType.IBAN, iban))
                .orElse(AccountIdentifier.create(AccountIdentifierType.BE, getBban()));
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
