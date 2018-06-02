package se.tink.backend.connector.response;

import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.ws.rs.ext.ExceptionMapper;
import se.tink.backend.common.utils.LogUtils;
import se.tink.libraries.serialization.utils.SerializationUtils;
import se.tink.backend.connector.exception.RequestException;

public class RequestErrorResponseMapper implements ExceptionMapper<RequestException> {

    private final static LogUtils log = new LogUtils(RequestErrorResponseMapper.class);

    @Override
    public Response toResponse(RequestException e) {
        ErrorResponse errorResponse = new ErrorResponse(e.getErrorMessage());
        errorResponse.setExternalAccountId(e.getExternalAccountId());
        errorResponse.setExternalUserId(e.getExternalUserId());
        errorResponse.setExternalTransactionId(e.getExternalTransactionId());

        log.warn("Request error: \"" + e.getLogMessage() + "\", error response: " + SerializationUtils
                .serializeToString(errorResponse));

        return Response.status(e.getStatus())
                .entity(errorResponse)
                .type(MediaType.APPLICATION_JSON)
                .build();
    }
}
