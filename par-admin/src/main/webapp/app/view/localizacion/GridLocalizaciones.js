Ext.define('Paranimf.view.localizacion.GridLocalizaciones', {
   extend: 'Paranimf.view.EditBaseGrid',

   alias: 'widget.gridLocalizaciones',
   store: 'Localizaciones',
    stateId: 'gridLocalizaciones',

   title: UI.i18n.gridTitle.localizaciones,

   dockedItems: [{
     xtype: 'pagingtoolbar',
     store: 'Localizaciones',
     dock: 'bottom',
     displayInfo: true
   }],


   initComponent: function() {

      this.columns = [{
         dataIndex: 'id',
         hidden: true,
         text: UI.i18n.field.idIntern
      }, {
          dataIndex: 'codigo',
          text: UI.i18n.field.codigo,
          flex: 5
      },{
          dataIndex: 'nombreEs',
          text: UI.i18n.field.name,
          flex: 5
      }, {
         dataIndex: 'nombreVa',
         text: UI.i18n.field.name_va,
         flex: 5
      }, {
          dataIndex: 'totalEntradas',
          text: UI.i18n.field.totalEntradas,
          flex: 2
      }];

      this.callParent(arguments);
   },


   showAddLocalizacionWindow: function() {
      this.createPercentageModalWindow('formLocalizaciones').show();
   }
});