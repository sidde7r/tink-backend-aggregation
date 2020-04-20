package se.tink.backend.aggregation.agents.nxgen.se.banks.seb;

public class SebConstants {
    public static class Urls {
        public static final String BASE = "https://mp.seb.se/1000/ServiceFactory/PC_BANK";

        public static final String LIST_ACCOUNTS =
                BASE + "/PC_BankLista01Konton_privat01.asmx/Execute";
    }
}
