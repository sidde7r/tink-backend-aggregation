package se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.nordeabase.fetcher.transactionalaccount.entities;

import static se.tink.backend.agents.rpc.AccountTypes.OTHER;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;
import java.math.BigDecimal;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import org.apache.commons.lang3.StringUtils;
import se.tink.backend.agents.rpc.AccountTypes;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.nordeabase.NordeaBaseConstants;
import se.tink.backend.aggregation.agents.utils.log.LogTag;
import se.tink.backend.aggregation.annotations.JsonObject;
import se.tink.backend.aggregation.log.AggregationLogger;
import se.tink.backend.aggregation.nxgen.core.account.nxbuilders.modules.balance.BalanceModule;
import se.tink.backend.aggregation.nxgen.core.account.nxbuilders.modules.id.IdModule;
import se.tink.backend.aggregation.nxgen.core.account.transactional.TransactionalAccount;
import se.tink.backend.aggregation.nxgen.core.account.transactional.TransactionalAccountType;
import se.tink.libraries.account.AccountIdentifier;
import se.tink.libraries.account.identifiers.NDAPersonalNumberIdentifier;
import se.tink.libraries.amount.ExactCurrencyAmount;

@JsonObject
public class AccountEntity {
    private static final AggregationLogger LOG = new AggregationLogger(AccountEntity.class);

    @JsonProperty("_id")
    private String id;

    @JsonProperty("_links")
    private List<LinkEntity> links;

    @JsonProperty("account_name")
    private String accountName;

    @JsonProperty("account_numbers")
    private List<AccountNumberEntity> accountNumbers;

    @JsonProperty("account_type")
    private String accountType;

    @JsonProperty("available_balance")
    private BigDecimal availableBalance;

    private BankEntity bank;

    @JsonProperty("booked_balance")
    private String bookedBalance;

    private String country;

    @JsonProperty("credit_limit")
    private String creditLimit;

    private String currency;

    @JsonProperty("latest_transaction_booking_date")
    private String latestTransactionBookingDate;

    private String product;

    private String status;

    @JsonProperty("value_dated_balance")
    private String valueDatedBalance;

    public TransactionalAccount toTinkAccount() {
        final AccountTypes type =
                NordeaBaseConstants.ACCOUNT_TYPE_MAPPER.translate(accountType).orElse(OTHER);

        switch (type) {
            case CHECKING:
                return parseAccount(TransactionalAccountType.CHECKING);
            case SAVINGS:
                return parseAccount(TransactionalAccountType.SAVINGS);
            case OTHER:
            default:
                throw new IllegalStateException("Unknown account type.");
        }
    }

    private TransactionalAccount parseAccount(TransactionalAccountType accountType) {
        AccountIdentifier identifier = generalGetAccountIdentifier();

        return TransactionalAccount.nxBuilder()
                .withType(accountType)
                .withBalance(BalanceModule.of(getAvailableBalance()))
                .withId(
                        IdModule.builder()
                                .withUniqueIdentifier(getLast4Bban())
                                .withAccountNumber(identifier.getIdentifier())
                                .withAccountName(product)
                                .addIdentifier(
                                        AccountIdentifier.create(
                                                AccountIdentifier.Type.IBAN, getIban()))
                                .addIdentifier(identifier)
                                .build())
                .putInTemporaryStorage(NordeaBaseConstants.StorageKeys.ACCOUNT_ID, id)
                .setApiIdentifier(id)
                .build();
    }

    @JsonIgnore
    public AccountIdentifier generalGetAccountIdentifier() {
        AccountIdentifier identifier = getAccountIdentifier();
        if (identifier.is(AccountIdentifier.Type.SE_NDA_SSN)) {
            return identifier.to(NDAPersonalNumberIdentifier.class).toSwedishIdentifier();
        } else {
            return identifier;
        }
    }

    @JsonIgnore
    public AccountIdentifier getAccountIdentifier() {
        if (NordeaBaseConstants.TransactionalAccounts.PERSONAL_ACCOUNT.equalsIgnoreCase(product)) {
            AccountIdentifier ssnIdentifier =
                    AccountIdentifier.create(AccountIdentifier.Type.SE_NDA_SSN, getBban());
            if (ssnIdentifier.isValid()) {
                return ssnIdentifier;
            }
        }
        return AccountIdentifier.create(AccountIdentifier.Type.SE, getBban());
    }

    private ExactCurrencyAmount getAvailableBalance() {
        return new ExactCurrencyAmount(availableBalance, currency);
    }

    private String getBban() {
        return Optional.ofNullable(accountNumbers).orElse(Collections.emptyList()).stream()
                .filter(
                        acc ->
                                StringUtils.equalsIgnoreCase(
                                        acc.getType(),
                                        NordeaBaseConstants.AccountTypesResponse.BBAN_SE))
                .findFirst()
                .map(AccountNumberEntity::getValue)
                .orElse(getIban());
    }

    private String getLast4Bban() {
        return getBban().substring(getBban().length() - 4);
    }

    private String getIban() {
        return Optional.ofNullable(accountNumbers).orElse(Collections.emptyList()).stream()
                .filter(
                        acc ->
                                StringUtils.equalsIgnoreCase(
                                        acc.getType(),
                                        NordeaBaseConstants.AccountTypesResponse.IBAN))
                .findFirst()
                .map(AccountNumberEntity::getValue)
                .orElseThrow(
                        () -> {
                            LOG.info(
                                    "Failed to fetch iban "
                                            + LogTag.from("openbanking_base_nordea"));
                            return new IllegalArgumentException();
                        });
    }
}
