package se.tink.backend.aggregation.agents.nxgen.fr.openbanking.nickel;

import static se.tink.backend.aggregation.agents.nxgen.fr.openbanking.nickel.NickelConstants.APP_VERSION;
import static se.tink.backend.aggregation.agents.nxgen.fr.openbanking.nickel.NickelConstants.BASE_URL_LENGTH;
import static se.tink.backend.aggregation.agents.nxgen.fr.openbanking.nickel.NickelConstants.DEVICE_TYPE;
import static se.tink.backend.aggregation.agents.nxgen.fr.openbanking.nickel.NickelConstants.EMPTY_STRING;
import static se.tink.backend.aggregation.agents.nxgen.fr.openbanking.nickel.NickelConstants.NICKEL_HEADERS;
import static se.tink.backend.aggregation.agents.nxgen.fr.openbanking.nickel.NickelConstants.QueryKeys;
import static se.tink.backend.aggregation.agents.nxgen.fr.openbanking.nickel.NickelConstants.StorageKeys;

import java.util.Optional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import se.tink.backend.aggregation.agents.nxgen.fr.openbanking.nickel.NickelConstants.HeaderKeys;
import se.tink.backend.aggregation.agents.nxgen.fr.openbanking.nickel.NickelConstants.HeaderValues;
import se.tink.backend.aggregation.agents.nxgen.fr.openbanking.nickel.NickelConstants.URLs;
import se.tink.backend.aggregation.agents.nxgen.fr.openbanking.nickel.authenticator.entity.NickelClientData;
import se.tink.backend.aggregation.agents.nxgen.fr.openbanking.nickel.authenticator.rpc.AuthResponse;
import se.tink.backend.aggregation.agents.nxgen.fr.openbanking.nickel.authenticator.rpc.AuthenticationsRequest;
import se.tink.backend.aggregation.agents.nxgen.fr.openbanking.nickel.authenticator.rpc.AuthenticationsResponse;
import se.tink.backend.aggregation.agents.nxgen.fr.openbanking.nickel.authenticator.rpc.ChallengeSMSInitResponse;
import se.tink.backend.aggregation.agents.nxgen.fr.openbanking.nickel.authenticator.rpc.ChallengeSMSResponse;
import se.tink.backend.aggregation.agents.nxgen.fr.openbanking.nickel.authenticator.rpc.MinimumViableAuthentcationRequest;
import se.tink.backend.aggregation.agents.nxgen.fr.openbanking.nickel.fetcher.entity.NickelAccountDetails;
import se.tink.backend.aggregation.agents.nxgen.fr.openbanking.nickel.fetcher.entity.NickelAccountOverview;
import se.tink.backend.aggregation.agents.nxgen.fr.openbanking.nickel.fetcher.rpc.NickelAccountsResponse;
import se.tink.backend.aggregation.agents.nxgen.fr.openbanking.nickel.fetcher.rpc.NickelOperationList;
import se.tink.backend.aggregation.agents.nxgen.fr.openbanking.nickel.fetcher.rpc.NickelUserResponse;
import se.tink.backend.aggregation.agents.nxgen.fr.openbanking.nickel.utils.NickelStorage;
import se.tink.backend.aggregation.nxgen.http.client.TinkHttpClient;
import se.tink.backend.aggregation.nxgen.http.filter.filterable.request.RequestBuilder;
import se.tink.backend.aggregation.nxgen.http.response.HttpResponse;
import se.tink.backend.aggregation.nxgen.http.url.URL;

@Slf4j
@RequiredArgsConstructor
public class NickelApiClient {

    private final TinkHttpClient client;
    private final NickelStorage storage;

    public Optional<HttpResponse> loginWithPassword(String username, String password) {
        storage.setSessionData(StorageKeys.CLIENT_ID, username);
        storage.setSessionData(StorageKeys.CLIENT_SCRT, password);
        return loginRequest(EMPTY_STRING);
    }

    public Optional<HttpResponse> loginWithMfaToken(String mfaToken) {
        return loginRequest(mfaToken);
    }

    public Optional<ChallengeSMSInitResponse> initiateSMSChallenge(String chalengeToken) {
        storage.setSessionData(QueryKeys.CHALLENGE_TKN, chalengeToken);
        RequestBuilder requestBuilder =
                createSMSRequest(new URL(URLs.SMS_CODE_REQUEST))
                        .queryParam(QueryKeys.CHALLENGE_TKN, chalengeToken);
        return Optional.of(addPathHeader(requestBuilder).post(ChallengeSMSInitResponse.class));
    }

    public Optional<ChallengeSMSResponse> loginWithOTPCode(String otp) {
        String chalengeToken = storage.getSessionData(QueryKeys.CHALLENGE_TKN);
        ChallengeSMSResponse response =
                createBaseRequest(new URL(URLs.SMS_CODE_VERIFICATIONS))
                        .queryParam(QueryKeys.CHALLENGE_TKN, chalengeToken)
                        .queryParam(QueryKeys.CODE, otp)
                        .post(ChallengeSMSResponse.class);
        return Optional.of(response);
    }

    public Optional<AuthenticationsResponse> getPersonalAccessToken() {
        AuthenticationsRequest request =
                AuthenticationsRequest.builder()
                        .barcode(storage.getSessionData(StorageKeys.CLIENT_ID))
                        .minimumViableToken(storage.getSessionData(StorageKeys.MFA_TKN))
                        .deviceToken(storage.getPersistentData(StorageKeys.DEVICE_ID))
                        .deviceType(DEVICE_TYPE)
                        .version(APP_VERSION)
                        .build();
        RequestBuilder requestBuilder = createBaseRequest(new URL(URLs.USER_PERSONAL_AUTH));
        return Optional.of(requestBuilder.post(AuthenticationsResponse.class, request));
    }

