package se.tink.backend.aggregation.agents.nxgen.fr.openbanking.lcl.apiclient.dto.transaction;

import lombok.Data;
import se.tink.backend.aggregation.annotations.JsonObject;

@JsonObject
@Data
public class BankTransactionCodeDto {

    private String code;

    private String domain;

    private String family;

    private String subFamily;
}
