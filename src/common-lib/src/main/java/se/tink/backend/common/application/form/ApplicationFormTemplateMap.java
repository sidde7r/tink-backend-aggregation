package se.tink.backend.common.application.form;

import com.google.common.collect.ImmutableMap;
import se.tink.backend.core.enums.ApplicationFormType;

public class ApplicationFormTemplateMap {

    public static Builder builder() {
        return new Builder();
    }

    public static class Builder {

        private ImmutableMap.Builder<String, ApplicationFormTemplate> builder = ImmutableMap
                .builder();
        private int counter = 0;

        public Builder put(String formName, ApplicationFormType formType, String... fieldNames) {
            builder.put(formName, ApplicationFormTemplate.create(counter++, formType, fieldNames));
            return this;
        }

        public ImmutableMap<String, ApplicationFormTemplate> build() {
            return builder.build();
        }
    }
}
