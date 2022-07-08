package org.reprogle.honeypot.utils;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

import org.reprogle.honeypot.Honeypot;
import org.reprogle.honeypot.HoneypotConfigManager;

public class HoneypotLogger {

	private File logFile;

	public HoneypotLogger() {
		try {
			logFile = new File(Honeypot.getPlugin().getDataFolder(), "logs.txt");
			if (logFile.createNewFile()) {
				Honeypot.getPlugin().getLogger().info("File created: " + logFile.getName());
			} else {
				Honeypot.getPlugin().getLogger().info("File already exists.");
			}
		} catch (IOException e) {
			Honeypot.getPlugin().getLogger().severe("An error occurred.");
		}
	}

	public void log(String message) {
		if(Boolean.FALSE.equals(HoneypotConfigManager.getPluginConfig().getBoolean("enable-logging"))) return;
		try (BufferedWriter bw = new BufferedWriter(new FileWriter(logFile, true))){
			DateTimeFormatter dtf = DateTimeFormatter.ofPattern("yyyy/MM/dd HH:mm:ss");  
			LocalDateTime now = LocalDateTime.now();
            bw.append("[" + dtf.format(now) + "] " + message + "\n");
        } catch (IOException e) {
            e.printStackTrace();
        }
	}

}
