package se.tink.backend.aggregation.agents.nxgen.se.banks.seb.entities;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;
import se.tink.backend.aggregation.annotations.JsonObject;

@JsonObject
public class UserInformation {
    @JsonProperty("USER_NAME")
    private String userName;

    // 10-digit personnummer + 4 digits
    @JsonProperty("SEB_KUND_NR")
    private String sebCustomerNumber;

    @JsonProperty("BUNT_ANTAL_OSIGNERAD")
    private String numberOfUnsignedPayments;

    @JsonProperty("EFACTURA_ANTAL_OSIGN")
    private String numberOfUnsignedEInvoices;

    @JsonProperty("IMS_SHORT_USERID")
    private String imsShortUserId;

    @JsonProperty("LOGON_COMPLETED")
    private String logonCompleted;

    @JsonProperty("STATUS")
    private String status;

    @JsonProperty("UNIQUE_ID")
    private String uniqueId;

    @JsonProperty("LOGIN_METHOD")
    private String loginMethod;

    @JsonProperty("GENDER")
    private String gender;

    @JsonProperty("AGE")
    private String age;

    // 12-digit personnummer
    @JsonProperty("SEB_BID_USERID12")
    private String bankIdUserId;

    @JsonIgnore
    public String getUserName() {
        return userName;
    }

    @JsonIgnore
    public String getSebCustomerNumber() {
        return sebCustomerNumber;
    }

    @JsonIgnore
    public String getShortUserId() {
        return imsShortUserId;
    }

    @JsonIgnore
    public String getSSN() {
        return bankIdUserId;
    }
}
