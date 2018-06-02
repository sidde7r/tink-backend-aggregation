package se.tink.backend.aggregation.agents.banks.uk.barclays.rpc;

import java.util.Map;

public interface Request {
    // 5 letter command identifier
    String getCommandId();

    // plaintext command body
    Map<String, String> getBody();
}
