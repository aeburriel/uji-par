Ext.define('Paranimf.view.compra.GridDetalleCompras', {
  extend: 'Paranimf.view.EditBaseGrid',

  alias: 'widget.gridDetalleCompras',
  title: UI.i18n.gridTitle.detalleCompras,
  store: 'ButacasCompra',
  selModel: {
      mode: 'MULTI'
  },
    stateId: 'gridDetalleCompras',

  tbar:[{
    action: 'anular',
    text: UI.i18n.button.anularEntrada
  }, {
    action: 'cambiar',
    text: UI.i18n.button.cambiarButaca
  }, {
    action: 'passButacaToCompra',
    text: UI.i18n.button.comprarButacaReservada
  }],
   
  dockedItems: [{
    xtype: 'pagingtoolbar',
    store: 'ButacasCompra',
    dock: 'bottom',
    displayInfo: true
  }],

  viewConfig: {
    getRowClass: function(record) {
      if (record && record.data.anulada)
        return 'gridAnulada'
    }
  },


  initComponent: function() {

    this.columns = [{
      dataIndex: 'id',
      text: UI.i18n.field.idIntern,
      hidden: true
    }, {
      dataIndex: 'localizacionNombre',
      text: UI.i18n.field.localizacion,
      flex: 1,
      sortable: false
    }, {
      dataIndex: 'fila',
      text: UI.i18n.field.fila,
      flex: 1,
      renderer: function(val) {
        return (val)?val:UI.i18n.message.noNumerada
      }
    }, {
      dataIndex: 'numero',
      text: UI.i18n.field.numeroSeient,
      flex: 1,
      renderer: function(val) {
        return (val)?val:UI.i18n.message.noNumerada
      }
    }, {
      dataIndex: 'tipo',
      text: UI.i18n.field.tipusEntrada,
      flex: 1
    }, {
      dataIndex: 'precio',
      text: UI.i18n.field.precio,
      flex: 1,
      renderer: function(val) {
        return (val == 0)?'':val.toFixed(2) + '€';
      }
    }, {
      dataIndex: 'presentada',
      text: UI.i18n.field.presentada,
      hidden: true,
      flex: 1,
      renderer: function(val) {
        return (val) ? UI.i18n.message.si : UI.i18n.message.no
      }
    }];
   
    this.callParent(arguments);
   },

  showFormasDePagoWindow: function(callback) {
    this.createPercentageModalWindow('formFormasDePago', undefined, undefined, UI.i18n.formTitle.formasPago, false, callback).show();
  }
});