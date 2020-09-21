package se.tink.backend.aggregation.agents.nxgen.de.banks.fints.mapper.transaction.detail;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.junit.Test;
import se.tink.backend.aggregation.agents.models.TransactionTypes;
import se.tink.backend.aggregation.nxgen.core.transaction.AggregationTransaction;
import se.tink.backend.aggregation.nxgen.core.transaction.Transaction;
import se.tink.libraries.amount.ExactCurrencyAmount;

public class DefaultCamtTransactionMapperTest {
    private static final String TEST_CAMT_PAYLOAD =
            "<?xml version=\"1.0\" encoding=\"UTF-8\"?> <Document xmlns=\"urn:iso:std:iso:20022:tech:xsd:camt.052.001.02\" xmlns:xsi=\"http://www.w3.org/2001/XMLSchema-instance\" xsi:schemaLocation=\"urn:iso:std:iso:20022:tech:xsd:camt.052.001.02 sample.xsd\"> <BkToCstmrAcctRpt> <GrpHdr> <MsgId>camt52_20200205090407__ONLINEBA</MsgId> <CreDtTm>2020-02-05T09:04:07+01:00</CreDtTm> <MsgPgntn> <PgNb>1</PgNb> <LastPgInd>true</LastPgInd> </MsgPgntn> </GrpHdr> <Rpt> <Id>camt052_ONLINEBA</Id> <ElctrncSeqNb>00000</ElctrncSeqNb> <CreDtTm>2020-02-05T09:04:07+01:00</CreDtTm> <Acct> <Id> <IBAN>DE05999999990000001003</IBAN> </Id> <Ccy>EUR</Ccy> <Svcr> <FinInstnId> <BIC>OKOYFIHH</BIC> <Nm>SUPER NAME</Nm> <Othr> <Id>DE 123456789</Id> <Issr>UmsStId</Issr> </Othr> </FinInstnId> </Svcr> </Acct> <Bal> <Tp> <CdOrPrtry> <Cd>PRCD</Cd> </CdOrPrtry> </Tp> <Amt Ccy=\"EUR\">112.44</Amt> <CdtDbtInd>CRDT</CdtDbtInd> <Dt> <Dt>2019-11-08</Dt> </Dt> </Bal> <Bal> <Tp> <CdOrPrtry> <Cd>CLBD</Cd> </CdOrPrtry> </Tp> <Amt Ccy=\"EUR\">4323.49</Amt> <CdtDbtInd>CRDT</CdtDbtInd> <Dt> <Dt>2020-02-04</Dt> </Dt> </Bal> <Ntry> <Amt Ccy=\"EUR\">2112.40</Amt> <CdtDbtInd>CRDT</CdtDbtInd> <Sts>BOOK</Sts> <BookgDt> <Dt>2019-11-11</Dt> </BookgDt> <ValDt> <Dt>2019-11-11</Dt> </ValDt> <AcctSvcrRef>NONREF</AcctSvcrRef> <BkTxCd/> <NtryDtls> <TxDtls> <Refs> <Prtry> <Tp>FI-UMSATZ-ID</Tp> <Ref>2019-11-11-09.25.19.222641</Ref> </Prtry> </Refs> <BkTxCd> <Prtry> <Cd>NTRF+166+9249+888</Cd> <Issr>DK</Issr> </Prtry> </BkTxCd> <RltdPties> <Dbtr> <Nm>Whoever Watson</Nm> </Dbtr> <DbtrAcct> <Id> <IBAN>DE86999999990000001000</IBAN> </Id> </DbtrAcct> <Cdtr> <Nm>John Doe</Nm> </Cdtr> <CdtrAcct> <Id> <IBAN>DE05999999990000001003</IBAN> </Id> </CdtrAcct> </RltdPties> <RltdAgts> <DbtrAgt> <FinInstnId> <BIC>OKOYFIHH</BIC> </FinInstnId> </DbtrAgt> <CdtrAgt> <FinInstnId> <BIC>OKOYFIHH</BIC> </FinInstnId> </CdtrAgt> </RltdAgts> <RmtInf> <Ustrd>UNSTRUCTUREDDESCRIPTION001</Ustrd> </RmtInf> </TxDtls> </NtryDtls> <AddtlNtryInf>ADDINFOENTRY001</AddtlNtryInf> </Ntry> <Ntry> <Amt Ccy=\"EUR\">123.00</Amt> <CdtDbtInd>DBIT</CdtDbtInd> <Sts>BOOK</Sts> <BookgDt> <Dt>2019-11-12</Dt> </BookgDt> <ValDt> <Dt>2019-11-12</Dt> </ValDt> <AcctSvcrRef>NONREF</AcctSvcrRef> <BkTxCd/> <NtryDtls> <TxDtls> <Refs> <EndToEndId>112415235123</EndToEndId> <MndtId>czxcbsdfgs</MndtId> <Prtry> <Tp>FI-UMSATZ-ID</Tp> <Ref>2019-11-12-04.01.11.25552</Ref> </Prtry> </Refs> <BkTxCd> <Prtry> <Cd>NDDT+105+9248+992</Cd> <Issr>DK</Issr> </Prtry> </BkTxCd> <RltdPties> <Dbtr> <Nm>John Doe</Nm> </Dbtr> <DbtrAcct> <Id> <IBAN>DE05999999990000001003</IBAN> </Id> </DbtrAcct> <Cdtr> <Nm>CREDITOR002</Nm> <Id> <PrvtId> <Othr> <Id>LUIDPPPAASD12001</Id> </Othr> </PrvtId> </Id> </Cdtr> <CdtrAcct> <Id> <IBAN>DE75999999990000001004</IBAN> </Id> </CdtrAcct> </RltdPties> <RltdAgts> <DbtrAgt> <FinInstnId> <BIC>OKOYFIHH</BIC> </FinInstnId> </DbtrAgt> <CdtrAgt> <FinInstnId> <BIC>OKOYFIHH</BIC> </FinInstnId> </CdtrAgt> </RltdAgts> <RmtInf> <Ustrd>UNSTRUCTUREDDESCRIPTION002</Ustrd> </RmtInf> </TxDtls> </NtryDtls> <AddtlNtryInf>ADDINFOENTRY002</AddtlNtryInf> </Ntry> <Ntry> <Amt Ccy=\"EUR\">223.14</Amt> <CdtDbtInd>DBIT</CdtDbtInd> <Sts>BOOK</Sts> <BookgDt> <Dt>2019-11-12</Dt> </BookgDt> <ValDt> <Dt>2019-11-12</Dt> </ValDt> <AcctSvcrRef>NONREF</AcctSvcrRef> <BkTxCd/> <NtryDtls> <TxDtls> <Refs> <EndToEndId>1007114711251 PAYPAL</EndToEndId> <MndtId>4JMJ224MVA8T4</MndtId> <Prtry> <Tp>FI-UMSATZ-ID</Tp> <Ref>2019-11-12-04.06.13.391512</Ref> </Prtry> </Refs> <BkTxCd> <Prtry> <Cd>NDDT+105+9248+992</Cd> <Issr>DK</Issr> </Prtry> </BkTxCd> <RltdPties> <Dbtr> <Nm>John Doe</Nm> </Dbtr> <DbtrAcct> <Id> <IBAN>DE05999999990000001003</IBAN> </Id> </DbtrAcct> <Cdtr> <Nm>CREDITOR003</Nm> <Id> <PrvtId> <Othr> <Id>LUIDPPPAASD12001</Id> </Othr> </PrvtId> </Id> </Cdtr> <CdtrAcct> <Id> <IBAN>DE75999999990000001004</IBAN> </Id> </CdtrAcct> </RltdPties> <RltdAgts> <DbtrAgt> <FinInstnId> <BIC>OKOYFIHH</BIC> </FinInstnId> </DbtrAgt> <CdtrAgt> <FinInstnId> <BIC>OKOYFIHH</BIC> </FinInstnId> </CdtrAgt> </RltdAgts> <RmtInf> <Ustrd>UNSTRUCTUREDDESCRIPTION003</Ustrd> </RmtInf> </TxDtls> </NtryDtls> <AddtlNtryInf>ADDINFOENTRY003</AddtlNtryInf> </Ntry> <Ntry> <Amt Ccy=\"EUR\">223.14</Amt> <CdtDbtInd>DBIT</CdtDbtInd> <Sts>BOOK</Sts> <BookgDt> <Dt>2019-11-12</Dt> </BookgDt> <ValDt> <Dt>2019-11-12</Dt> </ValDt> <AcctSvcrRef>NONREF</AcctSvcrRef> <BkTxCd/> <NtryDtls> <TxDtls> <Refs> <EndToEndId>1007114711251 PAYPAL</EndToEndId> <MndtId>4JMJ224MVA8T4</MndtId> <Prtry> <Tp>FI-UMSATZ-ID</Tp> <Ref>2019-11-12-04.06.13.391512</Ref> </Prtry> </Refs> <BkTxCd> <Prtry> <Cd>NDDT+105+9248+992</Cd> <Issr>DK</Issr> </Prtry> </BkTxCd> <RltdPties> <Dbtr> <Nm>John Doe</Nm> </Dbtr> <DbtrAcct> <Id> <IBAN>DE05999999990000001003</IBAN> </Id> </DbtrAcct> <Cdtr> <Nm>PayPal Europe S.a.r.l. et Cie S.C.A</Nm> <Id> <PrvtId> <Othr> <Id>LUIDPPPAASD12001</Id> </Othr> </PrvtId> </Id> </Cdtr> <CdtrAcct> <Id> <IBAN>DE75999999990000001004</IBAN> </Id> </CdtrAcct> </RltdPties> <RltdAgts> <DbtrAgt> <FinInstnId> <BIC>OKOYFIHH</BIC> </FinInstnId> </DbtrAgt> <CdtrAgt> <FinInstnId> <BIC>OKOYFIHH</BIC> </FinInstnId> </CdtrAgt> </RltdAgts> <RmtInf> <Ustrd>UNSTRUCTUREDDESCRIPTION004</Ustrd> </RmtInf> </TxDtls> </NtryDtls> <AddtlNtryInf>ADDINFOENTRY003</AddtlNtryInf> </Ntry> </Rpt> </BkToCstmrAcctRpt> </Document>";
    private static final Map<String, ExactCurrencyAmount> EXPECTED_DATA = new HashMap<>();

