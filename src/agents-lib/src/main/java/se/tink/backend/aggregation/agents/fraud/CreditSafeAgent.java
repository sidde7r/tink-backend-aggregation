package se.tink.backend.aggregation.agents.fraud;

import com.google.common.base.Preconditions;
import com.google.common.base.Splitter;
import com.google.common.base.Strings;
import com.google.common.collect.Iterables;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.google.common.primitives.Doubles;
import com.google.common.primitives.Ints;
import java.text.ParseException;
import java.util.Date;
import java.util.List;
import java.util.Map;
import javax.xml.ws.Binding;
import javax.xml.ws.BindingProvider;
import javax.xml.ws.handler.Handler;
import org.apache.commons.lang.time.DateUtils;
import org.apache.xerces.dom.ElementNSImpl;
import org.w3c.dom.Node;
import se.tink.backend.agents.rpc.Field;
import se.tink.backend.aggregation.agents.AbstractAgent;
import se.tink.backend.aggregation.agents.AgentContext;
import se.tink.backend.aggregation.agents.DeprecatedRefreshExecutor;
import se.tink.backend.aggregation.agents.bankid.CredentialsSignicatBankIdAuthenticationHandler;
import se.tink.backend.aggregation.agents.exceptions.AuthenticationException;
import se.tink.backend.aggregation.agents.exceptions.AuthorizationException;
import se.tink.backend.aggregation.agents.exceptions.errors.SessionError;
import se.tink.backend.aggregation.agents.fraud.creditsafe.soap.Account;
import se.tink.backend.aggregation.agents.fraud.creditsafe.soap.Error;
import se.tink.backend.aggregation.agents.fraud.creditsafe.soap.GetData;
import se.tink.backend.aggregation.agents.fraud.creditsafe.soap.GetDataRequest;
import se.tink.backend.aggregation.agents.fraud.creditsafe.soap.GetDataResponse;
import se.tink.backend.aggregation.agents.fraud.creditsafe.soap.GetDataSoap;
import se.tink.backend.aggregation.agents.fraud.creditsafe.soap.GetDataTest;
import se.tink.backend.aggregation.agents.fraud.creditsafe.soap.Language;
import se.tink.backend.aggregation.agents.utils.soap.SOAPLoggingHandler;
import se.tink.backend.agents.rpc.Credentials;
import se.tink.libraries.credentials_requests.CredentialsRequest;
import se.tink.backend.agents.rpc.CredentialsStatus;
import se.tink.libraries.user.rpc.User;
import se.tink.backend.aggregation.agents.utils.authentication.bankid.signicat.SignicatBankIdAuthenticator;
import se.tink.backend.aggregation.configuration.SignatureKeyPair;
import se.tink.backend.aggregation.agents.models.fraud.FraudAddressContent;
import se.tink.backend.aggregation.agents.models.fraud.FraudCompanyContent;
import se.tink.backend.aggregation.agents.models.fraud.FraudCompanyDirector;
import se.tink.backend.aggregation.agents.models.fraud.FraudCompanyEngagementContent;
import se.tink.backend.aggregation.agents.models.fraud.FraudCreditScoringContent;
import se.tink.backend.aggregation.agents.models.fraud.FraudCreditorContent;
import se.tink.backend.aggregation.agents.models.fraud.FraudDetailsContent;
import se.tink.backend.aggregation.agents.models.fraud.FraudDetailsContentType;
import se.tink.backend.aggregation.agents.models.fraud.FraudIdentityContent;
import se.tink.backend.aggregation.agents.models.fraud.FraudIncomeContent;
import se.tink.backend.aggregation.agents.models.fraud.FraudNonPaymentContent;
import se.tink.backend.aggregation.agents.models.fraud.FraudRealEstateEngagementContent;
import se.tink.credentials.demo.DemoCredentials;
import se.tink.libraries.date.ThreadSafeDateFormat;
import se.tink.libraries.i18n.Catalog;
import se.tink.libraries.strings.StringUtils;

@SuppressWarnings("unused")
public class CreditSafeAgent extends AbstractAgent implements DeprecatedRefreshExecutor {

    private static final String LOGIN_NAME_TEST = "TINKTESTIN";
    private static final String PASSWORD_TEST = "c7RCi13Q";
    private static final String PERSON_BLOCK_TEST = "PersonBasicBlock";
    private static final String PERSON_CREDIT_BLOCK_TEST = "PERSONCREDIT";

