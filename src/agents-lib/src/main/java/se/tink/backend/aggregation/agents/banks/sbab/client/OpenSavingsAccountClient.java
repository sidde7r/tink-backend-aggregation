package se.tink.backend.aggregation.agents.banks.sbab.client;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.common.annotations.VisibleForTesting;
import com.google.common.base.Objects;
import com.google.common.base.Preconditions;
import com.google.common.collect.ImmutableMap;
import com.google.common.collect.Iterables;
import com.google.common.collect.ListMultimap;
import com.google.common.collect.Multimaps;
import com.google.common.collect.Sets;
import com.google.common.net.HttpHeaders;
import com.sun.jersey.api.client.Client;
import com.sun.jersey.api.client.ClientResponse;
import com.sun.jersey.core.util.MultivaluedMapImpl;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import se.tink.backend.aggregation.agents.banks.sbab.model.response.AccountEntity;
import se.tink.backend.aggregation.agents.banks.sbab.model.response.OpenSavingsAccountResponse;
import se.tink.backend.aggregation.agents.banks.sbab.model.response.QuestionGroupEntity;
import se.tink.backend.aggregation.agents.banks.sbab.model.response.QuestionGroupResponse;
import se.tink.backend.aggregation.agents.banks.sbab.model.response.SignFormRequestBody;
import se.tink.backend.aggregation.agents.banks.sbab.util.OpenSavingsAccountModelMapper;
import se.tink.backend.aggregation.agents.banks.sbab.util.SBABOpenSavingsAccountUtils;
import se.tink.backend.common.i18n.SocialSecurityNumber;
import se.tink.libraries.serialization.utils.SerializationUtils;
import se.tink.libraries.application.GenericApplication;
import se.tink.libraries.application.GenericApplicationFieldGroup;
import se.tink.backend.aggregation.rpc.Credentials;
import se.tink.backend.core.enums.ApplicationFieldName;
import se.tink.libraries.application.ApplicationFieldOptionValues;
import se.tink.backend.core.enums.GenericApplicationFieldGroupNames;
import se.tink.backend.utils.ApplicationUtils;
import se.tink.backend.aggregation.log.AggregationLogger;

public class OpenSavingsAccountClient extends SBABClient {

    private static final AggregationLogger log = new AggregationLogger(OpenSavingsAccountClient.class);
    
    private static final String INITIAL_PAGE_URL =
            SECURE_BASE_URL + "/1/privat/sparkonto/sparkonto/oppna_signera_konto.html";
    
    private static final String SAVINGS_PURPOSE_KEY = SBABOpenSavingsAccountUtils.getAnswerKey(200, 200);
    private static final String SAVINGS_SOURCES_REASON_KEY = SBABOpenSavingsAccountUtils.getAnswerKey(200, 201);
    private static final String SAVINGS_SOURCES_BUSINESS_INDUSTRY_KEY = SBABOpenSavingsAccountUtils.getAnswerKey(200, 202);
    private static final String SAVINGS_SOURCES_REASON_COMPANY_NAME_KEY = SBABOpenSavingsAccountUtils.getAnswerKey(200, 203);
    private static final String SAVINGS_SOURCES_REASON_COMPANY_REGISTRATION_NUMBER_KEY = SBABOpenSavingsAccountUtils.getAnswerKey(200, 204);
    private static final String INITIAL_DEPOSIT_KEY = SBABOpenSavingsAccountUtils.getAnswerKey(200, 205);
    private static final String SAVINGS_FREQUENCY_KEY = SBABOpenSavingsAccountUtils.getAnswerKey(200, 206);
    private static final String SAVINGS_AMOUNT_PER_MONTH_KEY = SBABOpenSavingsAccountUtils.getAnswerKey(200, 207);
    private static final String PERSONS_SAVING_KEY = SBABOpenSavingsAccountUtils.getAnswerKey(200, 208);
    private static final String SAVINGS_SOURCES_KEY = SBABOpenSavingsAccountUtils.getAnswerKey(200, 209);
    private static final String BANK_SOURCE_KEY = SBABOpenSavingsAccountUtils.getAnswerKey(200, 210);
    private static final String MONEY_WITHDRAWAL_KEY = SBABOpenSavingsAccountUtils.getAnswerKey(200, 211);

    private static final String PEP_KEY = SBABOpenSavingsAccountUtils.getAnswerKey(111, 114);
    private static final String MONTHLY_INCOME_KEY = SBABOpenSavingsAccountUtils.getAnswerKey(111, 116);

