package es.uji.apps.par.database;

import java.math.BigDecimal;
import java.math.BigInteger;

public class DatabaseHelperPostgres implements DatabaseHelper
{
    public String paginate(int start, int limit, String sql)
    {
        return " select * from (" + sql + ") as s " +
               " offset " + start + " limit " + limit;
    }
    
    public Integer castId(Object id)
    {
        return (Integer)id;
    }
    
    public Boolean castBoolean(Object value)
    {
        return (Boolean)value;
    }
    
    public BigDecimal castBigDecimal(Object value)
    {
        if (value == null)
            return null;
        else if (value instanceof BigDecimal)
        	return (BigDecimal) value;
        else if (value instanceof Float)
            return new BigDecimal((Float)value);
        else if (value instanceof BigInteger)
            return new BigDecimal((BigInteger)value);
        else if (value instanceof Boolean)
        	return new BigDecimal (((Boolean)value)?1:0);
        else if (value instanceof Integer)
        	return new BigDecimal ((Integer)value);
        else 
            throw new RuntimeException("Unknown cast type for: " + value);
    }

    @Override
    public String caseString(String expresion, String[] condicionesValores)
    {
        StringBuffer buff = new StringBuffer();
        
        buff.append(" CASE ");
        
        for (int i=0; i<condicionesValores.length; i+=2)
        {
            buff.append(" WHEN ");
            buff.append(expresion);
            buff.append("=");
            buff.append(condicionesValores[i]);
            buff.append(" THEN ");
            buff.append(condicionesValores[i+1]);
        }
        
        buff.append(" END ");
        
        return buff.toString();
    }
    
    public String trueString()
    {
        return "true";
    }
    
    public String falseString()
    {
        return "false";
    }
    
    @Override
	public String trunc(String campo, String formato) {
    	if (formato.equals("DD"))
    		formato = "day";
		return "date_trunc('" + formato+ "', " + campo + ")";
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
		return "CAST(coalesce(" + columna + ", '0') AS integer)";
	}

	@Override
	public String toDate() {
		return "TO_TIMESTAMP";
	}

	@Override
	public String isNotEmptyString(String columna)
	{
		return String.format("%s <> ''", columna);
	}
}
