package se.tink.backend.aggregation.agents.nxgen.se.banks.swedbank.fetchers.investment.rpc;

import com.fasterxml.jackson.annotation.JsonIgnore;
import java.util.List;
import lombok.Getter;
import se.tink.backend.aggregation.agents.nxgen.se.banks.swedbank.serviceprovider.rpc.AmountEntity;
import se.tink.backend.aggregation.annotations.JsonObject;

@Getter
@JsonObject
public class DetailedPensionEntity {

    @JsonIgnore private static final String API_CLIENT_ERROR_MESSAGE = "No API client provided.";

    private String fullyFormattedNumber;
    private List<Object> settlements;
    private AmountEntity totalValue;
    private boolean isTrad;
    private String productId;
    private TotalEquitiesEntity totalEquities;
    private AmountEntity marketValue;
    private boolean holdingsFetched;
    private List<PlacementEntity> placements;
    private AmountEntity acquisitionValue;
    private String accountNumber;
    private String type;
    private String clearingNumber;
    private String encompassedHoldings;
    private ChartDataEntity chartData;
    private List<OperationEntity> operations;
    private PerformanceEntity performance;
    private String portfolio;
    private String name;
    private List<DetailedHoldingEntity> holdings;
    private String id;
}
