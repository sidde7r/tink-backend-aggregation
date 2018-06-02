package se.tink.backend.common.application.valuation;

import java.util.Optional;
import com.google.common.base.Preconditions;
import com.google.common.base.Strings;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;
import com.google.common.collect.ImmutableSet;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.UUID;
import se.tink.backend.common.ServiceContext;
import se.tink.backend.common.application.ApplicationEngine;
import se.tink.backend.common.application.ApplicationTemplate;
import se.tink.backend.common.application.PropertyUtils;
import se.tink.backend.common.application.field.ApplicationFieldFactory;
import se.tink.backend.common.merchants.GooglePlacesSearcher;
import se.tink.backend.common.repository.mysql.main.PropertyRepository;
import se.tink.backend.common.resources.ReverseGeoLookup;
import se.tink.libraries.uuid.UUIDUtils;
import se.tink.libraries.serialization.utils.SerializationUtils;
import se.tink.backend.core.Application;
import se.tink.backend.core.ApplicationField;
import se.tink.backend.core.ApplicationForm;
import se.tink.backend.core.ApplicationSummary;
import se.tink.backend.core.Coordinate;
import se.tink.backend.core.User;
import se.tink.backend.core.application.ApplicationPropertyKey;
import se.tink.libraries.application.GenericApplication;
import se.tink.libraries.application.GenericApplicationFieldGroup;
import se.tink.backend.core.enums.ApplicationFieldName;
import se.tink.libraries.application.ApplicationFieldOptionValues;
import se.tink.backend.core.enums.ApplicationFormName;
import se.tink.backend.core.enums.ApplicationStatusKey;
import se.tink.backend.core.enums.GenericApplicationFieldGroupNames;
import se.tink.backend.core.product.ProductArticle;
import se.tink.backend.core.property.Property;
import se.tink.backend.utils.ApplicationUtils;

public class ResidenceValuationEngine extends ApplicationEngine {
    private final ResidenceValuationTemplate valuationTemplate;
    private final PropertyRepository propertyRepository;
    private final ReverseGeoLookup reverseGeoLookup;

    public ResidenceValuationEngine(ServiceContext serviceContext,
            ApplicationFieldFactory fieldFactory,
            ApplicationTemplate template, User user) {
        super(ResidenceValuationEngine.class, serviceContext, fieldFactory, template, user);
        this.valuationTemplate = (ResidenceValuationTemplate) template;
        this.propertyRepository = serviceContext.getRepository(PropertyRepository.class);
        this.reverseGeoLookup = new ReverseGeoLookup(new GooglePlacesSearcher());
    }

    private static final ImmutableList<String> INITIAL_FORMS = ImmutableList.<String>builder()
            .add(ApplicationFormName.VALUATION_RESIDENCE_TYPE)
            .build();

    private static final ImmutableList<String> REQUIRED_FORMS = ImmutableList.<String>builder()
            .addAll(INITIAL_FORMS)
            .build();

    @Override
    public List<String> initialForms() {
        return INITIAL_FORMS;
    }

    @Override
    public List<String> requiredForms() {
        return REQUIRED_FORMS;
    }

    @Override
    public boolean onSubmit(ApplicationForm form, Application application) {
        switch (form.getName()) {
        case ApplicationFormName.VALUATION_LOADING:
            return true;
        default:
            return false;
        }
    }

    @Override
    public void resetConfirmation(Application application) {
    }

    @Override
    public void updateValueAndOptions(Application application, ApplicationForm form) {
    }

    @Override
    public List<String> formsToAttachAfter(ApplicationForm form, Application application, User user) {
        switch (form.getName()) {
        case ApplicationFormName.VALUATION_RESIDENCE_TYPE:
            return Lists.newArrayList(
                    getValuationParametersForm(application),
                    ApplicationFormName.VALUATION_LOADING);
        default:
            return Collections.emptyList();
        }
    }

    private String getValuationParametersForm(Application application) {
        Optional<ApplicationField> propertyType = ApplicationUtils.getFirst(application,
                ApplicationFormName.VALUATION_RESIDENCE_TYPE,
                ApplicationFieldName.VALUATION_RESIDENCE_TYPE);

        if (!propertyType.isPresent()) {
            throw new IllegalStateException("Application should have property type present.");
        }

        if (Objects.equals(propertyType.get().getValue(), ApplicationFieldOptionValues.APARTMENT)) {
            return ApplicationFormName.VALUATION_APARTMENT_PARAMETERS;
        } else {
            return ApplicationFormName.VALUATION_HOUSE_PARAMETERS;
        }
    }

    @Override
    public List<GenericApplicationFieldGroup> getGenericApplicationFieldGroups(Application application) {
        GenericApplicationFieldGroup residence = getResidenceGroup(application);
        GenericApplicationFieldGroup property = getPropertyGroup(application);

        return Lists.newArrayList(residence, property);
    }

    private GenericApplicationFieldGroup getResidenceGroup(Application application) {
        GenericApplicationFieldGroup group = new GenericApplicationFieldGroup();

        group.setName(GenericApplicationFieldGroupNames.RESIDENCE);
        group.setFields(getResidenceFields(application));
        return group;
    }

