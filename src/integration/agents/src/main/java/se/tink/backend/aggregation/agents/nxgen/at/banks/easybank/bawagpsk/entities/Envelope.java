package se.tink.backend.aggregation.agents.nxgen.at.banks.easybank.bawagpsk.entities;

import java.util.Optional;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;
import org.apache.commons.lang.StringUtils;
import se.tink.backend.aggregation.agents.nxgen.at.banks.easybank.bawagpsk.BawagPskConstants;
import se.tink.backend.aggregation.agents.nxgen.at.banks.easybank.bawagpsk.BawagPskUtils;

@XmlRootElement(name = "Envelope", namespace = "http://schemas.xmlsoap.org/soap/envelope/")
public class Envelope {
    private Body body;
    private String header;

    @XmlElement(name = "Body", namespace = "http://schemas.xmlsoap.org/soap/envelope/")
    public void setBody(Body body) {
        this.body = body;
    }

    public Body getBody() {
        return body;
    }

    @XmlElement(name = "Header", namespace = "http://schemas.xmlsoap.org/soap/envelope/")
    public void setHeader(String header) {
        this.header = header;
    }

    public String getHeader() {
        return header;
    }

    private static boolean errorMessageIndicatesIncorrectCredentials(final String message) {
        return message.equalsIgnoreCase(BawagPskConstants.Messages.STRING_TOO_SHORT)
                || StringUtils.containsIgnoreCase(
                        message, BawagPskConstants.Messages.INPUT_NOT_17_DIGITS);
    }

    public boolean credentialsAreIncorrect() {
        return getErrorMessage()
                .map(Envelope::errorMessageIndicatesIncorrectCredentials)
                .orElse(false);
    }

    public Optional<String> getErrorMessage() {
        return Optional.ofNullable(body)
                .map(Body::getFault)
                .map(Fault::getFaultString)
                .map(String::trim);
    }

    public String getXml() {
        return BawagPskUtils.entityToXml(this);
    }
}
