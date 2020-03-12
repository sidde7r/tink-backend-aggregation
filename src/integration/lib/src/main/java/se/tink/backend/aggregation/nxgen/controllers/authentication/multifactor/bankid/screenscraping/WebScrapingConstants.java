package se.tink.backend.aggregation.nxgen.controllers.authentication.multifactor.bankid.screenscraping;

import se.tink.backend.aggregation.utils.deviceprofile.DeviceProfileConfiguration;

public class WebScrapingConstants {
    public static final String USER_AGENT =
            DeviceProfileConfiguration.IOS_STABLE.getUserAgentEntity().getMozillaVersion()
                    + " "
                    + DeviceProfileConfiguration.IOS_STABLE
                            .getUserAgentEntity()
                            .getSystemAndBrowserInfo()
                    + " "
                    + DeviceProfileConfiguration.IOS_STABLE.getUserAgentEntity().getPlatform()
                    + " "
                    + DeviceProfileConfiguration.IOS_STABLE
                            .getUserAgentEntity()
                            .getPlatformDetails()
                    + " "
                    + DeviceProfileConfiguration.IOS_STABLE.getUserAgentEntity().getExtensions();
}
