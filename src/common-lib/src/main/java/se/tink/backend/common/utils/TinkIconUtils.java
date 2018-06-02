package se.tink.backend.common.utils;

import com.google.common.collect.ImmutableMap;
import se.tink.backend.core.Category;

public class TinkIconUtils {

    public static class IconsV2 {
        public static final char HOME = '\ue900';
        public static final char HOUSEANDGARDEN = '\ue901';
        public static final char FOODANDDRINKS = '\ue902';
        public static final char TRANSPORT = '\ue903';
        public static final char SHOPPING = '\ue904';
        public static final char LEISURE = '\ue905';
        public static final char HEALTHANDBEAUTY = '\ue906';
        public static final char OTHER = '\ue907';
        public static final char UNCATEGORIZED = '\ue908';
        public static final char SAVINGS = '\ue909';
        public static final char TRANSFER = '\ue90a';
        public static final char EXCLUDE = '\ue90b';
        public static final char SALARY = '\ue90c';
        public static final char PENSION = '\ue90d';
        public static final char REIMBURSEMENTS = '\ue90e';
        public static final char BENEFITS = '\ue90f';
        public static final char FINANCIAL = '\ue910';
        public static final char OTHERINCOME = '\ue911';
        public static final char OVERVIEW = '\ue912';
        public static final char FEED = '\ue913';
        public static final char ACCOUNTS = '\ue914';
        public static final char RESIDENCE = '\ue915';
        public static final char PROFILE = '\ue916';
        public static final char ARROWUP = '\ue917';
        public static final char ARROWDOWN = '\ue918';
        public static final char ARROWLEFT = '\ue919';
        public static final char ARROWRIGHT = '\ue91a';
        public static final char ARROWSEMIUP = '\ue91b';
        public static final char ARROWSEMIDOWN = '\ue91c';
        public static final char SEARCH = '\ue91d';
        public static final char PHOTO = '\ue91e';
        public static final char EDITPEN = '\ue91f';
        public static final char ARROWSMALLUP = '\ue920';
        public static final char ARROWSMALLDOWN = '\ue921';
        public static final char ARROWSMALLRIGHT = '\ue922';
        public static final char ARROWSMALLLEFT = '\ue923';
        public static final char CLOSE = '\ue924';
        public static final char ACCEPT = '\ue925';
        public static final char ARROWSMALLBACK = '\ue926';
        public static final char STAR = '\ue927';
        public static final char STARFILLED = '\ue928';
        public static final char PLUS = '\ue929';
        public static final char UPDATE = '\ue92a';
        public static final char PAUSE = '\ue92b';
        public static final char TRASHCAN = '\ue92c';
        public static final char PLAY = '\ue92d';
        public static final char RELOAD = '\ue92e';
        public static final char MOREOPTIONS = '\ue92f';
        public static final char SETTINGS = '\ue930';
        public static final char EVENT = '\ue931';
        public static final char ALERT = '\ue932';
        public static final char DOUBLETRANSACTION = '\ue933';
        public static final char PAYBILLS = '\ue934';
        public static final char ADDBUDGET = '\ue935';
        public static final char ADDSAVINGSGOAL = '\ue936';
        public static final char ADDBANK = '\ue937';
        public static final char SHAREDACCOUNT = '\ue938';
        public static final char INFO = '\ue939';
    }

    public static class Icons {
        public static final char TRANSFER = '\ue014';
        public static final char ACCOUNTS = '\ue023';
        public static final char ADD = '\ue091';
        public static final char ALERT = '\ue021';
        public static final char BACK_ARROW = '\ue028';
        public static final char BANKING = '\ue048';
        public static final char BANK_FEE = '\ue151';
        public static final char BUDGET = '\ue024';
        public static final char CASH_BACK = '\ue025';
        public static final char CERTIFIED = '\ue049';
        public static final char CHECK = '\ue038';
        public static final char CLOSE = '\ue039';
        public static final char CLOSE_THIN = '\ue063';
        public static final char DOWN = '\ue030';
        public static final char EDIT = '\ue058';
        public static final char ERASE = '\ue046';
        public static final char EXPENSES = '\ue017';
        public static final char FACEBOOK = '\ue044';
        public static final char FACEBOOK_MARKED = '\ue061';
        public static final char FEED = '\ue092';
        public static final char FOLD_DOWN = '\ue029';
        public static final char FOLD_UP = '\ue026';
        public static final char FOLLOW = '\ue093';
        public static final char INCOME = '\ue018';
        public static final char INFO = '\ue019';
        public static final char LEFT_TO_SPEND = '\ue108';
        public static final char LOCK_WIDE_OPEN = '\ue103';
        public static final char LOGO = '\ue022';
        public static final char LOGOTYPE = '\ue057';
        public static final char MAIL = '\ue042';
        public static final char MAIL_MARKED = '\ue059';
        public static final char MENU = '\ue027';
        public static final char NEXT = '\ue054';
        public static final char PREVIOUS = '\ue053';
        public static final char PROCESSING = '\ue070';
        public static final char QUIT = '\ue050';
        public static final char ROUND_RIGHT = '\ue055';
        public static final char SAFE = '\ue051';
        public static final char SAVINGS = '\ue015';
        public static final char SEARCH = '\ue047';
        public static final char SETTINGS = '\ue033';
        public static final char SHARE = '\ue081';
        public static final char SHARE_FACEBOOK = '\ue084';
        public static final char SHARE_GOOGLE_PLUS = '\ue082';
        public static final char SHARE_INSTAGRAM = '\ue083';
        public static final char SHARE_TWITTER = '\ue085';
        public static final char SHARED_ACCOUNT = '\ue094';
        public static final char SHARED_ACCOUNT_OUTLINED = '\ue095';
        public static final char STAR = '\ue041';
        public static final char STAR_OUTLINED = '\ue067';
        public static final char TARGET = '\ue034';
        public static final char TARGET_BACK = '\ue035';
        public static final char TARGET_JUMP = '\ue036';
        public static final char TARGET_JUMP_BACK = '\ue037';
        public static final char TRASH_CAN = '\ue090';
        public static final char TWITTER = '\ue045';
        public static final char TWITTER_MARKED = '\ue062';
        public static final char TWO_ARRROWS = '\ue052';
        public static final char UP = '\ue031';
        public static final char UP_DOWN = '\ue040';
        public static final char VIEW_GRAPH = '\ue064';
        public static final char VIEW_LIST = '\ue065';
        public static final char VIEW_PIES = '\ue066';
        public static final char WARNING = '\ue020';
        public static final char WEB = '\ue043';
        public static final char WEB_MARKED = '\ue060';
        public static final char LARGE_EXPENSE = '\ue132';
        public static final char DOUBLE_CHARGE = '\ue131';
        public static final char EINVOICE = '\ue162';
        public static final char HOME = '\ue069';
        public static final char ENTERTAINMENT = '\ue003';
        public static final char SHOPPING = '\ue004';
        public static final char WELLNESS = '\ue006';
        public static final char HOUSE = '\ue005';
        public static final char MISC = '\ue007';
        public static final char FOOD = '\ue013';
        public static final char TRANSPORT = '\ue012';
        public static final char UNCATEGORIZED = '\ue032';
        public static final char SALARY = '\ue068';
        public static final char PENSION = '\ue008';
        public static final char REFUND = '\ue009';
        public static final char BENEFITS = '\ue010';
        public static final char FINANCIAL = '\ue011';
        public static final char OTHER = '\ue056';

