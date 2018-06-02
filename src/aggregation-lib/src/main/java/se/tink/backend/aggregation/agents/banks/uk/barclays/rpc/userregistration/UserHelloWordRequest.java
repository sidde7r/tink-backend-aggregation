package se.tink.backend.aggregation.agents.banks.uk.barclays.rpc.userregistration;

import java.util.LinkedHashMap;
import java.util.Map;
import se.tink.backend.aggregation.agents.banks.uk.barclays.BarclaysConstants;
import se.tink.backend.aggregation.agents.banks.uk.barclays.rpc.Request;

public class UserHelloWordRequest implements Request {
    private String memorableWord;

    public UserHelloWordRequest(String memorableWord) {
        this.memorableWord = memorableWord;
    }

    public String getCommandId() {
        return BarclaysConstants.COMMAND.USER_HELLO;
    }

    public Map<String, String> getBody() {
        Map<String, String> m = new LinkedHashMap<>();
        m.put("memorableWord", memorableWord);
        return m;
    }
}
