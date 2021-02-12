package se.tink.backend.integration.agent_data_availability_tracker.serialization;

import java.util.List;
import java.util.Optional;
import se.tink.backend.agents.rpc.Account;
import se.tink.backend.agents.rpc.Balance;
import se.tink.backend.agents.rpc.BalanceType;
import se.tink.backend.integration.agent_data_availability_tracker.common.serialization.TrackingList;
import se.tink.backend.integration.agent_data_availability_tracker.common.serialization.TrackingMapSerializer;
import se.tink.libraries.account.AccountIdentifier;

public class AccountTrackingSerializer extends TrackingMapSerializer {

    private static final String ACCOUNT_ENTITY_NAME = "Account";
    private final Account account;

    public AccountTrackingSerializer(Account account) {
        super(String.format(ACCOUNT_ENTITY_NAME + "<%s>", String.valueOf(account.getType())));
        this.account = account;
    }

    @Override
    protected TrackingList populateTrackingMap(TrackingList.Builder listBuilder) {

        listBuilder
                .putRedacted("accountNumber", account.getAccountNumber())
                .putRedacted("bankId", account.getBankId())
                .putRedacted("name", account.getName())
                .putRedacted("holderName", account.getHolderName())
                .putRedacted("balance", account.getNullableBalance())
                .putRedacted("availableCredit", account.getNullableAvailableCredit())
                .putListed("type", account.getType());

        // If no identifiers are present we want to record that as a null value.
        if (!account.getIdentifiers().isEmpty()) {
            for (AccountIdentifier id : account.getIdentifiers()) {
                String key = "identifiers." + id.getType().toString();
                listBuilder.putRedacted(key, id.getIdentifier());
            }
        } else {
            listBuilder.putNull("identifiers");
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
