package se.tink.backend.aggregation.agents.nxgen.fi.openbanking.aktia.apiclient.dto.response;

import lombok.Data;
import se.tink.backend.aggregation.agents.nxgen.fi.openbanking.aktia.apiclient.dto.data.AccountCategoryCode;
import se.tink.backend.aggregation.annotations.JsonObject;

@JsonObject
@Data
public class AccountTypeDto {

    private String accountType;

    private AccountCategoryCode categoryCode;

    private String productCode;

    private boolean longTermSavings;
}
