package se.tink.backend.aggregation.agents.nxgen.de.banks.fints.protocol.parts.request;

import static org.assertj.core.api.Assertions.assertThat;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalTime;
import org.junit.Test;

public class BaseRequestPartTest {

    @Test
    public void shouldSerializeIntoFiveGroups() {
        // given
        BaseRequestPart segment =
                new TestRequestSegment() {
                    @Override
                    protected void compile() {
                        addGroup().element("1");
                        addGroup().element("2");
                        addGroup().element("3");
                        addGroup().element("4");
                        addGroup().element("5");
                    }
                };

        // when
        String segmentInFinTsFormat = segment.toFinTsFormat();

        // then
        assertThat(segmentInFinTsFormat).isEqualTo("1+2+3+4+5");
    }

    @Test
    public void shouldSerializeIntoTwoGroupsWithMultipleElements() {
        // given
        BaseRequestPart segment =
                new TestRequestSegment() {
                    @Override
                    protected void compile() {
                        addGroup().element("1").element("2").element("3").element("4");
                        addGroup().element("A").element("B");
                    }
                };

        // when
        String segmentInFinTsFormat = segment.toFinTsFormat();

        // then
        assertThat(segmentInFinTsFormat).isEqualTo("1:2:3:4+A:B");
    }

    @Test
    public void shouldTrimEmptyElementsOrGroupsOnRightSide() {
        BaseRequestPart segment =
                new TestRequestSegment() {
                    @Override
                    protected void compile() {
                        addGroup().element("1").element().element().element();
                        addGroup().element("A").element().element().element().element("B");
                        addGroup();
                        addGroup();
                        addGroup();
                        addGroup().element("Z").element().element();
                        addGroup();
                        addGroup();
                    }
                };

        // when
        String segmentInFinTsFormat = segment.toFinTsFormat();

        // then
        assertThat(segmentInFinTsFormat).isEqualTo("1+A::::B++++Z");
    }

    @Test
    public void shouldSerializeStringProperly() {
        // given

        BaseRequestPart segment =
                new TestRequestSegment() {
                    @Override
                    protected void compile() {
                        addGroup()
                                .element((String) null)
                                .element("")
                                .element("ASDFGHJKL")
                                .element("a'b?c+d:e@f");
                    }
                };
        // when
        String segmentInFinTsFormat = segment.toFinTsFormat();

        // then
        assertThat(segmentInFinTsFormat).isEqualTo("::ASDFGHJKL:a?'b??c?+d?:e?@f");
    }

    @Test
    public void shouldSerializeIntegerProperly() {
        // given
        BaseRequestPart segment =
                new TestRequestSegment() {
                    @Override
                    protected void compile() {
                        addGroup()
                                .element((Integer) null)
                                .element(0)
                                .element(Integer.MIN_VALUE)
                                .element(Integer.MAX_VALUE);
                    }
                };

        // when
        String segmentInFinTsFormat = segment.toFinTsFormat();

        // then
        assertThat(segmentInFinTsFormat).isEqualTo(":0:-2147483648:2147483647");
    }

    @Test
    public void shouldSerializeBooleanProperly() {
        // given
        BaseRequestPart segment =
                new TestRequestSegment() {
                    @Override
                    protected void compile() {
                        addGroup().element((Boolean) null).element(true).element(false);
                    }
                };

        // when
        String segmentInFinTsFormat = segment.toFinTsFormat();

        // then
        assertThat(segmentInFinTsFormat).isEqualTo(":J:N");
    }

    @Test
    public void shouldSerializeByteArrayProperly() {
        // given
        BaseRequestPart segment =
                new TestRequestSegment() {
                    @Override
                    protected void compile() {
                        addGroup()
                                .element((byte[]) null)
                                .element("0123456789".getBytes())
                                .element(new byte[0]);
                    }
                };

        // when
        String segmentInFinTsFormat = segment.toFinTsFormat();

        // then
        assertThat(segmentInFinTsFormat).isEqualTo(":@10@0123456789:@0@");
    }

    @Test
    public void shouldSerializeBigDecimalProperly() {
        // given
        BaseRequestPart segment =
                new TestRequestSegment() {
                    @Override
                    protected void compile() {
                        addGroup()
                                .element((BigDecimal) null)
                                .element(BigDecimal.valueOf(1002.0010000))
                                .element(BigDecimal.ZERO)
                                .element(BigDecimal.valueOf(120))
                                .element(BigDecimal.valueOf(-1203.0120300100));
                    }
                };

        // when
        String segmentInFinTsFormat = segment.toFinTsFormat();

        // then
        assertThat(segmentInFinTsFormat).isEqualTo(":1002,001:0,:120,:-1203,01203001");
    }

    @Test
    public void shouldSerializeLocalDateProperly() {
        // given
        BaseRequestPart segment =
                new TestRequestSegment() {
                    @Override
                    protected void compile() {
                        addGroup()
                                .element((LocalDate) null)
                                .element(LocalDate.of(1, 1, 1))
                                .element(LocalDate.of(2020, 12, 31));
                    }
                };

        // when
        String segmentInFinTsFormat = segment.toFinTsFormat();

        // then
        assertThat(segmentInFinTsFormat).isEqualTo(":00010101:20201231");
    }

    @Test
    public void shouldSerializeLocalTimeProperly() {
        // given
        BaseRequestPart segment =
                new TestRequestSegment() {
                    @Override
                    protected void compile() {
                        addGroup()
                                .element((LocalTime) null)
                                .element(LocalTime.of(10, 10))
                                .element(LocalTime.of(1, 2, 3))
                                .element(LocalTime.of(23, 59, 59));
                    }
                };

        // when
        String segmentInFinTsFormat = segment.toFinTsFormat();

        // then
        assertThat(segmentInFinTsFormat).isEqualTo(":101000:010203:235959");
    }

    private class TestRequestSegment extends BaseRequestPart {
        @Override
        public String getSegmentName() {
            return "TEST";
        }

        @Override
        public int getSegmentVersion() {
            return 0;
        }
    }
}
