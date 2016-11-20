package es.uji.apps.par.config;

import java.io.FileNotFoundException;
import java.io.InputStream;
import org.springframework.stereotype.Service;

@Service
public interface ConfigurationInterface {
	InputStream getPathToFile() throws FileNotFoundException;
}
