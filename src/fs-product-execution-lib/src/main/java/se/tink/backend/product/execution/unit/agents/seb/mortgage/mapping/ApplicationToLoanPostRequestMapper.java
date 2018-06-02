package se.tink.backend.product.execution.unit.agents.seb.mortgage.mapping;

import se.tink.backend.product.execution.unit.agents.seb.mortgage.model.LoanPostRequest;
import se.tink.libraries.application.GenericApplication;

public interface ApplicationToLoanPostRequestMapper {
    LoanPostRequest toLoanRequest(GenericApplication application);
}
