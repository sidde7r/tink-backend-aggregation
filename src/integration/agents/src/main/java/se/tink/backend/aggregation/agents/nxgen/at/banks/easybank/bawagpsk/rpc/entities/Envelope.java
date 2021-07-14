package se.tink.backend.aggregation.agents.nxgen.at.banks.easybank.bawagpsk.rpc.entities;

import jakarta.xml.bind.annotation.XmlElement;
import jakarta.xml.bind.annotation.XmlRootElement;
import java.util.Optional;
import org.apache.commons.lang3.StringUtils;
import se.tink.backend.aggregation.agents.nxgen.at.banks.easybank.bawagpsk.rpc.entities.constants.EntitiesConstants;
import se.tink.backend.aggregation.agents.nxgen.at.banks.easybank.bawagpsk.rpc.entities.utils.EntitiesUtils;

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
        return message.equalsIgnoreCase(EntitiesConstants.Messages.STRING_TOO_SHORT)
                || StringUtils.containsIgnoreCase(
                        message, EntitiesConstants.Messages.INPUT_NOT_17_DIGITS);
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
        return EntitiesUtils.entityToXml(this);
    }
}
