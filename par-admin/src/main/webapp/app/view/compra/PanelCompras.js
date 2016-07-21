Ext.define('Paranimf.view.compra.PanelCompras', {
  extend: 'Ext.panel.Panel',
  alias: 'widget.panelCompras',
  /*border: false,
  frame: false,*/
  layout: 'border',
  //autoScroll: false,

  buttons: [{
    xtype: 'button',
    text: UI.i18n.button.close,
    handler: function() {
      this.up('window').close();
    }
  }],

  items: [{
    region: 'north',
    xtype: 'panel',
    layout: 'hbox',
    items: [{
      xtype: 'textfield',
      name: 'buscadorCompras',
      fieldLabel: UI.i18n.field.buscarCompras,
      labelWidth: 340,
      margin: '10px 10px 10px 10px'
    }, {
      xtype: 'button',
      action: 'search',
      text: UI.i18n.button.buscar,
      margin: '10px 0px 10px 0px'
    }]
  },{
    region: 'center',
    xtype: 'gridCompras',
    collapsible: true,
    minHeight: 100,
    split: true,
    header: true,
    flex: 1.7
  }, {
    region: 'south',
    xtype: 'gridDetalleCompras',
    minHeight: 100,
    collapsible: true,
    split: true,
    flex: 1
  }]
});