package se.tink.backend.aggregation.agents.nxgen.se.banks.lansforsakringar.fetcher.transfer;

import java.util.Collection;
import java.util.List;
import java.util.Map;
import se.tink.backend.agents.rpc.Account;
import se.tink.backend.aggregation.agents.TransferDestinationsResponse;
import se.tink.backend.aggregation.agents.general.TransferDestinationPatternBuilder;
import se.tink.backend.aggregation.agents.models.TransferDestinationPattern;
import se.tink.backend.aggregation.agents.nxgen.se.banks.lansforsakringar.LansforsakringarApiClient;
import se.tink.libraries.account.AccountIdentifier.Type;

public class TransferDestinationFetcher
        implements se.tink.backend.aggregation.nxgen.controllers.refresh.transfer
                .TransferDestinationFetcher {

    private final LansforsakringarApiClient apiClient;

    public TransferDestinationFetcher(LansforsakringarApiClient apiClient) {
        this.apiClient = apiClient;
    }

    @Override
    public TransferDestinationsResponse fetchTransferDestinationsFor(Collection<Account> accounts) {
        return TransferDestinationsResponse.builder()
                .addTransferDestinations(getPaymentDestinations(accounts))
                .addTransferDestinations(getTransferDestinations(accounts))
                .build();
    }

    private Map<Account, List<TransferDestinationPattern>> getTransferDestinations(
            Collection<Account> accounts) {
        return null;
    }

    private Map<Account, List<TransferDestinationPattern>> getPaymentDestinations(
            Collection<Account> accounts) {
        return new TransferDestinationPatternBuilder()
                .setSourceAccounts(apiClient.fetchPaymentAccounts().getPaymentAccounts())
                .setDestinationAccounts(apiClient.fetchSavedPaymentRecipients().getRecipients())
                .setTinkAccounts(accounts)
                .addMultiMatchPattern(Type.SE_BG, TransferDestinationPattern.ALL)
                .addMultiMatchPattern(Type.SE_PG, TransferDestinationPattern.ALL)
                .build();
    }
}
