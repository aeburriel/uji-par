package es.uji.apps.par.dao;

import java.sql.Timestamp;
import java.util.List;

import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;

import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import com.mysema.query.jpa.impl.JPAQuery;

import es.uji.apps.par.db.MailDTO;
import es.uji.apps.par.db.QMailDTO;
import es.uji.apps.par.utils.DateUtils;

@Repository
public class MailDAO
{
    @PersistenceContext
    private EntityManager entityManager;

    private QMailDTO qMailDTO = QMailDTO.mailDTO;

    @Transactional
    public void insertaMail(String de, String para, String titulo, String texto, String uuid)
    {
        MailDTO mailDTO = new MailDTO();

        mailDTO.setDe(de);
        mailDTO.setPara(para);
        mailDTO.setTitulo(titulo);
        mailDTO.setTexto(texto);
        mailDTO.setFechaCreado(new Timestamp(DateUtils.getCurrentDate().getTime()));
		mailDTO.setUuid(uuid);
        entityManager.persist(mailDTO);
    }

    public List<MailDTO> getMailsPendientes()
    {
        JPAQuery query = new JPAQuery(entityManager);

        return query.from(qMailDTO).where(qMailDTO.fechaEnviado.isNull()).list(qMailDTO);
    }

    @Transactional
    public void marcaEnviado(long id)
    {
        MailDTO mail = getMailById(id);

        mail.setFechaEnviado(new Timestamp(DateUtils.getCurrentDate().getTime()));

        entityManager.persist(mail);
    }

    public MailDTO getMailById(long mailId)
    {
        return entityManager.find(MailDTO.class, mailId);
    }
}
