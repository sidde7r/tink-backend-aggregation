package se.tink.backend.aggregation.agents.nxgen.dk.openbanking.danskebank.mapper;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.catchThrowable;

import java.util.Arrays;
import java.util.Collections;
import java.util.NoSuchElementException;
import org.junit.Before;
import org.junit.Test;
import se.tink.backend.aggregation.agents.nxgen.dk.openbanking.danskebank.DanskeDkConstants;
import se.tink.backend.aggregation.agents.nxgen.dk.openbanking.danskebank.fixtures.DanskeIdentifierFixtures;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.ukopenbanking.ais.base.entities.AccountIdentifierEntity;
import se.tink.libraries.mapper.PrioritizedValueExtractor;

public class DanskeDkIdentifierMapperTest {

    private DanskeDkIdentifierMapper danskeDkIdentifierMapper;
    private AccountIdentifierEntity bbanIdentifier;
    private AccountIdentifierEntity shortBbanIdentifier;
    private AccountIdentifierEntity danskeIbanIdentifier;
    private AccountIdentifierEntity shortDanskeIbanIdentifier;
    private AccountIdentifierEntity differentIbanIdentifier;
    private AccountIdentifierEntity shortDifferentIbanIdentifier;
    private AccountIdentifierEntity panIdentifier;
    private AccountIdentifierEntity shortPanIdentifier;

    @Before
    public void setUp() {
        PrioritizedValueExtractor valueExtractor = new PrioritizedValueExtractor();
        danskeDkIdentifierMapper = new DanskeDkIdentifierMapper(valueExtractor);
        bbanIdentifier = DanskeIdentifierFixtures.bbanIdentifier();
        shortBbanIdentifier = DanskeIdentifierFixtures.shortBbanIdentifier();
        danskeIbanIdentifier = DanskeIdentifierFixtures.danskeIbanIdentifier();
        shortDanskeIbanIdentifier = DanskeIdentifierFixtures.shortDanskeIbanIdentifier();
        differentIbanIdentifier = DanskeIdentifierFixtures.ibanIdentifier();
        shortDifferentIbanIdentifier = DanskeIdentifierFixtures.shortIbanIdentifier();
        panIdentifier = DanskeIdentifierFixtures.panIdentifier();
        shortPanIdentifier = DanskeIdentifierFixtures.shortPanIdentifier();
    }

    @Test
    public void shouldGetBbanAsTransactionalAccountPrimaryIdentifier() {
        // given when
        AccountIdentifierEntity returnedId =
                danskeDkIdentifierMapper.getTransactionalAccountPrimaryIdentifier(
                        Arrays.asList(DanskeIdentifierFixtures.panIdentifier(), bbanIdentifier));

        // then
        assertThat(returnedId).isEqualTo(bbanIdentifier);
    }

    @Test
    public void shouldGetIbanAsTransactionalAccountPrimaryIdentifier() {
        // given when
        AccountIdentifierEntity returnedId =
                danskeDkIdentifierMapper.getTransactionalAccountPrimaryIdentifier(
                        Arrays.asList(
                                DanskeIdentifierFixtures.panIdentifier(),
                                bbanIdentifier,
                                danskeIbanIdentifier));

        // then
        assertThat(returnedId).isEqualTo(danskeIbanIdentifier);
    }

    @Test
    public void shouldThrowExceptionWhenIdentifiersIsEmptyList() {
        // given when
        Throwable throwable =
                catchThrowable(
                        () ->
                                danskeDkIdentifierMapper.getTransactionalAccountPrimaryIdentifier(
                                        Collections.emptyList()));

        // then
        assertThat(throwable)
                .isInstanceOf(NoSuchElementException.class)
                .hasMessage(
                        "Could not extract account identifier. No available identifier with type of: IBAN, BBAN");
    }

    @Test
    public void shouldThrowExceptionWhenIdentifiersContainWrongIdentifier() {
        // given when
        Throwable throwable =
                catchThrowable(
                        () ->
                                danskeDkIdentifierMapper.getTransactionalAccountPrimaryIdentifier(
                                        Collections.singletonList(
                                                DanskeIdentifierFixtures.panIdentifier())));

        // then
        assertThat(throwable)
                .isInstanceOf(NoSuchElementException.class)
                .hasMessage(
                        "Could not extract account identifier. No available identifier with type of: IBAN, BBAN");
    }

    @Test
    public void shouldFormatBban() {
        // given when
        String result = danskeDkIdentifierMapper.getUniqueIdentifier(bbanIdentifier);

        // then
        assertThat(result).hasSize((DanskeDkConstants.ACCOUNT_NO_MIN_LENGTH));
        assertThat(bbanIdentifier.getIdentification()).contains(result);
    }

    @Test
    public void shouldFormatShortBban() {
        // given when
        String result = danskeDkIdentifierMapper.getUniqueIdentifier(shortBbanIdentifier);

        // then
        assertThat(result).hasSize(DanskeDkConstants.ACCOUNT_NO_MIN_LENGTH).startsWith("0");
    }

    @Test
    public void shouldFormatIban() {
        // given when
        String result = danskeDkIdentifierMapper.getUniqueIdentifier(danskeIbanIdentifier);

        // then
        assertThat(result).hasSize((DanskeDkConstants.ACCOUNT_NO_MIN_LENGTH));
        assertThat(danskeIbanIdentifier.getIdentification()).contains(result);
    }

    @Test
    public void shouldFormatShortIban() {
        // given when
        String result = danskeDkIdentifierMapper.getUniqueIdentifier(shortDanskeIbanIdentifier);

        // then
        assertThat(result).hasSize(DanskeDkConstants.ACCOUNT_NO_MIN_LENGTH).startsWith("0");
    }

    @Test
    public void shouldFormatIbanIdentifierInWrongFormat() {
        // given when
        String result = danskeDkIdentifierMapper.getUniqueIdentifier(differentIbanIdentifier);

        // then
        assertThat(result).hasSize((DanskeDkConstants.ACCOUNT_NO_MIN_LENGTH));
        assertThat(differentIbanIdentifier.getIdentification()).contains(result);
    }

    @Test
    public void shouldFormatShortIbanIdentifierInWrongFormat() {
        // given when
        String result = danskeDkIdentifierMapper.getUniqueIdentifier(shortDifferentIbanIdentifier);

        // then
        assertThat(result).hasSize((DanskeDkConstants.ACCOUNT_NO_MIN_LENGTH)).startsWith("0");
    }

    @Test
    public void shouldFormatOtherIdentifier() {
        // given when
        String result = danskeDkIdentifierMapper.getUniqueIdentifier(panIdentifier);

        // then
        assertThat(result).hasSize((DanskeDkConstants.ACCOUNT_NO_MIN_LENGTH));
        assertThat(panIdentifier.getIdentification()).contains(result);
    }

    @Test
    public void shouldFormatOtherShortIdentifier() {
        // given when
        String result = danskeDkIdentifierMapper.getUniqueIdentifier(shortPanIdentifier);

        // then
        assertThat(result)
                .hasSize((DanskeDkConstants.ACCOUNT_NO_MIN_LENGTH))
                .endsWith(shortPanIdentifier.getIdentification())
                .startsWith("0");
    }
}
