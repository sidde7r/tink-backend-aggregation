package se.tink.backend.product.execution.unit.agents.seb.mortgage;

import se.tink.backend.product.execution.unit.agents.seb.mortgage.model.GetLoanStatusSignResponse;

public interface SEBMortgageBankIdCollector {
    GetLoanStatusSignResponse.BankIdStatus collect(String applicationId);
}
