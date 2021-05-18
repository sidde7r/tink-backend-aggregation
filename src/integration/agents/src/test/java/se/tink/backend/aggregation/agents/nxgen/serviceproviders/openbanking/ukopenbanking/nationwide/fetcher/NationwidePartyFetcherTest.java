package se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.ukopenbanking.nationwide.fetcher;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.mock;

import com.fasterxml.jackson.core.type.TypeReference;
import java.util.Collections;
import java.util.List;
import org.junit.Before;
import org.junit.Test;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.ukopenbanking.ais.base.PartyDataStorage;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.ukopenbanking.ais.base.ScaExpirationValidator;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.ukopenbanking.ais.base.UkOpenBankingApiClient;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.ukopenbanking.ais.base.entities.AccountEntity;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.ukopenbanking.ais.base.entities.PartyV31Entity;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.ukopenbanking.ais.base.interfaces.UkOpenBankingAisConfig;
import se.tink.backend.aggregation.agents.nxgen.uk.openbanking.nationwide.fetcher.NationwidePartyFetcher;
import se.tink.libraries.serialization.utils.SerializationUtils;

public class NationwidePartyFetcherTest {

    private NationwidePartyFetcher nationwideFetcher;
    private ScaExpirationValidator scaValidator;
    private PartyDataStorage storage;
    private UkOpenBankingAisConfig config;

    private List<PartyV31Entity> parties;
    private AccountEntity account;

    @Before
    public void setUp() {
        config = mock(UkOpenBankingAisConfig.class);
        scaValidator = mock(ScaExpirationValidator.class);
        storage = mock(PartyDataStorage.class);

        nationwideFetcher =
                new NationwidePartyFetcher(
                        mock(UkOpenBankingApiClient.class), config, storage, scaValidator);

        final String PARTIES =
                "[{\"PartyId\":\"PABC123\",\"PartyType\":\"Sole\",\"Name\":\"Semiotec\",\"FullLegalName\":\"Semiotec Limited\",\"LegalStructure\":\"UK.OBIE.PrivateLimitedCompany\",\"BeneficialOwnership\":true,\"AccountRole\":\"UK.OBIE.Principal\",\"EmailAddress\":\"contact@semiotec.co.jp\",\"Relationships\":{\"Account\":{\"Related\":\"https://api.alphabank.com/open-banking/v4.0/aisp/accounts/22289\",\"Id\":\"22289\"}},\"Address\":[{\"AddressType\":\"Business\",\"StreetName\":\"Street\",\"BuildingNumber\":\"15\",\"PostCode\":\"NW1 1AB\",\"TownName\":\"London\",\"Country\":\"GB\"}]},{\"PartyId\":\"PXSIF023\",\"PartyNumber\":\"0000007456\",\"PartyType\":\"Delegate\",\"Name\":\"Kevin Atkinson\",\"FullLegalName\":\"Mr Kevin Bartholmew Atkinson\",\"LegalStructure\":\"UK.OBIE.Individual\",\"BeneficialOwnership\":false,\"AccountRole\":\"UK.OBIE.Administrator\",\"EmailAddress\":\"kev@semiotec.co.jp\",\"Relationships\":{\"Account\":{\"Related\":\"https://api.alphabank.com/open-banking/v4.0/aisp/accounts/22289\",\"Id\":\"22289\"}}}]";

        parties =
                SerializationUtils.deserializeFromString(
                        PARTIES, new TypeReference<List<PartyV31Entity>>() {});

        final String CURRENT_ACCOUNT =
                "{\"AccountId\":\"xxxiddddxxxx\",\"Currency\":\"GBP\",\"AccountType\":\"Personal\",\"AccountSubType\":\"CurrentAccount\",\"Nickname\":\"someNickname\",\"Account\":[{\"SchemeName\":\"UK.OBIE.SortCodeAccountNumber\",\"Identification\":\"07111111111111\",\"Name\":\"Myszon Jelen\"},{\"SchemeName\":\"UK.OBIE.IBAN\",\"Identification\":\"1234 NAIA 5678 1234 55 1111\",\"Name\":\"Myszo Jelen\"}]}";
        account = SerializationUtils.deserializeFromString(CURRENT_ACCOUNT, AccountEntity.class);
    }

    @Test
    public void restoreIdentitiesFromPersistentStorageIfScaExpired() {
        // given
        given(config.isAccountPartiesEndpointEnabled()).willReturn(true);
        given(scaValidator.isScaExpired()).willReturn(true);
        given(storage.restoreParties()).willReturn(parties);

        // when
        List<PartyV31Entity> result = nationwideFetcher.fetchAccountParties(account);

        // then
        assertThat(result.size()).isEqualTo(2);
        assertThat(result.get(0)).isEqualTo(parties.get(0));
        assertThat(result.get(1)).isEqualTo(parties.get(1));
    }

    @Test
    public void shouldNotFetchNeitherRestoreIfScaExpiredAndNothingStored() {
        // given
        given(config.isAccountPartiesEndpointEnabled()).willReturn(true);
        given(scaValidator.isScaExpired()).willReturn(true);
        given(storage.restoreParties()).willReturn(Collections.emptyList());

        // when
        List<PartyV31Entity> result = nationwideFetcher.fetchAccountParties(account);

        // then
        assertThat(result.size()).isZero();
    }
}
