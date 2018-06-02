package se.tink.backend.main.transports;

import com.codahale.metrics.annotation.Timed;
import com.google.inject.Inject;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiParam;
import java.util.List;
import javax.ws.rs.Consumes;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.WebApplicationException;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import se.tink.api.annotations.Team;
import se.tink.api.annotations.TeamOwnership;
import se.tink.backend.api.StatisticsService;
import se.tink.backend.auth.Authenticated;
import se.tink.backend.auth.AuthenticatedUser;
import se.tink.backend.common.exceptions.LockException;
import se.tink.backend.core.Statistic;
import se.tink.backend.core.StatisticQuery;
import se.tink.backend.core.insights.InsightsResponse;
import se.tink.backend.core.oauth2.OAuth2AuthorizationScopeTypes;
import se.tink.backend.main.controllers.InsightsController;
import se.tink.backend.main.controllers.StatisticsServiceController;

@Path("/api/v1/statistics")
@Consumes(MediaType.APPLICATION_JSON)
@Produces(MediaType.APPLICATION_JSON)
@Api("Statistics Service")
public class StatisticsServiceJerseyTransport implements StatisticsService {
    private final StatisticsServiceController statisticsServiceController;
    private final InsightsController insightsController;

    @Inject
    public StatisticsServiceJerseyTransport(StatisticsServiceController statisticsServiceController,
            InsightsController insightsController) {
        this.statisticsServiceController = statisticsServiceController;
        this.insightsController = insightsController;
    }

    @GET
    @Timed
    @Override
    @ApiOperation(value = "List statistics", hidden = true)
    @TeamOwnership(Team.DATA)
    public List<Statistic> list(
            @Authenticated(scopes = OAuth2AuthorizationScopeTypes.STATISTICS_READ) AuthenticatedUser user) {
        try {
            return statisticsServiceController.list(user.getUser().getId(),
                    user.getUser().getProfile().getPeriodMode());
        } catch (LockException e) {
            throw new WebApplicationException(Response.Status.SERVICE_UNAVAILABLE);
        }
    }

    @POST
    @Path("/query")
    @TeamOwnership(Team.DATA)
    @Timed
    @ApiOperation(value = "Query statistics",
            notes = "Queries statistics",
            response = Statistic.class,
            responseContainer = "List"
    )
    @Override
    public List<Statistic> query(
            @Authenticated(scopes = OAuth2AuthorizationScopeTypes.STATISTICS_READ) AuthenticatedUser authenticatedUser,
            @ApiParam(value = "The query object", required = true) StatisticQuery query) {
        try {
            return statisticsServiceController.query(authenticatedUser.getUser().getId(),
                    authenticatedUser.getUser().getProfile().getPeriodMode(), query);
        } catch (LockException e) {
            throw new WebApplicationException(Response.Status.SERVICE_UNAVAILABLE);
        }
    }

    @POST
    @Path("/queries")
    @TeamOwnership(Team.DATA)
    @Timed
    @ApiOperation(value = "Query multiple statistics", hidden = true)
    @Override
    public List<Statistic> queries(
            @Authenticated(scopes = OAuth2AuthorizationScopeTypes.STATISTICS_READ) AuthenticatedUser authenticatedUser,
            @ApiParam(value = "List of query objects", required = true) List<StatisticQuery> queries) {
        try {
            return statisticsServiceController.queries(authenticatedUser.getUser().getId(),
                    authenticatedUser.getUser().getProfile().getPeriodMode(), queries);
        } catch (LockException e) {
            throw new WebApplicationException(Response.Status.SERVICE_UNAVAILABLE);
        }
    }

    @GET
    @Path("/insights")
    @TeamOwnership(Team.PFM)
    @ApiOperation(value = "", hidden = true)
    @Override
    public InsightsResponse insights(@Authenticated AuthenticatedUser authenticatedUser) {
        return insightsController.getInsights(authenticatedUser.getUser());
    }
}
