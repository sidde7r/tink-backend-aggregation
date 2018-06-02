package se.tink.backend.application;

import com.google.api.client.util.Objects;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import se.tink.backend.auth.AuthenticatedUser;
import se.tink.backend.combined.AbstractServiceIntegrationTest;
import se.tink.backend.common.application.ApplicationNotValidException;
import se.tink.backend.common.dao.ProductDAO;
import se.tink.backend.common.exceptions.FeatureFlagNotEnabledException;
import se.tink.backend.common.repository.mysql.main.AccountRepository;
import se.tink.backend.core.Account;
import se.tink.backend.core.AccountTypes;
import se.tink.backend.core.Application;
import se.tink.backend.core.ApplicationField;
import se.tink.backend.core.ApplicationFieldOption;
import se.tink.backend.core.ApplicationForm;
import se.tink.backend.core.User;
import se.tink.backend.core.enums.ApplicationFieldName;
import se.tink.backend.core.enums.ApplicationFormName;
import se.tink.backend.core.product.ProductInstance;
import se.tink.backend.core.product.ProductTemplate;
import se.tink.backend.core.product.ProductTemplateStatus;
import se.tink.backend.core.product.ProductType;
import se.tink.backend.main.controllers.ApplicationServiceController;
import se.tink.backend.rpc.application.CreateApplicationCommand;
import se.tink.libraries.account.AccountIdentifier;
import se.tink.libraries.application.ApplicationFieldOptionValues;
import se.tink.libraries.application.ApplicationType;
import se.tink.libraries.auth.HttpAuthenticationMethod;
import se.tink.libraries.serialization.utils.SerializationUtils;
import se.tink.libraries.uuid.UUIDUtils;

public class OpenSavingsAccountIntegrationTestBase extends AbstractServiceIntegrationTest {

    public static final String OPEN_SAVING_ACCOUNT_COLLECTOR_ID = "f786e364e9fb41588048a352eb8e4a3c";
    public static final String OPEN_SAVING_ACCOUNT_SBAB_ID = "df58b1b92149410dbb32ef43ef820d2e";

    private AuthenticatedUser authenticatedUser;
    boolean hasSetSecondCountry = false;
    boolean hasSetThirdCountry = false;

    protected Application createApplication(ApplicationServiceController applicationServiceController,
            Optional<String> userAgent) throws FeatureFlagNotEnabledException, ApplicationNotValidException {
        CreateApplicationCommand command = new CreateApplicationCommand(authenticatedUser.getUser(), userAgent,
                ApplicationType.OPEN_SAVINGS_ACCOUNT);
        Application application = applicationServiceController.createApplication(command);

        return application;
    }

    protected AuthenticatedUser getAuthenticatedUser() {
        if (authenticatedUser == null) {

            try {
                User user = getTestUser("karl.karlsson@tink.se");

                authenticatedUser = new AuthenticatedUser(HttpAuthenticationMethod.BASIC, user);

                createAndSaveProducts();
                createAndSaveAccounts();

            } catch (Exception e) {
                e.printStackTrace();
            }
        }

        return authenticatedUser;
    }

    public void fillFormUsingCollector(ApplicationForm form) {
        boolean isSBAB = false;
        boolean isTaxableInOtherCountry = false;
        boolean isTaxableInASecondCountry = false;
        boolean hasCitizenshipInOtherCountry = false;
        boolean hasCitizenshipInASecondCountry = false;
        boolean hasCitizenshipInAThirdCountry = false;
        fillForm(form, isSBAB, isTaxableInOtherCountry, isTaxableInASecondCountry, hasCitizenshipInOtherCountry, hasCitizenshipInASecondCountry, hasCitizenshipInAThirdCountry);
    }

    public void fillFormUsingSBAB(ApplicationForm form) {
        boolean isSBAB = true;
        boolean isTaxableInOtherCountry = false;
        boolean isTaxableInASecondCountry = false;
        boolean hasCitizenshipInOtherCountry = false;
        boolean hasCitizenshipInASecondCountry = false;
        boolean hasCitizenshipInAThirdCountry = false;
        fillForm(form, isSBAB, isTaxableInOtherCountry, isTaxableInASecondCountry, hasCitizenshipInOtherCountry, hasCitizenshipInASecondCountry, hasCitizenshipInAThirdCountry);
    }

