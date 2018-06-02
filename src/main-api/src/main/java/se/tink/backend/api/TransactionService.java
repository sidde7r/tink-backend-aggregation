package se.tink.backend.api;

import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiParam;
import java.util.List;
import javax.ws.rs.Consumes;
import javax.ws.rs.DELETE;
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
import se.tink.backend.core.Transaction;
import se.tink.backend.core.TransactionQuery;
import se.tink.backend.core.User;
import se.tink.backend.core.oauth2.OAuth2AuthorizationScopeTypes;
import se.tink.backend.rpc.CategorizeTransactionPartRequest;
import se.tink.backend.rpc.CategorizeTransactionPartResponse;
import se.tink.backend.rpc.CategorizeTransactionsListRequest;
import se.tink.backend.rpc.CategorizeTransactionsRequest;
import se.tink.backend.rpc.DeleteTransactionPartResponse;
import se.tink.backend.rpc.LinkTransactionsResponse;
import se.tink.backend.rpc.SimilarTransactionsResponse;
import se.tink.backend.rpc.SuggestTransactionsResponse;
import se.tink.backend.rpc.TinkMediaType;
import se.tink.backend.rpc.TransactionFraudulentRequest;
import se.tink.backend.rpc.TransactionLinkPromptRequest;
import se.tink.backend.rpc.TransactionLinkPromptResponse;
import se.tink.backend.rpc.TransactionLinkSuggestionResponse;
import se.tink.backend.rpc.TransactionQueryResponse;
import se.tink.backend.utils.ApiTag;

@Path("/api/v1/transactions")
@Api(value = ApiTag.TRANSACTION_SERVICE, description = "Transactions Service")
public interface TransactionService {

    /**
     * List transactions. Deprecated. Use {@link SearchService#searchQuery(User, String, int, int, String, String)} instead.
     */
    @GET
    @Produces({ MediaType.APPLICATION_JSON })
    @TeamOwnership(Team.DATA)
    @ApiOperation(value = "", hidden = true)
    @Deprecated
    List<Transaction> list(
            @Authenticated(scopes = { OAuth2AuthorizationScopeTypes.TRANSACTIONS_READ })
            @ApiParam(hidden = true) User user,
            @QueryParam("categories[]") List<String> categories,
            @QueryParam("accounts[]") List<String> accounts,
            @QueryParam("periods[]") List<String> periods,
            @QueryParam("offset") int offset,
            @QueryParam("limit") int limit,
            @QueryParam("sort") String sort,
            @QueryParam("order") String order
    );

    @PUT
    @Consumes({ MediaType.APPLICATION_JSON })
    @ApiOperation(
            value = "Update a list of transactions",
            notes = "Updates certain user modifiable properties of a list of transactions",
            tags = { ApiTag.TRANSACTION_SERVICE, ApiTag.HIDE }
    )
    @TeamOwnership(Team.DATA)
    void updateTransactions(
            @Authenticated(scopes = { OAuth2AuthorizationScopeTypes.TRANSACTIONS_WRITE })
            @ApiParam(hidden = true) User user,
            @ApiParam(value = "The transactions to be updated", required = true) List<Transaction> transactions
    );

    @PUT
    @Path("{id}")
    @TeamOwnership(Team.DATA)
    @Consumes({ MediaType.APPLICATION_JSON, TinkMediaType.APPLICATION_PROTOBUF })
    @Produces({ MediaType.APPLICATION_JSON, TinkMediaType.APPLICATION_PROTOBUF })
    @ApiOperation(
            value = "Update a transaction",
            notes = "Updates certain user modifiable properties of a transaction",
            tags = { ApiTag.TRANSACTION_SERVICE, ApiTag.HIDE }
    )
    Transaction updateTransaction(
            @Authenticated(scopes = { OAuth2AuthorizationScopeTypes.TRANSACTIONS_WRITE })
            @ApiParam(hidden = true) User user,
            @PathParam("id") @ApiParam(
                    value = "The id of the transaction",
                    required = true,
                    example = "8937fa00166946cbbcbbec569c9d6e90") String id,
            @ApiParam(value = "The transaction to be updated", required = true) Transaction transaction
    );

    @GET
    @Path("{id}")
    @TeamOwnership(Team.DATA)
    @Produces({ MediaType.APPLICATION_JSON })
    @ApiOperation(
            value = "Get one transaction",
            notes = "Returns a transaction matching the requested id",
            response = Transaction.class
    )
    Transaction getTransaction(
            @Authenticated(scopes = { OAuth2AuthorizationScopeTypes.TRANSACTIONS_READ })
            @ApiParam(hidden = true) User user,
            @PathParam("id") @ApiParam(
                    value = "The id of the transaction",
                    required = true,
                    example = "8937fa00166946cbbcbbec569c9d6e90") String id
    );

