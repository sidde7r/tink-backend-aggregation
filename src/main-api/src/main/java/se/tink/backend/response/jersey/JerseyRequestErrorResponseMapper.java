package se.tink.backend.response.jersey;

import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.ws.rs.ext.ExceptionMapper;
import se.tink.backend.common.utils.LogUtils;
import se.tink.backend.exception.jersey.JerseyRequestException;
import se.tink.libraries.serialization.utils.SerializationUtils;

/**
 * This class works as an interceptor for Jersey exceptions thrown in the code. It translates them to an API response,
 * so that each API request error is always on the same format.
 */
public class JerseyRequestErrorResponseMapper implements ExceptionMapper<JerseyRequestException> {

    private final static LogUtils log = new LogUtils(JerseyRequestErrorResponseMapper.class);

    @Override
    public Response toResponse(JerseyRequestException e) {
        JerseyErrorResponse errorResponse = new JerseyErrorResponse(e.getErrorMessage(), e.getErrorCode());
        errorResponse.setErrorDetails(e.getErrorDetails());

        log.warn("Request error: \"" + e.getLogMessage() + "\", error response: " + SerializationUtils
                .serializeToString(errorResponse));

        return Response.status(e.getStatus())
                .entity(errorResponse)
                .type(MediaType.APPLICATION_JSON)
                .build();
    }
}
