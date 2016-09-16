Ext.define('Paranimf.view.abono.GridAbonos', {
   extend: 'Paranimf.view.EditBaseGrid',

   alias: 'widget.gridAbonos',
   store: 'Abonos',

   title: UI.i18n.gridTitle.abonos,
    stateId: 'gridAbonos',

   dockedItems: [{
     xtype: 'pagingtoolbar',
     store: 'Abonos',
     dock: 'bottom',
     displayInfo: true
   }],

   initComponent: function() {

      this.columns = [{
        dataIndex: 'id',
        hidden: true,
        text: UI.i18n.field.idIntern
      }, {
        dataIndex: 'nombre',
        text: UI.i18n.field.nameMulti,
        flex: 2
      }, {
        dataIndex: 'plantillaPrecios_nombre',
        text: UI.i18n.field.plantillaprecios,
        sortable: false,
        flex: 1
      }];

      this.callParent(arguments);
   },

   showAddAbonoWindow: function() {
      this.createPercentageModalWindow('formAbonos', undefined, 0.8).show();
   }
});