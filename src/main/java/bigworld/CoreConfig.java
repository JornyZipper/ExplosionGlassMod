package bigworld;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.Properties;

/**
 * Lightweight file-based configuration for BigWorld.
 * Stores a single property `renderOptimizations.enabled` (default true).
 */
public final class CoreConfig {
    private static final String FILE_NAME = "bigworld.properties";
    private static final Properties PROPS = new Properties();

    private CoreConfig() {}

    public static void load() {
        File f = new File(System.getProperty("user.dir"), FILE_NAME);
        if (!f.exists()) {
            // create defaults
            PROPS.setProperty("renderOptimizations.enabled", "true");
            
            save();
            return;
        }

        try (FileInputStream in = new FileInputStream(f)) {
            PROPS.load(in);
        } catch (IOException e) {
            // ignore and keep defaults
        }
    }

    public static void save() {
        File f = new File(System.getProperty("user.dir"), FILE_NAME);
        try (FileOutputStream out = new FileOutputStream(f)) {
            PROPS.store(out, "BigWorld configuration");
        } catch (IOException e) {
            // ignore
        }
    }

    public static boolean isRenderOptimizationsEnabled() {
        return Boolean.parseBoolean(PROPS.getProperty("renderOptimizations.enabled", "true"));
    }

    public static void setRenderOptimizationsEnabled(boolean enabled) {
        PROPS.setProperty("renderOptimizations.enabled", Boolean.toString(enabled));
        save();
    }
    
}
