$(document).ready(function() {
	pm.bind("imprimir", function (dataParams) {
		window.open('/par/rest/compra/' + dataParams.uuid + '/pdftaquilla', '_blank');
	});

	pm.bind("anula", function (dataParams) {
		return $.ajax({
			url: "/par/rest/compra/" + dataParams.sesionId + '/' + dataParams.compraId + '/' + (dataParams.butacaId ? dataParams.butacaId : ''),
			type: "PUT",
			contentType: "application/json; charset=utf-8"
		});
	});

	pm.bind("pasarACompra", function (dataParams) {
		return $.ajax({
			url: "/par/rest/compra/" + dataParams.sesionId + '/passaracompra/' + dataParams.compraId + '?tipopago=' + dataParams.tipoPago + '&recibo=' + dataParams.recibo,
			type: "PUT",
			contentType: "application/json; charset=utf-8"
		});
	});

	pm.bind("pasarButacaACompra", function (dataParams) {
		return $.ajax({
			url: "/par/rest/compra/" + dataParams.sesionId + '/butacapassaracompra/' + dataParams.compraId + '?tipopago=' + dataParams.tipoPago + '&recibo=' + dataParams.recibo,
			type: "PUT",
			data: JSON.stringify([dataParams.butacaId]),
			contentType: "application/json; charset=utf-8"
		});
	});
});