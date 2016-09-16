Ext.define('Paranimf.view.usuario.GridUsuarios', {
   extend: 'Paranimf.view.EditBaseGrid',

   alias: 'widget.gridUsuarios',
   store: 'Usuarios',
    stateId: 'gridUsuarios',

   title: UI.i18n.gridTitle.user,

   dockedItems: [{
     xtype: 'pagingtoolbar',
     store: 'Usuarios',
     dock: 'bottom',
     displayInfo: true
   }],

   /*forceFit: true,*/

   initComponent: function() {

      this.columns = [{
         dataIndex: 'id',
         hidden: true,
         text: UI.i18n.field.idIntern
      }, {
         dataIndex: 'nombre',
         text: UI.i18n.field.name,
         flex: 5
      }, {
         dataIndex: 'usuario',
         text: UI.i18n.field.user,
         flex: 2
      }, {
         dataIndex: 'mail',
         text: UI.i18n.field.email,
         flex: 3
      }];

      this.callParent(arguments);
   },


   showAddUsuarioWindow: function() {
      this.createPercentageModalWindow('formUsuarios').show();
   }
});