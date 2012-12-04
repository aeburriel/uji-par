package es.uji.apps.par.services.dao;

import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import org.springframework.test.context.transaction.TransactionConfiguration;
import org.springframework.transaction.annotation.Transactional;

import es.uji.apps.par.dao.LocalizacionesDAO;
import es.uji.apps.par.model.ParLocalizacion;

@RunWith(SpringJUnit4ClassRunner.class)
@TransactionConfiguration(transactionManager = "transactionManager", defaultRollback = true)
@ContextConfiguration(locations = { "/applicationContext.xml" })
public class LocalizacionesDAOTest {

	@Autowired
	LocalizacionesDAO localizacionesDAO;
	
	@Test
	@Transactional
	public void getLocalizaciones() {
		Assert.assertNotNull(localizacionesDAO.get());
	}
	
	@Test
	@Transactional
	public void addLocalizacion() {
		ParLocalizacion parLocalizacion = preparaLocalizacion();
		ParLocalizacion localizacion = localizacionesDAO.add(parLocalizacion);
		
		Assert.assertNotNull(localizacion.getId());
	}
	
	private ParLocalizacion preparaLocalizacion() {
		return new ParLocalizacion("Nombre Localizacion");
	}
	
	@Test
	@Transactional
	public void updateLocalizacion() {
		ParLocalizacion localizacion = localizacionesDAO.add(preparaLocalizacion());
		
		Assert.assertNotNull(localizacion.getId());
		
		localizacion.setNombreEs("Prueba2");
		ParLocalizacion localizacionActualizada = localizacionesDAO.update(localizacion);
		Assert.assertEquals(localizacion.getId(), localizacionActualizada.getId());
	}
}