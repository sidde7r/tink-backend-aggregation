package se.tink.backend.integration.agent_data_availability_tracker.serialization;

import java.util.List;
import java.util.Objects;
import java.util.Optional;
import se.tink.backend.agents.rpc.Account;
import se.tink.backend.agents.rpc.Balance;
import se.tink.backend.agents.rpc.BalanceType;
import se.tink.backend.integration.agent_data_availability_tracker.common.serialization.TrackingList;
import se.tink.backend.integration.agent_data_availability_tracker.common.serialization.TrackingMapSerializer;
import se.tink.libraries.account.AccountIdentifier;
import se.tink.libraries.account.enums.AccountIdentifierType;

public class AccountTrackingSerializer extends TrackingMapSerializer {

    private static final String ACCOUNT_ENTITY_NAME = "Account";
    private final Account account;
    private final Integer numberOfTransactions;

    public AccountTrackingSerializer(Account account) {
        this(account, null);
    }

    public AccountTrackingSerializer(Account account, Integer numberOfTransactions) {
        super(String.format(ACCOUNT_ENTITY_NAME + "<%s>", account.getType()));
        this.account = account;
        this.numberOfTransactions = numberOfTransactions;
    }

    @Override
    protected TrackingList populateTrackingMap(TrackingList.Builder listBuilder) {

        /*
           We are not tracking the following Account fields (some of them should never be tracked):

           private AccountExclusion accountExclusion;
           private ExactCurrencyAmount exactAvailableCredit;
           private String currencyCode;
           private ExactCurrencyAmount availableBalance;
           private ExactCurrencyAmount creditLimit;
           private Date certainDate;
           private String credentialsId;
           private boolean excluded;
           private boolean favored;
           private String id;
           private double ownership;
           private String payload;
           private String userId;
           private boolean userModifiedExcluded;
           private boolean userModifiedName;
           private boolean userModifiedType;
           private List<TransferDestination> transferDestinations;
           private AccountDetails details;
           private boolean closed;
           private AccountHolder accountHolder;
           private String financialInstitutionId;
        */

        listBuilder
                .putRedacted("accountNumber", account.getAccountNumber())
                .putRedacted("bankId", account.getBankId())
                .putRedacted("name", account.getName())
                .putRedacted("holderName", account.getHolderName())
                .putRedacted("balance", account.getNullableBalance())
                .putRedacted("availableCredit", account.getNullableAvailableCredit())
                .putListed("type", account.getType());

        if (Objects.nonNull(this.numberOfTransactions)) {
            listBuilder.putListed("numberOfTransactions", Integer.toString(numberOfTransactions));
        } else {
            listBuilder.putNull("numberOfTransactions");
        }

        for (AccountIdentifierType type : AccountIdentifierType.values()) {
            Optional<AccountIdentifier> maybeAccountIdentifier =
                    account.getIdentifiers().stream()
                            .filter(identifier -> type.equals(identifier.getType()))
                            .findFirst();
            String key = "identifiers." + type.toString();
            if (maybeAccountIdentifier.isPresent()) {
                listBuilder.putRedacted(key, maybeAccountIdentifier.get().getIdentifier());
            } else {
                listBuilder.putNull(key);
            }
        }

        // Looping over all possible values here to set null on all non-existing
        for (BalanceType type : BalanceType.values()) {
            String key = "balances." + type.toString();
            Optional<Balance> balance = getBalance(account.getBalances(), type);

            if (!balance.isPresent()) {
                listBuilder.putNull(key);
                continue;
            }

            listBuilder.putRedacted(key, balance.get().getAmount().getDoubleValue());
        }

        if (Objects.nonNull(account.getExactBalance())
                && Objects.nonNull(account.getExactBalance().getExactValue())
                && Objects.nonNull(account.getExactBalance().getCurrencyCode())) {
            listBuilder.putRedacted(
                    "exactBalance.value", account.getExactBalance().getDoubleValue());
            listBuilder.putRedacted(
                    "exactBalance.currency", account.getExactBalance().getCurrencyCode());
        } else {
            listBuilder.putNull("exactBalance.value");
            listBuilder.putNull("exactBalance.currency");
        }

        if (Objects.nonNull(account.getAvailableBalance())
                && Objects.nonNull(account.getAvailableBalance().getExactValue())
                && Objects.nonNull(account.getAvailableBalance().getCurrencyCode())) {
            listBuilder.putRedacted(
                    "availableBalance.value", account.getAvailableBalance().getDoubleValue());
            listBuilder.putRedacted(
                    "availableBalance.currency", account.getAvailableBalance().getCurrencyCode());
        } else {
            listBuilder.putNull("availableBalance.value");
            listBuilder.putNull("availableBalance.currency");
        }

        // Accounts without flags are currently valid, so we do not track this as null if empty.
        account.getFlags().forEach(value -> listBuilder.putListed("flags", value));

        return listBuilder.build();
    }

    private Optional<Balance> getBalance(List<Balance> balances, BalanceType type) {
        if (balances == null) {
            return Optional.empty();
        }
        return balances.stream().filter(b -> b.getType() == type).findFirst();
    }
}