    private static final String PERSON_BLOCK = "TINK_P_BASIC";
    private static final String PERSON_CREDIT_BLOCK = "TINK_P_CREDIT";
    private static final String COMPANY_BLOCK = "TINK_C_BASIC";
    private static final String COMPANY_CREDIT_BLOCK = "TINK_C_CREDIT";

    private static final String NO_CONTENT = "-";

    private static final int STATUS_CODE_FIRST_INACTIVE = 200;

    private final Credentials credentials;
    private Account account;
    private final User user;
    private Splitter splitter = Splitter.on(" ");
    private boolean hasRefreshed = false;

    public CreditSafeAgent(CredentialsRequest request, AgentContext context, SignatureKeyPair signatureKeyPair) {
        super(request, context);

        credentials = request.getCredentials();
        user = request.getUser();
    }

    @Override
    public void refresh() throws Exception {
        // The refresh command will call refresh multiple times.
        // This check ensures the refresh only runs once.
        if (hasRefreshed) {
            return;
        }
        hasRefreshed = true;

        GetDataSoap service = null;

        if (context.isTestContext()) {
            GetDataTest client = new GetDataTest();
            service = client.getGetDataSoap();
        } else {
            GetData client = new GetData();
            service = client.getGetDataSoap();
        }

        GetDataRequest dataReq = new GetDataRequest();
        Account account = new Account();

        if (context.isTestContext()) {
            account.setUserName(LOGIN_NAME_TEST);
            account.setPassword(PASSWORD_TEST);
            dataReq.setBlockName(PERSON_CREDIT_BLOCK_TEST);
        } else {
            account.setUserName(configuration.getCreditSafe().getUsername());
            account.setPassword(configuration.getCreditSafe().getPassword());
            dataReq.setBlockName(PERSON_CREDIT_BLOCK);
        }

        account.setLanguage(Language.SWE);
        dataReq.setAccount(account);

        if (credentials.isDebug()) {
            addUserAgentHandler(service);
        }

        // Handle test data specially.

        dataReq.setSearchNumber(request.getCredentials().getUsername());

        GetDataResponse response = service.getDataBySecure(dataReq);

        List<FraudDetailsContent> detailsContent = Lists.newArrayList();

        // Validate response.

        Error error = response.getError();

        if (error != null) {

            // If error status is protected or blocked, create identity content saying that.

            Catalog catalog = Catalog.getCatalog(user.getProfile().getLocale());
            String knownErrorMessage = parseErrorMessageKnown(catalog, error);

            if (!Strings.isNullOrEmpty(knownErrorMessage)) {

                FraudIdentityContent content = new FraudIdentityContent();
                content.setContentType(FraudDetailsContentType.IDENTITY);
                content.setFirstName(catalog.getString("person"));
                content.setLastName(knownErrorMessage);
                content.setPersonIdentityNumber(credentials.getUsername());
                detailsContent.add(content);

                systemUpdater.updateFraudDetailsContent(detailsContent);

                log.info(knownErrorMessage);
                statusUpdater.updateStatus(CredentialsStatus.UPDATING);

            } else {

                String errorMessage = error.getRejectText() + " - " +
                        error.getRejectComment() + " - " + error.getCauseOfReject();

                log.warn("CreditSafe refresh gave error:" + errorMessage);
                statusUpdater.updateStatus(CredentialsStatus.TEMPORARY_ERROR, errorMessage);
            }
            return;

        }

        statusUpdater.updateStatus(CredentialsStatus.UPDATING);

        // Pars XMl data to structured format.

        Map<String, String> generalDataMap = Maps.newHashMap();
        List<Map<String, String>> nonPaymentsPrivateList = Lists.newArrayList();
        List<Map<String, String>> nonPaymentsPublicList = Lists.newArrayList();
        List<Map<String, String>> realEstateList = Lists.newArrayList();
        List<Map<String, String>> companyEngagementList = Lists.newArrayList();

        try {
            ElementNSImpl anyParameters = (ElementNSImpl) response.getParameters().getAny();
            Node dataSet = anyParameters.getFirstChild();
            Node parent = dataSet.getFirstChild();

            while (parent != null) {
                switch (parent.getNodeName()) {
                case "GETDATA_RESPONSE":
                    parseGeneralData(generalDataMap, parent);
                    break;
                case "ROP_PRIVATE_CLAIMS":
                    parseTagData(nonPaymentsPrivateList, parent);
                    break;
                case "ROP_PUBLIC_CLAIMS":
                    parseTagData(nonPaymentsPublicList, parent);
                    break;
                case "DETAILREALESTATE":
                    parseTagData(realEstateList, parent);
                    break;
                case "COMPANY_ENGAGEMENTS":
                    parseTagData(companyEngagementList, parent);
                    break;
                }
                parent = parent.getNextSibling();
            }
        } catch (Exception e) {
            log.error("Could not parse data from Creditsafe", e);
        }

        // Parse structured data and create fraud details contents.

        detailsContent.addAll(extractIdentityContent(generalDataMap));
        detailsContent.addAll(extractAddressContent(generalDataMap));
        detailsContent.addAll(extractNonPaymentsContent(nonPaymentsPrivateList));
        detailsContent.addAll(extractNonPaymentsContent(nonPaymentsPublicList));
        detailsContent.addAll(extractCreditScoringContent(generalDataMap));
        detailsContent.addAll(extractRealEstateContent(realEstateList));
        detailsContent.addAll(extractCreditsContent(generalDataMap));
        detailsContent.addAll(extractIncomeContent(generalDataMap));
        detailsContent.addAll(extractCompanyEngagementContent(companyEngagementList));


        systemUpdater.updateFraudDetailsContent(detailsContent);
    }

