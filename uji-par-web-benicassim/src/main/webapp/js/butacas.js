Butacas = (function() {
	
	var baseUrl = "";
	var sesionId;
	var precios = {};
	var uuidCompra;
	var gastosGestion;
	var reserva;
	var modoAdmin;
	var tipoEvento;
	var tarifaDefecto = '';
	var tarifas = {};
	
	var butacasSeleccionadas = [];
	
	function init(url, sesId, butacas, uuid, gastosGest, modoReserva, admin, tipoEv) {
		baseUrl = url;
		sesionId = sesId;
		butacasSeleccionadas = butacas;
		uuidCompra = uuid;
		gastosGestion = gastosGest;
		reserva = modoReserva;
		modoAdmin = admin;
		tipoEvento = tipoEv;
		
		if (modoAdmin)
		{
			$("#imagen_platea1,#imagen_platea2,#imagen_anfiteatro").load(function() {
				//console.log('Imágenes cargadas', document.body.scrollHeight);
				window.scrollTo(0, document.body.scrollHeight);
			});
		}	
	}
	
	function cargaPrecios(callback) {
		$.getJSON(baseUrl + '/rest/entrada/' + sesionId + '/precios', function(respuesta){
			//console.log("RESPUESTA");
			//console.log(respuesta);
			for (var i=0; i<respuesta.data.length; i++)
			{
				var sesion = respuesta.data[i];
				var idTarifa = sesion.tarifa.id;
				
				if (modoAdmin) {
					console.log("ES MODO ADMIN");
					tarifas[sesion.tarifa.id] = sesion.tarifa.nombre;
				}
				else {
					console.log("NO ES MODO ADMIN", sesion.tarifa);
					if (sesion.tarifa.isPublico == 'on')
						tarifas[sesion.tarifa.id] = sesion.tarifa.nombre;
				}
					
				
				
				if (precios[sesion.localizacion.codigo] == undefined)
					precios[sesion.localizacion.codigo]= {};
				
				if (sesion.tarifa.defecto)
					tarifaDefecto = sesion.tarifa.id;
				precios[sesion.localizacion.codigo][idTarifa] = sesion.precio;
			}
			
			refrescaEstadoButacas();
			compruebaEstadoButacas();
			
			callback(precios);
		});
	}
	
	
	function ocultaButacasSeleccionadas() {
		$('.butaca-seleccionada').remove();
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
	
	function imagenButaca(butaca)
	{
		if (esDiscapacitado(butaca.localizacion))
			return "seleccionadaDiscapacitado.png";
		else
			return "seleccionada.png";
	}
	
	function muestraButacaSeleccionada(butaca) {
		var butacaSeleccionada = $('<img src="' + baseUrl + '/img/' + imagenButaca(butaca) + '" class="butaca-seleccionada"/>');
		butacaSeleccionada.css("left", butaca.x + "px");
		butacaSeleccionada.css("top", butaca.y + "px");
		
		//console.log(butacaSeleccionada);
		
		var idDiv = idDivLocalizacion(butaca.localizacion);
		$('.localizacion_' + idDiv).append(butacaSeleccionada);
	
		butacaSeleccionada.click(function() {
			selecciona(butaca.localizacion, butaca.texto, butaca.fila, butaca.numero,
					butaca.x, butaca.y);
		});
		butacaSeleccionada.show();
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
		for ( var i = 0; i < butacasSeleccionadas.length; i++) {
			if (iguales(butaca, butacasSeleccionadas[i])) {
				butacasSeleccionadas.splice(i, 1);
				return;
			}
		}
	}
	
	function refrescaImagen(localizacion)
	{
		var imagen = $(".imagen_" + idDivLocalizacion(localizacion));
		var url = imagen.attr("src");
		
		if (url.indexOf("?") == -1)
			url += "?";
		
		url = url.replace(/\&rnd=.*/, "");
		
		imagen.attr("src", url + "&rnd=" + (new Date()).getTime());
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
			$('#totalSeleccionadas').text(UI.i18n.butacas.totalEntradas + total.toFixed(2) + ' €');
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
		if (precios[localizacion] == undefined || precios[localizacion][tarifaDefecto] == undefined) {
			var msg = UI.i18n.error.preuNoIntroduit;
			alert(msg);
			return;
		}
		
		var butaca = {
			localizacion : localizacion,
			fila : fila,
			numero : numero,
			x : x,
			y : y,
			tipo : tarifaDefecto,
			precio: precios[localizacion][tarifaDefecto],
			texto: texto
		};
	
		if (estaSeleccionada(butaca))
			eliminaButacaSeleccionada(butaca);
		else {
			anyadeButacaSeleccionada(butaca);
			compruebaEstadoButacas();
		}
	
		refrescaEstadoButacas();
	}
	
	function esDiscapacitado(localizacion)
	{
		return localizacion.indexOf('discapacitados') == 0;
	}
	
	function idDivLocalizacion(localizacion)
	{
		if (localizacion == 'discapacitados1')
			return 'platea1';
		else if (localizacion == 'discapacitados2')
			return 'platea2';
		else if (localizacion == 'discapacitados3')
			return 'anfiteatro';
		else 
			return localizacion;
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
		
		// console.log("ocupadas:", ocupadas);
		if (ocupadas.length > 0) {
			
			for (var i=0; i<ocupadas.length; i++)
			{
				eliminaButacaSeleccionada(ocupadas[i]);
				refrescaImagen(ocupadas[i].localizacion);
			}
			
			refrescaEstadoButacas();
			
			/* [[#{butacasOcupadas}]] */
			var msj = UI.i18n.butacas.ocupadas; 
			alert(msj);
		}
	}
	
	function compruebaEstadoButacas() {
		
		var path = baseUrl + "/rest/entrada/" + $("input[name=idSesion]").val() + "/ocupadas";
		
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
	
		// console.log('Precio:', value);
	
		if (value == 'normal') {
			$('.precio-normal').show();
			$('.precio-descuento').hide();
		} else {
			$('.precio-descuento').show();
			$('.precio-normal').hide();
		}
	}
	
	function muestraLocalizacion() {
		var localizacion = $('#localizacion').val();
		
		$('div[id^=localizacion_]').hide();
		
		if (localizacion == 'platea')
		{
			$('#localizacion_platea1').show();
			$('#localizacion_platea2').show();
		}
		else
		{
			$('#localizacion_anfiteatro').show();
		}
	}
	
	$(document).ready(function() {
		$('input[name=tipo]').click(function() {
			muestraPrecios();
		});
		
		$('#localizacion').change(function() {
			muestraLocalizacion();
		});
		
		$('#limpiarSeleccion').click(function(){
			limpiaSeleccion();
		});

		muestraPrecios();
		//muestraLocalizacion();
	});
	
	// Desde fuera del iframe nos han pedido que le pasemos las butacas seleccionadas 
	pm.bind("butacas", function(data){
		 return butacasSeleccionadas;
	});
	
	return {
		selecciona:selecciona,
		init:init,
		cargaPrecios:cargaPrecios,
		cambiaTipoButaca: cambiaTipoButaca,
		cambiaTipoTodasButacas: cambiaTipoTodasButacas
	};
	
}());