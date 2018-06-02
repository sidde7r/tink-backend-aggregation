package se.tink.backend.system.product.mortgage;

import java.util.Optional;
import com.google.common.base.Strings;
import com.google.common.collect.FluentIterable;
import com.google.common.collect.ImmutableList;
import com.google.inject.Inject;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import se.tink.backend.common.dao.ApplicationDAO;
import se.tink.backend.common.repository.mysql.main.AccountRepository;
import se.tink.libraries.uuid.UUIDUtils;
import se.tink.libraries.serialization.utils.SerializationUtils;
import se.tink.backend.core.Account;
import se.tink.backend.core.Application;
import se.tink.backend.core.ApplicationField;
import se.tink.backend.core.ApplicationForm;
import se.tink.backend.core.User;
import se.tink.backend.core.enums.ApplicationFieldName;
import se.tink.libraries.application.ApplicationFieldOptionValues;
import se.tink.backend.core.enums.ApplicationFormName;
import se.tink.backend.core.enums.ApplicationStatusKey;
import se.tink.libraries.application.ApplicationType;
import se.tink.backend.core.property.PropertyType;
import se.tink.backend.serialization.TypeReferences;
import se.tink.backend.utils.ApplicationUtils;
import se.tink.backend.utils.LogUtils;
import se.tink.backend.utils.guavaimpl.Orderings;
import se.tink.backend.utils.guavaimpl.Predicates;

public class ApplicationMortgageParameterFinder implements MortgageParameterFinder {
    private final ApplicationDAO applicationDAO;
    private final AccountRepository accountRepository;
    private static final LogUtils log = new LogUtils(ApplicationMortgageParameterFinder.class);

    @Inject
    public ApplicationMortgageParameterFinder(
            ApplicationDAO applicationDAO,
            AccountRepository accountRepository) {
        this.applicationDAO = applicationDAO;
        this.accountRepository = accountRepository;
    }

    @Override
    public MortgageParameters findMortgageParameters(User user) {
        ImmutableList<Application> applications = FluentIterable
                .from(applicationDAO.findByUserId(UUIDUtils.fromTinkUUID(user.getId())))
                .filter(Predicates.applicationIsOfType(ApplicationType.SWITCH_MORTGAGE_PROVIDER))
                .filter(application -> application.getStatus() != null &&
                        application.getStatus().getKey() != null)
                .filter(application -> {
                    Optional<ApplicationForm> productList = ApplicationUtils
                            .getFirst(application, ApplicationFormName.MORTGAGE_PRODUCTS);

                    // We only create product list form when it's available.
                    return productList.isPresent();
                }).toList();

        if (applications.isEmpty()) {
            return new MortgageParameters();
        }

        Application mostRecentApplication = applications.stream().max(Orderings.APPLICATION_BY_CREATED).get();

        MortgageParameters mortgageParameters = new MortgageParameters();

        mortgageParameters.setPropertyType(getPropertyType(mostRecentApplication));
        mortgageParameters.setMarketValue(getMarketValue(mostRecentApplication));
        mortgageParameters.setMortgageAmount(getMortgageAmount(mostRecentApplication));
        mortgageParameters.setNumberOfApplicants(getNumberOfApplicants(mostRecentApplication));

        return mortgageParameters;
    }

