package se.tink.backend.aggregation.agents.nxgen.de.openbanking.sparkassen.fetcher;

import java.math.BigDecimal;
import java.util.Arrays;
import java.util.stream.Collectors;
import java.util.stream.IntStream;
import se.tink.backend.aggregation.agents.nxgen.de.openbanking.sparkassen.fetcher.entities.AccountEntity;
import se.tink.backend.aggregation.agents.nxgen.de.openbanking.sparkassen.fetcher.rpc.FetchAccountsResponse;
import se.tink.backend.aggregation.agents.nxgen.de.openbanking.sparkassen.fetcher.rpc.FetchBalancesResponse;
import se.tink.libraries.serialization.utils.SerializationUtils;

public class FetcherTestData {

    public static final FetchAccountsResponse NULL_ACCOUNTS =
            SerializationUtils.deserializeFromString(
                    "{\"accounts\": null}", FetchAccountsResponse.class);

    public static FetchBalancesResponse getFetchBalancesResponse(
            String currency, BigDecimal... amounts) {
        String json = "{\n" + "\t\"account\": null,\n" + "\t\"balances\": [";
        json +=
                Arrays.stream(amounts)
                        .map(amount -> getBalanceEntityJsonString(currency, amount))
                        .collect(Collectors.joining(","));
        json += "]}";
        return SerializationUtils.deserializeFromString(json, FetchBalancesResponse.class);
    }

    private static String getBalanceEntityJsonString(String currency, BigDecimal amount) {
        return "{\n"
                + "    \"balanceAmount\": {\n"
                + "        \"amount\": \""
                + amount
                + "\",\n"
                + "        \"currency\": \""
                + currency
                + "\"\n"
                + "    },\n"
                + "    \"balanceType\": \"ASDF\",\n"
                + "    \"referenceDate\": \"ASDF\"\n"
                + "}";
    }

    public static FetchAccountsResponse getFetchAccountsResponse(int correctAccounts) {
        String json = "{\"accounts\": [";
        json +=
                IntStream.rangeClosed(1, correctAccounts)
                        .mapToObj(
                                x ->
                                        getAccountEntityJsonString(
                                                "CACC",
                                                "EUR",
                                                "DE86999999990000001000",
                                                "Asdf",
                                                "Asdf",
                                                "Asdf",
                                                "Asdf"))
                        .collect(Collectors.joining(","));
        json += "]}";

        return SerializationUtils.deserializeFromString(json, FetchAccountsResponse.class);
    }

    public static AccountEntity getAccountEntity(
            String accountType,
            String currency,
            String iban,
            String name,
            String ownerName,
            String product,
            String resourceId) {
        return SerializationUtils.deserializeFromString(
                getAccountEntityJsonString(
                        accountType, currency, iban, name, ownerName, product, resourceId),
                AccountEntity.class);
    }

    private static String getAccountEntityJsonString(
            String accountType,
            String currency,
            String iban,
            String name,
            String ownerName,
            String product,
            String resourceId) {
        return "{\n"
                + "    \"cashAccountType\": \""
                + accountType
                + "\",\n"
                + "    \"currency\": \""
                + currency
                + "\",\n"
                + "    \"iban\": \""
                + iban
                + "\",\n"
                + "    \"name\": \""
                + name
                + "\",\n"
                + "    \"ownerName\": \""
                + ownerName
                + "\",\n"
                + "    \"product\": \""
                + product
                + "\",\n"
                + "    \"resourceId\": \""
                + resourceId
                + "\"\n"
                + "}";
    }

