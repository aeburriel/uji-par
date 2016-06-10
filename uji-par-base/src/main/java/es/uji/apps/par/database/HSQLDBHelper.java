package es.uji.apps.par.database;

import java.math.BigDecimal;

public class HSQLDBHelper implements DatabaseHelper {
	@Override
	public String paginate(int start, int limit, String sql) {
		return null;
	}

	@Override
	public Integer castId(Object id) {
		return null;
	}

	@Override
	public Boolean castBoolean(Object value) {
		return null;
	}

	@Override
	public BigDecimal castBigDecimal(Object value) {
		return (value.toString().equals("true")) ? new BigDecimal(1) : new BigDecimal(value.toString());
	}

	@Override
	public String caseString(String condicion, String[] condicionesValores) {
		return null;
	}

	@Override
	public String trueString() {
		return "true";
	}

	@Override
	public String falseString() {
		return "false";
	}

	@Override
	public String trunc(String campo, String formato) {
		return campo;
	}

	@Override
	public int booleanToNumber(Object valor) {
		return 0;
	}

	@Override
	public String toInteger(String columna) {
		return columna;
	}
}
