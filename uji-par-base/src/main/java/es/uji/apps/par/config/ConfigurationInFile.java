package es.uji.apps.par.config;

import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Paths;

public class ConfigurationInFile implements ConfigurationInterface {
	@Override
	public InputStream getPathToFile() throws IOException {
		return Files.newInputStream(Paths.get("/etc/uji/par/app.properties"));
	}
}
