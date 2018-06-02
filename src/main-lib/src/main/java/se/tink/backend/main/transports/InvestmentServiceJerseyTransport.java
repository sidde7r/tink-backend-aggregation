package se.tink.backend.main.transports;

import com.google.inject.Inject;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiParam;
import javax.ws.rs.Consumes;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;
import se.tink.api.annotations.Team;
import se.tink.api.annotations.TeamOwnership;
import se.tink.backend.api.InvestmentService;
import se.tink.backend.auth.Authenticated;
import se.tink.backend.core.User;
import se.tink.backend.core.oauth2.OAuth2AuthorizationScopeTypes;
import se.tink.backend.main.controllers.InvestmentServiceController;
import se.tink.backend.rpc.InvestmentResponse;

@Path("/api/v1/investments")
@Api(value = "Investment Service", description = "A user can have a collection of financial instruments spread over one or multiple portfolios.")
public class InvestmentServiceJerseyTransport implements InvestmentService {
    private final InvestmentServiceController investmentServiceController;

    @Inject
    public InvestmentServiceJerseyTransport(InvestmentServiceController investmentServiceController) {
        this.investmentServiceController = investmentServiceController;
    }

    @GET
    @Consumes({ MediaType.APPLICATION_JSON })
    @Produces({ MediaType.APPLICATION_JSON })
    @ApiOperation(value = "List all the investments for a user.",
            notes = "Returns an object with a list of the authenticated user's portfolios and corresponding financial instruments.",
            response = InvestmentResponse.class)
    @TeamOwnership(Team.INTEGRATION)
    @Override
    public InvestmentResponse getInvestments(
            @Authenticated(scopes = OAuth2AuthorizationScopeTypes.INVESTMENTS_READ) @ApiParam(hidden = true) User user) {
        return new InvestmentResponse(investmentServiceController.getPortfolios(user.getId()));
    }
}
