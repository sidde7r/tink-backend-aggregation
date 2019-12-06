package se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.openbankproject.transactionalaccount.entities.transactions;

import se.tink.backend.aggregation.annotations.JsonObject;

@JsonObject
public class ImageEntity {

    private String id;
    private String label;
    private String url;
    private String date;
    private UserEntity user;
}
