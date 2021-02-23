package se.tink.backend.aggregation.agents.nxgen.serviceproviders.banks.euroinformation.fetcher.transactional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;

import java.io.StringReader;
import java.math.BigDecimal;
import java.util.Collection;
import java.util.Iterator;
import javax.xml.bind.JAXB;
import org.junit.Before;
import org.junit.Test;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.banks.euroinformation.EuroInformationApiClient;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.banks.euroinformation.EuroInformationConstants.Storage;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.banks.euroinformation.EuroInformationConstants.Tags;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.banks.euroinformation.authentication.rpc.LoginResponse;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.banks.euroinformation.fetcher.rpc.AccountSummaryResponse;
import se.tink.backend.aggregation.nxgen.core.account.entity.Party;
import se.tink.backend.aggregation.nxgen.core.account.transactional.TransactionalAccount;
import se.tink.backend.aggregation.nxgen.core.account.transactional.TransactionalAccountType;
import se.tink.backend.aggregation.nxgen.storage.SessionStorage;

public class EuroInformationAccountFetcherTest {
    private SessionStorage sessionStorage;
    private EuroInformationApiClient euroApiClient;
    private EuroInformationAccountFetcher accountFetcher;

    @Before
    public void setup() {
        euroApiClient = mock(EuroInformationApiClient.class);
        sessionStorage = new SessionStorage();
        accountFetcher = new EuroInformationAccountFetcher(euroApiClient, sessionStorage);
    }

    @Test
    public void shouldFetchAndMapAccountsWithHolder() {
        // given
        AccountSummaryResponse accountsResponse =
                JAXB.unmarshal(
                        new StringReader(ACCOUNTS_RESPONSE_WITH_HOLDER),
                        AccountSummaryResponse.class);
        LoginResponse loginResponse =
                JAXB.unmarshal(new StringReader(LOGIN_RESPONSE), LoginResponse.class);

        sessionStorage.put(Tags.ACCOUNT_LIST, accountsResponse);
        sessionStorage.put(Storage.LOGIN_RESPONSE, loginResponse);

        // when
        Collection<TransactionalAccount> accounts = accountFetcher.fetchAccounts();
        accounts.iterator();

        // then
        Iterator<TransactionalAccount> iterator = accounts.iterator();
        assertCheckingAccountValidWithHolderRoleHolder(iterator.next());
        assertSavingAccountValidWithHolderRoleOther(iterator.next());
    }

    @Test
    public void shouldFetchAndMapAccountsWithoutHolder() {
        // given
        AccountSummaryResponse accountsResponse =
                JAXB.unmarshal(
                        new StringReader(ACCOUNTS_RESPONSE_WITH_HOLDER),
                        AccountSummaryResponse.class);

        sessionStorage.put(Tags.ACCOUNT_LIST, accountsResponse);

        // when
        Collection<TransactionalAccount> accounts = accountFetcher.fetchAccounts();
        accounts.iterator();

        // then
        Iterator<TransactionalAccount> iterator = accounts.iterator();
        assertCheckingAccountValidWithoutHolder(iterator.next());
    }

    private void assertCheckingAccountValid(TransactionalAccount account) {
        assertThat(account).isNotNull();
        assertThat(account.getType()).isEqualTo(TransactionalAccountType.CHECKING.toAccountType());
        assertThat(account.getExactBalance().getCurrencyCode()).isEqualTo("EUR");
        assertThat(account.getExactBalance().getExactValue())
                .isEqualByComparingTo(new BigDecimal("10000.00"));
        assertThat(account.getIdModule().getUniqueId()).isEqualTo("IBAN1");
        assertThat(account.getIdModule().getAccountNumber()).isEqualTo("TEST_ACCOUNT_NUMBER_1");
        assertThat(account.getIdModule().getAccountName()).isEqualTo("TEST_NAME_1");
    }

    private void assertCheckingAccountValidWithHolderRoleHolder(TransactionalAccount account) {
        assertCheckingAccountValid(account);
        assertThat(account.getParties().get(0).getName()).isEqualTo("John Doe");
        assertThat(account.getParties().get(0).getRole()).isEqualTo(Party.Role.HOLDER);
    }

    private void assertSavingAccountValidWithHolderRoleOther(TransactionalAccount account) {
        assertThat(account).isNotNull();
        assertThat(account.getType()).isEqualTo(TransactionalAccountType.SAVINGS.toAccountType());
        assertThat(account.getExactBalance().getCurrencyCode()).isEqualTo("EUR");
        assertThat(account.getExactBalance().getExactValue())
                .isEqualByComparingTo(new BigDecimal("50000.00"));
        assertThat(account.getIdModule().getUniqueId()).isEqualTo("IBAN2");
        assertThat(account.getIdModule().getAccountNumber()).isEqualTo("TEST_ACCOUNT_NUMBER_2");
        assertThat(account.getIdModule().getAccountName()).isEqualTo("TEST_NAME_2");
        assertThat(account.getParties().get(0).getName()).isEqualTo("John Doe");
        assertThat(account.getParties().get(0).getRole()).isEqualTo(Party.Role.OTHER);
    }

