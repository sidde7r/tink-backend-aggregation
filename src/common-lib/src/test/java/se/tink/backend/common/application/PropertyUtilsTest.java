package se.tink.backend.common.application;

import com.google.common.collect.Lists;
import java.util.Collections;
import java.util.List;
import org.junit.Test;
import org.junit.experimental.runners.Enclosed;
import org.junit.runner.RunWith;
import se.tink.backend.core.FraudAddressContent;
import se.tink.backend.core.FraudRealEstateEngagementContent;
import static org.assertj.core.api.Assertions.assertThat;

@RunWith(Enclosed.class)
public class PropertyUtilsTest {
    public static class CleanStreetAddress {
        @Test
        public void clean() {
            assertThat(PropertyUtils.cleanStreetAddress("Götgatan 12 Lgh 1234")).isEqualTo("Götgatan 12");
            assertThat(PropertyUtils.cleanStreetAddress("Götgatan 12 10 Tr")).isEqualTo("Götgatan 12");
        }
    }
    
    @RunWith(Enclosed.class)
    public static class IsApartment {
        public static class WhenHavingEngagementInSameMunicipality {
            @Test
            public void falseWhenOnlyStreetAndNumber() {
                assertThat(PropertyUtils
                        .isApartment(
                                createAddressContent("Götgatan 80", "Stockholm", "Stockholm"),
                                createRealEstateEngagements("Stockholm")))
                        .isFalse();
            }

            @Test
            public void trueWhenContainsLgh() {
                assertThat(PropertyUtils
                        .isApartment(
                                createAddressContent("Götgatan 12 Lgh 1012", "Stockholm", "Stockholm"),
                                createRealEstateEngagements("Stockholm")))
                        .isTrue();
            }

            @Test
            public void trueWhenEndsWithNumberAndTr() {
                assertThat(PropertyUtils
                        .isApartment(
                                createAddressContent("Götgatan 80 1 Tr", "Stockholm", "Stockholm"),
                                createRealEstateEngagements("Stockholm")))
                        .isTrue();
                
                assertThat(PropertyUtils
                        .isApartment(
                                createAddressContent("Götgatan 80 10 Tr", "Stockholm", "Stockholm"),
                                createRealEstateEngagements("Stockholm")))
                        .isTrue();
            }

            @Test
            public void falseWhenContainsNumberAndTr() {
                assertThat(PropertyUtils
                        .isApartment(
                                createAddressContent("Götgatan 80 Tr 1", "Stockholm", "Stockholm"),
                                createRealEstateEngagements("Stockholm")))
                        .isFalse();
            }
        }

        public static class WhenNoEngagementsInSameMunicipality {
            @Test
            public void trueWhenNoEngagements() {
                assertThat(PropertyUtils
                        .isApartment(
                                createAddressContent("Götgatan", "Stockholm", "Stockholm"),
                                Collections.<FraudRealEstateEngagementContent>emptyList()))
                        .isTrue();
            }

            @Test
            public void trueWhenEngagementInOtherMunicipality() {
                assertThat(PropertyUtils
                        .isApartment(
                                createAddressContent("Götgatan", "Stockholm", "Stockholm"),
                                createRealEstateEngagements("Göteborg")))
                        .isTrue();
            }
        }

        private static FraudAddressContent createAddressContent(String address, String city, String community) {
            FraudAddressContent fraudAddressContent = new FraudAddressContent();
            fraudAddressContent.setAddress(address);
            fraudAddressContent.setCity(city);
            fraudAddressContent.setCommunity(community);
            return fraudAddressContent;
        }

        private static List<FraudRealEstateEngagementContent> createRealEstateEngagements(String... communities) {
            List<FraudRealEstateEngagementContent> contents = Lists.newArrayList();

            for (String community : communities) {
                FraudRealEstateEngagementContent content = new FraudRealEstateEngagementContent();
                content.setMuncipality(community);
                contents.add(content);
            }

            return contents;
        }
    }
}
