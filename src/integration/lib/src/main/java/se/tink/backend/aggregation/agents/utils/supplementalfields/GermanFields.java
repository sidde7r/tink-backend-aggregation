package se.tink.backend.aggregation.agents.utils.supplementalfields;

import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.IntStream;
import se.tink.backend.agents.rpc.Field;
import se.tink.backend.agents.rpc.SelectOption;
import se.tink.backend.aggregation.agents.utils.berlingroup.consent.ScaMethodEntity;
import se.tink.libraries.i18n.Catalog;
import se.tink.libraries.i18n.LocalizableKey;

public class GermanFields {

    public static class Startcode {

        private static final String FIELD_KEY = "startcodeField";

        private static final LocalizableKey DESCRIPTION = new LocalizableKey("Startcode");
        private static final LocalizableKey HELPTEXT =
                new LocalizableKey(
                        "Insert your girocard into the TAN-generator and press \"TAN\". Enter the startcode and press \"OK\".");

        public static Field build(Catalog catalog, String startcode) {
            return CommonFields.Information.build(
                    FIELD_KEY,
                    catalog.getString(DESCRIPTION),
                    startcode,
                    catalog.getString(HELPTEXT));
        }
    }

    public static class Tan {
        public static TanBuilder builder(Catalog catalog) {
            return new TanBuilder(catalog);
        }
    }

    public static class SelectOptions {

        public static List<SelectOption> prepareSelectOptions(
                List<? extends SelectEligible> methods) {
            return IntStream.range(0, methods.size())
                    .mapToObj(
                            index -> {
                                SelectEligible selectEligible = methods.get(index);
                                return new SelectOption(
                                        selectEligible.getName(),
                                        String.valueOf(index + 1),
                                        selectEligible.getIconUrl());
                            })
                    .collect(Collectors.toList());
        }

        public static List<SelectOption> prepareSelectOptions(
                List<ScaMethodEntity> methods, ScaMethodEntityToIconMapper methodToIconMapper) {
            return IntStream.range(0, methods.size())
                    .mapToObj(
                            index -> {
                                ScaMethodEntity scaMethodEntity = methods.get(index);
                                return new SelectOption(
                                        scaMethodEntity.getName(),
                                        String.valueOf(index + 1),
                                        methodToIconMapper.getIconUrl(scaMethodEntity));
                            })
                    .collect(Collectors.toList());
        }
    }

    public interface SelectEligible {
        String getName();

        String getAuthenticationType();

        String getIconUrl();
    }

    public interface ScaMethodEntityToIconMapper {
        String getIconUrl(ScaMethodEntity scaMethodEntity);
    }
}
