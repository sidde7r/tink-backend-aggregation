package se.tink.backend.common.resources;

import se.tink.backend.core.Credentials;
import se.tink.backend.core.signableoperation.SignableOperation;
import se.tink.backend.product.execution.ProductExecutorServiceFactory;
import se.tink.backend.product.execution.api.dto.CreateProductRequest;
import se.tink.backend.product.execution.model.User;
import se.tink.libraries.application.GenericApplication;

public class ProductExecutorRunnableFactory {
    private final ProductExecutorServiceFactory productExecutorServiceFactory;

    public ProductExecutorRunnableFactory(ProductExecutorServiceFactory productExecutorServiceFactory) {
        this.productExecutorServiceFactory = productExecutorServiceFactory;
    }

    public Runnable createProductExecutorRunnable(SignableOperation operation, GenericApplication genericApplication, Credentials credentials) {
        return () -> ProductExecutorRunnableFactory.this.run(operation, genericApplication, credentials);
    }

    private void run(SignableOperation operation, GenericApplication genericApplication, Credentials credentials) {

        CreateProductRequest request = new CreateProductRequest(new User(), genericApplication, operation, credentials);

        try {
            productExecutorServiceFactory.getProductExecutorService().createProduct(request);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