    private static final Map<Integer, String> TAX_RESIDENCE_KEYS = ImmutableMap.<Integer, String>builder()
            .put(0, SBABOpenSavingsAccountUtils.getAnswerKey(151, 151))
            .put(1, SBABOpenSavingsAccountUtils.getAnswerKey(151, 152))
            .put(2, SBABOpenSavingsAccountUtils.getAnswerKey(151, 153))
            .build();

    private static final Map<Integer, String> TAX_RESIDENCE_NOT_SE_ID_KEYS = ImmutableMap.<Integer, String>builder()
            .put(0, SBABOpenSavingsAccountUtils.getAnswerKey(151, 157))
            .put(1, SBABOpenSavingsAccountUtils.getAnswerKey(151, 158))
            .put(2, SBABOpenSavingsAccountUtils.getAnswerKey(151, 159))
            .build();

    private static final Map<Integer, String> CITIZENSHIP_KEYS = ImmutableMap.<Integer, String>builder()
            .put(0, SBABOpenSavingsAccountUtils.getAnswerKey(111, 111))
            .put(1, SBABOpenSavingsAccountUtils.getAnswerKey(111, 112))
            .put(2, SBABOpenSavingsAccountUtils.getAnswerKey(111, 113))
            .build();

    private static final Map<Integer, String> CITIZENSHIP_US_ID_KEYS = ImmutableMap.<Integer, String>builder()
            .put(0, SBABOpenSavingsAccountUtils.getAnswerKey(111, 117))
            .put(1, SBABOpenSavingsAccountUtils.getAnswerKey(111, 118))
            .put(2, SBABOpenSavingsAccountUtils.getAnswerKey(111, 119))
            .build();

    private static final Map<String, String> INDUSTRY_FIELD_TO_COMPANY_NAME_FIELD = ImmutableMap.<String, String>builder()
            .put(ApplicationFieldOptionValues.INDUSTRY_RESTAURANT, ApplicationFieldName.SBAB_SAVINGS_SOURCES_REASON_OWN_BUSINESS_RESTAURANT_COMPANY_NAME)
            .put(ApplicationFieldOptionValues.INDUSTRY_PAYMENTS, ApplicationFieldName.SBAB_SAVINGS_SOURCES_REASON_OWN_BUSINESS_PAYMENT_COMPANY_NAME)
            .put(ApplicationFieldOptionValues.INDUSTRY_GAMING, ApplicationFieldName.SBAB_SAVINGS_SOURCES_REASON_OWN_BUSINESS_GAMING_COMPANY_NAME)
            .put(ApplicationFieldOptionValues.INDUSTRY_HAIRDRESSER, ApplicationFieldName.SBAB_SAVINGS_SOURCES_REASON_OWN_BUSINESS_HAIRDRESSER_COMPANY_NAME)
            .put(ApplicationFieldOptionValues.INDUSTRY_CONSTRUCTION, ApplicationFieldName.SBAB_SAVINGS_SOURCES_REASON_OWN_BUSINESS_CONSTRUCTION_COMPANY_NAME)
            .put(ApplicationFieldOptionValues.INDUSTRY_WEAPON, ApplicationFieldName.SBAB_SAVINGS_SOURCES_REASON_OWN_BUSINESS_WEAPON_COMPANY_NAME)
            .build();

    private static final Map<String, String> INDUSTRY_FIELD_TO_COMPANY_REGISTRATION_NUMBER_FIELD = ImmutableMap.<String, String>builder()
            .put(ApplicationFieldOptionValues.INDUSTRY_RESTAURANT, ApplicationFieldName.SBAB_SAVINGS_SOURCES_REASON_OWN_BUSINESS_RESTAURANT_COMPANY_REGISTRATION_NUMBER)
            .put(ApplicationFieldOptionValues.INDUSTRY_PAYMENTS, ApplicationFieldName.SBAB_SAVINGS_SOURCES_REASON_OWN_BUSINESS_PAYMENT_COMPANY_REGISTRATION_NUMBER)
            .put(ApplicationFieldOptionValues.INDUSTRY_GAMING, ApplicationFieldName.SBAB_SAVINGS_SOURCES_REASON_OWN_BUSINESS_GAMING_COMPANY_REGISTRATION_NUMBER)
            .put(ApplicationFieldOptionValues.INDUSTRY_HAIRDRESSER, ApplicationFieldName.SBAB_SAVINGS_SOURCES_REASON_OWN_BUSINESS_HAIRDRESSER_COMPANY_REGISTRATION_NUMBER)
            .put(ApplicationFieldOptionValues.INDUSTRY_CONSTRUCTION, ApplicationFieldName.SBAB_SAVINGS_SOURCES_REASON_OWN_BUSINESS_CONSTRUCTION_COMPANY_REGISTRATION_NUMBER)
            .put(ApplicationFieldOptionValues.INDUSTRY_WEAPON, ApplicationFieldName.SBAB_SAVINGS_SOURCES_REASON_OWN_BUSINESS_WEAPON_COMPANY_REGISTRATION_NUMBER)
            .build();

