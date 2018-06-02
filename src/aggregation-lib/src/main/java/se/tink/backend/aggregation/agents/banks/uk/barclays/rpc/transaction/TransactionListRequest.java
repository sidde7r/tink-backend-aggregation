package se.tink.backend.aggregation.agents.banks.uk.barclays.rpc.transaction;

import java.util.LinkedHashMap;
import java.util.Map;
import se.tink.backend.aggregation.agents.banks.uk.barclays.BarclaysConstants;
import se.tink.backend.aggregation.agents.banks.uk.barclays.rpc.Request;

public class TransactionListRequest implements Request {

    private String accountIdentifier;

    public TransactionListRequest(String accountIdentifier) {
        this.accountIdentifier = accountIdentifier;
    }

    @Override
    public String getCommandId() {
        return BarclaysConstants.COMMAND.LIST_TRANSACTIONS;
    }

    @Override
    public Map<String, String> getBody() {
        Map<String, String> m = new LinkedHashMap<>();
        m.put("productIdentifier", accountIdentifier);
        return m;
    }
}
