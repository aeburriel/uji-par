package es.uji.apps.par.database;

import org.apache.commons.lang.StringUtils;

import java.math.BigDecimal;

public class DatabaseHelperOracle implements DatabaseHelper
{
    public String paginate(int start, int limit, String sql)
    {
        return " select * from " +
        "    (select a.*, rownum as rn from (" + sql + ") a " +
        "    where rownum <= " + (start+limit) + ") " +
        " where rn>=" + (start+1);   
    }
    
    public Integer castId(Object id)
    {
        return ((BigDecimal)id).intValue();
    }
    
    public Boolean castBoolean(Object value)
    {
        return ((BigDecimal)value).intValue() == 1;
    }
    
    public BigDecimal castBigDecimal(Object value)
    {
        return (BigDecimal)value;
    }
    
    public String caseString(String expresion, String[] condicionesValores)
    {
        StringBuffer buff = new StringBuffer();
        
        buff.append(" decode(");
        buff.append(expresion);
        buff.append(", ");
        
        buff.append(StringUtils.join(condicionesValores, ", "));
        
        buff.append(") ");
        
        return buff.toString();
    }

    @Override
    public String trueString()
    {
        return "1";
    }

    @Override
    public String falseString()
    {
        return "0";
    }

	@Override
	public String trunc(String campo, String formato) {
		return "TRUNC(" + campo + ", '" + formato + "')";
	}
	
	@Override
	public int booleanToNumber(Object valor) {
		if (valor == null)
			return 0;
		if (valor instanceof Boolean) {
			if (((Boolean) valor))
				return 1;
			else
				return 0;
		}

		return (Integer) valor;
	}
	
	public String toInteger(String columna) {
		return "TO_NUMBER(" + columna + ")";
	}

	@Override
	public String toDate() {
		return "TO_DATE";
	}

	@Override
	public String isEmptyString(String columna)
	{
		return String.format("LENGTH(%s) > 0", columna);
	}
}
