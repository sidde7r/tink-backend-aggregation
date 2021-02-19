package se.tink.backend.aggregation.agents.nxgen.de.banks.fints.fetcher.transactionalaccount;

import java.util.Collections;
import org.apache.commons.lang3.tuple.Pair;
import org.junit.Ignore;
import se.tink.backend.aggregation.agents.nxgen.de.banks.fints.FinTsAccountInformation;
import se.tink.backend.aggregation.agents.nxgen.de.banks.fints.FinTsDialogContext;
import se.tink.backend.aggregation.agents.nxgen.de.banks.fints.configuration.Bank;
import se.tink.backend.aggregation.agents.nxgen.de.banks.fints.configuration.FinTsConfiguration;
import se.tink.backend.aggregation.agents.nxgen.de.banks.fints.configuration.FinTsSecretsConfiguration;
import se.tink.backend.aggregation.agents.nxgen.de.banks.fints.protocol.parts.response.HICAZS;
import se.tink.backend.aggregation.agents.nxgen.de.banks.fints.protocol.parts.response.HIPINS;
import se.tink.backend.aggregation.agents.nxgen.de.banks.fints.protocol.parts.response.HISPA;
import se.tink.backend.aggregation.agents.nxgen.de.banks.fints.protocol.parts.response.HIUPD;
import se.tink.backend.aggregation.agents.nxgen.de.banks.fints.security.tan.SegmentType;
import se.tink.backend.aggregation.agents.nxgen.de.banks.fints.security.tan.TanByOperationLookup;

@Ignore
public class TestFixtures {

    static final String FETCH_TRANSACTIONS_RESPONSE_IN_XML =
            "HNHBK:1:3+000000003652+300+JC0032413584343+5+JC0032413584343:5'HNVSK:998:3+PIN:2+998+1+2::osNPJZ5/DHEBAAD0SEImhm?+owAQA+1:20200324:140046+2:2:13:@8@00000000:5:1+280:70169466:119546769:S:0:0+0'HNVSD:999:1+@3429@HNSHK:2:4+PIN:2+944+6193858+1+1+2::osNPJZ5/DHEBAAD0SEImhm?+owAQA+1+1:20200324:140046+1:999:1+6:10:16+280:70169466:119546769:S:0:0'HIRMG:3:2+0010::Nachricht entgegengenommen.'HIRMS:4:2:3+0020::*Abfrage CAMT Umsätze erfolgreich durchgeführt+0900::*TAN entwertet.'HITAN:5:6:3+2++ERNQJZ5/DHEBAAD0SEImhm?+owAQA'HICAZ:6:1:3+DE32701694660000979163:GENODEF1M03:979163::280:70169466+urn?:iso?:std?:iso?:20022?:tech?:xsd?:camt.052.001.02+@2976@<?xml version=\"1.0\" encoding=\"ISO-8859-1\" ?><Document xmlns=\"urn:iso:std:iso:20022:tech:xsd:camt.052.001.02\" xmlns:xsi=\"http://www.w3.org/2001/XMLSchema-instance\" xsi:schemaLocation=\"urn:iso:std:iso:20022:tech:xsd:camt.052.001.02 camt.052.001.02.xsd\"><BkToCstmrAcctRpt><GrpHdr><MsgId>052D2020-03-24T14:00:46.0N000000000</MsgId><CreDtTm>2020-03-24T14:00:46.0+01:00</CreDtTm><MsgPgntn><PgNb>1</PgNb><LastPgInd>true</LastPgInd></MsgPgntn></GrpHdr><Rpt><Id>3171C5220200324140046</Id><ElctrncSeqNb>000000000</ElctrncSeqNb><CreDtTm>2020-03-24T14:00:46.0+01:00</CreDtTm><Acct><Id><IBAN>DE32701694660000979163</IBAN></Id><Ccy>EUR</Ccy><Ownr><Nm>Jan Gillaaa</Nm></Ownr><Svcr><FinInstnId><BIC>GENODEF1M03</BIC><Nm>Raiffeisenbank</Nm><Othr><Id>DE 129511838</Id><Issr>UmsStId</Issr></Othr></FinInstnId></Svcr></Acct><Bal><Tp><CdOrPrtry><Cd>PRCD</Cd></CdOrPrtry></Tp><Amt Ccy=\"EUR\">277.56</Amt><CdtDbtInd>CRDT</CdtDbtInd><Dt><Dt>2020-01-30</Dt></Dt></Bal><Bal><Tp><CdOrPrtry><Cd>CLBD</Cd></CdOrPrtry></Tp><Amt Ccy=\"EUR\">275.56</Amt><CdtDbtInd>CRDT</CdtDbtInd><Dt><Dt>2020-03-02</Dt></Dt></Bal><Ntry><Amt Ccy=\"EUR\">1.70</Amt><CdtDbtInd>DBIT</CdtDbtInd><Sts>BOOK</Sts><BookgDt><Dt>2020-01-30</Dt></BookgDt><ValDt><Dt>2020-01-30</Dt></ValDt><AcctSvcrRef>2020013000533052000</AcctSvcrRef><BkTxCd/><NtryDtls><TxDtls><Refs><MsgId>xzQRTwxjAscBLUlZAxc</MsgId><PmtInfId>0000979163/000000001/  V00001</PmtInfId><EndToEndId>NOTPROVIDED</EndToEndId></Refs><BkTxCd><Prtry><Cd>NTRF+117+00900</Cd><Issr>ZKA</Issr></Prtry></BkTxCd><RltdPties><Dbtr><Nm>Jan Gillaaa</Nm></Dbtr><DbtrAcct><Id><IBAN>DE32701694660000979163</IBAN></Id></DbtrAcct><Cdtr><Nm>Jan Gillaaa</Nm></Cdtr><CdtrAcct><Id><IBAN>DE13100100100554481127</IBAN></Id></CdtrAcct></RltdPties><RltdAgts><CdtrAgt><FinInstnId><BIC>PBNKDEFFXXX</BIC></FinInstnId></CdtrAgt></RltdAgts><Purp><Cd>RINP</Cd></Purp><RmtInf><Ustrd>To own account IBAN: DE13100100100554481127 BIC: PBNKDEFF123</Ustrd></RmtInf></TxDtls></NtryDtls><AddtlNtryInf>DAUERAUFTRAG        </AddtlNtryInf></Ntry><Ntry><Amt Ccy=\"EUR\">1.00</Amt><CdtDbtInd>DBIT</CdtDbtInd><Sts>BOOK</Sts><BookgDt><Dt>2020-03-02</Dt></BookgDt><ValDt><Dt>2020-03-02</Dt></ValDt><AcctSvcrRef>2020030203530127000</AcctSvcrRef><BkTxCd/><NtryDtls><TxDtls><Refs><MsgId>xyJIfwxjAsednBtfW2o         0000005</MsgId><PmtInfId>0000979163/000000001/  V00001</PmtInfId><EndToEndId>NOTPROVIDED</EndToEndId></Refs><BkTxCd><Prtry><Cd>NTRF+117+00900</Cd><Issr>ZKA</Issr></Prtry></BkTxCd><RltdPties><Dbtr><Nm>Jan Gillaaa</Nm></Dbtr><DbtrAcct><Id><IBAN>DE32701694660000979163</IBAN></Id></DbtrAcct><Cdtr><Nm>Jan Gillaaa</Nm></Cdtr><CdtrAcct><Id><IBAN>DE13100100100554481127</IBAN></Id></CdtrAcct></RltdPties><RltdAgts><CdtrAgt><FinInstnId><BIC>PBNKDEFFXXX</BIC></FinInstnId></CdtrAgt></RltdAgts><Purp><Cd>RINP</Cd></Purp><RmtInf><Ustrd>To own account IBAN: DE13100100100554481127 BIC: AAAAAAAA111</Ustrd></RmtInf></TxDtls></NtryDtls><AddtlNtryInf>DAUERAUFTRAG        </AddtlNtryInf></Ntry></Rpt></BkToCstmrAcctRpt></Document>'HNSHA:7:2+6193858''HNHBS:8:1+5'";

