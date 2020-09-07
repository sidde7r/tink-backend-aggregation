package se.tink.backend.aggregation.agents.nxgen.fr.banks.banquepopulaire.apiclient.dto.transaction;

import lombok.Data;
import lombok.EqualsAndHashCode;
import se.tink.backend.aggregation.agents.nxgen.fr.banks.banquepopulaire.apiclient.dto.common.TypeDto;
import se.tink.backend.aggregation.annotations.JsonObject;

@EqualsAndHashCode(callSuper = true)
@Data
@JsonObject
public class CodeOperationDto extends TypeDto {
    private String sensOperation;
}
