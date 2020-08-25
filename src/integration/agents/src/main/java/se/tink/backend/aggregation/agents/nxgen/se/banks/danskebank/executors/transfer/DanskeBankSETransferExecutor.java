package se.tink.backend.aggregation.agents.nxgen.se.banks.danskebank.executors.transfer;

import java.util.Optional;
import se.tink.backend.aggregation.agents.exceptions.transfer.TransferExecutionException;
import se.tink.backend.aggregation.agents.nxgen.se.banks.danskebank.DanskeBankSEApiClient;
import se.tink.backend.aggregation.agents.nxgen.se.banks.danskebank.DanskeBankSEConfiguration;
import se.tink.backend.aggregation.agents.nxgen.se.banks.danskebank.executors.rpc.CreditorRequest;
import se.tink.backend.aggregation.agents.nxgen.se.banks.danskebank.executors.rpc.CreditorResponse;
import se.tink.backend.aggregation.agents.nxgen.se.banks.danskebank.executors.rpc.ListPayeesRequest;
import se.tink.backend.aggregation.agents.nxgen.se.banks.danskebank.executors.rpc.ListPayeesResponse;
import se.tink.backend.aggregation.agents.nxgen.se.banks.danskebank.executors.rpc.ValidatePaymentDateRequest;
import se.tink.backend.aggregation.agents.nxgen.se.banks.danskebank.executors.rpc.ValidatePaymentDateResponse;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.banks.danskebank.DanskeBankConfiguration;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.banks.danskebank.fetchers.rpc.ListAccountsRequest;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.banks.danskebank.fetchers.rpc.ListAccountsResponse;
import se.tink.backend.aggregation.nxgen.controllers.transfer.BankTransferExecutor;
import se.tink.libraries.transfer.rpc.Transfer;

public class DanskeBankSETransferExecutor implements BankTransferExecutor {

    private final DanskeBankSEApiClient apiClient;
    private final String deviceId;
    private final DanskeBankSEConfiguration configuration;

    private String dynamicBankIdJavascript;

    public DanskeBankSETransferExecutor(
            DanskeBankSEApiClient apiClient,
            String deviceId,
            DanskeBankConfiguration configuration) {
        this.apiClient = apiClient;
        this.deviceId = deviceId;
        this.configuration = (DanskeBankSEConfiguration) configuration;
    }

    @Override
    public Optional<String> executeTransfer(Transfer transfer) throws TransferExecutionException {
        ListAccountsResponse accounts =
                apiClient.listAccounts(
                        ListAccountsRequest.createFromLanguageCode(
                                configuration.getLanguageCode()));

        ListPayeesResponse listPayees =
                apiClient.listPayees(ListPayeesRequest.create(configuration.getLanguageCode()));

        CreditorResponse creditorName =
                apiClient.creditorName(
                        CreditorRequest.create(
                                transfer.getDestination().getIdentifier(),
                                configuration.getMarketCode()));

        CreditorResponse creditorBankName =
                apiClient.creditorBankName(
                        CreditorRequest.create(
                                transfer.getDestination().getIdentifier(),
                                configuration.getMarketCode()));
        validatePaymentDate(transfer, accounts);

        return Optional.empty();
    }

    private void validatePaymentDate(Transfer transfer, ListAccountsResponse accounts) {
        String transferType =
                accounts.isInternalAccount(transfer.getDestination().getIdentifier())
                        ? "internal"
                        : "external";

        ValidatePaymentDateRequest paymentDateRequest =
                new ValidatePaymentDateRequest(
                        transfer.getDueDate(),
                        configuration.getMarketCode(),
                        false,
                        "",
                        transfer.getDestination().getIdentifier(),
                        transferType);

        ValidatePaymentDateResponse paymentDateResponse =
                apiClient.validatePaymentDate(paymentDateRequest);
    }
}
