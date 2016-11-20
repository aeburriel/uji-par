package es.uji.apps.par.config;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.InputStream;

public class ConfigurationInFile implements ConfigurationInterface {
	@Override
	public InputStream getPathToFile() throws FileNotFoundException {
		return new FileInputStream("/etc/uji/par/app.properties");
	}
}
