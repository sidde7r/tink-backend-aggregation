package se.tink.backend.aggregation.agents.banks.uk.barclays.rpc.userregistration;

import java.util.LinkedHashMap;
import java.util.Map;
import se.tink.backend.aggregation.agents.banks.uk.barclays.BarclaysConstants;
import se.tink.backend.aggregation.agents.banks.uk.barclays.rpc.Request;

public class SmsChallengeRequest implements Request {
    private String smsVerificationCode;

    public SmsChallengeRequest(String smsVerificationCode) {
        this.smsVerificationCode = smsVerificationCode;
    }

    public String getCommandId() {
        return BarclaysConstants.COMMAND.SMS_CHALLENGE;
    }

    public Map<String, String> getBody() {
        Map<String, String> m = new LinkedHashMap<>();
        m.put("mvc", smsVerificationCode);
        return m;
    }
}
