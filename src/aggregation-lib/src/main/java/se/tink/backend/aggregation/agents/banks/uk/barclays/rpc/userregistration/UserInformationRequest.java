package se.tink.backend.aggregation.agents.banks.uk.barclays.rpc.userregistration;

import java.util.LinkedHashMap;
import java.util.Map;
import se.tink.backend.aggregation.agents.banks.uk.barclays.BarclaysConstants;
import se.tink.backend.aggregation.agents.banks.uk.barclays.rpc.Request;

public class UserInformationRequest implements Request {
    /*
    sortCode=111111
    opCode=NR001
    surname=XXXXXXXXXX
    title=Other
    forename=XXXXXXXXXXXX
    serviceCode=BMB
    accountNumber=11111111
    phoneNumber=+44XXXXXXXXX
     */
    private String sortCode;
    private String lastName;
    private String firstName;
    private String accountNumber;
    private String phoneNumber;

    public String getCommandId() {
        return BarclaysConstants.COMMAND.SEND_USERINFO;
    }

    public Map<String, String> getBody() {
        Map<String, String> m = new LinkedHashMap<>();
        m.put("sortCode", sortCode);
        m.put("surname", lastName);
        m.put("title", "other");
        m.put("forename", firstName);
        m.put("serviceCode", BarclaysConstants.PLATFORM_ID);
        m.put("accountNumber", accountNumber);
        m.put("phoneNumber", phoneNumber);
        return m;
    }

    public void setSortCode(String sortCode) {
        this.sortCode = sortCode;
    }

    public void setLastName(String lastName) {
        this.lastName = lastName;
    }

    public void setFirstName(String firstName) {
        this.firstName = firstName;
    }

    public void setAccountNumber(String accountNumber) {
        this.accountNumber = accountNumber;
    }

    public void setPhoneNumber(String phoneNumber) {
        this.phoneNumber = phoneNumber;
    }
}
