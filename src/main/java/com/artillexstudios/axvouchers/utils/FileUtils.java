package com.artillexstudios.axvouchers.utils;

import com.artillexstudios.axvouchers.AxVouchersPlugin;
import org.jetbrains.annotations.NotNull;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Iterator;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;

public class FileUtils {
    public static final Path PLUGIN_DIRECTORY = AxVouchersPlugin.getInstance().getDataFolder().toPath();;
    private static final Logger log = LoggerFactory.getLogger(FileUtils.class);

    public static void copyFromResource(@NotNull String path) {
        try (ZipFile zip = new ZipFile(AxVouchersPlugin.getInstance().getClass().getProtectionDomain().getCodeSource().getLocation().getPath())) {
            for (Iterator<? extends ZipEntry> it = zip.entries().asIterator(); it.hasNext(); ) {
                ZipEntry entry = it.next();
                if (entry.getName().startsWith(path + "/")) {
                    if (!entry.getName().endsWith(".yaml") && !entry.getName().endsWith(".yml")) continue;
                    InputStream resource = AxVouchersPlugin.getInstance().getResource(entry.getName());
                    if (resource == null) {
                        log.error("Could not find file {} in plugin's assets!", entry.getName());
                        continue;
                    }

                    Files.copy(resource, PLUGIN_DIRECTORY.resolve(entry.getName()));
                }
            }
        } catch (IOException exception) {
            log.error("An unexpected error occurred while extracting directory {} from plugin's assets!", path, exception);
        }
    }
}
