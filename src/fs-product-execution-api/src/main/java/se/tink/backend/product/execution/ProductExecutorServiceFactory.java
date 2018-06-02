package se.tink.backend.product.execution;

import se.tink.backend.product.execution.api.ProductExecutorService;

public interface ProductExecutorServiceFactory {
    String SERVICE_NAME = "productExecutor";

    ProductExecutorService getProductExecutorService();
}