    private Map<String, String> getResidenceFields(Application application) {
        Map<String, String> fields = Maps.newHashMap();

        ArrayList<String> formNames = Lists.newArrayList(INITIAL_FORMS);
        formNames.add(getValuationParametersForm(application));

        for (String formName : formNames) {
            ImmutableSet<String> fieldNames = valuationTemplate.getFieldNames(formName);
            for (String fieldName : fieldNames) {
                Optional<String> value = getValue(application, formName, fieldName);

                if (!value.isPresent()) {
                    continue;
                }

                fields.put(fieldName, value.get());
            }
        }

        return fields;
    }

    private static Optional<String> getValue(Application application, String formName, String fieldName) {
        Optional<ApplicationField> field = ApplicationUtils.getFirst(application, formName, fieldName);

        if (!field.isPresent()) {
            return Optional.empty();
        }

        String value = field.get().getValue();

        if (Strings.isNullOrEmpty(value)) {
            return Optional.empty();
        }

        return Optional.of(value);
    }

    private GenericApplicationFieldGroup getPropertyGroup(Application application) {
        Property property = getProperty(application);

        GenericApplicationFieldGroup group = new GenericApplicationFieldGroup();
        group.setName(GenericApplicationFieldGroupNames.PROPERTY);
        group.setFields(getPropertyFields(property));

        return group;
    }

    private Map<String, String> getPropertyFields(Property property) {
        HashMap<String, String> fields = Maps.newHashMap();

        fields.put(ApplicationFieldName.PROPERTY_ID, property.getId());
        fields.put(ApplicationFieldName.STREET_ADDRESS, property.getAddress());
        fields.put(ApplicationFieldName.CITY, property.getCity());
        fields.put(ApplicationFieldName.COMMUNITY, property.getCommunity());
        fields.put(ApplicationFieldName.POSTAL_CODE, property.getPostalCode());

        Coordinate coordinate = getCoordinate(property);
        if (coordinate != null) {
            fields.put(ApplicationFieldName.LATITUDE, String.valueOf(coordinate.getLatitude()));
            fields.put(ApplicationFieldName.LONGITUDE, String.valueOf(coordinate.getLongitude()));
        }

        return fields;
    }

    private Coordinate getCoordinate(Property property) {
        String addressString = String.format(
                "%s, %s %s",
                PropertyUtils.cleanStreetAddress(property.getAddress()),
                property.getPostalCode(),
                property.getCity());

        try {
            return reverseGeoLookup.getCoordinate(addressString).orElse(null);
        } catch (Exception e) {
            throw new IllegalStateException(
                    String.format("Expected geo code result for address: %s.", addressString),
                    e);
        }
    }

    @Override
    public String getPersonalNumber(Application application) {
        return null;
    }

    @Override
    public UUID getProductId(Application application) {
        String productId = (String) application.getProperties().get(ApplicationPropertyKey.PRODUCT_INSTANCE_ID);

        if (Strings.isNullOrEmpty(productId)) {
            return null;
        }

        return UUIDUtils.fromTinkUUID(productId);
    }

    @Override
    public ApplicationSummary getSummary(Application application) {
        Property property = getProperty(application);

        ApplicationSummary summary = new ApplicationSummary();
        summary.setTitle(getTitle(application.getStatus().getKey()));
        summary.setDescription(getDescription(application.getStatus().getKey()));
        summary.setStatusPayload(getApplicationSummaryStatusPayload(application.getStatus().getKey(), property.getMostRecentValuation()));
        summary.setStatusTitle(getApplicationSummaryStatusTitle(application.getStatus().getKey()));

        return summary;
    }

    private String getDescription(ApplicationStatusKey key) {
        switch (key) {
        case SIGNED:
            return "Värderingen är en uppskattning baserat på liknande bostäder som sålts i ditt område."; // TODO: Display some explanation of precision
        case ERROR:
            return "Tyvärr gick din bostad inte att värdera. Det kan bero på att det inte finns tillräckligt med sålda bostäder i ditt område att basera värderingen på.";
        default:
            return null;
        }
    }

    private String getTitle(ApplicationStatusKey key) {
        switch (key) {
        case SIGNED:
            return "Din bostad är värderad till";
        case ERROR:
            return "Vi kunde inte värdera din bostad";
        default:
            return "Fyll i frågorna för att göra klart värderingen.";
        }
    }

    private String getApplicationSummaryStatusPayload(ApplicationStatusKey key, Integer value) {
        if (value == null) {
            return null;
        }

        List<Map<String, String>> payload = Lists.newArrayList();

        switch (key) {
        case SIGNED:
            payload.add(ImmutableMap.<String,String>builder()
                    .put("value", value.toString())
                    .build());
            return SerializationUtils.serializeToString(payload);
        default:
            return null;
        }
    }

    private String getApplicationSummaryStatusTitle(ApplicationStatusKey key) {
        switch (key) {
        case SIGNED:
            return "Värdering gjord!";
        case ERROR:
            return "Värdering misslyckades.";
        default:
            return "Värdering påbörjad.";
        }
    }

    @Override
    public String getCompiledApplicationAsString(GenericApplication genericApplication,
            Optional<ProductArticle> productArticle) {
        // We're not showing a compiled version to the user
        return null;
    }

    private Property getProperty(Application application) {
        return Preconditions.checkNotNull(propertyRepository.findByUserIdAndId(
                UUIDUtils.toTinkUUID(application.getUserId()),
                (String) application.getProperties().get(ApplicationPropertyKey.PROPERTY_ID)));
    }
}
