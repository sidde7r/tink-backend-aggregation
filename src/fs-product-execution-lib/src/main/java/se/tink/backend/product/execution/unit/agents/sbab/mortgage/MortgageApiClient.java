package se.tink.backend.product.execution.unit.agents.sbab.mortgage;

import com.google.common.base.Objects;
import com.google.common.base.Preconditions;
import com.google.common.base.Strings;
import com.google.common.collect.ListMultimap;
import com.google.common.collect.Multimaps;
import com.sun.jersey.api.client.Client;
import com.sun.jersey.api.client.ClientResponse;
import com.sun.jersey.api.client.UniformInterfaceException;
import com.sun.jersey.api.client.WebResource;
import com.sun.jersey.core.util.MultivaluedMapImpl;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.Base64;
import java.util.List;
import java.util.Optional;
import javax.ws.rs.core.HttpHeaders;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.MultivaluedMap;
import javax.ws.rs.core.Response;
import se.tink.backend.common.application.mortgage.MortgageProvider;
import se.tink.backend.common.config.SbabMortgageIntegrationConfiguration;
import se.tink.backend.core.enums.GenericApplicationFieldGroupNames;
import se.tink.backend.product.execution.unit.agents.exceptions.application.InvalidApplicationException;
import se.tink.backend.product.execution.unit.agents.sbab.mortgage.model.ApplicationErrorEntity;
import se.tink.backend.product.execution.unit.agents.sbab.mortgage.model.request.MortgageApplicationRequest;
import se.tink.backend.product.execution.unit.agents.sbab.mortgage.model.response.DiscountResponse;
import se.tink.backend.product.execution.unit.agents.sbab.mortgage.model.response.InterestsResponse;
import se.tink.backend.product.execution.unit.agents.sbab.mortgage.model.response.MortgageApplicationResponse;
import se.tink.backend.product.execution.unit.agents.sbab.mortgage.model.request.MortgageSignatureRequest;
import se.tink.backend.product.execution.unit.agents.sbab.mortgage.model.response.MortgageSignatureResponse;
import se.tink.backend.product.execution.unit.agents.sbab.mortgage.model.response.MortgageSignatureStatus;
import se.tink.backend.product.execution.unit.agents.sbab.mortgage.model.response.MortgageSignatureStatusResponse;
import se.tink.backend.product.execution.unit.agents.sbab.mortgage.model.response.MortgageStatus;
import se.tink.backend.product.execution.unit.agents.sbab.mortgage.model.response.MortgageStatusResponse;
import se.tink.backend.product.execution.configuration.ProductExecutorConfiguration;
import se.tink.backend.product.execution.log.ProductExecutionLogger;
import se.tink.backend.product.execution.model.ProductType;
import se.tink.backend.product.execution.tracker.CreateProductExecutorTracker;
import se.tink.backend.utils.ApplicationUtils;
import se.tink.libraries.application.GenericApplication;
import se.tink.libraries.application.GenericApplicationFieldGroup;
import se.tink.libraries.uuid.UUIDUtils;

public class MortgageApiClient {
    private static final ProductExecutionLogger log = new ProductExecutionLogger(MortgageApiClient.class);

    public static final String DEFAULT_USER_AGENT = "Tink (+https://www.tink.se/; noc@tink.se)";

    private static final String REQUEST_PATH = "/LAR_WS/rest/v1";
    private static final String SIGN_PATH = "/SIGN/rest/v1";

    private static final String MORTGAGE_SIGN_STATUS_PATH = SIGN_PATH + "/signeringar/%s/status";
    private static final String MORTGAGE_APPLICATION_PATH = REQUEST_PATH + "/laneansokningar";
    private static final String CREATE_MORTGAGE_SIGNATURE_PATH = SIGN_PATH + "/signeringar/laneansokan";
    private static final String MORTGAGE_STATUS_PATH = REQUEST_PATH + "/laneansokningar/%s/status";
    private static final String INTEREST_RATES_PATH = REQUEST_PATH + "/rantor";
    private static final String DISCOUNT_PATH = REQUEST_PATH + "/rabatter";

    private static final int HTTP_OK_STATUS_CODE = ClientResponse.Status.OK.getStatusCode();

    protected final Client client;
    private final CreateProductExecutorTracker tracker;
    private String remoteIp;
    private String mortgageBaseUrl;
    private String authorizationValue;
    protected String signBaseUrl;

    public MortgageApiClient(Client client,
            ProductExecutorConfiguration configuration,
            CreateProductExecutorTracker tracker) {
        this.client = client;
        this.tracker = tracker;

        setConfiguration(configuration);
    }

