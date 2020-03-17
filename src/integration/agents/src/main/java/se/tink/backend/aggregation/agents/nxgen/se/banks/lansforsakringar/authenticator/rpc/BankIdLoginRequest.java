package se.tink.backend.aggregation.agents.nxgen.se.banks.lansforsakringar.authenticator.rpc;

import com.fasterxml.jackson.annotation.JsonIgnore;
import se.tink.backend.aggregation.annotations.JsonObject;

@JsonObject
public class BankIdLoginRequest {
    private String ssn;
    private String reference;
    private Boolean acceptingUserTerms;

    private BankIdLoginRequest(String ssn, String ref, boolean acceptTerms) {
        this.ssn = ssn;
        this.reference = ref;
        this.acceptingUserTerms = acceptTerms;
    }

    @JsonIgnore
    public static BankIdLoginRequest of(String ssn, String ref) {
        return new BankIdLoginRequest(ssn, ref, false);
    }

    @JsonIgnore
    public static BankIdLoginRequest of(String ssn, String ref, boolean acceptTerms) {
        return new BankIdLoginRequest(ssn, ref, acceptTerms);
    }
}
