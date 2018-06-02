package se.tink.backend.aggregation.agents.nxgen.serviceproviders.banks.swedbank.fetchers.transferdestination;

import java.util.Collection;
import java.util.List;
import java.util.Map;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import se.tink.backend.aggregation.agents.TransferDestinationsResponse;
import se.tink.backend.aggregation.agents.general.TransferDestinationPatternBuilder;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.banks.swedbank.SwedbankDefaultApiClient;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.banks.swedbank.fetchers.transferdestination.rpc.PaymentBaseinfoResponse;
import se.tink.backend.aggregation.nxgen.controllers.refresh.transfer.TransferDestinationFetcher;
import se.tink.backend.aggregation.rpc.Account;
import se.tink.backend.core.account.TransferDestinationPattern;
import se.tink.libraries.account.AccountIdentifier;

public class SwedbankDefaultTransferDestinationFetcher implements TransferDestinationFetcher {
    private static final Logger log = LoggerFactory.getLogger(SwedbankDefaultTransferDestinationFetcher.class);
    private final SwedbankDefaultApiClient apiClient;

    public SwedbankDefaultTransferDestinationFetcher(SwedbankDefaultApiClient apiClient) {
        this.apiClient = apiClient;
    }

    @Override
    public TransferDestinationsResponse fetchTransferDestinationsFor(Collection<Account> accounts) {
        return TransferDestinationsResponse.builder()
                .addTransferDestinations(getPaymentDestinations(accounts))
                .addTransferDestinations(getTransferDestinations(accounts))
                .build();
    }

    private Map<Account, List<TransferDestinationPattern>> getPaymentDestinations(Collection<Account> accounts) {
        PaymentBaseinfoResponse paymentBaseinfoResponse = apiClient.paymentBaseinfo();

        return new TransferDestinationPatternBuilder()
                .setSourceAccounts(paymentBaseinfoResponse.getPaymentSourceAccounts())
                .setDestinationAccounts(paymentBaseinfoResponse.getPaymentDestinations())
                .setTinkAccounts(accounts)
                .addMultiMatchPattern(AccountIdentifier.Type.SE_BG, TransferDestinationPattern.ALL)
                .addMultiMatchPattern(AccountIdentifier.Type.SE_PG, TransferDestinationPattern.ALL)
                .build();
    }

    private Map<Account, List<TransferDestinationPattern>> getTransferDestinations(Collection<Account> accounts) {
        PaymentBaseinfoResponse paymentBaseinfoResponse = apiClient.paymentBaseinfo();

        return new TransferDestinationPatternBuilder()
                .setSourceAccounts(paymentBaseinfoResponse.getTransferSourceAccounts())
                .setDestinationAccounts(paymentBaseinfoResponse.getTransferDestinations())
                .setTinkAccounts(accounts)
                .addMultiMatchPattern(AccountIdentifier.Type.SE, TransferDestinationPattern.ALL)
                .build();
    }
}
