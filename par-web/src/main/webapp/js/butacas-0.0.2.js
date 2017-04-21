Butacas = (function () {

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
    var butacasCompra = [];
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

        if (modoAdmin) {
            $("#imagen_platea1,#imagen_platea2,#imagen_anfiteatro").load(function () {
                window.scrollTo(0, document.body.scrollHeight);
            });

            $('body').on('DOMNodeRemoved', function (e) {
                if (e && e.target && e.target.className && e.target.className == "mcTooltipInner") {
                    if (e.target.parentNode.id == "mcTooltip" && e.target.parentNode.childElementCount == 1) {
                        $(".accionCompra").removeClass("accionCompra");
                    }
                }
            });
        }
    }

    function cargaPrecios(callback) {
        $.getJSON(baseUrl + '/rest/entrada/' + sesionId + '/precios', function (respuesta) {
            for (var i = 0; i < respuesta.data.length; i++) {
                var sesion = respuesta.data[i];
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
        for (var i = 0; i < butacasSeleccionadas.length; i++)
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
        for (var i = 0; i < butacasSeleccionadas.length; i++) {
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
        for (var i = 0; i < butacasSeleccionadas.length; i++) {
            if (iguales(butaca, butacasSeleccionadas[i])) {
                butacasSeleccionadas.splice(i, 1);
                return;
            }
        }
    }

    function muestraDetallesSeleccionadas() {
        $('#detallesSeleccionadas').empty();

        for (var i = 0; i < butacasSeleccionadas.length; i++) {
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

    function getSelectTipoButaca(posicion) {
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

    function actualizaTotal() {
        if (reserva) {
            $('#totalEntradas').text(butacasSeleccionadas.length);
            $('#totalSeleccionadas').text('RESERVA');
        }
        else {
            var total = 0;

            for (var i = 0; i < butacasSeleccionadas.length; i++) {
                total += butacasSeleccionadas[i].precio;
            }

            if (total > 0) {
                total += gastosGestion;
            }

            $('#totalEntradas').text(butacasSeleccionadas.length);
            $('#totalSeleccionadas').text(total.toFixed(2) + ' €');
        }
    }

    function cambiaTipoButaca(posicion, tipo) {
        var butaca = butacasSeleccionadas[posicion];

        if (precios[butaca.localizacion][tipo] != undefined) {
            butaca.tipo = tipo;
            butaca.precio = precios[butaca.localizacion][tipo];

            refrescaEstadoButacas();
        } else
            alert(UI.i18n.error.tarifaNoDisponible1 + "B" + butaca.numero + " F" + butaca.fila + UI.i18n.error.tarifaNoDisponible2);
    }

    function cambiaTipoTodasButacas(tipo) {
        if (tipo) {
            for (var i = 0; i < butacasSeleccionadas.length; i++)
                cambiaTipoButaca(i, tipo);
        }
    }

    function selecciona(localizacion, texto, fila, numero, x, y) {
        var butaca = {
            localizacion: localizacion,
            fila: fila,
            numero: numero,
            x: x,
            y: y,
            tipo: tarifaDefecto,
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
            if (modoAdmin)
                compruebaEstadoButacaSeleccionada(butaca);
            else
                compruebaEstadoButacas();
        }

        refrescaEstadoButacas();
    }

    function compruebaEstadoButacaSeleccionada(butaca) {
        var sesion = $("input[name=idSesion]").val();
        var path = baseUrl + "/rest/entrada/" + sesion + "/ocupadas";

        $.ajax({
            url: path,
            type: "POST",
            data: $.toJSON({uuidCompra: uuidCompra, butacas: butacasSeleccionadas}),
            contentType: "application/json; charset=utf-8",
            dataType: "json",
            success: function (ocupadas) {
                if (ocupadas.length > 0) {
                    var found = false;
                    for (var i = 0; i < ocupadas.length; i++) {
                        eliminaButacaSeleccionada(ocupadas[i]);
                        if (iguales(butaca, ocupadas[i])) {
                            found = true;
                        }
                    }

                    refrescaEstadoButacas();

                    if (found) {
                        tooltip.hide();
                        tooltip.ajax(
                            butaca.localizacion + "-" + butaca.fila + "-" + butaca.numero,
                            baseUrl + "/rest/entrada/" + sesion + '/compra/' + butaca.fila + '/' + butaca.numero + '/' + butaca.localizacion,
                            {
                                responseType: 'json',
                                context: {butaca: butaca},
                                success: respuestaDatosCompra
                            }
                        );
                    }
                }
            }
        });
    }

    function respuestaDatosCompra(data, context) {
        var butacaId;
        var precioCompra = 0;
        var precioButaca = 0;
        if (data != null) {
            for (var i = 0; i < data.parButacas.length; i++) {
                if (!data.parButacas[i].anulada) {
                    precioCompra += data.parButacas[i].precio;
                    $("#" + getIdButaca(data.parButacas[i])).addClass("accionCompra");
                    if (iguales(context.butaca, data.parButacas[i])) {
                        butacaId = data.parButacas[i].id;
                        precioButaca = data.parButacas[i].precio;
                    }
                }
            }

            butacasCompra = data.parButacas;
            var tooltip = $(
                "<div><div>" +  UI.i18n.butacas.precioButaca + " " + precioButaca + " €</div>" +
                "<div>" +  UI.i18n.butacas.precioCompra + " " + precioCompra + " €</div></div>"
            );

            if (data.observacionesReserva) {
                tooltip.append("<div>" + UI.i18n.butacas.observaciones + " <p> " + data.observacionesReserva + "</p></div>");
            }

            tooltip.append($('<button class="button-tooltip-primary" onclick="javascript:Butacas.anula(' + data.id + ');">' + UI.i18n.acciones.anulaCompra + '</button>'))
                .append("<div />")
                .append($('<button class="button-tooltip-primary" onclick="javascript:Butacas.anula(' + data.id + ', ' + butacaId + ', \'' + getIdButaca(context.butaca) + '\');">' + UI.i18n.acciones.anulaButaca + '</button>'));
            if (data.reserva) {
                tooltip
                    .append("<div />").append($('<button class="button-tooltip-primary" onclick="javascript:Butacas.pasarACompra(' + data.id + ');">' + UI.i18n.acciones.pasarACompra + '</button>'))
                    .append("<div />")
                    .append("<div />").append($('<button class="button-tooltip-primary" onclick="javascript:Butacas.pasarButacaACompra(' + data.id + ', ' + butacaId + ', \'' + getIdButaca(context.butaca) + '\');">' + UI.i18n.acciones.pasarButacaACompra + '</button>'));
            }
            else {
                tooltip
                    .append("<div />")
                    .append($('<button class="button-tooltip-primary" onclick="javascript:Butacas.imprimir(\'' + data.uuid + '\');">' + UI.i18n.acciones.imprimir + '</button>'))
            }

            return tooltip.html();
        }
    }

    function imprimir(uuid) {
        pm({
            target: window.parent,
            type: "imprimir",
            data: {uuid: uuid},
            success: function (data) {
                tooltip.hide();
            }
        });
    }

    function anula(compraId, butacaId, butacaSelectorId) {
        var respuesta = false;
        if (butacaId) {
            respuesta = confirm(UI.i18n.mensajes.anularButaca);
        }
        else {
            respuesta = confirm(UI.i18n.mensajes.anularCompra);
        }

        if (respuesta) {
            var sesionId = $("input[name=idSesion]").val();

            var params = {compraId: compraId, sesionId: sesionId};
            if (butacaId) {
                params = {compraId: compraId, sesionId: sesionId, butacaId: butacaId};
            }

            pm({
                target: window.parent,
                type: "anula",
                data: params,
                success: function (data) {
                    tooltip.hide();
                    if (butacaId) {
                        $("#" + butacaSelectorId).removeClass("mapaReservada").removeClass("mapaOcupada").addClass("mapaLibre");
                        $("#" + butacaSelectorId + '-mini').removeClass("mapaReservada").removeClass("mapaOcupada").addClass("mapaLibre");
                    }
                    else {
                        for (var i = 0; i < butacasCompra.length; i++) {
                            $("#" + getIdButaca(butacasCompra[i])).removeClass("mapaReservada").removeClass("mapaOcupada").addClass("mapaLibre");
                            $("#" + getIdButaca(butacasCompra[i]) + '-mini').removeClass("mapaReservada").removeClass("mapaOcupada").addClass("mapaLibre");
                        }
                    }
                    refrescaEstadoButacas();
                }
            });
        }
    }

    function pasarACompra(compraId) {
        var respuesta =  confirm(UI.i18n.mensajes.pasarACompra);

        if (respuesta) {
            $(document).on('confirmation', '.remodal', function () {
                var pago = $('#pago');
                var codigo = $('#codigo');
                var sesionId = $("input[name=idSesion]").val();

                pm({
                    target: window.parent,
                    type: "pasarACompra",
                    data: {compraId: compraId, sesionId: sesionId, tipoPago: pago.val(), recibo: codigo.val()},
                    success: function (data) {
                        tooltip.hide();
                        for (var i = 0; i < butacasCompra.length; i++) {
                            $("#" + getIdButaca(butacasCompra[i])).removeClass("mapaReservada").addClass("mapaOcupada");
                            $("#" + getIdButaca(butacasCompra[i]) + '-mini').removeClass("mapaReservada").addClass("mapaOcupada");
                        }
                        refrescaEstadoButacas();
                    }
                });
            });

            window.location.href = "#modal";
        }
    }

    function pasarButacaACompra(compraId, butacaId, butacaSelectorId) {
        var respuesta =  confirm(UI.i18n.mensajes.pasarButacaACompra);

        if (respuesta) {
            $(document).on('confirmation', '.remodal', function () {
                var pago = $('#pago');
                var codigo = $('#codigo');
                var sesionId = $("input[name=idSesion]").val();

                pm({
                    target: window.parent,
                    type: "pasarButacaACompra",
                    data: {compraId: compraId, sesionId: sesionId, tipoPago: pago.val(), recibo: codigo.val(), butacaId: butacaId},
                    success: function (data) {
                        tooltip.hide();
                        $("#" + butacaSelectorId).removeClass("mapaReservada").addClass("mapaOcupada");
                        $("#" + butacaSelectorId + '-mini').removeClass("mapaReservada").addClass("mapaOcupada");
                        refrescaEstadoButacas();
                    }
                });
            });

            window.location.href = "#modal";
        }
    }

    function refrescaEstadoButacas() {
        muestraBotonLimpiarSeleccion();
        redibujaButacasSeleccionadas();
        muestraDetallesSeleccionadas();
        guardaButacasEnHidden();
        actualizaTotal();
    }

    function muestraBotonLimpiarSeleccion() {
        var boton = $('#limpiarSeleccion');

        if (butacasSeleccionadas.length == 0)
            boton.hide();
        else
            boton.show();
    }

    function limpiaSeleccion() {
        butacasSeleccionadas = [];
        refrescaEstadoButacas();
    }

    function guardaButacasEnHidden() {
        $('input[name=butacasSeleccionadas]').val($.toJSON(butacasSeleccionadas));
    }

    function ocupadasSuccess(ocupadas) {
        if (ocupadas.length > 0) {

            for (var i = 0; i < ocupadas.length; i++) {
                eliminaButacaSeleccionada(ocupadas[i]);
            }

            refrescaEstadoButacas();

            var msj = UI.i18n.butacas.ocupadas;
            alert(msj);
        }
    }

    function compruebaEstadoButacas() {
        var path = baseUrl + "/rest/entrada/" + $("input[name=idSesion]").val() + "/ocupadas";

        $.ajax({
            url: path,
            type: "POST",
            data: $.toJSON({uuidCompra: uuidCompra, butacas: butacasSeleccionadas}),
            contentType: "application/json; charset=utf-8",
            dataType: "json",
            success: ocupadasSuccess
        });
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
        $('html,body').animate({
            scrollTop: $("#divButacas").offset().top
        });
    }

    $(document).ready(function () {
        $('input[name=tipo]').click(function () {
            muestraPrecios();
        });

        $('#limpiarSeleccion').click(function () {
            limpiaSeleccion();
        });

        muestraPrecios();
    });

    pm.bind("butacas", function (data) {
        return butacasSeleccionadas;
    });

    return {
        selecciona: selecciona,
        init: init,
        cargaPrecios: cargaPrecios,
        cambiaTipoButaca: cambiaTipoButaca,
        cambiaTipoTodasButacas: cambiaTipoTodasButacas,
        compruebaEstadoButacas: compruebaEstadoButacas,
        muestraLocalizacion: muestraLocalizacion,
        muestraMinimapa: muestraMinimapa,
        anula: anula,
        pasarACompra: pasarACompra,
        pasarButacaACompra: pasarButacaACompra,
        imprimir: imprimir
    };

}());
