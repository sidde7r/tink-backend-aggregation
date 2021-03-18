package se.tink.backend.aggregation.agents.nxgen.se.banks.handelsbanken.fetcher.transferdestination;

import java.lang.invoke.MethodHandles;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import se.tink.backend.agents.rpc.Account;
import se.tink.backend.aggregation.agents.TransferDestinationsResponse;
import se.tink.backend.aggregation.agents.general.TransferDestinationPatternBuilder;
import se.tink.backend.aggregation.agents.models.TransferDestinationPattern;
import se.tink.backend.aggregation.agents.nxgen.se.banks.handelsbanken.HandelsbankenSEApiClient;
import se.tink.backend.aggregation.agents.nxgen.se.banks.handelsbanken.HandelsbankenSEConstants;
import se.tink.backend.aggregation.agents.nxgen.se.banks.handelsbanken.executor.transfer.rpc.HandelsbankenSETransferContext;
import se.tink.backend.aggregation.agents.nxgen.se.banks.handelsbanken.rpc.Failure;
import se.tink.backend.aggregation.agents.nxgen.se.banks.handelsbanken.rpc.HandelsbankenSEPaymentContext;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.banks.handelsbanken.HandelsbankenSessionStorage;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.banks.handelsbanken.authenticator.rpc.ApplicationEntryPointResponse;
import se.tink.backend.aggregation.nxgen.controllers.refresh.transfer.TransferDestinationFetcher;
import se.tink.backend.aggregation.nxgen.http.response.HttpResponseException;
import se.tink.libraries.account.enums.AccountIdentifierType;
import se.tink.libraries.account.identifiers.SwedishSHBInternalIdentifier;

public class HandelsbankenSETransferDestinationFetcher implements TransferDestinationFetcher {
    private static final Logger logger =
            LoggerFactory.getLogger(MethodHandles.lookup().lookupClass());

    private final HandelsbankenSEApiClient client;
    private final HandelsbankenSessionStorage sessionStorage;

    public HandelsbankenSETransferDestinationFetcher(
            HandelsbankenSEApiClient client, HandelsbankenSessionStorage sessionStorage) {
        this.client = client;
        this.sessionStorage = sessionStorage;
    }

    @Override
    public TransferDestinationsResponse fetchTransferDestinationsFor(Collection<Account> accounts) {
        return sessionStorage
                .applicationEntryPoint()
                .map(
                        applicationEntryPoint -> {
                            TransferDestinationsResponse transferDestinations =
                                    new TransferDestinationsResponse();
                            Map<Account, List<TransferDestinationPattern>>
                                    bankTransferAccountDestinations =
                                            getBankTransferAccountDestinations(
                                                    accounts, applicationEntryPoint);
                            transferDestinations.addDestinations(bankTransferAccountDestinations);
                            transferDestinations.addDestinations(
                                    getPaymentAccountDestinations(accounts, applicationEntryPoint));
                            return transferDestinations;
                        })
                .orElseGet(TransferDestinationsResponse::new);
    }

    private Map<Account, List<TransferDestinationPattern>> getBankTransferAccountDestinations(
            Collection<Account> accounts, ApplicationEntryPointResponse applicationEntryPoint) {
        HandelsbankenSETransferContext transferContext =
                client.transferContext(applicationEntryPoint);

        return new TransferDestinationPatternBuilder()
                .setSourceAccounts(transferContext.retrieveOwnedSourceAccounts())
                .setDestinationAccounts(transferContext.retrieveDestinationAccounts())
                .setTinkAccounts(accounts)
                .addMultiMatchPattern(AccountIdentifierType.SE, TransferDestinationPattern.ALL)
                .matchDestinationAccountsOn(
                        AccountIdentifierType.SE_SHB_INTERNAL, SwedishSHBInternalIdentifier.class)
                .build();
    }

    private Map<Account, List<TransferDestinationPattern>> getPaymentAccountDestinations(
            Collection<Account> accounts, ApplicationEntryPointResponse applicationEntryPoint) {
        return fetchPaymentContext(applicationEntryPoint)
                .map(
                        paymentContext ->
                                new TransferDestinationPatternBuilder()
                                        .setSourceAccounts(
                                                paymentContext.retrieveOwnedSourceAccounts())
                                        .setTinkAccounts(accounts)
                                        .setDestinationAccounts(
                                                paymentContext.retrieveDestinationAccounts())
                                        .addMultiMatchPattern(
                                                AccountIdentifierType.SE_PG,
                                                TransferDestinationPattern.ALL)
                                        .addMultiMatchPattern(
                                                AccountIdentifierType.SE_BG,
                                                TransferDestinationPattern.ALL)
                                        .matchDestinationAccountsOn(
                                                AccountIdentifierType.SE_SHB_INTERNAL,
                                                SwedishSHBInternalIdentifier.class)
                                        .build())
                .orElseGet(Collections::emptyMap);
    }

    private Optional<HandelsbankenSEPaymentContext> fetchPaymentContext(
            ApplicationEntryPointResponse applicationEntryPoint) {
        try {
            return Optional.of(client.paymentContext(applicationEntryPoint));
        } catch (HttpResponseException e) {
            Failure failure = e.getResponse().getBody(Failure.class);
            if (!failure.customerIsUnder16()) {
                logger.error(
                        HandelsbankenSEConstants.Transfers.LOG_TAG
                                + " - unable to fetch payment context - "
                                + failure,
                        e);
            }
            return Optional.empty();
        }
    }
}
