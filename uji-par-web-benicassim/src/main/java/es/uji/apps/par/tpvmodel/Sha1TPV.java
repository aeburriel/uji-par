package es.uji.apps.par.tpvmodel;

import es.uji.apps.par.tpv.SHA1TPVInterface;
import es.uji.apps.par.utils.Utils;
import org.springframework.stereotype.Component;

@Component
public class Sha1TPV implements SHA1TPVInterface {

    public String getFirma(String importe, String orderPrefix, String id, String tpvCode, String tpvCurrency, String tpvTransaction, String url, String secret, String date) {
        return Utils.sha1(importe + orderPrefix + id + tpvCode + tpvCurrency + tpvTransaction + url + secret);
    }
}
