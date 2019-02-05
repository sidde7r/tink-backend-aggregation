package se.tink.backend.aggregation.agents.nxgen.be.banks.kbc.fetchers.dto;

import java.util.List;
import se.tink.backend.aggregation.agents.nxgen.be.banks.kbc.dto.TypeValuePair;
import se.tink.backend.aggregation.agents.nxgen.be.banks.kbc.dto.HeaderResponse;
import se.tink.backend.aggregation.annotations.JsonObject;

@JsonObject
public class AccountsResponse extends HeaderResponse {
    private TypeValuePair currentAccountsFilteredIndicator;
    private TypeValuePair savingsAccountsFilteredIndicator;
    private List<AgreementDto> agreements;

    public TypeValuePair getCurrentAccountsFilteredIndicator() {
        return currentAccountsFilteredIndicator;
    }

    public TypeValuePair getSavingsAccountsFilteredIndicator() {
        return savingsAccountsFilteredIndicator;
    }

    public List<AgreementDto> getAgreements() {
        return agreements;
    }
}
