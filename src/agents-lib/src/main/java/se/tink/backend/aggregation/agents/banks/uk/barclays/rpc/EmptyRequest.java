package se.tink.backend.aggregation.agents.banks.uk.barclays.rpc;

import java.util.LinkedHashMap;
import java.util.Map;

// for debug purposes
public class EmptyRequest implements Request {
    private String commandId;
    public EmptyRequest(String commandId) {
        this.commandId = commandId;
    }
    public String getCommandId() {
        return commandId;
    }

    public Map<String, String> getBody() {
        return new LinkedHashMap<>();
    }
}
