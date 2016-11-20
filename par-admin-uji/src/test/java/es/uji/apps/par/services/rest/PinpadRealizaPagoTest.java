package es.uji.apps.par.services.rest;

import static org.junit.Assert.*;
import static org.mockito.Matchers.*;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.io.IOException;
import java.math.BigDecimal;

import org.junit.Before;
import org.junit.Test;

import es.uji.apps.par.pinpad.ResultadoPagoPinpad;
import es.uji.apps.par.services.Pinpad;
import es.uji.apps.par.services.PinpadDataService;

public class PinpadRealizaPagoTest
{
    private Pinpad pinpad;
    private PinpadDataService pinpadService;

    @Before
    public void before()
    {
        pinpadService = mock(PinpadDataService.class);
        pinpad = new Pinpad(pinpadService);
    }

    @Test
    public void testError()
    {
        when(pinpadService.realizaPago(anyString(), (BigDecimal) anyObject(), anyString(), "")).thenReturn("");

        ResultadoPagoPinpad resultado = pinpad.realizaPago("1", new BigDecimal("1.02"), "Test");

        assertTrue("Error", resultado.getError());
    }
    
    @Test
    public void testNoError()
    {
        when(pinpadService.realizaPago(anyString(), (BigDecimal) anyObject(), anyString(), "")).thenReturn("123756");

        ResultadoPagoPinpad resultado = pinpad.realizaPago("1", new BigDecimal("1.02"), "Test");

        assertFalse("No error", resultado.getError());
    }
    
    @Test
    public void testrecuperaIdResultado()
    {
        when(pinpadService.realizaPago(anyString(), (BigDecimal) anyObject(), anyString(), "")).thenReturn("12345\n");

        ResultadoPagoPinpad resultado = pinpad.realizaPago("1", new BigDecimal("1.02"), "Test");

        assertEquals("Recupera id resultado", "12345", resultado.getCodigo());
    }

    @Test
    public void testExcepcion()
    {
        when(pinpadService.realizaPago(anyString(), (BigDecimal) anyObject(), anyString(), "")).thenThrow(new RuntimeException("Error de conexión"));

        ResultadoPagoPinpad resultado = pinpad.realizaPago("1", new BigDecimal("1.02"), "Test");

        assertTrue("Lanza excepción", resultado.getError());
        assertEquals("Lanza excepción mensaje", "Error de conexión", resultado.getMensajeExcepcion());
    }
}
