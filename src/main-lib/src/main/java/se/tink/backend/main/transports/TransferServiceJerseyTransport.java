package se.tink.backend.main.transports;

import com.google.inject.Inject;
import java.net.URI;
import java.util.List;
import java.util.NoSuchElementException;
import java.util.Set;
import java.util.function.Supplier;
import javax.ws.rs.Path;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.HttpHeaders;
import javax.ws.rs.core.Response;
import se.tink.backend.api.TransferService;
import se.tink.backend.auth.Authenticated;
import se.tink.backend.auth.AuthenticatedUser;
import se.tink.backend.common.exceptions.DuplicateException;
import se.tink.backend.common.resources.RequestHeaderUtils;
import se.tink.backend.core.enums.TransferType;
import se.tink.backend.core.signableoperation.SignableOperation;
import se.tink.backend.core.transfer.Transfer;
import se.tink.backend.core.transfer.TransferDestination;
import se.tink.backend.main.controllers.TransferServiceController;
import se.tink.backend.main.validators.exception.TransferNotFoundException;
import se.tink.backend.rpc.AccountListResponse;
import se.tink.backend.rpc.ClearingLookupResponse;
import se.tink.backend.rpc.GiroLookupResponse;
import se.tink.backend.rpc.TransferListResponse;
import se.tink.backend.utils.LogUtils;
import se.tink.libraries.account.AccountIdentifier.Type;
import se.tink.libraries.http.utils.HttpResponseHelper;

@Path("/api/v1/transfer")
public class TransferServiceJerseyTransport implements TransferService {

    @Context
    private HttpHeaders headers;

    private final TransferServiceController transferServiceController;

    private HttpResponseHelper httpResponseHelper;
    private static final LogUtils log = new LogUtils(TransferServiceJerseyTransport.class);

    @Inject
    public TransferServiceJerseyTransport(TransferServiceController transferServiceController) {
        this.transferServiceController = transferServiceController;
        this.httpResponseHelper = new HttpResponseHelper(log);
    }

    @Override
    public SignableOperation createTransfer(AuthenticatedUser authenticatedUser, Transfer incomingTransfer) {
        return transferServiceController.create(authenticatedUser.getUser(), incomingTransfer,
                RequestHeaderUtils.getRemoteIp(headers));
    }

    @Override
    public SignableOperation update(AuthenticatedUser authenticatedUser, String id, Transfer incomingTransfer) {
        return processCall(() -> transferServiceController
                .update(authenticatedUser.getUser(), id, incomingTransfer, RequestHeaderUtils.getRemoteIp(headers)));
    }

    @Override
    public Transfer get(AuthenticatedUser authenticatedUser, String id) {
        return processCall(() -> transferServiceController.get(authenticatedUser.getUser(), id));
    }

    @Override
    public SignableOperation getSignableOperation(AuthenticatedUser authenticatedUser, String id) {
        return processCall(() -> transferServiceController.getSignableOperation(authenticatedUser.getUser(), id));
    }

    @Override
    public AccountListResponse getSourceAccountsForTransfer(AuthenticatedUser authenticatedUser, String id) {
        return processCall(
                () -> transferServiceController.getSourceAccountsForTransfer(authenticatedUser.getUser(), id));
    }

    @Override
    public AccountListResponse getSourceAccounts(AuthenticatedUser authenticatedUser,
            Set<Type> explicitTypeFilter, Set<URI> explicitIdentifierFilter) {
        return processCall(() -> transferServiceController
                .getSourceAccounts(authenticatedUser.getUser(), explicitTypeFilter, explicitIdentifierFilter));
    }

    @Override
    public GiroLookupResponse giroLookup(AuthenticatedUser authenticatedUser, String giro) {
        return new GiroLookupResponse(
                processCall(() -> transferServiceController.giroLookup(authenticatedUser.getUser(), giro)));
    }

    @Override
    public TransferListResponse list(AuthenticatedUser authenticatedUser, final TransferType type) {
        List<Transfer> transfers = processCall(() -> transferServiceController.list(authenticatedUser.getUser(), type));
        TransferListResponse response = new TransferListResponse();
        response.setTransfers(transfers);
        return response;
    }

    @Override
    public TransferDestination createDestination(@Authenticated AuthenticatedUser authenticatedUser,
            TransferDestination destination) {
        return processCall(() -> transferServiceController.createDestination(authenticatedUser.getUser(),
                destination.getUri(), destination.getName()));
    }

    @Override
    public ClearingLookupResponse clearingLookup(AuthenticatedUser user, String clearing) {
        return processCall(() -> transferServiceController.clearingLookup(clearing));
    }

    private <T> T processCall(Supplier<T> supplier) {
        try {
            return supplier.get();
        } catch (IllegalArgumentException | TransferNotFoundException e) {
            httpResponseHelper.error(Response.Status.BAD_REQUEST, e.getMessage());
        } catch (IllegalStateException e) {
            httpResponseHelper.error(Response.Status.FORBIDDEN, e.getMessage());
        } catch (NoSuchElementException e) {
            httpResponseHelper.error(Response.Status.NOT_FOUND, e.getMessage());
        } catch (UnsupportedOperationException e) {
            httpResponseHelper.error(Response.Status.SERVICE_UNAVAILABLE, e.getMessage());
        } catch (DuplicateException conflictException) {
            httpResponseHelper.error(Response.Status.CONFLICT, conflictException.getMessage());
        }
        return null;
    }
}
