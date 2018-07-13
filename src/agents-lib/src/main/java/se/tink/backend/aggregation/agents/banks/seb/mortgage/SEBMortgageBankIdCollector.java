package se.tink.backend.aggregation.agents.banks.seb.mortgage;

import se.tink.backend.aggregation.agents.banks.seb.mortgage.model.GetLoanStatusSignResponse;

public interface SEBMortgageBankIdCollector {
    GetLoanStatusSignResponse.BankIdStatus collect(String applicationId);
}
