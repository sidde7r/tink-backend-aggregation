package se.tink.backend.categorization.api;
import javax.ws.rs.Consumes;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.MediaType;
import se.tink.backend.categorization.rpc.CategorizationResult;

@Path("/categorize")
@Consumes(MediaType.APPLICATION_JSON)
@Produces(MediaType.APPLICATION_JSON)
public interface CategorizationService {
    @GET
    @Path("{market}")
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    CategorizationResult category(@PathParam("market") String marketName, @QueryParam("description") String description);
}
