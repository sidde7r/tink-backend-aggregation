package se.tink.backend.aggregation.agents.nxgen.serviceproviders.banks.swedbank.authenticator;

import com.google.common.base.Preconditions;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import se.tink.backend.aggregation.agents.BankIdStatus;
import se.tink.backend.aggregation.agents.exceptions.AuthenticationException;
import se.tink.backend.aggregation.agents.exceptions.AuthorizationException;
import se.tink.backend.aggregation.agents.exceptions.BankIdException;
import se.tink.backend.aggregation.agents.exceptions.BankServiceException;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.banks.swedbank.SwedbankDefaultApiClient;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.banks.swedbank.authenticator.rpc.AbstractBankIdResponse;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.banks.swedbank.authenticator.rpc.CollectBankIdResponse;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.banks.swedbank.authenticator.rpc.InitBankIdResponse;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.banks.swedbank.rpc.LinkEntity;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.banks.swedbank.rpc.ProfileResponse;
import se.tink.backend.aggregation.nxgen.controllers.authentication.multifactor.bankid.BankIdAuthenticator;

public class SwedbankDefaultBankIdAuthenticator implements BankIdAuthenticator<AbstractBankIdResponse> {
    private static final Logger log = LoggerFactory.getLogger(SwedbankDefaultBankIdAuthenticator.class);
    private final SwedbankDefaultApiClient apiClient;
    private final String bankId;

    public SwedbankDefaultBankIdAuthenticator(SwedbankDefaultApiClient apiClient, String bankId) {
        this.apiClient = apiClient;
        this.bankId = bankId;
    }

    @Override
    public AbstractBankIdResponse init(String ssn) throws BankIdException, BankServiceException, AuthorizationException {
        InitBankIdResponse initBankIdResponse = apiClient.initBankId(ssn);

        LinkEntity linkEntity = initBankIdResponse.getLinks().getNextOrThrow();
        Preconditions.checkState(linkEntity.isValid(),
                "Login failed - Cannot proceed without valid link entity - Method:[%s], Uri:[%s]",
                linkEntity.getMethod(), linkEntity.getUri());

        return initBankIdResponse;
    }

    @Override
    public BankIdStatus collect(AbstractBankIdResponse response) throws AuthenticationException, AuthorizationException {
        CollectBankIdResponse collectBankIdResponse = apiClient.collectBankId(response.getLinks().getNextOrThrow());

        AbstractBankIdResponse.BankIdResponseStatus bankIdResponseStatus = collectBankIdResponse.getStatus();
        Preconditions.checkState(bankIdResponseStatus != null, "Login failed - Cannot proceed without bankId status");

        switch (bankIdResponseStatus) {
        case CLIENT_NOT_STARTED:
        case USER_SIGN:
            return BankIdStatus.WAITING;
        case COMPLETE:
            completeBankIdLogin(collectBankIdResponse);
            return BankIdStatus.DONE;
        default:
            log.warn("Login failed - Not implemented case - BankIdResponseStatus:[%s]", bankIdResponseStatus);
            throw new IllegalStateException();
        }
    }

    private void completeBankIdLogin(CollectBankIdResponse collectBankIdResponse) {
        ProfileResponse profileResponse = apiClient.completeBankId(collectBankIdResponse.getLinks().getNextOrThrow());
        apiClient.selectProfile(profileResponse.getNext(bankId));
    }
}