    public void fillFormUsingSBABWithTaxableInOtherCountry(ApplicationForm form) {
        boolean isSBAB = true;
        boolean isTaxableInOtherCountry = true;
        boolean isTaxableInASecondCountry = false;
        boolean hasCitizenshipInOtherCountry = false;
        boolean hasCitizenshipInASecondCountry = false;
        boolean hasCitizenshipInAThirdCountry = false;
        fillForm(form, isSBAB, isTaxableInOtherCountry, isTaxableInASecondCountry, hasCitizenshipInOtherCountry, hasCitizenshipInASecondCountry, hasCitizenshipInAThirdCountry);
    }

    public void fillFormUsingSBABWithTaxableSecondCountry(ApplicationForm form) {
        boolean isSBAB = true;
        boolean isTaxableInOtherCountry = true;
        boolean isTaxableInASecondCountry = true;
        boolean hasCitizenshipInOtherCountry = false;
        boolean hasCitizenshipInASecondCountry = false;
        boolean hasCitizenshipInAThirdCountry = false;
        fillForm(form, isSBAB, isTaxableInOtherCountry, isTaxableInASecondCountry, hasCitizenshipInOtherCountry, hasCitizenshipInASecondCountry, hasCitizenshipInAThirdCountry);
    }

    public void fillFormUsingSBABWithCitizenshipInOtherCountry(ApplicationForm form) {
        boolean isSBAB = true;
        boolean isTaxableInOtherCountry = false;
        boolean isTaxableInASecondCountry = false;
        boolean hasCitizenshipInOtherCountry = true;
        boolean hasCitizenshipInASecondCountry = false;
        boolean hasCitizenshipInAThirdCountry = false;
        fillForm(form, isSBAB, isTaxableInOtherCountry, isTaxableInASecondCountry, hasCitizenshipInOtherCountry, hasCitizenshipInASecondCountry, hasCitizenshipInAThirdCountry);
    }

    public void fillFormUsingSBABWithCitizenshipInASecondCountry(ApplicationForm form) {
        boolean isSBAB = true;
        boolean isTaxableInOtherCountry = false;
        boolean isTaxableInASecondCountry = false;
        boolean hasCitizenshipInOtherCountry = true;
        boolean hasCitizenshipInASecondCountry = true;
        boolean hasCitizenshipInAThirdCountry = false;
        fillForm(form, isSBAB, isTaxableInOtherCountry, isTaxableInASecondCountry, hasCitizenshipInOtherCountry, hasCitizenshipInASecondCountry, hasCitizenshipInAThirdCountry);
    }

    public void fillFormUsingSBABWithCitizenshipInAThirdCountry(ApplicationForm form) {
        boolean isSBAB = true;
        boolean isTaxableInOtherCountry = false;
        boolean isTaxableInASecondCountry = false;
        boolean hasCitizenshipInOtherCountry = true;
        boolean hasCitizenshipInASecondCountry = true;
        boolean hasCitizenshipInAThirdCountry = true;
        fillForm(form, isSBAB, isTaxableInOtherCountry, isTaxableInASecondCountry, hasCitizenshipInOtherCountry, hasCitizenshipInASecondCountry, hasCitizenshipInAThirdCountry);
    }

