package se.tink.backend.categorization.api;

import se.tink.backend.categorization.rpc.CreateTrainingResponse;
import se.tink.backend.categorization.rpc.FeedTrainingRequest;

import javax.ws.rs.Consumes;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;

public interface FastTextTrainerService {
    @POST
    @Path("/fasttext/training")
    @Produces(MediaType.APPLICATION_JSON)
    CreateTrainingResponse createTraining();

    @POST
    @Path("fasttext/training/{modelPath}")
    @Consumes(MediaType.APPLICATION_JSON)
    void feedTraining(@PathParam("modelPath") String modelPath, FeedTrainingRequest feedTrainingRequest);
}
