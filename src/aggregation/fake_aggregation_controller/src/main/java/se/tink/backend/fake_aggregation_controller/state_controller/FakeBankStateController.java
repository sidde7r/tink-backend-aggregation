package se.tink.backend.fake_aggregation_controller.state_controller;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import javax.ws.rs.Consumes;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import se.tink.backend.fake_aggregation_controller.dto.SetStateDto;

@Path("/bank_state")
public class FakeBankStateController {

    private final Map<String, String> credentialsIdToBankServerState;
    private static FakeBankStateController instance;

    private FakeBankStateController() {
        credentialsIdToBankServerState = new ConcurrentHashMap<>();
    }

    public static synchronized FakeBankStateController getInstance() {
        if (instance == null) {
            instance = new FakeBankStateController();
        }
        return instance;
    }

    @GET
    @Path("/{credentials_id}")
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    public Response getBankState(@PathParam(value = "credentials_id") String credentialsId) {
        return Response.ok(credentialsIdToBankServerState.getOrDefault(credentialsId, null))
                .build();
    }

    @POST
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    public Response setBankState(SetStateDto setStateDto) {
        credentialsIdToBankServerState.put(setStateDto.getCredentialsId(), setStateDto.getState());
        return Response.ok().build();
    }
}