    /**
     * Parses the a specific tag data.
     * 
     */
    private void parseTagData(List<Map<String, String>> dataMap, Node parent) {
        Map<String, String> resultMap = Maps.newHashMap();
        dataMap.add(resultMap);

        Node child = parent.getFirstChild();
        while (child != null) {
            String key = child.getNodeName();
            String value = child.getTextContent();
            resultMap.put(key, value);
            child = child.getNextSibling();
        }
    }

    /**
     * Recursively parses the data in GETDATA_RESPONSE tag.
     * 
     */
    private void parseGeneralData(Map<String, String> resultMap, Node parent) {
        Node child = parent.getFirstChild();
        while (child != null) {
            String key = child.getNodeName();
            String value = child.getTextContent();
            resultMap.put(key, value);
            child = child.getNextSibling();
        }
    }

    private List<FraudIdentityContent> extractIdentityContent(Map<String, String> resultMap) {
        List<FraudIdentityContent> contents = Lists.newArrayList();

        FraudIdentityContent content = new FraudIdentityContent();
        content.setContentType(FraudDetailsContentType.IDENTITY);

        String field = resultMap.get("FIRST_NAME");
        if (field != null && !Strings.isNullOrEmpty(field)) {
            content.setFirstName(StringUtils.formatHuman(field));
        }

        field = resultMap.get("GIVEN_NAME");
        if (field != null && !Strings.isNullOrEmpty(field)) {
            content.setGivenName(StringUtils.formatHuman(field));
        }

        field = resultMap.get("LAST_NAME");
        if (field != null && !Strings.isNullOrEmpty(field)) {
            content.setLastName(StringUtils.formatHuman(field));
        }

        field = resultMap.get("PNR");
        if (field != null && !Strings.isNullOrEmpty(field)) {
            if (field.length() == 12) {
                content.setPersonIdentityNumber(field.substring(0, 8) + "-" + field.substring(8));
            } else {
                content.setPersonIdentityNumber(field);
            }
        }

        if (!Strings.isNullOrEmpty(content.getPersonIdentityNumber())) {

            // Set names for demo account.

            if (credentials.getUsername().equals(DemoCredentials.USER7.getUsername())) {
                content.setFirstName("Malin Anna");
                content.setGivenName("Malin");
                content.setLastName("Hansson");
                content.setPersonIdentityNumber("20121212-1212");
            }
            contents.add(content);
        }

        return contents;
    }

