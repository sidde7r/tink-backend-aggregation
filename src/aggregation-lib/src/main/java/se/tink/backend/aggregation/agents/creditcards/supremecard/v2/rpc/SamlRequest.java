package se.tink.backend.aggregation.agents.creditcards.supremecard.v2.rpc;

import com.sun.jersey.core.util.MultivaluedMapImpl;
import java.util.Map;
import se.tink.backend.aggregation.agents.creditcards.supremecard.v2.SupremeCardApiConstants;

public class SamlRequest extends MultivaluedMapImpl {
    public static SamlRequest from(Map<String, String> SAMLInfo) {
        SamlRequest samlRequest = new SamlRequest();
        samlRequest.add(
                SupremeCardApiConstants.TARGET_PARAMETER_KEY,
                SAMLInfo.get(SupremeCardApiConstants.TARGET_PARAMETER_KEY));
        samlRequest.add(
                SupremeCardApiConstants.SAML_RESPONSE_PARAMETER_KEY,
                SAMLInfo.get(SupremeCardApiConstants.SAML_RESPONSE_PARAMETER_KEY));

        return samlRequest;
    }
}
