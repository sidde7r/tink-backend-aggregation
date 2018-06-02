package se.tink.backend.main.auth.exceptions.jersey;

import javax.ws.rs.WebApplicationException;
import javax.ws.rs.core.Response;
import se.tink.api.headers.TinkHttpHeaders;

@SuppressWarnings("serial")
public class UnsupportedClientJerseyException extends WebApplicationException {
    public UnsupportedClientJerseyException(String message) {
        super(Response
                .status(412)
                .header(TinkHttpHeaders.CLIENT_ERROR_HEADER_NAME, message)
                .build());
    }
}
