package se.tink.backend.aggregation.agents.creditcards.americanexpress.v3;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.api.client.repackaged.com.google.common.base.Strings;
import com.google.common.collect.ImmutableMap;
import com.google.common.collect.Maps;
import com.sun.jersey.api.client.Client;
import com.sun.jersey.api.client.WebResource;
import java.io.IOException;
import java.util.Collections;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import javax.ws.rs.core.MediaType;
import se.tink.backend.agents.rpc.Credentials;
import se.tink.backend.agents.rpc.Field;
import se.tink.backend.aggregation.agents.creditcards.americanexpress.v3.model.CardDetailsEntity;
import se.tink.backend.aggregation.agents.creditcards.americanexpress.v3.model.CardEntity;
import se.tink.backend.aggregation.agents.creditcards.americanexpress.v3.model.LoginRequest;
import se.tink.backend.aggregation.agents.creditcards.americanexpress.v3.model.LoginResponse;
import se.tink.backend.aggregation.agents.creditcards.americanexpress.v3.model.TimelineEntity;
import se.tink.backend.aggregation.agents.creditcards.americanexpress.v3.model.TimelineRequest;
import se.tink.backend.aggregation.agents.creditcards.americanexpress.v3.model.TimelineResponse;
import se.tink.backend.aggregation.agents.creditcards.americanexpress.v3.model.TransactionDetailsEntity;
import se.tink.backend.aggregation.agents.creditcards.americanexpress.v3.model.TransactionsRequest;
import se.tink.backend.aggregation.agents.creditcards.americanexpress.v3.model.TransactionsResponse;
import se.tink.backend.aggregation.agents.creditcards.americanexpress.v3.utils.MarketParameters;
import se.tink.libraries.date.ThreadSafeDateFormat;
import se.tink.libraries.strings.StringUtils;

public class AmericanExpressV3ApiClient {
    private final Client client;
    private final MarketParameters marketParameters;
    private final String defaultUserAgent;
    private final Credentials credentials;
    private String cupcake;
    private List<CardDetailsEntity> cardList;

    private static final ObjectMapper MAPPER = new ObjectMapper();
    private static final String SWEDISH_LOGIN_URL =
            "https://global.americanexpress.com/myca/intl/moblclient/emea/services/accountservicing/v1/loginSummary?Face=sv_SE";
    private static final String SWEDISH_TIMELINE_URL =
            "https://global.americanexpress.com/myca/intl/moblclient/emea/services/timeline/v1/timelineDetail?Face=sv_SE";
    private static final ThreadSafeDateFormat LOCAL_TIME_FORMAT =
            new ThreadSafeDateFormat("MM-dd-YYYY'T'HH:mm:ss"); // 09-06-2015T14:44:16"

    private static final ImmutableMap<String, MarketParameters> MARKET_PARAMETERS =
            new ImmutableMap.Builder<String, MarketParameters>()
                    .put(
                            "SG",
                            new MarketParameters(
                                    "en_SG",
                                    "sg.co.americanexpress.amexservice",
                                    "https://global.americanexpress.com/myca/intl/moblclient/japa/svc",
                                    "3.2.3",
                                    false))
                    .put(
                            "GB",
                            new MarketParameters(
                                    "en_GB",
                                    "uk.co.americanexpress.amexservice",
                                    "https://global.americanexpress.com/myca/intl/moblclient/emea/svc",
                                    "3.4.3",
                                    false))
                    .put(
                            "SE",
                            new MarketParameters(
                                    "sv_SE",
                                    "com.americanexpress.android.acctsvcs.se",
                                    "https://global.americanexpress.com/myca/intl/moblclient/emea/svc",
                                    "4.4.1",
                                    true))
                    .build();

    public AmericanExpressV3ApiClient(
            Client client, String market, String defaultUserAgent, Credentials credentials) {
        this.client = client;
        this.defaultUserAgent = defaultUserAgent;
        this.credentials = credentials;
        this.marketParameters = MARKET_PARAMETERS.get(market);
    }

    private WebResource.Builder createRequest(String url) {
        WebResource.Builder request =
                client.resource(url)
                        .header("User-Agent", defaultUserAgent)
                        .accept(MediaType.APPLICATION_JSON)
                        .header("appID", marketParameters.getAppId())
                        .header("oSversion", "4.2.2")
                        .header("Face", marketParameters.getLocale())
                        .header("clientType", "Android")
                        .header("deviceId", StringUtils.hashAsUUID(credentials.getId()))
                        .header("charset", "UTF-8")
                        .header("clientVersion", marketParameters.getClientVersion())
                        .type(MediaType.APPLICATION_JSON);

        if (!Strings.isNullOrEmpty(cupcake)) {
            request = request.header("cupcake", cupcake);
        }

        return request;
    }

