package se.tink.backend.aggregation.storage.database.models;

import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.Table;
import org.hibernate.annotations.Type;

@Entity
@Table(name = "aggregator_configurations")
public class AggregatorConfiguration {
    @Id private String aggregatorId;

    @Type(type = "text")
    private String aggregatorInfo;

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