    static {
        EXPECTED_DATA.put(
                "Whoever Watson UNSTRUCTUREDDESCRIPTION001", ExactCurrencyAmount.of(2112.4, "EUR"));
        EXPECTED_DATA.put(
                "CREDITOR002 UNSTRUCTUREDDESCRIPTION002", ExactCurrencyAmount.of(-123.0, "EUR"));
        EXPECTED_DATA.put(
                "CREDITOR003 UNSTRUCTUREDDESCRIPTION003", ExactCurrencyAmount.of(-223.14, "EUR"));
        EXPECTED_DATA.put("UNSTRUCTUREDDESCRIPTION004", ExactCurrencyAmount.of(-223.14, "EUR"));
    }

    @Test
    public void shouldGetAllTransactionsMappedProperly() {
        // given
        String camtPayload = TEST_CAMT_PAYLOAD;

        // when
        List<AggregationTransaction> transactions =
                new DefaultCamtTransactionMapper().parse(camtPayload);

        // then
        assertThat(transactions).hasSize(4);
        EXPECTED_DATA.forEach(
                (expectedTransactionDescription, expectedAmount) -> {
                    AggregationTransaction transactionUnderVerification =
                            getTransactionByDescription(
                                    transactions, expectedTransactionDescription);
                    assertThat(transactionUnderVerification).isExactlyInstanceOf(Transaction.class);
                    assertThat(transactionUnderVerification.getExactAmount())
                            .isEqualTo(expectedAmount);
                    assertThat(transactionUnderVerification.getType())
                            .isEqualTo(TransactionTypes.DEFAULT);
                    assertThat(((Transaction) transactionUnderVerification).isPending()).isFalse();
                });
    }

    private AggregationTransaction getTransactionByDescription(
            List<AggregationTransaction> transactions, String expectedTransactionDescription) {
        return transactions.stream()
                .filter(
                        transaction ->
                                expectedTransactionDescription.equals(transaction.getDescription()))
                .findFirst()
                .get();
    }
}
