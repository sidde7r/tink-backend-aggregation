package se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.ukopenbanking.ais.v31.mapper;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.Before;
import org.junit.Test;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.ukopenbanking.ais.base.entities.IdentityDataV31Entity;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.ukopenbanking.ais.v31.fixtures.PartyFixtures;
import se.tink.libraries.identitydata.IdentityData;

public class IdentityDataMapperTest {

    private IdentityDataMapper identityDataMapper;

    @Before
    public void setUp() {
        identityDataMapper = new IdentityDataMapper();
    }

    @Test
    public void dataIsCorrectlyMapepd() {
        IdentityDataV31Entity input = PartyFixtures.party();
        IdentityData result = identityDataMapper.map(input);
        assertThat(result.getDateOfBirth()).isNull();
        assertThat(result.getFullName()).isEqualTo(input.getName());
    }
}
