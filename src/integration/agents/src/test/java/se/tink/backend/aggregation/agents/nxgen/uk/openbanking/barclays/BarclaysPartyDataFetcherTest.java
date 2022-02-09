package se.tink.backend.aggregation.agents.nxgen.uk.openbanking.barclays;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verifyNoInteractions;

import com.fasterxml.jackson.core.type.TypeReference;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.MockitoJUnitRunner;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.ukopenbanking.ais.base.UkOpenBankingApiClient;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.ukopenbanking.ais.base.entities.AccountEntity;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.ukopenbanking.ais.base.entities.PartyV31Entity;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.ukopenbanking.ais.base.interfaces.PartyFetcher;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.ukopenbanking.ais.base.interfaces.UkOpenBankingAisConfig;
import se.tink.backend.aggregation.agents.nxgen.uk.openbanking.barclays.fetcher.BarclaysPartyFetcher;
import se.tink.backend.aggregation.nxgen.storage.PersistentStorage;
import se.tink.libraries.serialization.utils.SerializationUtils;

@RunWith(MockitoJUnitRunner.class)
public class BarclaysPartyDataFetcherTest {

    @Mock private PartyFetcher baseFetcher;
    @Mock private UkOpenBankingAisConfig config;
    @Mock private PersistentStorage storage;
    @InjectMocks private BarclaysPartyFetcher barclaysFetcher;

    private List<PartyV31Entity> parties;

    @Before
    public void setUp() {
        barclaysFetcher =
                new BarclaysPartyFetcher(mock(UkOpenBankingApiClient.class), config, storage);

        final String PARTIES =
                "[{\"PartyId\":\"PABC123\",\"PartyType\":\"Sole\",\"Name\":\"Semiotec\",\"FullLegalName\":\"Semiotec Limited\",\"LegalStructure\":\"UK.OBIE.PrivateLimitedCompany\",\"BeneficialOwnership\":true,\"AccountRole\":\"UK.OBIE.Principal\",\"EmailAddress\":\"contact@semiotec.co.jp\",\"Relationships\":{\"Account\":{\"Related\":\"https://api.alphabank.com/open-banking/v4.0/aisp/accounts/22289\",\"Id\":\"22289\"}},\"Address\":[{\"AddressType\":\"Business\",\"StreetName\":\"Street\",\"BuildingNumber\":\"15\",\"PostCode\":\"NW1 1AB\",\"TownName\":\"London\",\"Country\":\"GB\"}]},{\"PartyId\":\"PXSIF023\",\"PartyNumber\":\"0000007456\",\"PartyType\":\"Delegate\",\"Name\":\"Kevin Atkinson\",\"FullLegalName\":\"Mr Kevin Bartholmew Atkinson\",\"LegalStructure\":\"UK.OBIE.Individual\",\"BeneficialOwnership\":false,\"AccountRole\":\"UK.OBIE.Administrator\",\"EmailAddress\":\"kev@semiotec.co.jp\",\"Relationships\":{\"Account\":{\"Related\":\"https://api.alphabank.com/open-banking/v4.0/aisp/accounts/22289\",\"Id\":\"22289\"}}}]";

        parties =
                SerializationUtils.deserializeFromString(
                        PARTIES, new TypeReference<List<PartyV31Entity>>() {});
    }

    @Test
    public void shouldNotFetchDataForCreditCards() {
        // given
        final String CREDIT_CARD_ACCOUNT =
                "{\"AccountId\":\"xxxiddddxxxx\",\"Currency\":\"GBP\",\"AccountType\":\"Personal\",\"AccountSubType\":\"CreditCard\",\"Nickname\":\"someNickname\",\"Account\":[{\"SchemeName\":\"UK.OBIE.SortCodeAccountNumber\",\"Identification\":\"07111111111111\",\"Name\":\"Myszon Jelen\"},{\"SchemeName\":\"UK.OBIE.IBAN\",\"Identification\":\"1234 NAIA 5678 1234 55 1111\",\"Name\":\"Myszo Jelen\"}]}";
        AccountEntity card =
                SerializationUtils.deserializeFromString(CREDIT_CARD_ACCOUNT, AccountEntity.class);

        // when
        List<PartyV31Entity> result = barclaysFetcher.fetchAccountParties(card);

        // then
        assertThat(result).isEmpty();
        verifyNoInteractions(baseFetcher);
    }

