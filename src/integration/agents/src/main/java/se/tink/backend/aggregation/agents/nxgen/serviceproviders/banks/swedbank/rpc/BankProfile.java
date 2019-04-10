package se.tink.backend.aggregation.agents.nxgen.serviceproviders.banks.swedbank.rpc;

import com.google.common.base.Preconditions;
import java.util.Map;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.banks.swedbank.fetchers.transferdestination.rpc.PaymentBaseinfoResponse;
import se.tink.backend.aggregation.annotations.JsonObject;

@JsonObject
public class BankProfile {
    private BankEntity bank;
    private Map<String, MenuItemLinkEntity> menuItems;
    private EngagementOverviewResponse engagementOverViewResponse;
    private PaymentBaseinfoResponse paymentBaseinfoResponse;

    public BankProfile() {}

    public BankProfile(
            BankEntity bank,
            Map<String, MenuItemLinkEntity> menuItems,
            EngagementOverviewResponse engagementOverViewResponse,
            PaymentBaseinfoResponse paymentBaseinfoResponse) {
        this.bank = Preconditions.checkNotNull(bank);
        this.menuItems = Preconditions.checkNotNull(menuItems);
        this.engagementOverViewResponse = Preconditions.checkNotNull(engagementOverViewResponse);
        this.paymentBaseinfoResponse = paymentBaseinfoResponse;
    }

    public BankEntity getBank() {
        return bank;
    }

    public Map<String, MenuItemLinkEntity> getMenuItems() {
        return menuItems;
    }

    public EngagementOverviewResponse getEngagementOverViewResponse() {
        return engagementOverViewResponse;
    }

    public PaymentBaseinfoResponse getPaymentBaseinfoResponse() {
        return paymentBaseinfoResponse;
    }
}
