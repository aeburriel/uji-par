Ext.define('Paranimf.store.PreciosSesion', {
   extend: 'Ext.data.Store',
   model: 'Paranimf.model.PrecioSesion',

   sorters: ['parLocalizacione'],
   autoLoad: false,
   autoSync: false,
   pageSize: 100,
   remoteSort: true,

   proxy: {
      type: 'rest',
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