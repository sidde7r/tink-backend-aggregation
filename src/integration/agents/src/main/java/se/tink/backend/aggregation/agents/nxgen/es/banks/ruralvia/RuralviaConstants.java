package se.tink.backend.aggregation.agents.nxgen.es.banks.ruralvia;

import java.time.format.DateTimeFormatter;
import org.openqa.selenium.By;

public class RuralviaConstants {

    public static final By USER_FIELD_INPUT = By.tagName(Tags.INPUT_TAG);
    public static final String THERE_IS_NOT_DATA_FOR_THIS_CONSULT =
            "NO EXISTEN DATOS PARA LA CONSULTA REALIZADA";
    public static final String NOT_AVAILABLE_AT_THE_MOMENT =
            "disponible en estos momentos. Por favor, in";
    public static final String INVALID_PERIOD = "Periodo Invalido";
    public static final DateTimeFormatter LOCAL_DATE_PATTERN =
            DateTimeFormatter.ofPattern("dd-MM-uuuu");

    public static class Urls {
        public static final String RURALVIA_SECURE_HOST = "https://www.ruralvia.com";
        public static final String RURALVIA_MOBILE_LOGIN =
                "https://www.ruralvia.com/isum/Main?ISUM_SCR=login&loginType=accesoSeguro&ISUM_Portal=104&acceso_idioma=es_ES";
        public static final String RURALVIA_STILL_ALIVE =
                RURALVIA_SECURE_HOST
                        + "/isum/Main?ISUM_ID=menu&ISUM_SCR=groupScr&ISUM_CIPH=vHFigDiTGLnDNYt8yIybWs+dkNdI1NHIhZt2KUuy01s=";
    }

    public static class LoginForm {
        public static final String USER_FIELD = "divUser";
        public static final String ID_FIELD = "divDoc";
        public static final String PASSWORD_FIELD = "divErrorPasswordParent";
        public static final String WEB_VIEW = "label[for='accessTypeClassic']";
        public static final String ACCEPT_BUTTON = "acceptButton";
    }

    public static class HeaderValues {
        public static final String USER_AGENT =
                "Mozilla/5.0 (iPhone; CPU iPhone OS 12_4 like Mac OS X) AppleWebKit/605.1.15 (KHTML, like Gecko) Mobile/15E148 6.2.2-239 WRAPPER";
        public static final String ACCEPT_LANGUAGE = "es-ES";
    }

    public static class Tags {
        public static final String ATTRIBUTE_TAG_HREF = "href";
        public static final String INPUT_TAG = "input";
        public static final String ATTRIBUTE_TAG_VALUE = "value";
        public static final String ATTRIBUTE_TAG_ACTION = "action";
        public static final String ATTRIBUTE_TAG_NAME = "name";
        public static final String FORM_TAG = "form";
    }

    public static class ParamValues {
        public static final String ACCOUNT_SELECTED = "cuentaSel";
        public static final String RETURN_PAGE_KEY = "clavePaginaVolver";
        public static final String STATE_DESCRIPTION = "descEstado";
        public static final String SORT_ORDER = "ordenBusqueda";
        public static final String SEARCH_TYPE = "tipoBusqueda";
        public static final String PAGINATION_FIELD = "campoPaginacion";
        public static final String ACCOUNT_DESCRIPTION = "descripcionCuenta";
        public static final String PAGE_KEY = "clavePagina";
        public static final String FIRST_TIME = "primeraVez";
        public static final String CURRENT_PAGE = "paginaActual";
        public static final String CARD_NUMBER = "numeroTarjeta";
        public static final String PAGE_SIZE = "tamanioPagina";
        public static final String SELECTED_ACCOUNT = "SELCTA";
        public static final String FROM_DATE = "fechaDesde";
        public static final String TO_DATE = "fechaHasta";
        public static final String ACCOUNT = "cuenta";
        public static final String CARD_CODE = "codigoTarjeta";
        public static final String CARD_TYPE = "tipoTarjeta";
        public static final String CODE_CARD_TYPE = "codTipoTarjeta";
        public static final String ENTITY_CARD = "entidadTarjeta";
        public static final String AGREEMENT_CARD = "acuerdoTarjeta";
        public static final String DESCRIPTION_PAN = "DESCR_PAN";
        public static final String DESCRIPTION_CARD_TYPE = "DESCR_TIPOTAR";
    }

    public static class CssSelectors {
        private static final String CSS_BASE_OPEN = "input[name=";
        private static final String CSS_CLOSE_TAG = "]";
        public static final String CSS_CARD_CODE =
                CSS_BASE_OPEN + ParamValues.CARD_CODE + CSS_CLOSE_TAG;
        public static final String CSS_CARD_TYPE =
                CSS_BASE_OPEN + ParamValues.CARD_TYPE + CSS_CLOSE_TAG;
        public static final String CSS_CODE_CARD_TYPE =
                CSS_BASE_OPEN + ParamValues.CODE_CARD_TYPE + CSS_CLOSE_TAG;
        public static final String CSS_ENTITY_CARD =
                CSS_BASE_OPEN + ParamValues.ENTITY_CARD + CSS_CLOSE_TAG;
        public static final String CSS_AGREEMENT_CARD =
                CSS_BASE_OPEN + ParamValues.AGREEMENT_CARD + CSS_CLOSE_TAG;
        public static final String CSS_DESCRIPTION_PAN =
                CSS_BASE_OPEN + ParamValues.DESCRIPTION_PAN + CSS_CLOSE_TAG;
        public static final String CSS_DESCRIPTION_CARD_TYPE =
                CSS_BASE_OPEN + ParamValues.DESCRIPTION_CARD_TYPE + CSS_CLOSE_TAG;
    }
}