    private void setConfiguration(ProductExecutorConfiguration configuration) {
        SbabMortgageIntegrationConfiguration mortgageConfiguration = configuration.getIntegrations().getSbab()
                .getMortgage();
        if (mortgageConfiguration == null) {
            throw new IllegalArgumentException("Can not find SBAB mortgage configuration!");
        }

        this.signBaseUrl = configuration.getIntegrations().getSbab().getSignBaseUrl();
        this.mortgageBaseUrl = configuration.getMortgageURI(MortgageProvider.SBAB_BANKID);
        this.authorizationValue = formatAuthorizationValue(
                mortgageConfiguration.getUsername(),
                mortgageConfiguration.getPassword()
        );
    }

    public MortgageSignatureRequest getSignatureRequest(GenericApplication application) {
        ListMultimap<String, GenericApplicationFieldGroup> fieldGroupByName = Multimaps.index(
                application.getFieldGroups(), GenericApplicationFieldGroup::getName);

        Optional<MortgageSignatureRequest> signatureRequest = MortgageSignatureRequest.createFromApplication(
                getApplicantFieldGroups(fieldGroupByName), application.getPersonalNumber());

        Preconditions.checkState(signatureRequest.isPresent(),
                "Could not create signature request");

        return signatureRequest.get();
    }

    public String createSignature(MortgageSignatureRequest signatureRequest) throws Exception {
        try {
            log.debug(ProductExecutionLogger.newBuilder()
                    .withMessage("Start to create mortgage application signature in SBAB"));
            MortgageSignatureResponse response = createJsonRequest(getMortgageUrl(CREATE_MORTGAGE_SIGNATURE_PATH))
                    .header(HttpHeaders.AUTHORIZATION, authorizationValue)
                    .post(MortgageSignatureResponse.class, signatureRequest);
            log.debug(ProductExecutionLogger.newBuilder().withMessage("Mortgage application signature created in SBAB"));
            tracker.trackCreateProductSignature("sbab", ProductType.MORTGAGE, HTTP_OK_STATUS_CODE);
            return Preconditions.checkNotNull(response.getSignatureId());
        } catch (UniformInterfaceException e) {
            tracker.trackCreateProductSignature("sbab", ProductType.MORTGAGE, e.getResponse().getStatus());
            throw e;
        }
    }

    public String sendApplication(MortgageApplicationRequest applicationRequest, String signatureId)
            throws Exception {
        try {
            log.debug(ProductExecutionLogger.newBuilder().withMessage(
                    String.format("Start to send application to SBAB with signatureId = %s",
                            applicationRequest.getSignatureId())));
            applicationRequest.setSignatureId(signatureId);

            MortgageApplicationResponse applicationResponse = createJsonRequest(
                    getMortgageUrl(MORTGAGE_APPLICATION_PATH))
                    .header(HttpHeaders.AUTHORIZATION, authorizationValue)
                    .post(MortgageApplicationResponse.class, applicationRequest);

            log.debug(ProductExecutionLogger.newBuilder().withMessage(
                    String.format("Application sent to SBAB with signatureId = %s",
                            applicationRequest.getSignatureId())));
            tracker.trackSubmitApplication("sbab", ProductType.MORTGAGE, HTTP_OK_STATUS_CODE);
            return Preconditions.checkNotNull(applicationResponse.getSbabId());
        } catch (UniformInterfaceException e) {
            tracker.trackSubmitApplication("sbab", ProductType.MORTGAGE, e.getResponse().getStatus());
            ClientResponse response = e.getResponse();

            if (Objects.equal(response.getStatus(), Response.Status.BAD_REQUEST.getStatusCode())) {
                ApplicationErrorEntity responseEntity = response.getEntity(ApplicationErrorEntity.class);

                Optional<String> error = responseEntity.getError();

                if (error.isPresent()) {
                    throw new InvalidApplicationException(error.get());
                }
            }

            throw e;
        }
    }

    public void setRemoteIp(String remoteIp) {
        this.remoteIp = remoteIp;
    }

    protected String getSignUrl(String path) {
        return getUrl(signBaseUrl, path);
    }

    protected String getUrl(String baseUrl, String path) {
        return baseUrl + path;
    }

    protected String getSignUrl(String path, Object... args) {
        return String.format(getSignUrl(path), args);
    }

