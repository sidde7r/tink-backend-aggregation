package se.tink.backend.aggregation.agents.nxgen.serviceproviders.banks.bankdata.fetcher.entities;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import se.tink.backend.aggregation.annotations.JsonObject;

@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
@JsonObject
public class BankdataAccountIdEntity {

    private String regNo;
    private String accountNo;
    private String accountName;
}
