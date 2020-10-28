package se.tink.backend.aggregation.agents.nxgen.no.banks.handelsbanken.authenticator.rpc;

import lombok.Getter;
import se.tink.backend.aggregation.agents.exceptions.errors.BankIdError;
import se.tink.backend.aggregation.agents.nxgen.no.banks.handelsbanken.authenticator.entities.AgreementEntity;
import se.tink.backend.aggregation.agents.nxgen.no.banks.handelsbanken.authenticator.entities.CustomerEntity;
import se.tink.backend.aggregation.agents.nxgen.no.banks.handelsbanken.authenticator.entities.SessionEntity;
import se.tink.backend.aggregation.annotations.JsonObject;

@JsonObject
@Getter
public class SecondLoginResponse {
    private SessionEntity session;
    private CustomerEntity customer;
    private AgreementEntity agreement;

    public SecondLoginResponse throwErrorIfAgreementIsInactive() {
        if (!this.agreement.isActive()) {
            throw BankIdError.AUTHORIZATION_REQUIRED.exception();
        }
        return this;
    }
}
