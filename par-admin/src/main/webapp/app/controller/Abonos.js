Ext.define('Paranimf.controller.Abonos', {
   extend: 'Ext.app.Controller',

   views: ['EditModalWindow', 'EditBaseForm', 'EditBaseGrid', 'abono.GridAbonos', 'abono.GridSesionesAbonos', 'abono.FormAbonos', 'abono.PanelAbonos', 'abono.GridPreciosAbono', 'abono.FormSesionesAbono', 'abono.GridAbonados', 'abono.FormComprarAbono', 'abono.FormAbonados'],
   stores: ['Abonos', 'PlantillasPreciosCombo', 'PreciosSesion', 'SesionesAbono', 'SesionesCombo', 'EventosCombo', 'Abonados', 'Salas'],
   models: ['Abono', 'PrecioSesion', 'SesionAbono', 'Sesion', 'Evento', 'Abonado', 'Sala'],

   refs: [{
      ref: 'gridAbonos',
      selector: 'gridAbonos'
   }, {
      ref: 'gridSesionesAbonos',
      selector: 'gridSesionesAbonos'
   }, {
      ref: 'gridAbonados',
      selector: 'gridAbonados'
   }, {
      ref: 'gridPreciosAbono',
      selector: 'gridPreciosAbono'
   }, {
      ref: 'formAbonos',
      selector: 'formAbonos'
   }, {
      ref: 'formSesionesAbono',
      selector: 'formSesionesAbono'
   }, {
      ref: 'formAbonados',
      selector: 'formAbonados'
   }, {
      ref: 'formComprarAbono',
      selector: 'formComprarAbono'
   }, {
      ref: 'botonAnularPrevia',
      selector: 'formComprarAbono #anularPrevia'
   }, {
      ref: 'panelComprar',
      selector: 'formComprarAbono panel[name=panelComprar]'
   }, {
      ref: 'formComprarAbonoCards',
      selector: '#formComprarAbonoCards'
   }, {
      ref: 'estadoPagoTarjeta',
      selector: '#formComprarAbonoCards label[name=estadoPagoTarjeta]'
   },{
      ref: 'panelAbonos',
      selector: 'panelAbonos'
   }, {
      ref: 'idFormAbonos',
      selector: 'formAbonos textfield[name=id]'
   }, {
      ref: 'comboPlantillaPrecios',
      selector: 'formAbonos combobox[name=plantillaPrecios]'
   }, {
      ref: 'comboSalas',
      selector: 'formAbonos combobox[name=sala]'
   }, {
      ref: 'sesiones',
      selector: 'formAbonos hiddenfield[name=sesiones]'
   }, {
      ref: 'comboEventos',
      selector: 'formSesionesAbono combobox[name=evento]'
   }, {
      ref: 'comboSesiones',
      selector: 'formSesionesAbono combobox[name=sesion]'
   }, {
      ref: 'botonPagar',
      selector: 'formComprarAbono #pagar'
   }, {
      ref: 'hiddenTotalPrecio',
      selector: 'formComprarAbono #pasoPagar hiddenfield[name=hiddenTotalPrecio]'
   }, {
      ref: 'abonadoNombre',
      selector: 'formComprarAbono #pasoPagar textfield[name=nombre]'
   }, {
      ref: 'abonadoApellidos',
      selector: 'formComprarAbono #pasoPagar textfield[name=apellidos]'
   }, {
      ref: 'abonadoDireccion',
      selector: 'formComprarAbono #pasoPagar textfield[name=direccion]'
   }, {
      ref: 'abonadoPoblacion',
      selector: 'formComprarAbono #pasoPagar textfield[name=poblacion]'
   }, {
      ref: 'abonadoCP',
      selector: 'formComprarAbono #pasoPagar textfield[name=cp]'
   }, {
      ref: 'abonadoProvincia',
      selector: 'formComprarAbono #pasoPagar textfield[name=provincia]'
   }, {
      ref: 'abonadoTelefono',
      selector: 'formComprarAbono #pasoPagar textfield[name=telefono]'
   }, {
      ref: 'abonadoEmail',
      selector: 'formComprarAbono #pasoPagar textfield[name=email]'
   }, {
      ref: 'abonadoInfoPeriodica',
      selector: 'formComprarAbono #pasoPagar checkbox[name=infoPeriodica]'
   }
   ],

   init: function() {
      this.control({

         'gridAbonos button[action=add]': {
            click: this.addAbono
         },

         'gridAbonos button[action=edit]': {
            click: this.editAbono
         },

         'gridAbonos button[action=del]': {
            click: this.removeAbono
         },

         'gridAbonos': {
             selectionchange: this.loadAbonados
         },

         'gridAbonados button[action=add]': {
            click: this.addAbonado
         },

         'gridAbonados button[action=edit]': {
            click: this.editAbonado
         },

         'gridAbonados button[action=del]': {
            click: this.removeAbonado
         },

         'formAbonados button[action=save]': {
            click: this.saveAbonadoFormData
         },

         'panelAbonos': {
               beforeactivate: this.recargaStore
         },

         'formAbonos button[action=save]': {
            click: this.saveAbonoFormData
         }, 

         'formAbonos combobox[name=plantillaPrecios]': {
            change: this.cambiaPlantilla,
            beforeselect: this.cambiaPlantillaBefore
         },

         'formAbonos': {
            afterrender: this.preparaStorePlantillaPrecios
         },

         'formSesionesAbono button[action=save]': {
            click: this.addSesionAbono
         },

         'formSesionesAbono combobox[name=evento]': {
            change: this.cambiaSesiones
         },

         'gridSesionesAbonos button[action=add]': {
             click: this.addSesion
         },

         'gridSesionesAbonos button[action=del]': {
            click: this.removeSesion
         },

        'formComprarAbono #anularPrevia': {
             click: this.anularPrevia
         },

        'formComprarAbono #pagar': {
             click: this.registraCompra
         },

         'formComprarAbono': {
             afterrender: this.iniciaFormComprar
         }, 

         'formComprarAbono #comprarAnterior': {
            click: this.comprarAnterior
         },

         'formComprarAbono #comprarSiguiente': {
            click: this.comprarSiguiente
         },

         'formComprarAbono #comprarCancelar': {
            click: this.cerrarComprar
         }
      });

    this.intervalEstadoPago = {};
   },

   addAbono: function(button, event, opts) {
      this.getGridAbonos().showAddAbonoWindow();
      this.getFormAbonos().cargaComboStore('plantillaPrecios');
   },

   editAbono: function(button, event, opts) {
      this.getGridAbonos().edit('formAbonos');
      var idASeleccionar = -1;
      if (this.getGridAbonos().getSelectedColumnId() != undefined) {
         var selectedRecord = this.getGridAbonos().getSelectedRecord();
         idASeleccionar = selectedRecord.data.plantillaPrecios;
      }
      this.getFormAbonos().cargaComboStore('plantillaPrecios', idASeleccionar);
      this.loadSesiones();
   },

   removeAbono: function(button, event, opts) {
      var abonado = this.getGridAbonados().getStore().data.length;
      if (abonado > 0) {
        alert(UI.i18n.error.tieneAbonados);
      }
      else {
        this.getGridAbonos().remove();
      }
   },

   addAbonado: function(button, event, opts) {
      var abonoID = this.getGridAbonos().getSelectedColumnId();
      if (abonoID != undefined) {
        var title = UI.i18n.message.addAbonado;
        this.getGridAbonados().showAddAbonadoWindow(abonoID, title);
        this.getPanelComprar().show();
        this.cambiaVisibilidadBotonAnularPrevia();
      }
      else {
        alert(UI.i18n.message.selectAbono);
      }
   },

   editAbonado: function(button, event, opts) {
      this.getGridAbonados().edit('formAbonados');
   },

   cambiaVisibilidadBotonAnularPrevia: function() {
      if (this.butacasSeleccionadasPrevia)
        this.getBotonAnularPrevia().show();
      else
        this.getBotonAnularPrevia().hide();
    },

   removeAbonado: function(button, event, opts) {
      this.getGridAbonados().remove();
   },

   saveAbonoFormData: function(button, event, opts) {
      var grid = this.getGridAbonos();
      var form = this.getFormAbonos();
       var sesionesJSON = this.getGridSesionesAbonos().toJSON();
       if (sesionesJSON == "[]")
       {
           alert(UI.i18n.error.abonoSinSesiones);
           return;
       }
      this.getSesiones().setValue(sesionesJSON);
      form.saveFormData(grid, urlPrefix + 'abono', undefined, undefined, function(form, action) {
         if (action != undefined && action.response != undefined && action.response.responseText != undefined) {
            var respuesta = Ext.JSON.decode(action.response.responseText, true);
            var key = "UI.i18n.error." + respuesta.message;
            var msg = eval(key);

            if (msg != undefined)
               alert(msg);
            else
               alert(UI.i18n.error.formSave);
         }
      });
   },

   saveAbonadoFormData: function(button, event, opts) {
      var grid = this.getGridAbonados();
      var form = this.getFormAbonados();
      form.saveFormData(grid, urlPrefix + 'abono/abonado', undefined, undefined, function(form, action) {
         if (action != undefined && action.response != undefined && action.response.responseText != undefined) {
            var respuesta = Ext.JSON.decode(action.response.responseText, true);
            var key = "UI.i18n.error." + respuesta.message;
            var msg = eval(key);

            if (msg != undefined)
               alert(msg);
            else
               alert(UI.i18n.error.formSave);
         }
      });
   },

   addSesionAbono: function() {
      if (this.getComboSesiones().value != undefined) {
         if (this.getFormSesionesAbono().isValid()) {
            if (!this.getGridSesionesAbonos().containsId(this.getComboSesiones().value)) {
               var v = this.getComboSesiones().value;
               var sesion = this.getComboSesiones().findRecord(this.getComboSesiones().valueField || this.getComboSesiones().displayField, v);

               var record = {
                  id: sesion.data.id,
                  sesion: {id: sesion.data.id},
                  tituloVa: sesion.data.evento.tituloVa,
                  fechaCelebracion: sesion.data.fechaCelebracion,
                  horaApertura: sesion.data.horaApertura
               };
               this.getGridSesionesAbonos().addItemToStore(record);
               this.getFormSesionesAbono().up("window").close();
            } else
               alert(UI.i18n.error.peliculaJaAfegida);
         }
      }
   },

   cambiaPlantilla: function(combo, newValue, oldValue, opts) {
      if (newValue) {
        if (oldValue) {
          var oldSalaId = this.getComboPlantillaPrecios().getStore().findRecord("id", oldValue).data.sala;
          var newSalaId = this.getComboPlantillaPrecios().getStore().findRecord("id", newValue).data.sala;
          if (newSalaId != oldSalaId && this.getGridSesionesAbonos().store.data.length > 0) {
              this.getSesiones().setValue();
              this.getGridSesionesAbonos().store.loadRawData([]);
          }
        }

         var gridPreciosAbono = this.getGridPreciosAbono();
         Ext.Ajax.request({
           url : urlPrefix + 'plantillaprecios/' + newValue + '/precios',
           method: 'GET',
           success: function (response) {
               var respuesta = Ext.JSON.decode(response.responseText, true);
               gridPreciosAbono.store.loadRawData(respuesta.data);
           }, failure: function (response) {
              alert(UI.i18n.error.loadingPrecios);
           }
         });
      }
   },

   cambiaPlantillaBefore: function(combo, record, index) {
      if (this.getGridAbonados().store.data.length > 0 && this.getIdFormAbonos().getValue() > 0) {
        alert(UI.i18n.error.noModificableTieneAbonados);
        return false;
      }
   },

   cambiaSesiones: function(combo, newValue, oldValue, opts) {
      var storeSesiones = this.getComboSesiones().getStore();
      var eventoId = newValue;

      var salaId = this.getComboPlantillaPrecios().getStore().findRecord("id", this.getComboPlantillaPrecios().value).data.sala;
      storeSesiones.getProxy().url = urlPrefix + 'evento/' + eventoId + '/sesiones/sala/' + salaId;
      storeSesiones.load();
   },

   preparaStorePlantillaPrecios: function() {
      this.getGridSesionesAbonos().getStore().removeAll();
      this.getGridPreciosAbono().getStore().removeAll();
      this.getComboPlantillaPrecios().reset();
   },

   recargaStore: function(comp, opts) {
      console.log("RECARGA STORE ABONOS");
      this.getGridAbonos().recargaStore();
   },

   loadSesiones: function() {
      if (this.getGridAbonos().getSelectedColumnId() != undefined) {
         var selectedRecord = this.getGridAbonos().getSelectedRecord();
         var abonoId = selectedRecord.data.id;
         var gridSesionesAbonos = this.getGridSesionesAbonos();

         Ext.Ajax.request({
           url : urlPrefix + 'abono/' + abonoId + '/sesiones',
           method: 'GET',
           success: function (response) {
               var respuesta = Ext.JSON.decode(response.responseText, true);
               for (var i = 0; i < respuesta.data.length; i++) {
                  var record = {
                     id: respuesta.data[i].id,
                     sesion: {id: respuesta.data[i].sesion.id},
                     tituloVa: respuesta.data[i].sesion.evento.tituloVa,
                     fechaCelebracion: respuesta.data[i].sesion.fechaCelebracion,
                     horaApertura: respuesta.data[i].sesion.horaApertura
                  };

                  gridSesionesAbonos.addItemToStore(record);
               }
           }, failure: function (response) {
              alert(UI.i18n.error.loadingPrecios);
           }
         });
      }
   },

   addSesion: function(button, event, opts) {
      if (this.getGridAbonados().store.data.length > 0 && this.getIdFormAbonos().getValue() > 0) {
        alert(UI.i18n.error.noModificableTieneAbonados);
      }
      else {
        if (this.getComboPlantillaPrecios().value == undefined)
        {
          alert(UI.i18n.error.selectSala);
        }
        else {
          this.getGridSesionesAbonos().deseleccionar();
          this.getGridSesionesAbonos().showAddSesionWindow();
        }
      }
   },

   removeSesion: function(button, event, opts) {
      if (this.getGridAbonados().store.data.length > 0) {
        alert(UI.i18n.error.noModificableTieneAbonados);
      }
      else {
        this.getGridSesionesAbonos().remove();
      }
   },

   loadAbonados: function(selectionModel, record) {
      if (record[0]) {
         var storeAbonados = this.getGridAbonados().getStore();
         var abonoId = record[0].get("id");

         storeAbonados.getProxy().url = urlPrefix + 'abono/' + abonoId + '/abonados';
         storeAbonados.load();
      }
   },
   
   iniciaFormComprar: function() {
      this.idAbonado = null;
      this.idPagoTarjeta = null;
      this.butacasSeleccionadas = [];  
      var layout = this.getFormComprarAbonoCards().getLayout();
      layout.setActiveItem(0);
      this.cambiarEstadoBotonesComprar();
   },

   cerrarComprar: function() {
      this.getFormComprarAbono().up('window').close();  
   },

   comprarAnterior: function() {
      var layout = this.getFormComprarAbonoCards().getLayout();
      var pasoActual = layout.activeItem.id;

      if (pasoActual != 'pasoSeleccionar')
         layout.setActiveItem(layout.getActiveItem()-1);
      this.cambiarEstadoBotonesComprar();
   },

   comprarSiguiente: function() {
      var layout = this.getFormComprarAbonoCards().getLayout();
      var pasoActual = layout.activeItem.id;
  
      if (pasoActual == 'pasoSeleccionar') {
         this.comprarSiguienteNumeradas();
      }
      this.cambiarEstadoBotonesComprar();
   },

   comprarSiguienteNumeradas: function() {
      var me = this;
      
      pm({
        target: window.frames[1],
        type: 'butacas',
        data: {},
        success: function(butacas){
            me.butacasSeleccionadas = butacas;
            me.avanzarAPasoDePago(butacas);
          }
      });
   },

   avanzarAPasoDePago: function (butacas) {
      var layout = this.getFormComprarAbonoCards().getLayout();
      layout.setActiveItem(layout.getNext());
      this.consultaImportes(butacas);
      this.cambiarEstadoBotonesComprar();
   },

   consultaImportes: function(butacas) {
      var me = this;
      var idAbono = this.getGridAbonos().getSelectedRecord().data['id'];
      me.getFormComprarAbono().setLoading(UI.i18n.message.loading);
      me.rellenaDatosPasoPagar("-");
      this.getHiddenTotalPrecio().setValue("0");
      
      Ext.Ajax.request({
           url : urlPrefix + 'compra/' + idAbono + '/importe/abono',
           method: 'POST',
           jsonData: butacas,
           success: function (response) {
              me.getFormComprarAbono().setLoading(false);
              var importe = Ext.JSON.decode(response.responseText, true);
              me.getHiddenTotalPrecio().setValue(importe);
              me.rellenaDatosPasoPagar(importe.toFixed(2));
           }, failure: function (response) {
            me.getFormComprarAbono().setLoading(false);
              var respuesta = Ext.JSON.decode(response.responseText, true);
              
              if (respuesta['message']!=null)
                 alert(respuesta['message']);
              else
                 alert(UI.i18n.error.formSave);
           }
           });
    },

    rellenaDatosPasoPagar: function(importe) {
     Ext.getCmp('total').setText(UI.i18n.field.total + ": " + importe + " €");  
   },

    registraCompra: function() {
     var tipoPago = Ext.getCmp('tipoPago').value;
     
     if (tipoPago == 'tarjetaOffline') {
        var referenciaDePago = this.getReferenciaDePago().getValue().trim();
        if (referenciaDePago == undefined || !referenciaDePago || referenciaDePago == "") {
         alert(UI.i18n.error.errorReferenciaDePago);
         return false;
        }
     }
     this.deshabilitaBotonPagar();
     var idAbono = this.getGridAbonos().getSelectedRecord().data['id'];
     var me = this;
     me.getFormComprarAbono().setLoading(UI.i18n.message.loading);

     Ext.Ajax.request({
          url : urlPrefix + 'compra/' + idAbono + '/abono',
          method: 'POST',
          jsonData: {
                      abonado:
                      {
                        nombre: me.getAbonadoNombre().getValue(),
                        apellidos: me.getAbonadoApellidos().getValue(),
                        direccion: me.getAbonadoDireccion().getValue(),
                        poblacion: me.getAbonadoPoblacion().getValue(),
                        cp: me.getAbonadoCP().getValue(),
                        provincia: me.getAbonadoProvincia().getValue(),
                        telefono: me.getAbonadoTelefono().getValue(),
                        email: me.getAbonadoEmail().getValue(),
                        infoPeriodica: me.getAbonadoInfoPeriodica().getValue(),
                        anulado: false
                      },
                      butacasSeleccionadas: JSON.stringify(this.butacasSeleccionadas)
                    },
          success: function (response) {
             me.getFormComprarAbono().setLoading(false);
             var respuesta = Ext.JSON.decode(response.responseText, true);

             console.log("RECARGA STORE ABONADOS");
             me.getGridAbonados().recargaStore();

             me.idAbonado = respuesta['id'];
             if (tipoPago == 'metalico')
               me.marcaPagada(me.idAbonado);
             else if (tipoPago == 'tarjeta') {
               var abonoNombre = this.getGridAbonos().getSelectedRecord().data['nombre'];;
               me.pagarConTarjeta(me.idAbonado, abonoNombre);
             }
             else if (tipoPago == 'tarjetaOffline')
                me.marcaPagada(me.idAbonado, me.getReferenciaDePago().getValue().trim());
             
          }, failure: function (response) {
            me.getFormComprarAbono().setLoading(false);
            me.habilitaBotonPagar();
            me.muestraMensajePagoTarjeta(UI.i18n.error.errorRegistrandoCompra);
            var respuesta = Ext.JSON.decode(response.responseText, true);
            
            if (respuesta['message']!=null)
              alert(respuesta['message']);
            else
              alert(UI.i18n.error.formSave);
          }
        });  
   },

   habilitaBotonPagar: function()
   {
     if (this.getBotonPagar()!=null)
       this.getBotonPagar().setDisabled(false);
   },

   deshabilitaBotonPagar: function()
   {
     if (this.getBotonPagar()!=null)
       this.getBotonPagar().setDisabled(true);   
   },

   cambiarEstadoBotonesComprar: function () {
      var layout = this.getFormComprarAbonoCards().getLayout();
      Ext.getCmp('comprarAnterior').setDisabled(!layout.getPrev());
      Ext.getCmp('comprarSiguiente').setDisabled(!layout.getNext()); 
   },

   muestraMensajePagoTarjeta: function(mensaje) {
      if (this.getEstadoPagoTarjeta()!=null)
        this.getEstadoPagoTarjeta().setText(mensaje, false);
    },

   pagarConTarjeta: function(id, concepto) {
     var me = this;
     me.getFormComprarAbono().setLoading(UI.i18n.message.loading);
     me.muestraMensajePagoTarjeta(UI.i18n.message.pagoTarjetaEnviando);
     
      Ext.Ajax.request({
        url : urlPrefix + 'pago/abonado/' + id,
        method: 'POST',
        jsonData: concepto,
        success: function (response) {
           me.getFormComprarAbono().setLoading(false);
          var respuesta = Ext.JSON.decode(response.responseText, true);
          
          if (respuesta==null || respuesta.error) {
            var msj = UI.i18n.error.errorRealizaPago;
            
            if (respuesta['mensajeExcepcion'])
              msj += ' (' + respuesta['mensajeExcepcion']  + ')';
            
            alert(msj);
            me.muestraMensajePagoTarjeta(UI.i18n.error.errorRealizaPago);
            me.habilitaBotonPagar();
          }
          else {
            me.idPagoTarjeta = respuesta.codigo;
            me.muestraMensajePagoTarjeta(UI.i18n.message.pagoTarjetaEnviadoLector);
            me.lanzaComprobacionEstadoPago(id);
          }
          
        }, 
        failure: function (response) {
          me.getFormComprarAbono().setLoading(false);
          me.habilitaBotonPagar();
          me.muestraMensajePagoTarjeta(UI.i18n.error.errorRealizaPago);
          var respuesta = Ext.JSON.decode(response.responseText, true);
          
          if (respuesta!=null && respuesta['message']!=null)
            alert(respuesta['message']);
          else
            alert(UI.i18n.error.errorRealizaPago);
        }
      });
   },

   lanzaComprobacionEstadoPago: function(idPago) {
     var me = this;
     this.intervalEstadoPago[idPago] = window.setInterval(function(){me.compruebaEstadoPago(idPago);}, 1000);
   },

   desahiblitaEstadoBotonesComprar: function () {
     Ext.getCmp('comprarAnterior').setDisabled(true);
     Ext.getCmp('comprarSiguiente').setDisabled(true);  
   },

   paraComprobacionEstadoPago: function(idPago) {
     window.clearInterval(this.intervalEstadoPago[idPago]);
     delete this.intervalEstadoPago[idPago];
   },

   compruebaEstadoPago: function(idPago) {
     var me = this;
     me.getFormComprarAbono().setLoading(UI.i18n.message.loading);
     Ext.Ajax.request({
          url : urlPrefix + 'pago/' + idPago,
          method: 'GET',
          success: function (response) {
            me.getFormComprarAbono().setLoading(false);
            var respuesta = Ext.JSON.decode(response.responseText, true);
        
            if (respuesta==null || respuesta.error) {
              me.paraComprobacionEstadoPago(idPago);
              var msj = UI.i18n.error.errorRealizaPago;
              
              if (respuesta!=null && respuesta['mensajeExcepcion'])
                msj += ' (' + respuesta['mensajeExcepcion']  + ')';
              me.muestraMensajePagoTarjeta(msj);
            }
            else {
              if (respuesta['codigoAccion'] == '')
              {
                // El pago está en proceso
              }
              else if (respuesta['codigoAccion'] == '20' || respuesta['codigoAccion'] == '30' ) {
                me.paraComprobacionEstadoPago(idPago);
                me.muestraMensajePagoTarjeta(UI.i18n.message.pagoTarjetaCorrecto);
                me.desahiblitaEstadoBotonesComprar();
              }
              else {
                me.paraComprobacionEstadoPago(idPago);
                me.muestraMensajePagoTarjeta(UI.i18n.error.errorRealizaPago + '<br/>(' + respuesta['codigoAccion'] + ': ' + UI.i18n.error.pinpad[respuesta['codigoAccion']] + ')');
                me.habilitaBotonPagar();
              }
            }
          }, 
          failure: function (response) {
            me.getFormComprarAbono().setLoading(false);
            me.habilitaBotonPagar();
            me.muestraMensajePagoTarjeta(UI.i18n.error.errorRealizaPago);
            var respuesta = Ext.JSON.decode(response.responseText, true);
            
            if (respuesta!=null && respuesta['message']!=null)
              alert(respuesta['message']);
            else
              alert(UI.i18n.error.errorRealizaPago);
          }
        });
   },

   guardarDatosCompraPrevia: function() {
      this.butacasSeleccionadasPrevia = this.butacasSeleccionadas;
      this.idAbonadoPrevio = this.idAbonado;
      this.tituloAbonoPrevio =  this.getGridAbonos().getSelectedRecord().data['nombre'];
   },

   marcaPagada: function(idAbonado, referenciaDePago) {
    var me = this;
     
    Ext.Ajax.request({
      url : urlPrefix + 'compra/' + idAbonado + '/pagada/abonado' + (referenciaDePago != undefined ? "?referencia=" +
      referenciaDePago : ""),
      method: 'POST',
      success: function (response) {
          me.muestraMensajePagoTarjeta(UI.i18n.message.compraRegistradaOk);
          me.desahiblitaEstadoBotonesComprar();
        me.guardarDatosCompraPrevia();
        me.cambiaVisibilidadBotonAnularPrevia();
      }, 
      failure: function (response) {
        me.habilitaBotonPagar();
        me.muestraMensajePagoTarjeta(UI.i18n.error.errorRealizaPago);
        var respuesta = Ext.JSON.decode(response.responseText, true);
        
        if (respuesta!=null && respuesta['message']!=null)
          alert(respuesta['message']);
        else
          alert(UI.i18n.error.errorRealizaPago);
      }
    });
   },

  anularPrevia: function() {
    var textoButacas = "";
    
    for (var i=0; i<this.butacasSeleccionadasPrevia.length; i++) {
      var butaca = this.butacasSeleccionadasPrevia[i];
      var filaNum = "";

      if (butaca['fila']!=null && butaca['numero']!=null)
        filaNum = " fila: " + butaca['fila'] + ", núm: " + butaca['numero'];
      textoButacas += filaNum;
    }
    
    var textoConfirm = UI.i18n.message.anularPreviaIntro + '<br><b>' + this.tituloAbonoPrevio + '</b><br>' + textoButacas;
    
    Ext.Msg.confirm(UI.i18n.formTitle.anularPrevia, textoConfirm, function (id, value) {
      if (id == 'yes')
        this.llamaAnularPrevia();
    }, this);
  },
  
  llamaAnularPrevia: function() {
    var me = this;
    
    Ext.Ajax.request({
          url : urlPrefix + 'compra/abonado/' + this.idAbonadoPrevio,
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
  }
});