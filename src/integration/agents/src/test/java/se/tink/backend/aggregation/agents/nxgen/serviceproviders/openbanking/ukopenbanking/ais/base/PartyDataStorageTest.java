package se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.ukopenbanking.ais.base;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.Collections;
import java.util.List;
import java.util.Optional;
import org.junit.Before;
import org.junit.Test;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.ukopenbanking.ais.base.api.UkOpenBankingApiDefinitions;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.ukopenbanking.ais.base.entities.PartyV31Entity;
import se.tink.backend.aggregation.nxgen.storage.PersistentStorage;

public class PartyDataStorageTest {

    private PartyDataStorage partyDataStorage;

    @Before
    public void setUp() throws Exception {
        this.partyDataStorage = new PartyDataStorage(new PersistentStorage());
    }

    @Test
    public void shouldRestoreSinglePartyList() {
        PartyV31Entity party = getDummyParty();

        List<PartyV31Entity> parties = Collections.singletonList(party);
        partyDataStorage.storeParties(parties);

        List<PartyV31Entity> restoredParties = partyDataStorage.restoreParties();
        assertThat(restoredParties).hasSize(1);
        assertThat(restoredParties.get(0)).isEqualTo(party);
    }

    @Test
    public void shouldRestoreParty() {
        // given
        PartyV31Entity party = getDummyParty();

        // when
        partyDataStorage.storeParty(party);

        // then
        Optional<PartyV31Entity> restoredParty = partyDataStorage.restoreParty();
        assertThat(restoredParty.isPresent()).isTrue();
        assertThat(restoredParty.get()).isEqualTo(party);
    }

    private PartyV31Entity getDummyParty() {
        PartyV31Entity party = new PartyV31Entity();
        party.setPartyId("dummyId");
        party.setPartyType(UkOpenBankingApiDefinitions.PartyType.SOLE);
        return party;
    }
}
