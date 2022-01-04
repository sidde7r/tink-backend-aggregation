package se.tink.backend.aggregation.agents.nxgen.de.openbanking.fintecsystems.fetcher;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.Optional;
import org.junit.Test;
import se.tink.backend.agents.rpc.AccountTypes;
import se.tink.backend.aggregation.agents.nxgen.de.openbanking.fintecsystems.TestDataReader;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.fintecsystems.fetcher.FinTecSystemsReportMapper;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.fintecsystems.fetcher.data.FinTecSystemsReport;
import se.tink.backend.aggregation.nxgen.core.account.entity.Party;
import se.tink.backend.aggregation.nxgen.core.account.entity.Party.Role;
import se.tink.backend.aggregation.nxgen.core.account.transactional.TransactionalAccount;
import se.tink.libraries.account.identifiers.IbanIdentifier;

public class FinTecSystemsReportMapperTest {

    private static final String TEST_IBAN = "DE62888888880012345678";
    private static final String TEST_HOLDER = "Mustermann, Hartmut";

    @Test
    public void shouldTransformReportCorrectly() {
        // given
        FinTecSystemsReport finTecSystemsReport =
                TestDataReader.readFromFile(TestDataReader.REPORT, FinTecSystemsReport.class);
        FinTecSystemsReportMapper mapper = new FinTecSystemsReportMapper();

        // when
        Optional<TransactionalAccount> maybeTransactionalAccount =
                mapper.transformReportToTinkAccount(finTecSystemsReport);

        // then
        assertThat(maybeTransactionalAccount).isPresent();

        TransactionalAccount account = maybeTransactionalAccount.get();

        assertThat(account.getName()).isEqualTo(TEST_IBAN);
        assertThat(account.getAccountNumber()).isEqualTo(TEST_IBAN);
        assertThat(account.getUniqueIdentifier()).isEqualTo(TEST_IBAN);
        assertThat(account.getType()).isEqualTo(AccountTypes.CHECKING);
        assertThat(account.getIdentifiersAsList()).containsExactly(new IbanIdentifier(TEST_IBAN));
        assertThat(account.getParties()).containsExactly(new Party(TEST_HOLDER, Role.HOLDER));
    }
}
