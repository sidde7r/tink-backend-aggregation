package se.tink.backend.aggregation.utils.deviceprofile;

import com.google.common.base.Preconditions;
import se.tink.backend.aggregation.utils.deviceprofile.entity.UserAgentEntity;

public class DeviceProfile {

    private final String osVersion;
    private final String screenHeight;
    private final String screenWidth;
    private final UserAgentEntity userAgentEntity;
    private final String phoneModel;
    private final String os;
    private final String modelNumber;
    private final String make;

    private DeviceProfile(String osVersion, String screenHeight, String screenWidth, UserAgentEntity userAgentEntity,
            String phoneModel, String os, String modelNumber, String make) {
        Preconditions.checkNotNull(osVersion);
        Preconditions.checkNotNull(screenHeight);
        Preconditions.checkNotNull(screenWidth);
        Preconditions.checkNotNull(userAgentEntity);
        Preconditions.checkNotNull(phoneModel);
        Preconditions.checkNotNull(os);
        Preconditions.checkNotNull(modelNumber);
        Preconditions.checkNotNull(make);

        this.osVersion = osVersion;
        this.screenHeight = screenHeight;
        this.screenWidth = screenWidth;
        this.userAgentEntity = userAgentEntity;
        this.phoneModel = phoneModel;
        this.os = os;
        this.modelNumber = modelNumber;
        this.make = make;
    }

    public static DeviceProfileBuilder builder() {
        return new DeviceProfileBuilder();
    }

    public String getOsVersion() {
        return osVersion;
    }

    public String getScreenHeight() {
        return screenHeight;
    }

    public String getScreenWidth() {
        return screenWidth;
    }

    public UserAgentEntity getUserAgentEntity() {
        return userAgentEntity;
    }

    public String getPhoneModel() {
        return phoneModel;
    }

    public String getOs() {
        return os;
    }

    public String getModelNumber() {
        return modelNumber;
    }

    public String getMake() {
        return make;
    }

    public static class DeviceProfileBuilder {

        private String osVersion;
        private String screenHeight;
        private String screenWidth;
        private UserAgentEntity userAgentEntity;
        private String phoneModel;
        private String os;
        private String modelNumber;
        private String make;

        public DeviceProfileBuilder() {
        }

        public DeviceProfileBuilder setOS(String os) {
            Preconditions.checkNotNull(os);
            this.os = os;
            return this;
        }

        public DeviceProfileBuilder setOSVersion(String osVersion) {
            Preconditions.checkNotNull(osVersion);
            this.osVersion = osVersion;
            return this;
        }

        public DeviceProfileBuilder setScreenHeight(String screenHeight) {
            Preconditions.checkNotNull(screenHeight);
            this.screenHeight = screenHeight;
            return this;
        }

        public DeviceProfileBuilder setScreenWidth(String screenWidth) {
            Preconditions.checkNotNull(screenWidth);
            this.screenWidth = screenWidth;
            return this;
        }

        public DeviceProfileBuilder setUserAgent(UserAgentEntity userAgent) {
            Preconditions.checkNotNull(userAgent);
            this.userAgentEntity = userAgent;
            return this;
        }

        public DeviceProfileBuilder setPhoneModel(String phoneModel) {
            Preconditions.checkNotNull(phoneModel);
            this.phoneModel = phoneModel;
            return this;
        }

        public DeviceProfileBuilder setModelNumber(String modelNumber) {
            Preconditions.checkNotNull(modelNumber);
            this.modelNumber = modelNumber;
            return this;
        }

        public DeviceProfileBuilder setMake(String make) {
            Preconditions.checkNotNull(make);
            this.make = make;
            return this;
        }

        public DeviceProfile build() {
            return new DeviceProfile(osVersion, screenHeight, screenWidth, userAgentEntity, phoneModel, os, modelNumber,
                    make);
        }

    }
}
