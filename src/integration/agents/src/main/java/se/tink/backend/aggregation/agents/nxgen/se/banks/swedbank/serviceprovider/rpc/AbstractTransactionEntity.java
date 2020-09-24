package se.tink.backend.aggregation.agents.nxgen.se.banks.swedbank.serviceprovider.rpc;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonIgnore;
import java.util.Date;
import lombok.Getter;
import se.tink.backend.aggregation.annotations.JsonObject;

@Getter
@JsonObject
public abstract class AbstractTransactionEntity {
    @JsonFormat(pattern = "yyyy-MM-dd")
    protected Date date;

    protected String description;
    protected String text;
    protected String currency;
    protected String amount;

    @JsonIgnore
    protected String getTransactionDescription() {
        return this.text != null ? this.text : this.description;
    }
}
