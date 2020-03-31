package se.tink.backend.aggregation.agents.nxgen.se.banks.swedbank.serviceprovider.rpc;

import java.util.List;
import se.tink.backend.aggregation.annotations.JsonObject;

@JsonObject
public class BankEntity {
    private String name;
    private String url;
    private String bankId;
    private PrivateProfileEntity privateProfile;
    private List<PrivateProfileEntity> corporateProfiles;

    public String getName() {
        return name;
    }

    public String getUrl() {
        return url;
    }

    public String getBankId() {
        return bankId;
    }

    public PrivateProfileEntity getPrivateProfile() {
        if (corporateProfiles.isEmpty()) {
            return privateProfile;
        }
        return corporateProfiles.get(0);
    }
}
