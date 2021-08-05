package se.tink.backend.aggregation.agents.banks.lansforsakringar;

import com.google.common.base.Strings;
import com.sun.jersey.api.client.WebResource.Builder;
import java.net.URI;
import java.util.function.Function;
import javax.ws.rs.core.Cookie;
import javax.ws.rs.core.HttpHeaders;
import javax.ws.rs.core.MediaType;
import se.tink.backend.aggregation.nxgen.http.request.HttpMethod;
import se.tink.libraries.net.client.TinkApacheHttpClient4;

public class LansforsakringarBaseApiClient {
    private final TinkApacheHttpClient4 client;
    private final Function<String, URI> uriFunction;
    private final String deviceId;

    public static final String MOBIL_HOST = "mobil.lansforsakringar.se";
    public static final String API_HOST = "api.lansforsakringar.se";
    public static final String CLIENT_ID = "LFAB-A7MUaibetJF1eD87USxzhh3W";

    private static class HeaderValues {
        public static final String LF_APP_ID = "MOB";
        public static final String LF_APP_VERSION = "6.5.2";
        public static final String DEVICE_INFO = "iPhone;iOS 14.4.2;6.5.2;Portrait";
        public static final String USER_AGENT = "LF/6.5.2 (iPhone; iOS 14.4.2; Scale/2.00)";
        public static final String SECURITY_AUTHORIZATION =
                "Atmosphere atmosphere_app_id=\"" + CLIENT_ID + "\"";
    }

    public LansforsakringarBaseApiClient(
            TinkApacheHttpClient4 client, Function<String, URI> uriFunction, String deviceId) {
        this.client = client;
        this.uriFunction = uriFunction;
        this.deviceId = deviceId;
    }

    public Builder createClientRequest(
            String url, HttpMethod method, String token, String ticket, String userSession) {
        final String host = URI.create(url).getHost();
        Builder request =
                client.resource(uriFunction.apply(url))
                        .accept(MediaType.APPLICATION_JSON)
                        .header(HttpHeaders.USER_AGENT, HeaderValues.USER_AGENT)
                        .header("deviceId", deviceId);

        if (method.equals(HttpMethod.POST)) {
            request =
                    request.header("lf-app-id", HeaderValues.LF_APP_ID)
                            .header("lf-app-version", HeaderValues.LF_APP_VERSION);
        } else {
            request = request.header("deviceInfo", HeaderValues.DEVICE_INFO);
        }

        if (MOBIL_HOST.equalsIgnoreCase(host)) {
            if (ticket != null) {
                request = request.header("Utoken", ticket);
            }
            if (userSession != null) {
                request = request.cookie(new Cookie("USERSESSION", userSession));
            }
        }

        if (API_HOST.equalsIgnoreCase(host)) {
            final String authorization;
            if (Strings.isNullOrEmpty(token)) {
                authorization = HeaderValues.SECURITY_AUTHORIZATION;
            } else {
                authorization = "Bearer " + token;
            }
            request = request.header(HttpHeaders.AUTHORIZATION, authorization);
        }

        return request;
    }
}
