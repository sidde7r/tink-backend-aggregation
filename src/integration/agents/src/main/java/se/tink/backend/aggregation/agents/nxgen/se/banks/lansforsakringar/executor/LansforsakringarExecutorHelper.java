package se.tink.backend.aggregation.agents.nxgen.se.banks.lansforsakringar.executor;

import com.google.common.base.Objects;
import se.tink.backend.aggregation.agents.contexts.SupplementalRequester;
import se.tink.backend.aggregation.agents.nxgen.se.banks.lansforsakringar.LansforsakringarApiClient;
import se.tink.backend.aggregation.agents.nxgen.se.banks.lansforsakringar.executor.rpc.DirectTransferRequest;
import se.tink.libraries.account.AccountIdentifier;
import se.tink.libraries.account.AccountIdentifier.Type;
import se.tink.libraries.i18n.Catalog;
import se.tink.libraries.transfer.rpc.Transfer;

public class LansforsakringarExecutorHelper {

    private final LansforsakringarApiClient apiClient;
    private final SupplementalRequester supplementalRequester;
    private final Catalog catalog;

    public LansforsakringarExecutorHelper(
            LansforsakringarApiClient apiClient,
            SupplementalRequester supplementalRequester,
            Catalog catalog) {
        this.apiClient = apiClient;
        this.supplementalRequester = supplementalRequester;
        this.catalog = catalog;
    }

    boolean isSourceAccountValid(Transfer transfer) {
        AccountIdentifier sourceAccount = transfer.getSource();
        return apiClient.fetchTransferSourceAccounts().getAccounts().stream()
                .anyMatch(
                        acc ->
                                Objects.equal(
                                        acc.getAccountNumber(), sourceAccount.getIdentifier()));
    }

    boolean isDestinationValid(Transfer transfer) {
        return transfer.getDestination().is(Type.SE);
    }

    // No bankId signature needed
    boolean isInternalTransfer(Transfer transfer) {
        AccountIdentifier destinationAccount = transfer.getDestination();
        return apiClient.fetchTransferDestinationAccounts().getAccounts().stream()
                .anyMatch(
                        acc ->
                                Objects.equal(
                                        acc.getAccountNumber(),
                                        destinationAccount.getIdentifier()));
    }

    DirectTransferRequest createTransferRequest(Transfer transfer) {
        return new DirectTransferRequest(
                transfer.getRemittanceInformation().getValue(),
                transfer.getSource().getIdentifier(),
                transfer.getAmount().toBigDecimal(),
                transfer.getDestination().getIdentifier(),
                transfer.getSourceMessage());
    }
}
