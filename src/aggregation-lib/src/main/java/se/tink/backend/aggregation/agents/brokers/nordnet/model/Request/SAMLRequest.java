package se.tink.backend.aggregation.agents.brokers.nordnet.model.Request;

import com.sun.jersey.core.util.MultivaluedMapImpl;
import se.tink.backend.aggregation.agents.brokers.nordnet.model.html.CompleteBankIdPage;

public class SAMLRequest extends MultivaluedMapImpl {
    public static SAMLRequest from(CompleteBankIdPage completePage) {
        SAMLRequest request = new SAMLRequest();
        request.add("SAMLResponse", completePage.getSamlResponse());
        request.add("TARGET", completePage.getTarget());

        return request;
    }
}
