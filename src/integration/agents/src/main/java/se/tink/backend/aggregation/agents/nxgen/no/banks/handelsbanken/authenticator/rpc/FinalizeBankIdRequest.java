package se.tink.backend.aggregation.agents.nxgen.no.banks.handelsbanken.authenticator.rpc;

import com.sun.jersey.core.util.MultivaluedMapImpl;
import se.tink.backend.aggregation.agents.nxgen.no.banks.handelsbanken.HandelsbankenNOConstants;
import se.tink.backend.aggregation.agents.nxgen.no.banks.handelsbanken.HandelsbankenNOConstants.FinalizeBankIdForm;

public class FinalizeBankIdRequest extends MultivaluedMapImpl {

    private FinalizeBankIdRequest() {
        add(FinalizeBankIdForm.FORM, FinalizeBankIdForm.FORM_VALUE);
        add(FinalizeBankIdForm.BTN, "");
        add(FinalizeBankIdForm.VIEWSTATE, HandelsbankenNOConstants.UrlParameters.SESSION_2);
    }

    public static FinalizeBankIdRequest build() {
        return new FinalizeBankIdRequest();
    }
}
