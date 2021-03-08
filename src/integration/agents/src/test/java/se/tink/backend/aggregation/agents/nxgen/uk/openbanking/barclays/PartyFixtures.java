package se.tink.backend.aggregation.agents.nxgen.uk.openbanking.barclays;

import com.fasterxml.jackson.core.type.TypeReference;
import java.util.List;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.ukopenbanking.ais.base.entities.PartyV31Entity;
import se.tink.libraries.serialization.utils.SerializationUtils;

public class PartyFixtures {

    private static final String PARTIES =
            "[{\"PartyId\":\"PABC123\",\"PartyType\":\"Sole\",\"Name\":\"Semiotec\",\"FullLegalName\":\"Semiotec Limited\",\"LegalStructure\":\"UK.OBIE.PrivateLimitedCompany\",\"BeneficialOwnership\":true,\"AccountRole\":\"UK.OBIE.Principal\",\"EmailAddress\":\"contact@semiotec.co.jp\",\"Relationships\":{\"Account\":{\"Related\":\"https://api.alphabank.com/open-banking/v4.0/aisp/accounts/22289\",\"Id\":\"22289\"}},\"Address\":[{\"AddressType\":\"Business\",\"StreetName\":\"Street\",\"BuildingNumber\":\"15\",\"PostCode\":\"NW1 1AB\",\"TownName\":\"London\",\"Country\":\"GB\"}]},{\"PartyId\":\"PXSIF023\",\"PartyNumber\":\"0000007456\",\"PartyType\":\"Delegate\",\"Name\":\"Kevin Atkinson\",\"FullLegalName\":\"Mr Kevin Bartholmew Atkinson\",\"LegalStructure\":\"UK.OBIE.Individual\",\"BeneficialOwnership\":false,\"AccountRole\":\"UK.OBIE.Administrator\",\"EmailAddress\":\"kev@semiotec.co.jp\",\"Relationships\":{\"Account\":{\"Related\":\"https://api.alphabank.com/open-banking/v4.0/aisp/accounts/22289\",\"Id\":\"22289\"}}}]";

    public static List<PartyV31Entity> parties() {
        return SerializationUtils.deserializeFromString(
                PARTIES, new TypeReference<List<PartyV31Entity>>() {});
    }
}
