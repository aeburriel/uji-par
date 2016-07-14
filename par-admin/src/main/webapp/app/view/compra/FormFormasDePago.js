Ext.define('Paranimf.view.compra.FormFormasDePago', {
   extend: 'Paranimf.view.EditBaseForm',
   alias: 'widget.formFormasDePago',
   
   defaults: {
      msgTarget: 'side',
      labelWidth: 150,
      anchor: '100%',
      flex: 1
   },

   items: [
    {
      fieldLabel: UI.i18n.field.tipoPago,
      name: 'tipoPago',
      xtype: 'combobox',
      displayField: 'name',
      valueField : 'value',
      queryMode: 'local',
      typeAhead: false,
      allowBlank: false,
      forceSelection:true,
      store: new Ext.data.SimpleStore({
        fields: ['value', 'name'],
          data: payModes
      })
   },{
      name: 'referenciaDePago',
      xtype: 'textfield',
      allowBlank: false,
      hidden: true,
      fieldLabel: UI.i18n.field.referenciaDePago
  }]
});