    private List<FraudAddressContent> extractAddressContent(Map<String, String> resultMap) {
        List<FraudAddressContent> contents = Lists.newArrayList();

        FraudAddressContent content = new FraudAddressContent();
        content.setContentType(FraudDetailsContentType.ADDRESS);

        String field = resultMap.get("REGISTERED_ADDRESS");
        if (field != null && !Strings.isNullOrEmpty(field)) {
            // For compatible with Bisnode data, format Lgh as lgh.
            String address = StringUtils.formatHuman(field);
            content.setAddress(address.replace("Lgh", "lgh"));
        }

        field = resultMap.get("TOWN");
        if (field != null && !Strings.isNullOrEmpty(field)) {
            content.setCity(StringUtils.formatHuman(field));
        }

        field = resultMap.get("ZIPCODE");
        if (field != null && !Strings.isNullOrEmpty(field)) {
            content.setPostalcode(field);
        }

        field = resultMap.get("COMMUNITY");
        if (field != null && !Strings.isNullOrEmpty(field)) {
            content.setCommunity(StringUtils.formatHuman(field));
        }

        if (!Strings.isNullOrEmpty(content.getAddress())) {
            contents.add(content);
        }

        return contents;
    }

    private List<FraudNonPaymentContent> extractNonPaymentsContent(List<Map<String, String>> nonPaymentsList) {
        List<FraudNonPaymentContent> contents = Lists.newArrayList();

        for (Map<String, String> dataMap : nonPaymentsList) {
            FraudNonPaymentContent content = new FraudNonPaymentContent();
            content.setContentType(FraudDetailsContentType.NON_PAYMENT);

            String field = dataMap.get("AMOUNT");
            if (field != null && !Strings.isNullOrEmpty(field)) {
                content.setAmount(Doubles.tryParse(field));
            }

            field = dataMap.get("CREDITOR_NAME");
            if (field != null && !Strings.isNullOrEmpty(field)) {
                content.setName(StringUtils.formatHuman(field));
            }

            field = dataMap.get("TYPE_TEXT");
            if (field != null && !Strings.isNullOrEmpty(field)) {
                content.setType(StringUtils.formatHuman(field));
            }

            field = dataMap.get("CREATED_DATE");
            if (field != null && !Strings.isNullOrEmpty(field)) {
                try {
                    content.setDate(ThreadSafeDateFormat.FORMATTER_DAILY.parse(field));
                } catch (ParseException e) {
                    log.error("Could not parse Datum from " + field, e);
                }
            }

            // For public claims, there is no Creditor, use the type text instead.

            if (Strings.isNullOrEmpty(content.getName()) && !Strings.isNullOrEmpty(content.getType())) {
                content.setName(content.getType());
            }

            // Remove non-payments that are older than 3 years (legal reasons).
            
            if (!Strings.isNullOrEmpty(content.getName()) && content.getDate().after(DateUtils.addYears(new Date(), -3))) {
                contents.add(content);
            } else {
                log.info("Not adding non-payment details, too old, " + content.getName());
            }
        }

        return contents;
    }

    private List<FraudCreditScoringContent> extractCreditScoringContent(Map<String, String> resultMap) {
        List<FraudCreditScoringContent> contents = Lists.newArrayList();

        FraudCreditScoringContent content = new FraudCreditScoringContent();
        content.setContentType(FraudDetailsContentType.SCORING);

        String field = resultMap.get("SCORING");
        if (field != null && !Strings.isNullOrEmpty(field)) {
            content.setScore(Ints.tryParse(field));
        }

        if (content.getScore() != 0) {
            content.setMaxScore(100);
            contents.add(content);
        }

        return contents;
    }

    private List<FraudRealEstateEngagementContent> extractRealEstateContent(List<Map<String, String>> realEstateList) {
        List<FraudRealEstateEngagementContent> contents = Lists.newArrayList();

        for (Map<String, String> dataMap : realEstateList) {
            FraudRealEstateEngagementContent content = new FraudRealEstateEngagementContent();
            content.setContentType(FraudDetailsContentType.REAL_ESTATE_ENGAGEMENT);

            String field = dataMap.get("REALESTATE_DESCRIPTION");
            if (field != null && !Strings.isNullOrEmpty(field)) {
                content.setName(StringUtils.formatHuman(field));

                // Parse name and municipality from REALESTATE_DESCRIPTION.
                // First word is municipality, rest is name.

                Iterable<String> description = splitter.split(field);

                if (Iterables.size(description) > 1) {
                    String municipality = Iterables.get(description, 0);

                    content.setName(StringUtils.formatHuman(field.replace(municipality + " ", "")));
                    content.setMuncipality(StringUtils.formatHuman(municipality));
                }
            }

            if (Strings.isNullOrEmpty(content.getMuncipality())) {
                field = dataMap.get("PARISH");
                if (field != null && !Strings.isNullOrEmpty(field)) {
                    content.setMuncipality(StringUtils.formatHuman(field));
                }
            }

            if (!Strings.isNullOrEmpty(content.getName())) {
                contents.add(content);
            }
        }

        return contents;
    }

