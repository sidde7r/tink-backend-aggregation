package se.tink.backend.aggregation.agents.nxgen.be.banks.belfius.rpc;

import com.fasterxml.jackson.annotation.JsonProperty;
import se.tink.backend.aggregation.agents.nxgen.be.banks.belfius.BelfiusConstants;
import se.tink.backend.aggregation.annotations.JsonObject;

@JsonObject
public class OpenSessionRequest extends RequestEntity {

    private String locale;
    private String applicationType;
    private Attributes attributes;

    @JsonObject
    public static class Attributes {
        @JsonProperty(BelfiusConstants.Request.Session.Attribute.APPLICATION)
        private String application;

        @JsonProperty(BelfiusConstants.Request.Session.Attribute.VERSION_KIND_APP)
        private String versionKindApp;

        @JsonProperty(BelfiusConstants.Request.Session.Attribute.PLATFORM)
        private String platform;

        @JsonProperty(BelfiusConstants.Request.Session.Attribute.TYPE_DEVICE)
        private String typeDevice;

        @JsonProperty(BelfiusConstants.Request.Session.Attribute.APP_RELEASE)
        private String appRelease;

        public String getApplication() {
            return this.application;
        }

        public String getVersionKindApp() {
            return this.versionKindApp;
        }

        public String getPlatform() {
            return this.platform;
        }

        public String getTypeDevice() {
            return this.typeDevice;
        }

        public String getAppRelease() {
            return this.appRelease;
        }
    }

    public String getLocale() {
        return this.locale;
    }

    public String getApplicationType() {
        return this.applicationType;
    }

    public Attributes getAttributes() {
        return this.attributes;
    }

    public static OpenSessionRequest create(String locale) {
        OpenSessionRequest request = new OpenSessionRequest();
        request.applicationType = BelfiusConstants.Request.APPLICATION_TYPE;
        request.locale = locale;
        request.attributes = new OpenSessionRequest.Attributes();
        request.attributes.application = BelfiusConstants.Request.APPLICATION_ID;
        request.attributes.appRelease = BelfiusConstants.Request.APP_RELEASE;
        request.attributes.platform = BelfiusConstants.Request.PLATFORM;
        request.attributes.typeDevice = BelfiusConstants.Request.TYPE_DEVICE;
        request.attributes.versionKindApp = BelfiusConstants.Request.VERSION_KIND_APP;
        return request;
    }
}