    public Optional<AuthResponse> authentications(String token) {
        AuthenticationsRequest request =
                AuthenticationsRequest.builder()
                        .barcode(storage.getSessionData(StorageKeys.CLIENT_ID))
                        .minimumViableToken(storage.getSessionData(StorageKeys.MFA_TKN))
                        .deviceToken(storage.getPersistentData(StorageKeys.DEVICE_ID))
                        .deviceType(DEVICE_TYPE)
                        .version(APP_VERSION)
                        .minimumViableToken(token)
                        .build();
        RequestBuilder requestBuilder =
                createBaseRequest(new URL(URLs.USER_PERSONAL_AUTH))
                        .header(HeaderKeys.AUTH_CHALLENGE_TKN, token);
        return Optional.of(requestBuilder.post(AuthResponse.class, request));
    }

    public Optional<NickelAccountsResponse> getAccounts() {
        return Optional.of(
                createBaseRequest(new URL(URLs.ACCOUNTS_PATH))
                        .header(
                                HeaderKeys.AUTHORIZATION,
                                storage.getSessionData(StorageKeys.MFA_TKN))
                        .queryParam(
                                QueryKeys.CUSTOMER_ID,
                                storage.getSessionData(StorageKeys.CUSTOMER_ID))
                        .get(NickelAccountsResponse.class));
    }

    public Optional<NickelAccountDetails> getAccountDetails(String accessToken) {
        RequestBuilder requestBuilder =
                createBaseRequest(
                                new URL(
                                        String.format(
                                                URLs.PRIMARY_ACCOUNT_DETAILS_URL,
                                                storage.getSessionData(StorageKeys.CUSTOMER_ID))))
                        .header(
                                HeaderKeys.AUTHORIZATION,
                                String.format(HeaderValues.BEARER_FORMAT, accessToken));
        return Optional.of(addPathHeader(requestBuilder).get(NickelAccountDetails.class));
    }

    public Optional<NickelAccountOverview> getAccountOverview(String number, String accessToken) {
        return Optional.of(
                createBaseRequest(new URL(String.format(URLs.ACCOUNTS_OVERVIEW_PATH, number)))
                        .header(HeaderKeys.AUTHORIZATION, accessToken)
                        .get(NickelAccountOverview.class));
    }

    public Optional<NickelUserResponse> getEndUserIdentity() {
        return Optional.of(
                createBaseRequest(
                                new URL(
                                        String.format(
                                                URLs.USER_DATA_URL,
                                                storage.getSessionData(StorageKeys.CLIENT_ID))))
                        .header(
                                HeaderKeys.AUTHORIZATION,
                                String.format(
                                        HeaderValues.BEARER_FORMAT,
                                        storage.getSessionData(StorageKeys.PERSONAL_ACCESS_TKN)))
                        .get(NickelUserResponse.class));
    }

    public Optional<NickelOperationList> getTransactions(
            String apiIdentifier, String fromDate, String toDate, String accessToken) {
        RequestBuilder requestBuilder =
                createBaseRequest(new URL(String.format(URLs.USER_OPERATIONS_URL, apiIdentifier)))
                        .header(
                                HeaderKeys.AUTHORIZATION,
                                String.format(HeaderValues.BEARER_FORMAT, accessToken))
                        .queryParam(QueryKeys.DATE_TO, toDate)
                        .queryParam(QueryKeys.LABEL, EMPTY_STRING)
                        .queryParam(QueryKeys.DATE_FROM, fromDate);
        return Optional.of(addPathHeader(requestBuilder).get(NickelOperationList.class));
    }

    private Optional<HttpResponse> loginRequest(String mfaToken) {
        MinimumViableAuthentcationRequest request =
                MinimumViableAuthentcationRequest.builder()
                        .client(
                                new NickelClientData(
                                        storage.getOrCreatePersistentData(
                                                StorageKeys.DEVICE_ID, false)))
                        .password(storage.getSessionData(StorageKeys.CLIENT_SCRT))
                        .userId(storage.getSessionData(StorageKeys.CLIENT_ID))
                        .build();
        RequestBuilder requestBuilder =
                createBaseRequest(new URL(URLs.MINIMUM_VIABLE_AUTHENTICATIONS_URL));
        if (!mfaToken.isEmpty()) {
            request.setMfaToken(mfaToken);
            requestBuilder.header(HeaderKeys.NUDETECT_PAYLOAD, mfaToken);
        }
        return Optional.of(addPathHeader(requestBuilder).post(HttpResponse.class, request));
    }

    private RequestBuilder addPathHeader(RequestBuilder builder) {
        return builder.header(
                HeaderKeys.PATH, builder.getUrl().toString().substring(BASE_URL_LENGTH));
    }

    private RequestBuilder createBaseRequest(URL url) {
        return client.request(url)
                .removeAggregatorHeader()
                .accept(HeaderValues.ACCEPT)
                .headers(NICKEL_HEADERS)
                .header(HeaderKeys.CONTENT_TYPE, HeaderValues.JSON_CONTENT)
                .header(
                        HeaderKeys.SESSION_ID,
                        storage.getOrCreateSessionData(HeaderKeys.SESSION_ID, true));
    }

    private RequestBuilder createSMSRequest(URL url) {
        return client.request(url)
                .accept(HeaderValues.ACCEPT)
                .headers(NICKEL_HEADERS)
                .header(HeaderKeys.METHOD, "POST");
    }
}