    private void fillForm(
            ApplicationForm form,
            boolean isSBAB,
            boolean isTaxableInOtherCountry,
            boolean isTaxableInASecondCountry,
            boolean hasCitizenshipInOtherCountry,
            boolean hasCitizenshipInASecondCountry,
            boolean hasCitizenshipInAThirdCountry) {

        if (Objects.equal(form.getName(), ApplicationFormName.OPEN_SAVINGS_ACCOUNT_PRODUCTS)) {
            fillSavingsProductsFields(form, isSBAB);

        } else if (Objects.equal(form.getName(), ApplicationFormName.OPEN_SAVINGS_ACCOUNT_PRODUCT_DETAILS)) {
            // Does not have any fields.

        } else if (Objects.equal(form.getName(), ApplicationFormName.OPEN_SAVINGS_ACCOUNT_APPLICANT)) {
            fillApplicantForm(form);

        } else if (Objects.equal(form.getName(), ApplicationFormName.OPEN_SAVINGS_ACCOUNT_SWEDISH_CITIZEN)) {
            fillSwedishCitizenForm(form);

        } else if (Objects.equal(form.getName(), ApplicationFormName.COLLECTOR_OPEN_SAVINGS_ACCOUNT_KYC)) {
            fillSavingsKYCCollectorFields(form);

        } else if (Objects.equal(form.getName(), ApplicationFormName.SBAB_OPEN_SAVINGS_ACCOUNT_KYC_SAVINGS_PURPOSE)) {
            fillSavingsKycSbabSavingsPurposeFields(form);

        } else if (Objects.equal(form.getName(), ApplicationFormName.SBAB_OPEN_SAVINGS_ACCOUNT_KYC_INITIAL_DEPOSIT)) {
            fillSavingsKycSbabInitialDepositFields(form);

        } else if (Objects.equal(form.getName(), ApplicationFormName.SBAB_OPEN_SAVINGS_ACCOUNT_KYC_SAVINGS_FREQUENCY)) {
            fillSavingsKycSbabSavingsFrequencyFields(form);

        } else if (Objects.equal(form.getName(), ApplicationFormName.SBAB_OPEN_SAVINGS_ACCOUNT_KYC_PERSONS_SAVING)) {
            fillSavingsKycSbabPersonsSavingFields(form);

        } else if (Objects.equal(form.getName(), ApplicationFormName.SBAB_OPEN_SAVINGS_ACCOUNT_KYC_SAVINGS_SOURCES)) {
            fillSavingsKycSbabSavingsSourcesFields(form);

        } else if (Objects.equal(form.getName(), ApplicationFormName.SBAB_OPEN_SAVINGS_ACCOUNT_KYC_SAVINGS_AMOUNT_PER_MONTH)) {
            fillSavingsKycSbabSavingsAmountPerMonthFields(form);

        } else if (Objects.equal(form.getName(), ApplicationFormName.SBAB_OPEN_SAVINGS_ACCOUNT_KYC_SAVINGS_SOURCES_REASON)) {
            fillSavingsKycSbabSavingsSourcesReasonFields(form);

        } else if (Objects.equal(form.getName(), ApplicationFormName.SBAB_OPEN_SAVINGS_ACCOUNT_KYC_WITHDRAWAL)) {
            fillSavingsKycSbabWithdrawalFields(form);

        } else if (Objects.equal(form.getName(), ApplicationFormName.SBAB_OPEN_SAVINGS_ACCOUNT_KYC_MONTHLY_INCOME)) {
            fillSavingsKycSbabSavingsMonthlyIncomeFields(form);

        } else if (Objects.equal(form.getName(), ApplicationFormName.OPEN_SAVINGS_ACCOUNT_PEP)) {
            fillKYCForIsPepForm(form);

        } else if (Objects.equal(form.getName(), ApplicationFormName.OPEN_SAVINGS_ACCOUNT_CITIZENSHIP_IN_OTHER_COUNTRY)) {
            fillKYCForCitizenshipInOtherCountryForm(form, hasCitizenshipInOtherCountry);

        } else if (Objects.equal(form.getName(), ApplicationFormName.OPEN_SAVINGS_ACCOUNT_CITIZENSHIP_IN_YET_ANOTHER_COUNTRY)) {
            fillKYCForCitizenshipInYetAnotherCountryForm(form, hasCitizenshipInASecondCountry, hasCitizenshipInAThirdCountry);

        } else if (Objects.equal(form.getName(), ApplicationFormName.OPEN_SAVINGS_ACCOUNT_TAXABLE_IN_USA)) {
            fillTaxableInUSAForm(form);

        } else if (Objects.equal(form.getName(), ApplicationFormName.COLLECTOR_OPEN_SAVINGS_ACCOUNT_TAXABLE_IN_USA)) {
            fillTaxableInUSAForm(form);

        } else if (Objects.equal(form.getName(), ApplicationFormName.OPEN_SAVINGS_ACCOUNT_TAXABLE_IN_OTHER_COUNTRY)) {
            fillKYCForTaxableInOtherCountryForm(form, isTaxableInOtherCountry);

        } else if (Objects.equal(form.getName(), ApplicationFormName.OPEN_SAVINGS_ACCOUNT_TAXABLE_IN_YET_ANOTHER_COUNTRY)) {
            fillKYCForTaxableInYetAnotherCountryForm(form, isTaxableInASecondCountry);

        } else if (Objects.equal(form.getName(), ApplicationFormName.COLLECTOR_OPEN_SAVINGS_ACCOUNT_WITHDRAWAL_ACCOUNT)) {
            fillKYCforAccountWithdrawal(form);

        } else if (Objects.equal(form.getName(), ApplicationFormName.SBAB_OPEN_SAVINGS_ACCOUNT_CONFIRMATION)) {
            fillKYCForSBABConfirmationForm(form);
        } else if (Objects.equal(form.getName(), ApplicationFormName.COLLECTOR_OPEN_SAVINGS_ACCOUNT_CONFIRMATION)) {
            fillKYCForCollectorConfirmationForm(form);
        }
    }

