package se.tink.backend.aggregation.agents.nxgen.se.openbanking.sebopenbanking.fetcher.transferdestinations;

import java.util.Arrays;
import java.util.Collection;
import java.util.List;
import java.util.stream.Collectors;
import se.tink.backend.agents.rpc.Account;
import se.tink.backend.agents.rpc.AccountTypes;
import se.tink.backend.aggregation.agents.TransferDestinationsResponse;
import se.tink.backend.aggregation.agents.models.TransferDestinationPattern;
import se.tink.backend.aggregation.agents.nxgen.se.openbanking.sebopenbanking.SebApiClient;
import se.tink.backend.aggregation.agents.nxgen.se.openbanking.sebopenbanking.fetcher.transferdestinations.rpc.FetchAccountDetailsResponse;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.sebbase.SebCommonConstants;
import se.tink.backend.aggregation.nxgen.controllers.refresh.transfer.TransferDestinationFetcher;
import se.tink.libraries.account.enums.AccountIdentifierType;

public class SebTransferDestinationFetcher implements TransferDestinationFetcher {

    private final SebApiClient sebApiClient;

    public SebTransferDestinationFetcher(SebApiClient sebApiClient) {
        this.sebApiClient = sebApiClient;
    }

    @Override
    public TransferDestinationsResponse fetchTransferDestinationsFor(Collection<Account> accounts) {
        return new TransferDestinationsResponse(
                accounts.stream()
                        .filter(
                                account ->
                                        (AccountTypes.CHECKING.equals(account.getType())
                                                || AccountTypes.SAVINGS.equals(account.getType())))
                        .collect(
                                Collectors.toMap(
                                        account -> account,
                                        account -> getDestinationPatternsForAccount(account))));
    }

    private List<TransferDestinationPattern> getDestinationPatternsForAccount(Account account) {
        FetchAccountDetailsResponse accountDetailsResponse =
                sebApiClient.fetchAccountDetails(
                        account.getPayload(SebCommonConstants.StorageKeys.ACCOUNT_ID));
        if (accountDetailsResponse.isPaymentService()) {
            return getTransferAndPaymentDestinations();
        } else {
            return getTransferDestinations();
        }
    }

    private List<TransferDestinationPattern> getTransferDestinations() {
        return Arrays.asList(
                TransferDestinationPattern.createForMultiMatchAll(AccountIdentifierType.IBAN),
                TransferDestinationPattern.createForMultiMatchAll(AccountIdentifierType.SE));
    }

    private List<TransferDestinationPattern> getTransferAndPaymentDestinations() {
        return Arrays.asList(
                TransferDestinationPattern.createForMultiMatchAll(AccountIdentifierType.SE_BG),
                TransferDestinationPattern.createForMultiMatchAll(AccountIdentifierType.SE_PG),
                TransferDestinationPattern.createForMultiMatchAll(AccountIdentifierType.IBAN),
                TransferDestinationPattern.createForMultiMatchAll(AccountIdentifierType.SE));
    }
}
