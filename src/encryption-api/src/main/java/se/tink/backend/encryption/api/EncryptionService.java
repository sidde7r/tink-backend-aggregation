package se.tink.backend.encryption.api;

import javax.ws.rs.Consumes;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import se.tink.backend.encryption.rpc.DecryptionRequest;
import se.tink.backend.encryption.rpc.DecryptionResponse;
import se.tink.backend.encryption.rpc.EncryptionKeySet;
import se.tink.backend.encryption.rpc.EncryptionRequest;
import se.tink.backend.encryption.rpc.EncryptionResponse;
import se.tink.libraries.http.annotations.auth.AllowAnonymous;

@Path("/encryption")
@Consumes({
    MediaType.APPLICATION_JSON
})
@Produces({
    MediaType.APPLICATION_JSON
})
public interface EncryptionService {

    @POST
    @Path("injectKey")
    @Consumes({
        MediaType.APPLICATION_JSON
    })
    @Produces({
        MediaType.APPLICATION_JSON
    })
    public Response injectKey(String key);

    @POST
    @Path("encrypt")
    @Consumes({
            MediaType.APPLICATION_JSON
    })
    @Produces({
            MediaType.APPLICATION_JSON
    })
    EncryptionResponse encrypt(EncryptionRequest request);

    @POST
    @Path("decrypt")
    @Consumes({
            MediaType.APPLICATION_JSON
    })
    @Produces({
            MediaType.APPLICATION_JSON
    })
    DecryptionResponse decrypt(DecryptionRequest request);

    @POST
    @Path("generate/keys")
    @Produces({
            MediaType.APPLICATION_JSON
    })
    EncryptionKeySet generateEncryptionKeys();

    @GET
    @Path("ping")
    @Produces({
        MediaType.TEXT_PLAIN
    })
    @AllowAnonymous
    public String ping();

}
