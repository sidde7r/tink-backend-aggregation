package se.tink.backend.aggregation.agents.nxgen.dk.openbanking.danskebank.mapper;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.catchThrowable;

import java.util.Arrays;
import java.util.Collections;
import java.util.NoSuchElementException;
import org.junit.Before;
import org.junit.Test;
import se.tink.backend.aggregation.agents.nxgen.dk.openbanking.danskebank.DanskeConstants;
import se.tink.backend.aggregation.agents.nxgen.dk.openbanking.danskebank.fixtures.DanskeIdentifierFixtures;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.ukopenbanking.base.entities.AccountIdentifierEntity;
import se.tink.libraries.mapper.PrioritizedValueExtractor;

public class DanskeDkIdentifierMapperTest {

    private DanskeDkIdentifierMapper danskeDkIdentifierMapper;
    private AccountIdentifierEntity bbanIdentifier;
    private AccountIdentifierEntity shortBbanIdentifier;
    private AccountIdentifierEntity danskeIbanIdentifier;
    private AccountIdentifierEntity shortDanskeIbanIdentifier;
    private AccountIdentifierEntity differentIbanIdentifier;
    private AccountIdentifierEntity shortDifferentIbanIdentifier;

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
    }

    @Test
    public void shouldGetBbanAsTransactionalAccountPrimaryIdentifier() {
        // given when
        AccountIdentifierEntity returnedId =
                danskeDkIdentifierMapper.getTransactionalAccountPrimaryIdentifier(
                        Arrays.asList(bbanIdentifier, danskeIbanIdentifier),
                        DanskeConstants.ALLOWED_TRANSACTIONAL_ACCOUNT_IDENTIFIERS);

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
                        DanskeConstants.ALLOWED_TRANSACTIONAL_ACCOUNT_IDENTIFIERS);

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
                                        DanskeConstants.ALLOWED_TRANSACTIONAL_ACCOUNT_IDENTIFIERS));

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
                                        DanskeConstants.ALLOWED_TRANSACTIONAL_ACCOUNT_IDENTIFIERS));

        // then
        assertThat(throwable)
                .isInstanceOf(NoSuchElementException.class)
                .hasMessage(
                        "Could not extract account identifier. No available identifier with type of: BBAN, IBAN");
    }

    @Test
    public void shouldFormatBban() {
        // given when
        AccountIdentifierEntity returnedId =
                danskeDkIdentifierMapper.getTransactionalAccountPrimaryIdentifier(
                        Collections.singletonList(bbanIdentifier),
                        DanskeConstants.ALLOWED_TRANSACTIONAL_ACCOUNT_IDENTIFIERS);

        String result = danskeDkIdentifierMapper.formatIdentificationNumber(returnedId);

        // then
        assertThat(result).hasSize((DanskeConstants.ACCOUNT_NO_MIN_LENGTH));
    }

    @Test
    public void shouldFormatShortBban() {
        // given when
        AccountIdentifierEntity returnedId =
                danskeDkIdentifierMapper.getTransactionalAccountPrimaryIdentifier(
                        Collections.singletonList(shortBbanIdentifier),
                        DanskeConstants.ALLOWED_TRANSACTIONAL_ACCOUNT_IDENTIFIERS);

        String result = danskeDkIdentifierMapper.formatIdentificationNumber(returnedId);

        // then
        assertThat(result).hasSize(DanskeConstants.ACCOUNT_NO_MIN_LENGTH);
    }

    @Test
    public void shouldFormatIban() {
        // given when
        AccountIdentifierEntity returnedId =
                danskeDkIdentifierMapper.getTransactionalAccountPrimaryIdentifier(
                        Collections.singletonList(danskeIbanIdentifier),
                        DanskeConstants.ALLOWED_TRANSACTIONAL_ACCOUNT_IDENTIFIERS);

        String result = danskeDkIdentifierMapper.formatIdentificationNumber(returnedId);

        // then
        assertThat(result).hasSize((DanskeConstants.ACCOUNT_NO_MIN_LENGTH));
    }

    @Test
    public void shouldFormatShortIban() {
        // given when
        AccountIdentifierEntity returnedId =
                danskeDkIdentifierMapper.getTransactionalAccountPrimaryIdentifier(
                        Collections.singletonList(shortDanskeIbanIdentifier),
                        DanskeConstants.ALLOWED_TRANSACTIONAL_ACCOUNT_IDENTIFIERS);

        String result = danskeDkIdentifierMapper.formatIdentificationNumber(returnedId);

        // then
        assertThat(result).hasSize(DanskeConstants.ACCOUNT_NO_MIN_LENGTH);
    }

    @Test
    public void shouldFormatIbanIdentifierInWrongFormat() {
        // given when
        AccountIdentifierEntity returnedId =
                danskeDkIdentifierMapper.getTransactionalAccountPrimaryIdentifier(
                        Collections.singletonList(differentIbanIdentifier),
                        DanskeConstants.ALLOWED_TRANSACTIONAL_ACCOUNT_IDENTIFIERS);

        String result = danskeDkIdentifierMapper.formatIdentificationNumber(returnedId);

        // then
        assertThat(result).hasSize((DanskeConstants.ACCOUNT_NO_MIN_LENGTH));
    }

    @Test
    public void shouldFormatShortIbanIdentifierInWrongFormat() {
        // given when
        AccountIdentifierEntity returnedId =
                danskeDkIdentifierMapper.getTransactionalAccountPrimaryIdentifier(
                        Collections.singletonList(shortDifferentIbanIdentifier),
                        DanskeConstants.ALLOWED_TRANSACTIONAL_ACCOUNT_IDENTIFIERS);

        String result = danskeDkIdentifierMapper.formatIdentificationNumber(returnedId);

        // then
        assertThat(result).hasSize((DanskeConstants.ACCOUNT_NO_MIN_LENGTH));
    }
}
