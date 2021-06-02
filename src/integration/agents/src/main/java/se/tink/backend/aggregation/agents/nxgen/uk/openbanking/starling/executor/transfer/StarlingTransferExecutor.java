package se.tink.backend.aggregation.agents.nxgen.uk.openbanking.starling.executor.transfer;

import java.util.Optional;
import se.tink.backend.agents.rpc.Credentials;
import se.tink.backend.aggregation.agents.exceptions.transfer.TransferExecutionException;
import se.tink.backend.aggregation.agents.nxgen.uk.openbanking.starling.StarlingApiClient;
import se.tink.backend.aggregation.annotations.JsonObject;
import se.tink.backend.aggregation.nxgen.controllers.authentication.utils.StrongAuthenticationState;
import se.tink.backend.aggregation.nxgen.controllers.transfer.BankTransferExecutor;
import se.tink.backend.aggregation.nxgen.controllers.utils.SupplementalInformationHelper;
import se.tink.backend.aggregation.nxgen.exceptions.NotImplementedException;
import se.tink.libraries.transfer.rpc.Transfer;

@JsonObject
public class StarlingTransferExecutor implements BankTransferExecutor {

    public static final String COUNTRY_CODE = "GB";
    public static final String INDIVIDUAL = "INDIVIDUAL";

    private final StarlingApiClient apiClient;
    private final String redirectUrl;
    private final SupplementalInformationHelper supplementalInformationHelper;
    private final Credentials credentials;
    private final StrongAuthenticationState strongAuthenticationState;

    public StarlingTransferExecutor(
            StarlingApiClient apiClient,
            String redirectUrl,
            Credentials credentials,
            StrongAuthenticationState strongAuthenticationState,
            SupplementalInformationHelper supplementalInformationHelper) {
        this.apiClient = apiClient;
        this.redirectUrl = redirectUrl;
        this.credentials = credentials;
        this.strongAuthenticationState = strongAuthenticationState;
        this.supplementalInformationHelper = supplementalInformationHelper;
    }

    @Override
    public Optional<String> executeTransfer(Transfer transfer) throws TransferExecutionException {
        throw new NotImplementedException("Payments and transfers not implemented");
    }
}
