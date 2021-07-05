package se.tink.backend.aggregation.agents.nxgen.ee.openbanking.lhv.fetcher.transactional.rpc;

import java.util.List;
import se.tink.backend.aggregation.annotations.JsonObject;

@JsonObject
public class ConsentAccess {
    private String availableAccounts;
    private List<ConsentAccessAccounts> balances;
    private List<ConsentAccessAccounts> transactions;

    public ConsentAccess(String availableAccounts, List<ConsentAccessAccounts> consentedAccounts) {
        this.availableAccounts = availableAccounts;
        this.balances = consentedAccounts;
        this.transactions = consentedAccounts;
    }
}
