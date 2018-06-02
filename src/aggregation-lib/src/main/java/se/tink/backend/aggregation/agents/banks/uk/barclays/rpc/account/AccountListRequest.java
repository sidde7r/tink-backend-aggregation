package se.tink.backend.aggregation.agents.banks.uk.barclays.rpc.account;

import java.util.LinkedHashMap;
import java.util.Map;
import se.tink.backend.aggregation.agents.banks.uk.barclays.BarclaysConstants;
import se.tink.backend.aggregation.agents.banks.uk.barclays.rpc.Request;

public class AccountListRequest implements Request {
    public String getCommandId() {
        return BarclaysConstants.COMMAND.LIST_ACCOUNTS;
    }

    public Map<String, String> getBody() {
        return new LinkedHashMap<>();
    }
}
