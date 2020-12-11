package se.tink.backend.integration.agent_data_availability_tracker.serialization;

import se.tink.backend.agents.rpc.Account;
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
            account.getIdentifiers().stream()
                    .map(AccountIdentifier::getType)
                    .map(AccountIdentifier.Type::toString)
                    .forEach(value -> listBuilder.putListed("identifiers", value));
        } else {
            listBuilder.putNull("identifiers");
        }

        // Accounts without flags are currently valid, so we do not track this as null if empty.
        account.getFlags().forEach(value -> listBuilder.putListed("flags", value));

        return listBuilder.build();
    }
}
