package se.tink.backend.aggregation.agents.nxgen.se.banks.seb.entities;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;

@JsonIgnoreProperties(ignoreUnknown = true)
public class UserInformation {
    @JsonProperty("USER_NAME")
    private String userName;

    @JsonProperty("SEB_KUND_NR")
    private String sebCustomerNumber; // 10-digit personnummer + 4 digits

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

    @JsonProperty("SEB_BID_USERID12")
    private String bankIdUserId; // 12-digit personnummer

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
