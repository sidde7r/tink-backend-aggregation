package se.tink.backend.aggregation.agents.nxgen.es.banks.santander;

import static se.tink.backend.aggregation.agents.nxgen.es.banks.santander.SantanderEsConstants.Storage.ID_NUMBER;
import static se.tink.backend.aggregation.agents.nxgen.es.banks.santander.SantanderEsConstants.Storage.LOGIN_RESPONSE;

import org.w3c.dom.Node;
import se.tink.backend.aggregation.agents.nxgen.es.banks.santander.fetcher.rpc.LoginResponse;
import se.tink.backend.aggregation.agents.nxgen.es.banks.santander.utils.SantanderEsXmlUtils;
import se.tink.backend.aggregation.nxgen.storage.SessionStorage;

public class SantanderEsSessionStorage {

    private final SessionStorage sessionStorage;

    public SantanderEsSessionStorage(SessionStorage sessionStorage) {
        this.sessionStorage = sessionStorage;
    }

    public String getLoginResponseString() {
        return sessionStorage
                .get(LOGIN_RESPONSE, String.class)
                .orElseThrow(
                        () ->
                                new IllegalStateException(
                                        SantanderEsConstants.LogMessages.LOGIN_RESPONSE_NOT_FOUND));
    }

    public LoginResponse getLoginResponse() {
        String loginResponseString = getLoginResponseString();

        return SantanderEsXmlUtils.parseXmlStringToJson(loginResponseString, LoginResponse.class);
    }

    public void setLoginResponse(String responseString) {
        Node loginResponseNode =
                SantanderEsXmlUtils.getTagNodeFromSoapString(
                        responseString, SantanderEsConstants.NodeTags.METHOD_RESULT);

        String loginResponseString = SantanderEsXmlUtils.convertToString(loginResponseNode);

        sessionStorage.put(LOGIN_RESPONSE, loginResponseString);
    }

    public void setIdNumber(final String idNumber) {
        sessionStorage.put(ID_NUMBER, idNumber);
    }

    public String getUserId() {
        return sessionStorage.get(ID_NUMBER);
    }
}
