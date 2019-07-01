package se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.handelsbanken.liveenrolement;

import java.util.UUID;
import javax.ws.rs.core.MediaType;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.handelsbanken.HandelsbankenBaseConstants;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.handelsbanken.HandelsbankenBaseConstants.BodyKeys;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.handelsbanken.HandelsbankenBaseConstants.BodyValues;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.handelsbanken.configuration.HandelsbankenBaseConfiguration;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.handelsbanken.liveenrolement.entity.App;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.handelsbanken.liveenrolement.rpc.*;
import se.tink.backend.aggregation.nxgen.http.Form;
import se.tink.backend.aggregation.nxgen.http.TinkHttpClient;
import se.tink.backend.aggregation.nxgen.http.URL;

public class HandelsbankenBaseLiveEnrolement {

    private final TinkHttpClient client;
    private HandelsbankenBaseConfiguration configuration;

    public void setConfiguration(HandelsbankenBaseConfiguration configuration) {
        this.configuration = configuration;
    }

    public HandelsbankenBaseLiveEnrolement(TinkHttpClient client) {
        this.client = client;
    }

    public ThirdPartiesResponse getThirdPartiesId() {
        return client.request(new URL(HandelsbankenBaseConstants.Urls.THIRD_PARTIES))
                .header(
                        HandelsbankenBaseConstants.HeaderKeys.TPP_TRANSACTION_ID,
                        UUID.randomUUID().toString())
                .header(
                        HandelsbankenBaseConstants.HeaderKeys.TPP_REQUEST_ID,
                        UUID.randomUUID().toString())
                .accept(MediaType.APPLICATION_JSON_TYPE)
                .type(MediaType.APPLICATION_JSON)
                .post(ThirdPartiesResponse.class);
    }

    public TokenResponse getBearerToken(String clientId) {

        final Form params =
                Form.builder()
                        .put(BodyKeys.GRANT_TYPE, BodyValues.CLIENT_CREDENTIALS)
                        .put(BodyKeys.SCOPE, BodyValues.PSD2_ADMIN)
                        .put(BodyKeys.CLIENT_ID, clientId)
                        .build();

        return client.request(new URL(HandelsbankenBaseConstants.Urls.TOKEN))
                .body(params.toString())
                .accept(MediaType.APPLICATION_JSON)
                .type(MediaType.APPLICATION_FORM_URLENCODED_TYPE)
                .post(TokenResponse.class);
    }

    public SubscriptionRequest createSubscriptionBody(String oauthRedirectURI) {
        Product product = new Product(BodyValues.PRODUCT_ACCOUNTS);
        App app = new App(configuration.getAppName(), configuration.getAppDesc(), oauthRedirectURI);
        SubscriptionRequest subscriptionRequest = new SubscriptionRequest(app, product);
        return subscriptionRequest;
    }

    public AdditionalRequest getAdditionalRequest(String clientId, String subscription) {
        Product product = new Product(subscription);
        return new AdditionalRequest(clientId, product);
    }

    public SubscriptionResponse getAdditionalSubscription(
            String clientId, String appId, String code, String subscription) {
        return client.request(new URL(HandelsbankenBaseConstants.Urls.SUBSCRIPTIONS))
                .header(HandelsbankenBaseConstants.HeaderKeys.AUTHORIZATION, code)
                .header(
                        HandelsbankenBaseConstants.HeaderKeys.TPP_REQUEST_ID,
                        UUID.randomUUID().toString())
                .header(
                        HandelsbankenBaseConstants.HeaderKeys.TPP_TRANSACTION_ID,
                        UUID.randomUUID().toString())
                .header(HandelsbankenBaseConstants.HeaderKeys.X_IBM_CLIENT_ID, clientId)
                .body(getAdditionalRequest(appId, subscription))
                .accept(MediaType.APPLICATION_JSON_TYPE)
                .type(MediaType.APPLICATION_JSON)
                .post(SubscriptionResponse.class);
    }

    public SubscriptionResponse getSubscription(
            String clientId, String code, String oauthRedirectURI) {

        return client.request(new URL(HandelsbankenBaseConstants.Urls.SUBSCRIPTIONS))
                .header(HandelsbankenBaseConstants.HeaderKeys.AUTHORIZATION, code)
                .header(
                        HandelsbankenBaseConstants.HeaderKeys.TPP_REQUEST_ID,
                        UUID.randomUUID().toString())
                .header(
                        HandelsbankenBaseConstants.HeaderKeys.TPP_TRANSACTION_ID,
                        UUID.randomUUID().toString())
                .header(HandelsbankenBaseConstants.HeaderKeys.X_IBM_CLIENT_ID, clientId)
                .body(createSubscriptionBody(oauthRedirectURI))
                .accept(MediaType.APPLICATION_JSON_TYPE)
                .type(MediaType.APPLICATION_JSON)
                .post(SubscriptionResponse.class);
    }

    public void enroleAndSubscribeNewApp() {
        ThirdPartiesResponse thirdPartiesResponse = getThirdPartiesId();
        TokenResponse tokenResponse = getBearerToken(configuration.getClientId());
        SubscriptionResponse subscriptionResponse =
                getSubscription(
                        thirdPartiesResponse.getClientId(),
                        tokenResponse.getAccessToken(),
                        configuration.getRedirectUrl());
        SubscriptionResponse consents =
                getAdditionalSubscription(
                        thirdPartiesResponse.getClientId(),
                        subscriptionResponse.getClientId(),
                        tokenResponse.getAccessToken(),
                        HandelsbankenBaseConstants.BodyValues.SUBSCRIPTION_CONSENTS);
    }
}
