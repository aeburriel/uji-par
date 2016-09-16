Ext.define('Paranimf.view.evento.GridSesiones', {
   extend: 'Paranimf.view.EditBaseGrid',

   alias: 'widget.gridSesiones',
   store: 'Sesiones',

   title: UI.i18n.gridTitle.sesiones,
    stateId: 'gridSesiones',

   dockedItems: [{
     xtype: 'pagingtoolbar',
     store: 'Sesiones',
     dock: 'bottom',
     displayInfo: true
   }],

   viewConfig: {
    enableTextSelection: true,
    getRowClass: function(record) {
      if (record && record.data.anulada)
        return 'gridAnulada'
    }
  },

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
    }, {
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
      dataIndex: 'plantillaPrecios_nombre',
      text: UI.i18n.field.plantillaprecios,
      sortable: false,
      flex: 1
    }, {
      dataIndex: 'salaNombre',
      text: UI.i18n.field.sala,
      flex: 1
    }];

    this.callParent(arguments);
  },

  showAddSesionWindow: function() {
    this.createPercentageModalWindow('formSesiones', undefined, 0.8).show();
  }
});