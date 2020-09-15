package se.tink.backend.aggregation.agents.nxgen.pt.banks.edenred.entities;

import java.util.Date;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import se.tink.backend.aggregation.annotations.JsonObject;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@JsonObject
public class HolderCustomerEntity {

    private long id;
    private long regVersion;
    private String name;
    private Date registerDate;
    private Date lastLoginDateHour;
    private String status;
}
