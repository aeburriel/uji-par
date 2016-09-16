Ext.define('Paranimf.view.evento.GridEventosMultisesion', {
   extend: 'Paranimf.view.EditBaseGrid',

   alias: 'widget.gridEventosMultisesion',
   store: 'EventosMultisesion',
   title: UI.i18n.gridTitle.multisesion,
    stateId: 'gridEventosMultisesion',

   tbar: [{
      xtype: 'button',
      text: UI.i18n.button.add,
      action: 'add'
   }, {
      xtype: 'button',
      text: UI.i18n.button.del,
      action: 'del'
   }],

   	initComponent: function() {

    	this.columns = [{
        	dataIndex: 'id',
        	hidden: true,
        	text: UI.i18n.field.idIntern
      	}, {
	        dataIndex: 'tituloEs',
        	text: UI.i18n.field.titolNeutreLabel,
        	flex: 5
      	}, {
          dataIndex: 'versionLinguistica',
          text: UI.i18n.field.versionLinguistica,
          flex: 5,
          renderer: function (val, p, rec) {
            return eval("UI.i18n.versionLinguistica." + val);
          }
        }];

      	this.callParent(arguments);
   	},

   	showAddPelicula: function() {
   		this.createModalWindow('formPeliculaMultisesion', undefined, undefined, UI.i18n.formTitle.peliculas).show();
   	},

   	toJSON: function() {
        var jsonFilas = "";

        for (var i=0;i<this.store.getCount();i++) {
           if (jsonFilas != "")
              jsonFilas += ",";
           jsonFilas += '{ "id": ' + Ext.JSON.encode(this.store.getRange()[i].getData().id) + ', "versionLinguistica": ' + Ext.JSON.encode(this.store.getRange()[i].getData().versionLinguistica) + '}';
        }
        jsonFilas = "[" + jsonFilas + "]";

        return jsonFilas;
    }
});