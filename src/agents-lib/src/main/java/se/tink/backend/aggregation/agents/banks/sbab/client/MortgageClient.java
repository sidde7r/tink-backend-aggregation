package se.tink.backend.aggregation.agents.banks.sbab.client;

import com.google.common.base.Objects;
import com.google.common.base.Preconditions;
import com.google.common.collect.ListMultimap;
import com.google.common.collect.Multimaps;
import com.sun.jersey.api.client.Client;
import com.sun.jersey.api.client.ClientResponse;
import com.sun.jersey.api.client.UniformInterfaceException;
import com.sun.jersey.core.util.MultivaluedMapImpl;
import java.util.Base64;
import java.util.List;
import java.util.Optional;
import javax.ws.rs.core.HttpHeaders;
import javax.ws.rs.core.MultivaluedMap;
import javax.ws.rs.core.Response;
import se.tink.backend.aggregation.agents.banks.sbab.model.request.MortgageApplicationRequest;
import se.tink.backend.aggregation.agents.banks.sbab.model.request.MortgageSignatureRequest;
import se.tink.backend.aggregation.agents.banks.sbab.model.response.ApplicationErrorEntity;
import se.tink.backend.aggregation.agents.banks.sbab.model.response.DiscountResponse;
import se.tink.backend.aggregation.agents.banks.sbab.model.response.InterestsResponse;
import se.tink.backend.aggregation.agents.banks.sbab.model.response.MortgageApplicationResponse;
import se.tink.backend.aggregation.agents.banks.sbab.model.response.MortgageSignatureResponse;
import se.tink.backend.aggregation.agents.banks.sbab.model.response.MortgageSignatureStatus;
import se.tink.backend.aggregation.agents.banks.sbab.model.response.MortgageSignatureStatusResponse;
import se.tink.backend.aggregation.agents.banks.sbab.model.response.MortgageStatus;
import se.tink.backend.aggregation.agents.banks.sbab.model.response.MortgageStatusResponse;
import se.tink.backend.aggregation.agents.exceptions.application.InvalidApplicationException;
import se.tink.backend.aggregation.log.AggregationLogger;
import se.tink.backend.aggregation.agents.utils.CreateProductExecutorTracker;
import se.tink.backend.aggregation.rpc.Credentials;
import se.tink.backend.aggregation.rpc.Field;
import se.tink.backend.aggregation.rpc.ProductType;
import se.tink.backend.common.config.SbabIntegrationConfiguration;
import se.tink.backend.common.config.SbabMortgageIntegrationConfiguration;
import se.tink.backend.core.enums.GenericApplicationFieldGroupNames;
import se.tink.backend.utils.ApplicationUtils;
import se.tink.libraries.application.GenericApplication;
import se.tink.libraries.application.GenericApplicationFieldGroup;

public class MortgageClient extends SBABClient {

    private static final AggregationLogger log = new AggregationLogger(MortgageClient.class);

    private static final String REQUEST_PATH = "/LAR_WS/rest/v1";
    private static final String SIGN_PATH = "/SIGN/rest/v1";

    private static final String INTEREST_RATES_PATH = REQUEST_PATH + "/rantor";
    private static final String DISCOUNT_PATH = REQUEST_PATH + "/rabatter";
    private static final String MORTGAGE_APPLICATION_PATH = REQUEST_PATH + "/laneansokningar";
    private static final String MORTGAGE_STATUS_PATH = REQUEST_PATH + "/laneansokningar/%s/status";

    private static final String MORTGAGE_SIGN_STATUS_PATH = SIGN_PATH + "/signeringar/%s/status";
    private static final String CREATE_MORTGAGE_SIGNATURE_PATH = SIGN_PATH + "/signeringar/laneansokan";
    private static final int HTTP_OK_STATUS_CODE = ClientResponse.Status.OK.getStatusCode();

    private String mortgageBaseUrl;

    private String authorizationValue;

    private final CreateProductExecutorTracker tracker;

    public MortgageClient(Client client, Credentials credentials, CreateProductExecutorTracker tracker, String aggregator) {
        super(client, credentials, aggregator);

        this.tracker = tracker;
    }

