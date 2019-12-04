package se.tink.backend.aggregation.agents.nxgen.pt.banks.bancobpi.authentication.request;

import org.json.JSONException;
import org.json.JSONObject;
import se.tink.backend.aggregation.agents.exceptions.LoginException;
import se.tink.backend.aggregation.agents.exceptions.errors.LoginError;
import se.tink.backend.aggregation.agents.nxgen.pt.banks.bancobpi.authentication.MobileChallengeRequestedToken;
import se.tink.libraries.i18n.LocalizableKey;

public class SetupAccessPinResponse extends AuthenticationResponse {

    private static final String SUCCESS_STATUS_CODE = "AMGR_PUB_0001";

    private MobileChallengeRequestedToken mobileChallengeRequestedToken;

    SetupAccessPinResponse(String rawJsonResponse) throws LoginException {
        super(rawJsonResponse);
        fixSuccessFlag();
        if (isSuccess()) {
            mobileChallengeRequestedToken = new MobileChallengeRequestedToken();
            parseJsonResponse();
        }
    }

    private void fixSuccessFlag() {
        success = SUCCESS_STATUS_CODE.equals(getCode());
    }

    private void parseJsonResponse() throws LoginException {
        try {
            JSONObject jsonObject =
                    getResponse().getJSONObject("data").getJSONObject("MobileChallenge");
            mobileChallengeRequestedToken.setId(jsonObject.getString("Id"));
            jsonObject =
                    jsonObject
                            .getJSONObject("MobileChallengeRequestedToken")
                            .getJSONArray("List")
                            .getJSONObject(0);
            mobileChallengeRequestedToken.setUuid(jsonObject.getString("UUID"));
            jsonObject = new JSONObject(jsonObject.getString("TokenDefinition"));
            mobileChallengeRequestedToken.setPhoneNumber(jsonObject.getString("mobilePhoneNumber"));
            mobileChallengeRequestedToken.setProcessedOn(jsonObject.getString("processedOn"));
            mobileChallengeRequestedToken.setReplyWith(jsonObject.getString("replywith"));
        } catch (JSONException ex) {
            throw new LoginException(
                    LoginError.NOT_SUPPORTED,
                    new LocalizableKey("Response JSON doesn't have expected format"));
        }
    }

    public MobileChallengeRequestedToken getMobileChallengeRequestedToken() {
        return mobileChallengeRequestedToken;
    }
}
