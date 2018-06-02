package se.tink.backend.main.transports;

import com.fasterxml.jackson.core.JsonProcessingException;

import java.util.Optional;
import java.util.Set;
import com.google.inject.Inject;
import javax.ws.rs.Path;
import javax.ws.rs.WebApplicationException;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.HttpHeaders;
import javax.ws.rs.core.Response;
import org.springframework.dao.CannotAcquireLockException;
import se.tink.backend.api.ApplicationService;
import se.tink.backend.common.application.ApplicationAlreadySignedException;
import se.tink.backend.common.application.ApplicationCannotBeDeletedException;
import se.tink.backend.common.application.ApplicationNotCompleteException;
import se.tink.backend.common.application.ApplicationNotFoundException;
import se.tink.backend.common.application.ApplicationNotValidException;
import se.tink.backend.common.application.ApplicationNotModifiableException;
import se.tink.backend.common.application.ApplicationSigningNotInvokableException;
import se.tink.backend.common.exceptions.FeatureFlagNotEnabledException;
import se.tink.backend.common.resources.RequestHeaderUtils;
import se.tink.backend.common.utils.LogUtils;
import se.tink.backend.core.Application;
import se.tink.backend.auth.AuthenticatedUser;
import se.tink.backend.core.ApplicationForm;
import se.tink.backend.core.ApplicationSummary;
import se.tink.backend.core.application.EligibleApplicationTypesResponse;
import se.tink.backend.core.signableoperation.SignableOperation;
import se.tink.backend.main.controllers.ApplicationServiceController;
import se.tink.backend.rpc.application.ApplicationListCommand;
import se.tink.backend.rpc.ApplicationSummaryListResponse;
import se.tink.backend.rpc.application.CreateApplicationCommand;
import se.tink.backend.rpc.application.DeleteApplicationCommand;
import se.tink.backend.rpc.application.GetApplicationCommand;
import se.tink.backend.rpc.application.GetEligibleApplicationTypesCommand;
import se.tink.backend.rpc.application.GetSummaryCommand;
import se.tink.backend.rpc.application.SubmitApplicationCommand;
import se.tink.backend.rpc.application.SubmitApplicationFormCommand;
import se.tink.libraries.application.ApplicationType;
import se.tink.libraries.http.utils.HttpResponseHelper;

@Path("/api/v1/applications")
public class ApplicationServiceJerseyTransport implements ApplicationService {
    @Context
    private HttpHeaders headers;

    private HttpResponseHelper httpResponseHelper;
    private static final LogUtils log = new LogUtils(ApplicationServiceJerseyTransport.class);

    private final ApplicationServiceController applicationServiceController;

    @Inject
    public ApplicationServiceJerseyTransport(
            ApplicationServiceController applicationServiceController) {
        this.applicationServiceController = applicationServiceController;
        this.httpResponseHelper = new HttpResponseHelper(log);

    }

    @Override
    public EligibleApplicationTypesResponse getEligibleApplicationTypes(AuthenticatedUser authenticatedUser) {
        GetEligibleApplicationTypesCommand command = new GetEligibleApplicationTypesCommand(
                authenticatedUser.getUser().getId());

        Set<ApplicationType> eligibleApplicationTypes = applicationServiceController
                .getEligibleApplicationTypes(command);

        EligibleApplicationTypesResponse response = new EligibleApplicationTypesResponse();
        response.setEligibleApplicationTypes(eligibleApplicationTypes);

        return response;
    }

    @Override
    public ApplicationSummary getSummary(AuthenticatedUser authenticatedUser, String id) {
        GetSummaryCommand command = new GetSummaryCommand(id, authenticatedUser.getUser(), getUserAgent());
        try {
            return applicationServiceController.getSummary(command);
        } catch (ApplicationNotFoundException e) {
            httpResponseHelper.error(Response.Status.NOT_FOUND, "Application was not found");
        }
        return null;
    }

    @Override
    public Application getApplication(AuthenticatedUser authenticatedUser, String id) {
        GetApplicationCommand command = new GetApplicationCommand(id, authenticatedUser.getUser(), getUserAgent());

        try {
            return applicationServiceController.getApplication(command);
        } catch (ApplicationNotFoundException e) {
            httpResponseHelper.error(Response.Status.NOT_FOUND, "Application was not found");
        } catch (ApplicationNotValidException e) {
            log.error(authenticatedUser.getUser().getId(), "Failed to get application", e);
            httpResponseHelper.error(Response.Status.BAD_REQUEST);
        }
        return null;
    }

