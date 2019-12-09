package se.tink.backend.aggregation.agents.nxgen.pt.banks.bancobpi.authentication.request;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import se.tink.backend.aggregation.agents.exceptions.LoginException;
import se.tink.backend.aggregation.agents.exceptions.errors.LoginError;
import se.tink.libraries.i18n.LocalizableKey;

public class AuthenticationResponse {

    protected boolean success;
    private String code;
    private JSONObject response;

    AuthenticationResponse(final String rawJsonResponse) throws LoginException {
        parseRawJson(rawJsonResponse);
    }

    public boolean isSuccess() {
        return success;
    }

    public String getCode() {
        return code;
    }

    protected JSONObject getResponse() {
        return response;
    }

    private void parseRawJson(final String rawResponse) throws LoginException {
        try {
            response = new JSONObject(rawResponse);
            JSONArray statusArray =
                    response.getJSONObject("data")
                            .getJSONObject("TransactionStatus")
                            .getJSONObject("TransactionStatus")
                            .getJSONObject("AuthStatusReason")
                            .getJSONArray("List");
            if (statusArray.length() == 0) {
                success = true;
            } else {
                code = statusArray.getJSONObject(0).getString("Code");
            }
        } catch (JSONException e) {
            throw new LoginException(
                    LoginError.NOT_SUPPORTED,
                    new LocalizableKey("Response JSON doesn't have expected format"));
        }
    }
}
