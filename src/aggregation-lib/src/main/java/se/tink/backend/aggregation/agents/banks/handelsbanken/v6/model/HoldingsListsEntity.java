package se.tink.backend.aggregation.agents.banks.handelsbanken.v6.model;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import java.util.List;

@JsonIgnoreProperties(ignoreUnknown = true)
public class HoldingsListsEntity {
    private String title;
    private List<CustodyHoldingsEntity> holdingList;

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public List<CustodyHoldingsEntity> getHoldingList() {
        return holdingList;
    }

    public void setHoldingList(
            List<CustodyHoldingsEntity> holdingList) {
        this.holdingList = holdingList;
    }
}
