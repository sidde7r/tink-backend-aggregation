package se.tink.backend.aggregation.agents.nxgen.uk.openbanking.starling.featcher.transactional.rpc;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.google.common.base.Strings;
import java.util.Date;

public abstract class AccountHolder {

    private String firstName;
    private String lastName;

    @JsonFormat(pattern = "yyyy-MM-dd")
    private Date dateofBirth;

    private String email;
    private String phone;

    public String getFullName() {
        return (Strings.nullToEmpty(firstName) + " " + Strings.nullToEmpty(lastName)).trim();
    }
}
