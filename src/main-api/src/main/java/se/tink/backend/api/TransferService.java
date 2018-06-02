package se.tink.backend.api;

import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiParam;
import java.net.URI;
import java.util.Set;
import javax.ws.rs.Consumes;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.PUT;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.MediaType;
import se.tink.api.annotations.Team;
import se.tink.api.annotations.TeamOwnership;
import se.tink.backend.auth.Authenticated;
import se.tink.backend.auth.AuthenticatedUser;
import se.tink.backend.core.enums.FeatureFlags;
import se.tink.backend.core.enums.TransferType;
import se.tink.backend.core.oauth2.OAuth2AuthorizationScopeTypes;
import se.tink.backend.core.signableoperation.SignableOperation;
import se.tink.backend.core.transfer.Transfer;
import se.tink.backend.core.transfer.TransferDestination;
import se.tink.backend.rpc.AccountListResponse;
import se.tink.backend.rpc.ClearingLookupResponse;
import se.tink.backend.rpc.GiroLookupResponse;
import se.tink.backend.rpc.TinkMediaType;
import se.tink.backend.rpc.TransferListResponse;
import se.tink.backend.utils.ApiTag;
import se.tink.libraries.account.AccountIdentifier;

@Path("/api/v1/transfer")
@Api(value = ApiTag.TRANSFER_SERVICE, description = "A service that can be used initiate bank transfers, pay bills or sign e-invoices.")
@Consumes(MediaType.APPLICATION_JSON)
@Produces(MediaType.APPLICATION_JSON)
public interface TransferService {

    @POST
    @Consumes({ MediaType.APPLICATION_JSON, TinkMediaType.APPLICATION_PROTOBUF })
    @Produces({ MediaType.APPLICATION_JSON, TinkMediaType.APPLICATION_PROTOBUF })
    @ApiOperation(
            value = "Create new Transfer",
            notes = "Creates a new Transfer. The type of the transfer (BANK_TRANSFER or PAYMENT) will be based on the given destinationUri.",
            tags = { ApiTag.TRANSFER_SERVICE, ApiTag.HIDE }
    )
    @TeamOwnership(Team.INTEGRATION)
    SignableOperation createTransfer(
            @Authenticated(
                    requireFeatureGroup = FeatureFlags.FeatureFlagGroup.TRANSFERS_FEATURE,
                    scopes = { OAuth2AuthorizationScopeTypes.TRANSFER_EXECUTE }) AuthenticatedUser user,
            @ApiParam(name = "transfer", value = "The Transfer object") Transfer transfer
    );

    @GET
    @Produces({ MediaType.APPLICATION_JSON, TinkMediaType.APPLICATION_PROTOBUF })
    @ApiOperation(value = "", hidden = true)
    @TeamOwnership(Team.INTEGRATION)
    TransferListResponse list(
            @Authenticated(requireFeatureGroup = FeatureFlags.FeatureFlagGroup.TRANSFERS_FEATURE) AuthenticatedUser user,
            @QueryParam("type") TransferType type
    );

    @PUT
    @Path("/{id}")
    @TeamOwnership(Team.INTEGRATION)
    @Consumes({ MediaType.APPLICATION_JSON, TinkMediaType.APPLICATION_PROTOBUF })
    @Produces({ MediaType.APPLICATION_JSON, TinkMediaType.APPLICATION_PROTOBUF })
    @ApiOperation(value = "", hidden = true)
    SignableOperation update(
            @Authenticated(requireFeatureGroup = FeatureFlags.FeatureFlagGroup.TRANSFERS_FEATURE) AuthenticatedUser user,
            @PathParam("id") String id,
            Transfer transfer
    );

    @GET
    @Path("{id}")
    @TeamOwnership(Team.INTEGRATION)
    @Produces({ MediaType.APPLICATION_JSON, TinkMediaType.APPLICATION_PROTOBUF })
    @ApiOperation(value = "", hidden = true)
    Transfer get(
            @Authenticated(requireFeatureGroup = FeatureFlags.FeatureFlagGroup.TRANSFERS_FEATURE) AuthenticatedUser user,
            @PathParam("id") String id
    );

