Butacas = (function() {
	
	var baseUrl = "";
	var abonoId;
	var precios = {};
	var uuidCompra;
	var gastosGestion;
	var reserva;
	var modoAdmin;
	var tarifaDefecto = '';
	var tarifas = {};
	
	var butacasSeleccionadas = [];
	
	function init(url, abId, butacas, uuid, gastosGest, modoReserva, admin) {
		baseUrl = url;
		abonoId = abId;
		butacasSeleccionadas = butacas;
		uuidCompra = uuid;
		gastosGestion = gastosGest;
		reserva = modoReserva;
		modoAdmin = admin;
		
		if (modoAdmin)
		{
			$("#imagen_platea1,#imagen_platea2,#imagen_anfiteatro").load(function() {
				window.scrollTo(0, document.body.scrollHeight);
			});
		}	
	}
	
	function cargaPrecios(callback) {
		$.getJSON('/par/rest/entrada/' + abonoId + '/precios', function(respuesta){
			for (var i=0; i<respuesta.length; i++)
			{
				var sesion = respuesta[i];
				var idTarifa = sesion.tarifa.id;

                if (modoAdmin || sesion.tarifa.isPublico == 'on') {
                    if (modoAdmin) {
                        console.log("ES MODO ADMIN");
                    }
                    else {
                        console.log("NO ES MODO ADMIN", sesion.tarifa);
                    }
                    tarifas[sesion.tarifa.id] = sesion.tarifa.nombre;

                    if (precios[sesion.localizacion.codigo] == undefined)
                        precios[sesion.localizacion.codigo] = {};

                    if (sesion.tarifa.defecto)
                        tarifaDefecto = sesion.tarifa.id;
                    precios[sesion.localizacion.codigo][idTarifa] = sesion.precio;
                }
			}
			
			refrescaEstadoButacas();
			compruebaEstadoButacas();
			
			callback(precios);
		});
	}
	
	
	function ocultaButacasSeleccionadas() {
		$('.mapaSeleccionada').addClass('mapaLibre');
		$('.mapaSeleccionada').removeClass('mapaSeleccionada');
	}
	
	function iguales(butaca1, butaca2) {
		return butaca1.localizacion == butaca2.localizacion
				&& butaca1.fila == butaca2.fila
				&& butaca1.numero == butaca2.numero;
	}
	
	function estaSeleccionada(butaca) {
		for ( var i = 0; i < butacasSeleccionadas.length; i++)
			if (iguales(butaca, butacasSeleccionadas[i]))
				return true;
		
		return false;
	}
	
	function getIdButaca(butaca) {
		return butaca.localizacion + '-' + butaca.fila + '-' + butaca.numero;
	}

	function muestraButacaSeleccionada(butaca) {
		var id = getIdButaca(butaca);
		if (!$('#' + id).hasClass('mapaSeleccionada')) {
			$('#' + id).removeClass('mapaLibre');
			$('#' + id).addClass('mapaSeleccionada');
			$('#' + id + '-mini').removeClass('mapaLibre');
			$('#' + id + '-mini').addClass('mapaSeleccionada');
		}
	}
	
	function muestraButacasSeleccionadas() {
		for ( var i = 0; i < butacasSeleccionadas.length; i++) {
			muestraButacaSeleccionada(butacasSeleccionadas[i]);
		}
	}
	
	function redibujaButacasSeleccionadas() {
		ocultaButacasSeleccionadas();
		muestraButacasSeleccionadas();
	}
	
	function anyadeButacaSeleccionada(butaca) {
		butacasSeleccionadas.push(butaca);
	}
	
	function eliminaButacaSeleccionada(butaca) {
		if ($('#' + getIdButaca(butaca)).hasClass('mapaSeleccionada')) {
		    $('#' + getIdButaca(butaca)).removeClass('mapaSeleccionada');
		    $('#' + getIdButaca(butaca)).addClass('mapaLibre');
			$('#' + getIdButaca(butaca) + '-mini').removeClass('mapaSeleccionada');
			$('#' + getIdButaca(butaca) + '-mini').addClass('mapaLibre');
		}	    
		for ( var i = 0; i < butacasSeleccionadas.length; i++) {
			if (iguales(butaca, butacasSeleccionadas[i])) {
				butacasSeleccionadas.splice(i, 1);
				return;
			}
		}
	}
	
	function muestraDetallesSeleccionadas() {
		$('#detallesSeleccionadas').empty();
	
		for ( var i = 0; i < butacasSeleccionadas.length; i++) {
			var fila = $('<div class="entrada-seleccionada">'
					+ butacasSeleccionadas[i].texto.toUpperCase()
					+ '<br><span>' + UI.i18n.butacas.filaEntero + '</span>'
					+ butacasSeleccionadas[i].fila
					+ ', <span>' + UI.i18n.butacas.butacaEntero + '</span>'
					+ butacasSeleccionadas[i].numero + '<br>'
					+ getSelectTipoButaca(i)
					+ '<span>' + butacasSeleccionadas[i].precio.toFixed(2) + ' €</span></div>');
			$('#detallesSeleccionadas').append(fila);
			var combo = $('#detallesSeleccionadas select:last');
			combo.val(butacasSeleccionadas[i].tipo);
		}
	}
	
	function getSelectTipoButaca(posicion)
	{
		var st = '<select style="width:130px !important" onchange="Butacas.cambiaTipoButaca(' + posicion + ', this.value)">';
		//console.log("BUTACAS SELECCIONADAS", butacasSeleccionadas, tarifas);
		if (tarifas != undefined) {
			for (var key in tarifas) {
				st += '<option value="' + key + '">' + tarifas[key];
			}
		}
		st += '</select>';
		
		return st;
	}
	
	function actualizaTotal()
	{
		if (reserva)
		{
			$('#totalEntradas').text(butacasSeleccionadas.length);
			$('#totalSeleccionadas').text('RESERVA');
		}
		else
		{
			var total = 0;
			
			for (var i=0; i<butacasSeleccionadas.length; i++)
			{
				total += butacasSeleccionadas[i].precio;
			}
			
			if (total > 0)
			{
				total += gastosGestion;
			}	
			
			$('#totalEntradas').text(butacasSeleccionadas.length);
			$('#totalSeleccionadas').text(total.toFixed(2) + ' €');
		}	
	}
	
	function cambiaTipoButaca(posicion, tipo)
	{
		var butaca = butacasSeleccionadas[posicion];
		
		if (precios[butaca.localizacion][tipo] != undefined) {
			butaca.tipo = tipo;
			butaca.precio = precios[butaca.localizacion][tipo];
		
			refrescaEstadoButacas();
		} else
			alert(UI.i18n.error.tarifaNoDisponible1 + "B" + butaca.numero + " F" + butaca.fila + UI.i18n.error.tarifaNoDisponible2);
	}
	
	function cambiaTipoTodasButacas(tipo)
	{
		if (tipo)
		{
			for (var i=0; i<butacasSeleccionadas.length; i++)
				cambiaTipoButaca(i, tipo);
		}
	}
	
	function selecciona(localizacion, texto, fila, numero, x, y) {	
		var butaca = {
			localizacion : localizacion,
			fila : fila,
			numero : numero,
			x : x,
			y : y,
			tipo : tarifaDefecto,
			texto: texto
		};
	
		if (precios[localizacion] == undefined || precios[localizacion][tarifaDefecto] == undefined) {
			var msg = UI.i18n.error.preuNoIntroduit;
			alert(msg);
			eliminaButacaSeleccionada(butaca);
			return;
		}
		
		butaca.precio = precios[localizacion][tarifaDefecto];

		if (estaSeleccionada(butaca))
			eliminaButacaSeleccionada(butaca);
		else {
			anyadeButacaSeleccionada(butaca);
			compruebaEstadoButacas();
		}
	
		refrescaEstadoButacas();
	}
	
	function refrescaEstadoButacas()
	{
		muestraBotonLimpiarSeleccion();
		redibujaButacasSeleccionadas();
		muestraDetallesSeleccionadas();
		guardaButacasEnHidden();
		actualizaTotal();
	}
	
	function muestraBotonLimpiarSeleccion()
	{
		var boton = $('#limpiarSeleccion');
		
		if (butacasSeleccionadas.length == 0)
			boton.hide();
		else
			boton.show();
	}
	
	function limpiaSeleccion()
	{
		butacasSeleccionadas = [];
		refrescaEstadoButacas();
	}
	
	function guardaButacasEnHidden()
	{
		$('input[name=butacasSeleccionadas]').val($.toJSON(butacasSeleccionadas));
	}
	
	function ocupadasSuccess(ocupadas) {
		if (ocupadas.length > 0) {
			
			for (var i=0; i<ocupadas.length; i++)
			{
				eliminaButacaSeleccionada(ocupadas[i]);
			}
			
			refrescaEstadoButacas();
			
			var msj = UI.i18n.butacas.ocupadas; 
			alert(msj);
		}
	}
	
	function compruebaEstadoButacas() {
		
		var path = "/par/rest/entrada/" + $("input[name=abonoId]").val() + "/ocupadas";
		
		$.ajax({
			  url: path,
			  type:"POST",
			  data: $.toJSON({uuidCompra:uuidCompra, butacas:butacasSeleccionadas}),
			  contentType: "application/json; charset=utf-8",
			  dataType: "json",
			  success: ocupadasSuccess});
	}
	
	function muestraPrecios() {
		var value = $('input[name=tipo]:checked').val();
	
		if (value == 'normal') {
			$('.precio-normal').show();
			$('.precio-descuento').hide();
		} else {
			$('.precio-descuento').show();
			$('.precio-normal').hide();
		}
	}
	
	function muestraLocalizacion(localizacion) {
		$('div[id^=localizacion_]').hide();
		$('div[class=miniarea]').hide();
		$('div[id^=' + localizacion + ']').show();
	}
		
	function muestraMinimapa() {
		$('div[id^=localizacion_]').hide();
		
		$('div[class=miniarea]').show();
	}
	
	$(document).ready(function() {
		$('input[name=tipo]').click(function() {
			muestraPrecios();
		});
		
		$('#limpiarSeleccion').click(function(){
			limpiaSeleccion();
		});

		muestraPrecios();
	});
	
	pm.bind("butacas", function(data){
		 return butacasSeleccionadas;
	});
	
	return {
		selecciona:selecciona,
		init:init,
		cargaPrecios:cargaPrecios,
		cambiaTipoButaca: cambiaTipoButaca,
		cambiaTipoTodasButacas: cambiaTipoTodasButacas,
		compruebaEstadoButacas: compruebaEstadoButacas,
		muestraLocalizacion: muestraLocalizacion,
		muestraMinimapa: muestraMinimapa
	};
	
}());