Ext.define('Paranimf.model.Evento', {
   extend: 'Ext.data.Model',

   fields: [
      'id', 
      'tituloEs',
      'tituloVa',
      'descripcionEs',
      'descripcionVa',
      'companyiaEs',
      'companyiaVa',
      'interpretesEs',
      'interpretesVa',
      'duracionEs',
      'duracionVa',
      'premiosEs',
      'premiosVa',
      'caracteristicasEs',
      'caracteristicasVa',
      'comentariosEs',
      'comentariosVa',
      'tipoEvento',
      'promotor',
      'nifPromotor',
      
      'parTiposEvento',
      {name: 'tpv', mapping: 'parTpv.id'},
      'parTpv',
      {name: "multipleTpv", type:"boolean"},
      
      'dataBinary',
      'dataBinaryDetail',
      'imagenContentType',
      'imagenSrc',

      'asientosNumerados',
      'porcentajeIVA',
      'retencionSGAE',
      'ivaSGAE',
      'fechaPrimeraSesion',
      //{name: 'fechaPrimeraSesion', type: 'date', dateFormat: 'U'},

      {name: 'fechaInicio', type: 'date'},
      {name: 'fechaFin', type: 'date'},
      
      'rssId',
      
      'expediente',
      'codigoDistribuidora',
      'nombreDistribuidora',
      'nacionalidad',
      'vo',
      'formato',
      'metraje',
      'subtitulos',
      'multisesion',

      //Multisesión
      'versionLinguistica'
   ]
});