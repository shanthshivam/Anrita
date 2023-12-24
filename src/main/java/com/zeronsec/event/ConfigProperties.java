package com.zeronsec.event;

import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;

public class ConfigProperties {

	private static ConfigProperties cProperties;
	private static Properties properties = new Properties();

	public ConfigProperties() {
		try (InputStream iStream = getClass().getResourceAsStream("/config.properties")) {
            if (iStream != null) {
                properties.load(iStream);
            } else {
                throw new RuntimeException("config.properties not found in the classpath");
            }
        } catch (IOException e) {
            throw new RuntimeException("Failed to load config.properties", e);
        }

	}

	public static String getProperty(String property) {
		if (cProperties == null) {
			cProperties = new ConfigProperties();
		}
		return properties.getProperty(property);
	}

}
