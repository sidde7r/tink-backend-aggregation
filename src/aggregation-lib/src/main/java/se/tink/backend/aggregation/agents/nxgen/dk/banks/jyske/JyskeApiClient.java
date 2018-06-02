package se.tink.backend.aggregation.agents.nxgen.dk.banks.jyske;

import java.util.Collections;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import javax.ws.rs.core.MediaType;
import se.tink.backend.aggregation.agents.nxgen.dk.banks.jyske.accounts.checking.entities.AccountBriefEntity;
import se.tink.backend.aggregation.agents.nxgen.dk.banks.jyske.accounts.checking.entities.GetFutureTransactionsRequest;
import se.tink.backend.aggregation.agents.nxgen.dk.banks.jyske.accounts.checking.entities.GetTransactionsRequest;
import se.tink.backend.aggregation.agents.nxgen.dk.banks.jyske.accounts.checking.rpc.GetAccountResponse;
import se.tink.backend.aggregation.agents.nxgen.dk.banks.jyske.accounts.checking.rpc.GetTransactionsResponse;
import se.tink.backend.aggregation.agents.nxgen.dk.banks.jyske.authenticator.JyskeSecurityHelper;
import se.tink.backend.aggregation.agents.nxgen.dk.banks.jyske.authenticator.entities.NemIdEnrollEntity;
import se.tink.backend.aggregation.agents.nxgen.dk.banks.jyske.authenticator.entities.NemIdGenericRequest;
import se.tink.backend.aggregation.agents.nxgen.dk.banks.jyske.authenticator.entities.NemIdInitRequest;
import se.tink.backend.aggregation.agents.nxgen.dk.banks.jyske.authenticator.entities.NemIdLoginEncryptionEntity;
import se.tink.backend.aggregation.agents.nxgen.dk.banks.jyske.authenticator.entities.NemIdLoginWithInstallIdResponse;
import se.tink.backend.aggregation.agents.nxgen.dk.banks.jyske.authenticator.rpc.NemIdLoginResponse;
import se.tink.backend.aggregation.agents.nxgen.dk.banks.jyske.authenticator.rpc.NemIdResponse;
import se.tink.backend.aggregation.agents.nxgen.dk.banks.jyske.authenticator.security.Encryptable;
import se.tink.backend.aggregation.agents.nxgen.dk.banks.jyske.authenticator.security.Token;
import se.tink.backend.aggregation.agents.nxgen.dk.banks.jyske.investment.rpc.FetchInvestmentResponse;
import se.tink.backend.aggregation.nxgen.core.account.Account;
import se.tink.backend.aggregation.nxgen.http.RequestBuilder;
import se.tink.backend.aggregation.nxgen.http.TinkHttpClient;
import se.tink.backend.aggregation.nxgen.http.URL;

public class JyskeApiClient {

    private final TinkHttpClient client;
    private List<AccountBriefEntity> accounts;

    public JyskeApiClient(TinkHttpClient client) {
        this.client = client;
    }

    public NemIdLoginResponse nemIdInit(Token token) {
        NemIdInitRequest request = new NemIdInitRequest();
        request.setRsalabel(JyskeConstants.Crypto.RSA_LABEL);

        request.setData(
                JyskeSecurityHelper.encryptForBankdataWithRSAAndBase64Encode(
                        token.getBytes(), JyskeConstants.Crypto.PRODUCT_NEMID_KEY
                )
        );

        return createJsonRequest(JyskeConstants.Url.NEMID_INIT)
                .post(NemIdLoginResponse.class, request);
    }

    public NemIdResponse nemIdGetChallenge(NemIdLoginEncryptionEntity encryptionEntity, Token token) {
        return doNemIdRequest(JyskeConstants.Url.NEMID_GET_CHALLANGE, token, encryptionEntity);

    }

    public NemIdResponse nemIdEnroll(NemIdEnrollEntity enrollEntity, Token token) {
        return doNemIdRequest(JyskeConstants.Url.NEMID_ENROLL, token, enrollEntity);
    }

    public NemIdResponse nemIdLoginWithInstallId(NemIdLoginEncryptionEntity encryptionEntity, Token token) {
        return doNemIdRequest(JyskeConstants.Url.NEMID_LOGIN, token, encryptionEntity);
    }

    private NemIdResponse doNemIdRequest(URL url, Token token, Encryptable encryptable) {
        return createJsonRequest(url)
                .post(NemIdResponse.class, NemIdGenericRequest.create(token, encryptable));
    }

    public NemIdLoginResponse mobilServiceInit(Token token) {
        NemIdInitRequest request = new NemIdInitRequest();

        request.setData(
                JyskeSecurityHelper.encryptForServiceWithRSAAndBase64Encode(
                        token.getBytes(), JyskeConstants.Crypto.MOBILE_SERVICE_KEY
                )
        );

        return createJsonRequest(JyskeConstants.Url.TRANSPORT_KEY)
                .post(NemIdLoginResponse.class, request);
    }

