package se.tink.backend.utils;

import com.fasterxml.jackson.core.type.TypeReference;
import com.google.common.base.Strings;
import com.google.common.collect.FluentIterable;
import com.google.common.collect.ImmutableListMultimap;
import com.google.common.collect.ImmutableMap;
import com.google.common.collect.Iterables;
import com.google.common.collect.ListMultimap;
import com.google.common.collect.Lists;
import java.util.Collection;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import se.tink.libraries.serialization.utils.SerializationUtils;
import se.tink.backend.core.Application;
import se.tink.backend.core.ApplicationField;
import se.tink.backend.core.ApplicationFieldError;
import se.tink.backend.core.ApplicationFieldOption;
import se.tink.backend.core.ApplicationForm;
import se.tink.libraries.application.GenericApplication;
import se.tink.libraries.application.GenericApplicationFieldGroup;
import se.tink.backend.core.enums.ApplicationFieldName;
import se.tink.libraries.application.ApplicationFieldOptionValues;
import se.tink.backend.core.enums.ApplicationFormName;
import se.tink.libraries.application.ApplicationType;
import se.tink.backend.core.enums.GenericApplicationFieldGroupNames;
import se.tink.backend.core.product.ProductType;
import se.tink.backend.serialization.TypeReferences;
import se.tink.backend.utils.guavaimpl.Predicates;

public class ApplicationUtils {

    private static final TypeReference<Map<String, String>> TYPE_REFERENCE_MAP_STRING_STRING = new TypeReference<Map<String, String>>() {
    };
    public static final Comparator<ApplicationFieldOption> APPLICATION_FIELD_OPTION_INTEREST_ORDERING = Comparator
            .comparing(option -> {
                Map<String, String> map = SerializationUtils
                        .deserializeFromString(option.getSerializedPayload(), TYPE_REFERENCE_MAP_STRING_STRING);
                double rate = 0d;

                if (!Strings.isNullOrEmpty(map.get("interest"))) {
                    rate = Double.parseDouble(map.get("interest"));
                }

                return rate;
            });

    private static final ImmutableMap<ProductType, ApplicationType> APPLICATION_TYPE_BY_PRODUCT_TYPE = ImmutableMap.<ProductType, ApplicationType>builder()
            .put(ProductType.MORTGAGE, ApplicationType.SWITCH_MORTGAGE_PROVIDER)
            .put(ProductType.SAVINGS_ACCOUNT, ApplicationType.OPEN_SAVINGS_ACCOUNT)
            .put(ProductType.RESIDENCE_VALUATION, ApplicationType.RESIDENCE_VALUATION)
            .build();

    public static Optional<GenericApplicationFieldGroup> getApplicant(
            ListMultimap<String, GenericApplicationFieldGroup> fieldGroupByName) {

        return getApplicant(fieldGroupByName, 0);
    }

    public static Optional<GenericApplicationFieldGroup> getCoApplicant(
            ListMultimap<String, GenericApplicationFieldGroup> fieldGroupByName) {

        return getApplicant(fieldGroupByName, 1);
    }

    public static Optional<GenericApplicationFieldGroup> getApplicant(
            ListMultimap<String, GenericApplicationFieldGroup> fieldGroupByName, int index) {

        Optional<GenericApplicationFieldGroup> group = getFirst(fieldGroupByName,
                GenericApplicationFieldGroupNames.APPLICANTS);

        if (!group.isPresent()) {
            return Optional.empty();
        }

        return getSubGroup(group.get(), index);
    }

    public static int getNumberOfApplicants(ListMultimap<String, GenericApplicationFieldGroup> fieldGroupByName) {
        Optional<GenericApplicationFieldGroup> applicants = ApplicationUtils.getFirst(fieldGroupByName,
                GenericApplicationFieldGroupNames.APPLICANTS);

        if (!applicants.isPresent() || applicants.get().getSubGroups() == null) {
            return 0;
        }

        return applicants.get().getSubGroups().size();
    }

