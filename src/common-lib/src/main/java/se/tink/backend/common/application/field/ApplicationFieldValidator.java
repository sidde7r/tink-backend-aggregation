package se.tink.backend.common.application.field;

import com.fasterxml.jackson.core.type.TypeReference;
import com.google.common.base.Objects;
import com.google.common.base.Strings;
import com.google.common.collect.Iterables;
import com.google.common.collect.Lists;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import se.tink.libraries.i18n.Catalog;
import se.tink.libraries.serialization.utils.SerializationUtils;
import se.tink.backend.core.ApplicationField;
import se.tink.backend.core.ApplicationFieldError;
import se.tink.backend.core.ApplicationFieldOption;
import se.tink.backend.core.ApplicationForm;
import se.tink.backend.core.User;
import se.tink.backend.core.enums.ApplicationFieldType;

public class ApplicationFieldValidator {
    
    private static final TypeReference<List<String>> LIST_OF_STRINGS_TYPE_REFERENCE = new TypeReference<List<String>>() {
    };
    
    private final ApplicationFieldTemplate fieldTemplate;
    private final Catalog catalog;

    public ApplicationFieldValidator(final ApplicationFieldTemplate fieldTemplate, User user) {

        this.fieldTemplate = fieldTemplate;
        this.catalog = Catalog.getCatalog(user.getProfile().getLocale());
    }

    public void validate(ApplicationField field) {
        validate(field, null);
    }

    public void validate(ApplicationField field, ApplicationForm form) {

        ApplicationFieldTemplate.FieldSpec spec = fieldTemplate.getSpecForName(field.getTemplateName());

        if ((!spec.required && Strings.isNullOrEmpty(field.getValue())) || field.isReadOnly()) {
            // Optional and left null, or readOnly (which means the field has already been validated)
            // no validation needed
            return;
        }

        List<ApplicationFieldError> errors = Lists.newArrayList();

        if (field.getValue() == null) {
            if (ApplicationFieldTemplate.isDependencySatisfied(field, form)) {
                errors.add(new ApplicationFieldError(catalog
                        .getString("You have to supply information for this field.")));
            }
        } else {
            
            List<String> options = null;

            if (spec.options.isPresent()) {
                // Options are defined by the specification.
                options = spec.options.get();
            } else if (field.getOptions() != null && !field.getOptions().isEmpty()) {
                // Options are defined dynamically.
                options = Lists.newArrayList(Iterables.transform(field.getOptions(),
                        ApplicationFieldOption::getValue));
            }
            
            if (options != null) {
                
                List<String> values;
                
                if (Objects.equal(ApplicationFieldType.MULTI_SELECT, field.getType())) {
                    values = SerializationUtils.deserializeFromString(field.getValue(), LIST_OF_STRINGS_TYPE_REFERENCE);                    
                } else {
                    values = Lists.newArrayList(field.getValue());
                }
                
                if (values == null || values.isEmpty()) {
                    if (Objects.equal(ApplicationFieldType.MULTI_SELECT, field.getType())) {
                        errors.add(new ApplicationFieldError(catalog
                                .getString("You have to select at least one option.")));
                    } else {
                        errors.add(new ApplicationFieldError(catalog.getString("You have to select one option.")));
                    }
                } else {
                    for (String value : values) {
                        if (!options.contains(value)) {
                            errors.add(new ApplicationFieldError(catalog.getString("Invalid option selected.")));
                        }
                    }
                }
            }

            if (spec.pattern != null) {
                // FIXME potentially cache
                Pattern pattern = Pattern.compile(spec.pattern, Pattern.CASE_INSENSITIVE);
                Matcher matcher = pattern.matcher(field.getValue());
                if (!matcher.matches()) {
                    errors.add(new ApplicationFieldError(catalog.getString("Invalid input format.")));
                }
            }
        }

        field.setErrors(errors);
    }
}
