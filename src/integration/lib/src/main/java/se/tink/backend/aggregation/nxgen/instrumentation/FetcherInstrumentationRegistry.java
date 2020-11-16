package se.tink.backend.aggregation.nxgen.instrumentation;

import java.util.HashMap;
import java.util.Map;
import lombok.EqualsAndHashCode;
import lombok.RequiredArgsConstructor;
import se.tink.backend.agents.rpc.AccountHolderType;
import se.tink.backend.agents.rpc.AccountTypes;

public class FetcherInstrumentationRegistry {

    private final Map<Key, Integer> accountsSeen;

    public FetcherInstrumentationRegistry() {
        this.accountsSeen = new HashMap<>();
    }

    public int getNumberAccountsSeen(AccountHolderType holderType, AccountTypes type) {
        Key key = new Key(holderType, type);
        if (!accountsSeen.containsKey(key)) {
            return 0;
        }

        return accountsSeen.get(key);
    }

    public void personal(AccountTypes type, int numberAccountsSeen) {
        accountsSeen.put(new Key(AccountHolderType.PERSONAL, type), numberAccountsSeen);
    }

    public void business(AccountTypes type, int numberAccountsSeen) {
        accountsSeen.put(new Key(AccountHolderType.BUSINESS, type), numberAccountsSeen);
    }

    public void corporate(AccountTypes type, int numberAccountsSeen) {
        accountsSeen.put(new Key(AccountHolderType.CORPORATE, type), numberAccountsSeen);
    }

    @EqualsAndHashCode
    @RequiredArgsConstructor
    private static class Key {
        private final AccountHolderType holderType;
        private final AccountTypes accountType;
    }
}
