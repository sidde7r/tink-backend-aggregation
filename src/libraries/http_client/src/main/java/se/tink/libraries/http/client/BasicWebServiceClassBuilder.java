package se.tink.libraries.http.client;

import com.sun.jersey.api.client.WebResource;

public class BasicWebServiceClassBuilder implements ServiceClassBuilder {

    public BasicWebServiceClassBuilder(WebResource jerseyResource) {
        this.jerseyResource = jerseyResource;
    }

    private WebResource jerseyResource;

    @Override
    public <T> T build(Class<T> serviceClass) {
        return WebResourceFactory.newResource(serviceClass, jerseyResource);
    }

    @Override
    public <T> T build(Class<T> serviceClass, Object _hashSource) {
        // Only a single candidate. Always using it.
        return build(serviceClass);
    }
}
