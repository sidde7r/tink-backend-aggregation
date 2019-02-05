package se.tink.backend.aggregation.agents.nxgen.be.banks.kbc.fetchers.dto;

import se.tink.backend.aggregation.agents.general.models.GeneralAccountEntity;
import se.tink.backend.aggregation.agents.nxgen.be.banks.kbc.dto.TypeValuePair;
import se.tink.backend.aggregation.annotations.JsonObject;
import se.tink.libraries.account.AccountIdentifier;
import se.tink.libraries.account.identifiers.SepaEurIdentifier;

@JsonObject
public class BeneficiaryDto implements GeneralAccountEntity {
    private TypeValuePair accountNo;
    private TypeValuePair bic;
    private TypeValuePair beneficiaryName;

    @Override
    public AccountIdentifier generalGetAccountIdentifier() {
        return new SepaEurIdentifier(accountNo.getValue());
    }

    @Override
    public String generalGetBank() {
        return "";
    }

    @Override
    public String generalGetName() {
        return beneficiaryName.getValue();
    }
}
