package se.tink.backend.aggregation.agents.nxgen.pt.banks.bancobpi.authentication.request;

import org.json.JSONException;
import org.json.JSONObject;
import se.tink.backend.aggregation.agents.common.RequestException;
import se.tink.backend.aggregation.agents.nxgen.pt.banks.bancobpi.entity.MobileChallengeRequestedToken;

public class SetupAccessPinResponse extends AuthenticationResponse {

    private static final String SUCCESS_STATUS_CODE = "AMGR_PUB_0001";

    private MobileChallengeRequestedToken mobileChallengeRequestedToken;

    SetupAccessPinResponse(String rawJsonResponse) throws RequestException {
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

    private void parseJsonResponse() throws RequestException {
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
            throw new RequestException("Response JSON doesn't have expected format");
        }
    }

    public MobileChallengeRequestedToken getMobileChallengeRequestedToken() {
        return mobileChallengeRequestedToken;
    }
}