    @Test
    public void shouldNotFetchDataForBusinessAccounts() {
        // given
        final String CURRENT_BUSINESS_ACCOUNT =
                "{\"AccountId\":\"xxxiddddxxxx\",\"Currency\":\"GBP\",\"AccountType\":\"Business\",\"AccountSubType\":\"CurrentAccount\",\"Nickname\":\"someNickname\",\"Account\":[{\"SchemeName\":\"UK.OBIE.SortCodeAccountNumber\",\"Identification\":\"07111111111111\",\"Name\":\"Myszon Jelen\"},{\"SchemeName\":\"UK.OBIE.IBAN\",\"Identification\":\"1234 NAIA 5678 1234 55 1111\",\"Name\":\"Myszo Jelen\"}]}";
        AccountEntity account =
                SerializationUtils.deserializeFromString(
                        CURRENT_BUSINESS_ACCOUNT, AccountEntity.class);

        // when
        List<PartyV31Entity> result = barclaysFetcher.fetchAccountParties(account);

        // then
        assertThat(result).isEmpty();
        verifyNoInteractions(baseFetcher);
    }

    @Test
    public void shouldNotFetchDataForAccountsWithoutAccountType() {
        // given
        final String CURRENT_ACCOUNT =
                "{\"AccountId\":\"xxxiddddxxxx\",\"Currency\":\"GBP\",\"AccountSubType\":\"CurrentAccount\",\"Nickname\":\"someNickname\",\"Account\":[{\"SchemeName\":\"UK.OBIE.SortCodeAccountNumber\",\"Identification\":\"07111111111111\",\"Name\":\"Myszon Jelen\"},{\"SchemeName\":\"UK.OBIE.IBAN\",\"Identification\":\"1234 NAIA 5678 1234 55 1111\",\"Name\":\"Myszo Jelen\"}]}";
        AccountEntity account =
                SerializationUtils.deserializeFromString(CURRENT_ACCOUNT, AccountEntity.class);

        // when
        List<PartyV31Entity> result = barclaysFetcher.fetchAccountParties(account);

        // then
        assertThat(result).isEmpty();
        verifyNoInteractions(baseFetcher);
    }

    @Test
    public void restoreIdentitiesFromPersistentStorageIfScaExpired() {
        // given
        final String CURRENT_ACCOUNT =
                "{\"AccountId\":\"xxxiddddxxxx\",\"Currency\":\"GBP\",\"AccountType\":\"Personal\",\"AccountSubType\":\"CurrentAccount\",\"Nickname\":\"someNickname\",\"Account\":[{\"SchemeName\":\"UK.OBIE.SortCodeAccountNumber\",\"Identification\":\"07111111111111\",\"Name\":\"Myszon Jelen\"},{\"SchemeName\":\"UK.OBIE.IBAN\",\"Identification\":\"1234 NAIA 5678 1234 55 1111\",\"Name\":\"Myszo Jelen\"}]}";
        AccountEntity account =
                SerializationUtils.deserializeFromString(CURRENT_ACCOUNT, AccountEntity.class);

        given(storage.get(eq("last_SCA_time"), eq(String.class)))
                .willReturn(
                        Optional.of(
                                LocalDateTime.now().minusDays(1).toString())); //  sca is expired
        given(storage.get(eq("recent_identity_data_list"), Mockito.any(TypeReference.class)))
                .willReturn(Optional.of(parties)); // restoreParties

        // when
        List<PartyV31Entity> result = barclaysFetcher.fetchAccountParties(account);

        // then
        assertThat(result.size()).isEqualTo(2);
        assertThat(result.get(0)).isEqualTo(parties.get(0));
        assertThat(result.get(1)).isEqualTo(parties.get(1));
    }
}
