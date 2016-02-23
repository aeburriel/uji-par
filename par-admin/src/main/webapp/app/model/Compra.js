Ext.define('Paranimf.model.Compra', {
   extend: 'Ext.data.Model',

   fields: [
      'id',
      {name: 'fecha', type: 'date', dateFormat: 'U'},
      'nombre',
      'apellidos',
      'email',
      'pagada',
      'reserva',
      'taquilla',
      'telefono',
      'importe',
      'anulada',
      'caducada',
      'uuid',
      {name: 'desde', type: 'date', dateFormat: 'U'},
      {name: 'hasta', type: 'date', dateFormat: 'U'},
      'observacionesReserva',
      'idDevolucion'
   ]
});