    public static String getTransactionsResponse() {
        return "<?xml version=\"1.0\" encoding=\"UTF-8\"?> <Document xmlns=\"urn:iso:std:iso:20022:tech:xsd:camt.052.001.02\" xmlns:xsi=\"http://www.w3.org/2001/XMLSchema-instance\" xsi:schemaLocation=\"urn:iso:std:iso:20022:tech:xsd:camt.052.001.02 camt.052.001.02.xsd\"> <BkToCstmrAcctRpt> <GrpHdr> <MsgId>camt52_20200205090407__ONLINEBA</MsgId> <CreDtTm>2020-02-05T09:04:07+01:00</CreDtTm> <MsgPgntn> <PgNb>1</PgNb> <LastPgInd>true</LastPgInd> </MsgPgntn> </GrpHdr> <Rpt> <Id>camt052_ONLINEBA</Id> <ElctrncSeqNb>00000</ElctrncSeqNb> <CreDtTm>2020-02-05T09:04:07+01:00</CreDtTm> <Acct> <Id> <IBAN>DE05999999990000001003</IBAN> </Id> <Ccy>EUR</Ccy> <Svcr> <FinInstnId> <BIC>ASDFGHJK</BIC> <Nm>SUPER NAME</Nm> <Othr> <Id>DE 123456789</Id> <Issr>UmsStId</Issr> </Othr> </FinInstnId> </Svcr> </Acct> <Bal> <Tp> <CdOrPrtry> <Cd>PRCD</Cd> </CdOrPrtry> </Tp> <Amt Ccy=\"EUR\">112.44</Amt> <CdtDbtInd>CRDT</CdtDbtInd> <Dt> <Dt>2019-11-08</Dt> </Dt> </Bal> <Bal> <Tp> <CdOrPrtry> <Cd>CLBD</Cd> </CdOrPrtry> </Tp> <Amt Ccy=\"EUR\">4323.49</Amt> <CdtDbtInd>CRDT</CdtDbtInd> <Dt> <Dt>2020-02-04</Dt> </Dt> </Bal> <Ntry> <Amt Ccy=\"EUR\">2112.40</Amt> <CdtDbtInd>CRDT</CdtDbtInd> <Sts>BOOK</Sts> <BookgDt> <Dt>2019-11-11</Dt> </BookgDt> <ValDt> <Dt>2019-11-11</Dt> </ValDt> <AcctSvcrRef>NONREF</AcctSvcrRef> <BkTxCd/> <NtryDtls> <TxDtls> <Refs> <Prtry> <Tp>FI-UMSATZ-ID</Tp> <Ref>2019-11-11-09.25.19.222641</Ref> </Prtry> </Refs> <BkTxCd> <Prtry> <Cd>NTRF+166+9249+888</Cd> <Issr>DK</Issr> </Prtry> </BkTxCd> <RltdPties> <Dbtr> <Nm>Whoever Watson</Nm> </Dbtr> <DbtrAcct> <Id> <IBAN>DE86999999990000001000</IBAN> </Id> </DbtrAcct> <Cdtr> <Nm>John Doe</Nm> </Cdtr> <CdtrAcct> <Id> <IBAN>DE05999999990000001003</IBAN> </Id> </CdtrAcct> </RltdPties> <RltdAgts> <DbtrAgt> <FinInstnId> <BIC>ZXCVBNM</BIC> </FinInstnId> </DbtrAgt> <CdtrAgt> <FinInstnId> <BIC>SDFGHJKL</BIC> </FinInstnId> </CdtrAgt> </RltdAgts> <RmtInf> <Ustrd>UNSTRUCTUREDDESCRIPTION001</Ustrd> </RmtInf> </TxDtls> </NtryDtls> <AddtlNtryInf>ADDINFOENTRY001</AddtlNtryInf> </Ntry> <Ntry> <Amt Ccy=\"EUR\">123.00</Amt> <CdtDbtInd>DBIT</CdtDbtInd> <Sts>BOOK</Sts> <BookgDt> <Dt>2019-11-12</Dt> </BookgDt> <ValDt> <Dt>2019-11-12</Dt> </ValDt> <AcctSvcrRef>NONREF</AcctSvcrRef> <BkTxCd/> <NtryDtls> <TxDtls> <Refs> <EndToEndId>112415235123</EndToEndId> <MndtId>czxcbsdfgs</MndtId> <Prtry> <Tp>FI-UMSATZ-ID</Tp> <Ref>2019-11-12-04.01.11.25552</Ref> </Prtry> </Refs> <BkTxCd> <Prtry> <Cd>NDDT+105+9248+992</Cd> <Issr>DK</Issr> </Prtry> </BkTxCd> <RltdPties> <Dbtr> <Nm>John Doe</Nm> </Dbtr> <DbtrAcct> <Id> <IBAN>DE05999999990000001003</IBAN> </Id> </DbtrAcct> <Cdtr> <Nm>CREDITOR002</Nm> <Id> <PrvtId> <Othr> <Id>LUIDPPPAASD12001</Id> </Othr> </PrvtId> </Id> </Cdtr> <CdtrAcct> <Id> <IBAN>DE75999999990000001004</IBAN> </Id> </CdtrAcct> </RltdPties> <RltdAgts> <DbtrAgt> <FinInstnId> <BIC>QWERTYUIO</BIC> </FinInstnId> </DbtrAgt> <CdtrAgt> <FinInstnId> <BIC>ASDFGHJKL</BIC> </FinInstnId> </CdtrAgt> </RltdAgts> <RmtInf> <Ustrd>UNSTRUCTUREDDESCRIPTION002</Ustrd> </RmtInf> </TxDtls> </NtryDtls> <AddtlNtryInf>ADDINFOENTRY002</AddtlNtryInf> </Ntry> <Ntry> <Amt Ccy=\"EUR\">223.14</Amt> <CdtDbtInd>DBIT</CdtDbtInd> <Sts>BOOK</Sts> <BookgDt> <Dt>2019-11-12</Dt> </BookgDt> <ValDt> <Dt>2019-11-12</Dt> </ValDt> <AcctSvcrRef>NONREF</AcctSvcrRef> <BkTxCd/> <NtryDtls> <TxDtls> <Refs> <EndToEndId>1007114711251 PAYPAL</EndToEndId> <MndtId>4JMJ224MVA8T4</MndtId> <Prtry> <Tp>FI-UMSATZ-ID</Tp> <Ref>2019-11-12-04.06.13.391512</Ref> </Prtry> </Refs> <BkTxCd> <Prtry> <Cd>NDDT+105+9248+992</Cd> <Issr>DK</Issr> </Prtry> </BkTxCd> <RltdPties> <Dbtr> <Nm>John Doe</Nm> </Dbtr> <DbtrAcct> <Id> <IBAN>DE05999999990000001003</IBAN> </Id> </DbtrAcct> <Cdtr> <Nm>CREDITOR003</Nm> <Id> <PrvtId> <Othr> <Id>LUIDPPPAASD12001</Id> </Othr> </PrvtId> </Id> </Cdtr> <CdtrAcct> <Id> <IBAN>DE75999999990000001004</IBAN> </Id> </CdtrAcct> </RltdPties> <RltdAgts> <DbtrAgt> <FinInstnId> <BIC>ASDFGHJKL</BIC> </FinInstnId> </DbtrAgt> <CdtrAgt> <FinInstnId> <BIC>ZXCVBNM</BIC> </FinInstnId> </CdtrAgt> </RltdAgts> <RmtInf> <Ustrd>UNSTRUCTUREDDESCRIPTION003</Ustrd> </RmtInf> </TxDtls> </NtryDtls> <AddtlNtryInf>ADDINFOENTRY003</AddtlNtryInf> </Ntry> </Rpt> </BkToCstmrAcctRpt> </Document>";
    }

