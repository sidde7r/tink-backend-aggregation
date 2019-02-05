package se.tink.backend.aggregation.agents.nxgen.no.banks.sparebankensor.authenticator.rpc;

import com.sun.jersey.core.util.MultivaluedMapImpl;
import se.tink.backend.aggregation.agents.nxgen.no.banks.sparebankensor.SparebankenSorConstants;

public class InitBankIdBody extends MultivaluedMapImpl {

    private InitBankIdBody(String dob, String mobileNumber) {
        add("form1", "form1");
        add("phonenumber", mobileNumber);
        add("birthdate", dob);
        add("nextBtn", "");
        add("javax.faces.ViewState", SparebankenSorConstants.StaticUrlValues.E1S1);
    }

    public static InitBankIdBody build(String dob, String mobilenumber) {
        return new InitBankIdBody(dob, mobilenumber);
    }
}
