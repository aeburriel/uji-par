Ext.define('Paranimf.view.tipoevento.FormTiposEventos', {
   extend: 'Paranimf.view.EditBaseForm',
   alias: 'widget.formTiposEventos',

   url : urlPrefix + 'tiposeventos',
   
   defaults: {
      allowBlank: false,
      msgTarget: 'side',
      labelWidth: 90,
      anchor: '100%',
      xtype: 'textfield'
   },

   items: [{
      name: 'id',
      hidden: true,
      allowBlank: true
   }, {
      fieldLabel: UI.i18n.field.name,
      name: 'nombreEs'
   }, {
	  fieldLabel: UI.i18n.field.name_va,
	  name: 'nombreVa',
      hidden: langsAllowed && langsAllowed.length > 1 ? false : true,
      allowBlank: langsAllowed && langsAllowed.length > 1 ? false : true
   }, {
      xtype: 'combobox',
      forceSelection: false,
      hidden: true,
      queryMode: 'local',
      name: 'exportarICAA',
      labelWidth: 280,
      valueField: 'id',
      displayField: 'label',
      fieldLabel: UI.i18n.field.exportarICAA,
      store: 'SiNo'
   }]
});