package se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.ukopenbanking.ais.base.api.movedFromV31;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.catchThrowable;

import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;
import org.junit.Assert;
import org.junit.Test;
import se.tink.backend.aggregation.agents.exceptions.SessionException;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.ukopenbanking.ais.base.api.UkOpenBankingApiDefinitions.ConsentStatus;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.ukopenbanking.ais.base.api.UkOpenBankingApiDefinitions.CreditDebitIndicator;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.ukopenbanking.ais.base.api.UkOpenBankingApiDefinitions.EntryStatusCode;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.ukopenbanking.ais.base.api.UkOpenBankingApiDefinitions.ExternalAccountIdentification4Code;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.ukopenbanking.ais.base.api.UkOpenBankingApiDefinitions.ExternalLimitType;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.ukopenbanking.ais.base.api.UkOpenBankingApiDefinitions.PartyType;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.ukopenbanking.ais.base.api.UkOpenBankingApiDefinitions.TransactionMutability;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.ukopenbanking.ais.base.api.UkOpenBankingApiDefinitions.UkObBalanceType;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.ukopenbanking.ais.base.authenticator.rpc.ConsentResponse;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.ukopenbanking.ais.base.entities.AccountBalanceEntity;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.ukopenbanking.ais.base.entities.AccountIdentifierEntity;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.ukopenbanking.ais.base.entities.CreditLineEntity;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.ukopenbanking.ais.base.entities.PartyV31Entity;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.ukopenbanking.ais.v31.fetcher.entities.transaction.TransactionEntity;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.ukopenbanking.ais.v31.fixtures.BalanceFixtures;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.ukopenbanking.ais.v31.fixtures.ConsentFixtures;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.ukopenbanking.ais.v31.fixtures.IdentifierFixtures;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.ukopenbanking.ais.v31.fixtures.PartyFixtures;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.ukopenbanking.ais.v31.fixtures.TransactionEntityFixtures;

public class UkOpenBankingApiDefinitionsTest {

    @Test
    public void shouldMapUppercaseCredit() {
        // given
        AccountBalanceEntity balance = BalanceFixtures.balanceUppercaseCredit();

        // then
        Assert.assertEquals(CreditDebitIndicator.CREDIT, balance.getCreditDebitIndicator());
    }

    @Test
    public void shouldMapLowercaseDebit() {
        // given
        AccountBalanceEntity balance = BalanceFixtures.balanceLowercaseDebit();

        // then
        Assert.assertEquals(CreditDebitIndicator.DEBIT, balance.getCreditDebitIndicator());
    }

    @Test
    public void shouldReturnNullForUnknownCreditDebitIndicator() {
        // given
        CreditDebitIndicator creditDebitIndicator = CreditDebitIndicator.fromString("UNKNOWN");

        // then
        Assert.assertNull(creditDebitIndicator);
    }

    @Test
    public void shouldMapBalanceTypeInterimAvailable() {
        // given
        AccountBalanceEntity balance = BalanceFixtures.balanceUppercaseCredit();

        // then
        Assert.assertEquals(UkObBalanceType.INTERIM_AVAILABLE, balance.getType());
    }

    @Test
    public void shouldMapBalanceTypeExpected() {
        // given
        AccountBalanceEntity balance = BalanceFixtures.expectedBalance();

        // then
        Assert.assertEquals(UkObBalanceType.EXPECTED, balance.getType());
    }

    @Test
    public void shouldReturnNullForUnknownUkObBalanceType() {
        // given
        UkObBalanceType balanceType = UkObBalanceType.fromString("UNKNOWN");

        // then
        Assert.assertNull(balanceType);
    }

    @Test
    public void shouldMapCreditLinePreAgreed() {
        // given
        CreditLineEntity creditLineEntity = BalanceFixtures.preAgreedCreditLine();

        // then
        Assert.assertEquals(ExternalLimitType.PRE_AGREED, creditLineEntity.getType());
    }

    @Test
    public void shouldMapCreditLineAvailable() {
        // given
        CreditLineEntity creditLineEntity = BalanceFixtures.availableCreditLine();

        // then
        Assert.assertEquals(ExternalLimitType.AVAILABLE, creditLineEntity.getType());
    }

    @Test
    public void shouldReturnNullForUnknownExternalLimitType() {
        // given
        ExternalLimitType externalLimitType = ExternalLimitType.fromString("UNKNOWN");

        // then
        Assert.assertNull(externalLimitType);
    }

    @Test
    public void shouldReturnNullForUnknownTransactionStatusCode() {
        // given
        EntryStatusCode statusCode = EntryStatusCode.fromString("UNKNOWN");

        // then
        Assert.assertNull(statusCode);
    }

    @Test
    public void shouldMapBookedTransactionStatusCode() {
        // given
        TransactionEntity transaction = TransactionEntityFixtures.getMutableBookedTransaction();

        // then
        Assert.assertEquals(EntryStatusCode.BOOKED, transaction.getStatus());
    }

    @Test
    public void shouldReturnNullForUnknownTransactionMutabilityType() {
        // given
        TransactionMutability mutability = TransactionMutability.fromString("UNKNOWN");

        // then
        Assert.assertNull(mutability);
    }

    @Test
    public void shouldMapMutableTransaction() {
        // given
        TransactionEntity transaction = TransactionEntityFixtures.getMutablePendingTransaction();

        // then
        Assert.assertEquals(TransactionMutability.MUTABLE, transaction.getMutability());
    }

    @Test
    public void shouldMapUndefinedTransaction() {
        // given
        TransactionEntity transaction =
                TransactionEntityFixtures.getBookedTransactionWithUnspecifiedMutability();

        // then
        Assert.assertEquals(TransactionMutability.UNDEFINED, transaction.getMutability());
    }

    @Test
    public void shouldMapIdentifierAsIBAN() {
        // given
        AccountIdentifierEntity identifierEntity = IdentifierFixtures.ibanIdentifier();

        // then
        Assert.assertEquals(
                ExternalAccountIdentification4Code.IBAN, identifierEntity.getIdentifierType());
    }

    @Test
    public void shouldThrowIllegalStateExceptionForUnknownIdentifier() {
        // given
        Throwable throwable =
                catchThrowable(() -> ExternalAccountIdentification4Code.fromString("UNKNOWN"));

        // then
        assertThat(throwable)
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage(
                        "[UkOpenBankingApiDefinitions] UNKNOWN value is unknown for ExternalAccountIdentification4Code class!");
    }

    @Test
    public void shouldMapPartyType() {
        // given
        PartyV31Entity party = PartyFixtures.party();

        // then
        Assert.assertEquals(PartyType.DELEGATE, party.getPartyType());
    }

    @Test
    public void shouldMapPartiesType() {
        // given
        List<PartyV31Entity> parties = PartyFixtures.parties();

        // then
        Assert.assertEquals(
                Arrays.asList(PartyType.SOLE, PartyType.DELEGATE),
                parties.stream().map(PartyV31Entity::getPartyType).collect(Collectors.toList()));
    }

    @Test
    public void shouldMapConsentStatus() {
        // given
        ConsentResponse response = ConsentFixtures.authorisedConsent();

        // then
        Assert.assertTrue(response.getData().get().isAuthorised());
    }

    @Test
    public void shouldThrowConsentInvalidException() {
        // given
        Throwable throwable = catchThrowable(() -> ConsentStatus.fromString("UNKNOWN"));

        // then
        assertThat(throwable).isInstanceOf(SessionException.class);
    }
}
