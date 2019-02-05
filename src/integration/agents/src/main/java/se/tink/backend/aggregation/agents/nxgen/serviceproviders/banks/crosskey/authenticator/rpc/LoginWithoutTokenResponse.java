package se.tink.backend.aggregation.agents.nxgen.serviceproviders.banks.crosskey.authenticator.rpc;

import se.tink.backend.aggregation.agents.nxgen.serviceproviders.banks.crosskey.rpc.CrossKeyResponse;

public class LoginWithoutTokenResponse extends CrossKeyResponse {

    private String tanPosition;
    private String jobNumber;
    private String seqNumber;
    private String tanListFullness;
    private boolean needChangePin;
    private boolean needChangePinSoon;
    private boolean needChangePinSooner;
    private String nextUrl;
    private String userAliasId;

    public String getTanPosition() {
        return tanPosition;
    }

    public void setTanPosition(String tanPosition) {
        this.tanPosition = tanPosition;
    }

    public String getJobNumber() {
        return jobNumber;
    }

    public void setJobNumber(String jobNumber) {
        this.jobNumber = jobNumber;
    }

    public String getSeqNumber() {
        return seqNumber;
    }

    public void setSeqNumber(String seqNumber) {
        this.seqNumber = seqNumber;
    }

    public String getTanListFullness() {
        return tanListFullness;
    }

    public void setTanListFullness(String tanListFullness) {
        this.tanListFullness = tanListFullness;
    }

    public boolean isNeedChangePin() {
        return needChangePin;
    }

    public void setNeedChangePin(boolean needChangePin) {
        this.needChangePin = needChangePin;
    }

    public boolean isNeedChangePinSoon() {
        return needChangePinSoon;
    }

    public void setNeedChangePinSoon(boolean needChangePinSoon) {
        this.needChangePinSoon = needChangePinSoon;
    }

    public boolean isNeedChangePinSooner() {
        return needChangePinSooner;
    }

    public void setNeedChangePinSooner(boolean needChangePinSooner) {
        this.needChangePinSooner = needChangePinSooner;
    }

    public String getNextUrl() {
        return nextUrl;
    }

    public void setNextUrl(String nextUrl) {
        this.nextUrl = nextUrl;
    }

    public String getUserAliasId() {
        return userAliasId;
    }

    public void setUserAliasId(String userAliasId) {
        this.userAliasId = userAliasId;
    }
}
