package se.tink.backend.common.template;

import java.io.IOException;
import java.net.URI;

import com.github.jknack.handlebars.Handlebars;
import com.github.jknack.handlebars.Template;
import com.github.jknack.handlebars.TemplateLoader;
import com.github.jknack.handlebars.io.FileTemplateLoader;

public class TemplateFactory {
    protected static TemplateLoader loader;
    protected static Handlebars handlebars;

    static {
        try {
            loader = new FileTemplateLoader("./data/templates/", ".hbs");
            handlebars = new Handlebars(loader);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public static Template compileTemplate(String name) throws IOException {
        return handlebars.compile(URI.create(name));
    }
}
