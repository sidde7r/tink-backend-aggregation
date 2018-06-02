package se.tink.backend.common.application;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.google.common.base.Joiner;
import com.google.common.base.Objects;
import com.google.common.collect.FluentIterable;
import com.google.common.collect.Iterables;
import com.google.common.collect.Lists;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import se.tink.backend.common.application.field.ApplicationFieldTemplate;
import se.tink.libraries.i18n.Catalog;
import se.tink.backend.utils.LogUtils;
import se.tink.libraries.uuid.UUIDUtils;
import se.tink.backend.core.Application;
import se.tink.backend.core.ApplicationField;
import se.tink.backend.core.ApplicationForm;
import se.tink.backend.core.ApplicationSummary;
import se.tink.backend.core.User;
import se.tink.libraries.application.GenericApplication;
import se.tink.backend.core.enums.ApplicationFieldName;
import se.tink.libraries.application.ApplicationFieldOptionValues;
import se.tink.backend.core.enums.ApplicationFormName;
import se.tink.backend.core.enums.ApplicationFormStatusKey;
import se.tink.backend.core.enums.ApplicationStatusKey;
import se.tink.backend.core.product.ProductArticle;
import se.tink.backend.utils.ApplicationUtils;
import se.tink.backend.utils.guavaimpl.Predicates;

/**
 * The ApplicationProcessor has five main purposes:
 *  - Potentially add a submitted Form
 *  - Validation
 *  - Run Business logic rules for this application
 *  - Applying values known to Tink for newly created Forms
 *  - Enrich display properties and ApplicationFields
 *
 * The ApplicationProcessor could be run on an Application in any state, just created, in progress or to
 * re-validate an already complete form.
 *
 * It will first validate the incoming application and throw ApplicationNotValidException if the application or any of
 * its forms has been tampered with in a way that does not coinside with how it should be. It will then go through
 * all the forms' fields and validate their given values.
 *
 * When the validation is done the processor will continue to run the processor engine for this particular form type.
 * This engine can change status of Forms and potentially add newly created forms
 *
 * For all newly created forms, the valueApplyer will try to lookup data on the user and apply this to the application.
 *
 * Finally the processor will enrich all properties that can be used to display to the user. The enrichment will also
 * populate all the ApplicationFields with sugar for any client
 */
public class ApplicationProcessor {

    private static final LogUtils log = new LogUtils(ApplicationProcessor.class);

    private final ApplicationValidator validator;
    private final ApplicationEngine processorEngine;
    private final ApplicationValueApplier valueApplier;
    private final ApplicationEnricher applicationEnricher;
    private final User user;

    public ApplicationProcessor(
            final ApplicationValidator validator,
            final ApplicationEngine processorEngine,
            final ApplicationValueApplier valueApplier,
            final ApplicationEnricher applicationEnricher,
            final User user) {

        this.validator = validator;
        this.processorEngine = processorEngine;
        this.valueApplier = valueApplier;
        this.applicationEnricher = applicationEnricher;
        this.user = user;
    }

    public void process(Application application) throws ApplicationNotValidException {
        prepare(application);
        processorEngine.resetConfirmation(application);
        crunch(application);
        polish(application);
    }
    
    public void process(Application application, ApplicationForm form) throws ApplicationNotValidException {
        prepare(application);
        attachSubmittedForm(application, form);
        crunch(application);
        polish(application);
    }

    public GenericApplication getGenericApplication(Application application) {
        GenericApplication generic = new GenericApplication();
        
        if (!application.getProductArticle().isPresent()) {
            processorEngine.attachProduct(application);
        }
        
        generic.setApplicationId(application.getId());
        generic.setFieldGroups(processorEngine.getGenericApplicationFieldGroups(application));
        generic.setPersonalNumber(processorEngine.getPersonalNumber(application));
        generic.setProductId(processorEngine.getProductId(application));
        generic.setType(application.getType());
        generic.setUserId(application.getUserId());
        
        return generic;
    }

