package se.tink.backend.aggregation.agents.nxgen.se.banks.swedbank.serviceprovider.rpc;

import java.util.List;
import se.tink.backend.aggregation.annotations.JsonObject;

@JsonObject
public class ProfileResponse {
    private String userId;
    private boolean hasSwedbankProfile;
    private boolean hasSavingbankProfile;
    private List<BankEntity> banks;

    public String getUserId() {
        return userId;
    }

    public boolean isHasSwedbankProfile() {
        return hasSwedbankProfile;
    }

    public boolean isHasSavingbankProfile() {
        return hasSavingbankProfile;
    }

    public List<BankEntity> getBanks() {
        return banks;
    }
}
