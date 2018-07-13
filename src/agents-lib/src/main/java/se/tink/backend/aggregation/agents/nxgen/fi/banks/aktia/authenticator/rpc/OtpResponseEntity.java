package se.tink.backend.aggregation.agents.nxgen.fi.banks.aktia.authenticator.rpc;

public class OtpResponseEntity {
    private boolean otpAccepted;
    private OtpInfoEntity otpInfo;

    public boolean isOtpAccepted() {
        return otpAccepted;
    }

    public OtpInfoEntity getOtpInfo() {
        return otpInfo;
    }
}
