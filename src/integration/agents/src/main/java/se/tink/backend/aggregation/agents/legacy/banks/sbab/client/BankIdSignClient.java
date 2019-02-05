package se.tink.backend.aggregation.agents.banks.sbab.client;

import com.google.common.base.Strings;
import com.sun.jersey.api.client.Client;
import com.sun.jersey.api.client.ClientResponse;
import com.sun.jersey.api.client.WebResource;
import org.eclipse.jetty.http.HttpStatus;
import se.tink.backend.aggregation.agents.BankIdStatus;
import se.tink.backend.aggregation.agents.banks.sbab.exception.UnsupportedSignTypeException;
import se.tink.backend.aggregation.agents.banks.sbab.model.response.BankIdPollResponse;
import se.tink.backend.aggregation.agents.banks.sbab.model.response.BankIdStartResponse;
import se.tink.backend.aggregation.agents.banks.sbab.model.response.SignFormRequestBody;
import se.tink.backend.aggregation.log.AggregationLogger;
import se.tink.backend.agents.rpc.Credentials;

public class BankIdSignClient extends SBABClient {

    private static final AggregationLogger log = new AggregationLogger(BankIdSignClient.class);

    private static final String START_PATH = "/nx/%s/start";
    private static final String POLL_PATH = "/nx/%s/poll";

    private String signPath;

    public BankIdSignClient(Client client, Credentials credentials, String userAgent) {
        super(client, credentials, userAgent);
    }

    private void setSignMode(SignFormRequestBody signFormRequestBody) throws UnsupportedSignTypeException {
        signPath = signFormRequestBody.getSignUri();
    }

    public BankIdStartResponse initiateSign(SignFormRequestBody signFormRequestBody) throws Exception {
        setSignMode(signFormRequestBody);

        signFormRequestBody.add("nx_serial_number", signFormRequestBody.getFirst("nx_userid"));

        return createFormEncodedJsonRequest(getSignUrl(START_PATH, signFormRequestBody.getClient()))
                .post(BankIdStartResponse.class, signFormRequestBody);
    }

    public BankIdStatus getStatus(SignFormRequestBody signFormRequestBody, String orderReference) throws Exception {
        if (!signFormRequestBody.containsKey("nx_orderref")) {
            signFormRequestBody.add("nx_orderref", orderReference);
        }

        BankIdPollResponse pollResponse = createFormEncodedHtmlRequest(
                getSignUrl(POLL_PATH, signFormRequestBody.getClient()))
                .post(BankIdPollResponse.class, signFormRequestBody);

        log.info("BankID: Awaiting sign. Status = " + pollResponse.getStatus());

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

                log.info("BankID: Redirect HTTP Status = " + signResponse.getStatus());

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

                log.info("BankID: Received unhandled HTTP Status = " + signResponse.getStatus());
                return BankIdStatus.FAILED_UNKNOWN;
            default:
                return pollResponse.getBankIdStatus();
        }
    }
}