    private TestFixtures() {}

    static FinTsDialogContext getDialogContext() {
        FinTsConfiguration configuration = getFinTsConfiguration();
        FinTsDialogContext context =
                new FinTsDialogContext(configuration, new FinTsSecretsConfiguration(null, null));
        HICAZS hicazs =
                new HICAZS()
                        .setSupportedCamtFormats(
                                Collections.singletonList(
                                        "urn:iso:std:iso:20022:tech:xsd:camt.052.001.02"));
        hicazs.setSegmentVersion(1);
        context.addOperationSupportedByBank(SegmentType.HKCAZ, hicazs);
        context.setTanByOperationLookup(getOperationLookup());
        return context;
    }

    static FinTsConfiguration getFinTsConfiguration() {
        return new FinTsConfiguration(
                "foo", Bank.DEUTSCHE_BANK, "http://localhost:443/foo/bar", "foo", "foo");
    }

    static FinTsAccountInformation getAccount(
            int accType, String iban, String accNumber, String bic, String productName) {
        HIUPD hiupd =
                new HIUPD()
                        .setAccountType(accType)
                        .setIban(iban)
                        .setAccountNumber(accNumber)
                        .setBlz("12312")
                        .setProductName(productName)
                        .setFirstAccountHolder("First Holder");
        hiupd.addAllowedBusinessOperation("HKCAZ", 1);
        HISPA.Detail hispaDetail = new HISPA.Detail().setIban(iban).setBic(bic);
        return new FinTsAccountInformation(hiupd).setSepaDetails(hispaDetail);
    }

    private static TanByOperationLookup getOperationLookup() {
        HIPINS hipins =
                new HIPINS()
                        .setOperations(
                                Collections.singletonList(
                                        Pair.of(SegmentType.HKCAZ.getSegmentName(), false)));
        return new TanByOperationLookup(hipins);
    }
}
