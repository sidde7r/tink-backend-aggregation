package se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.ukopenbanking.hsbc.fetcher;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static se.tink.backend.agents.rpc.AccountTypes.CHECKING;
import static se.tink.backend.agents.rpc.AccountTypes.CREDIT_CARD;

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
import org.mockito.MockitoAnnotations;
import org.mockito.junit.MockitoJUnitRunner;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.ukopenbanking.ais.base.UkOpenBankingApiClient;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.ukopenbanking.ais.base.entities.AccountEntity;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.ukopenbanking.ais.base.entities.PartyV31Entity;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.ukopenbanking.ais.base.interfaces.UkOpenBankingAisConfig;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.ukopenbanking.ais.v31.mapper.AccountTypeMapper;
import se.tink.backend.aggregation.nxgen.storage.PersistentStorage;
import se.tink.libraries.serialization.utils.SerializationUtils;
import src.integration.agents.src.main.java.se.tink.backend.aggregation.agents.nxgen.uk.openbanking.hsbc.fetcher.HsbcKineticPartyFetcher;

@RunWith(MockitoJUnitRunner.class)
public class HsbcKineticPartyFetcherTest {

    @Mock private UkOpenBankingAisConfig config;
    @Mock private PersistentStorage storage;
    @Mock private UkOpenBankingApiClient apiClient;
    @InjectMocks private HsbcKineticPartyFetcher hsbcFetcher;
    private List<PartyV31Entity> parties;
    private AccountEntity account;

    private static final String PARTIES =
            "[{\"PartyId\":\"PABC123\",\"PartyType\":\"SOLE\",\"Name\":\"Semiotec\",\"FullLegalName\":\"Semiotec Limited\",\"LegalStructure\":\"UK.OBIE.PrivateLimitedCompany\",\"BeneficialOwnership\":true,\"AccountRole\":\"UK.OBIE.Principal\",\"EmailAddress\":\"contact@semiotec.co.jp\",\"Relationships\":{\"Account\":{\"Related\":\"https://api.alphabank.com/open-banking/v4.0/aisp/accounts/22289\",\"Id\":\"22289\"}},\"Address\":[{\"AddressType\":\"Business\",\"StreetName\":\"Street\",\"BuildingNumber\":\"15\",\"PostCode\":\"NW1 1AB\",\"TownName\":\"London\",\"Country\":\"GB\"}]}]";
    private static final String CURRENT_ACCOUNT =
            "{\"AccountId\":\"xxxiddddxxxx\",\"Currency\":\"GBP\",\"AccountType\":\"Personal\",\"AccountSubType\":\"CurrentAccount\",\"Nickname\":\"someNickname\",\"Account\":[{\"SchemeName\":\"UK.OBIE.SortCodeAccountNumber\",\"Identification\":\"07111111111111\",\"Name\":\"Myszon Jelen\"},{\"SchemeName\":\"UK.OBIE.IBAN\",\"Identification\":\"1234 NAIA 5678 1234 55 1111\",\"Name\":\"Myszo Jelen\"}]}";

    @Before
    public void setUp() {
        MockitoAnnotations.openMocks(hsbcFetcher);
        parties =
                SerializationUtils.deserializeFromString(
                        PARTIES, new TypeReference<List<PartyV31Entity>>() {});
        account = SerializationUtils.deserializeFromString(CURRENT_ACCOUNT, AccountEntity.class);
    }

    @Test
    public void restoreIdentitiesFromPersistentStorageIfScaExpired() {
        // given
        given(config.isAccountPartyEndpointEnabled()).willReturn(true);
        given(storage.get(eq("last_SCA_time"), eq(String.class)))
                .willReturn(
                        Optional.of(
                                LocalDateTime.now()
                                        .minusDays(1)
                                        .toString())); // check if sca is expired
        given(storage.get(eq("recent_identity_data_list"), Mockito.any(TypeReference.class)))
                .willReturn(Optional.of(parties)); // restoreParties
        given(AccountTypeMapper.getAccountType(account))
                .willReturn(CHECKING); // isCreditCard -> false

        // when
        List<PartyV31Entity> result = hsbcFetcher.fetchAccountParties(account);

        // then
        assertThat(result.size()).isOne();
        assertThat(result.get(0)).isEqualTo(parties.get(0));
        verify(storage, times(1)).get(eq("last_SCA_time"), eq(String.class));
    }

    @Test
    public void shouldNotFetchNeitherRestoreIfScaExpiredAndNothingStored() {
        // given
        given(config.isAccountPartyEndpointEnabled()).willReturn(true);
        given(AccountTypeMapper.getAccountType(account))
                .willReturn(CREDIT_CARD); // isCreditCard -> true

        // when
        List<PartyV31Entity> result = hsbcFetcher.fetchAccountParties(account);

        // then
        assertThat(result.size()).isZero();
    }

    @Test
    public void shouldFetchPartiesWhenHappyPath() {
        // given
        given(config.isAccountPartyEndpointEnabled()).willReturn(true);
        given(storage.get(eq("last_SCA_time"), eq(String.class)))
                .willReturn(
                        Optional.of(
                                LocalDateTime.now()
                                        .minusSeconds(10)
                                        .toString())); // sca not expired
        given(storage.get(eq("recent_identity_data_list"), Mockito.any(TypeReference.class)))
                .willReturn(Optional.of(parties)); // restoreParties
        given(AccountTypeMapper.getAccountType(account))
                .willReturn(CHECKING); // isCreditCard -> false
        given(apiClient.fetchAccountParty(any())).willReturn(Optional.of(parties.get(0)));

        // when
        List<PartyV31Entity> result = hsbcFetcher.fetchAccountParties(account);

        // then
        assertThat(result.size()).isEqualTo(1);
        assertThat(result.get(0).getPartyId()).isEqualTo("PABC123");
    }
}
