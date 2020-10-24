package se.tink.backend.aggregation.agents.nxgen.dk.banks.jyske;

import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import se.tink.backend.aggregation.nxgen.http.url.URL;

@NoArgsConstructor(access = AccessLevel.PRIVATE)
public final class JyskeConstants {

    public static final String INTEGRATION_NAME = "jyskebank-dk";
    public static final Charset CHARSET = StandardCharsets.UTF_8;

    @NoArgsConstructor(access = AccessLevel.PRIVATE)
    public static final class Url {
        private static final String BANKDATA_BASE_URL = "https://mobil.bankdata.dk/mobilbank";
        private static final String SERVICE_BASE_URL =
                "https://mobile-services.jyskebank.dk/mobilebank.services/rest";

        public static final URL NEMID_INIT = toBankDataUrl("/nemid/init");
        public static final URL NEMID_GET_CHALLANGE = toBankDataUrl("/nemid/get_challange");
        public static final URL NEMID_ENROLL = toBankDataUrl("/nemid/inroll");
        public static final URL NEMID_LOGIN = toBankDataUrl("/nemid/login_with_installid_prop");
        public static final URL GENERATE_CODE = toBankDataUrl("/nemid/generatecode");
        public static final URL CHANGE_TO_KEYCARD = toBankDataUrl("/nemid/changetokeycard");

        public static final URL GET_ACCOUNTS_WITH_EXTERNALS = toBankDataUrl("/accounts");

        public static final URL LOGOUT = toBankDataUrl("/invalidate");
        public static final URL TRANSPORT_KEY = toMobileServiceUrl("/V1-0/transportkey");
        public static final URL MOBILE_SERVICE_LOGIN = toMobileServiceUrl("/V1-0/login");

        private static URL toBankDataUrl(String endpoint) {
            return new URL(BANKDATA_BASE_URL + endpoint);
        }

        private static URL toMobileServiceUrl(String endpoint) {
            return new URL(SERVICE_BASE_URL + endpoint);
        }
    }

    @NoArgsConstructor(access = AccessLevel.PRIVATE)
    public static final class Header {
        public static final String APP_ID_KEY = "x-app-id";
        public static final String APP_ID_VALUE = "ios_phone_jyskemobilbank";
        public static final String APPID_KEY = "x-appid";
        public static final String APPID_VALUE = APP_ID_VALUE;
        public static final String VERSION_KEY = "x-version";
        public static final String VERSION_VALUE = "3.20.5";
        public static final String BANKNO_KEY = "x-bankNo";
        public static final String BANKNO_VALUE = "51";
        public static final String OS_KEY = "x-os";
        public static final String OS_VALUE = "ios";

        public static final String BUILDNO_KEY = "x-buildNo";
        public static final String BUILDNO_VALUE = "1364";

        public static final String PERSONALID_KEY = "x-personalId";
    }