    public static String getTransactionsResponseWithNoTransactions() {
        return "<?xml version=\"1.0\" encoding=\"UTF-8\"?> <Document xmlns=\"urn:iso:std:iso:20022:tech:xsd:camt.052.001.02\" xmlns:xsi=\"http://www.w3.org/2001/XMLSchema-instance\" xsi:schemaLocation=\"urn:iso:std:iso:20022:tech:xsd:camt.052.001.02 camt.052.001.02.xsd\"> <BkToCstmrAcctRpt> <GrpHdr> <MsgId>camt52_20200205090407__ONLINEBA</MsgId> <CreDtTm>2020-02-05T09:04:07+01:00</CreDtTm> <MsgPgntn> <PgNb>1</PgNb> <LastPgInd>true</LastPgInd> </MsgPgntn> </GrpHdr> <Rpt> <Id>camt052_ONLINEBA</Id> <ElctrncSeqNb>00000</ElctrncSeqNb> <CreDtTm>2020-02-05T09:04:07+01:00</CreDtTm> <Acct> <Id> <IBAN>DE05999999990000001003</IBAN> </Id> <Ccy>EUR</Ccy> <Svcr> <FinInstnId> <BIC>ASDFGHJK</BIC> <Nm>SUPER NAME</Nm> <Othr> <Id>DE 123456789</Id> <Issr>UmsStId</Issr> </Othr> </FinInstnId> </Svcr> </Acct> <Bal> <Tp> <CdOrPrtry> <Cd>PRCD</Cd> </CdOrPrtry> </Tp> <Amt Ccy=\"EUR\">112.44</Amt> <CdtDbtInd>CRDT</CdtDbtInd> <Dt> <Dt>2019-11-08</Dt> </Dt> </Bal> <Bal> <Tp> <CdOrPrtry> <Cd>CLBD</Cd> </CdOrPrtry> </Tp> <Amt Ccy=\"EUR\">4323.49</Amt> <CdtDbtInd>CRDT</CdtDbtInd> <Dt> <Dt>2020-02-04</Dt> </Dt> </Bal> </Rpt> </BkToCstmrAcctRpt> </Document>";
    }
}
