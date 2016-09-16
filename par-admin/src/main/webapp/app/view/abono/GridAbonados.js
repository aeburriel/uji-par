Ext.define('Paranimf.view.abono.GridAbonados', {
   extend: 'Paranimf.view.EditBaseGrid',

   alias: 'widget.gridAbonados',
   store: 'Abonados',

   title: UI.i18n.gridTitle.abonados,
    stateId: 'gridAbonados',

    dockedItems: [{
        xtype: 'pagingtoolbar',
        store: 'Abonados',
        dock: 'bottom',
        displayInfo: true
    }],

   initComponent: function() {

    this.columns = [{
      dataIndex: 'id',
      hidden: true,
      text: UI.i18n.field.idIntern
    }, {
      dataIndex: 'nombre',
      text: UI.i18n.field.nameMulti,
      flex: 1
    }, {
      dataIndex: 'apellidos',
      text: UI.i18n.field.surnameMulti,
      flex: 1
    }];

    this.callParent(arguments);
  },

  showAddAbonadoWindow: function(idAbono, title) {
    var window = this.createPercentageModalWindow('formComprarAbono', 0.95, 0.95, title).show();
    var cardLayout = Ext.getCmp('pasoSeleccionar').getLayout();

    cardLayout.setActiveItem(0);
    var url = "entrada/butacasAbonoFragment/" + idAbono + "?if=true";    
    Ext.getDom('iframeButacas').src = url;
  }
});