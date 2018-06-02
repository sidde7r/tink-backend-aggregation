package se.tink.backend.common.application.form;

import com.google.common.collect.ImmutableSet;
import com.google.common.collect.Iterables;
import java.util.List;
import java.util.Locale;
import java.util.Objects;
import java.util.Optional;
import se.tink.backend.common.application.ApplicationTemplate;
import se.tink.backend.common.application.field.ApplicationFieldValidator;
import se.tink.libraries.i18n.Catalog;
import se.tink.backend.core.Application;
import se.tink.backend.core.ApplicationField;
import se.tink.backend.core.ApplicationForm;
import se.tink.backend.core.User;
import se.tink.backend.core.enums.ApplicationFormStatusKey;
import se.tink.backend.utils.guavaimpl.Predicates;

public class ApplicationFormValidator {
    private final ApplicationFieldValidator fieldValidator;
    private final ApplicationTemplate template;
    protected final Catalog catalog;
    protected final Locale locale;

    public ApplicationFormValidator(final ApplicationTemplate template, final User user,
            final ApplicationFieldValidator fieldValidator) {
        
        this.template = template;
        this.fieldValidator = fieldValidator;
        this.catalog = Catalog.getCatalog(user.getProfile().getLocale());
        this.locale = Catalog.getLocale(user.getProfile().getLocale());
    }

    public void validate(ApplicationForm form, Application application) {

        ImmutableSet<String> fieldNamesFromTemplate = template.getFieldNames(form.getName());
        List<ApplicationField> fieldsOnForm = form.getFields();

        for (String fieldName : fieldNamesFromTemplate) {
            Optional<ApplicationField> field = fieldsOnForm.stream().filter(f ->
                    Predicates.applicationFieldOfTemplateName(fieldName).apply(f)).findFirst();

            if (!Objects.equals(form.getStatus().getKey(), ApplicationFormStatusKey.CREATED)) {
                // Don't evaluate field values if form just created
                fieldValidator.validate(field.get(), form);
                
                if (field.get().hasError()) {
                    form.updateStatus(ApplicationFormStatusKey.ERROR);
                }
            }
        }

        updateFormStatus(form);
    }

    private void updateFormStatus(ApplicationForm form) {
        if (form.getStatus().getKey() == ApplicationFormStatusKey.IN_PROGRESS) {

            boolean anyFieldHasError = Iterables.any(form.getFields(), Predicates.APPLICATION_FIELD_HAS_ERROR);
            if (!anyFieldHasError) {
                form.updateStatus(ApplicationFormStatusKey.COMPLETED);
            }
        }
    }
}
