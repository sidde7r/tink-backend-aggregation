package se.tink.backend.aggregation.agents.nxgen.se.openbanking.business.nordea.configuration;

import java.util.ArrayList;
import java.util.List;

public class NordeaSeBusinessConfiguration {

    public static List<String> getNordeaBusinessScopes() {
        List<String> scopes = new ArrayList<>();
        scopes.add("ACCOUNTS_BALANCES");
        scopes.add("ACCOUNTS_BASIC");
        scopes.add("ACCOUNTS_DETAILS");
        scopes.add("ACCOUNTS_TRANSACTIONS");
        return scopes;
    }
}
