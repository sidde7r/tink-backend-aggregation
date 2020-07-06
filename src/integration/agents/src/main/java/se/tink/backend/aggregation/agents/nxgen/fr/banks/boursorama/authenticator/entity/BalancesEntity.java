package se.tink.backend.aggregation.agents.nxgen.fr.banks.boursorama.authenticator.entity;

import java.util.List;
import se.tink.backend.aggregation.annotations.JsonObject;

@JsonObject
public class BalancesEntity {
    private List<String> accounts;
    private double amount;
    private String currency;
    private String description;
    private String id;
    private String label;
    private String updateDate;
}
