package se.tink.backend.aggregation.agents.nxgen.at.banks.bankaustria;

import se.tink.backend.aggregation.nxgen.http.URL;

public class BankAustriaConstants {

    public static final String NOT_OK = "ko";
    public static final String OK = "ok";


    public static final class Urls {
        static final String HOST = "https://mobile.bankaustria.at";
        static final URL LOGIN = new URL(HOST + "/IBOA/login.htm");
        static final URL SETTINGS = new URL(HOST + "/IBOA/manageFavouritesAccountController.htm");
        static final URL MOVEMENTS = new URL(HOST + "/IBOA/balanceMovements.htm");
        static final URL UPDATE_PAGE = new URL(HOST + "/IBOA/otmlUpdate.htm");
        static final URL LOGOUT = new URL(HOST + "/IBOA/otml_v2.0/maps/generic/logout_popup.xml");
    }

    public static final class Application {
        static final String PLATFORM = "ios";
        static final String PLATFORM_VERSION = "iPhone_Bank Austria_41_4.7.1";
        static final String OTMLID = "1.07";
    }

    public static final class Device {
        static final String IPHONE7_RESOLUTION = "{750, 1334}";
        static final String IPHONE7_DEVICEID = "Apple_iPhone7,2_iOS_11.1.2";
        static final String IPHONE7_OTML_LAYOUT_INITIAL = "97E5F84071E93B799B749A8FFAD8881B";
        static final String IPHONE7_USERAGENT = "Mozilla/5.0 (iPhone; U; CPU OS 3_2 like Mac OS X; en-us) AppleWebKit/531.21.10 (KHTML, like Gecko) Version/4.0.4 Mobile/7B334b Safari/531.21.10";
    }

    public static final class Header {
        static final String MANIFEST = "X-OTML-MANIFEST";
    }
}
