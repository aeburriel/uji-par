Ext.define('Paranimf.view.taquilla.FormComprar', {
   extend: 'Paranimf.view.EditBaseForm',
   alias: 'widget.formComprar',
   layout: 'fit',

   url : urlPrefix + 'sesiones',
   
   defaults: {
      anchor: '100%',
      xtype: 'textfield',
      labelWidth: 190
   },
   
   buttons: undefined,
   
   bbar: [{
	   id: 'anularPrevia',
	   text: UI.i18n.button.anularPrevia
   }, 
   '->',
   {
	   id: 'comprarAnterior',
	   text: UI.i18n.button.anterior
   },{
	   id: 'comprarSiguiente',
	   text: UI.i18n.button.siguiente
   },{
	   id: 'comprarCancelar',
	   text: UI.i18n.button.close
   }],

   items: [{
	   xtype: 'panel',
	   id: 'formComprarCards',
	   frame: false,
	   layout: 'card',
	   border: false,
	   autoScroll: true,
	   items: [{
		        id    : 'pasoSeleccionar',
		        xtype : 'panel',
		   		autoScroll: true,
		        layout: 'card',
		        border: false,
		        frame: false,
		        items: [{
			        id    : 'iframeButacas',
			        xtype : 'component',
			        autoEl : {
			            tag : 'iframe',
			            src : ''
			        }
		        },
		        {
		        	border: false,
		        	frame: false,
			        id    : 'noNumeradas',
			        xtype: 'panelSeleccionarNoNumeradas'
			    }]
		   },
	       {
		       	id: 'pasoPagar',
			   	autoScroll: true,
		       	xtype: 'panel',
		       	layout: {
		       		type: 'vbox',
		       		align: 'center',
		       		pack: 'start'
		       	},
		       	border: 0,
		       	frame: false,
		       	items: [
		       	        {
		       	        	name: 'panelComprar',
		       	        	xtype: 'panel',
							autoScroll: true,
		       	        	region: 'center',
		   					frame: false,
		   					align: 'middle',
		   					border: 0,
	   					 	layout: {
			   				    align: 'center',
			   					pack: 'start',
			   					type: 'vbox'
			   				},
			   				defaults: {
			   				 	frame: false,
			   				 	border: 0,
			   				 	margin: '10px',
			   				 	style: {
			   				 		fontSize: '20px'
			   				 	},
			   				 	fieldStyle: {
			   				 		fontSize: '20px'
			   				 	}
			   				},
		       	        	items: [{
								fieldLabel: UI.i18n.field.tipoPago,
							    id: 'tipoPago',
							    xtype: 'combobox',
							    displayField: 'name',
							    valueField : 'value',
							    queryMode: 'local',
							    //value: 'metalico',
							    typeAhead: false,
							    editable: false,
							    allowBlank: false,
							    forceSelection:true,
							    store: new Ext.data.SimpleStore({
							      fields: ['value', 'name'],
							        data: payModes
							    })
							 }, {
								xtype: 'label',
							    id: 'total'
							}, {
								xtype: 'hiddenfield',
								name: 'hiddenTotalPrecio'
							}, {
							 	xtype: 'numberfield',
							 	name: 'importePagado',
							 	decimalSeparator: '.',
							 	fieldLabel: UI.i18n.field.importePagado
							}, {
								name: 'dineroADevolver',
								xtype: 'label',
								text: UI.i18n.field.importeADevolver
							}, {
								name: 'referenciaDePago',
								xtype: 'textfield',
								allowBlank: false,
								hidden: true,
								fieldLabel: UI.i18n.field.referenciaDePago
							}, {
								xtype: 'button',
								id: 'pagar',
								scale: 'large',
								text: UI.i18n.button.pagar
							}, {
							    xtype: 'label',
								name: 'estadoPagoTarjeta'
							}, {
					    	 	xtype: 'button',
						    	name: 'verEntrada',
								scale: 'large',
								text: UI.i18n.button.verEntrada,
								hidden: true
						    }/*, {
					    	 	xtype: 'button',
						    	name: 'printEntradaDirectamente',
								scale: 'large',
								text: UI.i18n.button.printEntradaDirectamente,
								hidden: true
						    }*/
				     	]},
		       	        {
		       	        	name: 'panelReservar',
		       	        	xtype: 'panel',
							autoScroll: true,
		   					frame: false,
		   					border: 0,
		    		       	defaults: {
	   					 		frame: false,
			   					border: 0
		    		       	},		       	        	
		       	        	items: [{
		       	        		layout: {
			   					        align: 'middle',
			   					        pack: 'center',
			   					        type: 'vbox'
			   					},
		       	        	    xtype: 'panel',
		       	        	    items: [
		       	        	        {
										xtype: 'panel',
										layout: 'hbox',
									 	frame: false,
									 	border: 0,																 
										items: [
											{
												xtype: 'panel',
									        	layout: 'vbox',
							 					frame: false,
							 					border: 0,		       	        	        		        	 
									        	items: [
								        	    	{
								        	    		xtype: 'label',
								        	        	text: UI.i18n.field.desde
								        	        },
								        	        {
								        	        	xtype: 'datepicker',
								        	        	name: 'desde'
								        	        }		       	     		       	        	        	 		
									        	]
									        },
								         	{
								        		xtype: 'panel',
								        	 	layout: 'vbox',
						 					 	frame: false,
						 					 	border: 0,		 
											 	style: {
												   	marginLeft: '30px'
											 	},
								        	 	items: [
							        	        	{
							        	        	 	xtype: 'label',
							        	        	 	text: UI.i18n.field.hasta
							        	        	},
							        	        	{
							        	        		xtype: 'datepicker',
							        	        		name: 'hasta'
							        	        	}		       	     		       	        	        	 		
								        	 	]
								         	}		       	        	        		         
										]
									},

									{
		       	        	        	layout: 'hbox',
		       	        	        	margin: '30 0 0 0',
		       	        	        	border: 0,
		       	        	        	items: [{
		       	        	        		width: 170,
					       	        		xtype: 'combobox',
					       	        		forceSelection: false,
					       	        		queryMode: 'local',
					       	        		name: 'horaInicio',
					       	        	    store: 'Horas',
					       	        	    displayField: 'label',
					       	        	    valueField: 'id',
					       	        	    fieldLabel: UI.i18n.field.horaInicial,
					       	        	    allowBlank: false,
					       	        	    style: {
					       	        	    	marginRight: '20px'
					       	        	    }
					       	        	},{
					       	        		width: 170,
		       	        	        		xtype: 'combobox',
		       	        	        		forceSelection: false,
		       	        	        		queryMode: 'local',
		       	        	        		name: 'minutoInicio',
		       	        	        		allowBlank: false,
		       	        	        		store: 'Minutos',
		       	        	        		displayField: 'label',
		       	        	        		valueField: 'id',
		       	        	        		fieldLabel: UI.i18n.field.minutos
		       	        	        	}]
		       	        	        },

		       	        	        {
		       	        	        	layout: 'hbox',
		       	        	        	margin: '30 0 0 0',
       	        	        			border: 0,
       	        	        			items: [{
       	        	        				width: 170,
		       	        	        		xtype: 'combobox',
		       	        	        		name: 'horaFin',
		       	        	        		forceSelection: false,
		       	        	        		allowBlank: false,
		       	        	        		store: 'Horas',
		       	        	        		displayField: 'label',
		       	        	        		queryMode: 'local',
		       	        	        		valueField: 'id',
		       	        	        		fieldLabel: UI.i18n.field.horaFinal,
		       	        	        		style: {
					       	        	    	marginRight: '20px'
					       	        	    }
			       	        	        }, {
			       	        	        	width: 170,
		       	        	        		xtype: 'combobox',
		       	        	        		forceSelection: false,
		       	        	        		name: 'minutoFin',
		       	        	        		queryMode: 'local',
		       	        	        		allowBlank: false,
		       	        	        		store: 'Minutos',
		       	        	        		displayField: 'label',
		       	        	        		valueField: 'id',
		       	        	        		fieldLabel: UI.i18n.field.minutos
		       	        	        	}]
		       	        	        }, 

		       	        	        {
		       	        	        	margin: '30 0 0 0',
		       	        	        	xtype: 'textareafield',
		       	        	        	grow: true,
		       	        	        	name: 'observacionesReserva',
		       	        	        	fieldLabel: UI.i18n.field.observacionesReserva,
		       	        	        	width: 400
   	        	        			}, 

   	        	        			{
										xtype: 'button',
										id: 'reservar',
										scale: 'large',
										text: UI.i18n.button.reservar,
										style: {
										  	marginLeft: '175px'
										}
									}
		       	        	    ]
		       	           	}]
		       	        },
						{
		   					 layout: {
		   					        align: 'middle',
		   					        pack: 'center',
		   					        type: 'hbox'
		   					 },
		   					 frame: false,
		   					 border: 0,		       	        	
						     items: [{
								    	 xtype: 'button',
								    	 name: 'verEntrada',
										 scale: 'large',
										 text: UI.i18n.button.verEntrada,
										 hidden: true
								     }	 
						     ]
						 }			       	        
				]
	    }]
   }]
});
