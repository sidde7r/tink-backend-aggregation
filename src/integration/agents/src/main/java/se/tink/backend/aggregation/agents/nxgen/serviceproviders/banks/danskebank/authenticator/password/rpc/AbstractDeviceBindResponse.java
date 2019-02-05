package se.tink.backend.aggregation.agents.nxgen.serviceproviders.banks.danskebank.authenticator.password.rpc;

import se.tink.backend.aggregation.agents.nxgen.serviceproviders.banks.danskebank.rpc.AbstractResponse;

public class AbstractDeviceBindResponse extends AbstractResponse {
    private int httpCode;
    private String httpMessage;
    private String moreInformation;

    public int getHttpCode() {
        return httpCode;
    }

    public String getHttpMessage() {
        return httpMessage;
    }

    public String getMoreInformation() {
        return moreInformation;
    }
}