    @GET
    @Path("{id}/status")
    @TeamOwnership(Team.INTEGRATION)
    @Produces({ MediaType.APPLICATION_JSON, TinkMediaType.APPLICATION_PROTOBUF })
    @ApiOperation(
            value = "Signing status of Transfer",
            notes = "Get the SignableOperation of the underlying transfer.",
            tags = { ApiTag.TRANSFER_SERVICE, ApiTag.HIDE }
    )
    SignableOperation getSignableOperation(
            @Authenticated(
                    requireFeatureGroup = FeatureFlags.FeatureFlagGroup.TRANSFERS_FEATURE,
                    scopes = { OAuth2AuthorizationScopeTypes.TRANSFER_READ }) AuthenticatedUser user,
            @PathParam("id") @ApiParam(name = "id", value = "The id of the Transfer") String id
    );

    @GET
    @Path("{id}/accounts")
    @TeamOwnership(Team.INTEGRATION)
    @Produces({ MediaType.APPLICATION_JSON, TinkMediaType.APPLICATION_PROTOBUF })
    @ApiOperation(value = "", hidden = true)
    AccountListResponse getSourceAccountsForTransfer(
            @Authenticated(requireFeatureGroup = FeatureFlags.FeatureFlagGroup.TRANSFERS_FEATURE) AuthenticatedUser user,
            @PathParam("id") String id
    );

    @GET
    @Path("accounts")
    @TeamOwnership(Team.INTEGRATION)
    @Produces({ MediaType.APPLICATION_JSON, TinkMediaType.APPLICATION_PROTOBUF })
    @ApiOperation(value = "Get accounts and destinations", tags = { ApiTag.TRANSFER_SERVICE, ApiTag.HIDE })
    AccountListResponse getSourceAccounts(
            @Authenticated(
                    requireFeatureGroup = FeatureFlags.FeatureFlagGroup.TRANSFERS_FEATURE,
                    scopes = { OAuth2AuthorizationScopeTypes.TRANSFER_READ }) AuthenticatedUser user,
            @QueryParam("type[]") @ApiParam(name = "type", value = "The type of transfers the account is capable of") Set<AccountIdentifier.Type> explicitTypeFilter,
            @QueryParam("destination[]") @ApiParam(name = "destination", value = "Filter only accounts which can do transfers to the specified destination") Set<URI> explicitIdentifierFilter
    );

    @GET
    @Path("/lookup/giro/{number}")
    @TeamOwnership(Team.INTEGRATION)
    @Produces({ MediaType.APPLICATION_JSON, TinkMediaType.APPLICATION_PROTOBUF })
    @ApiOperation(value = "", hidden = true)
    GiroLookupResponse giroLookup(
            @Authenticated(requireFeatureGroup = FeatureFlags.FeatureFlagGroup.TRANSFERS_FEATURE) AuthenticatedUser user,
            @PathParam("number") String number
    );

    @POST
    @Path("destination")
    @TeamOwnership(Team.INTEGRATION)
    @Consumes({ MediaType.APPLICATION_JSON, TinkMediaType.APPLICATION_PROTOBUF })
    @ApiOperation(value = "", hidden = true)
    TransferDestination createDestination(
            @Authenticated(requireFeatureGroup = FeatureFlags.FeatureFlagGroup.TRANSFERS_FEATURE) AuthenticatedUser user,
            TransferDestination destination
    );

    @GET
    @Path("/lookup/clearing/{clearing}")
    @TeamOwnership(Team.INTEGRATION)
    @Produces({ MediaType.APPLICATION_JSON, TinkMediaType.APPLICATION_PROTOBUF })
    @ApiOperation(value = "", hidden = true)
    ClearingLookupResponse clearingLookup(
            @Authenticated(requireFeatureGroup = FeatureFlags.FeatureFlagGroup.TRANSFERS_FEATURE) AuthenticatedUser user,
            @PathParam("clearing") String clearing
    );
}