    private PropertyType getPropertyType(Application application) {
        // Property type is located on the mortgage security form
        Optional<ApplicationForm> mortgageSecurity = application.getFirstForm(ApplicationFormName.MORTGAGE_SECURITY);
        if (!mortgageSecurity.isPresent()) {
            return null;
        }

        boolean userHasEnteredCustomAddress = ApplicationUtils.isFirstNo(application,
                ApplicationFormName.MORTGAGE_SECURITY,
                ApplicationFieldName.IS_CORRECT_MORTGAGE);

        String propertyTypeField;
        if (userHasEnteredCustomAddress) {
            propertyTypeField = ApplicationFieldName.PROPERTY_TYPE;
        } else {
            propertyTypeField = ApplicationFieldName.DEFAULT_PROPERTY_TYPE;
        }

        // Get the property type value entered in the form
        Optional<ApplicationField> propertyType = ApplicationUtils.getFirst(application,
                ApplicationFormName.MORTGAGE_SECURITY, propertyTypeField);

        if (!propertyType.isPresent() || Strings.isNullOrEmpty(propertyType.get().getValue())) {
            return null;
        }

        // Map string value to correct enums
        switch (propertyType.get().getValue()) {
        case ApplicationFieldOptionValues.APARTMENT:
            return PropertyType.APARTMENT;
        case ApplicationFieldOptionValues.HOUSE:
                return PropertyType.HOUSE;
        case ApplicationFieldOptionValues.VACATION_HOUSE:
            return PropertyType.VACATION_HOUSE;
        default:
            return null;
        }
    }

    private Integer getMarketValue(Application application) {

        // The market value is validated when the current mortgage is validated, to ensure that the value to debt ratio
        // is not too high. So before using the market value, make sure that the current mortgage form has been completed.
        if (!hasCompletedCurrentMortgageForm(application)) {
            return null;
        }

        Optional<ApplicationField> estimatedMarketValue = ApplicationUtils.getFirst(application,
                ApplicationFormName.MORTGAGE_SECURITY_MARKET_VALUE,
                ApplicationFieldName.ESTIMATED_MARKET_VALUE);

        if (!estimatedMarketValue.isPresent() || Strings.isNullOrEmpty(estimatedMarketValue.get().getValue())) {
            return null;
        }

        return Double.valueOf(estimatedMarketValue.get().getValue()).intValue();
    }

    private Integer getMortgageAmount(Application application) {

        // Make sure that the form was actually completed (i.e. has not validation errors) before using its values.
        if (!hasCompletedCurrentMortgageForm(application)) {
            return null;
        }

        Optional<ApplicationField> currentMortgages = ApplicationUtils.getFirst(application,
                ApplicationFormName.CURRENT_MORTGAGES,
                ApplicationFieldName.CURRENT_MORTGAGE);

        if (!currentMortgages.isPresent()) {
            return null;
        }

        // Mortgages are account ids from the tink user
        List<String> accountIds = SerializationUtils.deserializeFromString(
                currentMortgages.get().getValue(),
                TypeReferences.LIST_OF_STRINGS);

        if (accountIds.isEmpty()) {
            return null;
        }

        return getMortgageAmount(application, accountIds);
    }

    private int getMortgageAmount(Application application, List<String> accountIds) {
        String userId = UUIDUtils.toTinkUUID(application.getUserId());

        Map<String, Account> accountById = FluentIterable
                .from(accountRepository.findByUserId(userId))
                .uniqueIndex(Account::getId);

        double loanAmount = 0;

        for (String accountId : accountIds) {
            Account account = accountById.get(accountId);

            if (account == null) {
                // This should never happen, since the mortgages are selected (and verified) from a list based
                // on the user's accounts.
                log.error(userId, String.format("The mortgage account doesn't exist [accountId:%s].", accountId));
                continue;
            }

            loanAmount += Math.abs(account.getBalance());
        }

        return (int) loanAmount;
    }

    private Integer getNumberOfApplicants(Application prioritizedApplications) {
        Optional<ApplicationField> hasCoApplicant = ApplicationUtils.getFirst(prioritizedApplications,
                ApplicationFormName.HAS_CO_APPLICANT,
                ApplicationFieldName.HAS_CO_APPLICANT);

        if (!hasCoApplicant.isPresent()) {
            return null;
        }

        if (Objects.equals(hasCoApplicant.get().getValue(), ApplicationFieldOptionValues.YES)) {
            return 2;
        } else {
            return 1;
        }
    }

    private boolean hasCompletedCurrentMortgageForm(Application application) {
        Optional<ApplicationForm> form = ApplicationUtils.getFirst(application, ApplicationFormName.CURRENT_MORTGAGES);
        return form.isPresent() && Objects.equals(form.get().getStatus().getKey(), ApplicationStatusKey.COMPLETED);
    }
}
