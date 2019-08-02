package se.tink.backend.aggregation.agents.nxgen.de.banks.fints.accounts.checking;

import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import se.tink.backend.agents.rpc.Account;
import se.tink.backend.aggregation.agents.TransferDestinationsResponse;
import se.tink.backend.aggregation.agents.general.TransferDestinationPatternBuilder;
import se.tink.backend.aggregation.agents.models.TransferDestinationPattern;
import se.tink.backend.aggregation.agents.nxgen.de.banks.fints.FinTsApiClient;
import se.tink.backend.aggregation.agents.nxgen.de.banks.fints.segments.accounts.SEPAAccount;
import se.tink.backend.aggregation.nxgen.controllers.refresh.transfer.TransferDestinationFetcher;
import se.tink.libraries.account.AccountIdentifier;

public class FinTsTransferDestinationFetcher implements TransferDestinationFetcher {

    private final FinTsApiClient apiClient;

    public FinTsTransferDestinationFetcher(FinTsApiClient apiClient) {
        this.apiClient = apiClient;
    }

    @Override
    public TransferDestinationsResponse fetchTransferDestinationsFor(Collection<Account> accounts) {
        return TransferDestinationsResponse.builder()
                .addTransferDestinations(getPaymentDestinations(accounts))
                .addTransferDestinations(getTransferDestinations(accounts))
                .build();
    }

    private Map<Account, List<TransferDestinationPattern>> getPaymentDestinations(
            Collection<Account> accounts) {
        List paymentAccounts =
                apiClient.getSepaAccounts().stream()
                        .filter(SEPAAccount::canMakePayment)
                        .collect(Collectors.toList());

        return new TransferDestinationPatternBuilder()
                .setSourceAccounts(paymentAccounts)
                .setDestinationAccounts(paymentAccounts)
                .setTinkAccounts(accounts)
                .addMultiMatchPattern(AccountIdentifier.Type.IBAN, TransferDestinationPattern.ALL)
                .build();
    }

    private Map<Account, List<TransferDestinationPattern>> getTransferDestinations(
            Collection<Account> accounts) {
        List transferAccounts =
                apiClient.getSepaAccounts().stream()
                        .filter(SEPAAccount::canMakeTransfer)
                        .collect(Collectors.toList());

        return new TransferDestinationPatternBuilder()
                .setSourceAccounts(transferAccounts)
                .setDestinationAccounts(transferAccounts)
                .setTinkAccounts(accounts)
                .addMultiMatchPattern(AccountIdentifier.Type.IBAN, TransferDestinationPattern.ALL)
                .build();
    }
}
