package se.tink.backend.aggregation.agents.banks.swedbank.model;

import java.util.Collections;
import java.util.List;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

@JsonIgnoreProperties(ignoreUnknown = true)
public class ProfileResponse extends AbstractResponse {
    protected List<BankEntity> banks;
    protected boolean hasSavingbankProfile;
    protected boolean hasSwedbankProfile;

    public List<BankEntity> getBanks() {
        return banks != null ? banks : Collections.emptyList();
    }

    public boolean isHasSavingbankProfile() {
        return hasSavingbankProfile;
    }

    public boolean isHasSwedbankProfile() {
        return hasSwedbankProfile;
    }

    public void setBanks(List<BankEntity> banks) {
        this.banks = banks;
    }

    public void setHasSavingbankProfile(boolean hasSavingbankProfile) {
        this.hasSavingbankProfile = hasSavingbankProfile;
    }

    public void setHasSwedbankProfile(boolean hasSwedbankProfile) {
        this.hasSwedbankProfile = hasSwedbankProfile;
    }

}
