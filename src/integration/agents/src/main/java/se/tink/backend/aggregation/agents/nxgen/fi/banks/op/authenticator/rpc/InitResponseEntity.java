package se.tink.backend.aggregation.agents.nxgen.fi.banks.op.authenticator.rpc;

import se.tink.backend.aggregation.agents.nxgen.fi.banks.op.rpc.OpBankResponseEntity;
import se.tink.backend.aggregation.annotations.JsonObject;

@JsonObject
public class InitResponseEntity extends OpBankResponseEntity {

    private boolean osVersionAccepted;
    private boolean appVersionAccepted;
    private String seed;

    // validate we get a response we can process
    public void validateResponse() {
        if (!this.isSuccess()) {
            throw new IllegalStateException(
                    "Unable to start session, response: " + this.toString());
        } else if (!this.isAppVersionAccepted()) {
            throw new IllegalStateException(
                    "App version not accepted, response: " + this.toString());
        } else if (!this.isAppVersionAccepted()) {
            throw new IllegalStateException(
                    "OS version not accepted, response: " + this.toString());
        }
    }

    public boolean isOsVersionAccepted() {
        return osVersionAccepted;
    }

    public boolean isAppVersionAccepted() {
        return appVersionAccepted;
    }

    public InitResponseEntity setAppVersionAccepted(boolean appVersionAccepted) {
        this.appVersionAccepted = appVersionAccepted;
        return this;
    }

    public String getSeed() {
        return seed;
    }

    public InitResponseEntity setSeed(String seed) {
        this.seed = seed;
        return this;
    }
}
