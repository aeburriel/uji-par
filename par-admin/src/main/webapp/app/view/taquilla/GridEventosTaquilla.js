Ext.define('Paranimf.view.taquilla.GridEventosTaquilla', {
   extend: 'Paranimf.view.EditBaseGrid',

   alias: 'widget.gridEventosTaquilla',
   store: 'EventosTaquilla',
    stateId: 'gridEventosTaquilla',

   title: UI.i18n.gridTitle.eventos,

   tbar: [{
      xtype: 'checkbox',
      fieldLabel: UI.i18n.field.ventaRetrasada,
      name: 'mostrarTodos',
      labelWidth: 410,
      labelAlign: 'right'
   }],

   dockedItems: [{
     xtype: 'pagingtoolbar',
     store: 'EventosTaquilla',
     dock: 'bottom',
     displayInfo: true
   }],

   initComponent: function() {

      this.columns = [{
        dataIndex: 'id',
        hidden: true,
        text: UI.i18n.field.idIntern
      }, {
        dataIndex: 'asientosNumerados',
        hidden: true,
        text: UI.i18n.field.asientosNumerados,
        renderer: function(val) {
          if (val ==0)
            return 'No';
          else
            return 'Sí';
        }
      }, {
        dataIndex: 'parTiposEvento',
        text: UI.i18n.field.type,
        flex: 2,
        renderer: function (val, p) {
          return langsAllowed && langsAllowed.length > 1 ? val["nombreVa"] : val["nombreEs"];
        }
      }, {
        dataIndex: langsAllowed && langsAllowed.length > 1 ? 'tituloVa' : 'tituloEs',
        text: langsAllowed && langsAllowed.length > 1 ? UI.i18n.field.title_va : UI.i18n.field.title,
        flex: 5
      },{
          dataIndex: 'rssId',
          text: UI.i18n.field.rssId,
          hidden: true
      }];

      this.callParent(arguments);
   }

});