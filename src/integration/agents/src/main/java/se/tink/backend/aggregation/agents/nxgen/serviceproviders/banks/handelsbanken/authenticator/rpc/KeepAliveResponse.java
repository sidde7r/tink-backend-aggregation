package se.tink.backend.aggregation.agents.nxgen.serviceproviders.banks.handelsbanken.authenticator.rpc;

import com.google.common.base.MoreObjects;
import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlRootElement;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.banks.handelsbanken.rpc.BaseResponse;

// Session expiration sends xml, so JAXB annotations needed.
@XmlRootElement(name = "response")
public class KeepAliveResponse extends BaseResponse {

    public boolean isAlive() {
        return getCode() == null;
    }

    public String createErrorMessage() {
        return MoreObjects.toStringHelper(this)
                .add("code", getCode())
                .add("links", getLinks().size())
                .add("details", getDetail())
                .add("message", getMessage())
                .add("error messages", getErrors())
                .add("desc", getDesc())
                .toString();
    }

    @Override
    @XmlAttribute
    public void setCode(String code) {
        super.setCode(code);
    }

    public static KeepAliveResponse aliveEntryPoint() {
        return new AliveEntryPoint();
    }

    public static KeepAliveResponse deadEntryPoint() {
        return new DeadEntryPoint();
    }

    private static class AliveEntryPoint extends KeepAliveResponse {
        @Override
        public boolean isAlive() {
            return true;
        }
    }

    private static class DeadEntryPoint extends KeepAliveResponse {
        @Override
        public boolean isAlive() {
            return false;
        }
    }
}
