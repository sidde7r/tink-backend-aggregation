package se.tink.backend.aggregation.agents.banks.uk.barclays.rpc.userregistration;

import java.util.LinkedHashMap;
import java.util.Map;
import se.tink.backend.aggregation.agents.banks.uk.barclays.BarclaysConstants;
import se.tink.backend.aggregation.agents.banks.uk.barclays.rpc.Request;

public class SmsInitRequest implements Request {
    public String getCommandId() {
        return BarclaysConstants.COMMAND.SMS_INIT;
    }

    public Map<String, String> getBody() {
        return new LinkedHashMap<>();
    }
}