    public OpenSavingsAccountClient(Client client, Credentials credentials, String aggregator) {
        super(client, credentials, aggregator);
    }

    public OpenSavingsAccountResponse submit(GenericApplication application) throws Exception {

        // Open the initial page of the application.

        Document initialPage = getJsoupDocument(INITIAL_PAGE_URL);
        Element form = initialPage.select("form[id=oppnaSparkontoForm]").first();

        Preconditions.checkState(form != null, "Could not find initial <form> to start the new savings account process."
                + " One reason could be that the user has reached the max number of accounts.");

        // Send the initial response to initiate the application process.

        String postUrl = SECURE_BASE_URL + form.attr("action");

        MultivaluedMapImpl initialRequestBody = createInitialRequestBody(application);
        appendToken(initialRequestBody, form);

        ClientResponse response = createFormEncodedHtmlRequest(postUrl)
                .header(HttpHeaders.REFERER, INITIAL_PAGE_URL)
                .post(ClientResponse.class, initialRequestBody);

        String detailsPageUrl = getRedirectUrl(response, SECURE_BASE_URL);
        Document detailsPage = getJsoupDocumentWithReferer(detailsPageUrl, INITIAL_PAGE_URL);
        SBABOpenSavingsAccountUtils.checkForInitialRequestErrors(detailsPage);

        // To avoid sending incorrect values to SBAB in an application, we check that the incoming question
        // groups match what we expect it to be.

        List<QuestionGroupEntity> questionGroups = getQuestionGroups(detailsPage);
        Preconditions.checkState(SBABOpenSavingsAccountUtils.validateQuestions(questionGroups),
                "The question groups from the bank did not math what we expected them to be, "
                        + "probably due to new or changed fields. Check this.");

        // Send the information from the application.

        Element informationForm = detailsPage.select("form[id=OppnaSparkontoBekrafta]").first();
        String informationUrl = SECURE_BASE_URL + informationForm.attr("action");

        MultivaluedMapImpl informationRequestBody = createInformationRequestBody(application);
        appendToken(informationRequestBody, informationForm);

        response = createFormEncodedHtmlRequest(informationUrl)
                .header(HttpHeaders.REFERER, detailsPageUrl)
                .post(ClientResponse.class, informationRequestBody);

        // Fetch the information needed to confirm the new account and initiate a sign process.

        Document confirmPage = getJsoupDocumentWithReferer(getRedirectUrl(response, SECURE_BASE_URL), detailsPageUrl);

        SBABOpenSavingsAccountUtils.checkForInformationRequestErrors(confirmPage);

        Element signForm = confirmPage.select("form").first();
        String confirmUrl = SECURE_BASE_URL + signForm.attr("action");

        Preconditions.checkState(confirmUrl.contains("OppnaSparkontoSignera"),
                "Did not find sign form");

        OpenSavingsAccountResponse openAccountResponse = new OpenSavingsAccountResponse();
        openAccountResponse.setConfirmUrl(confirmUrl);
        openAccountResponse.setReferer(detailsPageUrl);
        openAccountResponse.setStrutsTokenName(signForm.select("input[name=struts.token.name]").val());
        openAccountResponse.setToken(signForm.select("input[name=token]").val());

        return openAccountResponse;
    }

    public SignFormRequestBody initiateSignProcess(OpenSavingsAccountResponse openAccountResponse) throws Exception {
        ClientResponse response = createFormEncodedHtmlRequest(openAccountResponse.getConfirmUrl())
                .header(HttpHeaders.REFERER, openAccountResponse.getReferer())
                .post(ClientResponse.class, createSignRequestBody(openAccountResponse));

        String redirectUrl = getRedirectUrl(response, SECURE_BASE_URL);

        Document signPage = getJsoupDocumentWithReferer(redirectUrl, openAccountResponse.getReferer());
        Element signForm = signPage.select("form[id=signFormNexus]").first();

        SignFormRequestBody signFormRequestBody = SignFormRequestBody.from(signForm);
        signFormRequestBody.setReferer(redirectUrl);

        return signFormRequestBody;
    }

