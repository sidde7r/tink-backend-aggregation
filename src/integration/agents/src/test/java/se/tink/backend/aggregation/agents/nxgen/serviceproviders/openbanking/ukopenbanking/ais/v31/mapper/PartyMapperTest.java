package se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.ukopenbanking.ais.v31.mapper;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.Test;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.ukopenbanking.ais.base.entities.PartyV31Entity;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.ukopenbanking.ais.v31.fixtures.PartyFixtures;
import se.tink.libraries.identitydata.IdentityData;

public class PartyMapperTest {

    @Test
    public void mapDataCorrectly() {
        // given
        PartyV31Entity input = PartyFixtures.party();

        // when
        IdentityData result = PartyMapper.toIdentityData(input);

        // then
        assertThat(result.getDateOfBirth()).isNull();
        assertThat(result.getFullName()).isEqualTo(input.getName());
    }
}
