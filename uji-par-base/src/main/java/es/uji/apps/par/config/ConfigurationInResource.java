package es.uji.apps.par.config;

import java.io.FileNotFoundException;
import java.io.InputStream;

public class ConfigurationInResource implements ConfigurationInterface {
	@Override
	public InputStream getPathToFile() throws FileNotFoundException {
		InputStream stream = ConfigurationInResource.class.getClassLoader().getResourceAsStream("app-test.properties");
		return stream;
	}
}
