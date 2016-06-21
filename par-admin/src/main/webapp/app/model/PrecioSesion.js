Ext.define('Paranimf.model.PrecioSesion', {
   extend: 'Ext.data.Model',

   fields: [
    	'id', 
	  	'plantillaPrecios',
	  	{name: 'parLocalizacione.nombreVa', mapping: 'localizacion.nombreVa'},
	    {name: 'parLocalizacione.nombreEs', mapping: 'localizacion.nombreEs'},
	  	{name: 'localizacion_id', mapping: 'localizacion.id'},
        {name: 'totalEntradas', mapping: 'localizacion.totalEntradas'},
	  	'precio',
	  	'sesion_id',
	  	{name: 'parTarifa.nombre', mapping: 'tarifa.nombre'},
	  	{name: 'tarifa_id', mapping: 'tarifa.id'}
	]
});