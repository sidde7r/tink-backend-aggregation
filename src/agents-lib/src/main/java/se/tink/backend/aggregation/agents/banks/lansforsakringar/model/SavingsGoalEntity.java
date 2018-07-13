package se.tink.backend.aggregation.agents.banks.lansforsakringar.model;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import se.tink.backend.aggregation.log.AggregationLogger;

@JsonIgnoreProperties(ignoreUnknown = true)
public class SavingsGoalEntity {
    @JsonIgnore
    private static final AggregationLogger log = new AggregationLogger(SavingsGoalEntity.class);
    private String id;
    private String name;
    private Double amount;
    private Object category;
    private Object imageReference;
    private Integer chosenRisk;
    private String status;

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public Double getAmount() {
        return amount;
    }

    public void setAmount(Double amount) {
        this.amount = amount;
    }

    public Object getCategory() {
        return category;
    }

    public void setCategory(Object category) {
        if (category instanceof String) {
            log.info("LF - SavingsGoalEntity - category is type String");
        } else if (category instanceof Integer) {
            log.info("LF - SavingsGoalEntity - category is type Ieteger");
        }

        this.category = category;
    }

    public Object getImageReference() {
        return imageReference;
    }

    public void setImageReference(Object imageReference) {
        if (category instanceof String) {
            log.info("LF - SavingsGoalEntity - imageReference is type String");
        }

        this.imageReference = imageReference;
    }

    public Integer getChosenRisk() {
        return chosenRisk;
    }

    public void setChosenRisk(Integer chosenRisk) {
        this.chosenRisk = chosenRisk;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }
}
