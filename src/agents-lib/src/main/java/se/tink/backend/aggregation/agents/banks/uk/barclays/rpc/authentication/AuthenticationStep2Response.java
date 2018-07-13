package se.tink.backend.aggregation.agents.banks.uk.barclays.rpc.authentication;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import se.tink.backend.aggregation.agents.banks.uk.barclays.rpc.Response;

@JsonIgnoreProperties(ignoreUnknown = true)
public class AuthenticationStep2Response extends Response {
    /*
    {
        "op_gsn": "441p-01-12",
        "investmentOnly": false,
        "terms": null,
        "lastLoginTime": "",
        "op_status": "00000",
        "registeredServices": [],
        "deviceRegistrationStatus": 0,
        "errorCode": "00000",
        "firstLogin": true,
        "authFailureCount": 0,
        "serverTimeInMilliSeconds": 1500694123294,
        "isFeatureOnHighRisk": false,
        "op_msg": "00000"
    }
     */
    private boolean investmentOnly;
    private String lastLoginTime;
    private boolean firstLogin;
    private int authFailureCount;

    public boolean isInvestmentOnly() {
        return investmentOnly;
    }

    public String getLastLoginTime() {
        return lastLoginTime;
    }

    public boolean isFirstLogin() {
        return firstLogin;
    }

    public int getAuthFailureCount() {
        return authFailureCount;
    }
}
