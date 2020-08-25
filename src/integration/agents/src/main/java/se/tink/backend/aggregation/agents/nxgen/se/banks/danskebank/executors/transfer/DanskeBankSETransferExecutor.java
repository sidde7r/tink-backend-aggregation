package se.tink.backend.aggregation.agents.nxgen.se.banks.danskebank.executors.transfer;

import java.util.Optional;
import se.tink.backend.aggregation.agents.exceptions.transfer.TransferExecutionException;
import se.tink.backend.aggregation.agents.nxgen.se.banks.danskebank.DanskeBankSEApiClient;
import se.tink.backend.aggregation.agents.nxgen.se.banks.danskebank.DanskeBankSEConfiguration;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.banks.danskebank.DanskeBankConfiguration;
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
        return Optional.empty();
    }
}
