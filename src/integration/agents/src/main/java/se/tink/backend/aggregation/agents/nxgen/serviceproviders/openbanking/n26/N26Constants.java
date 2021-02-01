package se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.n26;

import java.net.URI;
import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.xs2adevelopers.Xs2aDevelopersConstants.ApiServices;
import se.tink.backend.aggregation.nxgen.http.url.URL;

@NoArgsConstructor(access = AccessLevel.PRIVATE)
public class N26Constants {

    public static class Url {
        public static final String BASE_URL = "https://xs2a.tech26.de";
        public static final String AUTHORIZE = "/oauth2/authorize";
        public static final String TOKEN = "/oauth2/token?role=";
        public static final String CONSENT_FETCH = parseXs2aUrlPath(ApiServices.CONSENT);
        public static final String CONSENT_STATUS = parseXs2aUrlPath(ApiServices.CONSENT_STATUS);
        public static final String CONSENT_DETAILS = parseXs2aUrlPath(ApiServices.CONSENT_DETAILS);
    }

    public static class QueryKeys {
        public static final String REDIRECT_URL = "redirect_uri";
        public static final String CLIENT_ID = "client_id";
        public static final String SCOPE = "scope";
        public static final String CODE_CHALLENGE = "code_challenge";
        public static final String STATE = "state";
        public static final String RESPONSE_TYPE = "response_type";
    }

    public static class BodyParam {
        public static final String CODE_VERIFIER = "code_verifier";
    }

    private static String parseXs2aUrlPath(String xs2aApiServices) {
        return "/v1" + xs2aApiServices.replace("berlingroup", "berlin-group");
    }

    public static String parseXs2aUrl(URL url) {
        URI uri = URI.create(url.get());

        String path = uri.getPath();
        return url.get().replace(path, parseXs2aUrlPath(path));
    }

    public static class QueryValues {
        public static final String CODE = "CODE";
        public static final String AISP_SCOPE = "DEDICATED_AISP";
        public static final String PISP_SCOPE = "DEDICATED_PISP";
    }

    public static class ErrorCodes {
        public static final String PERIOD_INVALID = "PERIOD_INVALID";
    }

    public static class ConsentErrorMessages {
        public static final String INVALID_TOKEN = "invalid_token";
        public static final String LOGIN_TIMEOUT = "Login attempt expired";
    }
}