    private MultivaluedMapImpl createSignRequestBody(OpenSavingsAccountResponse openAccountResponse) {
        MultivaluedMapImpl signBody = new MultivaluedMapImpl();

        signBody.add("__checkbox_kunddata.kontovillkor", "true");
        signBody.add("kunddata.kontovillkor", "true");
        signBody.add("struts.token.name", openAccountResponse.getStrutsTokenName());
        signBody.add(openAccountResponse.getStrutsTokenName(), openAccountResponse.getToken());

        return signBody;
    }

    @VisibleForTesting
    static MultivaluedMapImpl createInformationRequestBody(GenericApplication application) {

        ListMultimap<String, GenericApplicationFieldGroup> fieldGroupByName = Multimaps.index(
                application.getFieldGroups(), GenericApplicationFieldGroup::getName);

        List<GenericApplicationFieldGroup> kycFieldGroups = fieldGroupByName
                .get(GenericApplicationFieldGroupNames.KNOW_YOUR_CUSTOMER);

        Preconditions.checkState(!kycFieldGroups.isEmpty(),
                "No 'Know Your Customer' data supplied.");

        GenericApplicationFieldGroup kycFieldGroup = kycFieldGroups.get(0); // There should be only one.

        ListMultimap<String, GenericApplicationFieldGroup> fieldSubGroupByName = Multimaps.index(
                kycFieldGroup.getSubGroups(), GenericApplicationFieldGroup::getName);

        MultivaluedMapImpl informationBody = new MultivaluedMapImpl();

        // Group 111. KYC: General customer information.
        fillKYCInformation(informationBody, kycFieldGroup, fieldSubGroupByName);

        // Group 151. Residence for tax purposes.
        fillResidenceForTaxPurposesGroupInformation(informationBody, fieldSubGroupByName);

        // Group 200. Savings account purpose, equity source, etc.
        fillSavingsAccountInformation(informationBody, kycFieldGroup);

        throwIfNullValues(informationBody);

        return informationBody;
    }

    private static void throwIfNullValues(MultivaluedMapImpl informationBody) {
        Preconditions.checkState(!informationBody.toString().contains("null"),
                "A value was null --> " + informationBody.toString());
    }

    private static void fillSavingsAccountInformation(MultivaluedMapImpl informationBody,
            GenericApplicationFieldGroup fieldGroup) {

        fillPurpose(informationBody, fieldGroup);
        fillSourcesReason(informationBody, fieldGroup);
        fillPersonsSaving(informationBody, fieldGroup);
        fillSources(informationBody, fieldGroup);

        // Question 205. Key = null. Initial deposit.
        OpenSavingsAccountModelMapper.populateAnswer(informationBody, INITIAL_DEPOSIT_KEY, fieldGroup,
                ApplicationFieldName.SBAB_INITIAL_DEPOSIT);

        // Question 206. Key = null. Deposit frequency.
        OpenSavingsAccountModelMapper.populateAnswer(informationBody, SAVINGS_FREQUENCY_KEY, fieldGroup,
                ApplicationFieldName.SBAB_SAVINGS_FREQUENCY);

        // Question 207. Key = null. Deposit amount per month.
        OpenSavingsAccountModelMapper.populateAnswer(informationBody, SAVINGS_AMOUNT_PER_MONTH_KEY, fieldGroup,
                ApplicationFieldName.SBAB_SAVINGS_AMOUNT_PER_MONTH);

        // Question 211. Key = null. Withdrawal frequency.
        OpenSavingsAccountModelMapper.populateAnswer(informationBody, MONEY_WITHDRAWAL_KEY, fieldGroup,
                ApplicationFieldName.SBAB_MONEY_WITHDRAWAL);
    }

    private static void fillPurpose(MultivaluedMapImpl informationBody, GenericApplicationFieldGroup fieldGroup) {
        // Question 200. Key = null. Purpose of the savings account.
        OpenSavingsAccountModelMapper.populateMultiSelectAnswers(informationBody, SAVINGS_PURPOSE_KEY, fieldGroup,
                ApplicationFieldName.SBAB_SAVINGS_PURPOSE);

        // Question 200_6_fritext. Other.
        // Should be populated if <id=200> in [6]
        List<String> purposes = fieldGroup.getFieldAsListOfStrings(ApplicationFieldName.SBAB_SAVINGS_PURPOSE);
        if (purposes.contains(ApplicationFieldOptionValues.OTHER)) {
            OpenSavingsAccountModelMapper.populateMultiSelectCustomValueAnswer(informationBody,
                    SAVINGS_PURPOSE_KEY,
                    OpenSavingsAccountModelMapper.getAnswer(
                            ApplicationFieldName.SBAB_SAVINGS_PURPOSE,
                            ApplicationFieldOptionValues.OTHER),
                    fieldGroup.getField(ApplicationFieldName.SBAB_SAVINGS_PURPOSE_OTHER_VALUE));
        }
    }

