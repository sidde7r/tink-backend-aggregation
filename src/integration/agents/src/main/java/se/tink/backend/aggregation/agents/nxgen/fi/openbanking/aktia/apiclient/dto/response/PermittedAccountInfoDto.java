package se.tink.backend.aggregation.agents.nxgen.fi.openbanking.aktia.apiclient.dto.response;

import java.math.BigDecimal;
import lombok.Data;
import se.tink.backend.aggregation.annotations.JsonObject;

@JsonObject
@Data
public class PermittedAccountInfoDto {

    private String id;

    private String name;

    private String iban;

    private BigDecimal balance;
}
