package es.uji.apps.par.tpvmodel;

import es.uji.apps.par.tpv.IdTPVInterface;
import org.springframework.stereotype.Component;

@Component
public class IdTpv implements IdTPVInterface {

    public String getFormattedId(long id) {
        return String.valueOf(id);
    }
}
