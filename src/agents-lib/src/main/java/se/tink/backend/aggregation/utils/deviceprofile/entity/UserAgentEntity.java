package se.tink.backend.aggregation.utils.deviceprofile.entity;

import com.google.common.base.Preconditions;
import com.google.common.base.Strings;

public class UserAgentEntity {
    private String mozillaVersion;
    private String systemAndBrowserInfo;
    private String platform;
    private String platformDetails;
    private String extensions;

    private UserAgentEntity(String mozillaVersion, String systemAndBrowserInfo, String platform,
            String platformDetails, String extensions) {
        Preconditions.checkNotNull(mozillaVersion);
        Preconditions.checkNotNull(systemAndBrowserInfo);
        Preconditions.checkNotNull(platform);
        Preconditions.checkNotNull(platformDetails);
        Preconditions.checkNotNull(extensions);

        this.mozillaVersion = mozillaVersion;
        this.systemAndBrowserInfo = systemAndBrowserInfo;
        this.platform = platform;
        this.platformDetails = platformDetails;
        this.extensions = extensions;
    }

    public static UserAgentEntityBuilder create() {
        return new UserAgentEntityBuilder();
    }

    public String getMozillaVersion() {
        return mozillaVersion;
    }

    public String getSystemAndBrowserInfo() {
        return systemAndBrowserInfo;
    }

    public String getPlatform() {
        return platform;
    }

    public String getPlatformDetails() {
        return platformDetails;
    }

    public String getExtensions() {
        return extensions;
    }

    public void setMozillaVersion(String mozillaVersion) {
        this.mozillaVersion = mozillaVersion;
    }

    public void setSystemAndBrowserInfo(String systemAndBrowserInfo) {
        this.systemAndBrowserInfo = systemAndBrowserInfo;
    }

    public void setPlatform(String platform) {
        this.platform = platform;
    }

    public void setPlatformDetails(String platformDetails) {
        this.platformDetails = platformDetails;
    }

    public void setExtensions(String extensions) {
        this.extensions = extensions;
    }

    @Override
    public String toString() {
        StringBuilder builder = new StringBuilder();

        if (!Strings.isNullOrEmpty(mozillaVersion)) {
            builder.append(mozillaVersion).append(" ");
        }

        if (!Strings.isNullOrEmpty(systemAndBrowserInfo)) {
            builder.append(systemAndBrowserInfo).append(" ");
        }

        if (!Strings.isNullOrEmpty(platform)) {
            builder.append(platform).append(" ");
        }

        if (!Strings.isNullOrEmpty(platformDetails)) {
            builder.append(platformDetails).append(" ");
        }

        if (!Strings.isNullOrEmpty(extensions)) {
            builder.append(extensions);
        }

        return builder.toString();
    }

    public static class UserAgentEntityBuilder {
        private String mozillaVersion;
        private String systemAndBrowserInfo;
        private String platform;
        private String platformDetails;
        private String extensions;

        public UserAgentEntityBuilder setMozillaVersion(String mozillaVersion) {
            Preconditions.checkNotNull(mozillaVersion);
            this.mozillaVersion = mozillaVersion;
            return this;
        }

        public UserAgentEntityBuilder setSystemAndBrowserInfo(String systemAndBrowserInfo) {
            Preconditions.checkNotNull(systemAndBrowserInfo);
            this.systemAndBrowserInfo = systemAndBrowserInfo;
            return this;
        }

        public UserAgentEntityBuilder setPlatform(String platform) {
            Preconditions.checkNotNull(platform);
            this.platform = platform;
            return this;
        }

        public UserAgentEntityBuilder setPlatformDetails(String platformDetails) {
            Preconditions.checkNotNull(platformDetails);
            this.platformDetails = platformDetails;
            return this;
        }

        public UserAgentEntityBuilder setExtensions(String extensions) {
            Preconditions.checkNotNull(extensions);
            this.extensions = extensions;
            return this;
        }

        public UserAgentEntity build() {
            return new UserAgentEntity(mozillaVersion, systemAndBrowserInfo, platform, platformDetails, extensions);
        }
    }

}
