package se.tink.backend.categorization.client;

import se.tink.backend.categorization.api.FastTextClassifierService;
import se.tink.backend.categorization.api.FastTextTrainerService;
import se.tink.libraries.http.client.ServiceClassBuilder;

public class FastTextServiceFactory {
    public static String SERVICE_NAME = "fasttext";
    private ServiceClassBuilder builder;

    public FastTextServiceFactory(ServiceClassBuilder builder) {
        this.builder = builder;
    }

    public FastTextClassifierService getFastTextClassifierService() {
        return builder.build(FastTextClassifierService.class);
    }

    public FastTextTrainerService getFastTextTrainerService() {
        return builder.build(FastTextTrainerService.class);
    }
}