    private static void fillSourcesReason(MultivaluedMapImpl informationBody, GenericApplicationFieldGroup fieldGroup) {
        // Question 201. Key = null. Equity source.
        OpenSavingsAccountModelMapper.populateMultiSelectAnswers(informationBody, SAVINGS_SOURCES_REASON_KEY, fieldGroup,
                ApplicationFieldName.SBAB_SAVINGS_SOURCES_REASON);

        List<String> sourcesReason = fieldGroup.getFieldAsListOfStrings(ApplicationFieldName.SBAB_SAVINGS_SOURCES_REASON);

        if (sourcesReason.contains(ApplicationFieldOptionValues.OWN_BUSINESS_OR_DIVIDEND)) {
            // Question 202, 203 and 204.
            fillOwnBusinessOrDividendReason(informationBody, fieldGroup);
        }

        // Question 201_13_fritext. Other.
        // Should be populated if <id=201> in [13]
        if (sourcesReason.contains(ApplicationFieldOptionValues.OTHER)) {
            OpenSavingsAccountModelMapper.populateMultiSelectCustomValueAnswer(informationBody,
                    SAVINGS_SOURCES_REASON_KEY,
                    OpenSavingsAccountModelMapper.getAnswer(
                            ApplicationFieldName.SBAB_SAVINGS_SOURCES_REASON,
                            ApplicationFieldOptionValues.OTHER),
                    fieldGroup.getField(ApplicationFieldName.SBAB_SAVINGS_SOURCES_REASON_OTHER_VALUE));
        }
    }

    private static void fillOwnBusinessOrDividendReason(MultivaluedMapImpl informationBody,
            GenericApplicationFieldGroup fieldGroup) {
        // Question 202. Key = null. From which business industry the money comes.
        OpenSavingsAccountModelMapper.populateAnswer(informationBody, SAVINGS_SOURCES_BUSINESS_INDUSTRY_KEY,
                fieldGroup, ApplicationFieldName.SBAB_SAVINGS_SOURCES_REASON_OWN_BUSINESS_INDUSTRY);

        String industry = fieldGroup.getField(ApplicationFieldName.SBAB_SAVINGS_SOURCES_REASON_OWN_BUSINESS_INDUSTRY);

        if (INDUSTRY_FIELD_TO_COMPANY_NAME_FIELD.containsKey(industry)) {
            // Question 203 (hidden input) and 203_fritext_1 (custom value). Key = null. Company name.
            // Should be populated if <id=202> in [5,6,7,8,10,11]
            OpenSavingsAccountModelMapper.populateSelectCustomAnswerWithHiddenTrigger(informationBody,
                    SAVINGS_SOURCES_REASON_COMPANY_NAME_KEY,
                    fieldGroup.getField(INDUSTRY_FIELD_TO_COMPANY_NAME_FIELD.get(industry)));

            // Question 204 (hidden input) and 204_fritext_1 (custom value). Key = null. Corporate identity (organisationsnummer).
            // Should be populated if <id=202> in [5,6,7,8,10,11]
            OpenSavingsAccountModelMapper.populateSelectCustomAnswerWithHiddenTrigger(informationBody,
                    SAVINGS_SOURCES_REASON_COMPANY_REGISTRATION_NUMBER_KEY,
                    fieldGroup.getField(INDUSTRY_FIELD_TO_COMPANY_REGISTRATION_NUMBER_FIELD.get(industry)));
        } else if (Objects.equal(industry, ApplicationFieldOptionValues.OTHER)) {
            // Question 202_13_fritext. Other industry.
            // Should be populated if <id=202> in [13]
            OpenSavingsAccountModelMapper.populateMultiSelectCustomValueAnswer(informationBody,
                    SAVINGS_SOURCES_BUSINESS_INDUSTRY_KEY,
                    OpenSavingsAccountModelMapper.getAnswer(
                            ApplicationFieldName.SBAB_SAVINGS_SOURCES_REASON_OWN_BUSINESS_INDUSTRY,
                            ApplicationFieldOptionValues.OTHER),
                    fieldGroup.getField(ApplicationFieldName.SBAB_SAVINGS_SOURCES_REASON_OWN_BUSINESS_OTHER_VALUE));
        }
    }

