package se.tink.backend.main.auth.exceptions.jersey;

import javax.ws.rs.core.Response;
import javax.ws.rs.ext.ExceptionMapper;
import se.tink.backend.main.auth.exceptions.UnauthorizedDeviceException;

public class JerseyUnauthorizedDeviceResponseMapper implements ExceptionMapper<UnauthorizedDeviceException> {
    @Override
    public Response toResponse(UnauthorizedDeviceException exception) {
        return new UnauthorizedDeviceJerseyException(exception.getMfaUrl()).getResponse();
    }
}
