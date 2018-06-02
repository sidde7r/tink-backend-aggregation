package se.tink.backend.insights.core.valueobjects;

public enum ActivityDivType {
    INSIGHT("insight"),
    INSIGHT_SMALL(String.format("%s small", INSIGHT.getValue())),
    INSIGHT_MEDIUM(String.format("%s medium", INSIGHT.getValue())),
    INSIGHT_LARGE(String.format("%s large", INSIGHT.getValue())),

    INSIGHT_INFO(String.format("%s info", INSIGHT.getValue())),
    INSIGHT_INFO_SMALL(String.format("%s small", INSIGHT_INFO.getValue())),
    INSIGHT_INFO_MEDIUM(String.format("%s medium", INSIGHT_INFO.getValue())),
    INSIGHT_INFO_LARGE(String.format("%s large", INSIGHT_INFO.getValue())),

    INSIGHT_WARNING(String.format("%s warning", INSIGHT.getValue())),
    INSIGHT_WARNING_SMALL(String.format("%s small", INSIGHT_WARNING.getValue())),
    INSIGHT_WARNING_MEDIUM(String.format("%s medium", INSIGHT_WARNING.getValue())),
    INSIGHT_WARNING_LARGE(String.format("%s large", INSIGHT_WARNING.getValue()));

    private final String value;

    ActivityDivType(String value) {
        this.value = value;
    }

    public String getValue() {
        return value;
    }
}
