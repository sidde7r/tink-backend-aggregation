package se.tink.backend.aggregation.agents.nxgen.no.banks.handelsbanken.fetcher.rpc;

import com.sun.jersey.core.util.MultivaluedMapImpl;
import se.tink.backend.aggregation.agents.nxgen.no.banks.handelsbanken.HandelsbankenNOConstants;

public class FinalizeAksjerLoginRequest extends MultivaluedMapImpl {

    public FinalizeAksjerLoginRequest(String samlResponse) {
        add(HandelsbankenNOConstants.FinalizeInvestorLoginForm.SAML_RESPONSE, samlResponse);
    }
}
