package se.tink.backend.aggregation.agents.nxgen.pt.banks.caixa.fetcher.entities;

import com.amazonaws.util.StringUtils;
import java.math.BigDecimal;
import java.util.Optional;
import se.tink.backend.aggregation.agents.nxgen.pt.banks.caixa.CaixaConstants;
import se.tink.backend.aggregation.annotations.JsonObject;
import se.tink.backend.aggregation.nxgen.core.account.nxbuilders.modules.balance.BalanceModule;
import se.tink.backend.aggregation.nxgen.core.account.nxbuilders.modules.id.IdModule;
import se.tink.backend.aggregation.nxgen.core.account.transactional.TransactionalAccount;
import se.tink.backend.aggregation.nxgen.core.account.transactional.TransactionalAccountType;
import se.tink.libraries.account.AccountIdentifier;
import se.tink.libraries.account.enums.AccountIdentifierType;
import se.tink.libraries.amount.ExactCurrencyAmount;

@JsonObject
public final class AccountEntity {
    private String fullAccountKey;
    private String accountNumber;
    private String iban;
    private String accountType;
    private String currency;
    private String description;
    private BalanceEntity balanceEntity;

    public String getFullAccountKey() {
        return fullAccountKey;
    }

    public void setBalance(BalanceEntity balanceEntity) {
        this.balanceEntity = balanceEntity;
    }

    private TransactionalAccountType getTinkAccountType() {
        return CaixaConstants.ACCOUNT_TYPE_MAPPER
                .translate(accountType)
                .orElse(TransactionalAccountType.CHECKING);
    }

    public Optional<TransactionalAccount> toTinkAccount() {
        BalanceModule balanceModule =
                BalanceModule.builder()
                        .withBalance(
                                ExactCurrencyAmount.of(
                                        BigDecimal.valueOf(
                                                balanceEntity.getAvailableBalance(),
                                                BalanceEntity.SCALE),
                                        currency))
                        .build();

        String id = StringUtils.isNullOrEmpty(iban) ? accountNumber : iban;
        IdModule idModule =
                IdModule.builder()
                        .withUniqueIdentifier(id)
                        .withAccountNumber(id)
                        .withAccountName(description)
                        .addIdentifier(AccountIdentifier.create(AccountIdentifierType.IBAN, id))
                        .build();

        TransactionalAccountType accountType = getTinkAccountType();
        return TransactionalAccount.nxBuilder()
                .withType(accountType)
                .withoutFlags()
                .withBalance(balanceModule)
                .withId(idModule)
                .setApiIdentifier(fullAccountKey)
                .build();
    }
}
