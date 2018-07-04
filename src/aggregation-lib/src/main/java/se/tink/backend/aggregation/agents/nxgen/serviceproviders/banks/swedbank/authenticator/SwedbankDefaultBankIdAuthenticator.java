package se.tink.backend.aggregation.agents.nxgen.serviceproviders.banks.swedbank.authenticator;

import com.google.common.base.Preconditions;
import java.util.Map;
import org.apache.http.HttpStatus;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import se.tink.backend.aggregation.agents.BankIdStatus;
import se.tink.backend.aggregation.agents.exceptions.AuthenticationException;
import se.tink.backend.aggregation.agents.exceptions.AuthorizationException;
import se.tink.backend.aggregation.agents.exceptions.BankIdException;
import se.tink.backend.aggregation.agents.exceptions.BankServiceException;
import se.tink.backend.aggregation.agents.exceptions.errors.LoginError;
import se.tink.backend.aggregation.agents.nxgen.se.banks.swedbank.SwedbankSEConfiguration;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.banks.swedbank.SwedbankBaseConstants;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.banks.swedbank.SwedbankConfiguration;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.banks.swedbank.SwedbankDefaultApiClient;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.banks.swedbank.authenticator.rpc.AbstractBankIdAuthResponse;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.banks.swedbank.authenticator.rpc.CollectBankIdResponse;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.banks.swedbank.authenticator.rpc.InitBankIdResponse;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.banks.swedbank.rpc.BankProfile;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.banks.swedbank.rpc.EngagementOverviewResponse;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.banks.swedbank.rpc.ErrorResponse;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.banks.swedbank.rpc.LinkEntity;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.banks.swedbank.rpc.MenuItemLinkEntity;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.banks.swedbank.rpc.ProfileResponse;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.banks.swedbank.rpc.SelectedProfileResponse;
import se.tink.backend.aggregation.nxgen.controllers.authentication.multifactor.bankid.BankIdAuthenticator;
import se.tink.backend.aggregation.nxgen.http.HttpResponse;
import se.tink.backend.aggregation.nxgen.http.exceptions.HttpResponseException;
import se.tink.backend.aggregation.nxgen.storage.SessionStorage;

public class SwedbankDefaultBankIdAuthenticator implements BankIdAuthenticator<AbstractBankIdAuthResponse> {
    private static final Logger log = LoggerFactory.getLogger(SwedbankDefaultBankIdAuthenticator.class);
    private final SwedbankDefaultApiClient apiClient;

    public SwedbankDefaultBankIdAuthenticator(SwedbankDefaultApiClient apiClient) {
        this.apiClient = apiClient;
    }

    @Override
    public AbstractBankIdAuthResponse init(String ssn) throws BankIdException, BankServiceException, AuthorizationException {
        InitBankIdResponse initBankIdResponse = apiClient.initBankId(ssn);

        LinkEntity linkEntity = initBankIdResponse.getLinks().getNextOrThrow();
        Preconditions.checkState(linkEntity.isValid(),
                "Login failed - Cannot proceed without valid link entity - Method:[%s], Uri:[%s]",
                linkEntity.getMethod(), linkEntity.getUri());

        return initBankIdResponse;
    }

    @Override
    public BankIdStatus collect(AbstractBankIdAuthResponse response) throws AuthenticationException, AuthorizationException {
        try {
            CollectBankIdResponse collectBankIdResponse = apiClient.collectBankId(response.getLinks().getNextOrThrow());
            SwedbankBaseConstants.BankIdResponseStatus bankIdResponseStatus = collectBankIdResponse.getBankIdStatus();

            switch (bankIdResponseStatus) {
            case CLIENT_NOT_STARTED:
            case USER_SIGN:
                return BankIdStatus.WAITING;
            case CANCELLED:
                return BankIdStatus.CANCELLED;
            case COMPLETE:
                completeBankIdLogin(collectBankIdResponse);
                return BankIdStatus.DONE;
            case TIMEOUT:
                return BankIdStatus.TIMEOUT;
            default:
                log.warn("Login failed - Not implemented case - BankIdResponseStatus:[%s] from [%s]", bankIdResponseStatus,
                        collectBankIdResponse.getStatus());
                throw new IllegalStateException("Login failed - Cannot proceed with unknown bankId status");
            }
        } catch (HttpResponseException hre) {
            HttpResponse httpResponse = hre.getResponse();
            // when timing out, this can also be the response
            if (httpResponse.getStatus() == HttpStatus.SC_UNAUTHORIZED) {
                ErrorResponse errorResponse = httpResponse.getBody(ErrorResponse.class);
                if (errorResponse.hasErrorCode(SwedbankBaseConstants.BankErrorMessage.LOGIN_FAILED)) {
                    return BankIdStatus.TIMEOUT;
                }
            }

            // unknown error re-throw
            throw hre;
        }
    }

    private void completeBankIdLogin(CollectBankIdResponse collectBankIdResponse) throws AuthenticationException {
        apiClient.completeBankId(collectBankIdResponse.getLinks().getNextOrThrow());
    }
}
