Ext.define('Paranimf.store.EventosTaquillaAll', {
   extend: 'Ext.data.Store',
   model: 'Paranimf.model.Evento',

   sorters: [{
      property: 'fechaPrimeraSesion',
      direction: 'DESC'
   }],
   autoLoad: false,
   autoSync: false,
   pageSize: 20,
   remoteSort: true,

   proxy: {
      type: 'rest',
      url: urlPrefix + 'evento',
      reader: {
         type: 'json',
         root: 'data',
         totalProperty: 'total'
      }
   }
});