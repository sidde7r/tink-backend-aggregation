package se.tink.backend.common.application;

import com.google.common.base.Strings;
import com.google.common.collect.ImmutableMap;
import se.tink.backend.common.application.form.ApplicationFormEnricher;
import se.tink.libraries.i18n.Catalog;
import se.tink.libraries.i18n.LocalizableKey;
import se.tink.backend.core.Application;
import se.tink.backend.core.ApplicationForm;
import se.tink.backend.core.enums.ApplicationStatusKey;
import se.tink.libraries.application.ApplicationType;

/**
 * Class to enrich the Application and it's children forms with display messages.
 * Also uses a FieldEnricher to enrich ApplicationFields with all data other than name and value.
 */
public class ApplicationEnricher extends Enricher {

    private static final ImmutableMap<String, LocalizableKey> APPLICATION_TITLES = ImmutableMap.<String, LocalizableKey> builder()
            .put(ApplicationType.OPEN_SAVINGS_ACCOUNT.name(), new LocalizableKey("Open savings account"))
            .put(ApplicationType.SWITCH_MORTGAGE_PROVIDER.name(), new LocalizableKey("Move your mortgage"))
            .build();

    private static final ImmutableMap<String, LocalizableKey> APPLICATION_STATUS_MESSAGES = ImmutableMap.<String, LocalizableKey> builder()
            .put(ApplicationStatusKey.COMPLETED.name(), new LocalizableKey("The application is complete"))
            .put(ApplicationStatusKey.CREATED.name(), new LocalizableKey("The application is not yet started"))
            .put(ApplicationStatusKey.IN_PROGRESS.name(), new LocalizableKey("The application is started but not complete"))
            .put(ApplicationStatusKey.SIGNED.name(), new LocalizableKey("The application has been signed"))
            .build();

    private final ApplicationFormEnricher formEnricher;

    public ApplicationEnricher(final ApplicationFormEnricher formEnricher) {
        this.formEnricher = formEnricher;
    }

    private String getString(Catalog catalog, ImmutableMap<String, LocalizableKey> map, ApplicationType type) {
        return getString(catalog, map, type.name());
    }

    private String getString(Catalog catalog, ImmutableMap<String, LocalizableKey> map, ApplicationStatusKey key) {
        return getString(catalog, map, key.name());
    }

    public void enrich(Catalog catalog, Application application) {

        if (Strings.isNullOrEmpty(application.getTitle())) {
            application.setTitle(getString(catalog, APPLICATION_TITLES, application.getType()));
        }

        if (Strings.isNullOrEmpty(application.getStatus().getMessage())) {
            application.getStatus().setMessage(
                    getString(catalog, APPLICATION_STATUS_MESSAGES, application.getStatus().getKey()));
        }

        for (ApplicationForm form : application.getForms()) {
            formEnricher.enrich(form, application, catalog);
        }
    }
}
