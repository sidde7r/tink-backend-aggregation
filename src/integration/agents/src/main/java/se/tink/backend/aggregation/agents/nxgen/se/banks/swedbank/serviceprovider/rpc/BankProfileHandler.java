package se.tink.backend.aggregation.agents.nxgen.se.banks.swedbank.serviceprovider.rpc;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.google.common.base.Preconditions;
import java.lang.invoke.MethodHandles;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import se.tink.backend.aggregation.agents.exceptions.transfer.TransferExecutionException;
import se.tink.backend.aggregation.agents.nxgen.se.banks.swedbank.serviceprovider.SwedbankBaseConstants;
import se.tink.backend.aggregation.agents.nxgen.se.banks.swedbank.serviceprovider.fetchers.transferdestination.rpc.PaymentBaseinfoResponse;
import se.tink.backend.aggregation.annotations.JsonObject;
import se.tink.libraries.i18n.Catalog;
import se.tink.libraries.signableoperation.enums.InternalStatus;
import se.tink.libraries.signableoperation.enums.SignableOperationStatuses;

@JsonObject
public class BankProfileHandler {
    @JsonIgnore
    private static final Logger logger =
            LoggerFactory.getLogger(MethodHandles.lookup().lookupClass());

    @JsonProperty private List<BankProfile> bankProfiles = new ArrayList<>();
    @JsonProperty private BankProfile activeBankProfile;
    @JsonProperty private Map<String, MenuItemLinkEntity> menuItems;

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
                    .setInternalStatus(InternalStatus.BANKID_NEEDS_EXTENDED_USE_ENABLED.toString())
                    .build();
        }
    }

    @JsonIgnore
    public boolean isAuthorizedForAction(SwedbankBaseConstants.MenuItemKey menuItemKey) {
        Map<String, MenuItemLinkEntity> menuItems = getMenuItems();
        Preconditions.checkNotNull(menuItemKey);
        Preconditions.checkNotNull(menuItems);
        if (menuItems.containsKey(menuItemKey.getKey())) {
            MenuItemLinkEntity menuItem = menuItems.get(menuItemKey.getKey());
            return menuItem.isAuthorized();
        } else {
            logger.warn(String.format("Could not find key: %s", menuItemKey.getKey()));
            return false;
        }
    }

    @JsonIgnore
    public PaymentBaseinfoResponse getActivePaymentBaseInfo() {
        return Preconditions.checkNotNull(
                getActiveBankProfile().getPaymentBaseinfoResponse(),
                "Expected to have payment base info");
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
