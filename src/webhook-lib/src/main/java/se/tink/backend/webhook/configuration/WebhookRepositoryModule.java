package se.tink.backend.webhook.configuration;

import se.tink.backend.common.repository.mysql.main.OAuth2ClientRepository;
import se.tink.backend.common.repository.mysql.main.OAuth2WebHookRepository;
import se.tink.backend.guice.configuration.RepositoryModule;

public class WebhookRepositoryModule extends RepositoryModule {

    WebhookRepositoryModule(WebhookConfiguration configuration) {
        super(configuration.getDatabase(), null);
    }

    @Override
    protected void bindRepositories() {
        bindSpringBean(OAuth2ClientRepository.class);
        bindSpringBean(OAuth2WebHookRepository.class);
    }
}
