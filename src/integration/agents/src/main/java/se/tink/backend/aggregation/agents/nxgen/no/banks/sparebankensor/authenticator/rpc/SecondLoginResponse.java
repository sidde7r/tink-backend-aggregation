package se.tink.backend.aggregation.agents.nxgen.no.banks.sparebankensor.authenticator.rpc;

import se.tink.backend.aggregation.agents.nxgen.no.banks.sparebankensor.authenticator.entities.AgreementEntity;
import se.tink.backend.aggregation.agents.nxgen.no.banks.sparebankensor.authenticator.entities.CustomerEntity;
import se.tink.backend.aggregation.agents.nxgen.no.banks.sparebankensor.authenticator.entities.SessionEntity;
import se.tink.backend.aggregation.annotations.JsonObject;

@JsonObject
public class SecondLoginResponse {
    private SessionEntity session;
    private CustomerEntity customer;
    private AgreementEntity agreement;

    public SessionEntity getSession() {
        return session;
    }

    public CustomerEntity getCustomer() {
        return customer;
    }

    public AgreementEntity getAgreement() {
        return agreement;
    }
}
