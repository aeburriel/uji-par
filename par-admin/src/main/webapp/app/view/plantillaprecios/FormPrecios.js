Ext.define('Paranimf.view.plantillaprecios.FormPrecios', {
   extend: 'Paranimf.view.EditBaseForm',
   alias: 'widget.formPrecios',

   defaults: {
      allowBlank: false,
      msgTarget: 'side',
      labelWidth: 160,
      anchor: '100%',
      xtype: 'textfield'
   },

   items: [{
    name: 'id',
    hidden: true,
    allowBlank: true
   }, {
    fieldLabel: UI.i18n.field.plantillaprecios,
    name: 'plantillaPrecios',
    xtype: 'combobox',
    forceSelection: false,
    displayField: 'nombre',
    valueField: 'id',
    store: 'PlantillasPrecios',
    readOnly: true,
    queryMode: 'local',
    typeAhead: true
   }, {
    fieldLabel: UI.i18n.field.localizacion,
    name: 'localizacion',
    xtype: 'combobox',
    forceSelection: false,
    tpl: langsAllowed && langsAllowed.length > 1 ? '<tpl for="."><div class="x-boundlist-item" >{nombreVa} ({totalEntradas})</div></tpl>' : '<tpl for="."><div class="x-boundlist-item" >{nombreEs} ({totalEntradas})</div></tpl>',
    displayField: langsAllowed && langsAllowed.length > 1 ? 'nombreVa' : 'nombreEs',
    valueField: 'id',
    store: 'Localizaciones',
    disabled: true,
    queryMode: 'local',
    typeAhead: true
   }, {
    fieldLabel: UI.i18n.field.tarifa,
    name: 'tarifa',
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
    spinUpEnabled: true,
    spinDownEnabled: true,
    minValue: 0,
    xtype: 'numericfield',
    decimalPrecision: 2,
    alwaysDisplayDecimals: true
   }]
});