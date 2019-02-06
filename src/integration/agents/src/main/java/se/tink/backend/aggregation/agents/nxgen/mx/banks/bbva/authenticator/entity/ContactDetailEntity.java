package se.tink.backend.aggregation.agents.nxgen.mx.banks.bbva.authenticator.entity;

import se.tink.backend.aggregation.annotations.JsonObject;

@JsonObject
public class ContactDetailEntity {
    private String contact;
    private ContactTypeEntity contactType;

    public ContactDetailEntity(String phoneNumber) {
        this.contact = phoneNumber;
        this.contactType = new ContactTypeEntity();
    }
}
