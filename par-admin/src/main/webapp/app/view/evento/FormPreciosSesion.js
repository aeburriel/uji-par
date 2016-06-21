Ext.define('Paranimf.view.evento.FormPreciosSesion', {
   extend: 'Paranimf.view.EditBaseForm',
   alias: 'widget.formPreciosSesion',

   defaults: {
      allowBlank: false,
      msgTarget: 'side',
      labelWidth: 120,
      anchor: '100%',
      xtype: 'textfield'
   },

   items: [{
    name: 'id',
    hidden: true,
    allowBlank: true
   }, {
    fieldLabel: UI.i18n.field.localizacion,
    name: 'localizacion_id',
    xtype: 'combobox',
    displayField: langsAllowed && langsAllowed.length > 1 ? 'nombreVa' : 'nombreEs',
    forceSelection: false,
    valueField: 'id',
    store: 'Localizaciones',
    disabled: true,
    queryMode: 'local',
    typeAhead: true
   }, {
    fieldLabel: UI.i18n.field.tarifa,
    name: 'tarifa_id',
    xtype: 'combobox',
    forceSelection: false,
    displayField: 'nombre',
    valueField: 'id',
    store: 'Tarifas',
    disabled: true,
    queryMode: 'local',
    typeAhead: true
   }, {
    fieldLabel: UI.i18n.field.precio,
    name: 'precio',
    spinUpEnabled: false,
    spinDownEnabled: false,
    minValue: 0,
    xtype: 'numericfield',
    decimalPrecision: 2,
    alwaysDisplayDecimals: true
   }]
});