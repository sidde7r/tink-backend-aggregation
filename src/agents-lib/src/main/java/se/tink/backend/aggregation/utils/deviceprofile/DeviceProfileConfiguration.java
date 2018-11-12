package se.tink.backend.aggregation.utils.deviceprofile;

import se.tink.backend.aggregation.utils.deviceprofile.entity.UserAgentEntity;

public class DeviceProfileConfiguration {

    public static DeviceProfile IOS_STABLE = DeviceProfile.builder()
            .setMake("Apple")
            .setOS("iOS")
            .setModelNumber("iPhone10,4")
            .setOSVersion("11.1.2")
            .setPhoneModel("Iphone 8")
            .setScreenHeight("1334")
            .setScreenWidth("750")
            .setUserAgent(UserAgentEntity.create()
                    .setMozillaVersion("Mozilla/5.0")
                    .setSystemAndBrowserInfo("(iPhone; CPU OS 11_1_2 like Mac OS X)")
                    .setPlatform("AppleWebKit/604.1.25")
                    .setPlatformDetails("(KHTML, like Gecko)")
                    .build())
            .build();

    public static DeviceProfile ANDROID_STABLE = DeviceProfile.builder()
            .setMake("Samsung")
            .setOS("Android")
            .setModelNumber("SM-G960F")
            .setOSVersion("8.0")
            .setPhoneModel("Samsung S9")
            .setScreenHeight("2220")
            .setScreenWidth("1080")
            .setUserAgent(UserAgentEntity.create()
                    .setMozillaVersion("Mozilla/5.0")
                    .setSystemAndBrowserInfo("(Linux; Android 8.0.0; SM-G960F Build/R16NW)")
                    .setPlatform("AppleWebKit/537.36")
                    .setPlatformDetails("(KHTML, like Gecko)")
                    .build())
            .build();

}
