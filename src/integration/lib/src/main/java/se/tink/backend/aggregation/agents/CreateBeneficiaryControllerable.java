package se.tink.backend.aggregation.agents;

import java.util.Optional;
import se.tink.backend.aggregation.nxgen.controllers.payment.CreateBeneficiaryController;

/**
 * This interface exists as a temporary remedy for the poor design decision of doing type
 * introspection for SubsequentGenerationAgent.
 */
public interface CreateBeneficiaryControllerable {
    Optional<CreateBeneficiaryController> getCreateBeneficiaryController();
}
