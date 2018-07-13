package se.tink.backend.aggregation.agents.nxgen.be.banks.belfius.rpc;

import com.fasterxml.jackson.annotation.JsonProperty;
import se.tink.backend.aggregation.agents.nxgen.be.banks.belfius.BelfiusConstants;
import se.tink.backend.aggregation.annotations.JsonObject;

@JsonObject
public class StartFlowRequest extends RequestEntity {

    private String flowName;
    private String applicationId;
    private String applicationType;
    private Attributes attributes;

    @JsonObject
    public static class Attributes {
        @JsonProperty("bt_AppRelease")
        private String appRelease;
        @JsonProperty("bt_Platform")
        private String platform;
        @JsonProperty("bt_VersionKindApp")
        private String versionKindApp;
        @JsonProperty("bt_TypeDevice")
        private String typeDevice;
        @JsonProperty("bt_Application")
        private String application;

        public String getAppRelease() {
            return appRelease;
        }

        public String getPlatform() {
            return platform;
        }

        public String getVersionKindApp() {
            return versionKindApp;
        }

        public String getTypeDevice() {
            return typeDevice;
        }

        public String getApplication() {
            return application;
        }
    }

    public String getFlowName() {
        return flowName;
    }

    public String getApplicationId() {
        return applicationId;
    }

    public String getApplicationType() {
        return applicationType;
    }

    public Attributes getAttributes() {
        return attributes;
    }

    public static StartFlowRequest create() {
        StartFlowRequest request = new StartFlowRequest();
        request.flowName = BelfiusConstants.Request.START_FLOW_SERVICE_NAME;
        request.applicationId = BelfiusConstants.Request.APPLICATION_ID;
        request.applicationType = BelfiusConstants.Request.APPLICATION_TYPE;
        request.attributes = new StartFlowRequest.Attributes();
        request.attributes.appRelease = BelfiusConstants.Request.APP_RELEASE;
        request.attributes.platform = BelfiusConstants.Request.PLATFORM;
        request.attributes.versionKindApp = BelfiusConstants.Request.VERSION_KIND_APP;
        request.attributes.typeDevice = BelfiusConstants.Request.TYPE_DEVICE;
        request.attributes.application = BelfiusConstants.Request.APPLICATION_ID;
        return request;
    }
}
