package se.tink.backend.aggregation.agents.banks.swedbank.model;

import com.google.common.base.Preconditions;
import java.util.HashMap;
import java.util.Map;

public class ProfilesHandler {
    private Map<String, BankProfile> profiles = new HashMap<>();
    private BankProfile transferProfile;
    private BankProfile activeProfile;

    public boolean isActiveProfile(BankProfile bankProfile) {
        return activeProfile.bankId.equalsIgnoreCase(bankProfile.bankId);
    }

    public void addBankProfile(BankEntity bank, ProfileMenu profileMenu, EngagementOverviewResponse engagementOverView, boolean isActive, boolean isTransfer) {
        BankProfile profileEntity = new BankProfile(bank, profileMenu, engagementOverView);
        profiles.put(bank.getBankId(), profileEntity);
        if (isActive) {
            activeProfile = profileEntity;
        }
        if (isTransfer) {
            transferProfile = profileEntity;
        }
    }

    public Map<String, BankProfile> getProfiles() {
        return profiles;
    }

    public void setActiveProfile(BankProfile bankProfile) {
        activeProfile = bankProfile;
    }

    public BankProfile getActiveProfile() {
        return activeProfile;
    }

    public BankProfile getTransferProfile() {
        return transferProfile;
    }

    public static class BankProfile {
        BankEntity bank;
        String bankName;
        String bankId;
        LinkEntity profileLink;
        ProfileMenu profileMenu;
        EngagementOverviewResponse engagementOverview;

        public BankProfile(BankEntity bank, ProfileMenu profileMenu, EngagementOverviewResponse engagementOverView) {
            this.bank = Preconditions.checkNotNull(bank);
            this.bankName = bank.getName();
            this.bankId = Preconditions.checkNotNull(bank.getBankId());
            this.profileLink = Preconditions.checkNotNull(bank.getPrivateProfile().getLinks().getNext());
            this.profileMenu = Preconditions.checkNotNull(profileMenu);
            this.engagementOverview = Preconditions.checkNotNull(engagementOverView);
        }

        public BankEntity getBank() {
            return bank;
        }

        public String getBankId() {
            return bankId;
        }

        public EngagementOverviewResponse getEngagementOverview() {
            return engagementOverview;
        }

        public ProfileMenu getProfileMenu() {
            return profileMenu;
        }
    }
}
