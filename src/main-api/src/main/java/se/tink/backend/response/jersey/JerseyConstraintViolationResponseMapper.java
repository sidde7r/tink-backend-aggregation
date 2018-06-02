package se.tink.backend.response.jersey;

import javax.validation.ConstraintViolation;
import javax.validation.ConstraintViolationException;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.ws.rs.ext.ExceptionMapper;
import se.tink.backend.common.utils.LogUtils;

public class JerseyConstraintViolationResponseMapper implements ExceptionMapper<ConstraintViolationException> {

    private final static LogUtils log = new LogUtils(JerseyConstraintViolationResponseMapper.class);

    @Override
    public Response toResponse(ConstraintViolationException e) {
        ConstraintViolation violation = (ConstraintViolation) e.getConstraintViolations().toArray()[0];

        log.info(String.format("Constraint error for field '%s': '%s'.", violation.getPropertyPath(),
                violation.getMessage()));

        JerseyConstraintViolationErrorResponse errorResponse = new JerseyConstraintViolationErrorResponse(
                violation.getPropertyPath() + " " + violation.getMessage(), violation.getPropertyPath().toString());

        return Response.status(Response.Status.BAD_REQUEST)
                .entity(errorResponse)
                .type(MediaType.APPLICATION_JSON)
                .build();
    }
}
