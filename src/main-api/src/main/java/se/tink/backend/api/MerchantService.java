package se.tink.backend.api;

import javax.ws.rs.Consumes;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;
import se.tink.api.annotations.Team;
import se.tink.api.annotations.TeamOwnership;
import se.tink.backend.auth.Authenticated;
import se.tink.backend.core.Merchant;
import se.tink.backend.core.User;
import se.tink.backend.rpc.MerchantQuery;
import se.tink.backend.rpc.MerchantQueryResponse;
import se.tink.backend.rpc.MerchantSkipRequest;
import se.tink.backend.rpc.MerchantizeTransactionsRequest;
import se.tink.backend.rpc.SuggestMerchantizeRequest;
import se.tink.backend.rpc.SuggestMerchantizeResponse;
import se.tink.backend.rpc.TinkMediaType;

@Path("/api/v1/merchants")
@Consumes({ MediaType.APPLICATION_JSON })
@Produces({ MediaType.APPLICATION_JSON })
public interface MerchantService {
        
    @GET
    @Path("{id}")
    @TeamOwnership(Team.PFM)
    @Produces({ MediaType.APPLICATION_JSON, TinkMediaType.APPLICATION_PROTOBUF })
    public Merchant get(@Authenticated User user, @PathParam("id") String id);
            
    @POST
    @Path("query")
    @TeamOwnership(Team.PFM)
    @Consumes({ MediaType.APPLICATION_JSON, TinkMediaType.APPLICATION_PROTOBUF })
    @Produces({ MediaType.APPLICATION_JSON, TinkMediaType.APPLICATION_PROTOBUF })
    public MerchantQueryResponse query(@Authenticated User user, MerchantQuery request);
    
    @POST
    @Path("merchantize")
    @TeamOwnership(Team.PFM)
    @Consumes({ MediaType.APPLICATION_JSON, TinkMediaType.APPLICATION_PROTOBUF })
    @Produces({ MediaType.APPLICATION_JSON, TinkMediaType.APPLICATION_PROTOBUF })
    public Merchant merchantize(@Authenticated User user, MerchantizeTransactionsRequest requests);
    
    @POST
    @Path("address")
    @TeamOwnership(Team.PFM)
    @Consumes({ MediaType.APPLICATION_JSON, TinkMediaType.APPLICATION_PROTOBUF })
    @Produces({ MediaType.APPLICATION_JSON, TinkMediaType.APPLICATION_PROTOBUF })
    public MerchantQueryResponse address(@Authenticated User user, MerchantQuery request);

    @POST
    @Path("suggest")
    @TeamOwnership(Team.PFM)
    @Consumes({ MediaType.APPLICATION_JSON, TinkMediaType.APPLICATION_PROTOBUF })
    @Produces({ MediaType.APPLICATION_JSON, TinkMediaType.APPLICATION_PROTOBUF })
    public SuggestMerchantizeResponse suggest(@Authenticated User user, SuggestMerchantizeRequest request);

    @POST
    @Path("skip")
    @TeamOwnership(Team.PFM)
    @Consumes({ MediaType.APPLICATION_JSON, TinkMediaType.APPLICATION_PROTOBUF })
    @Produces({ MediaType.APPLICATION_JSON, TinkMediaType.APPLICATION_PROTOBUF })
    public void skip(@Authenticated User user, MerchantSkipRequest request);
}
