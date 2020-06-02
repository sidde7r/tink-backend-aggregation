package se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.creditagricole.transactionalaccount.entities;

import lombok.Data;
import se.tink.backend.aggregation.annotations.JsonObject;

@JsonObject
@Data
public class LinksDto {

    private LinkDetailsEntity first;

    private LinkDetailsEntity last;

    private LinkDetailsEntity next;

    private LinkDetailsEntity parentist;

    private LinkDetailsEntity prev;

    private LinkDetailsEntity self;
}
