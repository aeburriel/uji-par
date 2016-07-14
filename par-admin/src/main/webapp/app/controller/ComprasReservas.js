Ext.define('Paranimf.controller.ComprasReservas', {
  extend: 'Ext.app.Controller',

  views: ['EditModalWindow', 'EditBaseForm', 'EditBaseGrid', 'compra.PanelComprasReservas', 'compra.GridEventosComprasReservas', 'compra.GridSesionesComprasReservas', 'compra.PanelCompras', 'compra.FormCambiaButaca', 'compra.GridCompras', 'compra.GridDetalleCompras', 'compra.FormFormasDePago'],
  stores: ['Compras', 'EventosTaquillaAll', 'SesionesTaquillaAll', 'ButacasCompra', 'ButacasLibres'],
  models: ['Compra', 'Evento', 'Sesion', 'Butaca'],

  refs: [
    {
      ref: 'gridCompras',
      selector: 'gridCompras'
    },
    {
      ref: 'gridDetalleCompras',
      selector: 'gridDetalleCompras'
    },
    {
     	ref: 'panelCompras',
      selector: 'panelCompras'
    },
    {
  	  ref: 'botonVerCompras',
      selector: 'formComprar #verCompras'
    },
    {
      ref: 'gridEventosComprasReservas',
      selector: 'gridEventosComprasReservas'
    },
    {
      ref: 'gridSesionesComprasReservas',
      selector: 'gridSesionesComprasReservas'
    }, {
      ref: 'formCambiaButaca',
      selector: 'formCambiaButaca'
    }, {
      ref: 'idCambiaButaca',
      selector: 'formCambiaButaca textfield[name=id]'
    }, {
      ref: 'comboButacasLibres',
      selector: 'formCambiaButaca combo[name=butaca]'
    }, {
      ref: 'checkboxAnuladas',
      selector: 'gridCompras checkbox[action=showAnuladas]'
    }, {
      ref: 'checkboxOnline',
      selector: 'gridCompras checkbox[action=showOnline]'
    }, {
      ref: 'pagingToolbar',
      selector: 'gridCompras pagingtoolbar'
    }, {
      ref: 'buscadorCompras',
      selector: 'panelCompras textfield[name=buscadorCompras]'
    }, {
      ref: 'btAnular',
      selector: 'gridCompras button[action=anular]'
    }, {
      ref: 'btDesanular',
      selector: 'gridCompras button[action=desanular]'
    }, {
      ref: 'btPassToCompra',
      selector: 'gridCompras button[action=passToCompra]'
    }, {
      ref: 'btAnularDetalle',
      selector: 'gridDetalleCompras button[action=anular]'
    }, {
      ref: 'btCambiarButacaDetalle',
      selector: 'gridDetalleCompras button[action=anular]'
    }, {
      ref: 'pagingToolbarDetalle',
      selector: 'gridDetalleCompras pagingtoolbar'
    }, {
      ref: 'formFormasDePago',
      selector: 'formFormasDePago'
    }, {
      ref: 'comboTipoPago',
      selector: 'formFormasDePago combobox[name=tipoPago]'
    }, {
      ref: 'txtReciboPago',
      selector: 'formFormasDePago textfield[name=referenciaDePago]'
    }
  ],

  init: function() {
    this.control({
      'panelComprasReservas': {
        afterrender: this.loadEventos
      },

      'gridEventosComprasReservas': {
        selectionchange: this.loadSesiones
      },
      
      'gridSesionesComprasReservas': {
          itemdblclick: this.verCompras
      },      
      
      'gridSesionesComprasReservas button[action=verCompras]': {
        click: this.verCompras
      },

      'panelCompras': {
        afterrender: this.loadCompras
      },                     

      'panelCompras button[action=close]': {
        click: this.cerrarVerCompras
      },

      'gridCompras button[action=anular]': {
        click: this.anularCompraReserva
      },
      
      'gridCompras button[action=desanular]': {
        click: this.desanularCompraReserva
      },

      'gridCompras button[action=passToCompra]': {
        click: this.passarReservaACompra
      },

      'gridCompras': {
        selectionchange: this.loadButacas
      },

      'gridCompras checkbox[action=showAnuladas]': {
        change: this.showAnuladas
      },

      'gridCompras checkbox[action=showOnline]': {
        change: this.showOnline
      },

      'gridCompras pagingtoolbar': {
        beforechange: this.doRefresh
      },

      'gridDetalleCompras button[action=cambiar]': {
        click: this.cambiaButaca
      },

      'gridDetalleCompras button[action=anular]': {
        click: this.anularButacas
      },

      'panelCompras button[action=search]': {
        click: this.buscarCompra
      },

      'panelCompras textfield[name=buscadorCompras]': {
        specialkey: this.buscarCompraConEnter
      },

      'formCambiaButaca': {
        afterrender: this.butacasDisponibles
      },

      'formCambiaButaca button[action=save]': {
        click: this.saveCambiaButaca
      },

      'formFormasDePago button[action=save]': {
        click: this.doPassarReservaACompra
      },

      'formFormasDePago combobox[name=tipoPago]': {
        change: this.changeFormaPago
      }
    });     
  },

  buscarCompraConEnter: function(field, e) {
    if (e.getKey() == e.ENTER)
      this.buscarCompra();
  },

  buscarCompra: function() {
    var idSesion = this.getGridSesionesComprasReservas().getSelectedColumnId();
    this.getGridCompras().store.getProxy().url = urlPrefix + 'compra/' + idSesion + '?search=' + this.getBuscadorCompras().value,
    this.getGridCompras().store.load(function(records, operation, success) {

    })
  },

  doRefresh: function() {
    this.setStoreCompras();
  },

  showAnuladas: function(checkbox, newValue, oldValue) {
    this.setStoreCompras();
    console.log("showAnuladas");
    this.getPagingToolbar().moveFirst();
  },

  showOnline: function(checkbox, newValue, oldValue) {
    this.setStoreCompras();
    console.log("showOnline");
    this.getPagingToolbar().moveFirst();
  },

  anularCompraReserva: function() {
    if (!this.getGridCompras().hasRowSelected())
      alert(UI.i18n.message.selectRow);
    else {
      if (confirm(UI.i18n.message.sureAnular)) {
        var idSesion = this.getGridSesionesComprasReservas().getSelectedColumnId();
        var idCompra = this.getGridCompras().getSelectedColumnId();
        var me = this;
        this.getGridCompras().setLoading(UI.i18n.message.loading);

        Ext.Ajax.request({
          url : urlPrefix + 'compra/' + idSesion + '/' + idCompra,
          method: 'PUT',
          success: function (response) {
            me.setStoreCompras();
            me.getGridCompras().setLoading(false);
            me.getGridCompras().deseleccionar();
            me.getGridCompras().getStore().load();
          }, failure: function (response) {
            me.getGridCompras().setLoading(false);
            alert(UI.i18n.error.anularCompraReserva);
          }
        });
      }
    }
  },
  
  desanularCompraReserva: function() {
    if (!this.getGridCompras().hasRowSelected())
      alert(UI.i18n.message.selectRow);
    else {
      if (confirm(UI.i18n.message.sureDesanular)) {
        var idSesion = this.getGridSesionesComprasReservas().getSelectedColumnId();
        var idCompra = this.getGridCompras().getSelectedColumnId();
        var me = this;
        this.getGridCompras().setLoading(UI.i18n.message.loading);

        Ext.Ajax.request({
          url : urlPrefix + 'compra/' + idSesion + '/desanuladas/' + idCompra,
          method: 'PUT',
          success: function (response) {
            me.setStoreCompras();
            me.getGridCompras().setLoading(false);
            me.getGridCompras().deseleccionar();
            me.getGridCompras().getStore().load();
          }, failure: function (response) {
        	  
            me.getGridCompras().setLoading(false);
            
            var resp = Ext.JSON.decode(response.responseText, true);
            
            if (resp['message'])
            	alert(resp['message']);
            else	
            	alert(UI.i18n.error.desanularCompraReserva);
          }
        });
      }
    }
  },

  passarReservaACompra: function() {
    if (!this.getGridCompras().hasRowSelected())
      alert(UI.i18n.message.selectRow);
    else {
      var selectedRecord = this.getGridCompras().getSelectedRecord();
      
      if (selectedRecord.data.reserva == true)
        this.getGridCompras().showFormasDePagoWindow();
    }
  },

  changeFormaPago: function(combo, val, oldVal) {
    this.getTxtReciboPago().allowBlank = (val === 'tarjetaOffline');
    if (val === 'tarjetaOffline')
      this.getTxtReciboPago().show();
    else
      this.getTxtReciboPago().hide();
  },

  doPassarReservaACompra: function() {
    var tipoPago = this.getComboTipoPago().value;
    var txtRecibo = this.getTxtReciboPago().value;

    if (tipoPago != undefined && tipoPago !== 0 && tipoPago !== '') {
      
      if ((tipoPago == 'tarjetaOffline' && txtRecibo != undefined && txtRecibo != '') || tipoPago != 'tarjetaOffline') {
        txtRecibo = (tipoPago == 'tarjetaOffline') ? txtRecibo : '';
        var selectedRecord = this.getGridCompras().getSelectedRecord();
        
        if (selectedRecord.data.reserva == true) {
          
          if (confirm(UI.i18n.message.surePassarACompra)) {
            var idSesion = this.getGridSesionesComprasReservas().getSelectedColumnId();
            var idCompra = this.getGridCompras().getSelectedColumnId();
            var me = this;
            this.getGridCompras().setLoading(UI.i18n.message.loading);

            Ext.Ajax.request({
              url : urlPrefix + 'compra/' + idSesion + '/passaracompra/' + idCompra + '?tipopago=' + tipoPago + '&recibo=' + txtRecibo,
              method: 'PUT',
              success: function (response) {
                me.setStoreCompras();
                me.getGridCompras().setLoading(false);
                me.getGridCompras().deseleccionar();
                me.getGridCompras().getStore().load();
                me.getFormFormasDePago().up('window').close();
              }, failure: function (response) {
                me.getGridCompras().setLoading(false);
                var resp = Ext.JSON.decode(response.responseText, true);
                
                if (resp['message'])
                  alert(resp['message']);
                else  
                  alert(UI.i18n.error.passarReservaCompra);
              }
            });
          }
        } else
          alert(UI.i18n.error.compraNoReserva);
      }
    }
  },

  cambiaButaca: function() {
    var sesion = this.getGridSesionesComprasReservas().getSelectedRecord();
    var today = new Date();
    if (sesion.data.fechaCelebracion > today)
    {
      if (!this.getGridDetalleCompras().hasRowSelected()) {
        alert(UI.i18n.message.selectRow);
      }
      else {
        var record = this.getGridDetalleCompras().getSelectedRecord();
        if (record.data.fila && record.data.numero)
        {
          if (confirm(UI.i18n.message.cambiarButacaSoloLocalizacion)) {
            this.getGridDetalleCompras().edit('formCambiaButaca');
          }
        }
        else {
          alert(UI.i18n.message.noNumerada);
        }
      }
    }
    else {
      alert(UI.i18n.message.sesionCelebrada);
    }
  },

  butacasDisponibles: function () {
    var record = this.getGridDetalleCompras().getSelectedRecord();
    if (record) {
      this.getComboButacasLibres().getStore().getProxy().url = urlPrefix + 'butacas/' + record.data.id + '/libres/' + record.data.localizacion;
    }
  },

  saveCambiaButaca: function() {
    if (this.getComboButacasLibres().getValue()) {
      var valorCambioFilaButaca = this.getComboButacasLibres().getValue().split('#');
      if (valorCambioFilaButaca.length == 2)
      {
        var fila = valorCambioFilaButaca[0];
        var numero = valorCambioFilaButaca[1];
        var idCambiaButaca = this.getIdCambiaButaca().getValue();

        var me = this;

        if (idCambiaButaca && fila && numero) {
          Ext.Ajax.request({
            url : urlPrefix + 'butacas/' + idCambiaButaca + '/cambia/' + fila + '/' + numero,
            method: 'POST',
            success: function (response) {
              me.getFormCambiaButaca().up("window").close();
              me.getGridDetalleCompras().setLoading(false);
              me.getGridDetalleCompras().deseleccionar();
              me.getGridDetalleCompras().getStore().load();
            }, failure: function (response) {
              me.getGridDetalleCompras().setLoading(false);
              alert(UI.i18n.error.cambiarButaca);
            }
          });
        }
      }
    }
  },

  anularButacas: function() {
    if (!this.getGridDetalleCompras().hasRowSelected())
      alert(UI.i18n.message.selectRow);
    else {
      if (confirm(UI.i18n.message.sureAnular)) {
        var idSesion = this.getGridSesionesComprasReservas().getSelectedColumnId();
        var idsButacas = this.getGridDetalleCompras().getSelectedColumnIds();
        
        var me = this;
        this.getGridDetalleCompras().setLoading(UI.i18n.message.loading);

        Ext.Ajax.request({
          url : urlPrefix + 'compra/' + idSesion + '/butacas/anuladas',
          jsonData: idsButacas,
          method: 'PUT',
          success: function (response) {
            me.getGridDetalleCompras().setLoading(false);
            me.getGridDetalleCompras().deseleccionar();
            me.getGridDetalleCompras().getStore().load();
          }, failure: function (response) {
            me.getGridDetalleCompras().setLoading(false);
            alert(UI.i18n.error.anularEntrada);
          }
        });
      }
    }
  },

  muestraBotonesGrid: function(readOnlyUser) {
    if (readOnlyUser == undefined || !readOnlyUser) {
      this.getBtAnular().show();
      this.getBtAnularDetalle().show();
      this.getBtDesanular().show();
      this.getBtPassToCompra().show();
    } else {
      this.getBtAnular().hide();
      this.getBtAnularDetalle().hide();
      this.getBtDesanular().hide();
      this.getBtPassToCompra().hide();
    }
  },

  verCompras: function(button, event, opts) {
    if (this.getGridEventosComprasReservas().hasRowSelected() && this.getGridSesionesComprasReservas().hasRowSelected()) {
      var evento = this.getGridEventosComprasReservas().getSelectedRecord();
      var sesion = this.getGridSesionesComprasReservas().getSelectedRecord();
      this.getGridSesionesComprasReservas().showVerComprasWindow();
      this.muestraBotonesGrid(readOnlyUser);
    } else
      alert(UI.i18n.message.selectRow);
  },

  loadEventos: function() {
    if (this.getGridEventosComprasReservas().getStore().count() == 0)
      this.getGridEventosComprasReservas().getStore().load();
  },

  loadSesiones: function(selectionModel, record) {
    if (record[0]) {
      var storeSesiones = this.getGridSesionesComprasReservas().getStore();
      var eventoId = record[0].get('id');
      
      this.getGridSesionesComprasReservas().setTitle(UI.i18n.gridTitle.sesionesCompras + ': ' + record[0].get('tituloEs'));

      storeSesiones.getProxy().url = urlPrefix + 'evento/' + eventoId + '/sesiones';
      storeSesiones.load();
    }
  },

  setStoreCompras: function() {
    var storeCompras = this.getGridCompras().getStore();
    var me = this;
    storeCompras.getProxy().url = urlPrefix + 'compra/' + this.getGridSesionesComprasReservas().getSelectedColumnId();
    storeCompras.getProxy().setExtraParam("showAnuladas", (this.getCheckboxAnuladas().value)?1:0);
    storeCompras.getProxy().setExtraParam("showOnline", (this.getCheckboxOnline().value)?1:0);
  },

  loadCompras: function() {
    this.getGridDetalleCompras().getStore().removeAll();
    this.setStoreCompras();
    this.getPagingToolbar().moveFirst();
    this.getGridCompras().store.load();
  },

  loadButacas: function(selectionModel, record) {
    if (record[0]) {
      var storeButacas = this.getGridDetalleCompras().getStore();
      var compraid = record[0].get('id');

      storeButacas.getProxy().url = urlPrefix + 'compra/' + compraid + '/butacas';
      this.getPagingToolbarDetalle().moveFirst();
    }
  }
});