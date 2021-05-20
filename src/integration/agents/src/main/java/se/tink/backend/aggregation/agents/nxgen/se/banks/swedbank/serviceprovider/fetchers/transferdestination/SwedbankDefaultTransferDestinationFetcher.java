package se.tink.backend.aggregation.agents.nxgen.se.banks.swedbank.serviceprovider.fetchers.transferdestination;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import se.tink.backend.agents.rpc.Account;
import se.tink.backend.aggregation.agents.TransferDestinationsResponse;
import se.tink.backend.aggregation.agents.general.TransferDestinationPatternBuilder;
import se.tink.backend.aggregation.agents.models.TransferDestinationPattern;
import se.tink.backend.aggregation.agents.nxgen.se.banks.swedbank.serviceprovider.SwedbankDefaultApiClient;
import se.tink.backend.aggregation.agents.nxgen.se.banks.swedbank.serviceprovider.SwedbankStorage;
import se.tink.backend.aggregation.agents.nxgen.se.banks.swedbank.serviceprovider.fetchers.transferdestination.rpc.PaymentBaseinfoResponse;
import se.tink.backend.aggregation.agents.nxgen.se.banks.swedbank.serviceprovider.rpc.BankProfile;
import se.tink.backend.aggregation.nxgen.controllers.refresh.transfer.TransferDestinationFetcher;
import se.tink.libraries.account.enums.AccountIdentifierType;

public class SwedbankDefaultTransferDestinationFetcher implements TransferDestinationFetcher {

    private final SwedbankDefaultApiClient apiClient;
    private final SwedbankStorage swedbankStorage;

    public SwedbankDefaultTransferDestinationFetcher(
            SwedbankDefaultApiClient apiClient, SwedbankStorage swedbankStorage) {
        this.apiClient = apiClient;
        this.swedbankStorage = swedbankStorage;
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
            PaymentBaseinfoResponse paymentBaseinfoResponse =
                    swedbankStorage.getBankProfileHandler().getActivePaymentBaseInfo();

            paymentSourceAccounts.addAll(paymentBaseinfoResponse.getPaymentSourceAccounts());
            paymentDestinationAccounts.addAll(paymentBaseinfoResponse.getPaymentDestinations());
        }

        TransferDestinationPatternBuilder transferDestinationPatternBuilder =
                new TransferDestinationPatternBuilder()
                        .setSourceAccounts(paymentSourceAccounts)
                        .setDestinationAccounts(paymentDestinationAccounts)
                        .setTinkAccounts(accounts)
                        .addMultiMatchPattern(
                                AccountIdentifierType.SE_BG, TransferDestinationPattern.ALL)
                        .addMultiMatchPattern(
                                AccountIdentifierType.SE_PG, TransferDestinationPattern.ALL);

        return transferDestinationPatternBuilder.build();
    }

    private Map<Account, List<TransferDestinationPattern>> getTransferDestinations(
            Collection<Account> accounts) {
        List transferSourceAccounts = new ArrayList<>();
        List transferDestinationAccounts = new ArrayList<>();

        for (BankProfile bankProfile : apiClient.getBankProfiles()) {
            apiClient.selectProfile(bankProfile);
            PaymentBaseinfoResponse paymentBaseinfoResponse =
                    swedbankStorage.getBankProfileHandler().getActivePaymentBaseInfo();

            transferSourceAccounts.addAll(paymentBaseinfoResponse.getTransferSourceAccounts());
            transferDestinationAccounts.addAll(paymentBaseinfoResponse.getTransferDestinations());
        }

        TransferDestinationPatternBuilder transferDestinationPatternBuilder =
                new TransferDestinationPatternBuilder()
                        .setSourceAccounts(transferSourceAccounts)
                        .setDestinationAccounts(transferDestinationAccounts)
                        .setTinkAccounts(accounts)
                        .addMultiMatchPattern(
                                AccountIdentifierType.SE, TransferDestinationPattern.ALL);

        return transferDestinationPatternBuilder.build();
    }
}
