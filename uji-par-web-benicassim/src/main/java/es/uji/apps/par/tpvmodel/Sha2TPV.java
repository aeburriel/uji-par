package es.uji.apps.par.tpvmodel;

import com.mysema.commons.lang.Pair;
import es.uji.apps.par.tpv.HmacSha256TPVInterface;
import org.springframework.stereotype.Component;
import sis.redsys.api.ApiMacSha256;

@Component
public class Sha2TPV implements HmacSha256TPVInterface {

    @Override
    public Pair<String, String> getParametrosYFirma(String importe, String order, String tpvCode, String tpvCurrency, String tpvTransaction, String tpvTerminal, String url, String urlOk, String urlKo, String email, String tpvLang, String identificador, String concepto, String tpvNombre, String secret) throws Exception {
        ApiMacSha256 apiMacSha256 = new ApiMacSha256();

        apiMacSha256.setParameter("DS_MERCHANT_AMOUNT", importe);
        apiMacSha256.setParameter("DS_MERCHANT_ORDER", order);
        apiMacSha256.setParameter("DS_MERCHANT_MERCHANTCODE", tpvCode);
        apiMacSha256.setParameter("DS_MERCHANT_CURRENCY", tpvCurrency);
        apiMacSha256.setParameter("DS_MERCHANT_TRANSACTIONTYPE", tpvTransaction);
        apiMacSha256.setParameter("DS_MERCHANT_TERMINAL", tpvTerminal);
        apiMacSha256.setParameter("DS_MERCHANT_MERCHANTURL", url);
        apiMacSha256.setParameter("DS_MERCHANT_URLOK", urlOk);
        apiMacSha256.setParameter("DS_MERCHANT_URLKO", urlKo);
        apiMacSha256.setParameter("DS_MERCHANT_TITULAR", email);
        apiMacSha256.setParameter("DS_MERCHANT_CONSUMERLANGUAGE", tpvLang);
        apiMacSha256.setParameter("DS_MERCHANT_MERCHANTDATA", identificador);
        apiMacSha256.setParameter("DS_MERCHANT_PRODUCTDESCRIPTION", concepto);
        apiMacSha256.setParameter("DS_MERCHANT_MERCHANTNAME", tpvNombre);

        String merchantParameters = apiMacSha256.createMerchantParameters();
        String merchantSignature = apiMacSha256.createMerchantSignature(secret);

        return new Pair<String, String>(merchantParameters, merchantSignature);
    }
}
