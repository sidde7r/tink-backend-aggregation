package se.tink.backend.aggregation.agents.utils.jersey;

import com.sun.jersey.api.client.ClientHandlerException;
import com.sun.jersey.api.client.ClientRequest;
import com.sun.jersey.api.client.ClientResponse;
import com.sun.jersey.api.client.filter.ClientFilter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public abstract class MessageSignInterceptor extends ClientFilter {

    private static final Logger LOGGER =
            LoggerFactory.getLogger(MessageSignInterceptor.class.getName());

    @Override
    public ClientResponse handle(ClientRequest request) throws ClientHandlerException {
        appendAdditionalHeaders(request);
        prepareDigestAndAddAsHeader(request);
        getSignatureAndAddAsHeader(request);
        reorganiseHeaders(request);
        ClientResponse response = getNext().handle(request);
        return response;
    }

    protected abstract void appendAdditionalHeaders(ClientRequest request);

    protected abstract void getSignatureAndAddAsHeader(ClientRequest request);

    protected abstract void prepareDigestAndAddAsHeader(ClientRequest request);

    protected void reorganiseHeaders(ClientRequest request) {}
}
