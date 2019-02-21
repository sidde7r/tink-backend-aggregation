package se.tink.backend.aggregation.agents.nxgen.es.banks.bbva.fetcher.transactionalaccount.entities;

import com.fasterxml.jackson.annotation.JsonIgnore;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import se.tink.backend.agents.rpc.AccountTypes;
import se.tink.backend.aggregation.agents.nxgen.es.banks.bbva.BbvaConstants;
import se.tink.backend.aggregation.annotations.JsonObject;
import se.tink.backend.aggregation.log.AggregationLogger;
import se.tink.backend.aggregation.nxgen.core.account.entity.HolderName;
import se.tink.backend.aggregation.nxgen.core.account.transactional.TransactionalAccount;
import se.tink.libraries.account.AccountIdentifier;
import se.tink.libraries.amount.Amount;
import se.tink.libraries.serialization.utils.SerializationUtils;
import static se.tink.libraries.account.AccountIdentifier.Type.IBAN;

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
    private List<BalancesEntity> availableBalances;
    private String branch;
    private String accountProductId;
    private String accountProductDescription;
    private double actualBalanceInOriginalCurrency;
    private double actualBalance;

    @JsonIgnore
    public TransactionalAccount toTinkAccount(HolderName holderName) {
        String normalizedIban = iban.replaceAll(" ","").toLowerCase();

        return TransactionalAccount.builder(getTinkAccountType(), normalizedIban, new Amount(currency, availableBalance))
                .setAccountNumber(iban)
                .setName(name)
                .setHolderName(holderName)
                .putInTemporaryStorage(BbvaConstants.Storage.ACCOUNT_ID, id)
                .addIdentifier(AccountIdentifier.create(IBAN, normalizedIban))
                .build();
    }

    @JsonIgnore
    public boolean hasBalance() {
        return Objects.nonNull(availableBalance);
    }

    @JsonIgnore
    public boolean isTransactionalAccount() {
        Optional<AccountTypes> accountType = BbvaConstants.ACCOUNT_TYPE_MAPPER.translate(accountProductId);

        if (accountType.isPresent()) {
            return accountType.get().equals(AccountTypes.CHECKING) ||
                    accountType.get().equals(AccountTypes.SAVINGS);
        }

        LOGGER.infoExtraLong(SerializationUtils.serializeToString(this),
                BbvaConstants.Logging.UNKNOWN_ACCOUNT_TYPE);
        return false;
    }

    @JsonIgnore
    private AccountTypes getTinkAccountType() {
        Optional<AccountTypes> accountType = BbvaConstants.ACCOUNT_TYPE_MAPPER.translate(accountProductId);

        return accountType.orElse(AccountTypes.OTHER);
    }

    @JsonIgnore
    // until we have seen more than on account we do this
    public boolean isCreditCard() {
        return BbvaConstants.AccountType.CREDIT_CARD.equalsIgnoreCase(subfamilyTypeCode);
    }

    public List<BalancesEntity> getAvailableBalances() {
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
