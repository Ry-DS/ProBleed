package com.SimplyBallistic.ProBleed.util;

import org.bukkit.ChatColor;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.plugin.Plugin;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.file.Files;
import java.nio.file.StandardCopyOption;

public class LanguageYml {
	private static YamlConfiguration config;
	public LanguageYml(Plugin plugin) {
		
		File f=new File(plugin.getDataFolder(), "Language.yml");
		if(f.exists()){
		config=YamlConfiguration.loadConfiguration(f);
		config.setDefaults(YamlConfiguration.loadConfiguration(new BufferedReader(new InputStreamReader(plugin.getResource("Language.yml")))));


		plugin.getLogger().info("Lang Yaml found! Loading...");
		}
		else
			try {
				plugin.getLogger().info("Lang Yaml not found! Loading default...");
				Files.copy(plugin.getResource("Language.yml"), f.toPath(), StandardCopyOption.REPLACE_EXISTING);
				config=YamlConfiguration.loadConfiguration(f);

			} catch (IOException e) {
				e.printStackTrace();
			}
		
		
		
	}
	public static String get(String key){
		return ChatColor.translateAlternateColorCodes('&', config.getString(key));
		
	}

}
