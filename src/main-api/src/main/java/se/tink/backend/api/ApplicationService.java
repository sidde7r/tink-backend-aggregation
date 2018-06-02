package se.tink.backend.api;

import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiParam;
import javax.ws.rs.Consumes;
import javax.ws.rs.DELETE;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;
import se.tink.api.annotations.Team;
import se.tink.api.annotations.TeamOwnership;
import se.tink.backend.auth.Authenticated;
import se.tink.backend.auth.AuthenticatedUser;
import se.tink.backend.core.Application;
import se.tink.backend.core.ApplicationForm;
import se.tink.backend.core.ApplicationSummary;
import se.tink.backend.core.application.EligibleApplicationTypesResponse;
import se.tink.backend.core.enums.FeatureFlags;
import se.tink.backend.core.signableoperation.SignableOperation;
import se.tink.backend.rpc.ApplicationSummaryListResponse;
import se.tink.backend.rpc.TinkMediaType;

@Produces({ MediaType.APPLICATION_JSON, TinkMediaType.APPLICATION_PROTOBUF })
@Path("/api/v1/applications")
@Api(value = "Application Service", description = "A service managing a user's product applications.", hidden = true)
public interface ApplicationService {

    @POST
    @Path("{type}")
    @TeamOwnership(Team.FINANCIAL_SERVICES)
    @ApiOperation(value = "Create Application",
            notes = "Returns the newly created Application.",
            response = Application.class
    )
    Application createApplication(
            @Authenticated(
                    requireFeatureGroup = FeatureFlags.FeatureFlagGroup.APPLICATIONS_FEATURE
            ) AuthenticatedUser authenticatedUser,
            @PathParam("type") @ApiParam(value = "The application type", required = true) String type);

    @GET
    @Path("{id}")
    @TeamOwnership(Team.FINANCIAL_SERVICES)
    @ApiOperation(value = "Get Application",
            notes = "Returns an Application with the specified id.",
            response = Application.class
    )
    Application getApplication(
            @Authenticated(
                    requireFeatureGroup = FeatureFlags.FeatureFlagGroup.APPLICATIONS_FEATURE
            ) AuthenticatedUser authenticatedUser,
            @PathParam("id") @ApiParam(value = "The application ID", required = true) String id);
    
    @DELETE
    @Path("{id}")
    @TeamOwnership(Team.FINANCIAL_SERVICES)
    @ApiOperation(value = "Delete application",
            notes = "Deletes the application with the specified id."
    )
    void delete(
            @Authenticated(
                    requireFeatureGroup = FeatureFlags.FeatureFlagGroup.APPLICATIONS_FEATURE
            ) AuthenticatedUser authenticatedUser,
            @PathParam("id") @ApiParam(value = "The application ID", required = true) String id);
    
    @GET
    @Path("{id}/summary")
    @TeamOwnership(Team.FINANCIAL_SERVICES)
    @ApiOperation(value = "Get application summary",
            notes = "Returns an application summary for the specified id.",
            response = ApplicationSummary.class
    )
    ApplicationSummary getSummary(
            @Authenticated(
                    requireFeatureGroup = FeatureFlags.FeatureFlagGroup.APPLICATIONS_FEATURE
            ) AuthenticatedUser authenticatedUser,
            @PathParam("id") @ApiParam(value = "The application ID", required = true) String id);
    
    @GET
    @Path("/list")
    @TeamOwnership(Team.FINANCIAL_SERVICES)
    @ApiOperation(value = "Get all applications for a user",
            notes = "Returns a list of application summaries for all outstanding and completed applications for the user (wrapped in a response container).",
            response = ApplicationSummaryListResponse.class
    )
    ApplicationSummaryListResponse list(
            @Authenticated(
                    requireFeatureGroup = FeatureFlags.FeatureFlagGroup.APPLICATIONS_FEATURE
            ) AuthenticatedUser authenticatedUser);
    

    @POST
    @Path("{id}/form")
    @TeamOwnership(Team.FINANCIAL_SERVICES)
    @Consumes({ MediaType.APPLICATION_JSON, TinkMediaType.APPLICATION_PROTOBUF })
    @ApiOperation(value = "Submit Form to Application",
            notes = "Validates and completes the incoming Form on the Application. Returns the full Application with potentially a new uncompleted Form",
            response = Application.class
    )
    Application submitForm(
            @Authenticated(
                    requireFeatureGroup = FeatureFlags.FeatureFlagGroup.APPLICATIONS_FEATURE
            ) AuthenticatedUser authenticatedUser,
            @PathParam("id") @ApiParam(value = "The application ID", required = true) String id, @ApiParam(value="The form to complete", required = true) ApplicationForm form);

    @POST
    @Path("{id}/submit")
    @TeamOwnership(Team.FINANCIAL_SERVICES)
    @ApiOperation(value = "Submit Application",
            notes = "Submits the full Application",
            response = SignableOperation.class
    )
    SignableOperation submit(
            @Authenticated(
                    requireFeatureGroup = FeatureFlags.FeatureFlagGroup.APPLICATIONS_FEATURE
            ) AuthenticatedUser authenticatedUser,
            @PathParam("id") @ApiParam(value = "The application ID", required = true) String id);

    @GET
    @Path("eligibleApplicationTypes")
    @TeamOwnership(Team.FINANCIAL_SERVICES)
    @ApiOperation(value = "", hidden = true)
    EligibleApplicationTypesResponse getEligibleApplicationTypes(
            @Authenticated(requireFeatureGroup = FeatureFlags.FeatureFlagGroup.APPLICATIONS_FEATURE) AuthenticatedUser authenticatedUser);
}
