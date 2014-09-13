Ext
		.define(
				'Paranimf.view.evento.FormEventos',
				{
					extend : 'Paranimf.view.EditBaseForm',
					alias : 'widget.formEventos',
					fileUpload : true,
					enctype : 'multipart/form-data',

					url : urlPrefix + 'eventos',

					defaults : {
						allowBlank : false,
						msgTarget : 'side',
						labelWidth : 90,
						anchor : '100%',
						xtype : 'textfield',
						flex : 1
					},

					buttons : [ {
						xtype : 'button',
						text : UI.i18n.button.save,
						action : 'save'
					}, {
						xtype : 'button',
						text : UI.i18n.button.cancel,
						handler : function() {
							this.up('window').close();
						}
					}, {
						xtype : 'button',
						text : UI.i18n.button.eliminarImagen,
						action : 'deleteImage'
					} ],

					items : [
							{
								name : 'id',
								hidden : true,
								allowBlank : true
							},
							{
								xtype : 'fieldset',
								flex : 1,
								title : UI.i18n.field.tituloMulti,
								defaults : {
									xtype : 'textfield',
									anchor : '100%'
								},
								items : [ {
									fieldLabel : UI.i18n.field.rssId,
									labelWidth : 220,
									name : 'rssId',
									allowBlank : true 
								}, {
									fieldLabel : UI.i18n.field.title,
									name : 'tituloEs',
									allowBlank : false
								}, {
									fieldLabel : UI.i18n.field.title_va,
									name : 'tituloVa',
									allowBlank : false
								} ]
							},

							{
								xtype : 'fieldset',
								flex : 1,
								title : UI.i18n.field.descripcionMulti,
								defaults : {
									xtype : 'textarea',
									anchor : '100%'
								},
								items : [ {
									fieldLabel : UI.i18n.field.description,
									name : 'descripcionEs',
									allowBlank : true
								}, {
									fieldLabel : UI.i18n.field.description_va,
									name : 'descripcionVa',
									allowBlank : true
								} ]
							},

							{
								fieldLabel : UI.i18n.field.type,
								name : 'tipoEvento',
								labelWidth : 120,
								xtype : 'combobox',
								forceSelection: false,
								displayField : 'nombreEs',
								valueField : 'id',
								store : 'TiposEventosSinPaginar',
								queryMode : 'remote',
								typeAhead : true
							},
							{
								fieldLabel : UI.i18n.field.asientosNumerados,
								labelWidth : 120,
								name : 'asientosNumerados',
								xtype : 'combobox',
								displayField : 'name',
								valueField : 'value',
								queryMode : 'local',
								allowBlank : false,
								forceSelection : true,
								store : new Ext.data.SimpleStore({
									fields : [ 'value', 'name' ],
									data : [ [ true, UI.i18n.message.si ],
											[ false, UI.i18n.message.no ] ]
								})
							},

							{
								xtype : 'fieldset',
								title : UI.i18n.field.imagen,
								items : [ {
									name : 'dataBinary',
									anchor : '100%',
									allowBlank : true,
									fieldLabel : UI.i18n.field.uploadImagen,
									labelWidth : 90,
									msgTarget : 'side',
									xtype : 'filefield',
									buttonText : '...'
								}, {
									xtype : 'label',
									id : 'imagenInsertada'
								} ]
							},

							{
								xtype : 'fieldset',
								flex : 1,
								title : UI.i18n.field.retenciones,
								defaults : {
									xtype : 'numberfield',
									anchor : '100%',
									spinUpEnabled : false,
									spinDownEnabled : false,
									labelWidth: 120,
									minValue : 0
								},
								items : [ {
									fieldLabel : UI.i18n.field.porcentajeIVA,
									name : 'porcentajeIVA',
									allowBlank : false
								}, {
									fieldLabel : UI.i18n.field.ivaSGAE,
									name : 'ivaSGAE',
									allowBlank : true
								}, {
									fieldLabel : UI.i18n.field.retencionSGAE,
									name : 'retencionSGAE',
									allowBlank : true
								} ]
							},

							{
								xtype : 'fieldset',
								flex : 1,
								title : UI.i18n.field.icaa,
								defaults : {
									xtype : 'textfield',
									anchor : '100%',
									labelWidth : 120
								},
								items : [
										{
											fieldLabel : UI.i18n.field.expediente,
											name : 'expediente',
											labelWidth : 170
										},
										{
											fieldLabel : UI.i18n.field.codigoDistribuidora,
											name : 'codigoDistribuidora'
										},
										{
											fieldLabel : UI.i18n.field.nombreDistribuidora,
											name : 'nombreDistribuidora'
										},
										{
											fieldLabel : UI.i18n.field.nacionalidad,
											name : 'nacionalidad',
											xtype : 'combobox',
											store : 'Nacionalidades',
											displayField : 'label',
											valueField : 'id',
											queryMode : 'local',
											forceSelection : true
										},
										{
											fieldLabel : UI.i18n.field.vo,
											name : 'vo',
											xtype : 'combobox',
											displayField : 'name',
											valueField : 'value',
											queryMode : 'local',
											forceSelection : true,
											store : new Ext.data.SimpleStore(
													{
														fields : [ 'value',
																'name' ],
														data : [
																[
																		"1",
																		UI.i18n.versionOriginal.castella ],
																[
																		"2",
																		UI.i18n.versionOriginal.catala ],
																[
																		"3",
																		UI.i18n.versionOriginal.vasc ],
																[
																		"4",
																		UI.i18n.versionOriginal.gallec ],
																[
																		"5",
																		UI.i18n.versionOriginal.valencia ],
																[
																		"6",
																		UI.i18n.versionOriginal.angles ],
																[
																		"7",
																		UI.i18n.versionOriginal.frances ],
																[
																		"8",
																		UI.i18n.versionOriginal.alema ],
																[
																		"9",
																		UI.i18n.versionOriginal.portugues ],
																[
																		"A",
																		UI.i18n.versionOriginal.italia ],
																[
																		"Z",
																		UI.i18n.versionOriginal.altres ] ]
													})
										},
										{
											fieldLabel : UI.i18n.field.metraje,
											name : 'metraje'
										},
										{
											fieldLabel : UI.i18n.field.subtitulos,
											name : 'subtitulos',
											xtype : 'combobox',
											displayField : 'name',
											valueField : 'value',
											queryMode : 'local',
											forceSelection : true,
											store : new Ext.data.SimpleStore(
													{
														fields : [ 'value',
																'name' ],
														data : [
																[
																		"1",
																		UI.i18n.subtitulos.castella ],
																[
																		"2",
																		UI.i18n.subtitulos.catala ],
																[
																		"3",
																		UI.i18n.subtitulos.vasc ],
																[
																		"4",
																		UI.i18n.subtitulos.gallec ],
																[
																		"5",
																		UI.i18n.subtitulos.valencia ],
																[
																		"9",
																		UI.i18n.subtitulos.altres ],
																[
																		"0",
																		UI.i18n.subtitulos.sense ] ]
													})
										} ]
							},

							{
								xtype : 'fieldset',
								flex : 1,
								title : UI.i18n.field.companyMulti,
								defaults : {
									xtype : 'textfield',
									anchor : '100%'
								},
								items : [ {
									fieldLabel : UI.i18n.field.company,
									name : 'companyiaEs',
									allowBlank : true
								}, {
									fieldLabel : UI.i18n.field.company_va,
									name : 'companyiaVa',
									allowBlank : true
								} ]
							},

							{
								xtype : 'fieldset',
								flex : 1,
								title : UI.i18n.field.staffMulti,
								defaults : {
									xtype : 'textfield',
									anchor : '100%'
								},
								items : [ {
									fieldLabel : UI.i18n.field.staff,
									name : 'interpretesEs',
									allowBlank : true
								}, {
									fieldLabel : UI.i18n.field.staff_va,
									name : 'interpretesVa',
									allowBlank : true
								} ]
							},

							{
								xtype : 'fieldset',
								flex : 1,
								title : UI.i18n.field.duracionMulti,
								defaults : {
									xtype : 'textfield',
									anchor : '100%'
								},
								items : [ {
									fieldLabel : UI.i18n.field.duration,
									name : 'duracionEs',
									allowBlank : true
								}, {
									fieldLabel : UI.i18n.field.duration_va,
									name : 'duracionVa',
									allowBlank : true
								} ]
							},

							{
								xtype : 'fieldset',
								flex : 1,
								title : UI.i18n.field.awardsMulti,
								defaults : {
									xtype : 'textfield',
									anchor : '100%'
								},
								items : [ {
									fieldLabel : UI.i18n.field.awards,
									name : 'premiosEs',
									allowBlank : true
								}, {
									fieldLabel : UI.i18n.field.awards_va,
									name : 'premiosVa',
									allowBlank : true
								} ]
							},

							{
								xtype : 'fieldset',
								flex : 1,
								title : UI.i18n.field.characteristicsMulti,
								defaults : {
									xtype : 'textfield',
									anchor : '100%'
								},
								items : [
										{
											fieldLabel : UI.i18n.field.characteristics,
											name : 'caracteristicasEs',
											allowBlank : true
										},
										{
											fieldLabel : UI.i18n.field.characteristics_va,
											name : 'caracteristicasVa',
											allowBlank : true
										} ]
							},

							{
								xtype : 'fieldset',
								flex : 1,
								title : UI.i18n.field.commentsMulti,
								defaults : {
									xtype : 'textfield',
									anchor : '100%'
								},
								items : [ {
									fieldLabel : UI.i18n.field.comments,
									name : 'comentariosEs',
									allowBlank : true
								}, {
									fieldLabel : UI.i18n.field.comments_va,
									name : 'comentariosVa',
									allowBlank : true
								} ]
							} ]
				});