package se.tink.backend.aggregation.agents;

import java.util.List;
import se.tink.backend.agents.rpc.Account;

public interface RefreshBeneficiariesExecutor extends CapabilityExecutor {

    /* TODO: In the future, when splitting Transfer Destinations from Beneficiaries change to the custom Beneficiary response. */
    FetchTransferDestinationsResponse fetchBeneficiaries(List<Account> accounts);
}
