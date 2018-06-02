package se.tink.backend.common.application;

import java.util.Optional;
import com.google.common.collect.ImmutableSet;
import com.google.common.collect.ListMultimap;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.google.common.collect.Ordering;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Objects;
import java.util.UUID;
import se.tink.backend.common.ServiceContext;
import se.tink.backend.common.application.field.ApplicationFieldFactory;
import se.tink.backend.common.dao.ProductDAO;
import se.tink.backend.core.Application;
import se.tink.backend.core.ApplicationField;
import se.tink.backend.core.ApplicationForm;
import se.tink.backend.core.ApplicationSummary;
import se.tink.backend.core.User;
import se.tink.backend.core.application.ApplicationPropertyKey;
import se.tink.libraries.application.GenericApplication;
import se.tink.libraries.application.GenericApplicationFieldGroup;
import se.tink.backend.core.enums.ApplicationFormStatusKey;
import se.tink.backend.core.product.ProductArticle;
import se.tink.backend.utils.LogUtils;

public abstract class ApplicationEngine {

    protected final LogUtils log;

    private static final Comparator<ApplicationForm> ORDER_CHILDREN_AFTER_PARENT = (o1, o2) -> {
        if (o1.getParentId() == null) {
            return -1;
        }

        if (o2.getParentId() == null) {
            return 1;
        }

        if (o1.getParentId().equals(o2.getId())) {
            return 1;
        } else if (o2.getParentId().equals(o1.getId())) {
            return -1;
        }

        return 0;
    };

    protected final ApplicationTemplate template;
    protected final User user;
    protected final ServiceContext serviceContext;
    protected final ProductDAO productDAO;
    
    private final ApplicationFieldFactory fieldFactory;
    private final Comparator<ApplicationForm> formComparator;

    public ApplicationEngine(Class<? extends ApplicationEngine> clazz, ServiceContext serviceContext,
            final ApplicationFieldFactory fieldFactory, final ApplicationTemplate template, User user) {

        this.log = new LogUtils(clazz);
        this.fieldFactory = fieldFactory;
        
        this.serviceContext = serviceContext;
        this.productDAO = serviceContext.getDao(ProductDAO.class);
        this.template = template;
        this.user = user;

        List<Comparator<ApplicationForm>> comparators = Lists.newArrayList();
        comparators.add(template.getFormComparator());
        comparators.add(ORDER_CHILDREN_AFTER_PARENT);

        formComparator = Ordering.compound(comparators);
    }

    /*
     * List of form names that should be available from start.
     */
    public abstract List<String> initialForms();
    
    /*
     * The minimal steps required to complete the application.   
     */
    public abstract List<String> requiredForms();
    
    /*
     * Return `true` if an action was performed due to the submitted form. Otherwise `false`.
     */
    public abstract boolean onSubmit(ApplicationForm form, Application application);

    public abstract void resetConfirmation(Application application);
    
    public abstract void updateValueAndOptions(Application application, ApplicationForm form);

    public abstract List<String> formsToAttachAfter(ApplicationForm form, Application application, User user);
    
    public abstract List<GenericApplicationFieldGroup> getGenericApplicationFieldGroups(Application application);
    
    public abstract String getPersonalNumber(Application application);
    
    public abstract UUID getProductId(Application application);
    
    public abstract ApplicationSummary getSummary(Application application);

    public void attachProduct(Application application) {
        UUID productId = getProductId(application);
        
        if (productId == null) {
            return;
        }

        ProductArticle productArticle = productDAO.findArticleByUserIdAndId(application.getUserId(), productId);

        application.setProductArticle(productArticle);

        // FIXME: Remove this try/catch temp fix and ensure no NPE's will be thrown
        try {
            // We want to store the product article on the application properties to get it when product not attached.
            if (application.getProperties() == null) {
                log.warn("applicationProperties = null. Initializing.");
                application.setProperties(Maps.<ApplicationPropertyKey, Object>newHashMap());
            }

            application.getProperties().put(
                    ApplicationPropertyKey.PRODUCT_INSTANCE_ID, productArticle.getInstanceId().toString());
            application.getProperties().put(
                    ApplicationPropertyKey.PRODUCT_PROVIDER_NAME, productArticle.getProviderName());
        } catch (Exception e) {
            log.warn("Couldn't set product info on application", e);
        }
    }
    
    public void updateNumberOfSteps(Application application) {
        
        List<String> requiredForms = Lists.newArrayList(requiredForms());
        
        for (ApplicationForm form : application.getForms()) {
            if (requiredForms.remove(form.getName()) && requiredForms.isEmpty()) {
                break;
            }
        }
        
        application.setSteps(application.getForms().size() + requiredForms.size());
    }

    public void sortForms(Application application) {
        Collections.sort(application.getForms(), formComparator);
    }

    public ApplicationForm createNewForm(Application application, String formName, UUID parentId) {

        ApplicationForm form = createNewForm(application, formName);
        form.setParentId(parentId);
        return form;
    }
    
    public ApplicationForm createNewForm(Application application, String formName) {

        ApplicationForm form = template.createEmptyForm(application, formName);

        List<ApplicationField> fields = Lists.newArrayList();

        ImmutableSet<String> fieldsNames = template.getFieldNames(form.getName());
        for (String fieldName : fieldsNames) {
            fields.add(fieldFactory.createFromName(fieldName));
        }

        form.setFields(fields);

        return form;
    }
    
    protected boolean fieldValueEquals(final ApplicationField field, final String expectedValue) {
        return Objects.equals(field.getValue(), expectedValue);
    }
    
    protected static void populateFieldFromForm(GenericApplicationFieldGroup group, ApplicationForm form, String fieldName) {
        group.putField(fieldName, form.getFieldValue(fieldName).orElse(null));
    }
    
    protected static void populateFieldFromForm(GenericApplicationFieldGroup group, Optional<ApplicationForm> form, String fieldName) {
        if (!form.isPresent()) {
            return;
        }
        
        group.putField(fieldName, form.get().getFieldValue(fieldName).orElse(null));
    }
    
    public void resetForm(Application application, String formName) {
        Optional<ApplicationForm> form = application.getFirstForm(formName);
        
        if (form.isPresent() && !Objects.equals(form.get().getStatus().getKey(), ApplicationFormStatusKey.CREATED)) {
            
            form.get().updateStatus(ApplicationFormStatusKey.CREATED);
            
            for (ApplicationField field : form.get().getFields()) {
                field.setValue(null);
            }
        }
    }
    
    protected static Optional<ApplicationForm> getFirst(ListMultimap<String, ApplicationForm> formsByName, String formName) {
        
        List<ApplicationForm> forms = formsByName.get(formName);

        if (forms.isEmpty()) {
            return Optional.empty();
        }
        
        return Optional.of(forms.get(0));
    }

    public abstract String getCompiledApplicationAsString(GenericApplication genericApplication,
            Optional<ProductArticle> productArticle);
}