    public void attachProduct(Application application) {
        if (!application.getProductArticle().isPresent()) {
            processorEngine.attachProduct(application);
        }
    }
    
    public ApplicationSummary getApplicationSummary(Application application) {
        enrich(application);
        
        processorEngine.attachProduct(application);
        processorEngine.updateNumberOfSteps(application);
        
        Optional<ProductArticle> article = application.getProductArticle();
        
        ApplicationSummary summary = processorEngine.getSummary(application);
        summary.setId(UUIDUtils.toTinkUUID(application.getId()));
        summary.setImageUrl("http://d3w3yyufttgvi.cloudfront.net/activities/assets/provider-images/tink.png");

        int completedSteps = FluentIterable.from(application.getForms()).filter(
                input -> Objects.equal(input.getStatus().getKey(), ApplicationFormStatusKey.COMPLETED)).size();
        
        double progress = 0;
        if (application.getSteps() > 0) {
            progress = ((double) completedSteps) / ((double) application.getSteps());            
        }
         
        summary.setProgress(progress);
        summary.setProvider(article.isPresent() ? article.get().getProviderName() : "tink");
        summary.setStatusKey(application.getStatus().getKey());
        summary.setType(application.getType());
        
        return summary;
    }
    
    /**
     * Prepare. Apply the template and populate dynamic fields.
     */
    private void prepare(Application application) {
        replaceFormsWithPopulatedForms(application);
        ensureThatInitialFormsExist(application);
        processorEngine.attachProduct(application);
    }
    
    /**
     * Crunch. 
     */
    private void crunch(Application application) throws ApplicationNotValidException {
        validator.validate(application);
        processorEngine.attachProduct(application);
        attachAndDetachForms(application);
        updateApplicationStatus(application);
    }

    /**
     * Polish. Populate default values and localize.
     */
    private void polish(Application application) {

        Optional<GenericApplication> genericApplication = Optional.empty();
        
        // Ugly hack; this shouldn't be in the processor (since it's mortgage flow specific).
        
        Optional<ApplicationForm> form = Optional.empty();
        
        // Last required form before the confirmation status (in the SEB flow). It's the "no-other-properties" selection
        List<ApplicationForm> otherProperties =
                ApplicationUtils.getForms(application, ApplicationFormName.OTHER_PROPERTIES);
        if (!otherProperties.isEmpty()) {
            form = Optional.empty();

            for (ApplicationForm otherProperty : otherProperties) {
                Optional<String> propertyType = otherProperty.getFieldValue(ApplicationFieldName.PROPERTY_TYPE);
                if (propertyType.isPresent() &&
                        Objects.equal(propertyType.get(), ApplicationFieldOptionValues.NO_OTHER_PROPERTIES)) {
                    form = Optional.of(otherProperty);
                }
            }
        }

        if (form.isPresent() && Objects.equal(ApplicationFormStatusKey.COMPLETED, form.get().getStatus().getKey())) {
            genericApplication = Optional.of(getGenericApplication(application));
        }
        
        // Last required form before the confirmation status (in the SBAB flow).
        form = application.getFirstForm(ApplicationFormName.SBAB_HOUSEHOLD_CHILDREN);
        if (form.isPresent() && Objects.equal(ApplicationFormStatusKey.COMPLETED, form.get().getStatus().getKey())) {
            genericApplication = Optional.of(getGenericApplication(application));
        }
        
        valueApplier.populateDefaultValues(user, application);
        valueApplier.populatePayloads(user, application, genericApplication);
        enrich(application);
    }
    
    private void enrich(Application application) {
        applicationEnricher.enrich(Catalog.getCatalog(user.getProfile().getLocale()), application);
    }
    
    /**
     * Attach a _new_ form to the application. 
     */
    private void attachForm(Application application, ApplicationForm form) {
        
        List<ApplicationForm> forms = application.getForms();
        
        if (forms == null) {
            forms = Lists.newArrayList();
            application.setForms(forms);
        }
        
        forms.add(form);

        application.updateStatus(ApplicationStatusKey.IN_PROGRESS);
    }
    
