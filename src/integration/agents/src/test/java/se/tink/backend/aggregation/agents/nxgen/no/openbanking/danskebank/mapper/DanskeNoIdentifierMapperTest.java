package se.tink.backend.aggregation.agents.nxgen.no.openbanking.danskebank.mapper;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.Before;
import org.junit.Test;
import se.tink.backend.aggregation.agents.nxgen.no.openbanking.danskebank.DanskeNoConstants;
import se.tink.backend.aggregation.agents.nxgen.no.openbanking.danskebank.mapper.fixtures.DanskeNoIdentifierFixtures;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.ukopenbanking.ais.base.entities.AccountIdentifierEntity;
import se.tink.libraries.mapper.PrioritizedValueExtractor;

public class DanskeNoIdentifierMapperTest {

    private DanskeNoIdentifierMapper danskeNoIdentifierMapper;
    private AccountIdentifierEntity danskeNoBban;
    private AccountIdentifierEntity danskeNoIban;
    private AccountIdentifierEntity otherIbanIdentifier;
    private AccountIdentifierEntity panIdentifier;

    @Before
    public void setUp() {
        PrioritizedValueExtractor valueExtractor = new PrioritizedValueExtractor();
        danskeNoIdentifierMapper = new DanskeNoIdentifierMapper(valueExtractor);
        danskeNoBban = DanskeNoIdentifierFixtures.danskeNoBban();
        danskeNoIban = DanskeNoIdentifierFixtures.danskeNoIban();
        otherIbanIdentifier = DanskeNoIdentifierFixtures.otherIbanIdentifier();
        panIdentifier = DanskeNoIdentifierFixtures.panIdentifier();
    }

    @Test
    public void shouldFormatBban() {
        // given when
        String result = danskeNoIdentifierMapper.formatIdentificationNumber(danskeNoBban);

        // then
        assertThat(result).isEqualTo(danskeNoBban.getIdentification());
    }

    @Test
    public void shouldFormatIban() {
        // given when
        String result = danskeNoIdentifierMapper.formatIdentificationNumber(danskeNoIban);

        // then
        assertThat(result).hasSize((DanskeNoConstants.ACCOUNT_NO_MIN_LENGTH));
        assertThat(danskeNoIban.getIdentification()).endsWith(result);
    }

    @Test
    public void shouldFormatIbanIdentifierInWrongFormat() {
        // given when
        String result = danskeNoIdentifierMapper.formatIdentificationNumber(otherIbanIdentifier);

        // then
        assertThat(result).hasSize((DanskeNoConstants.ACCOUNT_NO_MIN_LENGTH));
        assertThat(otherIbanIdentifier.getIdentification()).endsWith(result);
    }

    @Test
    public void shouldFormatOtherIdentifier() {
        // given when
        String result = danskeNoIdentifierMapper.formatIdentificationNumber(panIdentifier);

        // then
        assertThat(result).hasSize((DanskeNoConstants.ACCOUNT_NO_MIN_LENGTH));
        assertThat(panIdentifier.getIdentification()).endsWith(result);
    }
}
