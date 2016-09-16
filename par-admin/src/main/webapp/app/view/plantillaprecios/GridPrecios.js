Ext.define('Paranimf.view.plantillaprecios.GridPrecios', {
   extend: 'Paranimf.view.EditBaseGrid',

   alias: 'widget.gridPrecios',
   store: 'Precios',
    stateId: 'gridPrecios',

   title: UI.i18n.gridTitle.precios,

   dockedItems: [{
     xtype: 'pagingtoolbar',
     store: 'Precios',
     dock: 'bottom',
     displayInfo: true
   }],

   initComponent: function() {

      this.columns = [{
        dataIndex: 'id',
        hidden: true,
        text: UI.i18n.field.idIntern,
        sortable: false
      }, {
        dataIndex: 'plantillaPrecios',
        hidden: true,
        text: UI.i18n.field.plantillaPreusInterna,
        sortable: false
      }, {
        dataIndex: 'localizacion',
        hidden: true,
        text: UI.i18n.field.idLocalitzacio,
        sortable: false
      }, {
        dataIndex: 'tarifa',
        hidden: true,
        text: UI.i18n.field.idTarifa,
        sortable: false
      }, {
        dataIndex: langsAllowed && langsAllowed.length > 1 ? 'parLocalizacione.nombreVa' : 'parLocalizacione.nombreEs',
        text: UI.i18n.field.localizacion,
        flex: 2
      }, {
        dataIndex: 'parTarifa.nombre',
        text: UI.i18n.field.tarifa,
        flex: 2
      }, {
        dataIndex: 'precio',
        text: UI.i18n.field.precio,
        flex: 1,
        renderer: Ext.util.Format.numberRenderer("0.00")
      }];

      this.callParent(arguments);
   },


   showAddPrecioWindow: function() {
      this.createPercentageModalWindow('formPrecios').show();
   }
});