    @Override
    public ApplicationSummaryListResponse list(AuthenticatedUser authenticatedUser) {
        ApplicationListCommand command = new ApplicationListCommand(authenticatedUser.getUser(), getUserAgent());

        try {
            return applicationServiceController.list(command);
        } catch (ApplicationNotValidException e) {
            throw new IllegalStateException("Could not process application");
        }
    }

    @Override
    public Application submitForm(AuthenticatedUser authenticatedUser, String id, ApplicationForm form) {
        if (form == null) {
            httpResponseHelper.error(Response.Status.BAD_REQUEST, "Form not supplied");
        }

        if (form.getId() == null) {
            httpResponseHelper.error(Response.Status.BAD_REQUEST, "Form id not supplied");
        }

        SubmitApplicationFormCommand command = new SubmitApplicationFormCommand(id, authenticatedUser.getUser(),
                getUserAgent());

        try {
            return applicationServiceController.submitForm(command, form);
        } catch (ApplicationNotFoundException e) {
            httpResponseHelper.error(Response.Status.NOT_FOUND, "Application was not found");
        } catch (ApplicationNotModifiableException e) {
            httpResponseHelper.error(Response.Status.FORBIDDEN, "Signed applications can't be modified.");
        } catch (ApplicationNotValidException e) {
            log.error(authenticatedUser.getUser().getId(), "Failed to submit form", e);
            httpResponseHelper.error(Response.Status.BAD_REQUEST);
        } catch (CannotAcquireLockException e) {
            httpResponseHelper.error(Response.Status.INTERNAL_SERVER_ERROR, "Unable to acquire lock.", e);
        } catch (Exception e) {
            log.error(authenticatedUser.getUser().getId(), "Couldn't submit application form", e);
            httpResponseHelper.error(Response.Status.INTERNAL_SERVER_ERROR);
        }
        return null;
    }

    @Override
    public SignableOperation submit(AuthenticatedUser authenticatedUser, String id) {
        SubmitApplicationCommand command = new SubmitApplicationCommand(id, authenticatedUser.getUser(), getUserAgent(),
                RequestHeaderUtils.getRemoteIp(headers));

        try {
            return applicationServiceController.submit(command);
        } catch (CannotAcquireLockException e) {
            httpResponseHelper.error(Response.Status.INTERNAL_SERVER_ERROR, "Unable to acquire lock.");
        } catch (ApplicationNotFoundException e) {
            httpResponseHelper.error(Response.Status.NOT_FOUND, "Application was not found");
        } catch (ApplicationAlreadySignedException e) {
            httpResponseHelper.error(Response.Status.PRECONDITION_FAILED,
                    "Application has already been signed.");
        } catch (ApplicationNotCompleteException e) {
            httpResponseHelper.error(Response.Status.PRECONDITION_FAILED,
                    "Application hasn't been completed yet.");
        } catch (ApplicationSigningNotInvokableException e) {
            httpResponseHelper.error(Response.Status.INTERNAL_SERVER_ERROR, "Unable to invoke application signing.");
        } catch (JsonProcessingException e) {
            log.error(authenticatedUser.getUser().getId(), "Couldn't serialize compiled application");
            throw new WebApplicationException(Response.Status.INTERNAL_SERVER_ERROR);
        } catch (Exception e) {
            log.error(authenticatedUser.getUser().getId(), "Couldn't submit application", e);
            throw new WebApplicationException(Response.Status.INTERNAL_SERVER_ERROR);
        }
        return null;
    }

    @Override
    public Application createApplication(AuthenticatedUser authenticatedUser, String type) {
        CreateApplicationCommand command = new CreateApplicationCommand(authenticatedUser.getUser(), getUserAgent(),
                ApplicationType.fromScheme(type));

        try {
            return applicationServiceController.createApplication(command);
        } catch (FeatureFlagNotEnabledException e) {
            httpResponseHelper.error(Response.Status.FORBIDDEN);
        } catch (ApplicationNotValidException e) {
            log.error(authenticatedUser.getUser().getId(), "Failed to create application", e);
            httpResponseHelper.error(Response.Status.BAD_REQUEST);
        }
        return null;
    }

    @Override
    public void delete(AuthenticatedUser authenticatedUser, String id) {
        DeleteApplicationCommand command = new DeleteApplicationCommand(authenticatedUser.getUser().getId(), id);

        try {
            applicationServiceController.delete(command);
        } catch (ApplicationNotFoundException e) {
            httpResponseHelper.error(Response.Status.NOT_FOUND);
        } catch (ApplicationCannotBeDeletedException e) {
            httpResponseHelper.error(Response.Status.FORBIDDEN, "Signed applications can't be deleted.");
        }
    }

    private Optional<String> getUserAgent() {
        return Optional.ofNullable(RequestHeaderUtils.getUserAgent(headers));
    }
}
