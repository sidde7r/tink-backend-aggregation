package se.tink.backend.aggregation.agents.nxgen.serviceproviders.banks.swedbank.rpc;

import com.google.common.base.Preconditions;
import java.util.Map;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.banks.swedbank.fetchers.transferdestination.rpc.PaymentBaseinfoResponse;

public class BankProfile {
    private final BankEntity bank;
    private final Map<String, MenuItemLinkEntity> menuItems;
    private final EngagementOverviewResponse engagementOverViewResponse;
    private final PaymentBaseinfoResponse paymentBaseinfoResponse;

    public BankProfile(BankEntity bank,
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