    private List<FraudCreditorContent> extractCreditsContent(Map<String, String> resultMap) {
        List<FraudCreditorContent> contents = Lists.newArrayList();

        FraudCreditorContent content = new FraudCreditorContent();
        content.setContentType(FraudDetailsContentType.CREDITS);

        String field = resultMap.get("DEBT_NUMBER");
        if (field != null && !Strings.isNullOrEmpty(field)) {
            content.setNumber(Ints.tryParse(field));
        }

        field = resultMap.get("DEBT_SUM");
        if (field != null && !Strings.isNullOrEmpty(field)) {
            content.setAmount(Doubles.tryParse(field));
        }

        field = resultMap.get("DEBT_DATE");
        if (field != null && !Strings.isNullOrEmpty(field)) {
            try {
                content.setRegistered(ThreadSafeDateFormat.FORMATTER_INTEGER_DATE.parse(field));
            } catch (ParseException e) {
                log.error("Could not parse Datum from " + field, e);
            }
        }

        contents.add(content);

        return contents;
    }

    private List<FraudIncomeContent> extractIncomeContent(Map<String, String> resultMap) {
        List<FraudIncomeContent> contents = Lists.newArrayList();

        FraudIncomeContent content = new FraudIncomeContent();
        content.setContentType(FraudDetailsContentType.INCOME);

        String field = resultMap.get("TEXEBLE_INCOME");
        if (field != null && !Strings.isNullOrEmpty(field)) {
            content.setIncomeByService(Doubles.tryParse(field));
        }

        double incomeByCapital = 0;
        double defecitByCapital = 0;

        field = resultMap.get("INCOME_CAPITAL");
        if (field != null && !Strings.isNullOrEmpty(field)) {
            incomeByCapital = Doubles.tryParse(field);
        }

        field = resultMap.get("DEFICIT_CAPITAL");
        if (field != null && !Strings.isNullOrEmpty(field)) {
            defecitByCapital = Doubles.tryParse(field);
        }

        content.setIncomeByCapital(incomeByCapital - defecitByCapital);

        field = resultMap.get("TOTAL_INCOME");
        if (field != null && !Strings.isNullOrEmpty(field)) {
            content.setTotalIncome(Doubles.tryParse(field));
        }

        field = resultMap.get("FINAL_TAX");
        if (field != null && !Strings.isNullOrEmpty(field)) {
            content.setFinalTax(Doubles.tryParse(field));
        }

        field = resultMap.get("TAX_YEAR");
        if (field != null && !Strings.isNullOrEmpty(field)) {
            content.setTaxYear(field);
        }

        content.setRegistered(new Date());

        if (content.getTotalIncome() != 0) {
            contents.add(content);
        }

        return contents;
    }

    private List<FraudDetailsContent> extractCompanyEngagementContent(List<Map<String, String>> companyEngagementList) {
        List<FraudDetailsContent> contents = Lists.newArrayList();

        for (Map<String, String> dataMap : companyEngagementList) {
            FraudCompanyEngagementContent content = new FraudCompanyEngagementContent();
            content.setContentType(FraudDetailsContentType.COMPANY_ENGAGEMENT);

            String field = dataMap.get("ORG_NUMBER");
            if (field != null && !Strings.isNullOrEmpty(field)) {
                content.setNumber(field);

                // Seed company data.

                FraudCompanyContent company = seedCompanyData(field);

                if (company == null) {
                    continue;
                }

                if (!Strings.isNullOrEmpty(company.getLatestRegname())) {
                    dataMap.put("COMPANY_NAME", company.getLatestRegname());
                }
            }

            field = dataMap.get("COMPANY_NAME");
            if (field != null && !Strings.isNullOrEmpty(field)) {
                content.setName(StringUtils.formatHuman(field));
            }

            field = dataMap.get("FUNCTION");
            if (field != null && !Strings.isNullOrEmpty(field)) {
                content.setRoles(Lists.newArrayList(StringUtils.formatHuman(field)));
            }

            field = dataMap.get("FROM_DATE");
            if (field != null && !Strings.isNullOrEmpty(field)) {
                try {
                    content.setInDate(ThreadSafeDateFormat.FORMATTER_DAILY.parse(field));
                } catch (ParseException e) {
                    log.error("Could not parse FROM_DATE from " + field, e);
                }
            }

            field = dataMap.get("TO_DATE");
            if (field != null && !Strings.isNullOrEmpty(field)) {
                try {
                    content.setOutDate(ThreadSafeDateFormat.FORMATTER_DAILY.parse(field));
                } catch (ParseException e) {
                    log.error("Could not parse TO_DATE from " + field, e);
                }
            }

            if (!Strings.isNullOrEmpty(content.getNumber())) {
                contents.add(content);
            }
        }

        return contents;
    }

