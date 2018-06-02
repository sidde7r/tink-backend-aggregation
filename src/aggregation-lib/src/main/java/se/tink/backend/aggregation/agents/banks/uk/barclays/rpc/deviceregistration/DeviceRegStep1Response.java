package se.tink.backend.aggregation.agents.banks.uk.barclays.rpc.deviceregistration;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import se.tink.backend.aggregation.agents.banks.uk.barclays.rpc.Response;

@JsonIgnoreProperties(ignoreUnknown = true)
public class DeviceRegStep1Response extends Response {
    /*
    {
        "op_gsn": "438p-01-01",
        "op_msg": null,
        "appServerSeed": "61fb7301d45d727de49b070e58f8f89dfe6fab6d3a6fa6884677fea507851bda",
        "op_status": "00000",
        "deviceServerSeed": "74c8ea4a46935878ab193a4dbd68133773f2629b4490fd5a98df9409f4b92f3a",
        "registeredServices": null,
        "deviceRegistrationStatus": 0,
        "errorCode": "00000",
        "re": "5dc22a8465baf3192b1a12372cc97df423857d9d0bd05e9bae038e2dc514e8de",
        "sid": "5512724633111162",
        "aid": "7646663432513025",
        "isFeatureOnHighRisk": false
    }
     */
    private String appServerSeed;
    private String deviceServerSeed;
    private String re;
    private String sid;
    private String aid;

    public String getAppServerSeed() {
        return appServerSeed;
    }

    public void setAppServerSeed(String appServerSeed) {
        this.appServerSeed = appServerSeed;
    }

    public String getDeviceServerSeed() {
        return deviceServerSeed;
    }

    public void setDeviceServerSeed(String deviceServerSeed) {
        this.deviceServerSeed = deviceServerSeed;
    }

    public String getRe() {
        return re;
    }

    public void setRe(String re) {
        this.re = re;
    }

    public String getSid() {
        return sid;
    }

    public void setSid(String sid) {
        this.sid = sid;
    }

    public String getAid() {
        return aid;
    }

    public void setAid(String aid) {
        this.aid = aid;
    }
}