    @POST
    @Path("query")
    @TeamOwnership(Team.DATA)
    @Consumes({ MediaType.APPLICATION_JSON, TinkMediaType.APPLICATION_PROTOBUF })
    @Produces({ MediaType.APPLICATION_JSON, TinkMediaType.APPLICATION_PROTOBUF })
    @ApiOperation(value = "", hidden = true)
    TransactionQueryResponse query(
            @Authenticated(scopes = { OAuth2AuthorizationScopeTypes.TRANSACTIONS_READ })
            @ApiParam(hidden = true) User user,
            TransactionQuery queryRequest
    );

    @GET
    @Path("{id}/similar")
    @TeamOwnership(Team.DATA)
    @Produces({ MediaType.APPLICATION_JSON })
    @ApiOperation(
            value = "Get similar transactions",
            notes = "Returns an object holding a list of transactions similar to the supplied transaction based on description and a list of statistics summarizing these transactions",
            response = SimilarTransactionsResponse.class
    )
    SimilarTransactionsResponse similar(
            @Authenticated(scopes = { OAuth2AuthorizationScopeTypes.TRANSACTIONS_READ })
            @ApiParam(hidden = true) User user,
            @PathParam("id") @ApiParam(value = "The id of the transaction", example = "8937fa00166946cbbcbbec569c9d6e90") String id,
            @QueryParam("categoryId") @ApiParam(value = "Returns similar of the this cateogry", required = false, example = "c3b543d4817c4c08a96da789282f0501") String categoryId,
            @QueryParam("includeSelf") @ApiParam(value = "Include the supplied transaction in response", required = false, example = "false") boolean includeSelf
    );

    @GET
    @Path("suggest")
    @TeamOwnership(Team.DATA)
    @Produces({ MediaType.APPLICATION_JSON })
    @ApiOperation(
            value = "Get categorization clusters",
            notes = "Returns an object holding clusters of transactions to be categorized and possible categorization level improvement",
            response = SuggestTransactionsResponse.class
    )
    SuggestTransactionsResponse suggest(
            @Authenticated(scopes = { OAuth2AuthorizationScopeTypes.TRANSACTIONS_READ })
            @ApiParam(hidden = true) User user,
            @QueryParam("numberOfClusters") @ApiParam(value = "Max number of clusters returned", example = "7") int numberOfClusters,
            @QueryParam("evaluateEverything") boolean evaluateEverything
    );

    @POST
    @Path("categorize")
    @TeamOwnership(Team.DATA)
    @Consumes({ MediaType.APPLICATION_JSON })
    @ApiOperation(value = "", hidden = true)
    @Deprecated
    void categorize(
            @Authenticated(scopes = { OAuth2AuthorizationScopeTypes.TRANSACTIONS_CATEGORIZE })
            @ApiParam(hidden = true) User user,
            List<CategorizeTransactionsRequest> categorizeRequests
    );

    @PUT
    @Path("categorize-multiple")
    @TeamOwnership(Team.DATA)
    @Consumes({ MediaType.APPLICATION_JSON })
    @ApiOperation(
            value = "Change category of transactions",
            notes = "Changes category of the supplied list of transactions to the supplied category",
            tags = { ApiTag.TRANSACTION_SERVICE, ApiTag.HIDE }
    )
    void categorize(
            @Authenticated(scopes = { OAuth2AuthorizationScopeTypes.TRANSACTIONS_CATEGORIZE })
            @ApiParam(hidden = true) User user,
            @ApiParam(
                    value = "Object holding a list of new categories and the transactions to be categorized",
                    required = true) CategorizeTransactionsListRequest categorizeListRequest
    );

    @PUT
    @Path("{id}/fraudulent")
    @TeamOwnership(Team.DATA)
    @Consumes({ MediaType.APPLICATION_JSON, TinkMediaType.APPLICATION_PROTOBUF })
    @Produces({ MediaType.APPLICATION_JSON, TinkMediaType.APPLICATION_PROTOBUF })
    @ApiOperation(value = "", hidden = true)
    Transaction fraudulent(
            @Authenticated(scopes = { OAuth2AuthorizationScopeTypes.TRANSACTIONS_WRITE })
            @ApiParam(hidden = true) User user,
            @PathParam("id") String id, TransactionFraudulentRequest request
    );

    @POST
    @Path("{id}/link/{counterpartTransactionId}")
    @TeamOwnership(Team.PFM)
    @Consumes({ MediaType.APPLICATION_JSON, TinkMediaType.APPLICATION_PROTOBUF })
    @Produces({ MediaType.APPLICATION_JSON, TinkMediaType.APPLICATION_PROTOBUF })
    @ApiOperation(
            value = "Link two transactions to net out the common disposable amount.",
            // TODO: document the different error scenarios.
            notes = "The transactions are required to have different signs (i.e. one income and one expense). If one transaction is -300 and the other is +100, the common disposable amount is 100.",
            response = LinkTransactionsResponse.class,
            hidden = true
    )
    LinkTransactionsResponse link(
            @Authenticated(scopes = { OAuth2AuthorizationScopeTypes.TRANSACTIONS_WRITE }) @ApiParam(hidden = true) User user,
            @PathParam("id") @ApiParam(value = "The id of one of the transactions to link.", example = "8937fa00166946cbbcbbec569c9d6e90") String id,
            @PathParam("counterpartTransactionId") @ApiParam(value = "The id of the other--the counterpart--transaction to link.", example = "a41ad81ca470cb0bb02d6e8d854a9") String counterpartTransactionId
    );

