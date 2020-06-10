package se.tink.backend.aggregation.agents.nxgen.se.banks.swedbank.serviceprovider.rpc;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.google.common.base.Preconditions;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import se.tink.backend.aggregation.agents.exceptions.transfer.TransferExecutionException;
import se.tink.backend.aggregation.agents.nxgen.se.banks.swedbank.serviceprovider.SwedbankBaseConstants;
import se.tink.backend.aggregation.annotations.JsonObject;
import se.tink.backend.aggregation.log.AggregationLogger;
import se.tink.libraries.i18n.Catalog;
import se.tink.libraries.signableoperation.enums.SignableOperationStatuses;

@JsonObject
public class BankProfileHandler {
    private List<BankProfile> bankProfiles = new ArrayList<>();
    private BankProfile activeBankProfile;
    private Map<String, MenuItemLinkEntity> menuItems;
    private final AggregationLogger log = new AggregationLogger(BankProfileHandler.class);

    public BankProfileHandler setActiveBankProfile(BankProfile activeBankProfile) {
        this.activeBankProfile = activeBankProfile;
        return this;
    }

    public BankProfile getActiveBankProfile() {
        return activeBankProfile;
    }

    public List<BankProfile> getBankProfiles() {
        return bankProfiles;
    }

    @JsonIgnore
    public BankProfileHandler addBankProfile(BankProfile bankProfile) {
        this.bankProfiles.add(bankProfile);
        return this;
    }

    @JsonIgnore
    public BankProfile findProfile(BankProfile requestedBankProfile) {
        return bankProfiles.stream()
                .filter(
                        profile ->
                                profile.getBank()
                                        .getBankId()
                                        .equalsIgnoreCase(
                                                requestedBankProfile.getBank().getBankId()))
                .findFirst()
                .orElseThrow(IllegalStateException::new);
    }

    @JsonIgnore
    // currently we are always using the last added bank profile for transfers
    public BankProfile findTransferProfile() {
        int transferProfileIndex = bankProfiles.size() - 1;

        return bankProfiles.get(transferProfileIndex);
    }

    @JsonIgnore
    public void throwIfNotAuthorizedForRegisterAction(
            SwedbankBaseConstants.MenuItemKey menuItemKey, Catalog catalog)
            throws TransferExecutionException {
        if (!isAuthorizedForAction(menuItemKey)) {
            throw TransferExecutionException.builder(SignableOperationStatuses.CANCELLED)
                    .setEndUserMessage(
                            catalog.getString(
                                    SwedbankBaseConstants.UserMessage
                                            .STRONGER_AUTHENTICATION_NEEDED))
                    .setMessage(SwedbankBaseConstants.ErrorMessage.NEEDS_EXTENDED_USE)
                    .build();
        }
    }

    @JsonIgnore
    public boolean isAuthorizedForAction(SwedbankBaseConstants.MenuItemKey menuItemKey) {
        Map<String, MenuItemLinkEntity> menuItems = getMenuItems();
        Preconditions.checkNotNull(menuItemKey);
        Preconditions.checkNotNull(menuItems);
        try {
            Preconditions.checkState(
                    menuItems.containsKey(menuItemKey.getKey()),
                    String.format(
                            "Unable to check state for the missing key: %s", menuItemKey.getKey()));
        } catch (IllegalStateException e) {
            log.warn(e.getMessage(), e);
            return false;
        }
        MenuItemLinkEntity menuItem = menuItems.get(menuItemKey.getKey());

        return menuItem.isAuthorized();
    }

    public Map<String, MenuItemLinkEntity> getMenuItems() {
        return Objects.isNull(getActiveBankProfile())
                ? menuItems
                : getActiveBankProfile().getMenuItems();
    }

    public void setMenuItems(Map<String, MenuItemLinkEntity> menuItems) {
        this.menuItems = menuItems;
    }
}
