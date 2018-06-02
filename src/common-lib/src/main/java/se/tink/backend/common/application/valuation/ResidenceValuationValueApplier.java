package se.tink.backend.common.application.valuation;

import java.util.Optional;
import java.util.Objects;
import se.tink.backend.common.application.ApplicationValueApplier;
import se.tink.backend.common.providers.ProviderImageProvider;
import se.tink.backend.common.repository.RepositoryFactory;
import se.tink.backend.core.Application;
import se.tink.backend.core.ApplicationForm;
import se.tink.backend.core.User;
import se.tink.libraries.application.GenericApplication;
import se.tink.backend.core.enums.ApplicationFormStatusKey;

public class ResidenceValuationValueApplier extends ApplicationValueApplier {
    public ResidenceValuationValueApplier(
            RepositoryFactory repositoryFactory,
            ProviderImageProvider providerImageProvider) {
        super(ResidenceValuationValueApplier.class, repositoryFactory, providerImageProvider);
    }

    @Override
    protected void populateDefaultValues(ApplicationForm form, User user) {
        if (!Objects.equals(form.getStatus().getKey(), ApplicationFormStatusKey.CREATED)) {
            return;
        }

        // TODO: Any default values to populate?
        switch (form.getName()) {
        default:
            // Do nothing.
        }
    }

    @Override
    public void populateDynamicFields(ApplicationForm form, User user, Application application) {
        // TODO: Any dynamic fields to populate?
        switch (form.getName()) {
        default:
            // Do nothing.
        }
    }

    @Override
    protected void populatePayload(ApplicationForm form, final Application application, User user,
            Optional<GenericApplication> genericApplication) {
        // TODO: Any payloads to populate?
        switch (form.getName()) {
        default:
            // Nothing.
        }
    }
}
