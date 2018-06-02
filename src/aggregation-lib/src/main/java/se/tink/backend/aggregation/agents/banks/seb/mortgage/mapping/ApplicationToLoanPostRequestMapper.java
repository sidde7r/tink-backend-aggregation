package se.tink.backend.aggregation.agents.banks.seb.mortgage.mapping;

import se.tink.backend.aggregation.agents.banks.seb.mortgage.model.LoanPostRequest;
import se.tink.libraries.application.GenericApplication;

public interface ApplicationToLoanPostRequestMapper {
    LoanPostRequest toLoanRequest(GenericApplication application);
}
