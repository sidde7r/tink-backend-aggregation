package se.tink.backend.aggregation.agents.nxgen.se.banks.swedbank.serviceprovider.rpc;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.google.common.base.Preconditions;
import java.util.Map;
import lombok.Getter;
import lombok.NoArgsConstructor;
import se.tink.backend.aggregation.agents.nxgen.se.banks.swedbank.serviceprovider.fetchers.transferdestination.rpc.PaymentBaseinfoResponse;
import se.tink.backend.aggregation.annotations.JsonObject;

@NoArgsConstructor
@Getter
@JsonObject
public class BankProfile {
    private BankEntity bank;
    private Map<String, MenuItemLinkEntity> menuItems;
    private EngagementOverviewResponse engagementOverViewResponse;
    private PaymentBaseinfoResponse paymentBaseinfoResponse;
    private String selectedProfileId;

    public BankProfile(
            BankEntity bank,
            Map<String, MenuItemLinkEntity> menuItems,
            EngagementOverviewResponse engagementOverViewResponse,
            PaymentBaseinfoResponse paymentBaseinfoResponse,
            String selectedProfileId) {
        this.bank = Preconditions.checkNotNull(bank);
        this.menuItems = Preconditions.checkNotNull(menuItems);
        this.engagementOverViewResponse = Preconditions.checkNotNull(engagementOverViewResponse);
        this.paymentBaseinfoResponse = paymentBaseinfoResponse;
        this.selectedProfileId = selectedProfileId;
    }

    @JsonIgnore
    public ProfileEntity getProfile() {
        return bank.getProfile(selectedProfileId);
    }
}
