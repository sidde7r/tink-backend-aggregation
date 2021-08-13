package se.tink.backend.aggregation.agents.nxgen.de.banks.commerzbank.entities;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.Collection;
import org.junit.Test;
import se.tink.backend.agents.rpc.AccountTypes;
import se.tink.backend.aggregation.agents.nxgen.de.banks.commerzbank.CommerzbankConstants;
import se.tink.backend.aggregation.agents.nxgen.de.banks.commerzbank.TestDataReader;
import se.tink.backend.aggregation.nxgen.core.account.creditcard.CreditCardAccount;
import se.tink.backend.aggregation.nxgen.core.account.entity.Party;
import se.tink.backend.aggregation.nxgen.core.account.transactional.TransactionalAccount;
import se.tink.libraries.account.identifiers.BbanIdentifier;
import se.tink.libraries.account.identifiers.IbanIdentifier;
import se.tink.libraries.account.identifiers.MaskedPanIdentifier;
import se.tink.libraries.amount.ExactCurrencyAmount;

public class ResultEntityTest {

    @Test
    public void shouldGetOneTransactionalAccountMappedProperly() {
        // given
        ResultEntity resultEntity =
                TestDataReader.readFromFile(
                        TestDataReader.RESULT_WITH_TWO_ACCOUNTS, ResultEntity.class);

        // when
        Collection<TransactionalAccount> transactionalAccounts =
                resultEntity.toTransactionalAccounts();

        // then
        assertThat(transactionalAccounts).hasSize(1);
        TransactionalAccount account = transactionalAccounts.iterator().next();

        assertThat(account.isUniqueIdentifierEqual("DE28120400000123456789")).isTrue();
        assertThat(account.getType()).isEqualTo(AccountTypes.CHECKING);
        assertThat(account.getName()).isEqualTo("PremiumGeschäftskonto Plus");
        assertThat(account.getAccountNumber()).isEqualTo("323123456789");
        assertThat(account.getParties()).contains(new Party("Imie Nazwisko", Party.Role.HOLDER));
        assertThat(account.getIdentifiers())
                .containsExactlyInAnyOrder(
                        new IbanIdentifier("COBADEFFXXX", "DE28120400000123456789"),
                        new BbanIdentifier("120400000123456789"));
        assertThat(account.getFromTemporaryStorage(CommerzbankConstants.Headers.IDENTIFIER))
                .isEqualTo("323123456789");
        assertThat(account.getFromTemporaryStorage(CommerzbankConstants.Headers.PRODUCT_TYPE))
                .isEqualTo("CurrentAccount");
        assertThat(account.getFromTemporaryStorage(CommerzbankConstants.Headers.PRODUCT_BRANCH))
                .isEqualTo("100");
        assertThat(account.getExactCreditLimit()).isEqualTo(ExactCurrencyAmount.inEUR(10000));
        assertThat(account.getExactBalance()).isEqualTo(ExactCurrencyAmount.inEUR(28543.10));
    }

    @Test
    public void shouldGetOneCardMappedProperly() {
        // given
        ResultEntity resultEntity =
                TestDataReader.readFromFile(
                        TestDataReader.RESULT_WITH_TWO_ACCOUNTS, ResultEntity.class);

        // when
        Collection<CreditCardAccount> creditCardAccounts = resultEntity.toCreditAccounts();

        // then
        assertThat(creditCardAccounts).hasSize(1);
        CreditCardAccount card = creditCardAccounts.iterator().next();

        assertThat(card.isUniqueIdentifierEqual("cc94ca39c95f0a6723bda56a0f6f9146")).isTrue();
        assertThat(card.getCardModule().getCardNumber()).isEqualTo("**** **** **** 5555");
        assertThat(card.getCardModule().getCardAlias())
                .isEqualTo("Business Card Premium (Kreditkarte)");

        assertThat(card.getType()).isEqualTo(AccountTypes.CREDIT_CARD);
        assertThat(card.getName()).isEqualTo("DE28120400000123456789");
        assertThat(card.getAccountNumber()).isEqualTo("DE28120400000123456789");
        assertThat(card.getParties()).contains(new Party("Imie Nazwisko", Party.Role.HOLDER));

        assertThat(card.getIdentifiers())
                .containsExactlyInAnyOrder(
                        new IbanIdentifier("DE28120400000123456789"),
                        new MaskedPanIdentifier("**** **** **** 5555"));
        assertThat(
                        card.getFromTemporaryStorage(
                                CommerzbankConstants.Headers.CREDIT_CARD_IDENTIFIER))
                .isEqualTo("1111222244445555");
        assertThat(
                        card.getFromTemporaryStorage(
                                CommerzbankConstants.Headers.CREDIT_CARD_PRODUCT_TYPE))
                .isEqualTo("CreditCard");
        assertThat(card.getFromTemporaryStorage(CommerzbankConstants.Headers.PRODUCT_BRANCH))
                .isEqualTo("756");
        assertThat(card.getExactBalance()).isEqualTo(ExactCurrencyAmount.inEUR(0));
        assertThat(card.getExactAvailableCredit()).isEqualTo(ExactCurrencyAmount.inEUR(3000));
    }

    @Test
    public void shouldGetFourOfEachAccountType() {
        // given
        ResultEntity resultEntity =
                TestDataReader.readFromFile(
                        TestDataReader.RESULT_WITH_MANY_ACCOUNTS, ResultEntity.class);

        // when
        Collection<CreditCardAccount> creditCardAccounts = resultEntity.toCreditAccounts();
        Collection<TransactionalAccount> transactionalAccounts =
                resultEntity.toTransactionalAccounts();

        // then
        assertThat(creditCardAccounts).hasSize(4);
        assertThat(transactionalAccounts).hasSize(4);
    }
}
