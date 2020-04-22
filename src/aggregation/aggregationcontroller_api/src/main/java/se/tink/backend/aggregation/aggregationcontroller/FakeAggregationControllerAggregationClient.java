package se.tink.backend.aggregation.aggregationcontroller;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.module.SimpleModule;
import com.google.inject.Inject;
import java.io.BufferedWriter;
import java.io.IOException;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.ProtocolException;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.Map;
import javax.ws.rs.core.Response;
import se.tink.backend.agents.rpc.Account;
import se.tink.backend.agents.rpc.Credentials;
import se.tink.backend.aggregation.aggregationcontroller.v1.core.HostConfiguration;
import se.tink.backend.aggregation.aggregationcontroller.v1.rpc.GenerateStatisticsAndActivitiesRequest;
import se.tink.backend.aggregation.aggregationcontroller.v1.rpc.OptOutAccountsRequest;
import se.tink.backend.aggregation.aggregationcontroller.v1.rpc.ProcessAccountsRequest;
import se.tink.backend.aggregation.aggregationcontroller.v1.rpc.SupplementalInformationRequest;
import se.tink.backend.aggregation.aggregationcontroller.v1.rpc.SupplementalInformationResponse;
import se.tink.backend.aggregation.aggregationcontroller.v1.rpc.UpdateAccountRequest;
import se.tink.backend.aggregation.aggregationcontroller.v1.rpc.UpdateCredentialsStatusRequest;
import se.tink.backend.aggregation.aggregationcontroller.v1.rpc.UpdateIdentityDataRequest;
import se.tink.backend.aggregation.aggregationcontroller.v1.rpc.UpdateTransactionsRequest;
import se.tink.backend.aggregation.aggregationcontroller.v1.rpc.UpdateTransferDestinationPatternsRequest;
import se.tink.backend.aggregation.aggregationcontroller.v1.rpc.UpdateTransfersRequest;
import se.tink.backend.system.rpc.UpdateFraudDetailsRequest;
import se.tink.libraries.amount.ExactCurrencyAmount;
import se.tink.libraries.signableoperation.rpc.SignableOperation;

