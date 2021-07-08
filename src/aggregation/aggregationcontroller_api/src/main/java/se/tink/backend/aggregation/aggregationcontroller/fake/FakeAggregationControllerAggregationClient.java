package se.tink.backend.aggregation.aggregationcontroller.fake;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.module.SimpleModule;
import com.google.inject.Inject;
import java.io.BufferedWriter;
import java.io.IOException;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.net.HttpURLConnection;
import java.net.InetSocketAddress;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.Map;
import javax.ws.rs.core.Response;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import se.tink.backend.agents.rpc.Account;
import se.tink.backend.agents.rpc.Credentials;
import se.tink.backend.aggregation.aggregationcontroller.iface.AggregationControllerAggregationClient;
import se.tink.backend.aggregation.aggregationcontroller.v1.core.HostConfiguration;
import se.tink.backend.aggregation.aggregationcontroller.v1.rpc.CoreRegulatoryClassification;
import se.tink.backend.aggregation.aggregationcontroller.v1.rpc.GenerateStatisticsAndActivitiesRequest;
import se.tink.backend.aggregation.aggregationcontroller.v1.rpc.OptOutAccountsRequest;
import se.tink.backend.aggregation.aggregationcontroller.v1.rpc.ProcessAccountsRequest;
import se.tink.backend.aggregation.aggregationcontroller.v1.rpc.RestrictAccountsRequest;
import se.tink.backend.aggregation.aggregationcontroller.v1.rpc.UpdateAccountRequest;
import se.tink.backend.aggregation.aggregationcontroller.v1.rpc.UpdateCredentialsStatusRequest;
import se.tink.backend.aggregation.aggregationcontroller.v1.rpc.UpdateIdentityDataRequest;
import se.tink.backend.aggregation.aggregationcontroller.v1.rpc.UpdateTransactionsRequest;
import se.tink.backend.aggregation.aggregationcontroller.v1.rpc.UpdateTransferDestinationPatternsRequest;
import se.tink.backend.aggregation.aggregationcontroller.v1.rpc.UpdateTransfersRequest;
import se.tink.backend.aggregation.aggregationcontroller.v1.rpc.UpsertRegulatoryClassificationRequest;
import se.tink.backend.aggregationcontroller.v1.rpc.credentialsservice.UpdateCredentialsSupplementalInformationRequest;
import se.tink.backend.fake_aggregation_controller.dto.SetStateDto;
import se.tink.libraries.amount.ExactCurrencyAmount;
import se.tink.libraries.signableoperation.rpc.SignableOperation;

