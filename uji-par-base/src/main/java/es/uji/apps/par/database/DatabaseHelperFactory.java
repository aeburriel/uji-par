package es.uji.apps.par.database;

import es.uji.apps.par.config.Configuration;

public class DatabaseHelperFactory
{
    public static DatabaseHelper newInstance(Configuration configuration)
    {
        if (configuration.getJdbUrl().toUpperCase().contains("ORACLE"))
        {
            return new DatabaseHelperOracle();
        }
        else if (configuration.getJdbUrl().toUpperCase().contains("POSTGRES"))
        {
            return new DatabaseHelperPostgres();
        }
        else if (configuration.getJdbUrl().toUpperCase().contains("HSQLDB")) {
			return new HSQLDBHelper();
		} else
        {
            throw new RuntimeException("No se ha encontrado un DatabaseHelper según la url de conexión JDBC: " + configuration
					.getJdbUrl());
        }
    }
}