    /**
     * Takes a org. number and looks up company details.
     * 
     * @return
     */
    public FraudCompanyContent seedCompanyData(String orgNumber) {
        log.info("Seeding compnay data for " + orgNumber);
        GetData client = new GetData();
        GetDataSoap service = client.getGetDataSoap();

        if (credentials.isDebug()) {
            addUserAgentHandler(service);
        }

        GetDataRequest dataReq = new GetDataRequest();
        Account account = new Account();

        account.setUserName(configuration.getCreditSafe().getUsername());
        account.setPassword(configuration.getCreditSafe().getPassword());
        account.setLanguage(Language.SWE);

        dataReq.setBlockName(COMPANY_CREDIT_BLOCK);
        dataReq.setAccount(account);
        dataReq.setSearchNumber(orgNumber);

        GetDataResponse response = service.getDataBySecure(dataReq);

        // Validate response.

        Error error = response.getError();

        if (error != null) {
            log.info("Error from Creditsafe: " + error.getRejectText());
            return null;
        }

        // Parse XML data to structured format.

        Map<String, String> generalDataMap = Maps.newHashMap();
        List<Map<String, String>> directorsList = Lists.newArrayList();

        ElementNSImpl anyParameters = (ElementNSImpl) response.getParameters().getAny();
        Node dataSet = anyParameters.getFirstChild();
        Node parent = dataSet.getFirstChild();

        while (parent != null) {
            switch (parent.getNodeName()) {
            case "GETDATA_RESPONSE":
                parseGeneralData(generalDataMap, parent);
                break;
            case "DIRECTORS":
                parseTagData(directorsList, parent);
                break;
            }
            parent = parent.getNextSibling();
        }

        if (!isCompanyActive(orgNumber, generalDataMap)) {
            return null;
        }

        return createFraudCompanyContent(orgNumber, generalDataMap, directorsList);
    }

    private boolean isCompanyActive(String orgNumber, Map<String, String> generalDataMap) {

        String statusCodeValue = generalDataMap.get("COMPANY_STATUS_CODE");
        if (Strings.isNullOrEmpty(statusCodeValue)) {
            return false;
        }

        try {
            int status = Integer.parseInt(statusCodeValue.replace("s", "").replace("S", ""));
            // Status codes can be found in GD>Product>Project>Fraud Detection>From CreditSafe>HämtaData Appendix.xls
            return status < STATUS_CODE_FIRST_INACTIVE; // not including
        } catch (NumberFormatException e) {
            log.error("Could not parse status from company with orgnr: " + orgNumber, e);
        }

        return false;
    }

