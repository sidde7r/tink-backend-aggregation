package se.tink.backend.product.execution.client;

import java.util.List;
import se.tink.backend.product.execution.ProductExecutorServiceFactory;
import se.tink.backend.product.execution.api.ProductExecutorService;
import se.tink.libraries.http.client.BasicWebServiceClassBuilder;
import se.tink.libraries.http.client.ServiceClassBuilder;
import se.tink.libraries.jersey.utils.InterContainerJerseyClientFactory;

public class ClientProductExecutorServiceFactory implements ProductExecutorServiceFactory {
    public static String SERVICE_NAME = "productExecutor";
    private ServiceClassBuilder builder;

    public ClientProductExecutorServiceFactory(ServiceClassBuilder builder) {
        this.builder = builder;
    }

    public ClientProductExecutorServiceFactory(List<String> pinnedCertificates, String url) {
        this(new BasicWebServiceClassBuilder(new InterContainerJerseyClientFactory(pinnedCertificates).build().resource(url)));
    }

    @Override
    public ProductExecutorService getProductExecutorService() {
        return builder.build(ProductExecutorService.class);
    }

}
