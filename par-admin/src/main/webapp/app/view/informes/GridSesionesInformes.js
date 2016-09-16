Ext.define('Paranimf.view.informes.GridSesionesInformes', {
  extend: 'Paranimf.view.EditBaseGrid',

  alias: 'widget.gridSesionesInformes',
  store: 'SesionesInformes',
    stateId: 'gridSesionesInformes',

  title: UI.i18n.gridTitle.informeSessions,
  tbar: [{
    xtype: 'combo',
    labelWidth: 200,
    fieldLabel: UI.i18n.button.selectInforme,
    store: 'TipoInformes',
    name: 'comboInformesSesiones',
    queryMode : 'remote',
    forceSelection: true,
    displayField: 'nombre',
    valueField: 'id'
  }, { 
    action: 'generarInforme',
    xtype: 'button',
    text: UI.i18n.button.generarInforme
  }],

  dockedItems: [{
    xtype: 'pagingtoolbar',
    store: 'SesionesInformes',
    dock: 'bottom',
    displayInfo: true
  }],

  initComponent: function() {
    this.columns = [{
      dataIndex: 'id',
      hidden: true,
      text: UI.i18n.field.idIntern,
      sortable: false
    }, {
      dataIndex: 'tituloEs',
      flex: 3,
      text: UI.i18n.field.tituloMulti,
      sortable: false
    }, {
      dataIndex: 'fechaCelebracion',
      text: UI.i18n.field.eventDate,
      format:'d/m/Y H:i',
      xtype: 'datecolumn',
      flex: 1,
      sortable: false
    }, {
      dataIndex: 'salaNombre',
      text: UI.i18n.field.sala,
      flex: 2,
      sortable: false
    }];

    this.callParent(arguments);
  },

  showAddSesionWindow: function() {
    this.createPercentageModalWindow('formSesiones', undefined, 0.8).show();
  }
});