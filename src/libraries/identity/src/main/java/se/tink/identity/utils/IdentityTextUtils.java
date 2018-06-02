package se.tink.libraries.identity.utils;

import se.tink.backend.core.FraudDetailsContentType;
import se.tink.libraries.i18n.Catalog;
import se.tink.libraries.identity.model.IdentityEventDocumentation;

public class IdentityTextUtils {

    public enum Format {
        HTML,
        MARKDOWN
    }

    public static IdentityEventDocumentation getDocumentation(String locale, FraudDetailsContentType type,
            Format format) {
        Catalog catalog = Catalog.getCatalog(locale);

        IdentityEventDocumentation documentation = new IdentityEventDocumentation();
        documentation.setSourceTitle(getSourceTitle(catalog));
        documentation.setSourceText(getSourceFromType(type));

        documentation.setInfoTitle(getInfoTitle(catalog));
        documentation.setInfoText(getInfoTextForType(catalog, type));

        documentation.setHelpTitle(getHelpTitle(catalog));
        documentation.setHelpText(getHelpTextForType(catalog, type, format));

        documentation.setFraudText(catalog.getString("What can I do if this is not correct?"));

        return documentation;
    }

    private static String getHelpTextForType(Catalog catalog, FraudDetailsContentType type, Format format) {
        switch (type) {
        case ADDRESS:
        case IDENTITY:
        case INCOME:
            return String.format(catalog
                            .getString("If any of the information is incorrect, please contact Skatteverket on %s."),
                    getContactNumberFromType(type, format));
        case COMPANY_ENGAGEMENT:
            return Catalog
                    .format(catalog.getString(
                            "If any of the information is incorrect, please contact Bolagsverket on {0} or Skatteverket on {1}."),
                            getContactNumberFromType(FraudDetailsContentType.COMPANY_ENGAGEMENT, format),
                            getContactNumberFromType(FraudDetailsContentType.INCOME, format));
        case REAL_ESTATE_ENGAGEMENT:
            return String.format(catalog
                            .getString("If any of the information is incorrect, please contact Lantmäteriet on %s."),
                    getContactNumberFromType(type, format));
        case CREDITS:
        case NON_PAYMENT:
            return String.format(catalog
                            .getString("If any of the information is incorrect, please contact Kronofogden on %s."),
                    getContactNumberFromType(type, format));
        case SCORING:
            return String
                    .format(
                            catalog
                                    .getString(
                                            "If you have any questions about you credit score, please contact Creditsafe on %s."),
                            getContactNumberFromType(type, format));
        case DOUBLE_CHARGE:
        case FREQUENT_ACCOUNT_ACTIVITY:
            return catalog
                    .getString(
                            "If you don’t recognise the transactions, please contact your bank and let them check who registered the transactions.");
        case LARGE_EXPENSE:
        case LARGE_WITHDRAWAL:
            return catalog
                    .getString(
                            "If you don’t recognise the transaction, please contact your bank and let them check who registered the purchase.");
        }
        return "";
    }

    private static String getContactNumberFromType(FraudDetailsContentType type, Format format) {
        switch (type) {
        case ADDRESS:
        case IDENTITY:
        case INCOME:
            switch (format) {
            case HTML:
                return "<a class=\"phonenumber\" href=\"tel://0771567567\">0771–567 567</a>";
            case MARKDOWN:
                return "[0771–567 567](tel://0771567567)";
            default:
                return "";
            }
        case COMPANY_ENGAGEMENT:
            switch (format) {
            case HTML:
                return "<a class=\"phonenumber\" href=\"tel://0771670670\">0771-670 670</a>";
            case MARKDOWN:
                return "[0771-670 670](tel://0771670670)";
            default:
                return "";
            }
        case REAL_ESTATE_ENGAGEMENT:
            switch (format) {
            case HTML:
                return "<a class=\"phonenumber\" href=\"tel://0771636363\">0771-63 63 63</a>";
            case MARKDOWN:
                return "[0771-63 63 63](tel://0771636363)";
            default:
                return "";
            }
        case CREDITS:
        case NON_PAYMENT:
            switch (format) {
            case HTML:
                return "<a class=\"phonenumber\" href=\"tel://0771737300\">0771‑73 73 00</a>";
            case MARKDOWN:
                return "[0771‑73 73 00](tel://0771737300)";
            default:
                return "";
            }
        case SCORING:
            switch (format) {
            case HTML:
                return "<a class=\"phonenumber\" href=\"tel://0317255000\">031-725 50 00</a>";
            case MARKDOWN:
                return "[031-725 50 00](tel://0317255000)";
            default:
                return "";
            }
        default:
            return "";
        }
    }

