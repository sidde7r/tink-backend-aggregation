package se.tink.backend.aggregation.agents.nxgen.serviceproviders.banks.sdc.fetcher.entities;

import com.fasterxml.jackson.annotation.JsonAutoDetect;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

/*
    Data:
"accountId": "9110.1233013631",
"agreementId": "117000633169711",
"refNumber": "1305361818",
"amount": "-3900",
"paymentDate": "2017-07-03",
"hostTms": "2017-07-03-21.21.44.358783",
"pstgWsId": "YGDS0333",
"pciHostTimeStamp": "0000-00-00-00.00.00.000000",
"dtCreate": "2017-07-03",
"ownText": "",
"balance": "-4711",
"ldbId": "01140",
"afrIdfr": "117063336169711",
"pccgHostTs": ""

*/

@JsonIgnoreProperties(ignoreUnknown = true)
@JsonAutoDetect(fieldVisibility = JsonAutoDetect.Visibility.ANY)
public class SdcTransactionEntityKey {
    private String accountId;
    private String agreementId;
    private String refNumber;
    private String amount;
    private String paymentDate;
    private String hostTms;
    private String pstgWsId;
    private String pciHostTimeStamp;
    private String dtCreate;
    private String ownText;
    private String balance;
    private String ldbId;
    private String afrIdfr;
    private String pccgHostTs;

    public String getAccountId() {
        return accountId;
    }

    public String getAgreementId() {
        return agreementId;
    }

    public String getRefNumber() {
        return refNumber;
    }

    public String getAmount() {
        return amount;
    }

    public String getPaymentDate() {
        return paymentDate;
    }

    public String getHostTms() {
        return hostTms;
    }

    public String getPstgWsId() {
        return pstgWsId;
    }

    public String getPciHostTimeStamp() {
        return pciHostTimeStamp;
    }

    public String getDtCreate() {
        return dtCreate;
    }

    public String getOwnText() {
        return ownText;
    }

    public String getBalance() {
        return balance;
    }

    public String getLdbId() {
        return ldbId;
    }

    public String getAfrIdfr() {
        return afrIdfr;
    }

    public String getPccgHostTs() {
        return pccgHostTs;
    }
}
