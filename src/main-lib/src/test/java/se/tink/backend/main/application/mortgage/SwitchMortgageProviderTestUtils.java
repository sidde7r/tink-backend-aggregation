package se.tink.backend.main.application.mortgage;

import com.google.common.collect.Lists;
import java.util.ArrayList;
import java.util.UUID;
import se.tink.backend.core.ApplicationField;
import se.tink.backend.core.ApplicationForm;
import se.tink.backend.core.enums.ApplicationFieldName;
import se.tink.backend.core.enums.ApplicationFormName;
import se.tink.backend.main.application.ApplicationTestUtils;

public class SwitchMortgageProviderTestUtils extends ApplicationTestUtils {

    public static ApplicationForm createCompletedSecurityForm(String propertyValue) {

        ApplicationForm form = createCompletedForm(UUID.randomUUID(), ApplicationFormName.MORTGAGE_SECURITY);

        ArrayList<ApplicationField> fields = Lists.newArrayList();

        fields.add(createField(ApplicationFieldName.PROPERTY_TYPE, propertyValue));

        form.setFields(fields);

        return form;
    }

}