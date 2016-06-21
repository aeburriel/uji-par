Ext.define('Paranimf.view.abono.FormSesionesAbono', {
   extend: 'Paranimf.view.EditBaseForm',
   alias: 'widget.formSesionesAbono',
   
   url : urlPrefix + 'abono',
   
   defaults: {
      allowBlank: false,
      msgTarget: 'side',
      labelWidth: 120,
      anchor: '100%',
      xtype: 'textfield',
      flex: 1
   },

    items: [{
      fieldLabel: UI.i18n.gridTitle.eventosCompras,
      name: 'evento',
      xtype: 'combobox',
      forceSelection: true,
      displayField: langsAllowed && langsAllowed.length > 1 ? 'tituloVa' : 'tituloEs',
      valueField: 'id',
      store: 'EventosCombo',
      queryMode: 'remote',
      typeAhead: true,
      allowBlank: false
    }, {
      fieldLabel: UI.i18n.gridTitle.sesionesCompras,
      name: 'sesion',
      xtype: 'combobox',
      forceSelection: true,
      tpl: '<tpl for="."><div class="x-boundlist-item" >{[Ext.util.Format.date(values.fechaCelebracion,"d/m/y H:i")]}</div></tpl>',
      displayField : 'fechaCelebracion',
      valueField: 'id',
      store: 'SesionesCombo',
      queryMode: 'local',
      typeAhead: true,
      allowBlank: false
    }]
});