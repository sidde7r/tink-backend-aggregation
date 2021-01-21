package se.tink.backend.aggregation.agents.nxgen.es.banks.bankinter;

import com.google.common.collect.Sets;
import java.util.Set;
import se.tink.backend.agents.rpc.AccountTypes;
import se.tink.backend.aggregation.agents.nxgen.es.banks.bankinter.fetcher.transactionalaccount.entities.BankinterHolder;
import se.tink.backend.aggregation.nxgen.core.account.AccountTypeMapper;
import se.tink.backend.aggregation.nxgen.core.account.entity.Holder.Role;
import se.tink.libraries.account.enums.AccountFlag;

public final class BankinterConstants {

    public static final String DEFAULT_CURRENCY = "EUR";

    private BankinterConstants() {
        throw new AssertionError();
    }

    public static final AccountTypeMapper ACCOUNT_TYPE_MAPPER =
            AccountTypeMapper.builder()
                    // N = cuenta nómina, C = COINC, Cuenta Corriente, E-cuenta
                    .put(AccountTypes.CHECKING, AccountFlag.PSD2_PAYMENT_ACCOUNT, "N", "C")
                    .build();

    public static class Urls {
        public static final String BASE = "https://bancaonline.bankinter.com";
        public static final String LOGIN_PAGE = BASE + "/gestion/login.xhtml";
        public static final String KEEP_ALIVE = BASE + "/gestion/rest/usuario/numavisos";
        public static final String IDENTITY_DATA = BASE + "/gestion/rest/usuario/datos";
        public static final String GLOBAL_POSITION = BASE + Paths.GLOBAL_POSITION;
        public static final String ACCOUNT = BASE + "/extracto/secure/movimientos_cuenta.xhtml";
        public static final String CREDIT_CARD = BASE + "/tarjetas/secure/tarjetas_ficha.xhtml";
        public static final String LOAN = BASE + "/prestamos/secure/prestamos.xhtml";
    }

    public static class Paths {
        public static final String GLOBAL_POSITION = "/extracto/secure/extracto_integral.xhtml";
        public static final String VERIFY_SCA = "/gestion/verifica_sca.xhtml";
    }

    public static class Holders {
        public static final BankinterHolder TITULAR_HOLDER_NAME =
                new BankinterHolder("//dt[text()='Titular']/following-sibling::dd/p", Role.HOLDER);
        public static final BankinterHolder AUTHORIZED_HOLDER_NAME =
                new BankinterHolder(
                        "//dt[text()='Apoderado']/following-sibling::dd/p", Role.AUTHORIZED_USER);
        public static final BankinterHolder TITULARS_HOLDER_NAME =
                new BankinterHolder(
                        "//dt[text()='Titulares']/following-sibling::dd/p", Role.HOLDER);
    }

    public static class LoginForm {
        public static final String FORM_ID = "loginForm";
        public static final String USERNAME_FIELD = "uid";
        public static final String PASSWORD_FIELD = "password";
        public static final String ERROR_PANEL = "errorPanel";
        public static final long SUBMIT_TIMEOUT_SECONDS = 5;
    }

    public static class ScaForm {
        public static final String CODE_FIELD_SELECTOR =
                "input[name$=inputSignCodeOtp].claveseguridad";
        public static final String SUBMIT_BUTTON_SELECTOR = "button[onclick*=enviarYFinalizar]";
        public static final long SUBMIT_TIMEOUT_SECONDS = 15;
    }

    public static class FormKeys {
        public static final String JSF_VIEWSTATE = "javax.faces.ViewState";
        public static final String JSF_PARTIAL_AJAX = "javax.faces.partial.ajax";
        public static final String JSF_PARTIAL_EXECUTE = "javax.faces.partial.execute";
        public static final String JSF_PARTIAL_RENDER = "javax.faces.partial.render";
        public static final String JSF_SOURCE = "javax.faces.source";
        public static final String LOAN_TRANSACTIONS_ID = "prestaForm:_idcl";
        public static final String LOAN_TRANSACTIONS_SUBMIT = "prestaForm_SUBMIT";
    }

    public static class FormValues {
        public static final String ACCOUNT_HEADER = "movimientos-cabecera";
        public static final String TRUE = "true";
        public static final String JSF_EXECUTE_ALL = "@all";
        public static final String LOAN_TRANSACTIONS_ID = "prestaForm:linkMasPagos";
        public static final String SUBMIT = "_SUBMIT";
    }

    public static class InstallmentStatus {
        public static final String PAID = "PAGADA";
    }

    public static class JsfPart {
        public static final String ACCOUNT_DETAILS = "movimientos-cabecera:head-datos-detalle";
        public static final String TRANSACTIONS_WAIT = "movimientos_espera";
        public static final String TRANSACTIONS_NAVIGATION = "movimientos-nav";
        public static final String TRANSACTIONS = "listado-movimientos";
        public static final String CARD_TRANSACTIONS_NAVIGATION = "movimientos-form:nav-sticky";
        public static final String CARD_TRANSACTIONS = "movimientos-form:listaMovimientos";
    }

    public static class QueryKeys {
        public static final String ACCOUNT_TYPE = "IND";
    }

    public static class HeaderKeys {
        public static final String JSF_REQUEST = "Faces-request";
        public static final String REQUESTED_WITH = "X-Requested-With";
    }

    public static class HeaderValues {
        public static final String USER_AGENT =
                "Mozilla/5.0 (iPhone; CPU iPhone OS 12_4 like Mac OS X) AppleWebKit/605.1.15 (KHTML, like Gecko) Mobile/15E148 6.2.2-239 WRAPPER";
        public static final String JSF_PARTIAL = "partial/ajax";
        public static final String REQUESTED_WITH = "XMLHttpRequest";
        public static final String ACCEPT_LANGUAGE = "es-ES";
    }

    public static class StorageKeys {
        public static final String FIRST_PAGINATION_KEY = "firstPaginationKey";
        public static final String RESPONSE_BODY = "responseBody";
    }

    public static class CardDetails {
        public static final String TYPE = "tipo";
        public static final String HOLDER_NAME = "titular";
        public static final String STATE = "estado";
    }

    public static class CardTypes {
        public static final Set<String> CREDIT = Sets.newHashSet("visa clasi", "visa oro");
        public static final Set<String> DEBIT = Sets.newHashSet("visa debit");
        public static final Set<String> ALL = Sets.union(CREDIT, DEBIT);
    }

    public static class CardState {
        public static final String REQUESTED = "solicitada";
        public static final String ENABLED = "activa";
        public static final Set<String> ALL = Sets.newHashSet(REQUESTED, ENABLED);
    }
}