    public MortgageSignatureStatus getMortgageSigningStatus(String signatureId) throws Exception {
        try {
            MortgageSignatureStatusResponse signingStatusResponse = createJsonRequest(
                    getMortgageUrl(MORTGAGE_SIGN_STATUS_PATH, signatureId))
                    .header(HttpHeaders.AUTHORIZATION, authorizationValue)
                    .get(MortgageSignatureStatusResponse.class);
            tracker.trackFetchApplicationSignStatus("sbab", ProductType.MORTGAGE,
                    HTTP_OK_STATUS_CODE);
            return signingStatusResponse.getStatus();
        } catch (UniformInterfaceException e) {
            tracker.trackFetchApplicationSignStatus("sbab", ProductType.MORTGAGE, e.getResponse().getStatus());
            throw e;
        }
    }

    WebResource.Builder createJsonRequest(String url, MultivaluedMap<String, String> queryParameters) {
        return client.resource(url)
                .queryParams(queryParameters)
                .header("User-Agent", DEFAULT_USER_AGENT)
                .type(MediaType.APPLICATION_JSON)
                .accept(MediaType.APPLICATION_JSON);
    }

    WebResource.Builder createJsonRequest(String url) throws Exception {
        return createRequest(url)
                .type(MediaType.APPLICATION_JSON)
                .accept(MediaType.APPLICATION_JSON);
    }

    WebResource.Builder createRequest(String url) {
        WebResource.Builder builder = client.resource(url).header("User-Agent", DEFAULT_USER_AGENT);
        return Strings.isNullOrEmpty(remoteIp) ? builder : builder.header("X-Forwarded-For", remoteIp);
    }

    WebResource.Builder createFormEncodedJsonRequest(String url) throws Exception {
        return createRequest(url)
                .type(MediaType.APPLICATION_FORM_URLENCODED_TYPE)
                .accept(MediaType.APPLICATION_JSON);
    }

    WebResource.Builder createFormEncodedHtmlRequest(String url) {
        return createRequest(url)
                .type(MediaType.APPLICATION_FORM_URLENCODED_TYPE)
                .accept(MediaType.TEXT_HTML_TYPE);
    }

    String getRedirectUrl(ClientResponse response, String baseUrl) throws URISyntaxException {
        String location = response.getHeaders().getFirst("Location");
        Preconditions.checkState(!Strings.isNullOrEmpty(location), "Did not get redirect url in response from bank.");
        return hasHost(location) ? location : baseUrl + location;
    }

    private static boolean hasHost(String url) throws URISyntaxException {
        URI uri = new URI(url);
        return uri.getHost() != null;
    }

    ClientResponse createGetRequest(String url) {
        return createRequest(url).get(ClientResponse.class);
    }

    ClientResponse createGetRequestWithReferer(String url, String referer) {
        return createRequest(url).header("Referer", referer).get(ClientResponse.class);
    }

    private List<GenericApplicationFieldGroup> getApplicantFieldGroups(
            ListMultimap<String, GenericApplicationFieldGroup> fieldGroupByName) {
        Optional<GenericApplicationFieldGroup> applicantGroup = ApplicationUtils
                .getFirst(fieldGroupByName, GenericApplicationFieldGroupNames.APPLICANTS);

        Preconditions.checkState(applicantGroup.isPresent(),
                "No 'Applicants' data supplied");

        return applicantGroup.get().getSubGroups();
    }

    private GenericApplicationFieldGroup getMortgageSecurityFieldGroup(
            ListMultimap<String, GenericApplicationFieldGroup> fieldGroupByName) {
        Optional<GenericApplicationFieldGroup> mortgageSecurityGroup = ApplicationUtils
                .getFirst(fieldGroupByName, GenericApplicationFieldGroupNames.MORTGAGE_SECURITY);

        Preconditions.checkState(mortgageSecurityGroup.isPresent(),
                "No 'Mortgage Security' data supplied");

        return mortgageSecurityGroup.get();
    }

    private GenericApplicationFieldGroup getHouseholdFieldGroup(
            ListMultimap<String, GenericApplicationFieldGroup> fieldGroupByName) {
        Optional<GenericApplicationFieldGroup> householdGroup = ApplicationUtils
                .getFirst(fieldGroupByName, GenericApplicationFieldGroupNames.HOUSEHOLD);

        Preconditions.checkState(householdGroup.isPresent(),
                "No 'Household' data supplied");

        return householdGroup.get();
    }

    private GenericApplicationFieldGroup getCurrentMortgageGroup(
            ListMultimap<String, GenericApplicationFieldGroup> fieldGroupByName) {
        Optional<GenericApplicationFieldGroup> currentMortgageGroup = ApplicationUtils
                .getFirst(fieldGroupByName, GenericApplicationFieldGroupNames.CURRENT_MORTGAGE);

        Preconditions.checkState(currentMortgageGroup.isPresent(),
                "No 'Current Mortgage' data supplied");

        return currentMortgageGroup.get();
    }

