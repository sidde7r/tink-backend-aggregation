package se.tink.backend.aggregation.aggregationcontroller.v1.rpc;

import com.fasterxml.jackson.annotation.JsonGetter;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonSetter;
import com.google.common.base.MoreObjects;
import com.google.protobuf.InvalidProtocolBufferException;
import com.google.protobuf.util.JsonFormat;
import se.tink.backend.aggregationcontroller.v1.rpc.enums.CredentialsRequestType;
import se.tink.connectivity.errors.ConnectivityError;
import se.tink.libraries.credentials.rpc.Credentials;
import se.tink.libraries.jersey.utils.SafelyLoggable;

@JsonIgnoreProperties(ignoreUnknown = true)
public class UpdateCredentialsStatusRequest implements SafelyLoggable {
    private Credentials credentials;
    private boolean updateContextTimestamp;
    private String userDeviceId;
    private String userId;
    private boolean isManual;
    private String refreshId;
    private CredentialsRequestType requestType;
    private String operationId;
    private ConnectivityError detailedError;
    private String consentId;

    public boolean isManual() {
        return isManual;
    }

    public void setManual(boolean manual) {
        isManual = manual;
    }

    public Credentials getCredentials() {
        return credentials;
    }

    public String getUserDeviceId() {
        return userDeviceId;
    }

    public String getUserId() {
        return userId;
    }

    public String getRefreshId() {
        return refreshId;
    }

    public boolean isUpdateContextTimestamp() {
        return updateContextTimestamp;
    }

    public void setCredentials(Credentials credentials) {
        this.credentials = credentials;
    }

    public void setUpdateContextTimestamp(boolean updateContextTimestamp) {
        this.updateContextTimestamp = updateContextTimestamp;
    }

    public void setUserDeviceId(String userDeviceId) {
        this.userDeviceId = userDeviceId;
    }

    public void setUserId(String userId) {
        this.userId = userId;
    }

    public void setRefreshId(String refreshId) {
        this.refreshId = refreshId;
    }

    public void setRequestType(CredentialsRequestType serviceCredentialsRequestType) {
        requestType = serviceCredentialsRequestType;
    }

    public CredentialsRequestType getRequestType() {
        return requestType;
    }

    public void setOperationId(String operationId) {
        this.operationId = operationId;
    }

    public String getOperationId() {
        return operationId;
    }

    public ConnectivityError getDetailedError() {
        return detailedError;
    }

    public void setDetailedError(ConnectivityError detailedError) {
        this.detailedError = detailedError;
    }

    @JsonGetter("detailedError")
    public Object getDetailedErrorAsJsonString() throws InvalidProtocolBufferException {
        if (detailedError == null) {
            return null;
        }
        return JsonFormat.printer().omittingInsignificantWhitespace().print(detailedError);
    }

    @JsonSetter("detailedError")
    public void setDetailedErrorFromJsonString(String json) throws InvalidProtocolBufferException {
        if (json == null) {
            detailedError = null;
            return;
        }

        ConnectivityError.Builder builder = ConnectivityError.newBuilder();
        JsonFormat.parser().ignoringUnknownFields().merge(json, builder);
        detailedError = builder.build();
    }

    public void setConsentId(String consentId) {
        this.consentId = consentId;
    }

    public String getConsentId() {
        return consentId;
    }

    @Override
    public String toSafeString() {
        return MoreObjects.toStringHelper(this)
                .add("credentials", credentials)
                .add("updateContextTimestamp", updateContextTimestamp)
                .add("userDeviceId", userDeviceId)
                .add("userId", userId)
                .add("isManual", isManual)
                .add("refreshId", refreshId)
                .add("requestType", requestType)
                .add("operationId", operationId)
                .toString();
    }
}
