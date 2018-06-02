package se.tink.backend.common.workers.activity.generators;

import se.tink.libraries.i18n.Catalog;
import se.tink.backend.common.utils.DeepLinkBuilderFactory;
import se.tink.backend.common.utils.TemplateUtils;
import se.tink.backend.common.workers.activity.ActivityGenerator;

public abstract class CustomHtmlActivtyGenerator extends ActivityGenerator {

    public CustomHtmlActivtyGenerator(Class<? extends ActivityGenerator> generatorClass,
            DeepLinkBuilderFactory deepLinkBuilderFactory) {
        super(generatorClass, 70, deepLinkBuilderFactory);
    }

    public CustomHtmlActivtyGenerator(Class<? extends ActivityGenerator> generatorClass, double defaultImportance,
            DeepLinkBuilderFactory deepLinkBuilderFactory) {
        super(generatorClass, defaultImportance, deepLinkBuilderFactory);
    }

    public CustomHtmlActivtyGenerator(Class<? extends ActivityGenerator> generatorClass, double minimumImportance,
            double maximumImportance, DeepLinkBuilderFactory deepLinkBuilderFactory) {
        super(generatorClass, minimumImportance, maximumImportance, deepLinkBuilderFactory);
    }

    protected String createHtml(String body) {
        return createHtml("", body);
    }

    protected String createHtml(String head, String body) {
        String format = TemplateUtils.getTemplate("data/templates/html-activity-format.html");
        String defaultStyle = TemplateUtils.getTemplate("data/templates/default-style.html");
        return Catalog.format(format, defaultStyle, head, body);
    }

    @Override
    public boolean isNotifiable() {
        return false;
    }
}
