package se.tink.backend.aggregation.agents.nxgen.serviceproviders.banks.swedbank.fetchers.transferdestination;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import se.tink.backend.agents.rpc.Account;
import se.tink.backend.aggregation.agents.TransferDestinationsResponse;
import se.tink.backend.aggregation.agents.general.TransferDestinationPatternBuilder;
import se.tink.backend.aggregation.agents.models.TransferDestinationPattern;
import se.tink.backend.aggregation.agents.nxgen.se.banks.swedbank.SwedbankSEConstants.StorageKey;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.banks.swedbank.SwedbankDefaultApiClient;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.banks.swedbank.fetchers.transferdestination.rpc.PaymentBaseinfoResponse;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.banks.swedbank.rpc.BankProfile;
import se.tink.backend.aggregation.nxgen.controllers.refresh.transfer.TransferDestinationFetcher;
import se.tink.backend.aggregation.nxgen.storage.SessionStorage;
import se.tink.libraries.account.AccountIdentifier;

public class SwedbankDefaultTransferDestinationFetcher implements TransferDestinationFetcher {

    private final SwedbankDefaultApiClient apiClient;
    private final SessionStorage sessionStorage;
    private Boolean hasExtendedBankId;

    public SwedbankDefaultTransferDestinationFetcher(
            SwedbankDefaultApiClient apiClient, SessionStorage sessionStorage) {
        this.apiClient = apiClient;
        this.sessionStorage = sessionStorage;
    }

    @Override
    public TransferDestinationsResponse fetchTransferDestinationsFor(Collection<Account> accounts) {
        this.hasExtendedBankId =
                sessionStorage.get(StorageKey.HAS_EXTENDED_BANKID, Boolean.class).orElse(false);

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

        TransferDestinationPatternBuilder transferDestinationPatternBuilder =
                new TransferDestinationPatternBuilder()
                        .setSourceAccounts(paymentSourceAccounts)
                        .setDestinationAccounts(paymentDestinationAccounts)
                        .setTinkAccounts(accounts);

        // Only users with extended bankID can make transfers to new recipients. Therefore only
        // adding the generic pattern for users with extended bankID. Already saved recipients
        // are added explicitly when we set destination accounts.
        if (hasExtendedBankId) {
            transferDestinationPatternBuilder.addMultiMatchPattern(
                    AccountIdentifier.Type.SE_BG, TransferDestinationPattern.ALL);
            transferDestinationPatternBuilder.addMultiMatchPattern(
                    AccountIdentifier.Type.SE_PG, TransferDestinationPattern.ALL);
        }

        return transferDestinationPatternBuilder.build();
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

        TransferDestinationPatternBuilder transferDestinationPatternBuilder =
                new TransferDestinationPatternBuilder()
                        .setSourceAccounts(transferSourceAccounts)
                        .setDestinationAccounts(transferDestinationAccounts)
                        .setTinkAccounts(accounts);

        // Only users with extended bankID can make transfers to new recipients. Therefore only
        // adding the generic pattern for users with extended bankID. Already saved recipients
        // are added explicitly when we set destination accounts.
        if (hasExtendedBankId) {
            transferDestinationPatternBuilder.addMultiMatchPattern(
                    AccountIdentifier.Type.SE, TransferDestinationPattern.ALL);
        }

        return transferDestinationPatternBuilder.build();
    }
}
