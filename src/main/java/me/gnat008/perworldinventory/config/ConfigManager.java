/*
 * Copyright (C) 2014-2015  Gnat008
 *
 *     This program is free software: you can redistribute it and/or modify
 *     it under the terms of the GNU General Public License as published by
 *     the Free Software Foundation, either version 3 of the License, or
 *     (at your option) any later version.
 *
 *     This program is distributed in the hope that it will be useful,
 *     but WITHOUT ANY WARRANTY; without even the implied warranty of
 *     MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *     GNU General Public License for more details.
 *
 *     You should have received a copy of the GNU General Public License
 *     along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

package me.gnat008.perworldinventory.config;

import me.gnat008.perworldinventory.PerWorldInventory;
import org.bukkit.configuration.file.YamlConfiguration;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class ConfigManager {
    
    private Map<String, File> configFiles = new HashMap<>();
    private Map<File, YamlConfiguration> configs = new HashMap<>();
    
    private PerWorldInventory plugin;

    private static ConfigManager manager;

    private ConfigManager(PerWorldInventory plugin) {
        this.plugin = plugin;
    }

    public static ConfigManager getManager(PerWorldInventory plugin) {
        if (manager == null) {
            manager = new ConfigManager(plugin);
        }

        return manager;
    }

    public void disable() {
        plugin.getPrinter().printToConsole("Saving configs for shutdown.", false);
        for (String config : configFiles.keySet()) {
            saveConfig(config);
        }
        
        this.configFiles.clear();
        this.configs.clear();
        manager = null;
    }

    public boolean getShouldSerialize(String path) {
        return getConfig("config").getBoolean(path);
    }
    
    public File getConfigFile(String config) {
        return configFiles.containsKey(config) ? configFiles.get(config) : null;
    }
    
    public YamlConfiguration getConfig(String config) {
        return configs.containsKey(getConfigFile(config)) ? configs.get(getConfigFile(config)) : null;
    }
    
    public File addConfigFile(String name, File file, boolean addConfig) {
        checkNotNull(name);
        checkNotNull(file);
        
        configFiles.put(name, file);
        if (addConfig) {
            reloadConfig(name);
        }
        
        return file;
    }
    
    public void reloadConfigs() {
        for (String config : configFiles.keySet()) {
            reloadConfig(config);
        }
    }
    
    public void reloadConfig(String config) {
        if (!config.equalsIgnoreCase("worlds")) {
            setDefaults(config);
        } else {
            if (getConfig("config").getBoolean("first-start")) {
                setDefaults(config);
                getConfig("config").set("first-start", false);
                saveConfig("config");
            }
        }
        
        addConfig(getConfigFile(config), YamlConfiguration.loadConfiguration(getConfigFile(config)));
    }

    private void addConfig(File file, YamlConfiguration config) {
        checkNotNull(file);
        checkNotNull(config);

        configs.put(file, config);
    }
    
    private void addDefault(YamlConfiguration config, String path, Object value) {
        if (!(config.contains(path))) {
            config.set(path, value);
        }
    }
    
    private void checkNotNull(Object o) {
        if (o == null)
            throw new IllegalArgumentException("Parameter cannot be null!");
    }
    
    private void saveConfig(String config) {
        try {
            getConfig(config).save(getConfigFile(config));
        } catch (IOException ex) {
            plugin.getPrinter().printToConsole("Error saving " + config + ".yml': " + ex.getMessage(), true);
        }
    }
    
    private void setDefaults(String config) {
        YamlConfiguration configuration = YamlConfiguration.loadConfiguration(getConfigFile(config));
        
        if (config.equalsIgnoreCase("config")) {
            addDefault(configuration, "first-start", true);
            addDefault(configuration, "player.ender-chest", true);
            addDefault(configuration, "player.inventory", true);
            addDefault(configuration, "player.stats", true);
            addDefault(configuration, "player-stats.can-fly", true);
            addDefault(configuration, "player-stats.display-name", false);
            addDefault(configuration, "player-stats.exhaustion", true);
            addDefault(configuration, "player-stats.exp", true);
            addDefault(configuration, "player-stats.food", true);
            addDefault(configuration, "player-stats.flying", true);
            addDefault(configuration, "player-stats.gamemode", false);
            addDefault(configuration, "player-stats.health", true);
            addDefault(configuration, "player-stats.level", true);
            addDefault(configuration, "player-stats.potion-effects", true);
            addDefault(configuration, "player-stats.saturation", true);
        } else if (config.equalsIgnoreCase("worlds")) {
            List<String> defaults = new ArrayList<>();
            defaults.add("world");
            defaults.add("world_nether");
            defaults.add("world_the_end");
            addDefault(configuration, "groups.default", defaults);
        }
        
        try {
            configuration.save(getConfigFile(config));
        } catch (IOException ex) {
            plugin.getPrinter().printToConsole("Error saving " + config + ".yml': " + ex.getMessage(), true);
        }
    }
}
