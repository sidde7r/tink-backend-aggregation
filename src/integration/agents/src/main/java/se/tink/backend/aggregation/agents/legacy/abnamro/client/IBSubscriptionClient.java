package se.tink.backend.aggregation.agents.abnamro.client;

import com.google.api.client.http.HttpStatusCodes;
import com.google.common.base.Preconditions;
import com.google.common.base.Strings;
import com.google.common.collect.Lists;
import com.sun.jersey.api.client.ClientHandlerException;
import com.sun.jersey.api.client.ClientResponse;
import com.sun.jersey.api.client.WebResource;
import java.io.OutputStream;
import java.util.List;
import java.util.Objects;
import javax.ws.rs.core.HttpHeaders;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.UriBuilder;
import se.tink.backend.aggregation.agents.abnamro.client.exceptions.IcsException;
import se.tink.backend.aggregation.agents.abnamro.client.exceptions.IcsRetryableException;
import se.tink.backend.aggregation.agents.abnamro.client.exceptions.SubscriptionException;
import se.tink.backend.aggregation.agents.abnamro.client.model.ErrorEntity;
import se.tink.backend.aggregation.agents.abnamro.client.model.PfmContractEntity;
import se.tink.backend.aggregation.agents.abnamro.client.model.creditcards.CreditCardAccountContainerEntity;
import se.tink.backend.aggregation.agents.abnamro.client.model.creditcards.CreditCardAccountResponse;
import se.tink.backend.aggregation.agents.abnamro.client.rpc.ErrorResponse;
import se.tink.backend.aggregation.agents.abnamro.client.rpc.PfmContractResponse;
import se.tink.backend.aggregation.agents.exceptions.LoginException;
import se.tink.backend.aggregation.agents.utils.jersey.JerseyClientFactory;
import se.tink.backend.aggregation.configuration.integrations.abnamro.AbnAmroConfiguration;
import se.tink.backend.aggregation.configuration.integrations.abnamro.AbnAmroInternetBankingConfiguration;
import se.tink.libraries.metrics.core.MetricId;
import se.tink.libraries.metrics.registry.MetricRegistry;

/** Service client for handling with grip subscriptions. */
public class IBSubscriptionClient extends IBClient {

    private AbnAmroInternetBankingConfiguration configuration;

    public IBSubscriptionClient(
            JerseyClientFactory clientFactory,
            OutputStream logOutputStream,
            AbnAmroConfiguration abnAmroConfiguration,
            MetricRegistry metricRegistry) {
        super(
                IBSubscriptionClient.class,
                clientFactory,
                logOutputStream,
                abnAmroConfiguration,
                metricRegistry);

        this.configuration = abnAmroConfiguration.getInternetBankingConfiguration();

        // Remove maybe
        metricRegistry.meter(MetricId.newId("ib_subscription_client_timers_rejected_contracts"));
    }

    public List<PfmContractEntity> getContracts(String bcNumber)
            throws SubscriptionException, LoginException {
        Preconditions.checkArgument(
                !Strings.isNullOrEmpty(bcNumber), "BcNumber must not be null or empty");

        String url = String.format("/pfmsubscription/%s/contracts", bcNumber);

        ClientResponse response = new IBClientRequestBuilder(url).build().get(ClientResponse.class);

        if (response.getStatus() == 403) {
            log.info("List<PfmContractEntity> getContracts: 403 error");
            try {
                throw new IllegalStateException(response.getEntity(String.class));
            } catch (Exception e) {
                throw new IllegalStateException(e.getMessage());
            }
        }

        validateContentType(response, MediaType.APPLICATION_JSON_TYPE);

        try {
            return Lists.newArrayList(response.getEntity(PfmContractResponse.class));
        } catch (ClientHandlerException e) {
            log.error(
                    "Could not deserialize PfmContractResponse {}",
                    response.getEntity(String.class));
            throw e;
        }
    }

    /**
     * This will return credit card information and credit card transactions for the specified user
     * and contract number.
     */
    public List<CreditCardAccountContainerEntity> getCreditCardAccountAndTransactions(
            String bcNumber, Long contract) throws IcsException {
        String url =
                UriBuilder.fromPath("/creditcardaccounts/pfm")
                        .queryParam("bcNumber", bcNumber)
                        .queryParam("contractNumbers", contract)
                        .build()
                        .toString();

        ClientResponse clientResponse =
                new IBClientRequestBuilder(url).build().get(ClientResponse.class);

        try {
            if (clientResponse.getStatus() == HttpStatusCodes.STATUS_CODE_OK) {
                CreditCardAccountResponse response =
                        clientResponse.getEntity(CreditCardAccountResponse.class);
                return response.getCreditCardAccountList().getCreditCardAccounts();
            }

            if (Objects.equals(clientResponse.getType(), MediaType.APPLICATION_JSON_TYPE)) {
                ErrorResponse response = clientResponse.getEntity(ErrorResponse.class);

                if (!response.getMessages().isEmpty()) {
                    ErrorEntity error = response.getMessages().get(0);

                    // The VPN connection between ABN AMRO and ICS is unstable so throw retryable
                    // exception if possible
                    if (error.isRetryable()) {
                        log.warn(
                                String.format(
                                        "Retryable error from ABN AMRO ICS (Key = '%s', Reason = '%s')",
                                        error.getMessageKey(), error.getReason()));
                        throw new IcsRetryableException(error.getMessageKey(), error.getReason());
                    }

                    throw new IcsException(error.getMessageKey(), error.getReason());
                }
            }

            throw new IcsException(
                    String.format(
                            "Could not fetch transactions. HTTP status: %d",
                            clientResponse.getStatus()));
        } finally {
            clientResponse.close();
        }
    }

    /** Have asked ABN AMRO to use "Token" instead of "Basic" /Erik */
    @Override
    protected WebResource.Builder createClientRequest(String path) {
        return super.createClientRequest(path)
                .header(
                        HttpHeaders.AUTHORIZATION,
                        String.format("Basic %s", configuration.getAuthorizationToken()));
    }

    private void validateContentType(ClientResponse response, MediaType expected)
            throws SubscriptionException {
        if (Objects.equals(response.getType(), expected)) {
            return;
        }

        log.error(
                String.format(
                        "Unexpected content type (Status = '%d', Expected = '%s', Received = '%s')",
                        response.getStatus(), expected, response.getType()));

        throw new SubscriptionException("Unexpected content type on response.");
    }
}
