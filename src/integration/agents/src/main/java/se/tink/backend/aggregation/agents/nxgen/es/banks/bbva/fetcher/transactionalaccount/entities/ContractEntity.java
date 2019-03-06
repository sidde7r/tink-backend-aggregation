package se.tink.backend.aggregation.agents.nxgen.es.banks.bbva.fetcher.transactionalaccount.entities;

import se.tink.backend.aggregation.agents.nxgen.es.banks.bbva.entities.BankEntity;
import se.tink.backend.aggregation.agents.nxgen.es.banks.bbva.entities.FormatsEntity;
import se.tink.backend.aggregation.annotations.JsonObject;

@JsonObject
public class ContractEntity {
    private String id;
    private String counterPart;
    private String country;
    private BankEntity bank;
    private FormatsEntity formats;

    public String getId() {
        return id;
    }

    public ContractEntity setId(String id) {
        this.id = id;
        return this;
    }

    public String getCounterPart() {
        return counterPart;
    }

    public String getCountry() {
        return country;
    }

    public BankEntity getBank() {
        return bank;
    }

    public FormatsEntity getFormats() {
        return formats;
    }
}
