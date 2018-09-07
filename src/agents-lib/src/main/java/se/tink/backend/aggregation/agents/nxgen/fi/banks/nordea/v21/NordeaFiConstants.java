package se.tink.backend.aggregation.agents.nxgen.fi.banks.nordea.v21;

import com.google.common.collect.Maps;
import java.util.Map;
import se.tink.backend.aggregation.nxgen.core.account.LoanDetails;
import se.tink.backend.aggregation.rpc.AccountTypes;

public class NordeaFiConstants {
    public static final String MARKET_CODE = "FI";
    public static final String CURRENCY = "EUR";

    public static class AccountType {
        private static final Map<String, String> ACCOUNT_NAMES_BY_CODE = Maps.newHashMap();
        private static final Map<String, AccountTypes> ACCOUNT_TYPES_BY_CODE = Maps.newHashMap();
        private static final Map<String, LoanDetails.Type> LOAN_TYPES_BY_CODE = Maps.newHashMap();

        static {
            addAccountType("FI0033","Time deposit invest. acct", AccountTypes.INVESTMENT);
            addAccountType("FI0035","Time deposit acc", AccountTypes.INVESTMENT);
            addAccountType("FI0313","Time deposit acc", AccountTypes.INVESTMENT);
            addAccountType("FI0314","ASP-tili", AccountTypes.INVESTMENT);
            addAccountType("FI0315","DepositPlus", AccountTypes.INVESTMENT);
            addAccountType("FI0316","InvestmentDeposit account", AccountTypes.INVESTMENT);
            addAccountType("FI0323","ProPersonnel Account", AccountTypes.INVESTMENT);
            addAccountType("FI0324", "Entrepreneurs PerkAccount", AccountTypes.INVESTMENT);
            addAccountType("FI0325","Continuous investment account", AccountTypes.INVESTMENT);
            addAccountType("FI0326","Junior account", AccountTypes.INVESTMENT);
            addAccountType("FI0328","Time deposit acc", AccountTypes.INVESTMENT);
            addAccountType("FI0329","Interest Extra Account", AccountTypes.INVESTMENT);
            addAccountType("FI033","Fixed-term investment account", AccountTypes.INVESTMENT);

            addAccountType("FI0331","Current account", AccountTypes.CHECKING);
            addAccountType("FI0337","CurrentAccount", AccountTypes.CHECKING);
            addAccountType("FI0339","Growth Account", AccountTypes.CHECKING);
            addAccountType("FI0340","Deposit account", AccountTypes.CHECKING);
            addAccountType("FI0342","Disposal account", AccountTypes.CHECKING);
            addAccountType("FI0343","ASP-tili", AccountTypes.CHECKING);
            addAccountType("FI0344","Servicing account", AccountTypes.CHECKING);
            addAccountType("FI0345","Tele account", AccountTypes.CHECKING);
            addAccountType("FI0346","Tele account", AccountTypes.CHECKING);
            addAccountType("FI0347","Parkki account", AccountTypes.CHECKING);
            addAccountType("FI0348","Disposal account", AccountTypes.CHECKING);
            addAccountType("FI035","Fixed-term account", AccountTypes.CHECKING);
            addAccountType("FI0351","Direct usage account", AccountTypes.CHECKING);
            addAccountType("FI0352","Korkoplustili", AccountTypes.CHECKING);
            addAccountType("FI0353","PerkAccount", AccountTypes.CHECKING);
            addAccountType("FI0354","Direct usage credit", AccountTypes.CHECKING);
            addAccountType("FI0355","Time deposit sav. account", AccountTypes.CHECKING);
            addAccountType("FI0361","Fixed-term currency acc.", AccountTypes.CHECKING);
            addAccountType("FI0364","PS-tili", AccountTypes.CHECKING);
            addAccountType("FI0610","Sight curr. deposit acc", AccountTypes.CHECKING);
            addAccountType("FI0620","Personal currency acc", AccountTypes.CHECKING);
            addAccountType("FI0630","Currency account/Gold", AccountTypes.CHECKING);

            addAccountType("FI35300","Visa Silver", AccountTypes.CREDIT_CARD);
            addAccountType("FI35700","Visa Silver", AccountTypes.CREDIT_CARD);
            addAccountType("FI36300","Visa Gold", AccountTypes.CREDIT_CARD);
            addAccountType("FI36690","Nordea Electron", AccountTypes.CREDIT_CARD);
            addAccountType("FI39000","Nordea Credit", AccountTypes.CREDIT_CARD);
            addAccountType("FI39100","Nordea Credit", AccountTypes.CREDIT_CARD);
            addAccountType("FI39200","Nordea Gold", AccountTypes.CREDIT_CARD);
            addAccountType("FI39210","MasterCard Premium", AccountTypes.CREDIT_CARD);
            addAccountType("FI39300","Nordea Gold", AccountTypes.CREDIT_CARD);
            addAccountType("FI39310","MasterCard Premium", AccountTypes.CREDIT_CARD);
            addAccountType("FI39412","Finnair Plus MC", AccountTypes.CREDIT_CARD);
            addAccountType("FI39415","Stockmann MC", AccountTypes.CREDIT_CARD);
            addAccountType("FI39512","Finnair Plus MC", AccountTypes.CREDIT_CARD);
            addAccountType("FI39514","Tuohi MasterCard", AccountTypes.CREDIT_CARD);
            addAccountType("FI39515","Stockmann MC", AccountTypes.CREDIT_CARD);
            addAccountType("FI39700","Nordea Platinum", AccountTypes.CREDIT_CARD);
            addAccountType("FI82020","MasterCard Liiga", AccountTypes.CREDIT_CARD);
            addAccountType("FI82120","MasterCard Liiga", AccountTypes.CREDIT_CARD);
            addAccountType("FI82400","Visa Debit", AccountTypes.CREDIT_CARD);
            addAccountType("FI82500","Business Visa Debit", AccountTypes.CREDIT_CARD);
            addAccountType("FI82890","Nordea Electron", AccountTypes.CREDIT_CARD);
            addAccountType("FI82900","Nordea Credit", AccountTypes.CREDIT_CARD);
            addAccountType("FI83000","Nordea Credit", AccountTypes.CREDIT_CARD);
            addAccountType("FI83100","Nordea Gold", AccountTypes.CREDIT_CARD);
            addAccountType("FI83200","Nordea Black", AccountTypes.CREDIT_CARD);
          
            addAccountType("FI0349", "HomeFlex", AccountTypes.LOAN);
            addAccountType("FI60001", "Flexicredit", AccountTypes.LOAN, LoanDetails.Type.BLANCO);
            addAccountType("FI60002", "Flexicredit", AccountTypes.LOAN, LoanDetails.Type.BLANCO);
            addAccountType("FI60003", "Flexicredit", AccountTypes.LOAN, LoanDetails.Type.BLANCO);
            addAccountType("FI60100", "Limit Flexicredit", AccountTypes.LOAN, LoanDetails.Type.BLANCO);
            addAccountType("FI11111", "Housing loan", AccountTypes.LOAN, LoanDetails.Type.MORTGAGE);
            addAccountType("FI50000", "Study loan", AccountTypes.LOAN, LoanDetails.Type.STUDENT);
            addAccountType("FI60000", "Other loan", AccountTypes.LOAN);
            addAccountType("FIO", "A1 Car Credit", AccountTypes.LOAN, LoanDetails.Type.VEHICLE);
        }

        private static void addAccountType(String code, String name, AccountTypes type) {
            ACCOUNT_NAMES_BY_CODE.put(code, name);
            ACCOUNT_TYPES_BY_CODE.put(code, type);
        }

        private static void addAccountType(String code, String name, AccountTypes type, LoanDetails.Type loanType) {
            addAccountType(code, name, type);
            LOAN_TYPES_BY_CODE.put(code, loanType);
        }

        public static String getAccountNameFromCode(String code) {
            return ACCOUNT_NAMES_BY_CODE.getOrDefault(code, "");
        }

        public static AccountTypes getAccountTypeFromCode(String code) {
            return ACCOUNT_TYPES_BY_CODE.getOrDefault(code, AccountTypes.CHECKING);
        }

        public static LoanDetails.Type getLoanTypeFromCode(String code) {
            return LOAN_TYPES_BY_CODE.getOrDefault(code, LoanDetails.Type.OTHER);
        }
    }
}
