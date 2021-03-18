package se.tink.backend.aggregation.agents.nxgen.se.banks.lansforsakringar.fetcher.transfer;

import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import se.tink.backend.agents.rpc.Account;
import se.tink.backend.aggregation.agents.TransferDestinationsResponse;
import se.tink.backend.aggregation.agents.general.TransferDestinationPatternBuilder;
import se.tink.backend.aggregation.agents.models.TransferDestinationPattern;
import se.tink.backend.aggregation.agents.nxgen.se.banks.lansforsakringar.LansforsakringarApiClient;
import se.tink.backend.aggregation.nxgen.controllers.refresh.transfer.TransferDestinationFetcher;
import se.tink.backend.aggregation.nxgen.http.response.HttpResponseException;
import se.tink.libraries.account.enums.AccountIdentifierType;

public class LansforsakringarTransferDestinationFetcher implements TransferDestinationFetcher {

    private final LansforsakringarApiClient apiClient;

    public LansforsakringarTransferDestinationFetcher(LansforsakringarApiClient apiClient) {
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
        try {
            return new TransferDestinationPatternBuilder()
                    .setSourceAccounts(apiClient.fetchTransferSourceAccounts().getAccounts())
                    .setDestinationAccounts(
                            apiClient.fetchSavedTransferDestinationAccounts().getAccounts())
                    .setTinkAccounts(accounts)
                    .addMultiMatchPattern(AccountIdentifierType.SE, TransferDestinationPattern.ALL)
                    .build();
        } catch (HttpResponseException e) {
            return Collections.emptyMap();
        }
    }

    private Map<Account, List<TransferDestinationPattern>> getPaymentDestinations(
            Collection<Account> accounts) {
        try {
            return new TransferDestinationPatternBuilder()
                    .setSourceAccounts(apiClient.fetchPaymentAccounts().getPaymentAccounts())
                    .setDestinationAccounts(apiClient.fetchSavedPaymentRecipients().getRecipients())
                    .setTinkAccounts(accounts)
                    .addMultiMatchPattern(
                            AccountIdentifierType.SE_BG, TransferDestinationPattern.ALL)
                    .addMultiMatchPattern(
                            AccountIdentifierType.SE_PG, TransferDestinationPattern.ALL)
                    .build();
        } catch (HttpResponseException e) {
            return Collections.emptyMap();
        }
    }
}