public class FakeAggregationControllerAggregationClient
        implements AggregationControllerAggregationClient {

    // TODO Make this configurable. Should be localhost if run locally.
    // https://tinkab.atlassian.net/jira/software/projects/AAP/boards/136?selectedIssue=AAP-279
    private static final String AGGREGATION_CONTROLLER_NAME = "fake_aggregation_controller";
    private static final int AGGREGATION_CONTROLLER_PORT = 8080;

    private static final ObjectMapper mapper = new ObjectMapper();

    static {
        SimpleModule module = new SimpleModule();
        module.addDeserializer(ExactCurrencyAmount.class, new ExactCurrencyAmountDeserializer());
        mapper.registerModule(module);
    }

    @Inject
    private FakeAggregationControllerAggregationClient() {}

    @Override
    public Response generateStatisticsAndActivityAsynchronously(
            HostConfiguration hostConfiguration, GenerateStatisticsAndActivitiesRequest request) {
        throw new UnsupportedOperationException("Not implemented");
    }

    @Override
    public Response updateTransactionsAsynchronously(
            HostConfiguration hostConfiguration, UpdateTransactionsRequest request) {
        callFakeAggregationController("updateTransactionsAsynchronously", request);
        return null;
    }

    @Override
    public String ping(HostConfiguration hostConfiguration) {
        throw new UnsupportedOperationException("Not implemented");
    }

    @Override
    public SupplementalInformationResponse getSupplementalInformation(
            HostConfiguration hostConfiguration, SupplementalInformationRequest request) {
        throw new UnsupportedOperationException("Not implemented");
    }

    @Override
    public Account updateAccount(
            HostConfiguration hostConfiguration, UpdateAccountRequest request) {
        callFakeAggregationController("updateAccount", request);
        try {
            return mapper.readValue(mapper.writeValueAsString(request.getAccount()), Account.class);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public Account updateAccountMetaData(
            HostConfiguration hostConfiguration, String accountId, String newBankId) {
        throw new UnsupportedOperationException("Not implemented");
    }

    @Override
    public Response updateTransferDestinationPatterns(
            HostConfiguration hostConfiguration, UpdateTransferDestinationPatternsRequest request) {
        throw new UnsupportedOperationException("Not implemented");
    }

    @Override
    public Response processAccounts(
            HostConfiguration hostConfiguration, ProcessAccountsRequest request) {
        callFakeAggregationController("processAccounts", request);
        return null;
    }

    @Override
    public Response optOutAccounts(
            HostConfiguration hostConfiguration, OptOutAccountsRequest request) {
        throw new UnsupportedOperationException("Not implemented");
    }

    private void callFakeAggregationController(String caller, Object data) {
        try {
            URL serverAddress =
                    new URL(
                            String.format(
                                    "http://%s:%d/data",
                                    AGGREGATION_CONTROLLER_NAME, AGGREGATION_CONTROLLER_PORT));
            HttpURLConnection connection = (HttpURLConnection) serverAddress.openConnection();
            connection.setDoOutput(true);
            connection.setDoInput(true);
            connection.setRequestProperty("Content-Type", "application/json");
            connection.setRequestMethod("POST");

            OutputStream os = connection.getOutputStream();
            BufferedWriter writer =
                    new BufferedWriter(new OutputStreamWriter(os, StandardCharsets.UTF_8));

            Map<String, Object> requestBody = new HashMap<>();
            String serializedRequestBody = new ObjectMapper().writeValueAsString(data);
            requestBody.put(caller, serializedRequestBody);

            writer.write(new ObjectMapper().writeValueAsString(requestBody));
            writer.flush();
            writer.close();
            os.close();
            connection.connect();

            int status =
                    connection.getResponseCode(); // this cannot be invoked before data stream is
            // ready when performing HTTP POST
            if (status != 200) {
                throw new RuntimeException(
                        "Invalid HTTP response status "
                                + "code "
                                + status
                                + " from web service server.");
            }
        } catch (MalformedURLException | ProtocolException | JsonProcessingException e) {
            throw new IllegalStateException("Could not connect to Fake Aggregation Controller", e);
        } catch (IOException e) {
            throw new RuntimeException("Could not connect to Fake Aggregation Controller", e);
        }
    }

    @Override
    public Response updateCredentials(
            HostConfiguration hostConfiguration, UpdateCredentialsStatusRequest request) {
        callFakeAggregationController("updateCredentials", request);
        return null;
    }

    @Override
    public Response updateSignableOperation(
            HostConfiguration hostConfiguration, SignableOperation signableOperation) {
        throw new UnsupportedOperationException("Not implemented");
    }

    @Override
    public Response processEinvoices(
            HostConfiguration hostConfiguration, UpdateTransfersRequest request) {
        throw new UnsupportedOperationException("Not implemented");
    }

    @Override
    public Response updateFraudDetails(
            HostConfiguration hostConfiguration, UpdateFraudDetailsRequest request) {
        throw new UnsupportedOperationException("Not implemented");
    }

    @Override
    public Response updateCredentialSensitive(
            HostConfiguration hostConfiguration, Credentials credentials, String sensitiveData) {
        callFakeAggregationController("updateCredentialSensitive", credentials);
        callFakeAggregationController("updateCredentialSensitiveString", sensitiveData);
        return null;
    }

    @Override
    public Response checkConnectivity(HostConfiguration hostConfiguration) {
        throw new UnsupportedOperationException("Not implemented");
    }

    @Override
    public Response updateIdentity(
            HostConfiguration hostConfiguration, UpdateIdentityDataRequest request) {
        callFakeAggregationController("updateIdentity", request);
        return null;
    }
}
