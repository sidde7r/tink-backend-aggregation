package se.tink.backend.main.auth.exceptions.jersey;

import javax.ws.rs.WebApplicationException;
import javax.ws.rs.core.Response;
import se.tink.api.headers.TinkHttpHeaders;

@SuppressWarnings("serial")
public class UnauthorizedDeviceJerseyException extends WebApplicationException {
    public UnauthorizedDeviceJerseyException(String mfaUrl) {
        super(Response
                .status(401)
                .header(TinkHttpHeaders.MULTI_FACTOR_URL_HEADER_NAME, mfaUrl)
                .build());
    }
}
