package se.tink.backend.aggregation.agents.banks.uk.barclays.rpc.userregistration;

import java.util.LinkedHashMap;
import java.util.Map;
import se.tink.backend.aggregation.agents.banks.uk.barclays.BarclaysConstants;
import se.tink.backend.aggregation.agents.banks.uk.barclays.rpc.Request;

public class PinSentryChallengeRequest implements Request {
    private String challengeResponse;
    private String lastFourDigits;

    public String getCommandId() {
        return BarclaysConstants.COMMAND.PINSENTRY_CHALLENGE;
    }

    public Map<String, String> getBody() {
        Map<String, String> m = new LinkedHashMap<>();
        m.put("otp", challengeResponse);
        m.put("lastFourDigits", lastFourDigits);
        return m;
    }

    public void setChallengeResponse(String challengeResponse) {
        this.challengeResponse = challengeResponse;
    }

    public void setLastFourDigits(String lastFourDigits) {
        this.lastFourDigits = lastFourDigits;
    }
}
