package se.tink.backend.aggregation.agents.nxgen.serviceproviders.banks.nordea.partner.fetcher.transactional.entity;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Getter;
import se.tink.backend.aggregation.annotations.JsonObject;

@JsonObject
@Getter
public class TransactionTypeEntity {
    @JsonProperty("transaction_group")
    private String transactionGroup;

    @JsonProperty("transaction_group_text")
    private String transactionGroupText;

    @JsonProperty("transaction_code")
    private String transactionCode;

    @JsonProperty("transaction_code_text")
    private String transactionCodeText;
}
