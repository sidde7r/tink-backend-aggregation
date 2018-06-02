package se.tink.backend.common.application.field;

import com.google.common.collect.Lists;
import java.util.List;
import se.tink.backend.core.ApplicationField;
import se.tink.backend.core.ApplicationFieldOption;

public class ApplicationFieldFactory {

    private final ApplicationFieldTemplate fieldTemplate;

    public ApplicationFieldFactory(final ApplicationFieldTemplate fieldTemplate) {

        this.fieldTemplate = fieldTemplate;
    }

    public ApplicationField createFromName(final String name) {
        ApplicationField field = new ApplicationField();
        
        ApplicationFieldTemplate.FieldSpec spec = fieldTemplate.getSpecForName(name);
        
        field.setName(name);
        
        field.setType(spec.type);
        field.setPattern(spec.pattern);
        field.setDefaultValue(spec.defaultValue);
        field.setRequired(spec.required);
        field.setDependency(spec.dependency);
        field.setReadOnly(spec.readOnly);

        if (spec.options.isPresent()) {
            List<ApplicationFieldOption> options = Lists.newArrayList();
            
            for (String value : spec.options.get()) {
                ApplicationFieldOption option = new ApplicationFieldOption();
                option.setValue(value);
                
                options.add(option);
            }
            
            field.setOptions(options);
        }
        
        return field;
    }
}
