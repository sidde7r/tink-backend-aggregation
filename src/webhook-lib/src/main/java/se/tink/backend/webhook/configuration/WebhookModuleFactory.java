package se.tink.backend.webhook.configuration;

import com.google.common.collect.ImmutableList;
import com.google.inject.Module;
import io.dropwizard.jersey.setup.JerseyEnvironment;

public class WebhookModuleFactory {

    public static Iterable<Module> build(WebhookConfiguration configuration, JerseyEnvironment jersey) {
        return ImmutableList.<Module>builder().add(
                new WebhookRepositoryModule(configuration),
                new WebhookServiceModule(configuration, jersey))
                .build();
    }
}