    @DELETE
    @Path("{id}/part/{partId}")
    @TeamOwnership(Team.DATA)
    @Consumes({ MediaType.APPLICATION_JSON, TinkMediaType.APPLICATION_PROTOBUF })
    @Produces({ MediaType.APPLICATION_JSON, TinkMediaType.APPLICATION_PROTOBUF })
    @ApiOperation(
            value = "Delete a transaction part.",
            // TODO: document the different error scenarios.
            notes = "If the part is linked to another transaction, the bilateral link is removed as well (i.e. the counterpart will be removed too).",
            response = DeleteTransactionPartResponse.class,
            hidden = true
    )
    DeleteTransactionPartResponse deletePart(
            @Authenticated(scopes = { OAuth2AuthorizationScopeTypes.TRANSACTIONS_WRITE }) @ApiParam(hidden = true) User user,
            @PathParam("id") @ApiParam(value = "The id of the transaction to which the part belongs to.", example = "8937fa00166946cbbcbbec569c9d6e90") String id,
            @PathParam("partId") @ApiParam(value = "The part id to delete.", example = "c3b543d4817c4c08a96da789282f0501") String partId);

    @POST
    @Path("{id}/part/{partId}/categorize")
    @TeamOwnership(Team.DATA)
    @Consumes({ MediaType.APPLICATION_JSON, TinkMediaType.APPLICATION_PROTOBUF })
    @Produces({ MediaType.APPLICATION_JSON, TinkMediaType.APPLICATION_PROTOBUF })
    @ApiOperation(
            value = "Categorize a transaction part.",
            response = CategorizeTransactionPartResponse.class,
            hidden = true
    )
    CategorizeTransactionPartResponse categorizePart(
            @Authenticated(scopes = { OAuth2AuthorizationScopeTypes.TRANSACTIONS_CATEGORIZE }) @ApiParam(hidden = true) User user,
            @PathParam("id") @ApiParam(value = "The id of the transaction to which the part belongs to.", example = "8937fa00166946cbbcbbec569c9d6e90") String id,
            @PathParam("partId") @ApiParam(value = "The id of the part to categorize.", example = "c3b543d4817c4c08a96da789282f0501") String partId,
            CategorizeTransactionPartRequest request);

    @GET
    @Path("{id}/link/suggest")
    @TeamOwnership(Team.DATA)
    @Consumes({ MediaType.APPLICATION_JSON, TinkMediaType.APPLICATION_PROTOBUF })
    @Produces({ MediaType.APPLICATION_JSON, TinkMediaType.APPLICATION_PROTOBUF })
    @ApiOperation(
            value = "Get suggestions for potential counterpart expenses for a reimbursement.",
            response = TransactionLinkSuggestionResponse.class,
            hidden = true
    )
    TransactionLinkSuggestionResponse linkSuggest(
            @Authenticated(scopes = { OAuth2AuthorizationScopeTypes.TRANSACTIONS_READ }) @ApiParam(hidden = true) User user,
            @PathParam("id") @ApiParam(value = "The id of the transaction to get suggestions for.", example = "8937fa00166946cbbcbbec569c9d6e90") String id,
            @QueryParam("limit") @ApiParam(value = "Max number of suggestions returned.", example = "5", allowableValues = "Between 0 and 100.", defaultValue = "5") int limit);

    @POST
    @Path("{id}/link/prompt/answer")
    @TeamOwnership(Team.PFM)
    @Consumes({ MediaType.APPLICATION_JSON, TinkMediaType.APPLICATION_PROTOBUF })
    @Produces({ MediaType.APPLICATION_JSON, TinkMediaType.APPLICATION_PROTOBUF })
    @ApiOperation(
            value = "Set the answer for the counterpart link prompt for a transaction.",
            response = TransactionLinkPromptResponse.class,
            hidden = true
    )
    TransactionLinkPromptResponse linkPrompt(
            @Authenticated(scopes = { OAuth2AuthorizationScopeTypes.TRANSACTIONS_CATEGORIZE }) @ApiParam(hidden = true) User user,
            @PathParam("id") @ApiParam(value = "The id of the transaction to which the answer applies.", example = "8937fa00166946cbbcbbec569c9d6e90") String id,
            TransactionLinkPromptRequest request);
}