    public FraudCompanyContent createFraudCompanyContent(String orgNumber, Map<String, String> generalDataMap,
            List<Map<String, String>> directorsList) {
        FraudCompanyContent companyContent = new FraudCompanyContent();

        try {
            String field = generalDataMap.get("ORGNR");
            if (!Strings.isNullOrEmpty(field)) {
                companyContent.setOrgNumber(field);
            }

            field = generalDataMap.get("NAME");
            if (!Strings.isNullOrEmpty(field)) {
                companyContent.setName(StringUtils.formatHuman(field));
            }

            field = generalDataMap.get("LATEST_REGNAME");
            if (!Strings.isNullOrEmpty(field)) {
                companyContent.setLatestRegname(StringUtils.formatHuman(field));
            }

            field = generalDataMap.get("ADDRESS");
            if (!Strings.isNullOrEmpty(field)) {
                companyContent.setAddress(StringUtils.formatHuman(field));
            }

            field = generalDataMap.get("ZIPCODE");
            if (!Strings.isNullOrEmpty(field)) {
                companyContent.setZipcode(StringUtils.formatHuman(field));
            }

            field = generalDataMap.get("TOWN");
            if (!Strings.isNullOrEmpty(field)) {
                companyContent.setTown(StringUtils.formatHuman(field));
            }

            field = generalDataMap.get("F-TAX");
            if (!Strings.isNullOrEmpty(field)) {
                companyContent.setFtax(field);
            }

            field = generalDataMap.get("MOMS");
            if (!Strings.isNullOrEmpty(field)) {
                companyContent.setMoms(field);
            }

            field = generalDataMap.get("COMPANY_TYPE_TEXT");
            if (!Strings.isNullOrEmpty(field)) {
                companyContent.setType(field);
            }

            field = generalDataMap.get("COMPANY_STATUS");
            if (!Strings.isNullOrEmpty(field)) {
                companyContent.setStatus(field);
            }

            field = generalDataMap.get("RATING");
            if (!Strings.isNullOrEmpty(field) && !NO_CONTENT.equals(field)) {
                companyContent.setCreditRating(Integer.valueOf(field));
            }

            field = generalDataMap.get("NR_EMPLOYEES");
            if (!Strings.isNullOrEmpty(field) && !NO_CONTENT.equals(field)) {
                companyContent.setNbrEmployees(Integer.valueOf(field));
            }

            field = generalDataMap.get("REVENUE");
            if (!Strings.isNullOrEmpty(field) && !NO_CONTENT.equals(field)) {
                companyContent.setRevenue(Double.valueOf(field).intValue());
            }

            field = generalDataMap.get("PROFIT_AFTER_TAX");
            if (!Strings.isNullOrEmpty(field) && !NO_CONTENT.equals(field)) {
                companyContent.setProfitAfterTax(Double.valueOf(field).intValue());
            }

            field = generalDataMap.get("NET_PROFIT");
            if (!Strings.isNullOrEmpty(field) && !NO_CONTENT.equals(field)) {
                companyContent.setNetProfit(Double.valueOf(field).intValue());
            }

            field = generalDataMap.get("TOTAL_CAPITAL");
            if (!Strings.isNullOrEmpty(field) && !NO_CONTENT.equals(field)) {
                companyContent.setTotalCapital(Double.valueOf(field).intValue());
            }

            field = generalDataMap.get("KR_GROSS_PROFIT_MARGIN_PERCENT");
            if (!Strings.isNullOrEmpty(field)) {
                companyContent.setGrossProfitMarginPercentage(field);
            }

            field = generalDataMap.get("QUICK_RATIO");
            if (!Strings.isNullOrEmpty(field)) {
                companyContent.setQuickRatioPercent(field);
            }

            field = generalDataMap.get("SOLIDITET");
            if (!Strings.isNullOrEmpty(field)) {
                companyContent.setSolidityPercentage(field);
            }

            List<FraudCompanyDirector> directors = Lists.newArrayList();

            for (Map<String, String> directorMap : directorsList) {
                FraudCompanyDirector director = new FraudCompanyDirector();

                field = directorMap.get("NAME");
                if (!Strings.isNullOrEmpty(field)) {
                    director.setName(StringUtils.formatHuman(field));
                }

                field = directorMap.get("FUNCTION");
                if (!Strings.isNullOrEmpty(field)) {
                    director.setRole(StringUtils.formatHuman(field));
                }

                directors.add(director);
            }
            companyContent.setDirectors(directors);

        } catch (Exception e) {
            log.error("Could not parse data from company wiht orgNumber " + orgNumber, e);
            return null;
        }
        return companyContent;
    }

    private static void addUserAgentHandler(GetDataSoap service) {
        Binding binding = ((BindingProvider) service).getBinding();

        @SuppressWarnings("rawtypes")
        List<Handler> handlerChain = binding.getHandlerChain();

        if (handlerChain == null) {
            handlerChain = Lists.newArrayList();
        }

        handlerChain.add(new SOAPLoggingHandler());
        binding.setHandlerChain(handlerChain);
    }

