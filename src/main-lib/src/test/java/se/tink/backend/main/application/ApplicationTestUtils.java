package se.tink.backend.main.application;

import com.google.common.collect.Lists;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.UUID;
import org.junit.Assert;
import se.tink.backend.core.Application;
import se.tink.backend.core.ApplicationField;
import se.tink.backend.core.ApplicationFieldError;
import se.tink.backend.core.ApplicationFieldOption;
import se.tink.backend.core.ApplicationForm;
import se.tink.backend.core.ApplicationFormStatus;
import se.tink.backend.core.ApplicationStatus;
import se.tink.backend.core.enums.ApplicationFieldType;
import se.tink.backend.core.enums.ApplicationFormStatusKey;
import se.tink.backend.core.enums.ApplicationFormType;
import se.tink.backend.core.enums.ApplicationStatusKey;
import se.tink.libraries.application.ApplicationType;

public class ApplicationTestUtils {

    public static Application create() {
        Application application = new Application();

        application.setStatus(new ApplicationStatus());
        application.getStatus().setKey(ApplicationStatusKey.CREATED);
        application.getStatus().setUpdated(new Date());

        application.setCreated(new Date());
        application.setType(ApplicationType.SWITCH_MORTGAGE_PROVIDER);
        application.setForms(Lists.<ApplicationForm>newArrayList());

        return application;
    }

    public static Application create(ApplicationForm... forms) {
        Application application = create();

        for (ApplicationForm form : forms) {
            application.attach(form);
        }

        return application;
    }

    public static void verifyStatus(Application application, ApplicationStatusKey status) {
        Assert.assertEquals(status, application.getStatus().getKey());
    }

    public static void verifyFormsOnOrder(Application application, String... formNames) {
        Assert.assertEquals(formNames.length, application.getForms().size());

        for (int i = 0; i < application.getForms().size(); i++) {
            ApplicationForm form = application.getForms().get(i);
            Assert.assertEquals(formNames[i], form.getName());
        }
    }

    public static void verifyFormStatus(Application application, String formName1, ApplicationFormStatusKey status1) {
        Assert.assertEquals(1, application.getForms().size());

        Assert.assertEquals(formName1, application.getForms().get(0).getName());
        Assert.assertEquals(status1, application.getForms().get(0).getStatus().getKey());
    }

    public static void verifyFormStatus(Application application, String formName1, ApplicationFormStatusKey status1, String formName2, ApplicationFormStatusKey status2) {
        Assert.assertEquals(2, application.getForms().size());

        Assert.assertEquals(formName1, application.getForms().get(0).getName());
        Assert.assertEquals(status1, application.getForms().get(0).getStatus().getKey());
        Assert.assertEquals(formName2, application.getForms().get(1).getName());
        Assert.assertEquals(status2, application.getForms().get(1).getStatus().getKey());
    }

    public static void verifyFormStatus(Application application, String formName1, ApplicationFormStatusKey status1, String formName2, ApplicationFormStatusKey status2, String formName3, ApplicationFormStatusKey status3) {
        Assert.assertEquals(3, application.getForms().size());

        Assert.assertEquals(formName1, application.getForms().get(0).getName());
        Assert.assertEquals(status1, application.getForms().get(0).getStatus().getKey());
        Assert.assertEquals(formName2, application.getForms().get(1).getName());
        Assert.assertEquals(status2, application.getForms().get(1).getStatus().getKey());
        Assert.assertEquals(formName3, application.getForms().get(2).getName());
        Assert.assertEquals(status3, application.getForms().get(2).getStatus().getKey());
    }

    public static ApplicationForm createForm(UUID id, String name, ApplicationFormStatusKey status) {
        return createForm(id, null, name, status);
    }

    public static ApplicationForm createForm(UUID id, UUID parentId, String name, ApplicationFormStatusKey status) {
        ApplicationForm form = new ApplicationForm();

        form.setStatus(new ApplicationFormStatus());
        form.getStatus().setKey(status);
        form.getStatus().setUpdated(new Date());

        form.setId(id);
        form.setName(name);
        form.setType(ApplicationFormType.FORM);
        form.setParentId(parentId);

        ArrayList<ApplicationField> fields = Lists.newArrayList();
        form.setFields(fields);

        return form;
    }

    public static ApplicationForm createCompletedForm(UUID id, String name) {
        return createForm(id, null, name, ApplicationFormStatusKey.COMPLETED);
    }

    public static ApplicationForm createCompletedForm(UUID id, UUID parentId, String name) {
        return createForm(id, parentId, name, ApplicationFormStatusKey.COMPLETED);
    }

    public static ApplicationForm createCompletedFormWithDescription(UUID id, UUID parentId, String name, String description) {
        ApplicationForm form = createCompletedForm(id, parentId, name);
        form.setDescription(description);
        return form;
    }

    public static ApplicationField createField(String name) {
        return createField(name, null, Lists.<ApplicationFieldError>newArrayList(), null);
    }

    public static ApplicationField createField(String name, String value) {
        return createField(name, value, null);
    }

    public static ApplicationField createField(String name, String value, String pattern) {
        return createField(name, value, Lists.<ApplicationFieldError>newArrayList(), pattern);
    }

    public static ApplicationField createField(String name, String value, List<ApplicationFieldError> errors,
            String pattern) {
        ApplicationField field = new ApplicationField();

        field.setName(name);
        field.setValue(value);
        field.setErrors(errors);
        field.setPattern(pattern);

        return field;
    }
    
    public static ApplicationField createSelectField(String name, String value, List<String> optionValues,
            boolean isMultiSelect) {
        
        ApplicationField field = createField(name, value);

        field.setType(isMultiSelect ? ApplicationFieldType.MULTI_SELECT : ApplicationFieldType.SELECT);

        List<ApplicationFieldOption> options = Lists.newArrayList();

        for (String optionValue : optionValues) {
            ApplicationFieldOption option = new ApplicationFieldOption();
            option.setValue(optionValue);
            options.add(option);
        }

        field.setOptions(options);

        return field;
    }
}
