Ext.define('Paranimf.store.TarifasCombo', {
   extend: 'Ext.data.Store',
   model: 'Paranimf.model.Tarifa',

   sorters: ['nombre'],
   autoLoad: false,
   autoSync: true,
   pageSize: 10000,
   remoteSort: true,

   proxy: {
      type: 'rest',
      url: urlPrefix + 'tarifa',
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