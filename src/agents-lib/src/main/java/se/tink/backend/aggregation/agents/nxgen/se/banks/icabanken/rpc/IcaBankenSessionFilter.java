package se.tink.backend.aggregation.agents.nxgen.se.banks.icabanken.rpc;

import javax.ws.rs.core.MultivaluedMap;
import com.google.api.client.repackaged.com.google.common.base.Strings;
import se.tink.backend.aggregation.agents.nxgen.se.banks.icabanken.IcaBankenConstants;
import se.tink.backend.aggregation.nxgen.http.HttpRequest;
import se.tink.backend.aggregation.nxgen.http.HttpResponse;
import se.tink.backend.aggregation.nxgen.http.exceptions.HttpClientException;
import se.tink.backend.aggregation.nxgen.http.exceptions.HttpResponseException;
import se.tink.backend.aggregation.nxgen.http.filter.Filter;
import se.tink.backend.aggregation.nxgen.storage.SessionStorage;

public class IcaBankenSessionFilter extends Filter {
    private final SessionStorage sessionStorage;

    public IcaBankenSessionFilter(SessionStorage sessionStorage) {
        this.sessionStorage = sessionStorage;
    }

    @Override
    public HttpResponse handle(HttpRequest httpRequest) throws HttpClientException, HttpResponseException {
        String sessionId = sessionStorage.get(IcaBankenConstants.IdTags.SESSION_ID_TAG);
        MultivaluedMap<String, Object> headers = httpRequest.getHeaders();

        if (!Strings.isNullOrEmpty(sessionId)) {
            headers.add(IcaBankenConstants.IdTags.SESSION_ID_TAG, sessionId);
        }

        headers.add(IcaBankenConstants.Headers.HEADER_CLIENT_OS,
                IcaBankenConstants.Headers.VALUE_CLIENT_OS);
        headers.add(IcaBankenConstants.Headers.HEADER_CLIENT_OS_VERSION,
                IcaBankenConstants.Headers.VALUE_CLIENT_OS_VERSION);
        headers.add(IcaBankenConstants.Headers.HEADER_CLIENT_HARDWARE,
                IcaBankenConstants.Headers.VALUE_CLIENT_HARDWARE);

        return nextFilter(httpRequest);
    }
}
