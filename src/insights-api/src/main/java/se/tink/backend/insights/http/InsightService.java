package se.tink.backend.insights.http;

import java.util.List;
import javax.ws.rs.Consumes;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import se.tink.api.annotations.Team;
import se.tink.api.annotations.TeamOwnership;
import se.tink.backend.insights.http.dto.CreateInsightsRequest;
import se.tink.backend.insights.http.dto.GetInsightsRequest;
import se.tink.backend.insights.http.dto.GetInsightsResponse;
import se.tink.backend.insights.http.dto.GetRenderedRequest;
import se.tink.backend.insights.http.dto.GetRenderedResponse;
import se.tink.backend.insights.http.dto.SelectActionRequest;
import se.tink.backend.insights.http.dto.SelectActionResponse;

@Path("/api/v3/insights")
@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)
public interface InsightService {

    @POST
    @TeamOwnership(Team.PFM)
    @Path("/render")
    GetRenderedResponse getRendered(GetRenderedRequest request);

    @POST
    @TeamOwnership(Team.PFM)
    Response create(CreateInsightsRequest request);

    @POST
    @TeamOwnership(Team.PFM)
    @Path("/action")
    SelectActionResponse selectAction(SelectActionRequest request);

    @GET
    @TeamOwnership(Team.PFM)
    GetInsightsResponse getInsights(GetInsightsRequest request);
}
