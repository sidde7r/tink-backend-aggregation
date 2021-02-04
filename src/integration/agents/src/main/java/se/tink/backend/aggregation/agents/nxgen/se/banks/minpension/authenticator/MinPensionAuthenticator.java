package se.tink.backend.aggregation.agents.nxgen.se.banks.minpension.authenticator;

import java.util.Optional;
import lombok.RequiredArgsConstructor;
import se.tink.backend.aggregation.agents.bankid.status.BankIdStatus;
import se.tink.backend.aggregation.agents.exceptions.AuthenticationException;
import se.tink.backend.aggregation.agents.exceptions.AuthorizationException;
import se.tink.backend.aggregation.agents.exceptions.bankservice.BankServiceException;
import se.tink.backend.aggregation.agents.exceptions.errors.AuthorizationError;
import se.tink.backend.aggregation.agents.nxgen.se.banks.minpension.MinPensionApiClient;
import se.tink.backend.aggregation.agents.nxgen.se.banks.minpension.MinPensionConstants;
import se.tink.backend.aggregation.agents.nxgen.se.banks.minpension.MinPensionConstants.ErrorMessage;
import se.tink.backend.aggregation.agents.nxgen.se.banks.minpension.MinPensionConstants.StorageKeys;
import se.tink.backend.aggregation.agents.nxgen.se.banks.minpension.authenticator.rpc.InitBankIdResponse;
import se.tink.backend.aggregation.agents.nxgen.se.banks.minpension.authenticator.rpc.PollBankIdResponse;
import se.tink.backend.aggregation.agents.nxgen.se.banks.minpension.authenticator.rpc.UserTOCResponse;
import se.tink.backend.aggregation.nxgen.controllers.authentication.multifactor.bankid.BankIdAuthenticator;
import se.tink.backend.aggregation.nxgen.storage.SessionStorage;
import se.tink.libraries.i18n.LocalizableKey;

@RequiredArgsConstructor
public class MinPensionAuthenticator implements BankIdAuthenticator<String> {
    private final MinPensionApiClient minPensionApiClient;
    private final SessionStorage sessionStorage;
    private String autoStartToken;

    @Override
    public String init(String ssn)
            throws BankServiceException, AuthorizationException, AuthenticationException {
        final InitBankIdResponse initBankIdResponse = minPensionApiClient.initBankid();
        autoStartToken = initBankIdResponse.getAutoStartToken();

        return initBankIdResponse.getOrderRef();
    }

    @Override
    public BankIdStatus collect(String orderRef)
            throws AuthenticationException, AuthorizationException {
        final PollBankIdResponse pollBankIdResponse = minPensionApiClient.pollBankId(orderRef);
        final String bankIdStatus = pollBankIdResponse.getAuthenticationStatus().toUpperCase();

        if (bankIdStatus.contains(MinPensionConstants.BankIdStatus.WAITING)) {
            return BankIdStatus.WAITING;
        } else if (bankIdStatus.contains(MinPensionConstants.BankIdStatus.CANCELLED)) {
            return BankIdStatus.CANCELLED;
        } else if (bankIdStatus.contains(MinPensionConstants.BankIdStatus.COMPLETE)) {
            completeAuthentication(pollBankIdResponse);
            return BankIdStatus.DONE;
        }
        return BankIdStatus.EXPIRED_AUTOSTART_TOKEN;
    }

    @Override
    public String refreshAutostartToken()
            throws BankServiceException, AuthorizationException, AuthenticationException {
        return init("");
    }

    @Override
    public Optional<String> getAutostartToken() {
        return Optional.ofNullable(autoStartToken);
    }

    private void completeAuthentication(PollBankIdResponse pollBankIdResponse) {
        final UserTOCResponse userResponse = minPensionApiClient.fetchUserTOCStatus();

        if (userResponse.isRegistrationTocIsRequired() && !userResponse.isUserAgreementApproved()) {
            throw AuthorizationError.ACCOUNT_BLOCKED.exception(
                    new LocalizableKey(ErrorMessage.KNOW_YOUR_CUSTOMER));
        }
        sessionStorage.put(
                StorageKeys.FIRST_NAME, pollBankIdResponse.getBankIdUser().getFirstname());
        sessionStorage.put(StorageKeys.LAST_NAME, pollBankIdResponse.getBankIdUser().getLastname());
    }
}