        public static final char TRANSFERS = '\ue014';
        public static final char TRANSFERS_SAVINGS = '\ue015';
        public static final char TRANSFERS_OTHER = '\ue014';
        public static final char TRANSFERS_EXCLUDE = '\ue016';
    }

    private static final ImmutableMap<String, Character> CATEGORY_ICONS_V1 = ImmutableMap.<String, Character>builder()
            .put("expenses", Icons.EXPENSES)
            .put("expenses:home", Icons.HOME)
            .put("expenses:entertainment", Icons.ENTERTAINMENT)
            .put("expenses:shopping", Icons.SHOPPING)
            .put("expenses:wellness", Icons.WELLNESS)
            .put("expenses:house", Icons.HOUSE)
            .put("expenses:misc", Icons.MISC)
            .put("expenses:food", Icons.FOOD)
            .put("expenses:transport", Icons.TRANSPORT)
            .put("expenses:misc.uncategorized", Icons.UNCATEGORIZED)

            .put("income", Icons.INCOME )
            .put("income:salary", Icons.SALARY)
            .put("income:pension", Icons.PENSION)
            .put("income:refund", Icons.REFUND)
            .put("income:benefits", Icons.BENEFITS)
            .put("income:financial", Icons.FINANCIAL)
            .put("income:other", Icons.OTHER)

            .put("transfers", Icons.TRANSFERS)
            .put("transfers:savings", Icons.TRANSFERS_SAVINGS)
            .put("transfers:other", Icons.TRANSFERS_OTHER)
            .put("transfers:exclude", Icons.TRANSFERS_EXCLUDE)

            .build();

    private static final ImmutableMap<String, Character> CATEGORY_ICONS_V2 = ImmutableMap.<String, Character>builder()
            .put("expenses", IconsV2.OTHER)
            .put("expenses:home", IconsV2.HOME)
            .put("expenses:entertainment", IconsV2.LEISURE)
            .put("expenses:shopping", IconsV2.SHOPPING)
            .put("expenses:wellness", IconsV2.LEISURE)
            .put("expenses:house", IconsV2.HOUSEANDGARDEN)
            .put("expenses:misc", IconsV2.OTHER)
            .put("expenses:food", IconsV2.FOODANDDRINKS)
            .put("expenses:transport", IconsV2.TRANSPORT)
            .put("expenses:misc.uncategorized", IconsV2.UNCATEGORIZED)

            .put("income", IconsV2.OTHERINCOME)
            .put("income:salary", IconsV2.SALARY)
            .put("income:pension", IconsV2.PENSION)
            .put("income:refund", IconsV2.REIMBURSEMENTS)
            .put("income:benefits", IconsV2.BENEFITS)
            .put("income:financial", IconsV2.FINANCIAL)
            .put("income:other", IconsV2.OTHER)

            .put("transfers", Icons.TRANSFERS)
            .put("transfers:savings", Icons.TRANSFERS_SAVINGS)
            .put("transfers:other", Icons.TRANSFERS_OTHER)
            .put("transfers:exclude", Icons.TRANSFERS_EXCLUDE)

            .build();

    public static char getV1CategoryIcon(Category category) {
        return getV1CategoryIcon(category.getCode());
    }

    public static char getV1CategoryIcon(String code) {
        Character value = CATEGORY_ICONS_V1.get(code);

        if (value != null) {
            return value;
        } else {
            int i = code.lastIndexOf('.');

            if (i > 1) {
                String parentKey = code.substring(0, i);

                return getV1CategoryIcon(parentKey);
            }
        }
        return '\0';
    }

    public static char getV2CategoryIcon(Category category) {
        return getV2CategoryIcon(category.getCode());
    }

    public static char getV2CategoryIcon(String code) {
        Character value = CATEGORY_ICONS_V2.get(code);

        if (value != null) {
            return value;
        } else {
            int i = code.lastIndexOf('.');

            if (i > 1) {
                String parentKey = code.substring(0, i);

                return getV2CategoryIcon(parentKey);
            }
        }
        return '\0';
    }

}
