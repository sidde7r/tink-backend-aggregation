package se.tink.backend.aggregation.agents.nxgen.se.banks.swedbank.fetchers.investment.entities;

import com.fasterxml.jackson.annotation.JsonIgnore;
import java.util.List;
import se.tink.backend.aggregation.agents.nxgen.se.banks.swedbank.fetchers.investment.rpc.PerformanceEntity;
import se.tink.backend.aggregation.agents.nxgen.se.banks.swedbank.serviceprovider.rpc.AmountEntity;
import se.tink.backend.aggregation.agents.nxgen.se.banks.swedbank.serviceprovider.rpc.LinksEntity;
import se.tink.backend.aggregation.annotations.JsonObject;

@JsonObject
public class PensionInsuranceEntity {
    private AmountEntity totalValue;
    private String type;
    private PerformanceEntity performance;
    private AmountEntity marketValue;
    private boolean holdingsFetched; // is true even though holdings list is empty
    private List<Object> holdings;
    private boolean isTrad;
    private LinksEntity links;
    private String id;
    private String name;
    private String accountNumber;
    private String clearingNumber;
    private String fullyFormattedNumber;

    @JsonIgnore
    public boolean hasNonEmptyHoldingsList() {
        return holdings != null && !holdings.isEmpty();
    }
}
