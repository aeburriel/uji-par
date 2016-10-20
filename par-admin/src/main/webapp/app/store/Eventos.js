Ext.define('Paranimf.store.Eventos', {
   extend: 'Ext.data.Store',
   model: 'Paranimf.model.Evento',

   sorters: [{
      property: 'fechaPrimeraSesion',
      direction: 'DESC'
   }],
   autoLoad: false,
   autoSync: true,
   pageSize: 20,
   remoteSort: true,

   proxy: {
      type: 'rest',
      url: urlPrefix + 'evento?activos=true',
      reader: {
         type: 'json',
         root: 'data',
         totalProperty: 'total'
      },
      writer: {
         type: 'json'
      }
   }
});