package se.tink.backend.product.execution.unit.agents.sbab.mortgage;

import com.google.common.base.Strings;
import com.sun.jersey.api.client.Client;
import com.sun.jersey.api.client.ClientResponse;
import com.sun.jersey.api.client.WebResource;
import javax.ws.rs.core.MediaType;
import org.eclipse.jetty.http.HttpStatus;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import se.tink.backend.product.execution.unit.agents.sbab.mortgage.model.response.BankIdPollResponse;
import se.tink.backend.product.execution.unit.agents.sbab.mortgage.model.response.BankIdStartResponse;
import se.tink.backend.product.execution.unit.agents.sbab.mortgage.model.BankIdStatus;
import se.tink.backend.product.execution.unit.agents.sbab.mortgage.model.SignFormRequestBody;
import se.tink.backend.product.execution.configuration.ProductExecutorConfiguration;
import se.tink.backend.product.execution.log.ProductExecutionLogger;
import se.tink.backend.product.execution.tracker.CreateProductExecutorTracker;

public class MortgageSignClient extends MortgageApiClient {
    private static final ProductExecutionLogger log = new ProductExecutionLogger(MortgageSignClient.class);

    private static final String BANKID_SIGN_WEBPAGE_PATH = "/sign/%s";

    private static final String START_PATH = "/nx/%s/start";
    private static final String POLL_PATH = "/nx/%s/poll";

    protected String signBaseUrl;
    private String signPath;

    public MortgageSignClient(Client client,
            ProductExecutorConfiguration configuration,
            CreateProductExecutorTracker tracker) {
        super(client, configuration, tracker);
    }

    public SignFormRequestBody initiateSignProcess(String mortgageSignatureId) throws Exception {
        String signWebPageUrl = getSignUrl(BANKID_SIGN_WEBPAGE_PATH, mortgageSignatureId);
        Document signWebPage = getJsoupDocument(signWebPageUrl);

        // Fetch the needed values from the SBAB sign web page.
        Element signForm = signWebPage.select("form[id=nx_sign]").first();
        return SignFormRequestBody.from(signForm);
    }

    private Document getJsoupDocument(String url) {
        return Jsoup.parse(createRequest(url).accept(MediaType.TEXT_HTML).get(String.class));
    }

    public BankIdStartResponse initiateSign(SignFormRequestBody signFormRequestBody) throws Exception {
        setSignMode(signFormRequestBody);

        signFormRequestBody.add("nx_serial_number", signFormRequestBody.getFirst("nx_userid"));

        return createFormEncodedJsonRequest(getSignUrl(START_PATH, signFormRequestBody.getClient()))
                .post(BankIdStartResponse.class, signFormRequestBody);
    }

    private void setSignMode(SignFormRequestBody signFormRequestBody) {
        signPath = signFormRequestBody.getSignUri();
    }

    public BankIdStatus getStatus(SignFormRequestBody signFormRequestBody, String orderReference) throws Exception {
        if (!signFormRequestBody.containsKey("nx_orderref")) {
            signFormRequestBody.add("nx_orderref", orderReference);
        }

        BankIdPollResponse pollResponse = createFormEncodedHtmlRequest(
                getSignUrl(POLL_PATH, signFormRequestBody.getClient()))
                .post(BankIdPollResponse.class, signFormRequestBody);



        log.info(ProductExecutionLogger
                .newBuilder()
                .withMessage(String.format("BankID: Awaiting sign. Status = %s", pollResponse.getStatus())));

        switch(pollResponse.getBankIdStatus()) {
        case DONE:
            signFormRequestBody.add("nx_signature", pollResponse.getSignature());
            signFormRequestBody.add("nx_ocsp", pollResponse.getOcsp());

            WebResource.Builder signRequest = createFormEncodedHtmlRequest(getSignUrl(signPath));
            boolean hasReferer = !Strings.isNullOrEmpty(signFormRequestBody.getReferer());

            if (hasReferer) {
                signRequest = signRequest.header("Referer", signFormRequestBody.getReferer());
            }

            ClientResponse signResponse = signRequest.post(ClientResponse.class, signFormRequestBody);


            log.info(ProductExecutionLogger
                    .newBuilder()
                    .withMessage(String.format("BankID: Redirect HTTP Status = %s", signResponse.getStatus())));

            if (HttpStatus.isSuccess(signResponse.getStatus())) {
                return BankIdStatus.DONE;
            } else if (HttpStatus.isRedirection(signResponse.getStatus())) {
                String location = getRedirectUrl(signResponse, signBaseUrl);

                if (hasReferer) {
                    createGetRequestWithReferer(location, signFormRequestBody.getReferer());
                } else {
                    createGetRequest(location);
                }

                return BankIdStatus.DONE;
            }

            log.info(ProductExecutionLogger
                    .newBuilder()
                    .withMessage(String.format("BankID: Received unhandled HTTP Status = %s", signResponse.getStatus())));
            return BankIdStatus.FAILED_UNKNOWN;
        default:
            return pollResponse.getBankIdStatus();
        }
    }

}
