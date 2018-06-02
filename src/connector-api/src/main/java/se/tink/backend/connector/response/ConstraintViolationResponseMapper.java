package se.tink.backend.connector.response;

import com.google.common.collect.Lists;
import java.util.List;
import javax.validation.ConstraintViolation;
import javax.validation.ConstraintViolationException;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.ws.rs.ext.ExceptionMapper;
import se.tink.backend.common.utils.LogUtils;

public class ConstraintViolationResponseMapper implements ExceptionMapper<ConstraintViolationException> {

    private final static LogUtils log = new LogUtils(ConstraintViolationResponseMapper.class);

    @Override
    public Response toResponse(ConstraintViolationException e) {
        List<ConstraintViolationErrorResponse> errorResponses = Lists.newArrayList();

        for(ConstraintViolation violation : e.getConstraintViolations()) {
            log.info(String.format("Constraint error for field '%s': '%s'.", violation.getPropertyPath(),
                    violation.getMessage()));

            ConstraintViolationErrorResponse errorResponse = new ConstraintViolationErrorResponse(
                    violation.getPropertyPath() + " " + violation.getMessage(), violation.getPropertyPath().toString());
            errorResponses.add(errorResponse);
        }

        ConstraintViolationErrorsResponse errorsResponse = new ConstraintViolationErrorsResponse(errorResponses);


        return Response.status(Response.Status.BAD_REQUEST)
                .entity(errorsResponse)
                .type(MediaType.APPLICATION_JSON)
                .build();
    }
}
