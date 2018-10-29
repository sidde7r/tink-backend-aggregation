package se.tink.backend.core;

import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.Table;
import org.hibernate.annotations.Type;

@Entity
@Table(name = "aggregator_configurations")
public class AggregatorConfigurations {
    @Id
    private String aggregatorId;
    @Type(type = "text")
    private String aggregatorInfo;

    public AggregatorConfigurations() {
        // Ok.
    }

    public String getAggregatorId() {
        return aggregatorId;
    }

    public void setAggregatorId(String aggregatorId) {
        this.aggregatorId = aggregatorId;
    }

    public String getAggregatorInfo() {
        return aggregatorInfo;
    }

    public void setAggregatorInfo(String aggregatorInfo) {
        this.aggregatorInfo = aggregatorInfo;
    }
}
