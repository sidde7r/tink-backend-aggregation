package se.tink.backend.aggregation.agents.nxgen.de.banks.fints.protocol.parts.request;

import static org.assertj.core.api.Assertions.assertThat;

import java.time.LocalDateTime;
import org.junit.Test;
import se.tink.backend.aggregation.agents.nxgen.de.banks.fints.security.tan.SegmentType;

public class HNVSDv1Test {

    @Test
    public void shouldSerializeProperlyWithoutAnySubSegments() {
        // given
        BaseRequestPart segment = new HNVSDv1();

        // when
        String serializedSegment = segment.toFinTsFormat();

        // then
        assertThat(serializedSegment).isEqualTo("HNVSD:1:1+@0@");
    }

    @Test
    public void shouldSerializeProperlyWithSomeSegments() {
        // given
        HNVSDv1 segment = new HNVSDv1();
        segment.addSegment(new HNVSDv1());
        segment.addSegment(
                HKTANv6.builder()
                        .segmentType(SegmentType.DKALE)
                        .tanProcess("2")
                        .tanMediumName("TANMEDIUM_asdf:?fdsa':asdf")
                        .build());
        // when
        String serializedSegment = segment.toFinTsFormat();

        // then
        assertThat(serializedSegment)
                .isEqualTo(
                        "HNVSD:1:1+@72@HNVSD:1:1+@0@'HKTAN:1:6+2+DKALE++++N+++++TANMEDIUM_asdf?:??fdsa?'?:asdf'");
    }

    @Test
    public void shouldSerializeProperlyWithLotsOfSegments() {
        // given
        HNVSDv1 segment = new HNVSDv1();
        int securityReference = 15125125;
        String systemId = "SYSTEMID_4771";
        String blz = "BLZ_3875873";
        String username = "USERNAME_125125";
        segment.addSegment(
                HNSHKv4.builder()
                        .securityProcedureVersion(214124)
                        .securityFunction("SECFUN_8882")
                        .securityReference(securityReference)
                        .systemId(systemId)
                        .creationTime(LocalDateTime.of(2020, 03, 11, 11, 25, 23))
                        .blz(blz)
                        .username(username)
                        .build());
        segment.addSegment(
                HKIDNv2.builder().systemId(systemId).blz(blz).username(username).build());
        segment.addSegment(
                HKVVBv3.builder().productId("PRODUCTID_1248278").productVersion("0.1").build());
        segment.addSegment(
                HKTANv6.builder()
                        .tanProcess("4")
                        .segmentType(SegmentType.HKIDN)
                        .tanMediumName("TANMEDIUMNAME_149828947")
                        .build());
        segment.addSegment(
                HNSHAv2.builder()
                        .securityReference(securityReference)
                        .password("PASSWORD_58883")
                        .tanAnswer("TANANSWER_15252")
                        .build());

        // when
        String serializedSegment = segment.toFinTsFormat();

        // then
        assertThat(serializedSegment)
                .isEqualTo(
                        "HNVSD:1:1+@335@HNSHK:1:4+PIN:214124+SECFUN_8882+15125125+1+1+1::SYSTEMID_4771+1+1:20200311:112523+1:999:1+6:10:16+280:BLZ_3875873:USERNAME_125125:S:0:0'HKIDN:1:2+280:BLZ_3875873+USERNAME_125125+SYSTEMID_4771+1'HKVVB:1:3+0+0+1+PRODUCTID_1248278+0.1'HKTAN:1:6+4+HKIDN++++N+++++TANMEDIUMNAME_149828947'HNSHA:1:2+15125125++PASSWORD_58883:TANANSWER_15252'");
    }
}
