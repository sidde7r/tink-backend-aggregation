package se.tink.backend.aggregation.agents.nxgen.serviceproviders.banks.swedbank.fetchers.transferdestination;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import se.tink.backend.agents.rpc.Account;
import se.tink.backend.aggregation.agents.TransferDestinationsResponse;
import se.tink.backend.aggregation.agents.general.TransferDestinationPatternBuilder;
import se.tink.backend.aggregation.agents.models.TransferDestinationPattern;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.banks.swedbank.SwedbankDefaultApiClient;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.banks.swedbank.fetchers.transferdestination.rpc.PaymentBaseinfoResponse;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.banks.swedbank.rpc.BankProfile;
import se.tink.backend.aggregation.nxgen.controllers.refresh.transfer.TransferDestinationFetcher;
import se.tink.libraries.account.AccountIdentifier;

public class SwedbankDefaultTransferDestinationFetcher implements TransferDestinationFetcher {
    private static final Logger log =
            LoggerFactory.getLogger(SwedbankDefaultTransferDestinationFetcher.class);
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

    private Map<Account, List<TransferDestinationPattern>> getPaymentDestinations(
            Collection<Account> accounts) {
        List paymentSourceAccounts = new ArrayList<>();
        List paymentDestinationAccounts = new ArrayList<>();

        for (BankProfile bankProfile : apiClient.getBankProfiles()) {
            apiClient.selectProfile(bankProfile);
            PaymentBaseinfoResponse paymentBaseinfoResponse = apiClient.paymentBaseinfo();

            paymentSourceAccounts.addAll(paymentBaseinfoResponse.getPaymentSourceAccounts());
            paymentDestinationAccounts.addAll(paymentBaseinfoResponse.getPaymentDestinations());
        }

        return new TransferDestinationPatternBuilder()
                .setSourceAccounts(paymentSourceAccounts)
                .setDestinationAccounts(paymentDestinationAccounts)
                .setTinkAccounts(accounts)
                .addMultiMatchPattern(AccountIdentifier.Type.SE_BG, TransferDestinationPattern.ALL)
                .addMultiMatchPattern(AccountIdentifier.Type.SE_PG, TransferDestinationPattern.ALL)
                .build();
    }

    private Map<Account, List<TransferDestinationPattern>> getTransferDestinations(
            Collection<Account> accounts) {
        List transferSourceAccounts = new ArrayList<>();
        List transferDestinationAccounts = new ArrayList<>();

        for (BankProfile bankProfile : apiClient.getBankProfiles()) {
            apiClient.selectProfile(bankProfile);
            PaymentBaseinfoResponse paymentBaseinfoResponse = apiClient.paymentBaseinfo();

            transferSourceAccounts.addAll(paymentBaseinfoResponse.getTransferSourceAccounts());
            transferDestinationAccounts.addAll(paymentBaseinfoResponse.getTransferDestinations());
        }

        return new TransferDestinationPatternBuilder()
                .setSourceAccounts(transferSourceAccounts)
                .setDestinationAccounts(transferDestinationAccounts)
                .setTinkAccounts(accounts)
                .addMultiMatchPattern(AccountIdentifier.Type.SE, TransferDestinationPattern.ALL)
                .build();
    }
}
