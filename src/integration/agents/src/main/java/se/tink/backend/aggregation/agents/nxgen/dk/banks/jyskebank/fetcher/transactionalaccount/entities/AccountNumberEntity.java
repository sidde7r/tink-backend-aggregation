package se.tink.backend.aggregation.agents.nxgen.dk.banks.jyskebank.fetcher.transactionalaccount.entities;

import lombok.Getter;
import se.tink.backend.aggregation.annotations.JsonObject;

@JsonObject
@Getter
public class AccountNumberEntity {
    private String regNo;
    private String accountNo;
    private String publicId;
}