public class FakeAggregationControllerAggregationClient
        implements AggregationControllerAggregationClient {

    private final Logger log =
            LoggerFactory.getLogger(FakeAggregationControllerAggregationClient.class);

    private final InetSocketAddress socket;
    private final ObjectMapper mapper;

    @Inject
    private FakeAggregationControllerAggregationClient(
            @FakeAggregationControllerSocket final InetSocketAddress socket) {
        this.socket = socket;

        log.info("Aggregation controller client will use: {}", socket);

        mapper = new ObjectMapper();
        SimpleModule module = new SimpleModule();
        module.addDeserializer(ExactCurrencyAmount.class, new ExactCurrencyAmountDeserializer());
        mapper.registerModule(module);
    }

    @Override
    public Response generateStatisticsAndActivityAsynchronously(
            HostConfiguration hostConfiguration, GenerateStatisticsAndActivitiesRequest request) {
        throw new UnsupportedOperationException("Not implemented");
    }

    @Override
    public Response updateTransactionsAsynchronously(
            HostConfiguration hostConfiguration, UpdateTransactionsRequest request) {
        callFakeAggregationControllerForSendingData("updateTransactionsAsynchronously", request);
        return Response.ok().build();
    }

    @Override
    public String ping(HostConfiguration hostConfiguration) {
        throw new UnsupportedOperationException("Not implemented");
    }

    @Override
    public Account updateAccount(
            HostConfiguration hostConfiguration, UpdateAccountRequest request) {
        callFakeAggregationControllerForSendingData("updateAccount", request);
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
        callFakeAggregationControllerForSendingData("processAccounts", request);
        return Response.ok().build();
    }

    @Override
    public Response optOutAccounts(
            HostConfiguration hostConfiguration, OptOutAccountsRequest request) {
        throw new UnsupportedOperationException("Not implemented");
    }

    @Override
    public Response restrictAccounts(
            HostConfiguration hostConfiguration, RestrictAccountsRequest request) {
        throw new UnsupportedOperationException("Not implemented");
    }

    @Override
    public Response updateCredentials(
            HostConfiguration hostConfiguration, UpdateCredentialsStatusRequest request) {
        callFakeAggregationControllerForSendingData("updateCredentials", request);
        return Response.ok().build();
    }

    @Override
    public Response updateSignableOperation(
            HostConfiguration hostConfiguration, SignableOperation signableOperation) {
        callFakeAggregationControllerForSendingData("updateSignableOperation", signableOperation);
        return Response.ok().build();
    }

    @Override
    public Response processEinvoices(
            HostConfiguration hostConfiguration, UpdateTransfersRequest request) {
        callFakeAggregationControllerForSendingData("processEinvoices", request);
        return Response.ok().build();
    }

    @Override
    public Response updateCredentialSensitive(
            HostConfiguration hostConfiguration,
            Credentials credentials,
            String sensitiveData,
            String operationId) {
        callFakeAggregationControllerForSendingData("updateCredentialSensitive", credentials);
        callFakeAggregationControllerForSendingData(
                "updateCredentialSensitiveString", sensitiveData);
        return Response.ok().build();
    }

    @Override
    public Response updateCredentialSupplementalInformation(
            HostConfiguration hostConfiguration,
            UpdateCredentialsSupplementalInformationRequest request) {
        callFakeAggregationControllerForSendingData(
                "updateCredentialSupplementalInformation", request);
        return Response.ok().build();
    }

    @Override
    public Response checkConnectivity(HostConfiguration hostConfiguration) {
        throw new UnsupportedOperationException("Not implemented");
    }

    @Override
    public Response updateIdentity(
            HostConfiguration hostConfiguration, UpdateIdentityDataRequest request) {
        callFakeAggregationControllerForSendingData("updateIdentity", request);
        return Response.ok().build();
    }

    @Override
    public CoreRegulatoryClassification upsertRegulatoryClassification(
            HostConfiguration hostConfiguration, UpsertRegulatoryClassificationRequest request) {
        callFakeAggregationControllerForSendingData("upsertRegulatoryClassification", request);
        return request.getClassification();
    }

    public void callFakeAggregationControllerForSendingFakeBankServerState(
            String credentialsId, String state) {
        try {
            String serializedRequestBody =
                    new ObjectMapper().writeValueAsString(new SetStateDto(credentialsId, state));
            callFakeAggregationController("bank_state", serializedRequestBody);
        } catch (JsonProcessingException e) {
            throw new IllegalStateException(e);
        }
    }

    private void callFakeAggregationControllerForSendingData(String caller, Object data) {
        try {
            Map<String, Object> innerRequestBody = new HashMap<>();
            innerRequestBody.put(caller, new ObjectMapper().writeValueAsString(data));
            String serializedRequestBody = new ObjectMapper().writeValueAsString(innerRequestBody);
            callFakeAggregationController("data", serializedRequestBody);
        } catch (JsonProcessingException e) {
            throw new IllegalStateException(e);
        }
    }

    private void callFakeAggregationController(String endpoint, String serialisedRequestBody) {
        try {
            URL serverAddress =
                    new URL(
                            String.format(
                                    "http://%s:%d/%s",
                                    socket.getHostString(), socket.getPort(), endpoint));

            HttpURLConnection connection = (HttpURLConnection) serverAddress.openConnection();
            connection.setDoOutput(true);
            connection.setDoInput(true);
            connection.setRequestProperty("Content-Type", "application/json");
            connection.setRequestMethod("POST");

            OutputStream os = connection.getOutputStream();
            BufferedWriter writer =
                    new BufferedWriter(new OutputStreamWriter(os, StandardCharsets.UTF_8));
            writer.write(serialisedRequestBody);

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
        } catch (IOException e) {
            throw new RuntimeException("Could not connect to Fake Aggregation Controller", e);
        }
    }
}