    private void fillSavingsProductsFields(ApplicationForm form, boolean isSBAB) {
        Optional<ApplicationField> field = form.getField(ApplicationFieldName.SAVINGS_PRODUCT);
        if (!field.isPresent()) {
            return;
        }

        List<ApplicationFieldOption> options = field.get().getOptions();
        if (options == null || options.isEmpty()) {
            return;
        }

        String value = null;

        for (ApplicationFieldOption option : options) {
            if (isSBAB) {
                if (Objects.equal(option.getLabel(), "SBAB")) {
                    value = option.getValue();
                    break;
                }
            } else {
                if (Objects.equal(option.getLabel(), "Collector Bank")) {
                    value = option.getValue();
                    break;
                }
            }
        }

        populateField(form, ApplicationFieldName.SAVINGS_PRODUCT, value);
    }

    private void createAndSaveProducts() {
        ProductDAO productDAO = serviceContext.getDao(ProductDAO.class);

        ProductTemplate sbabTemplate = new ProductTemplate();
        sbabTemplate.setId(UUIDUtils.fromTinkUUID(OPEN_SAVING_ACCOUNT_SBAB_ID));
        sbabTemplate.setProviderName("sbab-bankid");
        sbabTemplate.setName("SBAB");
        sbabTemplate.setType(ProductType.SAVINGS_ACCOUNT);
        sbabTemplate.setStatus(ProductTemplateStatus.ENABLED);
        ProductInstance sbabInstance = new ProductInstance();
        sbabInstance.setUserId(UUIDUtils.fromTinkUUID(authenticatedUser.getUser().getId()));
        sbabInstance.setTemplateId(sbabTemplate.getId());
        productDAO.save(sbabInstance);
        productDAO.save(sbabTemplate);

        ProductTemplate collectorTemplate = new ProductTemplate();
        collectorTemplate.setId(UUIDUtils.fromTinkUUID(OPEN_SAVING_ACCOUNT_COLLECTOR_ID));
        collectorTemplate.setProviderName("collector-bankid");
        collectorTemplate.setName("Collector Bank");
        collectorTemplate.setType(ProductType.SAVINGS_ACCOUNT);
        collectorTemplate.setStatus(ProductTemplateStatus.ENABLED);
        ProductInstance collectorInstance = new ProductInstance();
        collectorInstance.setUserId(UUIDUtils.fromTinkUUID(authenticatedUser.getUser().getId()));
        collectorInstance.setTemplateId(collectorTemplate.getId());
        productDAO.save(collectorInstance);
        productDAO.save(collectorTemplate);
    }

    private void createAndSaveAccounts() {
        Account savingsAccount = new Account();
        savingsAccount.setName(AccountIdentifier.Type.SE + " Swedbank Sparkonto");
        savingsAccount.setAccountNumber(AccountIdentifier.Type.SE + ":1234567891234");
        savingsAccount.setBalance(50000);
        savingsAccount.setId(AccountIdentifier.Type.SE + ":1234567891234");
        savingsAccount.setUserId(authenticatedUser.getUser().getId());
        savingsAccount.setType(AccountTypes.SAVINGS);

        AccountRepository accountRepository = serviceContext.getRepository(AccountRepository.class);
        accountRepository.save(Lists.newArrayList(savingsAccount));
    }

