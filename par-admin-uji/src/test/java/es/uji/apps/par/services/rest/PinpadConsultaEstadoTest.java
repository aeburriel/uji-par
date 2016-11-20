package es.uji.apps.par.services.rest;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import static org.mockito.Matchers.anyString;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import org.junit.Before;
import org.junit.Test;

import es.uji.apps.par.pinpad.EstadoPinpad;
import es.uji.apps.par.services.Pinpad;
import es.uji.apps.par.services.PinpadDataService;

public class PinpadConsultaEstadoTest
{
    private Pinpad pinpad;
    private PinpadDataService pinpadService;

    @Before
    public void before()
    {
        pinpadService = mock(PinpadDataService.class);
        pinpad = new Pinpad(pinpadService);
    }

	/*
0-<recibo>-Pago correcto
1-<recibo>-Pendiente de confirmación
5--Timeout
6--Recibo no creado
7--Error de firma
8--Error en parámetros
9--Error desconocido
	 */

    @Test
    public void testNoError()
    {
        when(pinpadService.consultaEstado(anyString(), "")).thenReturn("0-<recibo>-Pago correcto");

        EstadoPinpad estado = pinpad.getEstadoPinpad("");

        assertFalse("No error", estado.getError());
		assertEquals("0", estado.getCodigoAccion());
		assertEquals("<recibo>", estado.getRecibo());
		assertTrue(estado.getReady());
    }

	@Test
	public void testPendienteConfirmacion() {
		when(pinpadService.consultaEstado(anyString(), "")).thenReturn("1-<recibo>-Pendiente de confirmación");

		EstadoPinpad estado = pinpad.getEstadoPinpad("");

		assertFalse("No error", estado.getError());
		assertEquals("1", estado.getCodigoAccion());
		assertEquals("<recibo>", estado.getRecibo());
		assertFalse(estado.getReady());
	}

	@Test
	public void testTimeout() {
		when(pinpadService.consultaEstado(anyString(), "")).thenReturn("5--Timeout");

		EstadoPinpad estado = pinpad.getEstadoPinpad("");

		assertTrue("No error", estado.getError());
		assertEquals("5", estado.getCodigoAccion());
		assertEquals("", estado.getRecibo());
		assertFalse(estado.getReady());
	}

    @Test
    public void testMensajeConGuiones()
    {
        when(pinpadService.consultaEstado(anyString(), "")).thenReturn("1-2-Esto es el mensaje - ole");

        EstadoPinpad estado = pinpad.getEstadoPinpad("");

        assertEquals("Mensaje con guiones", "Esto es el mensaje - ole", estado.getMensaje());
    }

	@Test
	public void testReciboNoCreado() {
		when(pinpadService.consultaEstado(anyString(), "")).thenReturn("6--Recibo no creado");

		EstadoPinpad estado = pinpad.getEstadoPinpad("");

		assertFalse("Error", estado.getError());
		assertEquals("6", estado.getCodigoAccion());
		assertEquals("", estado.getRecibo());
		assertFalse(estado.getReady());
	}

	@Test
	public void testErrorFirma() {
		when(pinpadService.consultaEstado(anyString(), "")).thenReturn("7--Error de firma");

		EstadoPinpad estado = pinpad.getEstadoPinpad("");

		assertTrue("Error", estado.getError());
		assertEquals("7", estado.getCodigoAccion());
		assertEquals("", estado.getRecibo());
		assertFalse(estado.getReady());
	}

	@Test
	public void testErrorEnParametros() {
		when(pinpadService.consultaEstado(anyString(), "")).thenReturn("8--Error en parámetros");

		EstadoPinpad estado = pinpad.getEstadoPinpad("");

		assertTrue("Error", estado.getError());
		assertEquals("8", estado.getCodigoAccion());
		assertEquals("", estado.getRecibo());
		assertFalse(estado.getReady());
	}

	@Test
	public void testErrorDesconocido() {
		when(pinpadService.consultaEstado(anyString(), "")).thenReturn("9--Error desconocido");

		EstadoPinpad estado = pinpad.getEstadoPinpad("");

		assertTrue("Error", estado.getError());
		assertEquals("9", estado.getCodigoAccion());
		assertEquals("", estado.getRecibo());
		assertFalse(estado.getReady());
	}

}
