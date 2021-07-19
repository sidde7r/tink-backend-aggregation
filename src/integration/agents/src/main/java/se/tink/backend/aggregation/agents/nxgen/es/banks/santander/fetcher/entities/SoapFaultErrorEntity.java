package se.tink.backend.aggregation.agents.nxgen.es.banks.santander.fetcher.entities;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.google.common.base.Strings;
import jakarta.xml.bind.annotation.XmlRootElement;
import java.util.Optional;
import java.util.regex.Pattern;
import se.tink.backend.aggregation.agents.nxgen.es.banks.santander.SantanderEsConstants;
import se.tink.backend.aggregation.agents.nxgen.es.banks.santander.utils.SantanderEsXmlUtils;
import se.tink.backend.aggregation.annotations.JsonObject;
import se.tink.libraries.serialization.utils.SerializationUtils;

@JsonObject
@XmlRootElement(name = "error")
public class SoapFaultErrorEntity {
    @JsonProperty("errorDesc")
    private String errorDescription;

    @JsonProperty("errorCode")
    private String errorCode;

    public static Optional<SoapFaultErrorEntity> parseFaultErrorFromSoapError(
            String xmlErrorResponseString) {
        String errorAsString =
                SerializationUtils.deserializeFromString(
                        SerializationUtils.serializeToString(
                                SantanderEsXmlUtils.getTagNodeFromSoapString(
                                        xmlErrorResponseString,
                                        SantanderEsConstants.NodeTags.FAULT_ERROR)),
                        String.class);

        if (Strings.isNullOrEmpty(errorAsString)) {
            return Optional.empty();
        }
        SoapFaultErrorEntity soapFaultErrorEntity =
                SantanderEsXmlUtils.parseXmlStringToJson(errorAsString, SoapFaultErrorEntity.class);

        return Optional.ofNullable(soapFaultErrorEntity);
    }

    public String getErrorDescription() {
        return errorDescription;
    }

    public String getErrorCode() {
        return errorCode;
    }

    @JsonIgnore
    public boolean matchesErrorMessage(String errorMessage) {
        return Pattern.compile(Pattern.quote(errorMessage), Pattern.CASE_INSENSITIVE)
                .matcher(errorDescription)
                .find();
    }
}
