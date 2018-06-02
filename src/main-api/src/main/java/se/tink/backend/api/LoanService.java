package se.tink.backend.api;

import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiParam;
import javax.ws.rs.GET;
import javax.ws.rs.PUT;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;
import se.tink.api.annotations.Team;
import se.tink.api.annotations.TeamOwnership;
import se.tink.backend.auth.Authenticated;
import se.tink.backend.auth.AuthenticatedUser;
import se.tink.backend.core.Loan;
import se.tink.backend.core.LoanEventsResponse;
import se.tink.backend.core.LoanResponse;
import se.tink.backend.core.LoanTimelineResponse;
import se.tink.backend.core.UpdateLoanRequest;
import se.tink.backend.core.oauth2.OAuth2AuthorizationScopeTypes;
import se.tink.backend.rpc.TinkMediaType;
import se.tink.backend.utils.ApiTag;

@Path("/api/v1/loans")
@Api(value = ApiTag.LOAN_SERVICE, description = "A loan is technically an account, but also has additional details which can be handled/fetched on this endpoint.")
public interface LoanService {

    @GET
    @Produces({ MediaType.APPLICATION_JSON, TinkMediaType.APPLICATION_PROTOBUF })
    @ApiOperation(value = "Get loans", notes = "Get all the loans for a user.")
    @TeamOwnership(Team.INTEGRATION)
    LoanResponse get(
            @Authenticated(scopes = { OAuth2AuthorizationScopeTypes.ACCOUNTS_READ }) AuthenticatedUser authenticatedUser
    );

    @PUT
    @Path("/update")
    @Produces({ MediaType.APPLICATION_JSON, TinkMediaType.APPLICATION_PROTOBUF })
    @ApiOperation(
            value = "Update a loan",
            notes = "Updates certain user modifiable properties of a loan. Please refer to the body schema to see which properties are modifiable by the loan.",
            tags = { ApiTag.LOAN_SERVICE, ApiTag.HIDE }
    )
    @TeamOwnership(Team.INTEGRATION)
    Loan update(
            @Authenticated AuthenticatedUser authenticatedUser,
            @ApiParam(value = "The updated loan object", required = true) UpdateLoanRequest updateLoanRequest
    );

    @GET
    @Path("/events")
    @Produces({ MediaType.APPLICATION_JSON })
    @ApiOperation(
            value = "List loan events",
            notes = "Lists events that affect the properties of a loan such as interest rate changes."
    )
    @TeamOwnership(Team.PFM)
    LoanEventsResponse getEvents(@Authenticated AuthenticatedUser authenticatedUser);

    @GET
    @Path("/timelines")
    @Produces({ MediaType.APPLICATION_JSON })
    @ApiOperation(
            value = "List loan history",
            notes = "Lists historical rates and balances for all loans as well as weighted average calculations for all loans together."
    )
    @TeamOwnership(Team.PFM)
    LoanTimelineResponse getLoanTimelines(@Authenticated AuthenticatedUser authenticatedUser);
}
