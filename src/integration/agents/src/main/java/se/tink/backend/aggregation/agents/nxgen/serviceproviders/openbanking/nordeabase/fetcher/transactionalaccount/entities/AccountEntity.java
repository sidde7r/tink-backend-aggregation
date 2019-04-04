package se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.nordeabase.fetcher.transactionalaccount.entities;

import com.fasterxml.jackson.annotation.JsonProperty;
import org.apache.commons.lang.StringUtils;
import se.tink.backend.agents.rpc.AccountTypes;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.nordeabase.NordeaBaseConstants;
import se.tink.backend.aggregation.annotations.JsonObject;
import se.tink.backend.aggregation.nxgen.core.account.transactional.CheckingAccount;
import se.tink.backend.aggregation.nxgen.core.account.transactional.SavingsAccount;
import se.tink.backend.aggregation.nxgen.core.account.transactional.TransactionalAccount;
import se.tink.libraries.account.AccountIdentifier;
import se.tink.libraries.amount.Amount;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@JsonObject
public class AccountEntity {

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
        Optional<AccountTypes> type =
                NordeaBaseConstants.ACCOUNT_TYPE_MAPPER.translate(accountType);
        if (!type.isPresent()) {
            throw new IllegalStateException("Unknown account type.");
        }

        if (type.get().equals(AccountTypes.CHECKING)) {
            return parseCheckingAccount();
        }
        if (type.get().equals(AccountTypes.SAVINGS)) {
            return parseSavingsAccount();
        }
        throw new IllegalStateException("Unknown account type.");
    }

    private TransactionalAccount parseSavingsAccount() {
        return SavingsAccount.builder()
                .setUniqueIdentifier(getIban())
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
                .setUniqueIdentifier(getIban())
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
        Optional<AccountNumberEntity> accountNumberEntity =
                accountNumbers.stream()
                        .filter(acc -> StringUtils.containsIgnoreCase(acc.getType(), "bban"))
                        .findFirst();
        return accountNumberEntity.isPresent() ? accountNumberEntity.get().getValue() : getIban();
    }

    private String getIban() {
        Optional<AccountNumberEntity> accountNumberEntity =
                accountNumbers.stream()
                        .filter(acc -> StringUtils.containsIgnoreCase(acc.getType(), "iban"))
                        .findFirst();
        return accountNumberEntity.isPresent() ? accountNumberEntity.get().getValue() : "";
    }
}