    @NoArgsConstructor(access = AccessLevel.PRIVATE)
    public static final class Crypto {
        public static final String RSA_LABEL = "jbprodver001";
        public static final String CERT_TYPE = "X.509";
        public static final String PRODUCT_NEMID_PUBLIC_KEY =
                "LS0tLS1CRUdJTiBDRVJUSUZJQ0FURS0tLS0tCiBNSUlFVkRDQ0F6eWdBd0lCQWdJSkFPWjFuU0krWi8xVE1BMEdDU3FHU0liM0RRRUJCUVVBTUhreEN6QUpCZ05WQkFZVEFrUkxNUW93Q0FZRFZRUUlFd0VnTVIwd0d3WURWUVEKSEV4UkZjbkpwZEhOdlpTd2dSbkpsWkdWeWFXTnBZVEVSTUE4R0ExVUVDaE1JUW1GdWEyUmhkR0V4RERBS0JnTlZCQXNUQTFOSlN6RWVNQndHQTFVRUF4TVZUVzlpYVd4aVlXNQpySUhabGNqQXdNU0JRY205a01CNFhEVEUyTURReE1URTFNVEUwT0ZvWERUUXhNRFF3TlRFMU1URTBPRm93ZVRFTE1Ba0dBMVVFQmhNQ1JFc3hDakFJQmdOVkJBZ1RBU0F4SFRBCmJCZ05WQkFjVEZFVnljbWwwYzI5bExDQkdjbVZrWlhKcFkybGhNUkV3RHdZRFZRUUtFd2hDWVc1clpHRjBZVEVNTUFvR0ExVUVDeE1EVTBsTE1SNHdIQVlEVlFRREV4Vk5iMkoKcGJHSmhibXNnZG1WeU1EQXhJRkJ5YjJRd2dnRWlNQTBHQ1NxR1NJYjNEUUVCQVFVQUE0SUJEd0F3Z2dFS0FvSUJBUUNmaFNLSUsyMVVwd2FFV2hueURyMFNQWGRoUWM1K3FCOQowWTNpb1FLQnFnbnlTTWxCcVVMdzZBRVozU1hSRWlremRISW5hQmxzY1A5N05LdmluTXYwajJ3b2dKNGxEaWtEdVhpZFdvZk5kY2UyZUdkdWRxTzJDZjB1d2Z4UjFNcmwwUFJVClVSMWdZU3ZVOUhldW9FZExLTXhxYmV3WVliQlBPWVVXZmM1a1RSRk5mejAzU2NGcC9BWm9nTy9GNDlvcjVQbjFVNDF1MjlNaVRudkhEMlBscWZrbExTY3lUQStpVUxaK05IM1cKOS9lR1o2aWdDcFp1YlVTa2ozSlZhYWhKaXA5T0UzdXNHU2V0MGRRVCtja1E5NG1Pdnp6UkMvZS8wQytmYXh0WGJxd0xobkxtMUxwT3dka1NHTGNXL0VnbUlPNXB2MUdmM0lYNQpIbWxMUWxJTjFBZ01CQUFHamdkNHdnZHN3SFFZRFZSME9CQllFRkgvTitYQVRTSSs2TWR0RDVaYkRrLzNldjlQRU1JR3JCZ05WSFNNRWdhTXdnYUNBRkgvTitYQVRTSSs2TWR0CkQ1WmJEay8zZXY5UEVvWDJrZXpCNU1Rc3dDUVlEVlFRR0V3SkVTekVLTUFnR0ExVUVDQk1CSURFZE1Cc0dBMVVFQnhNVVJYSnlhWFJ6YjJVc0lFWnlaV1JsY21samFXRXhFVEEKUEJnTlZCQW9UQ0VKaGJtdGtZWFJoTVF3d0NnWURWUVFMRXdOVFNVc3hIakFjQmdOVkJBTVRGVTF2WW1sc1ltRnVheUIyWlhJd01ERWdVSEp2WklJSkFPWjFuU0krWi8xVE1BdwpHQTFVZEV3UUZNQU1CQWY4d0RRWUpLb1pJaHZjTkFRRUZCUUFEZ2dFQkFEcUdzRjdteGIxQ3hwUmFBT3Y1K3c1K0I0NHJReWtxWlAzaWljMTRBL3pHKzZtSExsZXlzY3dJaVlRCmxXOWl3NDgwc2FjMFp0N0dveGlXWkNoRmErUEQvK2pvTEIvNmxGTysvYngwR1BXeTVTa1BkWklJNzY4OS9abzcwckpkeVpSZDF0cDZxay9PLytXRFB5K00wdzkwSGlOWkcwcTAKTnF4Zm9tWnZaNmZBRnVQVnN1cEpJSnE2RExMMjRGdkJjQ2lKbTV2NkxQampyOS9sS3dYWmo1cEJSOWljcUJ1Y0hoUG1ydjlZUW1Nd0V5SG5EcUsyN05LS2ltMFhueTh3N3hHVwpWNUl0L3VUYkpNTmV0WVhJaVdrVzB3S1prUzcxaGdQU1NrZk1XaE4rdHg1ZVZpVkc3VlZJL0NONEFqNnpyZlBXNm1vRVRydU9meU9RbW1xMWxyMUE9Ci0tLS0tRU5EIENFUlRJRklDQVRFLS0tLS0KCg==";
        public static final String AES_PADDING = "XOXOXOXOXOXOXOXO";
        public static final String MOBILE_SERVICE_PUBLIC_KEY =
                "MIGfMA0GCSqGSIb3DQEBAQUAA4GNADCBiQKBgQCoahwTZuZhJxmSDdOQeTIfNpohuipfQS4ttaypknG7lOX4Y+bQf8xYpQx6C2fs4hR+W6KupEJBuxb2a8ENoEbxnXsbLv9YtDTmNQbNJ14ED+iRXqzgatmMtZs3RxQiK2qJ0InVjTSnNwHS3WAOhplp74TeCiEpoAsAlyzT7VPUEQIDAQAB";
    }

    @NoArgsConstructor(access = AccessLevel.PRIVATE)
    public static final class Storage {
        public static final String TOKEN = "token";
        public static final String INSTALL_ID = "installId";
        public static final String USER_ID = "userId";
        public static final String PIN_CODE = "pin";
        public static final String NEMID_CHALLENGE_ENTITY = "nemidChallengeEntity";
        public static final String NEMID_LOGIN_ENTITY = "nemidLoginResponse";
        public static final String KEYCARD_CHALLENGE_ENTITY = "keycardChallengeEntity";
    }

    @NoArgsConstructor(access = AccessLevel.PRIVATE)
    public static final class ErrorMessages {
        public static final String BANK_UNAVAILABLE_DURING_MIDNIGHT =
                "mobilbanken er lukket hverdage og ";
    }

    @NoArgsConstructor(access = AccessLevel.PRIVATE)
    public static class TimeoutFilter {
        public static final int NUM_TIMEOUT_RETRIES = 3;
        public static final int TIMEOUT_RETRY_SLEEP_MILLISECONDS = 1000;
    }
}
