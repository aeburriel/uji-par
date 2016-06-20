Ext.define('Paranimf.view.evento.FormSesiones', {
   extend: 'Paranimf.view.EditBaseForm',
   alias: 'widget.formSesiones',

   url : urlPrefix + 'sesiones',
   
   defaults: {
      anchor: '100%',
      xtype: 'textfield',
      labelWidth: 190,
   },

   items: [{
      name: 'id',
      hidden: true,
      allowBlank: true
   }, {
      name: 'rssId',
      hidden: true,
      allowBlank: true
   }, {
      name: 'preciosSesion',
      hidden: true,
      allowBlank: true
   }, {
      xtype: 'fieldset',
      title: UI.i18n.field.dadesGenerals,
      defaults: {
         anchor: '100%',
         labelWidth: 200,
         allowBlank: false
      },
      items: [{
         fieldLabel: UI.i18n.field.eventDate,
         name: 'fechaCelebracion',
         xtype: 'datefield',
         startDay: 1,
         value: new Date()
      }, {
         name: 'horaCelebracion',
         xtype: 'timefield',
         fieldLabel: UI.i18n.field.sessionTime,
         minValue: '8:00 AM',
         maxValue: '23:30 PM',
         format: 'H:i',
         increment: 30
      }, {
         name: 'horaApertura',
         xtype: 'timefield',
         fieldLabel: UI.i18n.field.opening,
         minValue: '8:00 AM',
         maxValue: '23:30 PM',
         format: 'H:i',
         increment: 30,
         allowBlank: true
      }, {
        fieldLabel: UI.i18n.field.sala,
        name: 'sala',
        xtype: 'combobox',
        forceSelection: false,
        displayField: 'nombre',
        valueField: 'id',
        store: 'SalasEvento',
        queryMode: 'local',
        typeAhead: true
     }]
   }, {
      xtype: 'fieldset',
      name: 'reservesOnline',
      title: UI.i18n.field.reservesOnline,
      defaults: {
         anchor: '100%',
         labelWidth: 200,
         allowBlank: false
      },
      items: [ {
    	 fieldLabel: UI.i18n.field.canalInternet,
    	 name: 'canalInternet',
    	 xtype: 'checkboxfield',
    	 inputValue: 1,
    	 uncheckedValue: 0,
    	 checked: true,
    	 allowBlank: true
      },
      {
         fieldLabel: UI.i18n.field.startOnlineSelling,
         name: 'fechaInicioVentaOnline',
         xtype: 'datefield',
         startDay: 1,
         value: new Date()
      }, {
         name: 'horaInicioVentaOnline',
         xtype: 'timefield',
         fieldLabel: UI.i18n.field.horaInicioVentaOnline,
         minValue: '0:00 AM',
         maxValue: '23:00 PM',
         format: 'H:i',
         increment: 30
      }, {
         fieldLabel: UI.i18n.field.endOnlineSelling,
         name: 'fechaFinVentaOnline',
         xtype: 'datefield',
         startDay: 1,
         value: new Date()
      }, {
         name: 'horaFinVentaOnline',
         xtype: 'timefield',
         fieldLabel: UI.i18n.field.horaFinVentaOnline,
         minValue: '0:00 AM',
         maxValue: '23:00 PM',
         format: 'H:i',
         increment: 30
      }]
   }, {
	      xtype: 'fieldset',
	      title: UI.i18n.field.sesionCine,
        name: 'sesionCine',
	      defaults: {
	         anchor: '100%',
	         labelWidth: 200
	      },
	      items: [{
	          fieldLabel: UI.i18n.field.name,
	          name: 'nombre',
	          xtype: 'textfield'
	         },{
	          fieldLabel: UI.i18n.field.versionLinguistica,
	          name: 'versionLinguistica',
	          xtype: 'combobox',
	          displayField: 'name',
	          valueField : 'value',
	          queryMode: 'local',
	          forceSelection:true,
	          store: new Ext.data.SimpleStore({
	            fields: ['value', 'name'],
	              data: [
	                ['S', UI.i18n.versionLinguistica.S],
                  ['B', UI.i18n.versionLinguistica.B],
                  ['R', UI.i18n.versionLinguistica.R],
                  ['Q', UI.i18n.versionLinguistica.Q],
                  ['P', UI.i18n.versionLinguistica.P],
                  ['Z', UI.i18n.versionLinguistica.Z],
                  ['W', UI.i18n.versionLinguistica.W],
                  ['Y', UI.i18n.versionLinguistica.Y],
                  ['V', UI.i18n.versionLinguistica.V],
                  ['U', UI.i18n.versionLinguistica.U],
                  ['T', UI.i18n.versionLinguistica.T],
                  ['F', UI.i18n.versionLinguistica.F],
                  ['X', UI.i18n.versionLinguistica.X]
	              ]
	          })      
	         }
	      ]
   }, {
      fieldLabel: UI.i18n.field.plantillaprecios,
      name: 'plantillaPrecios',
      xtype: 'combobox',
      forceSelection: false,
      displayField: 'nombre',
      valueField: 'id',
      store: 'PlantillasPrecios',
      queryMode: 'local',
      typeAhead: true,
      allowBlank: false
   }, {
      xtype: 'gridPreciosSesion'
   }],
    
    areDatesValid: function() {
        var dateIni = this.getForm().findField('fechaInicioVentaOnline').getValue();
        var dateEnd = this.getForm().findField('fechaFinVentaOnline').getValue();
        
        if (dateIni.valueOf() > dateEnd.valueOf())
           return false;

        return true;
     }
});