package se.tink.backend.aggregation.agents.nxgen.no.banks.handelsbanken.authenticator.rpc;

import com.sun.jersey.core.util.MultivaluedMapImpl;
import se.tink.backend.aggregation.agents.nxgen.no.banks.handelsbanken.HandelsbankenNOConstants;
import se.tink.backend.aggregation.agents.nxgen.no.banks.handelsbanken.HandelsbankenNOConstants.InitBankIdForm;

public class InitBankIdRequest extends MultivaluedMapImpl {

    private InitBankIdRequest(String dob, String mobileNumber) {
        add(InitBankIdForm.FORM, InitBankIdForm.FORM_VALUE);
        add(InitBankIdForm.PHONE_NUMBER, mobileNumber);
        add(InitBankIdForm.BIRTHDATE, dob);
        add(InitBankIdForm.BTN, "");
        add(InitBankIdForm.VIEWSTATE, HandelsbankenNOConstants.UrlParameters.SESSION_1);
    }

    public static InitBankIdRequest build(String dob, String mobileNumber) {
        return new InitBankIdRequest(dob, mobileNumber);
    }
}
