package se.tink.backend.system.cli.export;

import com.google.common.base.Preconditions;
import com.google.common.base.Strings;
import com.google.inject.Injector;
import freemarker.core.ParseException;
import freemarker.template.Configuration;
import freemarker.template.MalformedTemplateNameException;
import freemarker.template.Template;
import freemarker.template.TemplateException;
import freemarker.template.TemplateExceptionHandler;
import freemarker.template.TemplateNotFoundException;
import freemarker.template.Version;
import io.dropwizard.setup.Bootstrap;
import java.io.File;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.io.Writer;
import java.util.Locale;
import java.util.Map;
import net.sourceforge.argparse4j.inf.Namespace;
import se.tink.backend.common.ServiceContext;
import se.tink.backend.common.config.ServiceConfiguration;
import se.tink.backend.export.controller.ExportController;
import se.tink.backend.export.factory.ExportObjectFactory;
import se.tink.backend.export.helper.LocalFileTemplateLoader;
import se.tink.backend.export.helper.UserNotFoundException;
import se.tink.backend.system.cli.ServiceContextCommand;
import se.tink.backend.utils.LogUtils;

public class ExportUserDataCommand extends ServiceContextCommand<ServiceConfiguration> {

    private static final String EXPORT_TEMPLATE_MARKDOWN = "user-data-export-md.ftl";
    private static final String EXPORT_TEMPLATES_DIRECTORY = "/data/data-export";
    private static final LogUtils log = new LogUtils(ExportUserDataCommand.class);

    public ExportUserDataCommand() {
        super("export-user-data", "Exports user data according to GDPR compliance");
    }

    @Override
    public void run(Bootstrap<ServiceConfiguration> bootstrap, Namespace namespace, ServiceConfiguration configuration,
            Injector injector, ServiceContext serviceContext) {

        // Input validation
        final String userId = System.getProperty("userid");
        log.info(userId, "Exporting user's data");
        Preconditions.checkArgument(Strings.nullToEmpty(userId).trim().length() > 0);

        // Creator of export objects for mapping to template
        ExportObjectFactory exportObjectFactory = injector.getInstance(ExportObjectFactory.class);

        try {
            exportObjectFactory.validateUser(userId);
        } catch (UserNotFoundException e) {
            log.error(userId, e.getMessage());

        }

        ExportController controller = injector.getInstance(ExportController.class);

        populateTemplate(userId, controller.getMappedExportObjects(userId));

    }

    private void populateTemplate(String userId, Map<String, Object> mapper) {
        Configuration cfg = new Configuration(new Version("2.3.20"));

        // Where do we load the templates from:
        String userDir = System.getProperty("user.dir");
        File file = new File(userDir + EXPORT_TEMPLATES_DIRECTORY);

        LocalFileTemplateLoader templateLoader = new LocalFileTemplateLoader();

        templateLoader.setBaseDir(file);

        cfg.setTemplateLoader(templateLoader);

        // Some other recommended settings:
        cfg.setDefaultEncoding("UTF-8");
        cfg.setLocale(Locale.US);
        cfg.setTemplateExceptionHandler(TemplateExceptionHandler.RETHROW_HANDLER);

        try {
            Template template = cfg.getTemplate(EXPORT_TEMPLATE_MARKDOWN);

            // 2.3. Generate the output

            // Write output to the console
            Writer consoleWriter = new OutputStreamWriter(System.out);
            template.process(mapper, consoleWriter);

            // Note: Uncomment this to write to file
            //            // For the sake of example, also write output into a file:
            //            Writer fileWriter = new FileWriter(new File("<pathToDir>" + userId + ".md"));
            //            template.process(mapper, fileWriter);

        } catch (TemplateNotFoundException e) {
            log.error(userId, String.format("No export template found for %s", e.getTemplateName()), e);
        } catch (MalformedTemplateNameException e) {
            log.error(userId, String.format("Export template name not supported %s", e.getTemplateName()), e);
        } catch (ParseException e) {
            log.error(userId,
                    String.format("Could not parse template %s at line %d and column %d ", e.getTemplateName(),
                            e.getLineNumber(), e.getColumnNumber()), e);
        } catch (IOException | TemplateException e ) {
            log.error(userId, e);
        }
    }
}
