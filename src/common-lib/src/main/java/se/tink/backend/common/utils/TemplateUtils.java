package se.tink.backend.common.utils;

import java.io.File;
import java.io.IOException;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;

import com.google.common.base.Charsets;
import com.google.common.cache.CacheBuilder;
import com.google.common.cache.CacheLoader;
import com.google.common.cache.LoadingCache;
import com.google.common.io.Files;

import se.tink.backend.utils.LogUtils;

public class TemplateUtils {
    private static final LogUtils log = new LogUtils(TemplateUtils.class);

    private static LoadingCache<String, String> TEMPLATES_CACHE = CacheBuilder.newBuilder().maximumSize(1000)
            .expireAfterWrite(10, TimeUnit.MINUTES).build(new CacheLoader<String, String>() {
                public String load(String path) {
                    return loadTemplate(path);
                }
            });

    public static String getTemplate(String path) {
        try {
            return TEMPLATES_CACHE.get(path);
        } catch (ExecutionException e) {
            log.error("Could not get template from cache: " + path);
            return null;
        }
    }

    private static String loadTemplate(String path) {
        try {
            return Files.toString(new File(path), Charsets.UTF_8);
        } catch (IOException e) {
            log.error("Could not load template from path: " + path);
            return null;
        }
    }
}
