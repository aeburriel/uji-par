Ext.define('Paranimf.controller.GenerarFicheros', {
   extend: 'Ext.app.Controller',

   views: ['EditModalWindow', 'EditBaseForm', 'EditBaseGrid', 'generarficheros.PanelSesiones', 'generarficheros.GridSesionesCompleto', 'generarficheros.FormDatosFichero'],
   stores: ['SesionesFicheros', 'TipoEnvio'],
   models: ['Sesion'],

   refs: [{
	   ref: 'gridSesionesCompleto',
	   selector: 'gridSesionesCompleto'
   }, {
      ref: 'txtFechaInicial',
      selector: 'panelSesionesFicheros datefield[name=fechaInicio]'
   }, {
      ref: 'txtFechaFinal',
      selector: 'panelSesionesFicheros datefield[name=fechaFin]'
   }, {
      ref: 'formDatosFichero',
      selector: 'formDatosFichero'
   }, {
      ref: 'fechaUltimoEnvioHabitual',
      selector: 'formDatosFichero datefield[name=fechaUltimoEnvioHabitual]'
   }, {
      ref: 'tipoEnvio',
      selector: 'formDatosFichero combobox[name=tipoEnvio]'
   }],

   init: function() {
      this.control({
         'panelSesionesFicheros button[name=filtrar]': {
            click: this.filtrarSesiones
         },

         'gridSesionesCompleto button[action=saveFileICAA]': {
            click: this.showOpcionsFileICAA
         },

         'formDatosFichero button[action=save]': {
            click: this.saveFileICAA
         }
      });
   },

   showOpcionsFileICAA: function() {
      var idsSelected = this.getGridSesionesCompleto().getSelectedColumnIds();
      if (idsSelected && idsSelected.length > 0) {
         this.getGridSesionesCompleto().showDatosFicheroForm();
      } else
         alert(UI.i18n.message.noRowSelectedFileICAA);
   },

   saveFileICAA: function() {
      if (this.getFormDatosFichero().isValid()) {
         var me = this;
         var idsSelected = this.getGridSesionesCompleto().getSelectedColumnIds();
         var fechaEnvioHabitualAnterior = this.getFechaUltimoEnvioHabitual().rawValue;
         var tipoEnvio = this.getTipoEnvio().value;

         this.getFormDatosFichero().submit({target: '_blank'});
         this.getFormDatosFichero().submit({
            standardSubmit: true,
            url : urlPrefix + 'comunicacionesicaa',
            method: 'POST',
            timeout: 120000,
            params: {
               ids: idsSelected,
               fechaEnvioHabitualAnterior: fechaEnvioHabitualAnterior,
               tipoEnvio: tipoEnvio
            }
         });
      }
   },

   filtrarSesiones: function() {
      if (this.getTxtFechaInicial().rawValue || this.getTxtFechaFinal().rawValue) {
         var storeSesiones = this.getGridSesionesCompleto().getStore();

         storeSesiones.getProxy().url = urlPrefix + 'evento/sesionesficheros';
         storeSesiones.getProxy().extraParams = {
            'fechaInicio': this.getTxtFechaInicial().rawValue,
            'fechaFin': this.getTxtFechaFinal().rawValue
         };
         storeSesiones.load();
      }
   }
});