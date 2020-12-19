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
	var secundaria;
	
	var ultimaButacaSeleccionada = null;	// Última butaca seleccionada
	var butacasSeleccionadas = [];
	var butacasDiscapacitados = [];
	var butacasAcompañantes = [];
	
	function init(url, sesId, butacas, uuid, gastosGest, modoReserva, admin, tipoEv, segundaPantalla=null) {
		baseUrl = url;
		sesionId = sesId;
		butacasSeleccionadas = butacas;
		uuidCompra = uuid;
		gastosGestion = gastosGest;
		reserva = modoReserva;
		modoAdmin = admin;
		tipoEvento = tipoEv;
		secundaria = segundaPantalla;
		
		if (modoAdmin)
		{
			$("#imagen_platea1,#imagen_platea2,#imagen_anfiteatro").load(function() {
				//console.log('Imágenes cargadas', document.body.scrollHeight);
				window.scrollTo(0, document.body.scrollHeight);
			});
			setInterval(function(){refrescaImágenes();}, 15000);
		} else {
			actualizaInformacion();
		}
	}
	
	function cargaButacasAccesibles() {
		$.getJSON(baseUrl + '/rest/entrada/' + sesionId + '/accesibles', function(respuesta){
			for (var i=0; i<respuesta.data.length; i++) {
				butacasDiscapacitados.push(respuesta.data[i]);
			}
		});
	}

	function cargaButacasAcompañantes() {
		$.getJSON(baseUrl + '/rest/entrada/' + sesionId + '/acompanantes', function(respuesta){
			for (var i=0; i<respuesta.data.length; i++) {
				butacasAcompañantes.push(respuesta.data[i]);
			}
		});
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

	function esAccesible(butaca) {
		for ( var i = 0; i < butacasDiscapacitados.length; i++)
			if (iguales(butaca, butacasDiscapacitados[i]))
				return true;

		return false;
	}

	function esAcompañante(butaca) {
		for ( var i = 0; i < butacasAcompañantes.length; i++) {
			if (iguales(butaca, butacasAcompañantes[i])) {
				butaca.numero_enlazada = butacasAcompañantes[i].numero_enlazada;
				return true;
			}
		}

		return false;
	}

	function estaSeleccionada(butaca) {
		for ( var i = 0; i < butacasSeleccionadas.length; i++)
			if (iguales(butaca, butacasSeleccionadas[i]))
				return true;
		
		return false;
	}
	
	function imagenButaca(butaca)
	{
		if (esDiscapacitado(butaca)) {
			return "seleccionadaDiscapacitado.png";
		} else if (esAcompañante(butaca)) {
			return "seleccionadaAcompanante.png"
		} else {
			return "seleccionada.png";
		}
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

		if (secundaria !== null) {
			secundaria.seleccionaButaca(butaca);
		}
	}
	
	function eliminaButacaSeleccionada(butaca) {
		if (secundaria !== null) {
			secundaria.deseleccionaButaca(butaca);
		}

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
		
		url = url.replace(/\#.*/, "");
		
		imagen.attr("src", url + "#" + (new Date()).getTime());
	}

	function refrescaImágenes() {
		refrescaImagen("general");
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
	
	function actualizaInformacion() {
		$('.tituloInformacion').text(UI.i18n.informativo.titulo);
		$('.cuerpoInformacion').html(UI.i18n.informativo.cuerpo);
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

		if (estaSeleccionada(butaca)) {
			eliminaButacaSeleccionada(butaca);
		} else {
			if (esAccesible(butaca)) {
				// Butaca accesible
				alert(UI.i18n.butacas.discapacitado);
				anyadeButacaSeleccionada(butaca);
			} else if (esAcompañante(butaca)) {
				// Butaca de acompañante
				if (estáVinculadaASeleccionadas(butaca)) {
					anyadeButacaSeleccionada(butaca);
				} else {
					alert(UI.i18n.butacas.acompañante);
				}
			} else {
				// Resto de butacas
				anyadeButacaSeleccionada(butaca);
			}
			ultimaButacaSeleccionada = butaca;
			compruebaEstadoButacas();
		}
	
		refrescaEstadoButacas();
	}
	
	function esDiscapacitado(butaca)
	{
		if (esAccesible(butaca)) {
			return true;
		}
		return butaca.localizacion.indexOf('discapacitados') == 0;
	}

	function estáVinculadaASeleccionadas(butaca) {
		var asociada = {
				localizacion : butaca.localizacion,
				fila : butaca.fila,
				numero : butaca.numero_enlazada
		};

		return estaSeleccionada(asociada);
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
		compruebaValidezButacas();
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
			if (!esAcompañante(ultimaButacaSeleccionada)) {
				var msj = UI.i18n.butacas.ocupadas;
				alert(msj);
			}
		}
	}

	function validaSuccess(response) {
		// console.log("validez:", response);
		var resultado = response.result;

		if (modoAdmin) {
			activaBotónSiguiente(resultado);
		} else {
			activaBotónComprar(resultado);
		}

		if (!resultado) {
			alert(response.message);
		}
	}

	function activaBotónComprar(activar) {
		var botón = $("#seleccionComprar");

		if (activar) {
			botón.show();
		} else {
			botón.hide();
		}
	}

	function activaBotónSiguiente(activar) {
		if (typeof parent.Ext == "undefined") {
			return;
		}

		var botón = parent.Ext.getCmp("comprarSiguiente");

		if (activar) {
			botón.enable();
		} else {
			botón.disable();
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

	function compruebaValidezButacas() {
		if (butacasSeleccionadas.length == 0) {
			return;
		}

		var path = baseUrl + "/rest/entrada/" + $("input[name=idSesion]").val() + "/valida";

		$.ajax({
			  url: path,
			  type:"POST",
			  data: $.toJSON({uuidCompra:uuidCompra, butacas:butacasSeleccionadas}),
			  contentType: "application/json; charset=utf-8",
			  dataType: "json",
			  success: validaSuccess});
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
		cargaButacasAccesibles:cargaButacasAccesibles,
		cargaButacasAcompañantes:cargaButacasAcompañantes,
		cargaPrecios:cargaPrecios,
		cambiaTipoButaca: cambiaTipoButaca,
		cambiaTipoTodasButacas: cambiaTipoTodasButacas
	};
	
}());