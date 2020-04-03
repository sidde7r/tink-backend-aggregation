package se.tink.backend.aggregation.agents.nxgen.de.banks.fints.protocol.parts.request;

import static org.assertj.core.api.Assertions.assertThat;

import java.time.LocalDateTime;
import org.junit.BeforeClass;
import org.junit.Test;
import se.tink.backend.aggregation.agents.nxgen.de.banks.fints.FinTsDialogContext;
import se.tink.backend.aggregation.agents.nxgen.de.banks.fints.configuration.Bank;
import se.tink.backend.aggregation.agents.nxgen.de.banks.fints.configuration.FinTsConfiguration;
import se.tink.backend.aggregation.agents.nxgen.de.banks.fints.configuration.FinTsSecretsConfiguration;
import se.tink.backend.aggregation.agents.nxgen.de.banks.fints.security.SecurityReferenceGenerator;

public class FinTsRequestTest {

    private static final int SECURITY_REFERENCE = 1995881;
    private static final String BLZ = "BLZ_77712";
    private static final String USERNAME = "USERNAME_99123";
    private static final String PRODUCT_ID = "PRODUCTID_8881293";
    private static final String PRODUCT_VERSION = "PRODUCTVERSION_001";
    private static final String PIN = "PIN_99120";
    private static final LocalDateTime dateTime = LocalDateTime.of(2020, 12, 28, 17, 33, 45);

    private static FinTsDialogContext context;

    @BeforeClass
    public static void beforeClass() {
        FinTsConfiguration con =
                new FinTsConfiguration(BLZ, Bank.POSTBANK, "SuperEndpoint", USERNAME, PIN);
        FinTsSecretsConfiguration secCon =
                new FinTsSecretsConfiguration(PRODUCT_ID, PRODUCT_VERSION);
        context =
                new FinTsDialogContext(
                        con,
                        secCon,
                        new SecurityReferenceGenerator() {
                            @Override
                            public int generate() {
                                return SECURITY_REFERENCE;
                            }
                        });
    }

    @Test
    public void shouldSerializeThisBigExampleCorrectly() {
        // given
        FinTsRequest request = new FinTsRequest();

        request.addSegment(
                HNHBKv3.builder()
                        .dialogId(context.getDialogId())
                        .messageNumber(context.getMessageNumber())
                        .build());
        request.addSegment(
                HNVSKv3.builder()
                        .securityProcedureVersion(context.getSecurityProcedureVersion())
                        .blz(BLZ)
                        .systemId(context.getSystemId())
                        .username(USERNAME)
                        .creationTime(dateTime)
                        .build());

        HNVSDv1 hnvsd = new HNVSDv1();
        hnvsd.addSegment(
                HNSHKv4.builder()
                        .securityProcedureVersion(context.getSecurityProcedureVersion())
                        .securityFunction(context.getChosenSecurityFunction())
                        .securityReference(context.getSecurityReference())
                        .systemId(context.getSystemId())
                        .blz(BLZ)
                        .username(USERNAME)
                        .creationTime(dateTime)
                        .build());
        hnvsd.addSegment(
                HKIDNv2.builder()
                        .systemId(context.getSystemId())
                        .blz(BLZ)
                        .username(USERNAME)
                        .build());
        hnvsd.addSegment(
                HKVVBv3.builder().productId(PRODUCT_ID).productVersion(PRODUCT_VERSION).build());
        hnvsd.addSegment(new HKSYNv3());
        hnvsd.addSegment(
                HNSHAv2.builder()
                        .securityReference(context.getSecurityReference())
                        .password(PIN)
                        .build());
        request.addSegment(hnvsd);

        request.addSegment(HNHBSv1.builder().messageNumber(context.getMessageNumber()).build());

        // when
        String requestInFinTsFormat = request.toFinTsFormat();

        // then
        assertThat(requestInFinTsFormat)
                .isEqualTo(
                        "HNHBK:1:3+000000000413+300+0+1'HNVSK:998:3+PIN:1+998+1+1::0+1:20201228:173345+2:2:13:@8@00000000:5:1+280:BLZ_77712:USERNAME_99123:S:0:0+0'HNVSD:999:1+@245@HNSHK:2:4+PIN:1+999+1995881+1+1+1::0+1+1:20201228:173345+1:999:1+6:10:16+280:BLZ_77712:USERNAME_99123:S:0:0'HKIDN:3:2+280:BLZ_77712+USERNAME_99123+0+1'HKVVB:4:3+0+0+1+PRODUCTID_8881293+PRODUCTVERSION_001'HKSYN:5:3+0'HNSHA:6:2+1995881++PIN_99120''HNHBS:7:1+1'");
    }

    @Test
    public void shouldOrderSegmentsProperly() {
        // given
        FinTsRequest request = new FinTsRequest();
        request.addSegment(new HKSPAv1());
        request.addSegment(new HKSPAv1());
        request.addSegment(new HKSPAv1());
        request.addSegment(
                HNVSKv3.builder()
                        .securityProcedureVersion(0)
                        .blz("")
                        .systemId("")
                        .username("")
                        .creationTime(dateTime)
                        .build());
        request.addSegment(new HKSPAv1());
        request.addSegment(new HKSPAv1());
        HNVSDv1 hnvsd = new HNVSDv1();
        hnvsd.addSegment(new HKSPAv1());
        hnvsd.addSegment(new HKSPAv1());
        request.addSegment(hnvsd);
        request.addSegment(new HKSPAv1());
        request.addSegment(new HKSPAv1());

        // when
        String requestInFinTsFormat = request.toFinTsFormat();

        // then
        assertThat(requestInFinTsFormat)
                .isEqualTo(
                        "HKSPA:1:1'HKSPA:2:1'HKSPA:3:1'HNVSK:998:3+PIN:0+998+1+1+1:20201228:173345+2:2:13:@8@00000000:5:1+280:::S:0:0+0'HKSPA:4:1'HKSPA:5:1'HNVSD:999:1+@20@HKSPA:6:1'HKSPA:7:1''HKSPA:8:1'HKSPA:9:1'");
    }

    @Test
    public void shouldPutMessageLengthIntoHeaderSegmentWithJustHeader() {
        FinTsRequest request = new FinTsRequest();
        request.addSegment(
                HNHBKv3.builder()
                        .dialogId("DIALOGID_8991")
                        .finTsVersion("FINTS_124213")
                        .messageNumber(1024)
                        .build());

        // when
        String requestInFinTsFormat = request.toFinTsFormat();

        // then
        assertThat(requestInFinTsFormat)
                .isEqualTo("HNHBK:1:3+000000000055+FINTS_124213+DIALOGID_8991+1024'");
    }

    @Test
    public void shouldPutMessageLengthIntoHeaderSegmentWithMultipleSegments() {
        FinTsRequest request = new FinTsRequest();
        request.addSegment(
                HNHBKv3.builder()
                        .dialogId("DIALOGID_4214")
                        .finTsVersion("FINTS_6456")
                        .messageNumber(6644)
                        .build());
        for (int i = 0; i < 10; i++) {
            request.addSegment(new HKSPAv1());
        }

        // when
        String requestInFinTsFormat = request.toFinTsFormat();

        // then
        assertThat(requestInFinTsFormat)
                .isEqualTo(
                        "HNHBK:1:3+000000000155+FINTS_6456+DIALOGID_4214+6644'HKSPA:2:1'HKSPA:3:1'HKSPA:4:1'HKSPA:5:1'HKSPA:6:1'HKSPA:7:1'HKSPA:8:1'HKSPA:9:1'HKSPA:10:1'HKSPA:11:1'");
    }
}