    public MortgageApplicationRequest getMortgageApplicationRequest(GenericApplication application)
            throws InvalidApplicationException {
        ListMultimap<String, GenericApplicationFieldGroup> fieldGroupByName = Multimaps.index(
                application.getFieldGroups(), GenericApplicationFieldGroup::getName);

        List<GenericApplicationFieldGroup> applicants = getApplicantFieldGroups(fieldGroupByName);
        GenericApplicationFieldGroup mortgageSecurity = getMortgageSecurityFieldGroup(fieldGroupByName);
        GenericApplicationFieldGroup household = getHouseholdFieldGroup(fieldGroupByName);
        GenericApplicationFieldGroup currentMortgage = getCurrentMortgageGroup(fieldGroupByName);

        return MortgageApplicationRequest
                .createFromApplication(applicants, mortgageSecurity, household, currentMortgage,
                        UUIDUtils.toTinkUUID(application.getCredentialsId()));
    }

    private String getMortgageUrl(String path) {
        return getUrl(mortgageBaseUrl, path);
    }

    private String getMortgageUrl(String path, Object... args) {
        return String.format(getMortgageUrl(path), args);
    }

    private static String formatAuthorizationValue(String username, String password) {
        String base64EncodedVal = Base64.getEncoder().encodeToString(
                String.format(
                        "%s:%s",
                        username,
                        password
                ).getBytes()
        );
        return String.format("Basic %s", base64EncodedVal);
    }

    public MortgageStatus getMortgageStatus(String mortgageId) throws Exception {
        try {
            MortgageStatusResponse response = createJsonRequest(getMortgageUrl(MORTGAGE_STATUS_PATH, mortgageId))
                    .header(HttpHeaders.AUTHORIZATION, authorizationValue)
                    .get(MortgageStatusResponse.class);

            tracker.trackFetchApplicationStatus("sbab", ProductType.MORTGAGE, HTTP_OK_STATUS_CODE);
            return response.getStatus();
        } catch (UniformInterfaceException e) {
            tracker.trackFetchApplicationStatus("sbab", ProductType.MORTGAGE, e.getResponse().getStatus());
            if (Objects.equal(e.getResponse().getStatus(), Response.Status.NOT_FOUND.getStatusCode())) {

                log.error(ProductExecutionLogger
                        .newBuilder().withMessage("Could not find mortgage at bank"));
            }

            throw e;
        }
    }

    public InterestsResponse getInterestRates(int marketValue, int mortgageValue) throws Exception {
        MultivaluedMap<String, String> queryParameters = new MultivaluedMapImpl();
        queryParameters.add("marknadsvarde", String.valueOf(marketValue));
        queryParameters.add("soktBelopp", String.valueOf(mortgageValue));

        try {
            InterestsResponse response = createJsonRequest(getMortgageUrl(INTEREST_RATES_PATH),
                    queryParameters)
                    .header(HttpHeaders.AUTHORIZATION, authorizationValue)
                    .get(InterestsResponse.class);
            tracker.trackFetchProductInformation("sbab", ProductType.MORTGAGE,
                    HTTP_OK_STATUS_CODE);
            return response;
        } catch (UniformInterfaceException e) {
            tracker.trackFetchProductInformation("sbab", ProductType.MORTGAGE, e.getResponse().getStatus());
            throw e;
        }
    }

    public DiscountResponse getMortgageDiscounts(int numberOfApplicants, int mortgageValue,
            String mortgagePurpose) {
        MultivaluedMap<String, String> queryParameters = new MultivaluedMapImpl();
        queryParameters.add("antalSokande", String.valueOf(numberOfApplicants));
        queryParameters.add("soktBelopp", String.valueOf(mortgageValue));
        queryParameters.add("syfte", mortgagePurpose);
        try {
            DiscountResponse response = createJsonRequest(getMortgageUrl(DISCOUNT_PATH), queryParameters)
                    .header(HttpHeaders.AUTHORIZATION, authorizationValue)
                    .get(DiscountResponse.class);
            tracker.trackFetchProductDiscount("sbab", ProductType.MORTGAGE, HTTP_OK_STATUS_CODE);
            return response;
        } catch (UniformInterfaceException e) {
            tracker.trackFetchProductDiscount("sbab", ProductType.MORTGAGE, e.getResponse().getStatus());
            throw e;
        }
    }
}
