package se.tink.backend.aggregation.agents.nxgen.it.openbanking.unicredit.fetcher;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;
import junitparams.JUnitParamsRunner;
import junitparams.Parameters;
import org.junit.Test;
import org.junit.runner.RunWith;
import se.tink.backend.aggregation.nxgen.core.account.entity.Party;
import se.tink.backend.aggregation.nxgen.core.account.entity.Party.Role;

@RunWith(JUnitParamsRunner.class)
public class UnicreditITTransactionalAccountMapperTest {

    @Test
    @Parameters(method = "input")
    public void shouldParseOwnerNamesIntoExpectedParties(
            String inputOwnerName, List<Party> expectedParties) {
        // given
        UnicreditITTransactionalAccountMapper mapper = new UnicreditITTransactionalAccountMapper();

        // when
        List<Party> parties = mapper.parseOwnerName(inputOwnerName);

        // then
        assertThat(parties).containsExactlyInAnyOrderElementsOf(expectedParties);
    }

    private Object[] input() {
        return new Object[] {
            new Object[] {"A e B", toHolders("A", "B")},
            new Object[] {"A, B", toHolders("A", "B")},
            new Object[] {"A e B e C", toHolders("A", "B", "C")},
            new Object[] {"A E B", toHolders("A", "B")},
            new Object[] {"Aa Bb, Cc Dd, Ee Ff", toHolders("Aa Bb", "Cc Dd", "Ee Ff")},
            new Object[] {"Aa,b", toHolders("Aa,b")},
            new Object[] {"Aa eb", toHolders("Aa Eb")},
        };
    }

    private List<Party> toHolders(String... names) {
        return Arrays.stream(names)
                .map(name -> new Party(name, Role.HOLDER))
                .collect(Collectors.toList());
    }
}
