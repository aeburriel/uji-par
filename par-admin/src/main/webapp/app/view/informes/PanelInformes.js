Ext.define('Paranimf.view.informes.PanelInformes', {
  extend: 'Ext.panel.Panel',
  alias: 'widget.panelInformes',
  frame: false,
  border: false,
  layout: 'border',

  items: [{
    border: false,
    region: 'north',
    autoScroll: false,
    xtype: 'panel',
    height: 70,
    defaults: {
      border: 0,
      xtype: 'panel',
      layout: 'hbox',
      margin: '0px 0px 10px 0px'
    },
    items: [{
      items: [{
        xtype: 'datefield',
        name: 'fechaInicio',
        startDay: 1,
        fieldLabel: UI.i18n.field.startDate,
        margin: '0px 10px 0px 0px'
      }, {
        xtype: 'datefield',
        name: 'fechaFin',
        startDay: 1,
        fieldLabel: UI.i18n.field.endDate,
        margin: '0px 10px 0px 0px'
      }, {
        xtype: 'button',
        action: 'filtrarSessions',
        text: UI.i18n.button.filtrarSessions
      }]
    }, {
      items: [{
        xtype: 'combo',
        labelWidth: 270,
        width: 500,
        fieldLabel: UI.i18n.message.informesGenerals,
        store: 'TiposInformesGenerales',
        name: 'comboInformesGenerales',
        queryMode : 'remote',
        forceSelection: true,
        displayField: 'nombre',
        valueField: 'id'
      }, { 
        action: 'generarInformeGeneral',
        xtype: 'button',
        margin: '0px 0px 0px 10px',
        text: UI.i18n.button.generarInforme
      }]
    }
  ]}, {
      region: 'center',
      xtype: 'gridSesionesInformes'
  }]
});