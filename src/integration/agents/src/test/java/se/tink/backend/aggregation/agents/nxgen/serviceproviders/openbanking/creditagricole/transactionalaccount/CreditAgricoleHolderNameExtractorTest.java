package se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.creditagricole.transactionalaccount;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.List;
import org.junit.Test;

public class CreditAgricoleHolderNameExtractorTest {

    @Test
    public void shouldExtractSingleName() {
        // given
        String name = "M. NAME SURNAME";
        // when
        List<String> values = CreditAgricoleHolderNameExtractor.extract(name);
        // then
        assertThat(values).hasSize(1);
        assertThat(values.get(0)).isEqualTo("NAME SURNAME");
    }

    @Test
    public void shouldExtractMultipleHolders() {
        // given
        String name = "M. SURNAME1 NAME1 OU MME SURNAME2 NAME2";
        // when
        List<String> values = CreditAgricoleHolderNameExtractor.extract(name);
        // then
        assertThat(values).hasSize(2);
        assertThat(values.get(0)).isEqualTo("SURNAME1 NAME1");
        assertThat(values.get(1)).isEqualTo("SURNAME2 NAME2");
    }

    @Test
    public void shouldExtractMultipleHoldersWithoutTitles() {
        // given
        String name = "SURNAME1 NAME1 OU SURNAME2 NAME2";
        // when
        List<String> values = CreditAgricoleHolderNameExtractor.extract(name);
        // then
        assertThat(values).hasSize(2);
        assertThat(values.get(0)).isEqualTo("SURNAME1 NAME1");
        assertThat(values.get(1)).isEqualTo("SURNAME2 NAME2");
    }

    @Test
    public void shouldExtractWhenOnlyNameAndSurnameGiven() {
        // given
        String name = "SURNAME NAME";
        // when
        List<String> values = CreditAgricoleHolderNameExtractor.extract(name);
        // then
        assertThat(values).hasSize(1);
        assertThat(values.get(0)).isEqualTo("SURNAME NAME");
    }
}