    private static void fillPersonsSaving(MultivaluedMapImpl informationBody, GenericApplicationFieldGroup fieldGroup) {
        // Question 208. Key = null. Who will deposit money?
        OpenSavingsAccountModelMapper.populateMultiSelectAnswers(informationBody, PERSONS_SAVING_KEY, fieldGroup,
                ApplicationFieldName.SBAB_PERSONS_SAVING);

        // Question 208_2_fritext. Other.
        // Should be populated if <id=208> in [2]
        List<String> personsSaving = fieldGroup.getFieldAsListOfStrings(ApplicationFieldName.SBAB_PERSONS_SAVING);
        if (personsSaving.contains(ApplicationFieldOptionValues.PERSONS_SAVING_OTHER)) {
            OpenSavingsAccountModelMapper.populateMultiSelectCustomValueAnswer(informationBody,
                    PERSONS_SAVING_KEY,
                    OpenSavingsAccountModelMapper.getAnswer(
                            ApplicationFieldName.SBAB_PERSONS_SAVING,
                            ApplicationFieldOptionValues.PERSONS_SAVING_OTHER),
                    fieldGroup.getField(ApplicationFieldName.SBAB_PERSONS_SAVING_OTHER_VALUE));
        }
    }

    private static void fillSources(MultivaluedMapImpl informationBody, GenericApplicationFieldGroup fieldGroup) {
        // Question 209. Key = null. Source account.
        OpenSavingsAccountModelMapper.populateMultiSelectAnswers(informationBody, SAVINGS_SOURCES_KEY, fieldGroup,
                ApplicationFieldName.SBAB_SAVINGS_SOURCES);

        List<String> sources = fieldGroup.getFieldAsListOfStrings(ApplicationFieldName.SBAB_SAVINGS_SOURCES);

        // Question 210. Key = null. Source bank.
        if (sources.contains(ApplicationFieldOptionValues.MY_ACCOUNT_IN_SWEDISH_BANK)) {
            fillMyAccountInSwedishBank(informationBody, fieldGroup);
        }

        // Question 209_2_fritext. Other way.
        // Should be populated if <id=209> in [2]
        if (sources.contains(ApplicationFieldOptionValues.OTHER_WAY)) {
            OpenSavingsAccountModelMapper.populateMultiSelectCustomValueAnswer(informationBody,
                    SAVINGS_SOURCES_KEY,
                    OpenSavingsAccountModelMapper.getAnswer(
                            ApplicationFieldName.SBAB_SAVINGS_SOURCES,
                            ApplicationFieldOptionValues.OTHER_WAY),
                    fieldGroup.getField(ApplicationFieldName.SBAB_SAVINGS_SOURCES_OTHER_WAY_VALUE));
        }
    }

    private static void fillMyAccountInSwedishBank(MultivaluedMapImpl informationBody,
            GenericApplicationFieldGroup fieldGroup) {
        OpenSavingsAccountModelMapper.populateMultiSelectAnswers(informationBody, BANK_SOURCE_KEY, fieldGroup,
                ApplicationFieldName.SBAB_SAVINGS_SOURCES_MY_ACCOUNT_IN_SWEDISH_BANK);

        // Question 210_6_fritext. Other.
        // Should be populated if <id=210> in [6]
        List<String> swedishBanks = fieldGroup.getFieldAsListOfStrings(ApplicationFieldName.SBAB_SAVINGS_SOURCES_MY_ACCOUNT_IN_SWEDISH_BANK);
        if (swedishBanks.contains(ApplicationFieldOptionValues.OTHER)) {
            OpenSavingsAccountModelMapper.populateMultiSelectCustomValueAnswer(informationBody,
                    BANK_SOURCE_KEY,
                    OpenSavingsAccountModelMapper.getAnswer(
                            ApplicationFieldName.SBAB_SAVINGS_SOURCES_MY_ACCOUNT_IN_SWEDISH_BANK,
                            ApplicationFieldOptionValues.OTHER),
                    fieldGroup.getField(
                            ApplicationFieldName.SBAB_SAVINGS_SOURCES_MY_ACCOUNT_IN_SWEDISH_BANK_OTHER_VALUE));
        }
    }

    private static void fillResidenceForTaxPurposesGroupInformation(MultivaluedMapImpl informationBody,
            ListMultimap<String, GenericApplicationFieldGroup> fieldSubGroupByName) {
        List<GenericApplicationFieldGroup> residenceForTaxGroups = fieldSubGroupByName
                .get(GenericApplicationFieldGroupNames.RESIDENCE_FOR_TAX_PURPOSES);

        Preconditions.checkState(!residenceForTaxGroups.isEmpty(),
                "The application must contain a residence for tax purposes group");

        // Get the sub groups containing all tax residences.
        residenceForTaxGroups = residenceForTaxGroups.get(0).getSubGroups();

        Preconditions.checkState(residenceForTaxGroups != null && !residenceForTaxGroups.isEmpty(),
                "The application must contain residence(s) for tax purposes");

        fillResidenceForTaxPurposesInformation(informationBody, residenceForTaxGroups, 0);
        fillResidenceForTaxPurposesInformation(informationBody, residenceForTaxGroups, 1);
        fillResidenceForTaxPurposesInformation(informationBody, residenceForTaxGroups, 2);
    }

