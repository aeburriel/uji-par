Ext.define('Paranimf.controller.Tarifas', {
   extend: 'Ext.app.Controller',

   views: ['EditModalWindow', 'EditBaseForm', 'EditBaseGrid', 'tarifa.GridTarifas', 'tarifa.FormTarifas', 'tarifa.PanelTarifas'],
   stores: ['Tarifas', 'TarifasCombo'],
   models: ['Tarifa'],

   refs: [{
      ref: 'gridTarifas',
      selector: 'gridTarifas'
   }, {
      ref: 'formTarifas',
      selector: 'formTarifas'
   }],

   init: function() {
      this.control({

         'panelTarifas': {
            afterrender: this.recargaStore
         },

         'gridTarifas button[action=add]': {
            click: this.addTarifa
         },

         'gridTarifas button[action=edit]': {
            click: this.editLocalizacion
         },

         'gridTarifas button[action=del]': {
            click: this.removeLocalizacion
         },

         'gridTarifas': {
			   beforeactivate: this.recargaStore
         },

         'formTarifas button[action=save]': {
            click: this.saveLocalizacionesFormData
         }
      });
   },

   recargaStore: function(comp, opts) {
      console.log("RECARGA STORE TARIFAS");
      this.getGridTarifas().recargaStore();
   },

   addTarifa: function(button, event, opts) {
      this.getGridTarifas().showAddTarifaWindow();
   },

   editLocalizacion: function(button, event, opts) {
      this.getGridTarifas().edit('formTarifas');
   },

   removeLocalizacion: function(button, event, opts) {
      this.getGridTarifas().remove();
   },

   saveLocalizacionesFormData: function(button, event, opts) {
      var grid = this.getGridTarifas();
      var form = this.getFormTarifas();
      form.saveFormData(grid, urlPrefix + 'tarifa');
   }
});