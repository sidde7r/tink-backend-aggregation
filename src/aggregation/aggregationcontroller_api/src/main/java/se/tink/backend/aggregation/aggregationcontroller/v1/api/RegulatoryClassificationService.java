package se.tink.backend.aggregation.aggregationcontroller.v1.api;

import javax.ws.rs.Consumes;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;
import se.tink.api.annotations.Team;
import se.tink.api.annotations.TeamOwnership;
import se.tink.backend.aggregation.aggregationcontroller.v1.rpc.CoreRegulatoryClassification;
import se.tink.backend.aggregation.aggregationcontroller.v1.rpc.UpsertRegulatoryClassificationRequest;

@Path("/aggregation/controller/v1/regulatory-classification")
@Consumes(MediaType.APPLICATION_JSON)
@Produces(MediaType.APPLICATION_JSON)
public interface RegulatoryClassificationService {
    @POST
    @Path("/upsert")
    @TeamOwnership(Team.AGGREGATION_EXPERIENCE)
    CoreRegulatoryClassification upsertRegulatoryClassification(
            UpsertRegulatoryClassificationRequest request);
}
