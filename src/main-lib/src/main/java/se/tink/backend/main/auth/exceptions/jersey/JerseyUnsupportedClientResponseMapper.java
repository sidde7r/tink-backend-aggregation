package se.tink.backend.main.auth.exceptions.jersey;

import javax.ws.rs.core.Response;
import javax.ws.rs.ext.ExceptionMapper;
import se.tink.backend.main.auth.exceptions.UnsupportedClientException;

public class JerseyUnsupportedClientResponseMapper implements ExceptionMapper<UnsupportedClientException> {
    @Override
    public Response toResponse(UnsupportedClientException exception) {
        return new UnsupportedClientJerseyException(exception.getMessage()).getResponse();
    }
}
