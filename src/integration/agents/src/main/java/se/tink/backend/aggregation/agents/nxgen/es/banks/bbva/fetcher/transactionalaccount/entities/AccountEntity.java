package se.tink.backend.aggregation.agents.nxgen.es.banks.bbva.fetcher.transactionalaccount.entities;

import static se.tink.backend.aggregation.agents.nxgen.es.banks.bbva.BbvaPredicates.IS_TRANSACTIONAL_ACCOUNT;
import static se.tink.backend.aggregation.agents.nxgen.es.banks.bbva.BbvaTypeMappers.ACCOUNT_TYPE_MAPPER;
import static se.tink.libraries.account.AccountIdentifier.Type.IBAN;

import com.fasterxml.jackson.annotation.JsonIgnore;
import io.vavr.collection.List;
import io.vavr.control.Option;
import java.util.Objects;
import se.tink.backend.agents.rpc.AccountTypes;
import se.tink.backend.aggregation.agents.nxgen.es.banks.bbva.BbvaConstants;
import se.tink.backend.aggregation.annotations.JsonObject;
import se.tink.backend.aggregation.log.AggregationLogger;
import se.tink.backend.aggregation.nxgen.core.account.entity.HolderName;
import se.tink.backend.aggregation.nxgen.core.account.transactional.TransactionalAccount;
import se.tink.libraries.account.AccountIdentifier;
import se.tink.libraries.amount.Amount;
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
    private Double availableBalance;
    private List<BalanceEntity> availableBalances;
    private String branch;
    private String accountProductId;
    private String accountProductDescription;
    private double actualBalanceInOriginalCurrency;
    private double actualBalance;

    @JsonIgnore
    public TransactionalAccount toTinkAccount(String holder) {
        final String normalizedIban = iban.replaceAll(" ", "").toUpperCase();
        final HolderName holderName = Option.of(holder).map(HolderName::new).getOrNull();

        return TransactionalAccount.builder(
                        getTinkAccountType(),
                        normalizedIban,
                        new Amount(currency, availableBalance))
                .setAccountNumber(iban)
                .setName(name)
                .setHolderName(holderName)
                .putInTemporaryStorage(BbvaConstants.StorageKeys.ACCOUNT_ID, id)
                .addIdentifier(AccountIdentifier.create(IBAN, normalizedIban))
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
    private AccountTypes getTinkAccountType() {
        return Option.ofOptional(ACCOUNT_TYPE_MAPPER.translate(accountProductId))
                .getOrElse(AccountTypes.OTHER);
    }

    @JsonIgnore
    // until we have seen more than on account we do this
    public boolean isCreditCard() {
        return BbvaConstants.AccountType.CREDIT_CARD.equalsIgnoreCase(subfamilyTypeCode);
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

    public double getAvailableBalance() {
        return availableBalance;
    }

    public String getBranch() {
        return branch;
    }
}
