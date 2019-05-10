package se.tink.backend.aggregation.agents.nxgen.es.banks.openbank.fetcher.rpc;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;
import java.time.LocalDate;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import se.tink.backend.aggregation.agents.nxgen.es.banks.openbank.OpenbankConstants.UsernameTypes;
import se.tink.backend.aggregation.annotations.JsonObject;
import se.tink.libraries.identitydata.IdentityData;
import se.tink.libraries.identitydata.countries.EsIdentityData;
import se.tink.libraries.identitydata.countries.EsIdentityData.EsIdentityDataBuilder;
import se.tink.libraries.serialization.utils.SerializationUtils;

@JsonObject
public class IdentityResponse {

    private static final Logger LOGGER = LoggerFactory.getLogger(IdentityResponse.class);

    @JsonProperty("fechaNacimiento")
    private String birthDate;

    @JsonProperty("nombreCliente")
    private String clientName;

    @JsonProperty("apellidoUno")
    private SurnameOneEntity surnameOne;

    @JsonProperty("documentoIdentificacion")
    private IdentificationDocumentEntity identity;

    public void setClientName(String clientName) {
        this.clientName = clientName != null ? clientName.trim() : null;
    }

    @JsonIgnore
    public IdentityData toTinkIdentity() {
        EsIdentityDataBuilder builder = EsIdentityData.builder();

        if (surnameOne == null || identity == null) {
            throw new IllegalStateException(
                    String.format(
                            "Identity response without actual identity for Openbank ES: %s",
                            SerializationUtils.serializeToString(this)));
        }

        switch (identity.getDocumentType()) {
            case UsernameTypes.NIF:
                builder.setNifNumber(identity.getDocumentNumber());
                break;
            case UsernameTypes.NIE:
                builder.setNieNumber(identity.getDocumentNumber());
                break;
            case UsernameTypes.PASSPORT:
                builder.setPassportNumber(identity.getDocumentNumber());
                break;
            case UsernameTypes.OTHER_DOCUMENT:
            default:
                LOGGER.warn(
                        "Unknown document type {}, assuming passport", identity.getDocumentType());
                builder.setPassportNumber(identity.getDocumentNumber());
                break;
        }

        return builder.addFirstNameElement(clientName)
                .addSurnameElement(surnameOne.getSurname())
                .setDateOfBirth(LocalDate.parse(birthDate))
                .build();
    }
}