    public static String getMortgageSecurityPropertyType(Application application) {
        Optional<String> mortgageSecurityPropertyType = Optional.empty();

        Optional<ApplicationForm> mortgageSecurityForm = ApplicationUtils.getFirst(application,
                ApplicationFormName.MORTGAGE_SECURITY);

        if (ApplicationUtils.isYes(mortgageSecurityForm, ApplicationFieldName.IS_CORRECT_MORTGAGE)) {
            mortgageSecurityPropertyType = mortgageSecurityForm.get().getFieldValue(
                    ApplicationFieldName.DEFAULT_PROPERTY_TYPE);
        } else {
            mortgageSecurityPropertyType = mortgageSecurityForm.get().getFieldValue(
                    ApplicationFieldName.MORTGAGE_SECURITY_PROPERTY_TYPE);
        }

        // Default to house.
        return mortgageSecurityPropertyType.orElse(ApplicationFieldOptionValues.HOUSE);
    }

    public static Optional<GenericApplicationFieldGroup> getFirst(
            ListMultimap<String, GenericApplicationFieldGroup> fieldGroupByName, String groupName) {
        List<GenericApplicationFieldGroup> applicantFieldGroups = fieldGroupByName.get(groupName);

        if (applicantFieldGroups.isEmpty()) {
            return Optional.empty();
        }

        return Optional.of(applicantFieldGroups.get(0));
    }

    public static Optional<GenericApplicationFieldGroup> getSubGroup(GenericApplicationFieldGroup group, int index) {
        if (index < 0 || index >= group.getSubGroups().size()) {
            return Optional.empty();
        }

        return Optional.of(group.getSubGroups().get(index));
    }

    public static Optional<GenericApplicationFieldGroup> getFirstSubGroup(Optional<GenericApplicationFieldGroup> group, String groupName) {
        if (!group.isPresent()){
            return Optional.empty();
        }
        return getFirstSubGroup(group.get(), groupName);
    }

    public static Optional<GenericApplicationFieldGroup> getFirstSubGroup(GenericApplicationFieldGroup group, String groupName) {
        List<GenericApplicationFieldGroup> groups = getSubGroups(group, groupName);
        if (groups.isEmpty()) {
            return Optional.empty();
        }

        return Optional.ofNullable(groups.get(0));
    }

    public static Optional<ApplicationForm> getFirst(Application application, String formName) {
        return getForm(application, formName, 0);
    }

    public static Optional<ApplicationForm> getForm(Application application, String formName, int index) {
        List<ApplicationForm> forms = getForms(application, formName);

        if (index >= forms.size()) {
            return Optional.empty();
        }

        return Optional.of(forms.get(index));
    }

    public static List<ApplicationForm> getForms(Application application, String formName) {
        if (application == null || application.getForms() == null || application.getForms().isEmpty()) {
            return Lists.newArrayList();
        }

        return Lists.newArrayList(Iterables.filter(application.getForms(), Predicates.applicationFormOfName(formName)));
    }

    public static Optional<ApplicationField> getField(Optional<ApplicationForm> form, String fieldName) {
        if (!form.isPresent()) {
            return Optional.empty();
        }

        return form.get().getField(fieldName);
    }

    public static Optional<ApplicationField> getFirst(Application application, String formName, String fieldName) {
        return getField(getFirst(application, formName), fieldName);
    }

    public static boolean isNo(ApplicationForm form, String fieldName) {
        return Objects.equals(ApplicationFieldOptionValues.NO, form.getFieldValue(fieldName).orElse(null));
    }

    public static boolean isNo(Optional<ApplicationForm> form, String fieldName) {
        if (!form.isPresent()) {
            return false;
        }

        return isNo(form.get(), fieldName);
    }

    public static boolean isFirstNo(Application application, String formName, String fieldName) {
        return isNo(getFirst(application, formName), fieldName);
    }

