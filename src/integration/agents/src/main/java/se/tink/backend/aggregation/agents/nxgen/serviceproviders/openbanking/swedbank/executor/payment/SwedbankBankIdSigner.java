package se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.swedbank.executor.payment;

import java.util.Optional;
import se.tink.backend.aggregation.agents.bankid.status.BankIdStatus;
import se.tink.backend.aggregation.agents.exceptions.AuthenticationException;
import se.tink.backend.aggregation.agents.exceptions.errors.BankIdError;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.swedbank.SwedbankApiClient;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.swedbank.SwedbankConstants.AuthStatus;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.swedbank.authenticator.entities.ChallengeDataEntity;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.swedbank.authenticator.rpc.AuthenticationResponse;
import se.tink.backend.aggregation.nxgen.controllers.payment.PaymentRequest;
import se.tink.backend.aggregation.nxgen.controllers.signing.multifactor.bankid.BankIdSigner;
import se.tink.backend.aggregation.nxgen.http.response.HttpResponseException;

public class SwedbankBankIdSigner implements BankIdSigner<PaymentRequest> {

    private SwedbankApiClient apiClient;
    private AuthenticationResponse authenticationResponse;
    private boolean isMissingExtendedBankId = false;

    public SwedbankBankIdSigner(SwedbankApiClient apiClient) {
        this.apiClient = apiClient;
    }

    @Override
    public BankIdStatus collect(PaymentRequest paymentRequest) throws AuthenticationException {
        final AuthenticationResponse scaResponse;
        try {
            scaResponse = apiClient.getScaResponse(authenticationResponse.getCollectAuthUri());
        } catch (HttpResponseException e) {
            return BankIdStatus.FAILED_UNKNOWN;
        }

        if (scaResponse.isInterrupted()) {
            return BankIdStatus.INTERRUPTED;
        }

        if (scaResponse.isMissingExtendedBankId()) {
            isMissingExtendedBankId = true;
            return BankIdStatus.DONE;
        }

        switch (scaResponse.getScaStatus().toLowerCase()) {
            case AuthStatus.RECEIVED:
            case AuthStatus.STARTED:
                return BankIdStatus.WAITING;
            case AuthStatus.FINALIZED:
                return BankIdStatus.DONE;
            case AuthStatus.FAILED:
                return BankIdStatus.EXPIRED_AUTOSTART_TOKEN;
            default:
                return BankIdStatus.FAILED_UNKNOWN;
        }
    }

    @Override
    public Optional<String> getAutostartToken() {
        return Optional.ofNullable(
                Optional.ofNullable(authenticationResponse.getChallengeData())
                        .map(ChallengeDataEntity::getAutoStartToken)
                        .orElseThrow(BankIdError.UNKNOWN::exception));
    }

    public void setAuthenticationResponse(AuthenticationResponse authenticationResponse) {
        this.authenticationResponse = authenticationResponse;
    }

    public boolean isMissingExtendedBankId() {
        return isMissingExtendedBankId;
    }
}
