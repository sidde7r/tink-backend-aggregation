package se.tink.backend.common.workers.activity.renderers.models;

public class Icon
{
    private char character;
    private String colorType;

    public String getColorType() {
        return colorType;
    }

    public void setColorType(String colorType) {
        this.colorType = colorType;
    }

    public char getChar() {
        return character;
    }

    public int getHtmlChar()
    {
        return character;
    }

    public void setChar(char icon) {
        this.character = icon;
    }

    public static final class IconColorTypes
    {
        public static final String INCOME = "income";
        public static final String EXPENSE = "expense";
        public static final String LEFT_TO_SPEND = "left-to-spend";
        public static final String TRANSFER = "transfer";
        public static final String UNCATEGORIZED = "uncategorized";
        public static final String POSITIVE = "positive";
        public static final String WARNING = "warning";
        public static final String CRITICAL = "critical";
        public static final String INFO = "info";
        public static final String GREY = "grey";
        public static final String TINK_ORANGE = "tink-orange";
    }

}
