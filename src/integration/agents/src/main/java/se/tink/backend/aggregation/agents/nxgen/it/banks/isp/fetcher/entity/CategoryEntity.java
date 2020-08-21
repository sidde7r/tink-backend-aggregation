package se.tink.backend.aggregation.agents.nxgen.it.banks.isp.fetcher.entity;

import java.math.BigDecimal;
import lombok.Data;
import se.tink.backend.aggregation.annotations.JsonObject;

@JsonObject
@Data
public class CategoryEntity {
    private Long categoryId;
    private String categoryName;
    private BigDecimal score;
}