    /**
     * Attach a _submitted_ form to the application. 
     */
    private void attachSubmittedForm(Application application, ApplicationForm form) throws ApplicationNotValidException {

        Optional<ApplicationForm> existingForm = application.getForm(form.getId());
        if (!existingForm.isPresent()) {
            throw new ApplicationNotValidException("Submitted form not available on Application");
        }

        // Fields that have dependencies that aren't fulfilled shouldn't be included. Reset them!
        resetValuesForUnfulfilledDependencies(form);
        
        existingForm.get().populateValues(form, ApplicationForm.PopulateValueMode.SKIP_READ_ONLY);
        existingForm.get().updateStatus(ApplicationFormStatusKey.IN_PROGRESS);
        
        // FIXME: This should probably be done _after_ the form has been validated. 
        if (processorEngine.onSubmit(existingForm.get(), application)) {
            // Since the `onSubmit` action might change the available information, we need to re-populate the application.
            replaceFormsWithPopulatedForms(application);
        }

        processorEngine.updateValueAndOptions(application, existingForm.get());
    }
    
    private void resetValuesForUnfulfilledDependencies(ApplicationForm form) {
        if (form.getFields() == null) {
            return;
        }

        for (ApplicationField field : form.getFields()) {
            if (!ApplicationFieldTemplate.isDependencySatisfied(field, form)) {
                field.setValue(null);
            }
        }
    }

    /**
     * Detach a form from the application.
     */
    private void detachForm(Application application, ApplicationForm form) {
        application.getForms().remove(form);
        // TODO: Remove forms recursively.
    }
    
    /**
     * Update the application status, based on the status of its forms.
     */
    private void updateApplicationStatus(Application application) {

        // Don't update the application status if it has already been signed.
        if (application.getStatus().getKey().ordinal() >= ApplicationStatusKey.SIGNED.ordinal()) {
            return;
        }
        
        boolean allFormsCompleted = Iterables.all(application.getForms(),
                Predicates.applicationFormOfStatus(ApplicationFormStatusKey.COMPLETED));

        if (allFormsCompleted) {
            application.updateStatus(ApplicationStatusKey.COMPLETED);
            return;
        }

        boolean allFormsCreated = Iterables.all(application.getForms(),
                Predicates.applicationFormOfStatus(ApplicationFormStatusKey.CREATED));

        if (allFormsCreated) {
            application.updateStatus(ApplicationStatusKey.CREATED);
            return;
        }

        boolean anyFormsDisqualified = Iterables.any(application.getForms(),
                Predicates.applicationFormOfStatus(ApplicationFormStatusKey.DISQUALIFIED));

        if (anyFormsDisqualified) {
            application.updateStatus(ApplicationStatusKey.DISQUALIFIED);
            return;
        }

        application.updateStatus(ApplicationStatusKey.IN_PROGRESS);
    }
    
    private void replaceFormsWithPopulatedForms(Application application) {
        List<ApplicationForm> forms = application.getForms();
        for (int i = 0; i < forms.size(); i++) {
            ApplicationForm form = forms.get(i);
            ApplicationForm updatedForm = getPopulatedForm(application, form);
            forms.remove(i);
            forms.add(i, updatedForm);
        }
        log.info(application, "Forms replaced with populated forms");
    }
    
    private void ensureThatInitialFormsExist(Application application) {

        List<String> formNames = Lists.newArrayList(Iterables.transform(
                Iterables.filter(application.getForms(),
                        form -> {
                            // Initial forms don't have a parent.
                            return form.getParentId() == null;
                        }),
                ApplicationForm::getName));

        for (String formName : processorEngine.initialForms()) {
            if (!formNames.remove(formName)) {
                attachForm(application, getPopulatedForm(application, formName));
            }
        }
    }
    
