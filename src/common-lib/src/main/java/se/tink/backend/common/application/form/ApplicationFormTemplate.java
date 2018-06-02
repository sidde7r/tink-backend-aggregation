package se.tink.backend.common.application.form;

import com.google.common.collect.ImmutableSet;
import se.tink.backend.core.enums.ApplicationFormType;

public class ApplicationFormTemplate {
    public final Integer order;
    public final ApplicationFormType type;
    public final ImmutableSet<String> fieldNames;

    ApplicationFormTemplate(Integer order, ApplicationFormType type, String... fieldNames) {
        this.order = order;
        this.type = type;
        this.fieldNames = ImmutableSet.copyOf(fieldNames);
    }

    public static ApplicationFormTemplate create(int order, ApplicationFormType type, String... fieldNames) {
        return new ApplicationFormTemplate(order, type, fieldNames);
    }
}
