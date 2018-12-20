package se.tink.backend.aggregation.agents.nxgen.se.brokers.avanza.fetcher.investment.entities;

import se.tink.libraries.pair.Pair;

public class SessionAccountPair extends Pair<String, String> {
    public SessionAccountPair(String authSession, String accountId) {
        super(authSession, accountId);
    }

    public String getAuthSession() {
        return this.first;
    }

    public String getAccountId() {
        return this.second;
    }
}
