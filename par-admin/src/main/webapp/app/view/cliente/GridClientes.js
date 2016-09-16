Ext.define('Paranimf.view.cliente.GridClientes', {
   extend: 'Paranimf.view.EditBaseGrid',

   alias: 'widget.gridClientes',
   store: 'Clientes',

   title: UI.i18n.gridTitle.client,
    stateId: 'gridClientes',

   dockedItems: [{
     xtype: 'pagingtoolbar',
     store: 'Clientes',
     dock: 'bottom',
     displayInfo: true
   }],

   tbar: [{
      xtype: 'button',
      text: UI.i18n.button.emails,
      action: 'copyEmails'
   }],

   viewConfig: {
      enableTextSelection: true
   },

   initComponent: function() {
      this.columns = [{
         dataIndex: 'id',
         hidden: true,
         text: UI.i18n.field.idIntern
      }, {
         dataIndex: 'nombre',
         text: UI.i18n.field.name,
         flex: 4
      }, {
         dataIndex: 'apellidos',
         text: UI.i18n.field.surnameMulti,
         flex: 4
      }, {
         dataIndex: 'direccion',
         text: UI.i18n.field.address,
         flex: 3
      }, {
         dataIndex: 'poblacion',
         text: UI.i18n.field.poblacion,
         flex: 3
      }, {
         dataIndex: 'cp',
         text: UI.i18n.field.codigoPostal,
         flex: 3
      }, {
         dataIndex: 'provincia',
         text: UI.i18n.field.provincia,
         flex: 3
      }, {
         dataIndex: 'telefono',
         text: UI.i18n.field.phoneMobile,
         flex: 3
      }, {
         dataIndex: 'email',
         text: UI.i18n.field.email,
         flex: 4
      }];

      this.callParent(arguments);
   }
});