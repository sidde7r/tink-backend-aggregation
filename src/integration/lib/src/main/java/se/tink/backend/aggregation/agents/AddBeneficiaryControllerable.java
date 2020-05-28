package se.tink.backend.aggregation.agents;

import java.util.Optional;
import se.tink.backend.aggregation.nxgen.controllers.payment.AddBeneficiaryController;

/**
 * This interface exists as a temporary remedy for the poor design decision of doing type
 * introspection for SubsequentGenerationAgent.
 */
public interface AddBeneficiaryControllerable {
    Optional<AddBeneficiaryController> getAddBeneficiaryController();
}
