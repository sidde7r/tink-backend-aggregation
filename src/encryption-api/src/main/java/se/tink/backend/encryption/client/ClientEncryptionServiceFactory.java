package se.tink.backend.encryption.client;

import se.tink.libraries.http.client.BasicWebServiceClassBuilder;
import se.tink.libraries.http.client.ServiceClassBuilder;
import se.tink.backend.encryption.api.EncryptionService;
import se.tink.libraries.jersey.utils.InterContainerJerseyClientFactory;

public class ClientEncryptionServiceFactory implements EncryptionServiceFactory {

    private ServiceClassBuilder builder;

    /**
     * Helper constructor to make it more enjoyable to create a factory for a basic URL.
     * <p>
     * Not exposing constructor immediately to make it explicit that we are not making pinned calls.
     * 
     * @param url
     *            to point the factory to.
     */
    public static ClientEncryptionServiceFactory buildWithoutPinning(String url) {
        return new ClientEncryptionServiceFactory(url);
    }

    private ClientEncryptionServiceFactory(String url) {
        this(new BasicWebServiceClassBuilder(
                InterContainerJerseyClientFactory.withoutPinning().build().resource(url)));
    }

    public ClientEncryptionServiceFactory(ServiceClassBuilder builder) {
        this.builder = builder;
    }

    @Override
    public EncryptionService getEncryptionService() {
        return builder.build(EncryptionService.class);
    }
}
