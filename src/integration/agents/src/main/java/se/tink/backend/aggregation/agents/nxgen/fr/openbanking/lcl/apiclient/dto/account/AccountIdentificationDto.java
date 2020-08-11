package se.tink.backend.aggregation.agents.nxgen.fr.openbanking.lcl.apiclient.dto.account;

import lombok.Data;
import se.tink.backend.aggregation.annotations.JsonObject;

@JsonObject
@Data
public class AccountIdentificationDto {

    private String currency;

    private String iban;

    private GenericIdentificationDto other;
}
