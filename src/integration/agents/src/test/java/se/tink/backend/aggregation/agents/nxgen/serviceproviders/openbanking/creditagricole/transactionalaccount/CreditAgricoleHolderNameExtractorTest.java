package se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.creditagricole.transactionalaccount;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.List;
import org.junit.Test;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.creditagricole.transactionalaccount.entities.AccountIdEntity;
import se.tink.backend.aggregation.nxgen.core.account.entity.Party;
import se.tink.libraries.serialization.utils.SerializationUtils;

public class CreditAgricoleHolderNameExtractorTest {

    @Test
    public void shouldExtractSingleName() {
        // given
        String name = "M. NAME SURNAME";
        // when
        List<Party> values = CreditAgricoleHolderNameExtractor.extractAccountHolders(name, null);
        // then
        assertThat(values).hasSize(1);
        assertThat(values.get(0).getName()).isEqualTo("Name Surname");
    }

    @Test
    public void shouldExtractMultipleHolders() {
        // given
        String name = "M. SURNAME1 NAME1 OU MME SURNAME2 NAME2";
        // when
        List<Party> values = CreditAgricoleHolderNameExtractor.extractAccountHolders(name, null);
        // then
        assertThat(values).hasSize(2);
        assertThat(values.get(0).getName()).isEqualTo("Surname1 Name1");
        assertThat(values.get(1).getName()).isEqualTo("Surname2 Name2");
    }

    @Test
    public void shouldExtractMultipleHoldersWithoutTitles() {
        // given
        String name = "SURNAME1 NAME1 OU SURNAME2 NAME2";
        // when
        List<Party> values = CreditAgricoleHolderNameExtractor.extractAccountHolders(name, null);
        // then
        assertThat(values).hasSize(2);
        assertThat(values.get(0).getName()).isEqualTo("Surname1 Name1");
        assertThat(values.get(1).getName()).isEqualTo("Surname2 Name2");
    }

    @Test
    public void shouldExtractWhenOnlyNameAndSurnameGiven() {
        // given
        String name = "SURNAME NAME";
        // when
        List<Party> values = CreditAgricoleHolderNameExtractor.extractAccountHolders(name, null);
        // then
        assertThat(values).hasSize(1);
        assertThat(values.get(0).getName()).isEqualTo("Surname Name");
    }

    @Test
    public void shouldExtractValueFromAreaEntity() {
        // given
        String name = "Compte de paiement";
        AccountIdEntity accountId =
                SerializationUtils.deserializeFromString(
                        "{\n"
                                + "  \"area\": {\n"
                                + "    \"areaId\": \"uuid\",\n"
                                + "    \"areaLabel\": \"MME NAME SURNAME\"\n"
                                + "  },\n"
                                + "  \"iban\": \"iban\",\n"
                                + "  \"other\": null,\n"
                                + "  \"currency\": \"EUR\"\n"
                                + "}",
                        AccountIdEntity.class);
        // when
        List<Party> values =
                CreditAgricoleHolderNameExtractor.extractAccountHolders(name, accountId);
        // then
        assertThat(values).hasSize(1);
        assertThat(values.get(0).getName()).isEqualTo("Name Surname");
    }
}
