Ext.define('Paranimf.model.Precio', {
   extend: 'Ext.data.Model',

   fields: [
    	'id', 
	  	{name: 'plantillaPrecios', mapping: 'plantillaPrecios.id'},
	    {name: 'parLocalizacione.nombreEs', mapping: 'localizacion.nombreEs'},
	  	{name: 'parLocalizacione.nombreVa', mapping: 'localizacion.nombreVa'},
	  	{name: 'localizacion', mapping: 'localizacion.id'},
	  	//'localizacion',
	  	'precio',
	  	{name: 'parTarifa.nombre', mapping: 'tarifa.nombre'},
	  	{name: 'tarifa', mapping: 'tarifa.id'}
	]
});