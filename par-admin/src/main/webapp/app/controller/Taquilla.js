Ext.define('Paranimf.controller.Taquilla', {
    extend: 'Ext.app.Controller',

    views: ['EditModalWindow', 'EditBaseForm', 'EditBaseGrid', 'taquilla.PanelTaquilla', 'taquilla.GridEventosTaquilla', 'taquilla.GridSesionesTaquilla',
        'taquilla.FormComprar', 'taquilla.PanelSeleccionarNoNumeradas', 'taquilla.PanelNumeroEntradas'],
    stores: ['EventosTaquilla', 'SesionesTaquilla', 'Horas', 'Minutos'],
    models: ['Evento', 'Compra', 'HoraMinuto'],

    refs: [
        {
            ref: 'panelTaquilla',
            selector: 'panelTaquilla'
        },
        {
            ref: 'gridEventosTaquilla',
            selector: 'gridEventosTaquilla'
        },
        {
            ref: 'gridSesionesTaquilla',
            selector: 'gridSesionesTaquilla'
        },
        {
            ref: 'formComprar',
            selector: 'formComprar'
        },
        {
            ref: 'formComprarCards',
            selector: '#formComprarCards'
        },
        {
            ref: 'disponiblesNoNumeradas',
            selector: 'panelSeleccionarNoNumeradas label[name=disponibles]'
        },
        {
            ref: 'localizacionesNoNumeradas',
            selector: 'panelSeleccionarNoNumeradas panel[name=localizaciones]'
        },
        {
            ref: 'estadoPagoTarjeta',
            selector: '#formComprarCards label[name=estadoPagoTarjeta]'
        },
        {
            ref: 'verEntrada',
            selector: 'formComprar button[name=verEntrada]'
        },
        {
            ref: 'printEntradaDirectamente',
            selector: 'formComprar button[name=printEntradaDirectamente]'
        },
        {
            ref: 'continuarCompra',
            selector: 'formComprar button[name=continuarCompra]'
        },
        {
            ref: 'botonPagar',
            selector: 'formComprar #pagar'
        },
        {
            ref: 'botonReservar',
            selector: 'formComprar #reservar'
        },
        {
            ref: 'panelComprar',
            selector: 'formComprar panel[name=panelComprar]'
        },
        {
            ref: 'botonAnularPrevia',
            selector: 'formComprar #anularPrevia'
        },
        {
            ref: 'panelReservar',
            selector: 'formComprar panel[name=panelReservar]'
        },
        {
            ref: 'reservarDesde',
            selector: 'formComprar datepicker[name=desde]'
        },
        {
            ref: 'reservarHasta',
            selector: 'formComprar datepicker[name=hasta]'
        },
        {
            ref: 'observacionesReserva',
            selector: 'formComprar textareafield[name=observacionesReserva]'
        },
        {
            ref: 'panelAnfiteatro',
            selector: 'panelSeleccionarNoNumeradas panelNumeroEntradas[name=anfiteatro]'
        },
        {
            ref: 'panelPlatea1',
            selector: 'panelSeleccionarNoNumeradas panelNumeroEntradas[name=platea1]'
        },
        {
            ref: 'panelPlatea2',
            selector: 'panelSeleccionarNoNumeradas panelNumeroEntradas[name=platea2]'
        },
        {
            ref: 'panelDiscapacitados1',
            selector: 'panelSeleccionarNoNumeradas panelNumeroEntradas[name=discapacitados1]'
        },
        {
            ref: 'panelDiscapacitados2',
            selector: 'panelSeleccionarNoNumeradas panelNumeroEntradas[name=discapacitados2]'
        },
        {
            ref: 'totalPrecioCompra',
            selector: 'panelSeleccionarNoNumeradas label[name=totalPrecios]'
        }, {
            ref: 'importePagado',
            selector: 'formComprar #pasoPagar numberfield[name=importePagado]'
        }, {
            ref: 'importeADevolver',
            selector: 'formComprar #pasoPagar label[name=dineroADevolver]'
        }, {
            ref: 'referenciaDePago',
            selector: 'formComprar #pasoPagar textfield[name=referenciaDePago]'
        }, {
            ref: 'hiddenTotalPrecio',
            selector: 'formComprar #pasoPagar hiddenfield[name=hiddenTotalPrecio]'
        }, {
            ref: 'horaInicio',
            selector: 'formComprar combobox[name=horaInicio]'
        }, {
            ref: 'horaFin',
            selector: 'formComprar combobox[name=horaFin]'
        }, {
            ref: 'minutoInicio',
            selector: 'formComprar combobox[name=minutoInicio]'
        }, {
            ref: 'minutoFin',
            selector: 'formComprar combobox[name=minutoFin]'
        }, {
            ref: 'checkMostrarTodosEventos',
            selector: 'gridEventosTaquilla checkbox[name=mostrarTodos]'
        }, {
            ref: 'btReservar',
            selector: 'gridSesionesTaquilla button[action=reservar]'
        }
    ],

    init: function () {
        this._allEventosShown = false;
        this.control({

            'panelTaquilla': {
                beforeactivate: this.recargaStore
            },
            'gridEventosTaquilla': {
                selectionchange: this.loadSesiones
            },
            'gridSesionesTaquilla': {
                itemdblclick: this.comprar
            },
            'gridSesionesTaquilla button[action=comprar]': {
                click: this.comprar
            },
            'gridSesionesTaquilla button[action=reservar]': {
                click: this.reservar
            },
            'formComprar #comprarAnterior': {
                click: this.comprarAnterior
            },
            'formComprar #comprarSiguiente': {
                click: this.comprarSiguiente
            },
            'formComprar #comprarCancelar': {
                click: this.cerrarComprar
            },
            'formComprar #anularPrevia': {
                click: this.anularPrevia
            },
            'formComprar': {
                afterrender: this.iniciaFormComprar
            },
            'formComprar #pagar': {
                click: this.registraCompra
            },
            'formComprar #reservar': {
                click: this.registraReserva
            },
            'formComprar button[name=verEntrada]': {
                click: this.verEntrada
            },
            'formComprar button[name=printEntradaDirectamente]': {
                click: this.imprimirEntrada
            },
            'formComprar button[name=continuarCompra]': {
                click: this.continuarCompra
            },
            'panelSeleccionarNoNumeradas': {
                afterrender: this.panelSeleccionarNoNumeradasCreado
            },
            'panelNumeroEntradas numberfield': {
                change: this.actualizaPrecio
            },
            'formComprar #tipoPago': {
                select: this.muestraOcultaDevolucionImporte,
                afterrender: this.seleccionaModoPago
            },
            'formComprar #pasoPagar numberfield[name=importePagado]': {
                change: this.actualizaCambioADevolver
            },

            'formComprar panel[name=panelReservar]': {
                beforerender: this.setFechaHoraFinalReserva
            },

            'gridEventosTaquilla checkbox[name=mostrarTodos]': {
                change: this.showHideTodosEventos
            }
        });

        this.intervalEstadoPago = {};
    },

    showHideTodosEventos: function () {
        var doRecargar = true;
        if (this.getCheckMostrarTodosEventos().checked) {
            if (!confirm(UI.i18n.message.confirmVentaDegradada)) {
                doRecargar = false;
                this.getCheckMostrarTodosEventos().setValue('');
                this._allEventosShown = false;
            } else {
                this.getBtReservar().hide();
                this._allEventosShown = true;
            }
        } else
            this.getBtReservar().show();

        if (doRecargar) {
            var url = (this.getCheckMostrarTodosEventos().checked) ? urlPrefix + 'evento' : urlPrefix + 'evento?activos=true';
            this.getGridEventosTaquilla().store.proxy.url = url;
            this.recargaStore();
        }
    },

    setFechaHoraFinalReserva: function (panel) {
        var seccionSeleccionada = this.getGridSesionesTaquilla().getSelectedRecord();
        var fecha = seccionSeleccionada.data.fechaFinVentaOnline ? seccionSeleccionada.data.fechaFinVentaOnline : seccionSeleccionada.data.fechaCelebracion;
        this.getReservarHasta().setValue(fecha);
        panel.down('combobox[name=horaFin]').setValue(fecha.getHours());
        panel.down('combobox[name=minutoFin]').setValue(fecha.getMinutes());
    },

    actualizaCambioADevolver: function () {
        var importeADevolver = this.getImportePagado().value - this.getHiddenTotalPrecio().value;
        this.getImporteADevolver().setText(UI.i18n.field.importeADevolver + importeADevolver.toFixed(2) + " €");
    },

    muestraOcultaDevolucionImporte: function (combo, records, eOpts) {
        var newvalue = combo.getValue();
        this.getBotonPagar().setText(UI.i18n.button.pagar);
        this.getImportePagado().hide();
        this.getImporteADevolver().hide();
        this.getReferenciaDePago().hide();

        if (newvalue == 'metalico') {
            this.getImportePagado().show();
            this.getImporteADevolver().show();
        } else if (newvalue == 'tarjetaOffline') {
            this.getBotonPagar().setText(UI.i18n.button.registrarPago);
            this.getReferenciaDePago().show();
        }
    },

    actualizaPrecio: function () {
        var precio = 0;
        var EURO = '€';
        for (var key in this.precios) {
            var panel = Ext.ComponentQuery.query('panelSeleccionarNoNumeradas panelNumeroEntradas[name=' + key + ']');

            if (panel != undefined && panel.length >= 1) {
                for (var tarifa in this.precios[key]) {
                    var numberfield = panel[0].down('numberfield[name=' + tarifa + ']');
                    var numeroEntradasTarifa = (numberfield.getValue() == '') ? 0 : numberfield.getValue();
                    var precioTarifa = this.precios[key][tarifa];
                    precio += numeroEntradasTarifa * precioTarifa;
                }
            }
        }
        this.getTotalPrecioCompra().setText(UI.i18n.field.totalCompra + precio.toFixed(2) + EURO);
    },

    iniciaFormComprar: function () {
        this.idCompra = null;
        this.idPagoTarjeta = null;
        this.butacasSeleccionadas = [];
        var layout = this.getFormComprarCards().getLayout();
        layout.setActiveItem(0);
        this.cambiarEstadoBotonesComprar();
    },

    seleccionaModoPago: function (combo, opts) {
        if (combo != undefined) {
            var store = combo.getStore();
            if (store != undefined && store.getAt(0) != undefined)
                combo.setValue(combo.getStore().getAt(0).data.value);
        }

        this.muestraOcultaDevolucionImporte(combo);
    },

    muestraDisponibles: function () {
        var DISPONIBLES = UI.i18n.field.entradesDisponibles;
        for (var key in this.disponibles) {
            var panel = Ext.ComponentQuery.query('panelSeleccionarNoNumeradas panelNumeroEntradas[name=' + key + ']');

            if (panel != undefined && panel.length >= 1)
                panel[0].down('label[name=disponibles]').setText(DISPONIBLES + this.disponibles[key]);
        }
    },

    anularPrevia: function () {
        var textoButacas = "";

        for (var i = 0; i < this.butacasSeleccionadasPrevia.length; i++) {
            var butaca = this.butacasSeleccionadasPrevia[i];
            var filaNum = "";

            if (butaca['fila'] != null && butaca['numero'] != null)
                filaNum = " fila: " + butaca['fila'] + ", núm: " + butaca['numero'];
            textoButacas += filaNum;
        }

        var textoConfirm = UI.i18n.message.anularPreviaIntro + '<br><b>' + this.tituloEventoPrevio + '</b><br>' +
            UI.i18n.message.anularPreviaSesion + '<b>' + this.sesionPrevia + '</b><br>' + textoButacas;

        Ext.Msg.confirm(UI.i18n.formTitle.anularPrevia, textoConfirm, function (id, value) {
            if (id == 'yes')
                this.llamaAnularPrevia();
        }, this);
    },

    llamaAnularPrevia: function () {
        var me = this;

        Ext.Ajax.request({
            url: urlPrefix + 'compra/' + this.idSesionPrevia + '/' + this.idCompraPrevia,
            method: 'PUT',
            success: function (response) {
                me.butacasSeleccionadasPrevia = null;
                me.idCompraPrevia = null;
                me.cambiaVisibilidadBotonAnularPrevia();
                me.getFormComprar().up('window').close();
                me.comprar();
                alert(UI.i18n.message.compraAnuladaOk);
            }, failure: function (response) {
                alert(UI.i18n.error.anularCompraReserva);
            }
        });
    },

    /*
     -- en la uji se usa para ocultar la tarifa de descuento para cine, teatro y cualquier cosa que tenga de precio < 8
     --vemos a ver si con la nueva gestión de tarifas no es necesario, ya que solamente se muestran los
     --conceptos que tengan precio
     ocultaDescuentosNoDisponiblesNoNumeradas: function() {
     var tipoEvento = this.getGridEventosTaquilla().getSelectedRecord().data['parTiposEvento']['nombreEs'].toLowerCase();

     for (var localizacion in this.precios) {

     var precioNormal = this.precios[localizacion]['normal'];
     var precioDescuento = this.precios[localizacion]['descuento'];
     var panel = Ext.ComponentQuery.query('panelSeleccionarNoNumeradas panelNumeroEntradas[name=' + localizacion + ']');

     //TODO -> esta funcion es del par-public, y si no está puesto, falla
     if (descuentoNoDisponible != undefined) {
     if (descuentoNoDisponible(tipoEvento, precioNormal, precioDescuento))
     {
     panel[0].down('numberfield[name=descuento]').hide();
     panel[0].down('panel[name=preuDescuento]').hide();
     }
     else
     {
     panel[0].down('numberfield[name=descuento]').show();
     panel[0].down('panel[name=preuDescuento]').show();
     }
     }
     }
     },*/

    muestraPrecios: function () {
        var PRECIOS = UI.i18n.field.precioPorEntrada;
        var EURO = ' &euro;';
        for (var key in this.precios) {
            var panel = Ext.ComponentQuery.query('panelSeleccionarNoNumeradas panelNumeroEntradas[name=' + key + ']');

            if (panel != undefined && panel.length >= 1) {
                for (var tarifa in this.precios[key])
                    panel[0].down('panel[name=preu' + tarifa + ']').update(PRECIOS + this.precios[key][tarifa] + EURO);
            }
        }
    },

    activaCamposCompra: function () {
        for (var key in this.disponibles) {
            var hayDisponibles = (this.disponibles[key] == 0) ? false : true;

            if (this.precios[key] != undefined && this.precios[key] != '' && this.precios[key] != 0) {
                for (var tarifa in this.precios[key]) {
                    var numberfield = Ext.ComponentQuery.query('panelSeleccionarNoNumeradas panelNumeroEntradas[name=' + key + '] panel[name=panelTarifas] numberfield[name=' + tarifa + ']')[0];
                    if (hayDisponibles) {
                        if (numberfield)
                            numberfield.setDisabled(false);
                    }
                }
            }
        }
    },

    panelSeleccionarNoNumeradasCreado: function () {
        var me = this;
        var idSesion = this.getGridSesionesTaquilla().getSelectedRecord().data['id'];

        this.cargaLocalizaciones(idSesion, function () {
            me.cargaPrecios(idSesion, function () {
                me.cargaDisponibles(idSesion, function () {
                    me.muestraDisponibles();
                    me.muestraPrecios();
                    me.activaCamposCompra();
                });
            });
        });
    },

    avanzarAPasoDePago: function (butacas) {
        var layout = this.getFormComprarCards().getLayout();
        layout.setActiveItem(layout.getNext());
        this.consultaImportes(butacas);
        this.cambiarEstadoBotonesComprar();
    },

    consultaImportes: function (butacas) {
        var me = this;
        var idSesion = this.getGridSesionesTaquilla().getSelectedRecord().data['id'];
        me.getFormComprar().setLoading(UI.i18n.message.loading);
        me.rellenaDatosPasoPagar("-");
        this.getHiddenTotalPrecio().setValue("0");

        Ext.Ajax.request({
            url: urlPrefix + 'compra/' + idSesion + '/importe',
            method: 'POST',
            jsonData: butacas,
            success: function (response) {
                me.getFormComprar().setLoading(false);
                var importe = Ext.JSON.decode(response.responseText, true);
                me.getHiddenTotalPrecio().setValue(importe);
                me.rellenaDatosPasoPagar(importe.toFixed(2));
            }, failure: function (response) {
                me.getFormComprar().setLoading(false);
                var respuesta = Ext.JSON.decode(response.responseText, true);

                if (respuesta['message'] != null)
                    alert(respuesta['message']);
                else
                    alert(UI.i18n.error.formSave);
            }
        });

    },

    getButacasNoNumeradas: function () {
        var me = this;
        var butacas = [];

        for (var key in this.precios) {
            var panel = Ext.ComponentQuery.query('panelSeleccionarNoNumeradas panelNumeroEntradas[name=' + key + ']');

            if (panel != undefined && panel.length >= 1) {
                for (var tarifa in this.precios[key]) {
                    var valueNumberField = panel[0].down('numberfield[name=' + tarifa + ']').value;

                    for (var i = 0; i < parseInt(valueNumberField); i++) {
                        var butaca = {
                            localizacion: key,
                            fila: null,
                            numero: null,
                            x: null,
                            y: null,
                            tipo: tarifa,
                            precio: me.precios[key][tarifa]
                        };
                        butacas.push(butaca);
                    }
                }
            }
        }
        return butacas;
    },

    muestraMensajePagoTarjeta: function (mensaje) {
        if (this.getEstadoPagoTarjeta() != null)
            this.getEstadoPagoTarjeta().setText(mensaje, false);
    },

    habilitaBotonPagar: function () {
        if (this.getBotonPagar() != null)
            this.getBotonPagar().setDisabled(false);
    },

    deshabilitaBotonPagar: function () {
        if (this.getBotonPagar() != null)
            this.getBotonPagar().setDisabled(true);
    },

    habilitaBotonReservar: function () {
        if (this.getBotonReservar() != null)
            this.getBotonReservar().setDisabled(false);
    },

    deshabilitaBotonReservar: function () {
        if (this.getBotonReservar() != null)
            this.getBotonReservar().setDisabled(true);
    },

    registraCompra: function () {
        var tipoPago = Ext.getCmp('tipoPago').value;

        if (tipoPago == 'tarjetaOffline') {
            var referenciaDePago = this.getReferenciaDePago().getValue().trim();
            if (referenciaDePago == undefined || !referenciaDePago || referenciaDePago == "") {
                alert(UI.i18n.error.errorReferenciaDePago);
                return false;
            }
        }
        this.deshabilitaBotonPagar();
        var idSesion = this.getGridSesionesTaquilla().getSelectedRecord().data['id'];
        var me = this;
        me.getFormComprar().setLoading(UI.i18n.message.loading);

        Ext.Ajax.request({
            url: urlPrefix + 'compra/' + idSesion,
            method: 'POST',
            jsonData: this.butacasSeleccionadas,
            success: function (response) {
                me.getFormComprar().setLoading(false);
                var respuesta = Ext.JSON.decode(response.responseText, true);

                me.idCompra = respuesta['id'];
                me.uuidCompra = respuesta['uuid'];

                if (tipoPago == 'metalico')
                    me.marcaPagada(me.idCompra);
                else if (tipoPago == 'tarjeta') {
                    var tituloEvento = me.getGridEventosTaquilla().getSelectedRecord().data['tituloVa'];
                    me.pagarConTarjeta(respuesta['id'], tituloEvento);
                }
                else if (tipoPago == 'tarjetaOffline')
                    me.marcaPagada(me.idCompra, me.getReferenciaDePago().getValue().trim());
                else if (tipoPago == 'transferencia')
                    me.marcaPagada(me.idCompra);

            }, failure: function (response) {
                me.getFormComprar().setLoading(false);
                me.habilitaBotonPagar();
                me.muestraMensajePagoTarjeta(UI.i18n.error.errorRegistrandoCompra);
                var respuesta = Ext.JSON.decode(response.responseText, true);

                if (respuesta['message'] != null)
                    alert(respuesta['message']);
                else
                    alert(UI.i18n.error.formSave);
            }
        });
    },

    registraReserva: function () {
        this.deshabilitaBotonReservar();
        var idSesion = this.getGridSesionesTaquilla().getSelectedRecord().data['id'];
        var me = this;
        me.getFormComprar().setLoading(UI.i18n.message.loading);
        Ext.Ajax.request({
            url: urlPrefix + 'reserva/' + idSesion,
            method: 'POST',
            jsonData: {
                butacasSeleccionadas: this.butacasSeleccionadas,
                desde: this.getReservarDesde().getValue(),
                hasta: this.getReservarHasta().getValue()
                ,
                observaciones: this.getObservacionesReserva().getValue(),
                horaInicial: this.getHoraInicio().value,
                horaFinal: this.getHoraFin().value,
                minutoInicial: this.getMinutoInicio().value,
                minutoFinal: this.getMinutoFin().value
            },
            success: function (response) {
                me.getFormComprar().setLoading(false);
                me.habilitaBotonReservar();
                me.cerrarComprar();

            }, failure: function (response) {
                me.getFormComprar().setLoading(false);
                me.habilitaBotonReservar();
                var respuesta = Ext.JSON.decode(response.responseText, true);

                if (respuesta['message'] != null)
                    alert(respuesta['message']);
                else
                    alert(UI.i18n.error.formSave);
            }
        });
    },

    pagarConTarjeta: function (id, concepto) {
        var me = this;
        me.getFormComprar().setLoading(UI.i18n.message.loading);
        me.muestraMensajePagoTarjeta(UI.i18n.message.pagoTarjetaEnviando);

        Ext.Ajax.request({
            url: urlPrefix + 'pago/' + id,
            method: 'POST',
            jsonData: concepto,
            success: function (response) {
                me.getFormComprar().setLoading(false);
                var respuesta = Ext.JSON.decode(response.responseText, true);

                if (respuesta == null || respuesta.error) {
                    var msj = UI.i18n.error.errorRealizaPago;

                    if (respuesta['mensajeExcepcion'])
                        msj += ' (' + respuesta['mensajeExcepcion'] + ')';

                    alert(msj);
                    me.muestraMensajePagoTarjeta(UI.i18n.error.errorRealizaPago);
                    me.habilitaBotonPagar();
                }
                else {
                    var newElement = document.createElement('div');
                    newElement.innerHTML = respuesta.codigo;

                    var scripts = newElement.getElementsByTagName("script");
                    var loadingSrc = false;
                    for (var i = 0; i < scripts.length; ++i) {
                        var script = scripts[i];
                        if (script.src != undefined && script.src != '' && !loadingSrc) {
                            var loadingSrc = true;
                            jQuery.getScript(script.src).done(function() {
                                for (var i = 0; i < scripts.length; ++i) {
                                    var script = scripts[i];
                                    if (script.src == undefined || script.src == '')
                                        eval(script.innerHTML);
                                }
                            });
                        }
                        else if (!loadingSrc) {
                            eval(script.innerHTML);
                        }
                    }
                    //me.idPagoTarjeta = respuesta.codigo;
                    me.muestraMensajePagoTarjeta(UI.i18n.message.pagoTarjetaEnviadoLector);
                    me.lanzaComprobacionEstadoPago(id);
                }

            },
            failure: function (response) {
                me.getFormComprar().setLoading(false);
                me.habilitaBotonPagar();
                me.muestraMensajePagoTarjeta(UI.i18n.error.errorRealizaPago);
                var respuesta = Ext.JSON.decode(response.responseText, true);

                if (respuesta != null && respuesta['message'] != null)
                    alert(respuesta['message']);
                else
                    alert(UI.i18n.error.errorRealizaPago);
            }
        });
   },

    lanzaComprobacionEstadoPago: function (idPago) {
        var me = this;
        this.intervalEstadoPago[idPago] = window.setInterval(function () {
            me.compruebaEstadoPago(idPago);
        }, 2000);
    },

    paraComprobacionEstadoPago: function (idPago) {
        window.clearInterval(this.intervalEstadoPago[idPago]);
        delete this.intervalEstadoPago[idPago];
    },

    compruebaEstadoPago: function (idPago) {
        var me = this;
        me.getFormComprar().setLoading(UI.i18n.message.loading);
        Ext.Ajax.request({
            url: urlPrefix + 'pago/' + idPago,
            method: 'GET',
            success: function (response) {
                me.getFormComprar().setLoading(false);
                var respuesta = Ext.JSON.decode(response.responseText, true);

                if (respuesta == null || respuesta.error) {
                    me.paraComprobacionEstadoPago(idPago);
                    var msj = UI.i18n.error.errorRealizaPago;

                    if (respuesta != null && respuesta['mensajeExcepcion'])
                        msj += ' (' + respuesta['mensajeExcepcion'] + ')';
                    me.muestraMensajePagoTarjeta(msj);
                }
                else {
                    if (respuesta['codigoAccion'] == '1' || respuesta['codigoAccion'] == '6') {
                        // El pago está en proceso
                    }
                    else if (respuesta['codigoAccion'] == '0') {
                        me.paraComprobacionEstadoPago(idPago);
                        me.muestraMensajePagoTarjeta(UI.i18n.message.pagoTarjetaCorrecto);
                        me.desahiblitaEstadoBotonesComprar();
                        me.muestraEnlacePdf();
                    }
                    else {
                        me.paraComprobacionEstadoPago(idPago);
                        me.muestraMensajePagoTarjeta(UI.i18n.error.errorRealizaPago + '<br/>(' + respuesta['codigoAccion'] + ': ' + UI.i18n.error.pinpad[respuesta['codigoAccion']] + ')');
                        me.habilitaBotonPagar();
                    }
                }
            },
            failure: function (response) {
                me.getFormComprar().setLoading(false);
                me.habilitaBotonPagar();
                me.muestraMensajePagoTarjeta(UI.i18n.error.errorRealizaPago);
                var respuesta = Ext.JSON.decode(response.responseText, true);

                if (respuesta != null && respuesta['message'] != null)
                    alert(respuesta['message']);
                else
                    alert(UI.i18n.error.errorRealizaPago);
            }
        });
    },

    muestraEnlacePdf: function () {
        this.getVerEntrada().show();

        var isFirefox = navigator.userAgent.toLowerCase().indexOf('firefox') > -1;
        if (!isFirefox)
            this.getPrintEntradaDirectamente().show();
    },

    verEntrada: function () {
        this.getContinuarCompra().show();

        var href = urlPrefix + 'compra/' + this.uuidCompra + '/pdftaquilla';
        this.windowEntrada = window.open(href, 'Imprimir entrada');
    },

    imprimirEntrada: function () {
        this.getContinuarCompra().show();

        printJS({printable: urlPrefix + 'compra/'  + this.uuidCompra + '/pdftaquilla'});
    },

    continuarCompra: function () {
        this.getFormComprar().up('window').close();
        this.comprar();

        if (this.windowEntrada != null)
            this.windowEntrada.close();
    },

    guardarDatosCompraPrevia: function () {
        this.butacasSeleccionadasPrevia = this.butacasSeleccionadas;
        this.idCompraPrevia = this.idCompra;
        this.idSesionPrevia = this.getGridSesionesTaquilla().getSelectedRecord().data['id'];
        this.tituloEventoPrevio = this.getGridEventosTaquilla().getSelectedRecord().data['tituloVa'];
        this.sesionPrevia = Ext.Date.format(this.getGridSesionesTaquilla().getSelectedRecord().data['fechaCelebracion'], 'd/m/Y H:i');
    },

    cargaLocalizaciones: function (sesionId, callback) {
        var me = this;
        this.getFormComprar().setLoading(UI.i18n.message.loading);

        Ext.Ajax.request({
            url: urlPrefix + 'evento/sesion/' + sesionId + '/localizacion',
            method: 'GET',
            success: function (response) {
                me.getFormComprar().setLoading(false);
                var jsonLocalizaciones = Ext.decode(response.responseText);

                for (var i = 0; i < jsonLocalizaciones.length; i++) {
                    var localizacion = jsonLocalizaciones[i];
                    var field = new Ext.form.Panel({
                        columnWidth: 1 / 2,
                        name: localizacion.codigo,
                        hidden: true,
                        title: UI.i18n.legends.entrades + " " + localizacion.nombreEs.toUpperCase(),
                        items: [{
                            name: localizacion.codigo,
                            xtype: 'panelNumeroEntradas'
                        }]
                    });
                    me.getLocalizacionesNoNumeradas().add(field);
                }
                callback();
            },
            failure: function (response) {
                me.getFormComprar().setLoading(false);
                alert(UI.i18n.error.loadingLocalizaciones);
            }
        });
    },

    cargaPrecios: function (sesionId, callback) {
        var me = this;
        this.getFormComprar().setLoading(UI.i18n.message.loading);

        Ext.Ajax.request({
            url: urlPrefix + 'compra/' + sesionId + '/precios',
            method: 'GET',
            success: function (response) {
                me.getFormComprar().setLoading(false);
                var jsonPrecios = Ext.decode(response.responseText);
                me.precios = {};

                for (var i = 0; i < jsonPrecios.data.length; i++) {
                    var sesion = jsonPrecios.data[i];

                    if (me.precios[sesion.localizacion.codigo] == undefined)
                        me.precios[sesion.localizacion.codigo] = {};
                    me.precios[sesion.localizacion.codigo][sesion.tarifa.id] = sesion.precio;

                    var field = new Ext.form.field.Number({
                        columnWidth: 2 / 3,
                        name: sesion.tarifa.id,
                        fieldLabel: sesion.tarifa.nombre,
                        minWidth: 260,
                        width: 60,
                        labelWidth: 200,
                        labelAlign: 'right',
                        allowDecimals: false,
                        value: 0,
                        minValue: 0,
                        disabled: true
                    });

                    var label = new Ext.form.Panel({
                        columnWidth: 1 / 3,
                        name: 'preu' + sesion.tarifa.id,
                        html: UI.i18n.field.precioPorEntrada + "...",
                        border: false,
                        minHeight: 30,
                        padding: '5 0 0 5'
                    });

                    var panelConNombreDeLocalizacionDeTarifa = Ext.ComponentQuery.query('panelSeleccionarNoNumeradas panelNumeroEntradas[name=' + sesion.localizacion.codigo + '] panel[name=panelTarifas]')[0];
                    var panelAHacerVisible = Ext.ComponentQuery.query('panelSeleccionarNoNumeradas panel[name=' + sesion.localizacion.codigo + ']')[0];
                    panelAHacerVisible.show();
                    panelConNombreDeLocalizacionDeTarifa.add(field);
                    panelConNombreDeLocalizacionDeTarifa.add(label);
                    panelConNombreDeLocalizacionDeTarifa.setHeight(panelConNombreDeLocalizacionDeTarifa.height + 25);
                }
                callback();
            },
            failure: function (response) {
                me.getFormComprar().setLoading(false);
                alert(UI.i18n.error.loadingPreciosNoNumeradas);
            }
        });
    },

    cargaDisponibles: function (sesionId, callback) {
        var me = this;
        me.getFormComprar().setLoading(UI.i18n.message.loading);

        Ext.Ajax.request({
            url: urlPrefix + 'compra/' + sesionId + '/disponibles',
            method: 'GET',
            success: function (response) {
                me.getFormComprar().setLoading(false);
                var jsonData = Ext.decode(response.responseText);
                me.disponibles = {};

                for (var i = 0; i < jsonData.data.length; i++) {
                    var disponible = jsonData.data[i];
                    me.disponibles[disponible.localizacion] = disponible.disponibles;
                }
                callback();
            },
            failure: function (response) {
                me.getFormComprar().setLoading(false);
                alert(UI.i18n.error.loadingDisponiblesNoNumeradas);
            }
        });
    },

    rellenaDatosPasoPagar: function (importe) {
        Ext.getCmp('total').setText(UI.i18n.field.total + ": " + importe + " €");
    },

    cerrarComprar: function () {
        this.getFormComprar().up('window').close();
    },

    entradasNumeradas: function () {
        return Ext.getCmp('pasoSeleccionar').getLayout().activeItem.id == 'iframeButacas';
    },

    comprarAnterior: function () {
        var layout = this.getFormComprarCards().getLayout();
        var pasoActual = layout.activeItem.id;

        if (pasoActual != 'pasoSeleccionar')
            layout.setActiveItem(layout.getActiveItem() - 1);
        this.cambiarEstadoBotonesComprar();
    },

    comprarSiguiente: function () {
        var layout = this.getFormComprarCards().getLayout();
        var pasoActual = layout.activeItem.id;

        if (pasoActual == 'pasoSeleccionar') {
            if (this.entradasNumeradas())
                this.comprarSiguienteNumeradas();
            else
                this.comprarSiguienteSinNumerar();
        }
        this.cambiarEstadoBotonesComprar();
    },

    comprarSiguienteNumeradas: function () {
        var me = this;

        // Llamamos al iframe de butacas para que nos pase las butacas seleccionadas
        pm({
            target: window.frames[window.frames.length - 1],
            type: 'butacas',
            data: {},
            success: function (butacas) {
                me.butacasSeleccionadas = butacas;
                me.avanzarAPasoDePago(butacas);
            }
        });
    },

    comprarSiguienteSinNumerar: function () {
        this.butacasSeleccionadas = this.getButacasNoNumeradas();
        this.avanzarAPasoDePago(this.butacasSeleccionadas);
    },

    cambiarEstadoBotonesComprar: function () {
        var layout = this.getFormComprarCards().getLayout();
        Ext.getCmp('comprarAnterior').setDisabled(!layout.getPrev());
        Ext.getCmp('comprarSiguiente').setDisabled(!layout.getNext());
    },

    desahiblitaEstadoBotonesComprar: function () {
        Ext.getCmp('comprarAnterior').setDisabled(true);
        Ext.getCmp('comprarSiguiente').setDisabled(true);
    },

    recargaStore: function (comp, opts) {
        this.getGridEventosTaquilla().deseleccionar();
        this.getGridSesionesTaquilla().store.proxy.url = urlPrefix + 'evento/-1/sesiones';
        this.getGridSesionesTaquilla().store.loadPage(1);
        this.getGridEventosTaquilla().store.loadPage(1);
    },

    loadSesiones: function (selectionModel, record) {
        if (record[0]) {
            var storeSesiones = this.getGridSesionesTaquilla().getStore();
            var eventoId = record[0].get('id');
            this.getGridSesionesTaquilla().setTitle(UI.i18n.gridTitle.sesionesCompras + ': ' + record[0].get('tituloVa'));
            var url = urlPrefix + 'evento/' + eventoId + '/sesiones';

            if (!this._allEventosShown)
                url += '?activos=true';
            storeSesiones.getProxy().url = url;
            storeSesiones.load();
        }
    },

    comprar: function (button, event, opts) {
        if (this.getGridEventosTaquilla().hasRowSelected() && this.getGridSesionesTaquilla().hasRowSelected()) {
            var evento = this.getGridEventosTaquilla().getSelectedRecord();
            var sesion = this.getGridSesionesTaquilla().getSelectedRecord();

            if (sesion.data.anulada == true)
                alert(UI.i18n.error.venderSesionAnulada);
            else {
                var title = UI.i18n.formTitle.comprar + " -> " + UI.i18n.message.evento + ": " + evento.data.tituloVa + " - " + UI.i18n.message.sessio + ": " + Ext.Date.format(sesion.data.fechaCelebracion, 'd/m/Y H:i');
                this.getGridSesionesTaquilla().showComprarWindow(sesion.data['id'], evento.data['asientosNumerados'], title, false);
                this.getPanelComprar().show();
                this.getPanelReservar().hide();
                this.cambiaVisibilidadBotonAnularPrevia();
            }
        } else
            alert(UI.i18n.message.selectRow);
    },

    cambiaVisibilidadBotonAnularPrevia: function () {
        if (this.butacasSeleccionadasPrevia)
            this.getBotonAnularPrevia().show();
        else
            this.getBotonAnularPrevia().hide();
    },

    reservar: function (button, event, opts) {
        if (this.getGridEventosTaquilla().hasRowSelected() && this.getGridSesionesTaquilla().hasRowSelected()) {
            var evento = this.getGridEventosTaquilla().getSelectedRecord();
            var sesion = this.getGridSesionesTaquilla().getSelectedRecord();

            if (sesion.data.anulada == true)
                alert(UI.i18n.error.reservarSesionAnulada);
            else {
                var title = UI.i18n.formTitle.reservar + " -> " + UI.i18n.message.evento + ": " + evento.data.tituloVa + " - " + UI.i18n.message.sessio + ": " + Ext.Date.format(sesion.data.fechaCelebracion, 'd/m/Y H:i');
                this.getGridSesionesTaquilla().showComprarWindow(sesion.data['id'], evento.data['asientosNumerados'], title, true);
                this.getPanelReservar().show();
                this.getPanelComprar().hide();
            }
        } else
            alert(UI.i18n.message.selectRow);
    },

    marcaPagada: function (idCompra, referenciaDePago) {
        var me = this;

        var tipoPago = Ext.getCmp('tipoPago').value;

        Ext.Ajax.request({
            url: urlPrefix + 'compra/' + idCompra + '/pagada?tipopago=' + tipoPago + (referenciaDePago != undefined ? "&referencia=" +
            referenciaDePago : ""),
            method: 'POST',
            success: function (response) {
                me.muestraMensajePagoTarjeta(UI.i18n.message.compraRegistradaOk);
                me.desahiblitaEstadoBotonesComprar();
                me.muestraEnlacePdf();
                me.guardarDatosCompraPrevia();
                me.cambiaVisibilidadBotonAnularPrevia();
            },
            failure: function (response) {
                me.habilitaBotonPagar();
                me.muestraMensajePagoTarjeta(UI.i18n.error.errorRealizaPago);
                var respuesta = Ext.JSON.decode(response.responseText, true);

                if (respuesta != null && respuesta['message'] != null)
                    alert(respuesta['message']);
                else
                    alert(UI.i18n.error.errorRealizaPago);
            }
        });
    }
});