package se.tink.backend.aggregation.agents.nxgen.dk.banks.jyske;

import javax.ws.rs.core.MediaType;
import se.tink.backend.aggregation.agents.nxgen.dk.banks.jyske.authenticator.JyskeSecurityHelper;
import se.tink.backend.aggregation.agents.nxgen.dk.banks.jyske.authenticator.entities.KeycardEnrollEntity;
import se.tink.backend.aggregation.agents.nxgen.dk.banks.jyske.authenticator.entities.NemIdEnrollEntity;
import se.tink.backend.aggregation.agents.nxgen.dk.banks.jyske.authenticator.entities.NemIdGenerateCodeRequest;
import se.tink.backend.aggregation.agents.nxgen.dk.banks.jyske.authenticator.entities.NemIdGenericRequest;
import se.tink.backend.aggregation.agents.nxgen.dk.banks.jyske.authenticator.entities.NemIdInitRequest;
import se.tink.backend.aggregation.agents.nxgen.dk.banks.jyske.authenticator.entities.NemIdLoginEncryptionEntity;
import se.tink.backend.aggregation.agents.nxgen.dk.banks.jyske.authenticator.entities.NemIdLoginInstallIdEncryptionEntity;
import se.tink.backend.aggregation.agents.nxgen.dk.banks.jyske.authenticator.entities.NemIdLoginWithInstallIdResponse;
import se.tink.backend.aggregation.agents.nxgen.dk.banks.jyske.authenticator.rpc.NemIdLoginResponse;
import se.tink.backend.aggregation.agents.nxgen.dk.banks.jyske.authenticator.rpc.NemIdResponse;
import se.tink.backend.aggregation.agents.nxgen.dk.banks.jyske.authenticator.security.Encryptable;
import se.tink.backend.aggregation.agents.nxgen.dk.banks.jyske.authenticator.security.Token;
import se.tink.backend.aggregation.agents.nxgen.dk.banks.jyske.filters.JyskeBankUnavailableFilter;
import se.tink.backend.aggregation.nxgen.http.client.TinkHttpClient;
import se.tink.backend.aggregation.nxgen.http.filter.filterable.request.RequestBuilder;
import se.tink.backend.aggregation.nxgen.http.response.HttpResponse;
import se.tink.backend.aggregation.nxgen.http.url.URL;

public class JyskeApiClient {

    private final TinkHttpClient client;
    private JyskePublicKeys configuration;

    public JyskeApiClient(TinkHttpClient client) {
        this.client = client;
        this.client.addFilter(new JyskeBankUnavailableFilter());
        this.configuration = new JyskePublicKeys();
    }

    public NemIdLoginResponse nemIdInit(Token token) {
        NemIdInitRequest request = new NemIdInitRequest();
        request.setRsalabel(JyskeConstants.Crypto.RSA_LABEL);

        request.setData(
                JyskeSecurityHelper.encryptForBankdataWithRSAAndBase64Encode(
                        token.getBytes(), configuration.getProductNemIdPublicKey()));

        return createJsonRequest(JyskeConstants.Url.NEMID_INIT)
                .post(NemIdLoginResponse.class, request);
    }

    public NemIdResponse nemIdGetChallenge(
            NemIdLoginEncryptionEntity encryptionEntity, Token token) {
        return doNemIdRequest(JyskeConstants.Url.NEMID_GET_CHALLANGE, token, encryptionEntity);
    }

    public NemIdResponse nemIdEnroll(KeycardEnrollEntity enrollEntity, Token token) {
        return doNemIdRequest(JyskeConstants.Url.NEMID_ENROLL, token, enrollEntity);
    }

    public NemIdResponse nemIdEnroll(NemIdEnrollEntity enrollEntity, Token token) {
        return doNemIdRequest(JyskeConstants.Url.NEMID_ENROLL, token, enrollEntity);
    }

    public NemIdResponse nemIdLoginWithInstallId(
            NemIdLoginInstallIdEncryptionEntity encryptionEntity, Token token) {
        return doNemIdRequest(JyskeConstants.Url.NEMID_LOGIN, token, encryptionEntity);
    }

    public NemIdResponse generateCode(NemIdGenerateCodeRequest request, Token token) {
        return doNemIdRequest(JyskeConstants.Url.GENERATE_CODE, token, request);
    }

    private NemIdResponse doNemIdRequest(URL url, Token token, Encryptable encryptable) {
        return createJsonRequest(url)
                .post(
                        NemIdResponse.class,
                        NemIdGenericRequest.create(
                                token, JyskeConstants.Crypto.AES_PADDING, encryptable));
    }

    public NemIdLoginResponse sendTransportKey(Token token) {
        NemIdInitRequest request = new NemIdInitRequest();

        request.setData(
                JyskeSecurityHelper.encryptForServiceWithRSAAndBase64Encode(
                        token.getBytes(), configuration.getMobileServicePublicKey()));

        return createJsonRequest(JyskeConstants.Url.TRANSPORT_KEY)
                .post(NemIdLoginResponse.class, request);
    }

    public NemIdLoginResponse mobilServiceLogin(
            NemIdLoginWithInstallIdResponse nemIdLogin, Token token) {
        NemIdInitRequest request = new NemIdInitRequest();
        request.setData(nemIdLogin.encrypt(token));
        return createJsonRequest(JyskeConstants.Url.MOBILE_SERVICE_LOGIN)
                .post(NemIdLoginResponse.class, request);
    }

    // Only present for session handler sake. Used to check if session is still live, or throws an
    // exception.
    public HttpResponse fetchAccounts() {
        return this.createRequest(JyskeConstants.Url.GET_ACCOUNTS_WITH_EXTERNALS)
                .header(JyskeConstants.Header.BUILDNO_KEY, JyskeConstants.Header.BUILDNO_VALUE)
                .get(HttpResponse.class);
    }

    public NemIdResponse changeToKeyCard() {
        return createJsonRequest(JyskeConstants.Url.CHANGE_TO_KEYCARD).get(NemIdResponse.class);
    }

    public void logout(String username) {
        this.createRequest(JyskeConstants.Url.LOGOUT)
                .header(JyskeConstants.Header.BUILDNO_KEY, JyskeConstants.Header.BUILDNO_VALUE)
                .header(JyskeConstants.Header.PERSONALID_KEY, username)
                .accept(MediaType.APPLICATION_JSON_TYPE)
                .get(null);
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
