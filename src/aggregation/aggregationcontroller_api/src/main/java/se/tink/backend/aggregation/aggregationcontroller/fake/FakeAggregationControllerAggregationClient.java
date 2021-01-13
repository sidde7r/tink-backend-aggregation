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
import java.net.MalformedURLException;
import java.net.ProtocolException;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.Map;
import javax.ws.rs.core.Response;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import se.tink.backend.agents.rpc.Account;
import se.tink.backend.agents.rpc.AccountHolder;
import se.tink.backend.agents.rpc.Credentials;
import se.tink.backend.aggregation.aggregationcontroller.iface.AggregationControllerAggregationClient;
import se.tink.backend.aggregation.aggregationcontroller.v1.core.HostConfiguration;
import se.tink.backend.aggregation.aggregationcontroller.v1.rpc.CoreRegulatoryClassification;
import se.tink.backend.aggregation.aggregationcontroller.v1.rpc.GenerateStatisticsAndActivitiesRequest;
import se.tink.backend.aggregation.aggregationcontroller.v1.rpc.OptOutAccountsRequest;
import se.tink.backend.aggregation.aggregationcontroller.v1.rpc.ProcessAccountsRequest;
import se.tink.backend.aggregation.aggregationcontroller.v1.rpc.RestrictAccountsRequest;
import se.tink.backend.aggregation.aggregationcontroller.v1.rpc.UpdateAccountHolderRequest;
import se.tink.backend.aggregation.aggregationcontroller.v1.rpc.UpdateAccountRequest;
import se.tink.backend.aggregation.aggregationcontroller.v1.rpc.UpdateCredentialsStatusRequest;
import se.tink.backend.aggregation.aggregationcontroller.v1.rpc.UpdateIdentityDataRequest;
import se.tink.backend.aggregation.aggregationcontroller.v1.rpc.UpdateTransactionsRequest;
import se.tink.backend.aggregation.aggregationcontroller.v1.rpc.UpdateTransferDestinationPatternsRequest;
import se.tink.backend.aggregation.aggregationcontroller.v1.rpc.UpdateTransfersRequest;
import se.tink.backend.aggregation.aggregationcontroller.v1.rpc.UpsertRegulatoryClassificationRequest;
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
        callFakeAggregationController("updateTransactionsAsynchronously", request);
        return Response.ok().build();
    }

    @Override
    public String ping(HostConfiguration hostConfiguration) {
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

    private void callFakeAggregationController(String caller, Object data) {
        try {
            URL serverAddress =
                    new URL(
                            String.format(
                                    "http://%s:%d/data", socket.getHostString(), socket.getPort()));
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
        return Response.ok().build();
    }

    @Override
    public Response updateSignableOperation(
            HostConfiguration hostConfiguration, SignableOperation signableOperation) {
        callFakeAggregationController("updateSignableOperation", signableOperation);
        return Response.ok().build();
    }

    @Override
    public Response processEinvoices(
            HostConfiguration hostConfiguration, UpdateTransfersRequest request) {
        callFakeAggregationController("processEinvoices", request);
        return Response.ok().build();
    }

    @Override
    public Response updateCredentialSensitive(
            HostConfiguration hostConfiguration,
            Credentials credentials,
            String sensitiveData,
            String operationId) {
        callFakeAggregationController("updateCredentialSensitive", credentials);
        callFakeAggregationController("updateCredentialSensitiveString", sensitiveData);
        return Response.ok().build();
    }

    @Override
    public Response checkConnectivity(HostConfiguration hostConfiguration) {
        throw new UnsupportedOperationException("Not implemented");
    }

    @Override
    public Response updateIdentity(
            HostConfiguration hostConfiguration, UpdateIdentityDataRequest request) {
        callFakeAggregationController("updateIdentity", request);
        return Response.ok().build();
    }

    @Override
    public AccountHolder updateAccountHolder(
            HostConfiguration hostConfiguration, UpdateAccountHolderRequest request) {
        callFakeAggregationController("updateAccountHolder", request);
        return request.getAccountHolder();
    }

    @Override
    public CoreRegulatoryClassification upsertRegulatoryClassification(
            HostConfiguration hostConfiguration, UpsertRegulatoryClassificationRequest request) {
        callFakeAggregationController("upsertRegulatoryClassification", request);
        return request.getClassification();
    }
}
