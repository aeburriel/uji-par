Ext.define('Paranimf.view.compra.GridSesionesComprasReservas', {
  extend: 'Paranimf.view.EditBaseGrid',

  alias: 'widget.gridSesionesComprasReservas',
  store: 'SesionesTaquillaAll',

  title: UI.i18n.gridTitle.sesionesCompras,
    stateId: 'gridSesionesComprasReservas',

  dockedItems: [{
    xtype: 'pagingtoolbar',
    store: 'SesionesTaquillaAll',
    dock: 'bottom',
    displayInfo: true
  }],
   
  tbar: [{
    xtype: 'button',
  	text: UI.i18n.button.verCompras,
  	action: 'verCompras'
  }],   
   
  initComponent: function() {
    this.columns = [{
      dataIndex: 'id',
      hidden: true,
      text: UI.i18n.field.idIntern
    }, {
      hidden: true,
      dataIndex: 'horaCelebracion',
      text: UI.i18n.field.sessionTime
    }, {
      dataIndex: 'fechaCelebracion',
      text: UI.i18n.field.eventDate,
      format:'d/m/Y H:i',
      xtype: 'datecolumn',
      flex: 1
    }, {
      dataIndex: 'horaApertura',
      text: UI.i18n.field.opening,
      flex: 1
    },{
      dataIndex: 'fechaInicioVentaOnline',
      text: UI.i18n.field.startOnlineSelling,
      xtype: 'datecolumn',
      flex: 1,
      renderer: function (val, p, rec) {
        return (!rec.data.canalInternet)?'-':Ext.util.Format.date(val, 'd/m/Y');
      }
    }, {
      dataIndex: 'fechaFinVentaOnline',
      text: UI.i18n.field.endOnlineSelling,
      xtype: 'datecolumn',
      flex: 1,
      renderer: function (val, p, rec) {
        return (!rec.data.canalInternet)?'-':Ext.util.Format.date(val, 'd/m/Y');
      }
    }, {
      dataIndex: 'butacasVendidas',
      text: UI.i18n.field.butacasVendidas,
      flex: 1
    }, {
      dataIndex: 'butacasReservadas',
      text: UI.i18n.field.butacasReservadas,
      flex: 1
    }];

    this.callParent(arguments);
    
    //this.getDockedItems('toolbar[dock=top]')[0].hide();
  },

  showVerComprasWindow: function() {
    this.createPercentageModalWindow('panelCompras', undefined, 0.95, UI.i18n.gridTitle.compras, false).show();
  }
});