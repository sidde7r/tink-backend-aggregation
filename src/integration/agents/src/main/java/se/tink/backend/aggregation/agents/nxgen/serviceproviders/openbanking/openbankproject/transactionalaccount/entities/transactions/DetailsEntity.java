package se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.openbankproject.transactionalaccount.entities.transactions;

import com.fasterxml.jackson.annotation.JsonFormat;
import java.util.Date;

public class DetailsEntity {

    private String type;
    private String description;

    @JsonFormat(pattern = "yyyy-mm-dd")
    private Date posted;

    private String completed;
    private NewBalanceEntity new_balance;
    private ValueEntity value;

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public String getDescription() {
        return description;
    }

    public String getCompleted() {
        return completed;
    }

    public Date getPosted() {
        return posted;
    }

    public NewBalanceEntity getNew_balance() {
        return new_balance;
    }

    public ValueEntity getValue() {
        return value;
    }
}