    TimelineEntity getTimeLine(CardDetailsEntity cardEntity) throws Exception {
        TimelineRequest timelineRequest = new TimelineRequest();
        timelineRequest.setTimeZone("CEST");
        timelineRequest.setTimeZoneOffset("7200000");
        timelineRequest.setLocalTime(LOCAL_TIME_FORMAT.format(new Date()));
        timelineRequest.setSortedIndex(cardEntity.getSortedIndex());
        timelineRequest.setPendingChargeEnabled(true);
        timelineRequest.setCmlEnabled(true);

        return MAPPER.readValue(
                        createRequest(
                                        Objects.equals("sv_SE", marketParameters.getLocale())
                                                ? SWEDISH_TIMELINE_URL
                                                : marketParameters.getBaseUrl())
                                .post(String.class, timelineRequest),
                        TimelineResponse.class)
                .getTimeline();
    }

    public LoginResponse login() {
        String profileInfo = credentials.getSensitivePayload("profileInfo");

        LoginRequest loginRequest = new LoginRequest();
        loginRequest.setProfileInfo(profileInfo);
        loginRequest.setTimeZoneOffsetInMilli("7200000");
        loginRequest.setClientVersion(marketParameters.getClientVersion());
        loginRequest.setHardwareId(
                StringUtils.hashAsUUID(
                        credentials.getId() + credentials.getField(Field.Key.USERNAME)));
        loginRequest.setOsBuild("iOS 10.3.2");
        loginRequest.setPassword(credentials.getField(Field.Key.PASSWORD));
        loginRequest.setUser(credentials.getField(Field.Key.USERNAME));
        loginRequest.setDeviceModel("iPhone9,3");
        loginRequest.setLocale(marketParameters.getLocale());
        loginRequest.setRememberMe("true");
        loginRequest.setClientType("iPhone");
        loginRequest.setUserTimeStampInMilli(Long.toString(System.currentTimeMillis()));

        LoginResponse loginResponse;
        try {
            loginResponse =
                    MAPPER.readValue(
                            createRequest(
                                            Objects.equals(marketParameters.getLocale(), "sv_SE")
                                                    ? SWEDISH_LOGIN_URL
                                                    : marketParameters.getBaseUrl()
                                                            + "/v1/loginSummary.do")
                                    .post(String.class, loginRequest),
                            LoginResponse.class);
        } catch (IOException e) {
            throw new IllegalStateException(e.getMessage(), e);
        }

        if (loginResponse.getLogonData() != null) {
            cupcake = loginResponse.getLogonData().getCupcake();
        }

        if (loginResponse.getLogonData() != null
                && loginResponse.getLogonData().getProfileData() != null) {
            credentials.setSensitivePayload(
                    "profileInfo", loginResponse.getLogonData().getProfileData().getData());
        }

        // Save this response for laters, for the refresh operation in the agent
        if (loginResponse.getSummaryData() != null
                && loginResponse.getSummaryData().getCardList() != null) {
            cardList = loginResponse.getSummaryData().getCardList();
        }

        return loginResponse;
    }

    TransactionDetailsEntity getTransactionDetails(int billingIndex, CardDetailsEntity cardEntity)
            throws Exception {
        TransactionsRequest transactionsRequest = new TransactionsRequest();
        transactionsRequest.setBillingIndexList(
                Collections.singletonList(Integer.toString(billingIndex)));
        transactionsRequest.setSortedIndex(cardEntity.getSortedIndex());
        return MAPPER.readValue(
                        createRequest(marketParameters.getBaseUrl() + "/v1/transaction.do")
                                .post(String.class, transactionsRequest),
                        TransactionsResponse.class)
                .getTransactionDetails();
    }

    MarketParameters getMarketParameters() {
        return marketParameters;
    }

    /** Create a map from suppIndex (00, 01 etc.) to card number (XXX-ddddd). */
    Map<String, String> createCardNumberBySuppIndexMap(List<CardEntity> cardList) {
        Map<String, String> cardNumberBySuppIndex = Maps.newHashMap();
        for (CardEntity card : cardList) {
            cardNumberBySuppIndex.put(
                    card.getSuppIndex(), productNameToCardNumber(card.getCardProductName()));
        }
        return cardNumberBySuppIndex;
    }

    String productNameToCardNumber(String prodName) {
        return "XXX-" + prodName.substring(prodName.length() - 5);
    }

    List<CardDetailsEntity> getCardList() {
        return cardList != null ? cardList : Collections.<CardDetailsEntity>emptyList();
    }
}
