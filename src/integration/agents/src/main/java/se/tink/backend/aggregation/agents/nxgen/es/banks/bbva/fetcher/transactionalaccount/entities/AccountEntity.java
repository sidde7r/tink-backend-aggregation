package se.tink.backend.aggregation.agents.nxgen.es.banks.bbva.fetcher.transactionalaccount.entities;

import static se.tink.backend.aggregation.agents.nxgen.es.banks.bbva.BbvaPredicates.IS_TRANSACTIONAL_ACCOUNT;
import static se.tink.backend.aggregation.agents.nxgen.es.banks.bbva.BbvaTypeMappers.ACCOUNT_TYPE_MAPPER;

import com.fasterxml.jackson.annotation.JsonIgnore;
import io.vavr.collection.List;
import io.vavr.control.Option;
import java.math.BigDecimal;
import java.util.Locale;
import java.util.Objects;
import java.util.Optional;
import se.tink.backend.aggregation.agents.nxgen.es.banks.bbva.BbvaConstants;
import se.tink.backend.aggregation.agents.nxgen.es.banks.bbva.BbvaConstants.StorageKeys;
import se.tink.backend.aggregation.annotations.JsonObject;
import se.tink.backend.aggregation.log.AggregationLogger;
import se.tink.backend.aggregation.nxgen.core.account.nxbuilders.modules.balance.BalanceModule;
import se.tink.backend.aggregation.nxgen.core.account.nxbuilders.modules.id.IdModule;
import se.tink.backend.aggregation.nxgen.core.account.transactional.TransactionalAccount;
import se.tink.libraries.account.AccountIdentifier;
import se.tink.libraries.account.AccountIdentifier.Type;
import se.tink.libraries.account.identifiers.formatters.DisplayAccountIdentifierFormatter;
import se.tink.libraries.amount.ExactCurrencyAmount;
import se.tink.libraries.serialization.utils.SerializationUtils;

@JsonObject
public class AccountEntity {
    private static final AggregationLogger LOGGER = new AggregationLogger(AccountEntity.class);

    private String id;
    private String name;
    private String productDescription;
    private String iban;
    private String productFamilyCode;
    private String subfamilyCode;
    private String subfamilyTypeCode;
    private String typeCode;
    private String typeDescription;
    private String familyCode;
    private String currency;
    private BigDecimal availableBalance;
    private List<BalanceEntity> availableBalances;
    private String branch;
    private String accountProductId;
    private String accountProductDescription;
    private double actualBalanceInOriginalCurrency;
    private double actualBalance;

    @JsonIgnore
    public Optional<TransactionalAccount> toTinkAccount(String holderName) {
        final AccountIdentifier ibanIdentifier =
                AccountIdentifier.create(Type.IBAN, iban.replaceAll("\\s+", ""));
        final DisplayAccountIdentifierFormatter formatter = new DisplayAccountIdentifierFormatter();
        final String formattedIban = ibanIdentifier.getIdentifier(formatter);

        return TransactionalAccount.nxBuilder()
                .withTypeAndFlagsFrom(ACCOUNT_TYPE_MAPPER, accountProductId)
                .withBalance(BalanceModule.of(getBalance()))
                .withId(
                        IdModule.builder()
                                .withUniqueIdentifier(getUniqueIdentifier())
                                .withAccountNumber(formattedIban)
                                .withAccountName(name)
                                .addIdentifier(ibanIdentifier)
                                .build())
                .addHolderName(holderName)
                .putInTemporaryStorage(StorageKeys.ACCOUNT_ID, id)
                .build();
    }

    @JsonIgnore
    public boolean hasBalance() {
        return Objects.nonNull(availableBalance);
    }

    @JsonIgnore
    public boolean isTransactionalAccount() {
        return Option.ofOptional(ACCOUNT_TYPE_MAPPER.translate(accountProductId))
                .filter(IS_TRANSACTIONAL_ACCOUNT)
                .onEmpty(
                        () ->
                                LOGGER.infoExtraLong(
                                        SerializationUtils.serializeToString(this),
                                        BbvaConstants.LogTags.UNKNOWN_ACCOUNT_TYPE))
                .isDefined();
    }

    @JsonIgnore
    // until we have seen more than on account we do this
    public boolean isCreditCard() {
        return BbvaConstants.AccountType.CREDIT_CARD.equalsIgnoreCase(subfamilyTypeCode);
    }

    @JsonIgnore
    private ExactCurrencyAmount getBalance() {
        return ExactCurrencyAmount.of(availableBalance, currency);
    }

    @JsonIgnore
    private String getUniqueIdentifier() {
        return iban.replaceAll("\\s+", "").toUpperCase(Locale.ENGLISH);
    }

    public List<BalanceEntity> getAvailableBalances() {
        return availableBalances;
    }

    public String getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    public String getIban() {
        return iban;
    }

    public String getCurrency() {
        return currency;
    }

    public BigDecimal getAvailableBalance() {
        return availableBalance;
    }

    public String getBranch() {
        return branch;
    }
}
