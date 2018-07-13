package se.tink.backend.aggregation.agents.banks.uk.barclays.rpc;

import java.util.LinkedHashMap;
import java.util.Map;
import se.tink.backend.aggregation.agents.banks.uk.barclays.BarclaysConstants;

// We must ask for configuration at least once after we have authenticated or registered in order to
// continue send requests.
public class ConfigurationRequest implements Request {
    public String getCommandId() {
        return BarclaysConstants.COMMAND.GET_CONFIGURATION;
    }

    public Map<String, String> getBody() {
        Map<String, String> m = new LinkedHashMap<>();
        m.put("adobeId", BarclaysConstants.ADOBE_ID);
        return m;
    }
}
