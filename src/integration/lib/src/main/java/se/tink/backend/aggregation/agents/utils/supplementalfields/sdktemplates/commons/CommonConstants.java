package se.tink.backend.aggregation.agents.utils.supplementalfields.sdktemplates.commons;

import lombok.experimental.UtilityClass;

@UtilityClass
public class CommonConstants {

    public static class FieldTypes {
        public static class BackwardCompatible {
            public static final String INPUT = "INPUT"; // an input that should be shown to the user
            public static final String TEXT = "TEXT"; // a text which should be shown to the user
        }

        public static class TinkLinkCompatible {
            public static final String CHANGE_METHOD =
                    "CHANGE_METHOD"; // for changing the 2FA method.
            public static final String COLOR =
                    "COLOR"; // describes a color that might be used as bgr in some of templates
            public static final String ICON =
                    "ICON"; // describes an image that should be included for display
            public static final String TEMPLATE =
                    "TEMPLATE"; // indicates what template should be used
        }
    }

    public static class FieldStyles {
        public static class BackwardCompatible {
            public static final String INSTRUCTION =
                    "INSTRUCTION"; // will show information as instruction
            public static final String TEXT =
                    "TEXT"; // describes an image that should be included for display
        }

        public static class TinkLinkCompatible {
            public static final String IDENTITY_HINT =
                    "IDENTITY_HINT"; // For displaying icon with text
            public static final String ORDERED_LIST = "ORDERED_LIST"; // will show a list of points
            public static final String POSITIONAL_INPUT =
                    "POSITIONAL_INPUT"; // An input style where the user needs to fill in only
            // specific fields of the full string
            public static final String TITLE = "TITLE"; // will show text as a title
        }
    }
}
