package se.tink.backend.aggregation.agents.nxgen.no.banks.handelsbanken.fetcher.investment.rpc;

import com.sun.jersey.core.util.MultivaluedMapImpl;
import se.tink.backend.aggregation.agents.nxgen.no.banks.handelsbanken.HandelsbankenNOConstants;

public class FinalizeInvestorLoginRequest extends MultivaluedMapImpl {

    public FinalizeInvestorLoginRequest(String samlResponse) {
        add(HandelsbankenNOConstants.FinalizeInvestorLoginForm.RELAY_STATE,
                HandelsbankenNOConstants.QueryParamPairs.INVESTOR_TARGET.getValue());
        add(HandelsbankenNOConstants.FinalizeInvestorLoginForm.SAML_RESPONSE, samlResponse);
    }
}
