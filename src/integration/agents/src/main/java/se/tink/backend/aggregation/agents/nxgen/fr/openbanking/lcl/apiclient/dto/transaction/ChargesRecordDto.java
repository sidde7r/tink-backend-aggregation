package se.tink.backend.aggregation.agents.nxgen.fr.openbanking.lcl.apiclient.dto.transaction;

import lombok.Data;
import se.tink.backend.aggregation.agents.nxgen.fr.openbanking.lcl.apiclient.dto.common.AmountDto;
import se.tink.backend.aggregation.annotations.JsonObject;

@JsonObject
@Data
public class ChargesRecordDto {

    private FinancialInstitutionIdentificationDto agent;

    private AmountDto amount;

    private ChargeBearerCode bearer;

    private Boolean chargeIncludedIndicator;

    private CodeAndIssuerDto code;

    private CreditDebitIndicator creditDebitIndicator;

    private String rate;

    private TaxChargesDto tax;
}
