package se.tink.backend.aggregation.agents.banks.lansforsakringar.model;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import java.util.ArrayList;

@JsonIgnoreProperties(ignoreUnknown = true)
public class CardResponseEntity {

    private ArrayList<CardEntity> list;

    public ArrayList<CardEntity> getList() {
        return list;
    }

    public void setList(ArrayList<CardEntity> list) {
        this.list = list;
    }
}