    private void fillSavingsKYCCollectorFields(ApplicationForm form) {
        populateField(form, ApplicationFieldName.COLLECTOR_INITIAL_DEPOSIT, ApplicationFieldOptionValues.LESS_THAN_10000);
        populateField(form, ApplicationFieldName.COLLECTOR_SAVINGS_FREQUENCY, ApplicationFieldOptionValues.ONE_TO_FIVE_TIMES_A_MONTH);
        populateField(form, ApplicationFieldName.COLLECTOR_SAVINGS_SOURCES_REASON, ApplicationFieldOptionValues.SWITCH_OF_BANK_OR_SAVINGS_FORM);
        populateField(form, ApplicationFieldName.COLLECTOR_MONEY_WITHDRAWAL, ApplicationFieldOptionValues.LESS_THAN_ONCE_A_MONTH);

        Optional<ApplicationField> fieldSavingPurpose = form.getField(ApplicationFieldName.COLLECTOR_SAVINGS_PURPOSE);
        if (fieldSavingPurpose.isPresent()) {
            List<ApplicationFieldOption> options = fieldSavingPurpose.get().getOptions();
            if (options != null && !options.isEmpty()) {
                String value = options.get(2).getValue();
                populateField(form, ApplicationFieldName.COLLECTOR_SAVINGS_PURPOSE, value);
            }
        }
        Optional<ApplicationField> fieldSavingSources = form.getField(ApplicationFieldName.COLLECTOR_SAVINGS_SOURCES);
        if (fieldSavingSources.isPresent()) {
            List<ApplicationFieldOption> options = fieldSavingSources.get().getOptions();
            if (options != null && !options.isEmpty()) {
                String value1 = options.get(0).getValue();
                populateField(form, ApplicationFieldName.COLLECTOR_SAVINGS_SOURCES, value1);
            }
        }
        Optional<ApplicationField> fieldAccountForMoneyWithdrawal = form.getField(ApplicationFieldName.COLLECTOR_ACCOUNT_FOR_MONEY_WITHDRAWAL);
        if (fieldAccountForMoneyWithdrawal.isPresent()) {
            List<ApplicationFieldOption> options = fieldAccountForMoneyWithdrawal.get().getOptions();
            if (options != null && !options.isEmpty()) {
                String value1 = options.get(0).getValue();
                populateField(form, ApplicationFieldName.COLLECTOR_ACCOUNT_FOR_MONEY_WITHDRAWAL, value1);
            } else {
                populateField(form, ApplicationFieldName.COLLECTOR_ACCOUNT_FOR_MONEY_WITHDRAWAL, "11111111111111111");
            }
        }
    }

    private void fillApplicantForm(ApplicationForm form) {
        populateField(form, ApplicationFieldName.NAME, "Alexandra Tsampikakis");
        populateField(form, ApplicationFieldName.PERSONAL_NUMBER, "201212121212");
        populateField(form, ApplicationFieldName.EMAIL, "alexandra.tsampikakis@tink.se");
        populateField(form, ApplicationFieldName.PHONE_NUMBER, "0730320320");
        populateField(form, ApplicationFieldName.STREET_ADDRESS, "Kronobergsgatan 43");
        populateField(form, ApplicationFieldName.POSTAL_CODE, "11233");
        populateField(form, ApplicationFieldName.TOWN, "Stockholm");
    }

    private void fillSavingsKycSbabSavingsPurposeFields(ApplicationForm form) {
        Optional<ApplicationField> fieldSavingPurpose = form.getField(ApplicationFieldName.SBAB_SAVINGS_PURPOSE);
        if (fieldSavingPurpose.isPresent()) {
            List<ApplicationFieldOption> options = fieldSavingPurpose.get().getOptions();
            if (options != null && !options.isEmpty()) {
                String value = options.get(2).getValue();
                populateField(form, ApplicationFieldName.SBAB_SAVINGS_PURPOSE, value);
            }
        }
    }

    private void fillSavingsKycSbabInitialDepositFields(ApplicationForm form) {
        populateField(form, ApplicationFieldName.SBAB_INITIAL_DEPOSIT, ApplicationFieldOptionValues.LESS_THAN_50000);
    }

