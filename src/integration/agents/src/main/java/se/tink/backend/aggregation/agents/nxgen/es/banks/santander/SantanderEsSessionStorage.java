package se.tink.backend.aggregation.agents.nxgen.es.banks.santander;

import org.w3c.dom.Node;
import se.tink.backend.aggregation.agents.nxgen.es.banks.santander.fetcher.rpc.LoginResponse;
import se.tink.backend.aggregation.agents.nxgen.es.banks.santander.utils.SantanderEsXmlUtils;
import se.tink.backend.aggregation.nxgen.storage.SessionStorage;

public class SantanderEsSessionStorage {

    private final SessionStorage sessionStorage;

    public SantanderEsSessionStorage(
            SessionStorage sessionStorage) {
        this.sessionStorage = sessionStorage;
    }

    public void setLoginResponse(String responseString) {
        Node loginResponseNode =
                SantanderEsXmlUtils.getTagNodeFromSoapString(
                        responseString, SantanderEsConstants.NodeTags.METHOD_RESULT);

        String loginResponseString = SantanderEsXmlUtils.convertToString(loginResponseNode);

        sessionStorage.put(SantanderEsConstants.Storage.LOGIN_RESPONSE, loginResponseString);
    }

    public String getLoginResponseString() {
        return sessionStorage
                .get(SantanderEsConstants.Storage.LOGIN_RESPONSE, String.class)
                .orElseThrow(
                        () ->
                                new IllegalStateException(
                                        SantanderEsConstants.LogMessages
                                                .LOGIN_RESPONSE_NOT_FOUND));
    }

    public LoginResponse getLoginResponse() {
        String loginResponseString = getLoginResponseString();


        return SantanderEsXmlUtils.parseXmlStringToJson(loginResponseString, LoginResponse.class);
    }
}
