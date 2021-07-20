package se.tink.backend.aggregation.agents.nxgen.se.banks.handelsbanken.rpc;

import jakarta.xml.bind.annotation.XmlAccessType;
import jakarta.xml.bind.annotation.XmlAccessorType;
import jakarta.xml.bind.annotation.XmlAttribute;
import jakarta.xml.bind.annotation.XmlRootElement;
import se.tink.backend.aggregation.agents.nxgen.se.banks.handelsbanken.HandelsbankenSEConstants.ErrorMessage;
import se.tink.backend.aggregation.agents.nxgen.se.banks.handelsbanken.HandelsbankenSEConstants.ResponseCodes;

@XmlRootElement(name = "response")
@XmlAccessorType(XmlAccessType.FIELD)
public class XmlErrorResponse {

    @XmlAttribute private String code;
    @XmlAttribute private String label;

    public String getCode() {
        return code;
    }

    public String getLabel() {
        return label;
    }

    public boolean isServiceUnavailable() {
        // Error text to look for is very specific, as to not accidentally treat something that's
        // an issue on our and as a bank side failure.
        return ResponseCodes.SERVICE_NOT_AVAILABLE.equals(code)
                && ErrorMessage.SERVICE_NOT_AVAILABLE.equalsIgnoreCase(label);
    }
}