    private void fillSavingsKycSbabSavingsFrequencyFields(ApplicationForm form) {
        populateField(form, ApplicationFieldName.SBAB_SAVINGS_FREQUENCY, ApplicationFieldOptionValues.LESS_THAN_ONCE_A_MONTH);
    }

    private void fillSavingsKycSbabSavingsAmountPerMonthFields(ApplicationForm form) {
        populateField(form, ApplicationFieldName.SBAB_SAVINGS_AMOUNT_PER_MONTH, ApplicationFieldOptionValues.BETWEEN_10000_AND_20000);
    }

    private void fillSavingsKycSbabPersonsSavingFields(ApplicationForm form) {
        populateField(form, ApplicationFieldName.SBAB_PERSONS_SAVING, ApplicationFieldOptionValues.PERSONS_SAVING_ME);
        populateField(form, ApplicationFieldName.SBAB_PERSONS_SAVING, ApplicationFieldOptionValues.PERSONS_SAVING_OTHER);
        populateField(form, ApplicationFieldName.SBAB_PERSONS_SAVING_OTHER_VALUE, "Karl Karlsson");
    }

    private void fillSavingsKycSbabSavingsSourcesFields(ApplicationForm form) {
        Optional<ApplicationField> fieldSavingSources = form.getField(ApplicationFieldName.SBAB_SAVINGS_SOURCES);
        if (fieldSavingSources.isPresent()) {
            List<ApplicationFieldOption> options = fieldSavingSources.get().getOptions();
            if (options != null && !options.isEmpty()) {
                String value1 = options.get(0).getValue();
                populateField(form, ApplicationFieldName.SBAB_SAVINGS_SOURCES, value1);
            }
        }
    }

    private void fillSavingsKycSbabSavingsSourcesReasonFields(ApplicationForm form) {
        populateField(form, ApplicationFieldName.SBAB_SAVINGS_SOURCES_REASON, ApplicationFieldOptionValues.GIFT_OR_INHERITANCE);
    }

    private void fillSavingsKycSbabWithdrawalFields(ApplicationForm form) {
        populateField(form, ApplicationFieldName.SBAB_MONEY_WITHDRAWAL, ApplicationFieldOptionValues.LESS_THAN_ONCE_A_MONTH);
    }

    private void fillSavingsKycSbabSavingsMonthlyIncomeFields(ApplicationForm form) {
        populateField(form, ApplicationFieldName.SBAB_SAVINGS_MONTHLY_INCOME, ApplicationFieldOptionValues.MORE_THAN_70000);
    }

    private void fillKYCForIsPepForm(ApplicationForm form) {
        populateField(form, ApplicationFieldName.IS_PEP, ApplicationFieldOptionValues.NO);
    }

    private void fillKYCForCitizenshipInOtherCountryForm(ApplicationForm form, boolean hasCitizenshipInOtherCountry) {
        if (hasCitizenshipInOtherCountry) {
            populateField(form, ApplicationFieldName.CITIZENSHIP_IN_OTHER_COUNTRY, ApplicationFieldOptionValues.YES);
            populateField(form, ApplicationFieldName.CITIZENSHIP_COUNTRY, "AF");
            hasSetSecondCountry = false;
        } else {
            populateField(form, ApplicationFieldName.CITIZENSHIP_IN_OTHER_COUNTRY, ApplicationFieldOptionValues.NO);
        }
    }

    private void fillKYCForCitizenshipInYetAnotherCountryForm(ApplicationForm form, boolean hasCitizenshipInASecondCountry, boolean hasCitizenshipInAThirdCountry) {
        if (hasCitizenshipInASecondCountry) {
            if (!hasSetSecondCountry) {
                populateField(form, ApplicationFieldName.CITIZENSHIP_IN_YET_ANOTHER_COUNTRY, ApplicationFieldOptionValues.YES);
                populateField(form, ApplicationFieldName.CITIZENSHIP_COUNTRY, "ES");
                hasSetSecondCountry = true;
            } else if (hasCitizenshipInAThirdCountry && !hasSetThirdCountry) {
                populateField(form, ApplicationFieldName.CITIZENSHIP_IN_YET_ANOTHER_COUNTRY, ApplicationFieldOptionValues.YES);
                populateField(form, ApplicationFieldName.CITIZENSHIP_COUNTRY, "DK");
                hasSetThirdCountry = true;
            } else {
                populateField(form, ApplicationFieldName.CITIZENSHIP_IN_YET_ANOTHER_COUNTRY, ApplicationFieldOptionValues.NO);
            }
        } else {
            populateField(form, ApplicationFieldName.CITIZENSHIP_IN_YET_ANOTHER_COUNTRY, ApplicationFieldOptionValues.NO);
        }
    }

