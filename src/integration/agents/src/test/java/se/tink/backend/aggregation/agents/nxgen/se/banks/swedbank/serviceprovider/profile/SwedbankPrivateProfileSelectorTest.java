package se.tink.backend.aggregation.agents.nxgen.se.banks.swedbank.serviceprovider.profile;

import java.util.List;
import org.junit.Assert;
import org.junit.Test;
import se.tink.backend.aggregation.agents.nxgen.se.banks.swedbank.serviceprovider.rpc.BankEntity;
import se.tink.backend.aggregation.agents.nxgen.se.banks.swedbank.serviceprovider.rpc.ProfileEntity;
import se.tink.backend.aggregation.agents.nxgen.se.banks.swedbank.serviceprovider.rpc.ProfileResponse;
import se.tink.libraries.pair.Pair;
import se.tink.libraries.serialization.utils.SerializationUtils;

public class SwedbankPrivateProfileSelectorTest {

    SwedbankProfileSelector swedbankProfileSelector = new SwedbankPrivateProfileSelector();

    @Test
    public void shouldTakePortalProfileAsPrivateProfile() {
        List<Pair<BankEntity, ProfileEntity>> result =
                swedbankProfileSelector.selectBankProfiles(getProfiles());

        Assert.assertEquals(2, result.size());
    }

    private List<BankEntity> getProfiles() {
        return SerializationUtils.deserializeFromString(
                        "{\"banks\":["
                                + "{\"name\":\"Swedbank AB (publ)\","
                                + "\"bankId\":\"08999\","
                                + "\"url\":\"https://www.swedbank.se\","
                                + "\"corporateProfiles\":[],"
                                + "\"servicePortalProfile\":{"
                                + "\"activeProfileLanguage\":\"sv\","
                                + "\"numberOfUnreadDocuments\":0,"
                                + "\"targetType\":\"NEUTRAL\","
                                + "\"customerName\":\"Esbjörn Fakename\","
                                + "\"customerNumber\":\"**HASHED:yH**\","
                                + "\"id\":\"ID\","
                                + "\"bankId\":\"08999\","
                                + "\"bankName\":\"Swedbank AB (publ)\","
                                + "\"url\":\"https://www.swedbank.se\","
                                + "\"customerInternational\":false,"
                                + "\"youthProfile\":false,"
                                + "\"links\":{\"edit\":{\"method\":\"PUT\",\"uri\":\"/v5/profile/subscription/ID\"},\"next\":{\"method\":\"POST\",\"uri\":\"/v5/profile/ID\"}}}},"
                                + "{\"name\":\"Sparbanken Skåne AB (publ)\","
                                + "\"bankId\":\"08313\","
                                + "\"url\":\"https://www.sparbankenskane.se\","
                                + "\"privateProfile\":{"
                                + "\"activeProfileLanguage\":\"sv\","
                                + "\"targetType\":\"PRIVATE\","
                                + "\"customerName\":\"Esbjörn Fakename\","
                                + "\"customerNumber\":\"**HASHED:yH**\","
                                + "\"id\":\"ID2\","
                                + "\"bankId\":\"08313\","
                                + "\"bankName\":\"Sparbanken Skåne AB (publ)\","
                                + "\"url\":\"https://www.sparbankenskane.se\","
                                + "\"customerInternational\":false,"
                                + "\"youthProfile\":false,"
                                + "\"links\":{\"edit\":{\"method\":\"PUT\",\"uri\":\"/v5/profile/subscription/ID2\"},\"next\":{\"method\":\"POST\",\"uri\":\"/v5/profile/ID2\"}}},"
                                + "\"corporateProfiles\":[]}],"
                                + "\"hasSwedbankProfile\":true,"
                                + "\"hasSavingbankProfile\":true,"
                                + "\"userId\":\"**HASHED:yH**\"}",
                        ProfileResponse.class)
                .getBanks();
    }
}