    private String parseErrorMessageKnown(Catalog catalog, Error error) {
        String errorCode = error.getCauseOfReject();
        if (errorCode != null) {

            switch (errorCode) {
            case "S2":
                return catalog.getString("protected identity");
            case "S3":
                return catalog.getString("locked identity");
            case "S4":
                return catalog.getString("deceased");
            case "S5":
                return catalog.getString("removed from register");
            case "S6":
                return catalog.getString("emigrated");
            case "S7":
                return catalog.getString("changed person number");
            }
        }
        return null;
    }

    public List<FraudDetailsContent> seedPersonData(String  personNumber) {
        log.info(String.format("Seeding person data for %s.", personNumber));
        GetData client = new GetData();
        GetDataSoap service = client.getGetDataSoap();

        GetDataRequest dataReq = new GetDataRequest();
        Account account = new Account();

        account.setUserName(configuration.getCreditSafe().getUsername());
        account.setPassword(configuration.getCreditSafe().getPassword());
        account.setLanguage(Language.SWE);

        dataReq.setBlockName(PERSON_BLOCK);
        dataReq.setAccount(account);
        dataReq.setSearchNumber(personNumber);

        GetDataResponse response = service.getDataBySecure(dataReq);

        // Validate response.

        Error error = response.getError();

        if (error != null) {
            log.info("Error from Creditsafe: " + error.getRejectText());
            return null;
        }

        // Parse XMl data to structured format.

        Map<String, String> generalDataMap = Maps.newHashMap();

        ElementNSImpl anyParameters = (ElementNSImpl) response.getParameters().getAny();
        Node dataSet = anyParameters.getFirstChild();
        Node parent = dataSet.getFirstChild();

        while (parent != null) {
            switch (parent.getNodeName()) {
            case "GETDATA_RESPONSE":
                parseGeneralData(generalDataMap, parent);
                break;
            }
            parent = parent.getNextSibling();
        }

        List<FraudDetailsContent> detailsContent = Lists.newArrayList();
        detailsContent.addAll(extractAddressContent(generalDataMap));
        
        return detailsContent;
    }
    
    @Override
    public boolean login() throws AuthenticationException, AuthorizationException {
        if (credentials.isDemoCredentials()) {
            credentials.setUsername(DemoCredentials.USER7.getUsername());
            context.setTestContext(true);

            log.info("Using test settings.");
        }

        // Take the value from activation.

        if (!Strings.isNullOrEmpty(credentials.getSupplementalInformation())) {
            log.info("Request from fraud activation service.");
            credentials.setSensitivePayload(FraudUtils.ID_CONTROL_AUTH_KEY, credentials.getSupplementalInformation());
        }

        String authTokenGenerated = StringUtils.hashAsStringSHA1(credentials.getUsername(),
                FraudUtils.ID_CONTROL_AUTH_SALT);

        // If there there is no FraudUtils.ID_CONTROL_AUTH_KEY on the credentials payload, authenticate with BankID.
        // If there is a token, validate the token against the person number.

        String authTokenActual = credentials.getSensitivePayload(FraudUtils.ID_CONTROL_AUTH_KEY);

        if (!context.isTestContext()) {
            if (authTokenActual == null) {
                if (!request.isManual()) {
                    log.info("No auth token exists, but cannot ask for BankId for auto refresh.");
                    throw SessionError.SESSION_EXPIRED.exception();
                }
                
                log.info("No auth token exists.");

                // Create the authenticator

                SignicatBankIdAuthenticator authenticator = new SignicatBankIdAuthenticator(credentials.getField(Field.Key.USERNAME),
                        credentials.getUserId(),
                        credentials.getId(),
                        context.getCatalog(),
                        new CredentialsSignicatBankIdAuthenticationHandler(credentials, context));

                // Run the authenticator synchronously.

                authenticator.run();

                // Save token for future.

                if (credentials.getStatus() != CredentialsStatus.AUTHENTICATION_ERROR) {
                    credentials.setSensitivePayload(FraudUtils.ID_CONTROL_AUTH_KEY, authTokenGenerated);
                }
            } else {
                Preconditions.checkState(authTokenGenerated.equals(authTokenActual),
                        "Auth token exists but is incorrect. generated: %s, actual: %s",
                        authTokenGenerated, authTokenActual);
            }
        }

        credentials.setSupplementalInformation(null);
        statusUpdater.updateStatus(CredentialsStatus.UPDATING);
        
        return true;
    }

    @Override
    public void logout() throws Exception {
        // TODO Implement.
    }
}
