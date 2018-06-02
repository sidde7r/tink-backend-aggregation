package se.tink.backend.api;

import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiParam;
import java.util.List;
import javax.annotation.Nullable;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.MediaType;
import se.tink.api.annotations.Team;
import se.tink.api.annotations.TeamOwnership;
import se.tink.backend.auth.Authenticated;
import se.tink.backend.auth.AuthenticatedUser;
import se.tink.backend.rpc.TinkMediaType;
import se.tink.backend.rpc.calendar.BusinessDaysResponse;
import se.tink.libraries.date.Period;

@Path("/api/v1/calendar")
@Api(value = "Calendar Service", description = "Calendar service to fetch information about calendar items.")
public interface CalendarService {

    /**
     * The years, months and days that are considered business days
     * <p>
     * Example response from GET /businessdays/2016-03?months=3:
     * <pre>
     *  {
     *      "2016": {
     *          "3": [1,2,3,4,6,7,…],
     *          "4": [1,4,5,6,7,…],
     *          "5": [1,2,3,…]
     *      }
     *  }
     * </pre>
     *
     * @param startYear The year of the first month to be returned
     * @param startMonth The first month to be returned (on MM or M format)
     * @param months     Optional (default: 1) number of months to return from the startMonth (max 24 months)
     * @return Hash on format {YYYY: {M: [D,…], …}, …}
     */
    @GET
    @Path("/businessdays/{startYear}-{startMonth}")
    @TeamOwnership(Team.PFM)
    @Produces({
        MediaType.APPLICATION_JSON, TinkMediaType.APPLICATION_PROTOBUF
    })
    @ApiOperation(value = "Business days",
    notes = "Get the business days available for this user."
            )
    BusinessDaysResponse businessDays(@Authenticated @ApiParam(hidden = true) AuthenticatedUser user,
            @ApiParam(value="Start year for queried business days", required = true, example = "2016") @PathParam("startYear") Integer startYear,
            @ApiParam(value="Start month for queried business days", required = true, example = "05") @PathParam("startMonth") Integer startMonth,
            @ApiParam(value="Number of months queried for, default 1", required = false) @Nullable @QueryParam("months") Integer months);

    /**
     * Get list of periods start and end date.
     *
     * Example response from GET /periods/2016-03:
     * <pre>
     *     [
     *        {
     *            "clean": true,
     *            "endDate": 1458773999000,
     *            "name": "2016-03",
     *            "resolution": "MONTHLY_ADJUSTED",
     *            "startDate": 1456354800000
     *        }
     *     ]
     * </pre>
     *
     * @param user
     * @param period Possible inputs: yyyy, yyyy-M, yyyy-MM
     * @return
     */
    @GET
    @Path("/periods/{period}")
    @TeamOwnership(Team.PFM)
    @Produces({
        MediaType.APPLICATION_JSON, TinkMediaType.APPLICATION_PROTOBUF
    })
    @ApiOperation(value = "Period details",
    notes = "Get details for supplied period. Will always return resolution `MONTHLY`. Possible inputs: YYYY, YYYY-MM, YYYY-MM-DD"
            )
    List<Period> listPeriods(
            @Authenticated @ApiParam(hidden = true) AuthenticatedUser user,
            @ApiParam(value="Period to get details for", required = true, example = "2016-05-21") @PathParam("period") String period);
}