    @Override
    public void setConfiguration(SbabIntegrationConfiguration configuration) {
        super.setConfiguration(configuration);

        SbabMortgageIntegrationConfiguration mortgageConfiguration = configuration.getMortgage();

        if (mortgageConfiguration != null) {
            String schema = mortgageConfiguration.isHttps() ? "https://" : "http://";
            this.mortgageBaseUrl = schema + mortgageConfiguration.getTargetHost();

            this.authorizationValue = formatAuthorizationValue(
                    mortgageConfiguration.getUsername(),
                    mortgageConfiguration.getPassword()
            );
        }
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
            String mortgagePurpose)
            throws Exception {
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
                log.error("Could not find mortgage at bank");
            }

            throw e;
        }
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

    public String sendApplication(MortgageApplicationRequest applicationRequest, String signatureId)
            throws Exception {
        try {
            applicationRequest.setSignatureId(signatureId);

            MortgageApplicationResponse applicationResponse = createJsonRequest(
                    getMortgageUrl(MORTGAGE_APPLICATION_PATH))
                    .header(HttpHeaders.AUTHORIZATION, authorizationValue)
                    .post(MortgageApplicationResponse.class, applicationRequest);

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

    public MortgageApplicationRequest getMortgageApplicationRequest(GenericApplication application)
            throws InvalidApplicationException {
        ListMultimap<String, GenericApplicationFieldGroup> fieldGroupByName = Multimaps.index(
                application.getFieldGroups(), GenericApplicationFieldGroup::getName);

        List<GenericApplicationFieldGroup> applicants = getApplicantFieldGroups(fieldGroupByName);
        GenericApplicationFieldGroup mortgageSecurity = getMortgageSecurityFieldGroup(fieldGroupByName);
        GenericApplicationFieldGroup household = getHouseholdFieldGroup(fieldGroupByName);
        GenericApplicationFieldGroup currentMortgage = getCurrentMortgageGroup(fieldGroupByName);

        return MortgageApplicationRequest
                .createFromApplication(applicants, mortgageSecurity, household, currentMortgage, credentials);
    }

    public String createSignature(MortgageSignatureRequest signatureRequest) throws Exception {
        try {
            MortgageSignatureResponse response = createJsonRequest(getMortgageUrl(CREATE_MORTGAGE_SIGNATURE_PATH))
                    .header(HttpHeaders.AUTHORIZATION, authorizationValue)
                    .post(MortgageSignatureResponse.class, signatureRequest);
            tracker.trackCreateProductSignature("sbab", ProductType.MORTGAGE, HTTP_OK_STATUS_CODE);
            return Preconditions.checkNotNull(response.getSignatureId());
        } catch (UniformInterfaceException e) {
            tracker.trackCreateProductSignature("sbab", ProductType.MORTGAGE, e.getResponse().getStatus());
            throw e;
        }
    }

    public MortgageSignatureRequest getSignatureRequest(GenericApplication application) {
        ListMultimap<String, GenericApplicationFieldGroup> fieldGroupByName = Multimaps.index(
                application.getFieldGroups(), GenericApplicationFieldGroup::getName);

        Optional<MortgageSignatureRequest> signatureRequest = MortgageSignatureRequest.createFromApplication(
                getApplicantFieldGroups(fieldGroupByName), credentials.getField(Field.Key.USERNAME));

        Preconditions.checkState(signatureRequest.isPresent(),
                "Could not create signature request");

        return signatureRequest.get();
    }

    private List<GenericApplicationFieldGroup> getApplicantFieldGroups(
            ListMultimap<String, GenericApplicationFieldGroup> fieldGroupByName) {
        Optional<GenericApplicationFieldGroup> applicantGroup = ApplicationUtils
                .getFirst(fieldGroupByName, GenericApplicationFieldGroupNames.APPLICANTS);

        Preconditions.checkState(applicantGroup.isPresent(),
                "No 'Applicants' data supplied");

        return applicantGroup.get().getSubGroups();
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

    private GenericApplicationFieldGroup getMortgageSecurityFieldGroup(
            ListMultimap<String, GenericApplicationFieldGroup> fieldGroupByName) {
        Optional<GenericApplicationFieldGroup> mortgageSecurityGroup = ApplicationUtils
                .getFirst(fieldGroupByName, GenericApplicationFieldGroupNames.MORTGAGE_SECURITY);

        Preconditions.checkState(mortgageSecurityGroup.isPresent(),
                "No 'Mortgage Security' data supplied");

        return mortgageSecurityGroup.get();
    }

    private String getMortgageUrl(String path) {
        return getUrl(mortgageBaseUrl, path);
    }

    private String getMortgageUrl(String path, Object... args) {
        return String.format(getMortgageUrl(path), args);
    }
}
