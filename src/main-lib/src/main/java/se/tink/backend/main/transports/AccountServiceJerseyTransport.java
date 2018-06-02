package se.tink.backend.main.transports;

import com.google.inject.Inject;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiParam;
import java.util.List;
import java.util.NoSuchElementException;
import javax.ws.rs.Consumes;
import javax.ws.rs.GET;
import javax.ws.rs.PUT;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.WebApplicationException;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import se.tink.api.annotations.Team;
import se.tink.api.annotations.TeamOwnership;
import se.tink.backend.api.AccountService;
import se.tink.backend.auth.Authenticated;
import se.tink.backend.core.Account;
import se.tink.backend.core.User;
import se.tink.backend.core.oauth2.OAuth2AuthorizationScopeTypes;
import se.tink.backend.main.controllers.AccountServiceController;
import se.tink.backend.rpc.AccountListResponse;
import se.tink.backend.rpc.TinkMediaType;
import se.tink.backend.utils.ApiTag;

@Path("/api/v1/accounts")
@Api(value = ApiTag.ACCOUNT_SERVICE,
        description = "An account could either be a debit account, a credit card, a loan or mortgage.")
public class AccountServiceJerseyTransport implements AccountService {

    private final AccountServiceController accountServiceController;

    @Inject
    public AccountServiceJerseyTransport(AccountServiceController accountServiceController) {
        this.accountServiceController = accountServiceController;
    }

    /**
     * External users shouldn't use this method anymore. Use /accounts/list => getAccountsList (User)
     */
    @GET
    @Produces(MediaType.APPLICATION_JSON)
    @Deprecated
    @TeamOwnership(Team.DATA)
    @Override
    public List<Account> list(
            @Authenticated(scopes = OAuth2AuthorizationScopeTypes.ACCOUNTS_READ) @ApiParam(hidden = true) User user) {
        return accountServiceController.list(user.getId());
    }

    @GET
    @Path("/list")
    @Produces({ MediaType.APPLICATION_JSON, TinkMediaType.APPLICATION_PROTOBUF })
    @ApiOperation(
            value = "List accounts",
            notes = "Returns an object with a list of the authenticated user's accounts.",
            response = AccountListResponse.class
    )
    @TeamOwnership(Team.DATA)
    @Override
    public AccountListResponse listAccounts(
            @Authenticated(scopes = OAuth2AuthorizationScopeTypes.ACCOUNTS_READ) @ApiParam(hidden = true) User user) {
        return new AccountListResponse(accountServiceController.list(user.getId()));
    }

    @PUT
    @Path("{id}")
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    @ApiOperation(
            value = "Update an Account",
            notes = "Updates certain user modifiable properties of an account. Please refer to the body schema to see which properties are modifiable by the user.",
            response = Account.class,
            tags = { ApiTag.ACCOUNT_SERVICE, ApiTag.HIDE }
    )
    @TeamOwnership(Team.DATA)
    @Override
    public Account update(
            @Authenticated(scopes = OAuth2AuthorizationScopeTypes.ACCOUNTS_WRITE) @ApiParam(hidden = true) User user,
            @PathParam("id") @ApiParam(value = "The id of the account",
                    required = true,
                    example = "8937fa00166946cbbcbbec569c9d6e90") String id,
            @ApiParam(value = "The updated account object", required = true) Account account
    ) {
        try {
            return accountServiceController.update(user.getId(), id, account);
        } catch (NoSuchElementException | IllegalArgumentException ex) {
            throw new WebApplicationException(Response.Status.BAD_REQUEST);
        }
    }
}
