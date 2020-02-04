package se.tink.backend.aggregation.agents.nxgen.pt.banks.bancobpi.authentication.request;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import se.tink.backend.aggregation.agents.common.RequestException;

public class AuthenticationResponse {

    protected boolean success;
    private String code;
    private JSONObject response;

    AuthenticationResponse(final String rawJsonResponse) throws RequestException {
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

    private void parseRawJson(final String rawResponse) throws RequestException {
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
            throw new RequestException("Response JSON doesn't have expected format");
        }
    }
}
