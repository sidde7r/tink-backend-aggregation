package se.tink.backend.aggregation.agents.nxgen.se.banks.swedbank.serviceprovider.rpc;

import com.fasterxml.jackson.annotation.JsonIgnore;
import java.util.Map;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import se.tink.backend.aggregation.agents.nxgen.se.banks.swedbank.serviceprovider.fetchers.transferdestination.rpc.PaymentBaseinfoResponse;
import se.tink.backend.aggregation.annotations.JsonObject;

@NoArgsConstructor
@AllArgsConstructor
@Getter
@JsonObject
public class BankProfile {
    private BankEntity bank;
    private Map<String, MenuItemLinkEntity> menuItems;
    private EngagementOverviewResponse engagementOverViewResponse;
    private PaymentBaseinfoResponse paymentBaseinfoResponse;
    private String selectedProfileId;

    @JsonIgnore
    public ProfileEntity getProfile() {
        return bank.getProfile(selectedProfileId);
    }
}