    private static void fillResidenceForTaxPurposesInformation(MultivaluedMapImpl informationBody,
            List<GenericApplicationFieldGroup> residenceForTaxGroups, int index) {
        GenericApplicationFieldGroup residenceForTaxGroup;
        residenceForTaxGroup = Iterables.get(residenceForTaxGroups, index, null);

        if (residenceForTaxGroup != null) {
            String countryCode = residenceForTaxGroup.getField("country-code");

            // Question 151-153. Key = "skattehemvist_{index+1}".
            OpenSavingsAccountModelMapper.populateAnswer(informationBody, TAX_RESIDENCE_KEYS.get(index),
                    OpenSavingsAccountModelMapper.getResidenceForTaxPurposesAnswer(countryCode));

            if (!"se".equalsIgnoreCase(countryCode)) {
                // Question 157-159 (hidden input) and corresponding _1_fritext (custom value).
                // Key = "tin_7/8/9". Should be populated if skattehemvist_{index+1} is not SE.
                OpenSavingsAccountModelMapper.populateSelectCustomAnswerWithHiddenTrigger(informationBody,
                        TAX_RESIDENCE_NOT_SE_ID_KEYS.get(index),
                        residenceForTaxGroup.getField(ApplicationFieldName.TAXPAYER_IDENTIFICATION_NUMBER));
            }
        } else {
            OpenSavingsAccountModelMapper.populateAnswer(informationBody, TAX_RESIDENCE_KEYS.get(index), "0");
        }
    }

    private static void fillKYCInformation(MultivaluedMapImpl informationBody, GenericApplicationFieldGroup fieldGroup,
            ListMultimap<String, GenericApplicationFieldGroup> fieldSubGroupByName) {
        List<GenericApplicationFieldGroup> citizenshipGroups = fieldSubGroupByName
                .get(GenericApplicationFieldGroupNames.CITIZENSHIPS);

        Preconditions.checkState(!citizenshipGroups.isEmpty(),
                "The application must contain a citizenships group");

        // Get the sub groups containing all citizenships.
        citizenshipGroups = citizenshipGroups.get(0).getSubGroups();

        Preconditions.checkState(citizenshipGroups != null && !citizenshipGroups.isEmpty(),
                "The application must contain citizenship(s)");

        fillCitizenshipInformation(informationBody, citizenshipGroups, 0);
        fillCitizenshipInformation(informationBody, citizenshipGroups, 1);
        fillCitizenshipInformation(informationBody, citizenshipGroups, 2);

        // Question 114. Key = "pep".
        OpenSavingsAccountModelMapper.populateAnswer(informationBody, PEP_KEY, fieldGroup, ApplicationFieldName.IS_PEP);

        // Question 116. Key = null. Monthly salary before tax.
        OpenSavingsAccountModelMapper.populateAnswer(informationBody, MONTHLY_INCOME_KEY, fieldGroup,
                ApplicationFieldName.SBAB_SAVINGS_MONTHLY_INCOME);
    }

    private static void fillCitizenshipInformation(MultivaluedMapImpl informationBody,
            List<GenericApplicationFieldGroup> citizenshipGroups, int index) {
        GenericApplicationFieldGroup citizenshipGroup = Iterables.get(citizenshipGroups, index, null);

        if (citizenshipGroup != null) {
            String countryCode = citizenshipGroup.getField("country-code");

            // Question 111-113. Key = "medborgarskap_{index+1}".
            OpenSavingsAccountModelMapper.populateAnswer(informationBody, CITIZENSHIP_KEYS.get(index),
                    OpenSavingsAccountModelMapper.getCitizenshipCountryAnswer(countryCode));

            if ("us".equalsIgnoreCase(countryCode)) {
                // Question 117-119. Key = "tin_{index+1}". Should be populated if medborgarskap_{index+1} = US.
                OpenSavingsAccountModelMapper.populateAnswer(informationBody, CITIZENSHIP_US_ID_KEYS.get(index),
                        citizenshipGroup.getField(ApplicationFieldName.TAXPAYER_IDENTIFICATION_NUMBER));
            }
        } else {
            OpenSavingsAccountModelMapper.populateAnswer(informationBody, CITIZENSHIP_KEYS.get(index), "0");
        }
    }

