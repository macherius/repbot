package net.sf.repbot.preferences;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.Reader;
import java.nio.charset.Charset;
import java.util.Properties;

public class PreferencesImpl implements Preferences {

	public final static String REPBOT_PROPERTIES_PATH = "repbot.properties";
	private final static String REPBOT_DEFAULT_PROPERTIES_PATH = "/repbot.default.properties";

	private Properties userProperties = null;
	private Properties defaultProperties = null;
	private boolean isDebug = false;

	public PreferencesImpl() throws IllegalArgumentException {
		this(REPBOT_PROPERTIES_PATH, REPBOT_DEFAULT_PROPERTIES_PATH);
	}

	public PreferencesImpl(String userPrefsFilePath) throws IllegalArgumentException {
		this(userPrefsFilePath, REPBOT_DEFAULT_PROPERTIES_PATH);
	}

	public PreferencesImpl(String userPrefsFilePath, String defaultPrefsResourcePath) throws IllegalArgumentException {
		this.defaultProperties = loadPropertiesFromResource(defaultPrefsResourcePath);
		this.userProperties = loadPropertiesFromFile(userPrefsFilePath, this.defaultProperties);
	}

	private static Properties loadPropertiesFromResource(String resourcePath) throws IllegalArgumentException {
		try (Reader stream = new InputStreamReader(PreferencesImpl.class.getResourceAsStream(resourcePath),
				Charset.forName("US-ASCII"));) {
			Properties props = new Properties();
			props.load(stream);
			return props;
		} catch (NullPointerException npe) {
			throw new IllegalArgumentException("Failed to open resource:", npe);
		} catch (IOException ioe) {
			throw new IllegalArgumentException("Failed to read from resource:", ioe);
		}
	}

	public static Properties loadPropertiesFromFile(String filePath, Properties defaultProperties)
			throws IllegalArgumentException {
		try (Reader stream = new InputStreamReader(new FileInputStream(filePath), Charset.forName("US-ASCII"));) {
			Properties props = new Properties(defaultProperties);
			props.load(stream);
			return props;
		} catch (FileNotFoundException fnfe) {
			throw new IllegalArgumentException("Failed to open file: " + filePath, fnfe);
		} catch (IOException | NullPointerException ioe) {
			throw new IllegalArgumentException("Failed to read from file: " + filePath, ioe);
		}
	}
	
	@Override
	public Properties getUserProperties() {
		return userProperties;
	}

	@Override
	public Properties getDefaultProperties() {
		return defaultProperties;
	}

	public boolean isDebug() {
		return isDebug;
	}

}
