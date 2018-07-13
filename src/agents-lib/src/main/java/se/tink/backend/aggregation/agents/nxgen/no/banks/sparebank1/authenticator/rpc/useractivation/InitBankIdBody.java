package se.tink.backend.aggregation.agents.nxgen.no.banks.sparebank1.authenticator.rpc.useractivation;

import com.sun.jersey.core.util.MultivaluedMapImpl;

public class InitBankIdBody extends MultivaluedMapImpl {

    public InitBankIdBody(String mobileNumber, String dob, String formId, String viewStateValues) {
        add(formId, formId);
        add("bankid-mobile-number", mobileNumber);
        add("bankid-mobile-birthdate", dob);
        add("nesteMobil", "Neste");
        add("javax.faces.ViewState", viewStateValues);
    }
}