    public static boolean isYes(Optional<ApplicationForm> form, String fieldName) {
        if (!form.isPresent()) {
            return false;
        }

        return isYes(form.get(), fieldName);
    }

    public static boolean isYes(ApplicationForm form, String fieldName) {
        return Objects.equals(ApplicationFieldOptionValues.YES, form.getFieldValue(fieldName).orElse(null));
    }

    public static boolean isFirstYes(Application application, String formName, String fieldName) {
        return isYes(getFirst(application, formName), fieldName);
    }

    public static boolean isFalse(ApplicationForm form, String fieldName) {
        return Objects.equals(ApplicationFieldOptionValues.FALSE, form.getFieldValue(fieldName).orElse(null));
    }

    public static Optional<String> getFirstErrorMessage(ApplicationField field) {

        if (!field.hasError()) {
            return Optional.empty();
        }

        for (ApplicationFieldError error : field.getErrors()) {
            if (!Strings.isNullOrEmpty(error.getMessage())) {
                return Optional.of(error.getMessage());
            }
        }

        return Optional.empty();
    }

    /**
     * @return field.type == MULTI_SELECT ? field.values.contains(expectedValue) : value.equals(expectedValue)
     */
    public static boolean isFieldContainingExpectedValue(ApplicationField field, Collection<String> expectedValue) {
        String fieldValue = field.getValue();

        if (fieldValue == null) {
            return false;
        }

        switch (field.getType()) {
        case MULTI_SELECT:
            // Multi select has list of values, not one item
            List<String> dependencyFieldValues = SerializationUtils.deserializeFromString(fieldValue,
                    TypeReferences.LIST_OF_STRINGS);

            if (dependencyFieldValues == null || expectedValue == null) {
                return false;
            }

            for (String dependencyFieldValue : dependencyFieldValues) {
                if (dependencyFieldValue != null && expectedValue.contains(dependencyFieldValue)) {
                    return true;
                }
            }

            return false;
        default:
            // Regular singular value
            return expectedValue != null && expectedValue.contains(fieldValue);
        }
    }

    public static ApplicationFieldOption getOptionWithLowestInterestRate(ApplicationForm form) {
        List<ApplicationFieldOption> chosenOptions = getChosenMortgages(form);
        if (chosenOptions != null && !chosenOptions.isEmpty()) {
            return chosenOptions.stream().min(APPLICATION_FIELD_OPTION_INTEREST_ORDERING).get();
        }

        return null;
    }

    public static List<ApplicationFieldOption> getChosenMortgages(ApplicationForm form) {
        Optional<ApplicationField> field = form.getField(ApplicationFieldName.CURRENT_MORTGAGE);
        if (!field.isPresent()) {
            return null;
        }

        List<ApplicationFieldOption> options = Lists.newArrayList();
        String value = field.get().getValue();
        for (ApplicationFieldOption option : field.get().getOptions()) {
            if (value.contains(option.getValue())) {
                options.add(option);
            }
        }

        return options;
    }

    public static List<GenericApplicationFieldGroup> getSubGroups(Optional<GenericApplicationFieldGroup> group,
            String name) {
        if (!group.isPresent()) {
            return Lists.newArrayList();
        }

        return getSubGroups(group.get(), name);
    }

    public static List<GenericApplicationFieldGroup> getSubGroups(GenericApplicationFieldGroup group, String name) {
        if (group == null || group.getSubGroups() == null || group.getSubGroups().isEmpty()) {
            return Lists.newArrayList();
        }

        return Lists.newArrayList(Iterables.filter(group.getSubGroups(), Predicates.fieldGroupByName(name)));
    }

    public static ApplicationType getApplicationType(ProductType productType) {
        return APPLICATION_TYPE_BY_PRODUCT_TYPE.get(productType);
    }

    public static ImmutableListMultimap<String, GenericApplicationFieldGroup> getGroupsByName(
            GenericApplication application) {
        return FluentIterable
                .from(application.getFieldGroups())
                .index(GenericApplicationFieldGroup::getName);
    }
}
