package se.tink.backend.aggregation.agents.nxgen.se.banks.swedbank.fetchers.investment.rpc;

import java.util.List;
import lombok.Getter;
import se.tink.backend.aggregation.agents.nxgen.se.banks.swedbank.serviceprovider.rpc.AbstractAccountEntity;
import se.tink.backend.aggregation.agents.nxgen.se.banks.swedbank.serviceprovider.rpc.AmountEntity;
import se.tink.backend.aggregation.agents.nxgen.se.banks.swedbank.serviceprovider.rpc.LinksEntity;
import se.tink.backend.aggregation.annotations.JsonObject;

@JsonObject
@Getter
public abstract class AbstractInvestmentAccountEntity extends AbstractAccountEntity {
    protected AmountEntity totalValue;
    protected String type;
    protected PerformanceEntity performance;
    protected AmountEntity marketValue;
    protected List<HoldingEntity> holdings;
    protected LinksEntity links;
    protected String id;
}
