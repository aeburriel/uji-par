Ext.define('Paranimf.view.evento.GridPreciosSesion', {
   extend: 'Paranimf.view.EditBaseGrid',

   alias: 'widget.gridPreciosSesion',
   store: 'PreciosSesion',

   title: UI.i18n.gridTitle.precios,

   initComponent: function() {

      this.columns = [{
        dataIndex: 'id',
        hidden: true,
        sortable: false,
        text: UI.i18n.field.idIntern
      }, {
        dataIndex: 'localizacion_id',
        hidden: true,
        sortable: false,
        text: UI.i18n.field.idLocalitzacio
      }, {
        dataIndex: 'tarifa_id',
        hidden: true,
        sortable: false,
        text: UI.i18n.field.idTarifa
      }, {
        dataIndex: 'parLocalizacione.nombreVa',
        text: UI.i18n.field.localizacion,
        flex: 2,
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


   showAddPrecioSesionWindow: function() {
      this.createPercentageModalWindow('formPreciosSesion').show();
   },

   toJSON: function() {
    var jsonFilas = "";

      for (var i=0;i<this.store.getCount();i++) {
         if (jsonFilas != "")
            jsonFilas += ",";

          jsonFilas += '{';
          jsonFilas += '"precio":' + this.store.getRange()[i].data.precio + ',';
          jsonFilas += '"localizacion":{"id": ' + this.store.getRange()[i].data.localizacion + '},';
          jsonFilas += '"tarifa":{"id": ' + this.store.getRange()[i].data.tarifa + '}';
          jsonFilas += '}';
      }
      jsonFilas = "[" + jsonFilas + "]";
      return jsonFilas;
   }
});