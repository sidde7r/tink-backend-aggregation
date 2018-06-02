package se.tink.backend.aggregation.agents.banks.lansforsakringar.model;

import java.util.ArrayList;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

@JsonIgnoreProperties(ignoreUnknown = true)
public class AccountResponseEntity {

    private ArrayList<AccountEntity> list;

    public ArrayList<AccountEntity> getList() {
        return list;
    }

    public void setList(ArrayList<AccountEntity> list) {
        this.list = list;
    }
}