    private void fillTaxableInUSAForm(ApplicationForm form) {
        populateField(form, ApplicationFieldName.TAXABLE_IN_USA, ApplicationFieldOptionValues.NO);
    }

    private void fillKYCForTaxableInOtherCountryForm(ApplicationForm form, boolean isTaxableInOtherCountry) {
        if (isTaxableInOtherCountry) {
            populateField(form, ApplicationFieldName.TAXABLE_IN_OTHER_COUNTRY, ApplicationFieldOptionValues.YES);
            populateField(form, ApplicationFieldName.TAXABLE_COUNTRY, "AF");
            populateField(form, ApplicationFieldName.TAXPAYER_IDENTIFICATION_NUMBER, "123456789");
            hasSetSecondCountry = false;
        } else {
            populateField(form, ApplicationFieldName.TAXABLE_IN_OTHER_COUNTRY, ApplicationFieldOptionValues.NO);
        }
    }

    private void fillKYCForTaxableInYetAnotherCountryForm(ApplicationForm form, boolean isTaxableInASecondCountry) {
        if (isTaxableInASecondCountry && !hasSetSecondCountry) {
            populateField(form, ApplicationFieldName.TAXABLE_IN_YET_ANOTHER_COUNTRY, ApplicationFieldOptionValues.YES);
            populateField(form, ApplicationFieldName.TAXABLE_COUNTRY, "DK");
            populateField(form, ApplicationFieldName.TAXPAYER_IDENTIFICATION_NUMBER, "123456789");
            hasSetSecondCountry = true;
        } else {
            populateField(form, ApplicationFieldName.TAXABLE_IN_YET_ANOTHER_COUNTRY, ApplicationFieldOptionValues.NO);
        }
    }

    private void fillKYCforAccountWithdrawal(ApplicationForm form) {
        AccountRepository accountRepository = serviceContext.getRepository(AccountRepository.class);
        List<Account> accounts = accountRepository.findByUserId(authenticatedUser.getUser().getId());
        if (accounts == null) {
            return;
        }

        Optional<ApplicationField> field = form.getField(ApplicationFieldName.COLLECTOR_ACCOUNT_FOR_MONEY_WITHDRAWAL);
        if (field.isPresent()) {
            List<ApplicationFieldOption> options = Lists.newArrayList();
            for (Account account : accounts) {
                ApplicationFieldOption option = new ApplicationFieldOption();
                option.setValue(account.getId());
                option.setLabel(account.getName());
                option.setDescription(account.getAccountNumber());

                Map<String, String> payload = Maps.newHashMap();
                payload.put("provider", account.getName());
                option.setSerializedPayload(SerializationUtils.serializeToString(payload));

                options.add(option);
            }

            field.get().setOptions(options);
            field.get().setValue(options.get(0).getValue());
        }
    }

    private void fillKYCForCollectorConfirmationForm(ApplicationForm form) {
        populateField(form, ApplicationFieldName.COLLECTOR_SAVINGS_CONFIRMATION, ApplicationFieldOptionValues.TRUE);
    }

    private void fillKYCForSBABConfirmationForm(ApplicationForm form) {
        populateField(form, ApplicationFieldName.SBAB_SAVINGS_CONFIRMATION, ApplicationFieldOptionValues.TRUE);
    }

    private void fillSwedishCitizenForm(ApplicationForm form) {
        populateField(form, ApplicationFieldName.SWEDISH_CITIZEN, ApplicationFieldOptionValues.YES);
    }

    private void populateField(ApplicationForm form, String name, String value) {
        Optional<ApplicationField> field = form.getField(name);
        if (field.isPresent()) {
            field.get().setValue(value);
        }
    }

}