    public NemIdLoginResponse mobilServiceLogin(NemIdLoginWithInstallIdResponse nemIdLogin, Token token) {
        NemIdInitRequest request = new NemIdInitRequest();
        request.setData(nemIdLogin.encrypt(token));
        return createJsonRequest(JyskeConstants.Url.MOBILE_SERVICE_LOGIN)
                .post(NemIdLoginResponse.class, request);
    }

    public GetAccountResponse fetchAccounts() {

        GetAccountResponse getAccountResponse = this.createRequest(JyskeConstants.Url.GET_ACCOUNTS)
                .header(JyskeConstants.Header.BUILDNO_KEY, JyskeConstants.Header.BUILDNO_VALUE)
                .get(GetAccountResponse.class);

        this.accounts = getAccountResponse.getAccountsBrief();

        return getAccountResponse;
    }

    public GetTransactionsResponse fetchTransactions(Account account, int page) {
        GetTransactionsRequest request = new GetTransactionsRequest();

        Optional<AccountBriefEntity> accountBriefEntity = this.accounts.stream()
                .filter(acc -> Objects.equals(Optional.ofNullable(acc.getRegNo()).orElse("") +
                        Optional.ofNullable(acc.getAccountNo()).orElse(""), account.getAccountNumber()))
                .findFirst();

        if (accountBriefEntity.isPresent()) {
            request.setAccounts(Collections.singletonList(accountBriefEntity.get()));
        } else {
            throw new IllegalStateException("No such account fetched previously");
        }

        request.setPage(page);
        request.setShowPlanning(false);

        return this.createJsonRequest(JyskeConstants.Url.GET_TRANSACTIONS)
                .header(JyskeConstants.Header.BUILDNO_KEY, JyskeConstants.Header.BUILDNO_VALUE)
                .post(GetTransactionsResponse.class, request);
    }

    public GetTransactionsResponse fetchFutureTransactions(Account account) {

        GetFutureTransactionsRequest request = new GetFutureTransactionsRequest();

        Optional<AccountBriefEntity> accountBriefEntity = this.accounts.stream()
                .filter(acc -> Objects.equals(acc.getRegNo() + acc.getAccountNo(), account.getAccountNumber()))
                .findFirst();

        if (accountBriefEntity.isPresent()) {
            request.setAccounts(Collections.singletonList(accountBriefEntity.get()));
        } else {
            throw new IllegalStateException("No such account fetched previously");
        }

        request.setPage(0);

        return this.createJsonRequest(JyskeConstants.Url.GET_FUTURE_TRANSACTIONS)
                .header(JyskeConstants.Header.BUILDNO_KEY, JyskeConstants.Header.BUILDNO_VALUE)
                .post(GetTransactionsResponse.class, request);
    }

    public FetchInvestmentResponse fetchInvestment() {
        return this.createJsonRequest(JyskeConstants.Url.GET_INVESTMENT_GROUPS)
                .header(JyskeConstants.Header.BUILDNO_KEY, JyskeConstants.Header.BUILDNO_VALUE)
                .post(FetchInvestmentResponse.class, "[]");
    }

    public void logout(String username) {
        this.createRequest(JyskeConstants.Url.LOGOUT)
                .header(JyskeConstants.Header.BUILDNO_KEY, JyskeConstants.Header.BUILDNO_VALUE)
                .header(JyskeConstants.Header.PERSONALID_KEY, username)
                .accept(MediaType.APPLICATION_JSON_TYPE)
                .get(null);
    }

    public String fetchCards() {
        return createJsonRequest(JyskeConstants.Url.GET_CARDS)
                .get(String.class);
    }

    private RequestBuilder createRequest(URL url) {
        return client.request(url)
                .header(JyskeConstants.Header.APP_ID_KEY, JyskeConstants.Header.APP_ID_VALUE)
                .header(JyskeConstants.Header.APPID_KEY, JyskeConstants.Header.APPID_VALUE)
                .header(JyskeConstants.Header.VERSION_KEY, JyskeConstants.Header.VERSION_VALUE)
                .header(JyskeConstants.Header.BANKNO_KEY, JyskeConstants.Header.BANKNO_VALUE)
                .header(JyskeConstants.Header.OS_KEY, JyskeConstants.Header.OS_VALUE);
    }

    private RequestBuilder createJsonRequest(URL url) {
        return this.createRequest(url)
                .accept(MediaType.APPLICATION_JSON_TYPE)
                .type(MediaType.APPLICATION_JSON_TYPE);
    }
}
