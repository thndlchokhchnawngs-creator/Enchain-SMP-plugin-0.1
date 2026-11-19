package dev.anyenchant;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.nio.charset.StandardCharsets;
import java.util.Locale;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.serializer.legacy.LegacyComponentSerializer;
import org.bukkit.command.CommandSender;
import org.bukkit.configuration.file.YamlConfiguration;

public final class Messages {

    private static final LegacyComponentSerializer LEGACY = LegacyComponentSerializer.legacyAmpersand();

    private final AnyEnchantPlugin plugin;
    private YamlConfiguration active = new YamlConfiguration();
    private YamlConfiguration fallback = new YamlConfiguration();

    public Messages(AnyEnchantPlugin plugin) {
        this.plugin = plugin;
        reload();
    }

    public void reload() {
        String lang = plugin.getConfig().getString("language", "th").toLowerCase(Locale.ROOT);
        File file = new File(plugin.getDataFolder(), "messages_" + lang + ".yml");
        active = file.exists() ? YamlConfiguration.loadConfiguration(file) : new YamlConfiguration();

        fallback = new YamlConfiguration();
        try (InputStream in = plugin.getResource("messages_en.yml")) {
            if (in != null) {
                try (Reader reader = new InputStreamReader(in, StandardCharsets.UTF_8)) {
                    fallback = YamlConfiguration.loadConfiguration(reader);
                }
            }
        } catch (IOException ex) {
            plugin.getLogger().warning("Could not read bundled messages: " + ex.getMessage());
        }
    }

    /** pairs = key1, value1, key2, value2 ... แทนที่ {key} ในข้อความ */
    public Component get(String key, String... pairs) {
        String raw = active.getString(key, fallback.getString(key, key));
        for (int i = 0; i + 1 < pairs.length; i += 2) {
            raw = raw.replace("{" + pairs[i] + "}", pairs[i + 1]);
        }
        return LEGACY.deserialize(raw);
    }

    public void send(CommandSender to, String key, String... pairs) {
        to.sendMessage(get("prefix").append(get(key, pairs)));
    }

    public void sendRaw(CommandSender to, String key, String... pairs) {
        to.sendMessage(get(key, pairs));
    }
}
