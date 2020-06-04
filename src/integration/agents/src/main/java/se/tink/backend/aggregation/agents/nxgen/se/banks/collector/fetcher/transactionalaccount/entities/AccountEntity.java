package se.tink.backend.aggregation.agents.nxgen.se.banks.collector.fetcher.transactionalaccount.entities;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;
import java.math.BigDecimal;
import java.util.Optional;
import se.tink.backend.agents.rpc.AccountTypes;
import se.tink.backend.aggregation.agents.nxgen.se.banks.collector.CollectorConstants;
import se.tink.backend.aggregation.agents.nxgen.se.banks.collector.fetcher.transactionalaccount.rpc.SavingsResponse;
import se.tink.backend.aggregation.annotations.JsonObject;
import se.tink.backend.aggregation.nxgen.core.account.nxbuilders.modules.balance.BalanceModule;
import se.tink.backend.aggregation.nxgen.core.account.nxbuilders.modules.id.IdModule;
import se.tink.backend.aggregation.nxgen.core.account.transactional.TransactionalAccount;
import se.tink.backend.aggregation.nxgen.core.account.transactional.TransactionalAccountType;
import se.tink.libraries.account.AccountIdentifier;
import se.tink.libraries.amount.ExactCurrencyAmount;

@JsonObject
public class AccountEntity {

    private String accountId;

    private String background;

    private String color;

    private String name;

    private String product;

    private String state;

    @JsonProperty("text")
    private TextEntity textEntity;

    @JsonProperty("title")
    private TitleEntity titleEntity;

    private boolean visible;

    public String getProduct() {
        return product;
    }

    public boolean isVisible() {
        return visible;
    }

    public String getColor() {
        return color;
    }

    public TitleEntity getTitleEntity() {
        return titleEntity;
    }

    public String getAccountId() {
        return accountId;
    }

    public String getBackground() {
        return background;
    }

    public String getName() {
        return name;
    }

    public String getState() {
        return state;
    }

    public TextEntity getTextEntity() {
        return textEntity;
    }

    @JsonIgnore
    public boolean isActive() {
        return state.equalsIgnoreCase("active");
    }

    @JsonIgnore
    private ExactCurrencyAmount getBalance(BigDecimal balance) {
        return new ExactCurrencyAmount(balance, CollectorConstants.Currency.SEK);
    }

    @JsonIgnore
    public Optional<TransactionalAccount> toTinkAccount(SavingsResponse savingsResponse) {
        TransactionalAccountType accountType = getTinkAccountType();
        return TransactionalAccount.nxBuilder()
                .withType(accountType)
                .withInferredAccountFlags()
                .withBalance(BalanceModule.of(getBalance(savingsResponse.getBalance())))
                .withId(
                        IdModule.builder()
                                .withUniqueIdentifier(accountId)
                                .withAccountNumber(savingsResponse.getAccountNumber())
                                .withAccountName(name)
                                .addIdentifier(
                                        AccountIdentifier.create(
                                                AccountIdentifier.Type.SE_PG,
                                                savingsResponse
                                                        .getExternalPaymentEntity()
                                                        .getPlusGiroIdentifier()))
                                .build())
                .addHolderName(name)
                .setApiIdentifier(savingsResponse.getAccountNumber())
                .build();
    }

    private TransactionalAccountType getTinkAccountType() {
        return TransactionalAccountType.from(
                        CollectorConstants.ACCOUNT_TYPE_MAPPER
                                .translate(product)
                                .orElse(AccountTypes.CHECKING))
                .get();
    }

    @JsonObject
    public static class TextEntity {

        @JsonProperty("default")
        private String defaultString;

        private String none;

        public String getDefaultString() {
            return defaultString;
        }

        public String getNone() {
            return none;
        }
    }

    @JsonObject
    public static class TitleEntity {

        @JsonProperty("default")
        private String defaultString;

        private String none;

        public String getDefaultString() {
            return defaultString;
        }

        public String getNone() {
            return none;
        }
    }
}
