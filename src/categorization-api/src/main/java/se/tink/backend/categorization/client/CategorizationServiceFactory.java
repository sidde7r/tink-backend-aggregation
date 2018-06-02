package se.tink.backend.categorization.client;

import se.tink.backend.categorization.api.CategorizationService;
import se.tink.backend.categorization.api.FastTextClassifierService;
import se.tink.backend.categorization.api.FastTextTrainerService;
import se.tink.libraries.http.client.ServiceClassBuilder;

public class CategorizationServiceFactory {
    public static String SERVICE_NAME = "categorization";
    private ServiceClassBuilder builder;

    public CategorizationServiceFactory(ServiceClassBuilder builder) {
        this.builder = builder;
    }

    public CategorizationService getCategorizationService() {
        return builder.build(CategorizationService.class);
    }
}
