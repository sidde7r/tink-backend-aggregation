package se.tink.backend.aggregation.agents.banks.crosskey.responses;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.databind.ObjectMapper;
import se.tink.backend.aggregation.agents.banks.crosskey.errors.CrossKeyErrorHandler;

@JsonIgnoreProperties(ignoreUnknown = true)
public class BaseResponse {
    public StatusResponse status;
    private String deviceId;
    private String deviceToken;
    private String tanPosition;
    private String autoStartToken;

    public static <T extends BaseResponse> T deserializeResponse(
            String response, Class<T> model, CrossKeyErrorHandler errorHandler) throws Exception {
        response = BaseResponse.filterResponse(response);

        ObjectMapper mapper = new ObjectMapper();
        T r = mapper.readValue(response, model);
        r.checkForErrors(errorHandler);

        return r;
    }

    public static BaseResponse deserializeResponse(
            String response, CrossKeyErrorHandler errorHandler) throws Exception {
        return BaseResponse.deserializeResponse(response, BaseResponse.class, errorHandler);
    }

    public static String filterResponse(String r) {
        String[] response = r.split("\n");

        return response[1];
    }

    public void setDeviceId(String deviceId) {
        this.deviceId = deviceId;
    }

    public void setNewToken(String token) {
        deviceToken = token;
    }

    public void setDeviceToken(String token) {
        deviceToken = token;
    }

    public void setTanPosition(String tanPosition) {
        this.tanPosition = tanPosition;
    }

    public void setAutoStartToken(String autoStartToken) {
        this.autoStartToken = autoStartToken;
    }

    public String getDeviceId() {
        return deviceId;
    }

    public String getDeviceToken() {
        return deviceToken;
    }

    public String getTanPosition() {
        return tanPosition;
    }

    public String getAutoStartToken() {
        return autoStartToken;
    }

    public void checkForErrors(CrossKeyErrorHandler errorHandler) throws Exception {
        /**
         * status.errors.size() - will throw NullPointerException if: - The errors field doesn't
         * exist in the response - The response is empty
         */
        if (status.errors.size() > 0) {
            String errCode = status.errors.get(0);

            errorHandler.handleError(errCode);
        }
    }
}
