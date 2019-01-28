package se.tink.backend.aggregation.agents.nxgen.se.banks.handelsbanken.fetcher.transferdestination;

import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import se.tink.backend.aggregation.agents.TransferDestinationsResponse;
import se.tink.backend.aggregation.agents.general.TransferDestinationPatternBuilder;
import se.tink.backend.aggregation.agents.nxgen.se.banks.handelsbanken.HandelsbankenSEApiClient;
import se.tink.backend.aggregation.agents.nxgen.se.banks.handelsbanken.HandelsbankenSEConstants;
import se.tink.backend.aggregation.agents.nxgen.se.banks.handelsbanken.executor.transfer.rpc.HandelsbankenSETransferContext;
import se.tink.backend.aggregation.agents.nxgen.se.banks.handelsbanken.rpc.HandelsbankenSEPaymentContext;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.banks.handelsbanken.HandelsbankenSessionStorage;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.banks.handelsbanken.authenticator.rpc.ApplicationEntryPointResponse;
import se.tink.backend.aggregation.log.AggregationLogger;
import se.tink.backend.aggregation.nxgen.controllers.refresh.transfer.TransferDestinationFetcher;
import se.tink.backend.aggregation.nxgen.http.exceptions.HttpResponseException;
import se.tink.backend.aggregation.rpc.Account;
import se.tink.backend.aggregation.agents.models.TransferDestinationPattern;
import se.tink.libraries.account.AccountIdentifier;
import se.tink.libraries.account.identifiers.SwedishSHBInternalIdentifier;

public class HandelsbankenSETransferDestinationFetcher implements TransferDestinationFetcher {

    private static final AggregationLogger LOGGER =
            new AggregationLogger(HandelsbankenSETransferDestinationFetcher.class);

    private final HandelsbankenSEApiClient client;
    private final HandelsbankenSessionStorage sessionStorage;

    public HandelsbankenSETransferDestinationFetcher(HandelsbankenSEApiClient client,
            HandelsbankenSessionStorage sessionStorage) {
        this.client = client;
        this.sessionStorage = sessionStorage;
    }

    @Override
    public TransferDestinationsResponse fetchTransferDestinationsFor(Collection<Account> accounts) {
        return sessionStorage.applicationEntryPoint().map(applicationEntryPoint -> {
            TransferDestinationsResponse transferDestinations = new TransferDestinationsResponse();
            Map<Account, List<TransferDestinationPattern>> bankTransferAccountDestinations = getBankTransferAccountDestinations(
                    accounts, applicationEntryPoint);
            transferDestinations.addDestinations(bankTransferAccountDestinations);
            transferDestinations.addDestinations(getPaymentAccountDestinations(accounts, applicationEntryPoint));
            return transferDestinations;
        }).orElseGet(TransferDestinationsResponse::new);
    }

    private Map<Account, List<TransferDestinationPattern>> getBankTransferAccountDestinations(
            Collection<Account> accounts,
            ApplicationEntryPointResponse applicationEntryPoint) {
        HandelsbankenSETransferContext transferContext = client.transferContext(applicationEntryPoint);

        return new TransferDestinationPatternBuilder()
                .setSourceAccounts(transferContext.retrieveOwnedSourceAccounts())
                .setDestinationAccounts(transferContext.retrieveDestinationAccounts())
                .setTinkAccounts(accounts)
                .addMultiMatchPattern(AccountIdentifier.Type.SE, TransferDestinationPattern.ALL)
                .matchDestinationAccountsOn(AccountIdentifier.Type.SE_SHB_INTERNAL, SwedishSHBInternalIdentifier.class)
                .build();
    }

    private Map<Account, List<TransferDestinationPattern>> getPaymentAccountDestinations(
            Collection<Account> accounts,
            ApplicationEntryPointResponse applicationEntryPoint) {
        return fetchPaymentContext(applicationEntryPoint).map(paymentContext ->
                new TransferDestinationPatternBuilder()
                        .setSourceAccounts(paymentContext.retrieveOwnedSourceAccounts())
                        .setTinkAccounts(accounts)
                        .setDestinationAccounts(paymentContext.retrieveDestinationAccounts())
                        .addMultiMatchPattern(AccountIdentifier.Type.SE_PG, TransferDestinationPattern.ALL)
                        .addMultiMatchPattern(AccountIdentifier.Type.SE_BG, TransferDestinationPattern.ALL)
                        .matchDestinationAccountsOn(AccountIdentifier.Type.SE_SHB_INTERNAL,
                                SwedishSHBInternalIdentifier.class)
                        .build()
        ).orElseGet(Collections::emptyMap);
    }

    private Optional<HandelsbankenSEPaymentContext> fetchPaymentContext(
            ApplicationEntryPointResponse applicationEntryPoint) {
        try {
            return Optional.of(client.paymentContext(applicationEntryPoint));
        } catch (HttpResponseException e) {
            HandelsbankenSEPaymentContext.Failure failure = e.getResponse()
                    .getBody(HandelsbankenSEPaymentContext.Failure.class);
            if (!failure.customerIsUnder16()) {
                LOGGER.error(HandelsbankenSEConstants.Fetcher.Transfers.LOG_TAG +
                        " - unable to fetch payment context - " + failure);
            }
            return Optional.empty();
        }
    }
}
