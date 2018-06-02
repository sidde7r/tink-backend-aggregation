package se.tink.backend.api;

import se.tink.backend.core.User;
import se.tink.backend.rpc.InvestmentResponse;

public interface InvestmentService {

    InvestmentResponse getInvestments(User user);
}