    private List<QuestionGroupEntity> getQuestionGroups(Document detailsPage) {
        for (Element script : detailsPage.select("script")) {

            if (script.toString().contains("var frageGrupper=")) {
                Optional<QuestionGroupResponse> questionGroupResponse = SBABOpenSavingsAccountUtils
                        .parseQuestionGroups(script.html());

                if (!questionGroupResponse.isPresent()) {
                    break;
                }

                List<QuestionGroupEntity> questionGroups = questionGroupResponse.get().getQuestionGroups();

                if (questionGroups == null || questionGroups.isEmpty()) {
                    break;
                }

                return questionGroups;
            }
        }

        throw new IllegalStateException("Could not parse question groups");
    }

    @VisibleForTesting
    private static MultivaluedMapImpl createInitialRequestBody(GenericApplication application) {

        ListMultimap<String, GenericApplicationFieldGroup> fieldGroupByName = Multimaps.index(
                application.getFieldGroups(), GenericApplicationFieldGroup::getName);

        Optional<GenericApplicationFieldGroup> applicant = ApplicationUtils.getApplicant(fieldGroupByName);

        Preconditions.checkState(applicant.isPresent(), "No applicant data supplied.");

        MultivaluedMapImpl requestBody = new MultivaluedMapImpl();
        requestBody.add("__checkbox_kunddata.uppdateradEpostsparr", "false");
        requestBody.add("__checkbox_kunddata.medsokandeToggle", "true");
        requestBody.add("kunddata.kontoNamn", "Sparkonto");
        requestBody.add("kunddata.uppdateradAnnatTelefonnummer", "");
        requestBody.add("kunddata.uppdateradEpost", applicant.get().getField(ApplicationFieldName.EMAIL));
        requestBody.add("kunddata.uppdateradEpostsparr", "true");
        requestBody.add("kunddata.uppdateradMobilTelefonnummer",
                applicant.get()
                        .getField(ApplicationFieldName.PHONE_NUMBER));
        requestBody.add("personnummerSelected",
                getPersonalNumberWithDash(applicant.get().getField(ApplicationFieldName.PERSONAL_NUMBER)));

        return requestBody;
    }

    private static String getPersonalNumberWithDash(String personalNumber) {
        SocialSecurityNumber.Sweden ssn = new SocialSecurityNumber.Sweden(personalNumber);
        if (!ssn.isValid()) {
            return null;
        }

        return ssn.asStringWithDash();
    }

    private static void appendToken(MultivaluedMapImpl body, Element element) {
        body.putSingle("struts.token.name", element.select("input[name=struts.token.name]").val());
        body.putSingle(body.getFirst("struts.token.name"), element.select("input[name=token]").val());
    }

    public String getNewAccountNumber(List<AccountEntity> accountsBefore, List<AccountEntity> accountsAfter) {
        // Check if there's one additional account compared to before.
        if (Objects.equal(accountsAfter.size(), accountsBefore.size() + 1)) {
            Set<String> accountNumbersBefore = accountsBefore.stream()
                    .map(AccountEntity::getAccountNumber)
                    .collect(Collectors.toSet());

            Set<String> accountNumbersAfter = accountsAfter.stream()
                    .map(AccountEntity::getAccountNumber)
                    .collect(Collectors.toSet());

            Set<String> accountNumberDiff = Sets.difference(accountNumbersAfter, accountNumbersBefore);
            
            // It should always be 1...
            if (accountNumberDiff.size() == 1) {
                return (String) accountNumberDiff.toArray()[0];
            } else {
                // ...but if it for some reason is not, let's still count it as a successful execution. Something still
                // went wrong---and we want to fix it---so let's log an error.
                // TODO - Search logs for this message ( from 2017-06-22 ), when we can ensure there are no more
                // TODO - false positives ( that the diff size is NOT 1 ), we should throw an exception here
                log.error(String.format(
                        "The difference of unique account numbers is not 1. Before: %s After: %s Diff: %s",
                        SerializationUtils.serializeToString(accountNumbersBefore),
                        SerializationUtils.serializeToString(accountNumbersAfter),
                        SerializationUtils.serializeToString(accountNumberDiff)));
                return null;
            }
        } else {
            ObjectMapper mapper = new ObjectMapper();
            String errorMessage = "The size of the savings account list was not increased by 1 after sign.";

            try {
                errorMessage += String.format(" Accounts before: %s Accounts after: %s",
                        mapper.writeValueAsString(accountsBefore), mapper.writeValueAsString(accountsAfter));
            } catch (JsonProcessingException e) {
                // Do nothing.
            }

            throw new IllegalStateException(errorMessage);
        }
    }
}
