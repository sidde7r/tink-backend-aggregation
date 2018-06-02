package se.tink.backend.categorization.api;
import javax.ws.rs.Consumes;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.MediaType;
import se.tink.backend.categorization.rpc.FastTextClassifierResult;
import se.tink.backend.categorization.rpc.FastTextTrainRequest;

@Path("/fasttext/classifiers")
@Consumes(MediaType.APPLICATION_JSON)
@Produces(MediaType.APPLICATION_JSON)
public interface FastTextClassifierService {
    @GET
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    void train(FastTextTrainRequest request);

    @GET
    @Path("{modelName}")
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    FastTextClassifierResult classifyQuery(@PathParam("modelName") String modelName, @QueryParam("q") String query);
}
