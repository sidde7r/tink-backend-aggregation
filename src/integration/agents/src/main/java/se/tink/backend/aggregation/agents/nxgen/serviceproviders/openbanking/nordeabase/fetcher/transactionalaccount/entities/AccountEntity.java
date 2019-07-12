package se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.nordeabase.fetcher.transactionalaccount.entities;

import static se.tink.backend.agents.rpc.AccountTypes.OTHER;

import com.fasterxml.jackson.annotation.JsonProperty;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import org.apache.commons.lang.StringUtils;
import se.tink.backend.agents.rpc.AccountTypes;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.nordeabase.NordeaBaseConstants;
import se.tink.backend.aggregation.agents.utils.log.LogTag;
import se.tink.backend.aggregation.annotations.JsonObject;
import se.tink.backend.aggregation.log.AggregationLogger;
import se.tink.backend.aggregation.nxgen.core.account.transactional.CheckingAccount;
import se.tink.backend.aggregation.nxgen.core.account.transactional.SavingsAccount;
import se.tink.backend.aggregation.nxgen.core.account.transactional.TransactionalAccount;
import se.tink.libraries.account.AccountIdentifier;
import se.tink.libraries.amount.Amount;

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
    private double availableBalance;

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
                return parseCheckingAccount();
            case SAVINGS:
                return parseSavingsAccount();
            case OTHER:
            default:
                throw new IllegalStateException("Unknown account type.");
        }
    }

    private TransactionalAccount parseSavingsAccount() {
        return SavingsAccount.builder()
                .setUniqueIdentifier(getUniqueId())
                .setAccountNumber(getBban())
                .setBalance(getAvailableBalance())
                .setAlias(getBban())
                .addAccountIdentifier(
                        AccountIdentifier.create(AccountIdentifier.Type.IBAN, getIban()))
                .addHolderName(accountName)
                .setProductName(product)
                .setApiIdentifier(id)
                .putInTemporaryStorage(NordeaBaseConstants.StorageKeys.ACCOUNT_ID, id)
                .build();
    }

    private TransactionalAccount parseCheckingAccount() {
        return CheckingAccount.builder()
                .setUniqueIdentifier(getUniqueId())
                .setAccountNumber(getBban())
                .setBalance(getAvailableBalance())
                .setAlias(getBban())
                .addAccountIdentifier(
                        AccountIdentifier.create(AccountIdentifier.Type.IBAN, getIban()))
                .addHolderName(accountName)
                .setProductName(product)
                .setApiIdentifier(id)
                .putInTemporaryStorage(NordeaBaseConstants.StorageKeys.ACCOUNT_ID, id)
                .build();
    }

    private Amount getAvailableBalance() {
        return new Amount(currency, availableBalance);
    }

    private String getBban() {
        return Optional.ofNullable(accountNumbers).orElse(Collections.emptyList()).stream()
                .filter(acc -> StringUtils.equalsIgnoreCase(acc.getType(), "bban"))
                .findFirst()
                .map(AccountNumberEntity::getValue)
                .orElse(getIban());
    }

    private String getIban() {
        return Optional.ofNullable(accountNumbers).orElse(Collections.emptyList()).stream()
                .filter(acc -> StringUtils.equalsIgnoreCase(acc.getType(), "iban"))
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

    private String getUniqueId() {
        String bban = getBban();

        return !country.equalsIgnoreCase("SE")
                ? getIban()
                : "************" + bban.substring(bban.length() - 4);
    }
}
