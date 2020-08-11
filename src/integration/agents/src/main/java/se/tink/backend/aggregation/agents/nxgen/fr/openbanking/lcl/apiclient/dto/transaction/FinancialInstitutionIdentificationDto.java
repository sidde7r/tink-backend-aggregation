package se.tink.backend.aggregation.agents.nxgen.fr.openbanking.lcl.apiclient.dto.transaction;

import lombok.Data;
import se.tink.backend.aggregation.annotations.JsonObject;

@JsonObject
@Data
public class FinancialInstitutionIdentificationDto {

    private String bicFi;

    private ClearingSystemMemberIdentificationDto clearingSystemMemberId;

    private String name;
}
