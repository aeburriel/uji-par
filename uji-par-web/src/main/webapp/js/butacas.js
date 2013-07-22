Butacas = (function() {
	
	var baseUrl = "";
	
	var butacasSeleccionadas = [];
	
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
	
	function muestraButacaSeleccionada(butaca) {
		var butacaSeleccionada = $('<img src="' + baseUrl + '/img/seleccionada.png" class="butaca-seleccionada"/>');
		butacaSeleccionada.css("left", butaca.x + "px");
		butacaSeleccionada.css("top", butaca.y + "px");
		$('#localizacion_' + butaca.localizacion).append(butacaSeleccionada);
	
		butacaSeleccionada.click(function() {
			// console.log("Click sobre seleccionada: ", butaca);
			selecciona(butaca.localizacion, butaca.fila, butaca.numero,
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
		var imagen = $("#imagen_" + localizacion);
		
		var url = imagen.attr("src").replace(/\?.*/, "");
		
		imagen.attr("src", url + "?" + (new Date()).getTime());
	}
	
	function muestraDetallesSeleccionadas() {
		var detalles = "";
	
		$('#detallesSeleccionadas').empty();
	
		for ( var i = 0; i < butacasSeleccionadas.length; i++) {
			var fila = $('<div class="entrada-seleccionada">'
					+ '<span>' + UI.i18n.butacas.localizacion + '=</span>'
					+ butacasSeleccionadas[i].localizacion
					+ ', <span>' + UI.i18n.butacas.fila + '=</span>'
					+ butacasSeleccionadas[i].fila
					+ ', <span>' + UI.i18n.butacas.butaca + '=</span>'
					+ butacasSeleccionadas[i].numero
					+ ', <span>' + UI.i18n.butacas.tipo + '=</span>'
					+ butacasSeleccionadas[i].tipo + ', ' + "</div>");
			$('#detallesSeleccionadas').append(fila);
		}
	}
	
	function selecciona(localizacion, fila, numero, x, y) {
		var tipoEntrada = $('input[name=tipo]:checked').val();
		var butaca = {
			localizacion : localizacion,
			fila : fila,
			numero : numero,
			x : x,
			y : y,
			tipo : tipoEntrada
		};
	
		if (estaSeleccionada(butaca)) {
			// console.log('Ya está seleccionada:', butaca);
			eliminaButacaSeleccionada(butaca);
		} else {
			// console.log('Añade a seleccionadas:', butaca);
			anyadeButacaSeleccionada(butaca);
			compruebaEstadoButaca(butaca);
		}
	
		refrescaEstadoButacas();
	}
	
	function refrescaEstadoButacas()
	{
		redibujaButacasSeleccionadas();
		muestraDetallesSeleccionadas();
		guardaButacasEnHidden();
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
	
	function compruebaEstadoButaca(butaca) {
		
		var path = baseUrl + "/rest/entrada/" + $("input[name=idSesion]").val() + "/ocupadas";
		
		$.ajax({
			  url: path,
			  type:"POST",
			  data: $.toJSON(butacasSeleccionadas),
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
		$('#localizacion_' + localizacion).show();
	}
	
	$(document).ready(function() {
		$('input[name=tipo]').click(function() {
			muestraPrecios();
		});
		
		$('#localizacion').change(function() {
			muestraLocalizacion();
		});

		muestraPrecios();
		muestraLocalizacion();
	});
	
	function setBaseUrl(url) {
		baseUrl = url;
	}
	
	return {
		selecciona:selecciona,
		setBaseUrl:setBaseUrl
	};
}());