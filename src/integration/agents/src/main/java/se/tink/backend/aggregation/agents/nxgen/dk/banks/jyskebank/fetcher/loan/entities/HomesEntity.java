package se.tink.backend.aggregation.agents.nxgen.dk.banks.jyskebank.fetcher.loan.entities;

import java.util.Collections;
import java.util.List;
import lombok.Getter;
import se.tink.backend.aggregation.annotations.JsonObject;

@JsonObject
@Getter
public class HomesEntity {
    private String name;
    private String propertyNo;
    private List<LoansEntity> loans = Collections.emptyList();
}
