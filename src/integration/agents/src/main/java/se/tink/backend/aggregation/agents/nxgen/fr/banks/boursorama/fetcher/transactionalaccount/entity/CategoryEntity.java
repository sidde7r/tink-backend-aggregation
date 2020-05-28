package se.tink.backend.aggregation.agents.nxgen.fr.banks.boursorama.fetcher.transactionalaccount.entity;

import se.tink.backend.aggregation.annotations.JsonObject;

@JsonObject
public class CategoryEntity {
    private String color;
    private String distributionColor;
    private String iconId;
    private int id;
    private String label;
    private String parentLabel;
}
