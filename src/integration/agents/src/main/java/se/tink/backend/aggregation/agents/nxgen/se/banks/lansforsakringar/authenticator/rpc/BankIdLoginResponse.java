package se.tink.backend.aggregation.agents.nxgen.se.banks.lansforsakringar.authenticator.rpc;

import se.tink.backend.aggregation.agents.nxgen.se.banks.lansforsakringar.authenticator.entities.SessionEntity;
import se.tink.backend.aggregation.agents.nxgen.se.banks.lansforsakringar.authenticator.entities.TermsEntity;
import se.tink.backend.aggregation.annotations.JsonObject;

@JsonObject
public class BankIdLoginResponse {
    private SessionEntity session;
    private TermsEntity terms;

    public SessionEntity getSession() {
        return session;
    }

    public TermsEntity getTerms() {
        return terms;
    }
}
