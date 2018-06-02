package se.tink.backend.system.client;

import se.tink.libraries.jersey.utils.InterContainerJerseyClientFactory;
import se.tink.libraries.http.client.BasicWebServiceClassBuilder;
import se.tink.libraries.http.client.ServiceClassBuilder;
import se.tink.backend.system.api.CronService;
import se.tink.backend.system.api.NotificationGatewayService;
import se.tink.backend.system.api.ProcessService;
import se.tink.backend.system.api.UpdateService;

public class ClientSystemServiceFactory implements SystemServiceFactory {
    private ServiceClassBuilder builder;

    /**
     * Helper constructor to make it more enjoyable to create a factory for a basic URL.
     * <p>
     * Not exposing constructor immediately to make it explicit that we are not making pinned calls.
     * 
     * @param url
     *            to point the factory to.
     */
    public static ClientSystemServiceFactory buildWithoutPinning(String url) {
        return new ClientSystemServiceFactory(url);
    }

    private ClientSystemServiceFactory(String url) {
        this(new BasicWebServiceClassBuilder(
                InterContainerJerseyClientFactory.withoutPinning().build().resource(url)));
    }

    public ClientSystemServiceFactory(ServiceClassBuilder builder) {
        this.builder = builder;
    }

    @Override
    public UpdateService getUpdateService() {
        return builder.build(UpdateService.class);
    }

    public NotificationGatewayService getNotificationGatewayService() {
        return builder.build(NotificationGatewayService.class);
    }

    @Override
    public ProcessService getProcessService() {
        return builder.build(ProcessService.class);
    }

    @Override
    public CronService getCronService() {
        return builder.build(CronService.class);
    }
}