    private ApplicationForm getPopulatedForm(Application application, String formName) {
        ApplicationForm preparedForm = processorEngine.createNewForm(application, formName);
        valueApplier.populateDynamicFields(preparedForm, user, application);
        return preparedForm;
    }
    
    private ApplicationForm getPopulatedForm(Application application, String formName, UUID parentId) {
        ApplicationForm preparedForm = getPopulatedForm(application, formName);
        preparedForm.setParentId(parentId);
        return preparedForm;
    }
    
    private ApplicationForm getPopulatedForm(Application application, ApplicationForm form) {
        ApplicationForm preparedForm = getPopulatedForm(application, form.getName(), form.getParentId());
        preparedForm.populateValues(form, ApplicationForm.PopulateValueMode.WRITE_READ_ONLY);
        preparedForm.setId(form.getId());
        preparedForm.setStatus(form.getStatus());
        return preparedForm;
    }

    private static final Joiner FORM_NAME_JOINER = Joiner.on(", ");
    
    private void attachAndDetachForms(Application application) {

        List<ApplicationForm> forms = application.getForms();

        List<ApplicationForm> formsToAttach = Lists.newArrayList();
        List<ApplicationForm> formsToRemove = Lists.newArrayList();
    
        for (ApplicationForm form : forms) {
    
            if (Objects.equal(form.getStatus().getKey(), ApplicationFormStatusKey.COMPLETED)) {
                List<String> formNamesToAttach = processorEngine.formsToAttachAfter(form, application, user);
    
                List<ApplicationForm> attachedFormsDueToCurrent = application.getFormNamesByParentId(form.getId(),
                        false);
    
                for (String formNameToAttach : formNamesToAttach) {

                    Optional<ApplicationForm> existingForm = attachedFormsDueToCurrent.stream().filter(f ->
                            Predicates.applicationFormOfName(formNameToAttach).apply(f)).findFirst();

                    if (existingForm.isPresent()) {
                        attachedFormsDueToCurrent.remove(existingForm.get());
                        continue;
                    }
    
                    formsToAttach.add(getPopulatedForm(application, formNameToAttach, form.getId()));
                }
    
                if (attachedFormsDueToCurrent.size() > 0) {
                    formsToRemove.addAll(attachedFormsDueToCurrent);

                    String toRemove = FORM_NAME_JOINER.join(Iterables.transform(attachedFormsDueToCurrent,
                            ApplicationForm::getName));
                    log.info(user.getId(), "attachAndDetachForms => adding to detach list: [" + toRemove + "]");

                    for (ApplicationForm formToRemove : attachedFormsDueToCurrent) {
                        // recursively remove children as well
                        List<ApplicationForm> removeDueToRecursion = application.getFormNamesByParentId(
                                formToRemove.getId(), true);
                        formsToRemove.addAll(removeDueToRecursion);

                        String toRemoveRecursion = FORM_NAME_JOINER.join(Iterables.transform(removeDueToRecursion,
                                ApplicationForm::getName));
                        log.info(user.getId(), "attachAndDetachForms => adding to detach list (due to recursion): [" + toRemoveRecursion + "]");
                    }
                }
            }
        }
    
        for (ApplicationForm formToRemove : formsToRemove) {
            log.info(user.getId(), "attachAndDetachForms => detaching: " + formToRemove.getName());
            detachForm(application, formToRemove);
        }
    
        for (ApplicationForm formToAttach : formsToAttach) {
            log.info(user.getId(), "attachAndDetachForms => attaching: " + formToAttach.getName());
            attachForm(application, formToAttach);
        }
        
        processorEngine.sortForms(application);
        processorEngine.updateNumberOfSteps(application);
    }

    public String getCompiledApplicationAsString(Application application, GenericApplication genericApplication)
            throws JsonProcessingException {
        return processorEngine.getCompiledApplicationAsString(genericApplication, application.getProductArticle());
    }
}
