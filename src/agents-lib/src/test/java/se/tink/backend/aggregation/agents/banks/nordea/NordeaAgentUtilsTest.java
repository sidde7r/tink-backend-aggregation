package se.tink.backend.aggregation.agents.banks.nordea;

import org.junit.Test;
import se.tink.backend.system.rpc.Loan;
import static org.assertj.core.api.Assertions.assertThat;

public class NordeaAgentUtilsTest {
    @Test
    public void mapLoanTypes() {
        // Non-loan types
        assertThat(NordeaAgentUtils.getLoanTypeForCode(null)).isEqualTo(Loan.Type.OTHER);
        assertThat(NordeaAgentUtils.getLoanTypeForCode("")).isEqualTo(Loan.Type.OTHER);
        assertThat(NordeaAgentUtils.getLoanTypeForCode("FI0339")).isEqualTo(Loan.Type.OTHER); // Growth Account (CHECKING)

        // Loan types
        assertThat(NordeaAgentUtils.getLoanTypeForCode("SE00091")).isEqualTo(Loan.Type.OTHER); // Låna spar
        assertThat(NordeaAgentUtils.getLoanTypeForCode("SE00200")).isEqualTo(Loan.Type.MORTGAGE); // Bolån
        assertThat(NordeaAgentUtils.getLoanTypeForCode("SE10102")).isEqualTo(Loan.Type.VEHICLE); // Bil- och Fritidskredit
    }
}
