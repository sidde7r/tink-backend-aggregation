package se.tink.backend.aggregation.agents.nxgen.no.banks.nordeapoc.client;

import static se.tink.backend.aggregation.agents.nxgen.no.banks.nordeapoc.NordeaNoConstants.HeaderKeys;
import static se.tink.backend.aggregation.agents.nxgen.no.banks.nordeapoc.NordeaNoConstants.HeaderValues;

import javax.ws.rs.core.MediaType;
import lombok.AllArgsConstructor;
import se.tink.backend.aggregation.agents.exceptions.errors.SessionError;
import se.tink.backend.aggregation.agents.nxgen.no.banks.nordeapoc.NordeaNoStorage;
import se.tink.backend.aggregation.nxgen.http.client.TinkHttpClient;
import se.tink.backend.aggregation.nxgen.http.filter.filterable.request.RequestBuilder;
import se.tink.backend.aggregation.nxgen.http.url.URL;

@AllArgsConstructor
public class BaseClient {

    private final TinkHttpClient client;
    private final NordeaNoStorage storage;

    RequestBuilder baseAuthorizedRequest(String url) {
        return baseAuthorizedRequest(new URL(url));
    }

    RequestBuilder baseAuthorizedRequest(URL url) {
        String bearerToken =
                storage.retrieveOauthToken()
                        .orElseThrow(SessionError.SESSION_EXPIRED::exception)
                        .toAuthorizeHeader();
        return commonNordeaPrivateRequest(url)
                .header(HeaderKeys.AUTHORIZATION, bearerToken)
                .header(HeaderKeys.X_AUTHORIZATION, bearerToken);
    }

    RequestBuilder commonNordeaPrivateRequest(String url) {
        return commonNordeaPrivateRequest(new URL(url));
    }

    RequestBuilder commonNordeaPrivateRequest(URL url) {
        return request(url)
                .header(HeaderKeys.ACCEPT_ENCODING, HeaderValues.BR_GZIP_ENCODING)
                .header(HeaderKeys.PLATFORM_TYPE, HeaderValues.PLATFORM_TYPE)
                .header(HeaderKeys.ACCEPT_LANGUAGE, HeaderValues.ACCEPT_LANGUAGE)
                .accept(MediaType.APPLICATION_JSON)
                .header(HeaderKeys.HOST, HeaderValues.NORDEA_PRIVATE_HOST)
                .header(HeaderKeys.APP_LANGUAGE, HeaderValues.APP_LANGUAGE)
                .header(HeaderKeys.PLATFORM_VERSION, HeaderValues.PLATFORM_VERSION)
                .header(HeaderKeys.APP_SEGMENT, HeaderValues.HOUSEHOLD_APP_SEGMENT)
                .header(HeaderKeys.DEVICE_ID, storage.retrieveDeviceId())
                .header(HeaderKeys.DEVICE_MODEL, HeaderValues.DEVICE_MODEL)
                .header(HeaderKeys.APP_COUNTRY, HeaderValues.APP_COUNTRY)
                .header(HeaderKeys.APP_VERSION, HeaderValues.APP_VERSION_SHORT)
                .type(MediaType.APPLICATION_FORM_URLENCODED);
    }

    RequestBuilder request(String url) {
        return client.request(url);
    }

    RequestBuilder request(URL url) {
        return client.request(url);
    }
}
