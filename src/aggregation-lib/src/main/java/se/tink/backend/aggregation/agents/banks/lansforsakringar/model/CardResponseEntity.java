package se.tink.backend.aggregation.agents.banks.lansforsakringar.model;

import java.util.ArrayList;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

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
