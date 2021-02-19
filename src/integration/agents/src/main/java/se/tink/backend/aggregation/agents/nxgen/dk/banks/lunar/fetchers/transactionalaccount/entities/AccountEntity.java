package se.tink.backend.aggregation.agents.nxgen.dk.banks.lunar.fetchers.transactionalaccount.entities;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;
import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;
import se.tink.backend.aggregation.annotations.JsonObject;
import se.tink.backend.aggregation.nxgen.core.account.entity.Holder;
import se.tink.backend.aggregation.nxgen.core.account.nxbuilders.modules.balance.BalanceModule;
import se.tink.backend.aggregation.nxgen.core.account.nxbuilders.modules.id.IdModule;
import se.tink.backend.aggregation.nxgen.core.account.transactional.TransactionalAccount;
import se.tink.backend.aggregation.nxgen.core.account.transactional.TransactionalAccountType;
import se.tink.libraries.account.identifiers.BbanIdentifier;
import se.tink.libraries.account.identifiers.IbanIdentifier;
import se.tink.libraries.amount.ExactCurrencyAmount;

@Slf4j
@JsonObject
public class AccountEntity extends BaseResponseEntity {

    private static final String LUNAR_BANK_NAME = "Lunar";
    private static final String ACTIVE_STATE = "active";

    @JsonProperty("BIC")
    private String bic;

    @JsonProperty("IBAN")
    private String iban;

    private BigDecimal balanceAmount;
    private BigDecimal balanceUsableAmount;
    private String bankName;
    private String bban;
    private String currency;
    private String displayAccountNumber;
    private String displayName;
    @Getter private Boolean isShared;
    private String originGroupId;
    private String state;

    @JsonIgnore
    public Optional<TransactionalAccount> toTransactionalAccount(List<Holder> accountHolders) {
        if (!ACTIVE_STATE.equalsIgnoreCase(state)) {
            // Wiski delete this log after getting more data
            log.info("Lunar account state is different than active. State: {}", state);
        }

        return TransactionalAccount.nxBuilder()
                .withType(TransactionalAccountType.CHECKING)
                .withoutFlags()
                .withBalance(buildBalanceModule())
                .withId(buildIdModule())
                .setApiIdentifier(originGroupId)
                .setBankIdentifier(originGroupId)
                .addHolders(accountHolders)
                .build();
    }

    private IdModule buildIdModule() {
        return IdModule.builder()
                .withUniqueIdentifier(iban)
                .withAccountNumber(displayAccountNumber)
                .withAccountName(displayName)
                .addIdentifier(new IbanIdentifier(iban))
                .addIdentifier(new BbanIdentifier(bban))
                .build();
    }

    private BalanceModule buildBalanceModule() {
        return BalanceModule.builder()
                .withBalance(ExactCurrencyAmount.of(balanceAmount, currency))
                .setAvailableBalance(ExactCurrencyAmount.of(balanceUsableAmount, currency))
                .build();
    }

    @JsonIgnore
    public boolean isLunarAccount() {
        return LUNAR_BANK_NAME.equalsIgnoreCase(bankName);
    }
}
