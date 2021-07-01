package se.tink.backend.aggregation.agents.nxgen.fr.openbanking.boursorama.fetcher;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.List;
import org.junit.Before;
import org.junit.Test;
import se.tink.backend.aggregation.nxgen.core.account.entity.Party;

public class BoursoramaHolderNamesExtractorTest {

    private BoursoramaHolderNamesExtractor objectUnderTest;

    @Before
    public void setUp() {
        objectUnderTest = new BoursoramaHolderNamesExtractor();
    }

    @Test
    public void shouldReturnSingleHolderName() {
        // given
        String name = "M OU MME NAME SURNAME";

        // when
        List<Party> holderNames = objectUnderTest.extract(name);

        // then
        assertThat(holderNames).hasSize(1);
    }

    @Test
    public void shouldReturnMultipleHolderNames() {
        // given
        String name = "NAME SURNAME / NAME2 SURNAME2";

        // when
        List<Party> holderNames = objectUnderTest.extract(name);

        // then
        assertThat(holderNames).hasSize(2);
    }
}
