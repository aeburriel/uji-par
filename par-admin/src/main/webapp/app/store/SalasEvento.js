Ext.define('Paranimf.store.SalasEvento', {
   extend: 'Ext.data.Store',
   model: 'Paranimf.model.Sala',

   sorters: ['nombre'],
   autoLoad: false,
   autoSync: false,
   pagesize: 1000,

   proxy: {
      type: 'rest',
      url: urlPrefix + 'sala/evento/',
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