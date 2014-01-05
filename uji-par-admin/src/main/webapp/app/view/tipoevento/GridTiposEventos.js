Ext.define('Paranimf.view.tipoevento.GridTiposEventos', {
   extend: 'Paranimf.view.EditBaseGrid',

   alias: 'widget.gridTiposEventos',
   store: 'TiposEventos',

   title: UI.i18n.gridTitle.tipoEvento,

   dockedItems: [{
     xtype: 'pagingtoolbar',
     store: 'TiposEventos',
     dock: 'bottom',
     displayInfo: true
   }],

   /*forceFit: true,*/

   initComponent: function() {

      this.columns = [{
         dataIndex: 'id',
         hidden: true,
         text: UI.i18n.field.idIntern
      }, {
         dataIndex: 'nombreEs',
         text: UI.i18n.field.name,
         flex: 5
      }, {
          dataIndex: 'nombreVa',
          text: UI.i18n.field.name_va,
          flex: 5
      }, {
        dataIndex: 'exportarICAA',
        text: UI.i18n.field.exportarICAA,
        flex: 5,
        renderer: function(value) {
          if (value)
            return "Sí";
          return 'No';
        }
      }];

      this.callParent(arguments);
   },


   showAddTipoEventoWindow: function() {
      this.createPercentageModalWindow('formTiposEventos').show();
   }
});