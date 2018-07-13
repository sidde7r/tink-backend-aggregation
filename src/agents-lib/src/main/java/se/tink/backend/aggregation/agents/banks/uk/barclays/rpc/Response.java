package se.tink.backend.aggregation.agents.banks.uk.barclays.rpc;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import java.util.Optional;

@JsonIgnoreProperties(ignoreUnknown = true)
public class Response {
    // a sha256 hash digest of the original data (used in auth)
    private byte[] __dataHash__;

    private String op_gsn;
    private String op_msg;
    private String op_status;
    private String errorCode;
    private boolean isFeatureOnHighRisk;
    private int deviceRegistrationStatus;

    public byte[] get__dataHash__() {
        return __dataHash__;
    }

    public void set__dataHash__(byte[] __dataHash__) {
        this.__dataHash__ = __dataHash__;
    }

    public boolean isError() {
        return !Optional.ofNullable(errorCode).orElse(op_status).equals("00000");
    }

    public String getOp_gsn() {
        return op_gsn;
    }

    public void setOp_gsn(String op_gsn) {
        this.op_gsn = op_gsn;
    }

    public String getOp_msg() {
        return op_msg;
    }

    public void setOp_msg(String op_msg) {
        this.op_msg = op_msg;
    }

    public String getOp_status() {
        return op_status;
    }

    public void setOp_status(String op_status) {
        this.op_status = op_status;
    }

    public String getErrorCode() {
        return errorCode;
    }

    public void setErrorCode(String errorCode) {
        this.errorCode = errorCode;
    }

    public boolean isFeatureOnHighRisk() {
        return isFeatureOnHighRisk;
    }

    public void setFeatureOnHighRisk(boolean featureOnHighRisk) {
        isFeatureOnHighRisk = featureOnHighRisk;
    }

    public int getDeviceRegistrationStatus() {
        return deviceRegistrationStatus;
    }

    public void setDeviceRegistrationStatus(int deviceRegistrationStatus) {
        this.deviceRegistrationStatus = deviceRegistrationStatus;
    }
}
