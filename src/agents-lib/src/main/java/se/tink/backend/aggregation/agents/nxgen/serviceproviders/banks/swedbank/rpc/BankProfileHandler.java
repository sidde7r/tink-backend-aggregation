package se.tink.backend.aggregation.agents.nxgen.serviceproviders.banks.swedbank.rpc;

import com.fasterxml.jackson.annotation.JsonIgnore;
import java.util.ArrayList;
import java.util.List;
import se.tink.backend.aggregation.annotations.JsonObject;

@JsonObject
public class BankProfileHandler {
    private List<BankProfile> bankProfiles = new ArrayList<>();
    private BankProfile activeBankProfile;

    public BankProfileHandler setActiveBankProfile(
            BankProfile activeBankProfile) {
        this.activeBankProfile = activeBankProfile;
        return this;
    }

    @JsonIgnore
    public BankProfile getActiveBankProfile() {
        return activeBankProfile;
    }

    public List<BankProfile> getBankProfiles() {
        return bankProfiles;
    }

    public BankProfileHandler addBankProfile(
            BankProfile bankProfile) {
        this.bankProfiles.add(bankProfile);
        return this;
    }

    public BankProfile findProfile(BankProfile requestedBankProfile) {
        return bankProfiles.stream()
                .filter(profile -> profile.getBank().getBankId().equalsIgnoreCase(requestedBankProfile.getBank().getBankId()))
                .findFirst()
                .orElseThrow(IllegalStateException::new);
    }

    // currently we are always using the last added bank profile for transfers
    public BankProfile findTransferProfile() {
        int transferProfileIndex = bankProfiles.size() -1;

        return bankProfiles.get(transferProfileIndex);
    }
}
