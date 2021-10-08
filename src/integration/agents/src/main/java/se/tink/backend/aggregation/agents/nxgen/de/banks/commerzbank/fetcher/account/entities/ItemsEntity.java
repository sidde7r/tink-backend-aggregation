package se.tink.backend.aggregation.agents.nxgen.de.banks.commerzbank.fetcher.account.entities;

import java.util.List;
import lombok.Getter;
import se.tink.backend.aggregation.annotations.JsonObject;

@JsonObject
@Getter
public class ItemsEntity {
    private String displayCustomerNumber;
    private String customerNumber;
    private String customerName;
    private String accountOwnerTitle;
    private boolean primary;
    private List<ProductsEntity> products;
}
