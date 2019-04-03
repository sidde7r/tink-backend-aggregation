
package se.tink.backend.aggregation.agents.nxgen.es.openbanking.bbva.fetcher.transactionalaccount.entity.transaction;

import se.tink.backend.aggregation.annotations.JsonObject;

@JsonObject
public class AttachedInfoEntity {

    private String name;
    private String type;
    private Integer size;
    private String date;

}