    public static String getSourceFromType(FraudDetailsContentType type) {
        switch (type) {
        case ADDRESS:
        case IDENTITY:
            return "Statens personadressregister, SPAR";
        case COMPANY_ENGAGEMENT:
            return "Bolagsverket, Skatteverket";
        case REAL_ESTATE_ENGAGEMENT:
            return "Lantmäteriet";
        case CREDITS:
        case NON_PAYMENT:
            return "Kronofogden";
        case SCORING:
            return "Creditsafe";
        case DOUBLE_CHARGE:
        case LARGE_EXPENSE:
        case LARGE_WITHDRAWAL:
        case FREQUENT_ACCOUNT_ACTIVITY:
            return "Tink";
        case INCOME:
            return "Skatteverket";
        }
        return "";
    }

    private static String getInfoTextForType(Catalog catalog, FraudDetailsContentType type) {
        switch (type) {
        case ADDRESS:
        case IDENTITY:
            return catalog
                    .getString("The information is fetched from Statens personadressregister. SPAR includes all persons who are registered as resident in Sweden. The data in SPAR is updated each day with data from the Swedish Population Register.");
        case COMPANY_ENGAGEMENT:
            return catalog
                    .getString("The information is fetched from Bolagsverket and Skatteveket. Bolagsverket is a public authority whose main purpose is to register new companies and handle changes in register for existing companies, such as changes in address and changes of board and accountants. Skatteverket handled F-tax and moms.");
        case REAL_ESTATE_ENGAGEMENT:
            return catalog
                    .getString("The information is fetched from Lantmäteriet. Lantmäteriet is the authority that handles geographical information, appoints district names and is responsible for real estate classification.");
        case CREDITS:
            return catalog
                    .getString("The information is fetched from Kronofogden. Kronofogden is a neutral administrative authority. The assignment is to create a balance between the one paying and the one who is payed. It is the duty of Kronofogden to help the one not getting payed, but also supporting the one who cannot pay a debt.");
        case NON_PAYMENT:
            return catalog
                    .getString("The information is fetched from Kronofogden. A non-payment is a sign of you not being able to handle your payments. The effect could for example be that your are not getting a mortgage, apartment, job or phone subscription.");
        case INCOME:
            return catalog
                    .getString("The information is fetched from Skatteverket when your tax return has been approved.");
        case SCORING:
            return catalog
                    .getString("Creditsafe scoring is a tool to determine the solvency level for a person. The scoring is done by a number of generic credit scoring models and is mainly based on income, assets, age, company engagements, civil status and non-payments. The scoring is a scale from 1 to 100 where 1-19 means low solvency, 20-39 medium solvency, 40-69 high solvency and 70-100 very high solvency.");
        case DOUBLE_CHARGE:
            return catalog
                    .getString("Every day Tink scans all of your transactions to find things that differ from you normal behaviour and things we want to keep you extra alerted on. In this case we have notices there were two transactions deducted from your account the same day with the same amount and description. It could be someone with bad intentions that has charged you twice but it could of course also been you buying the same thing twice the same day.");
        case LARGE_EXPENSE:
            return catalog
                    .getString("Every day Tink scans all of your transactions to find things that differ from you normal behaviour and things we want to keep you extra alerted on. In this case we have notices there was an unusual large expense on your account.");
        case LARGE_WITHDRAWAL:
            return catalog
                    .getString("Every day Tink scans all of your transactions to find things that differ from you normal behaviour and things we want to keep you extra alerted on. In this case we have notices there was an unusual large cash withdrawal on your account.");
        case FREQUENT_ACCOUNT_ACTIVITY:
            return catalog
                    .getString("Every day Tink scans all of your transactions to find things that differ from you normal behaviour and things we want to keep you extra alerted on. In this case we have notices there was an unusual high activity on one of you accounts with more transactions than usual.");
        }
        return "";
    }

    private static String getSourceTitle(Catalog catalog) {
        return catalog.getString("Source") + ":";
    }

    private static String getInfoTitle(Catalog catalog) {
        return catalog.getString("Where does this information come from?");
    }

    private static String getHelpTitle(Catalog catalog) {
        return catalog.getString("What can I do if this looks weird?");
    }
}
