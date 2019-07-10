package se.tink.backend.integration.agentcapabilitytracker.transmitter.serialization;

import se.tink.backend.agents.rpc.Account;
import se.tink.libraries.account.AccountIdentifier;

public class AccountTrackingSerializer extends TrackingMapSerializer {

    public static final String ACCOUNT = "Account";
    private final Account account;

    public AccountTrackingSerializer(Account account) {
        super(String.format(ACCOUNT + "<%s>", String.valueOf(account.getType())));
        this.account = account;
    }

    @Override
    protected TrackingList populateTrackingMap(TrackingList.Builder listBuilder) {

        // TODO: balance, currency, interest, credit
        listBuilder
                .putRedacted("accountNumber", account.getAccountNumber())
                .putRedacted("bankId", account.getBankId())
                .putRedacted("name", account.getName())
                .putRedacted("holderName", account.getHolderName())
                .putListed("type", account.getType());

        account.getIdentifiers().stream()
                .map(AccountIdentifier::getType)
                .map(AccountIdentifier.Type::toString)
                .forEach(value -> listBuilder.putListed("identifiers", value));

        account.getFlags().forEach(value -> listBuilder.putListed("flags", value));

        return listBuilder.build();
    }
}
