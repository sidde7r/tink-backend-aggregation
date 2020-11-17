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
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.danskebank.DanskebankV31Constant;
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
                        Arrays.asList(bbanIdentifier, danskeIbanIdentifier),
                        DanskebankV31Constant.ALLOWED_TRANSACTIONAL_ACCOUNT_IDENTIFIERS);

        // then
        assertThat(returnedId).isEqualTo(bbanIdentifier);
    }

    @Test
    public void shouldGetIbanAsTransactionalAccountPrimaryIdentifier() {
        // given when
        AccountIdentifierEntity returnedId =
                danskeDkIdentifierMapper.getTransactionalAccountPrimaryIdentifier(
                        Arrays.asList(
                                DanskeIdentifierFixtures.panIdentifier(), danskeIbanIdentifier),
                        DanskebankV31Constant.ALLOWED_TRANSACTIONAL_ACCOUNT_IDENTIFIERS);

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
                                        Collections.emptyList(),
                                        DanskebankV31Constant
                                                .ALLOWED_TRANSACTIONAL_ACCOUNT_IDENTIFIERS));

        // then
        assertThat(throwable)
                .isInstanceOf(NoSuchElementException.class)
                .hasMessage(
                        "Could not extract account identifier. No available identifier with type of: BBAN, IBAN");
    }

    @Test
    public void shouldThrowExceptionWhenIdentifiersContainWrongIdentifier() {
        // given when
        Throwable throwable =
                catchThrowable(
                        () ->
                                danskeDkIdentifierMapper.getTransactionalAccountPrimaryIdentifier(
                                        Collections.singletonList(
                                                DanskeIdentifierFixtures.panIdentifier()),
                                        DanskebankV31Constant
                                                .ALLOWED_TRANSACTIONAL_ACCOUNT_IDENTIFIERS));

        // then
        assertThat(throwable)
                .isInstanceOf(NoSuchElementException.class)
                .hasMessage(
                        "Could not extract account identifier. No available identifier with type of: BBAN, IBAN");
    }

    @Test
    public void shouldFormatBban() {
        // given when
        String result = danskeDkIdentifierMapper.formatIdentificationNumber(bbanIdentifier);

        // then
        assertThat(result).hasSize((DanskeDkConstants.ACCOUNT_NO_MIN_LENGTH));
        assertThat(bbanIdentifier.getIdentification()).contains(result);
    }

    @Test
    public void shouldFormatShortBban() {
        // given when
        String result = danskeDkIdentifierMapper.formatIdentificationNumber(shortBbanIdentifier);

        // then
        assertThat(result).hasSize(DanskeDkConstants.ACCOUNT_NO_MIN_LENGTH).startsWith("0");
    }

    @Test
    public void shouldFormatIban() {
        // given when
        String result = danskeDkIdentifierMapper.formatIdentificationNumber(danskeIbanIdentifier);

        // then
        assertThat(result).hasSize((DanskeDkConstants.ACCOUNT_NO_MIN_LENGTH));
        assertThat(danskeIbanIdentifier.getIdentification()).contains(result);
    }

    @Test
    public void shouldFormatShortIban() {
        // given when
        String result =
                danskeDkIdentifierMapper.formatIdentificationNumber(shortDanskeIbanIdentifier);

        // then
        assertThat(result).hasSize(DanskeDkConstants.ACCOUNT_NO_MIN_LENGTH).startsWith("0");
    }

    @Test
    public void shouldFormatIbanIdentifierInWrongFormat() {
        // given when
        String result =
                danskeDkIdentifierMapper.formatIdentificationNumber(differentIbanIdentifier);

        // then
        assertThat(result).hasSize((DanskeDkConstants.ACCOUNT_NO_MIN_LENGTH));
        assertThat(differentIbanIdentifier.getIdentification()).contains(result);
    }

    @Test
    public void shouldFormatShortIbanIdentifierInWrongFormat() {
        // given when
        String result =
                danskeDkIdentifierMapper.formatIdentificationNumber(shortDifferentIbanIdentifier);

        // then
        assertThat(result).hasSize((DanskeDkConstants.ACCOUNT_NO_MIN_LENGTH)).startsWith("0");
    }

    @Test
    public void shouldFormatOtherIdentifier() {
        // given when
        String result = danskeDkIdentifierMapper.formatIdentificationNumber(panIdentifier);

        // then
        assertThat(result).hasSize((DanskeDkConstants.ACCOUNT_NO_MIN_LENGTH));
        assertThat(panIdentifier.getIdentification()).contains(result);
    }

    @Test
    public void shouldFormatOtherShortIdentifier() {
        // given when
        String result = danskeDkIdentifierMapper.formatIdentificationNumber(shortPanIdentifier);

        // then
        assertThat(result)
                .hasSize((DanskeDkConstants.ACCOUNT_NO_MIN_LENGTH))
                .endsWith(shortPanIdentifier.getIdentification())
                .startsWith("0");
    }
}