    private void assertCheckingAccountValidWithoutHolder(TransactionalAccount account) {
        assertCheckingAccountValid(account);
        assertThat(account.getParties().isEmpty()).isTrue();
    }

    private String ACCOUNTS_RESPONSE_WITH_HOLDER =
            "<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n"
                    + "<root>\n"
                    + "    <code_retour>omg</code_retour>\n"
                    + "    <msg_retour />\n"
                    + "    <date_msg>20210219020528</date_msg>\n"
                    + "    <code_retour_cpl>please</code_retour_cpl>\n"
                    + "    <category_list>\n"
                    + "        <category>\n"
                    + "            <name>help</name>\n"
                    + "            <code>TEST_CODE</code>\n"
                    + "        </category>\n"
                    + "        <category>\n"
                    + "            <name>TEST_NAME</name>\n"
                    + "            <code>TEST_CODE_2</code>\n"
                    + "        </category>\n"
                    + "    </category_list>\n"
                    + "    <liste_compte>\n"
                    + "        <compte>\n"
                    + "            <account_type>01</account_type>\n"
                    + "            <iban>IBAN1</iban>\n"
                    + "            <devise>EUR</devise>\n"
                    + "            <account_number>TEST_ACCOUNT_NUMBER_1</account_number>\n"
                    + "            <intc>TEST_NAME</intc>\n"
                    + "            <int>TEST_NAME_1</int>\n"
                    + "            <tit>TEST</tit>\n"
                    + "            <refprd />\n"
                    + "            <codprd>TEST</codprd>\n"
                    + "            <refctr_exi_val>TEST</refctr_exi_val>\n"
                    + "            <refctr_inn_val>TEST</refctr_inn_val>\n"
                    + "            <category_code>TEST</category_code>\n"
                    + "            <category_name>TEST</category_name>\n"
                    + "            <solde>10000</solde>\n"
                    + "            <agreed_overdraft>TEST_AO</agreed_overdraft>\n"
                    + "            <appcpt>1</appcpt>\n"
                    + "            <isholder>1</isholder>\n"
                    + "            <webid>TEST</webid>\n"
                    + "            <checkingaccount>1</checkingaccount>\n"
                    + "            <characteristics>0</characteristics>\n"
                    + "            <simulation>0</simulation>\n"
                    + "            <contract />\n"
                    + "        </compte>\n"
                    + "        <compte>\n"
                    + "            <account_type>02</account_type>\n"
                    + "            <iban>IBAN2</iban>\n"
                    + "            <devise>EUR</devise>\n"
                    + "            <account_number>TEST_ACCOUNT_NUMBER_2</account_number>\n"
                    + "            <intc>TEST</intc>\n"
                    + "            <int>TEST_NAME_2</int>\n"
                    + "            <tit>TEST</tit>\n"
                    + "            <refprd />\n"
                    + "            <codprd>BTEST</codprd>\n"
                    + "            <refctr_exi_val>TEST</refctr_exi_val>\n"
                    + "            <refctr_inn_val>TEST</refctr_inn_val>\n"
                    + "            <category_code>TEST</category_code>\n"
                    + "            <category_name>TEST</category_name>\n"
                    + "            <solde>50000</solde>\n"
                    + "            <agreed_overdraft>TEST</agreed_overdraft>\n"
                    + "            <appcpt>5</appcpt>\n"
                    + "            <isholder>0</isholder>\n"
                    + "            <webid>TEST</webid>\n"
                    + "            <checkingaccount>0</checkingaccount>\n"
                    + "            <characteristics>0</characteristics>\n"
                    + "            <simulation>0</simulation>\n"
                    + "            <contract />\n"
                    + "        </compte>\n"
                    + "    </liste_compte>\n"
                    + "</root>";

    private String LOGIN_RESPONSE =
            "<?xml version=\"1.0\" encoding=\"UTF-8\" ?>"
                    + "<root>"
                    + "<code_retour>**HASHED:mv**</code_retour>"
                    + "<libelle_client>John Doe</libelle_client>"
                    + "<cdc>"
                    + "<civ><![CDATA[SIR JOHN DOE]]></civ>"
                    + "<prenom><![CDATA[]]></prenom>"
                    + "<nom><![CDATA[]]></nom>"
                    + "<tel>+0202020020</tel>"
                    + "<mel>00000@targobank.es</mel>"
                    + "</cdc>"
                    + "<dtcnx>TEST</dtcnx>"
                    + "<typctr>PAR</typctr>"
                    + "<rib>**HASHED:sn**</rib>"
                    + "<fede>94</fede>"
                    + "<urlfede>**HASHED:cs**</urlfede>"
                    + "<userid>**HASHED:aG**</userid>"
                    + "</root>";
}
