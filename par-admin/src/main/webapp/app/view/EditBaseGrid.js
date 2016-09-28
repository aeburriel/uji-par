Ext.override(Ext.grid.View,{
   loadingText: UI.i18n.message.loading
});

Ext.define('Paranimf.view.EditBaseGrid', {
   extend: 'Ext.grid.Panel',

   viewConfig: {
      enableTextSelection: true
   },

    stateful: false,

   tbar: [{
      xtype: 'button',
      text: UI.i18n.button.add,
      action: 'add',
      hidden: (readOnlyUser == undefined)?false:readOnlyUser
   }, {
      xtype: 'button',
      text: (readOnlyUser == undefined || readOnlyUser == false)?UI.i18n.button.edit:UI.i18n.button.ver,
      action: 'edit'
   }, {
      xtype: 'button',
      text: UI.i18n.button.del,
      action: 'del',
      hidden: (readOnlyUser == undefined)?false:readOnlyUser
   }],

   getSelectedRecord: function(grid) {
      if (grid == undefined)
         return this.getSelectedRecordOfGrid(this);
      else
         return this.getSelectedRecordOfGrid(grid);
   },

   getSelectedRecordOfGrid: function(grid) {
      var selectedRows = grid.getSelectionModel().getSelection();
      var indiceFilaSeleccionada = grid.getStore().indexOf(selectedRows[0]);
      return grid.getStore().getAt(indiceFilaSeleccionada);
   },

   getIndiceFilaSeleccionada: function() {
      var selectedRows = this.getSelectionModel().getSelection();
      var indiceFilaSeleccionada = this.getStore().indexOf(selectedRows[0]);

      return indiceFilaSeleccionada;
   },

   createModalWindow: function(xtype, width, height, title) {
      return Ext.create('Paranimf.view.EditModalWindow', {
         title: (title)?title:this.title,
         items: [{
            autoScroll: true,
            xtype: xtype,
            width: (width)? width: 500,
            height: (height)? height: 'auto'
         }]
      });
   },

   createPercentageModalWindow: function(xtype, percentageWidth, percentageHeight, title, autoScrollable, callback) {
      var viewport = Ext.ComponentQuery.query('viewport')[0];
      percentageWidth = (percentageWidth)?viewport.width*percentageWidth:viewport.width*0.8;
      percentageHeight = (percentageHeight)?viewport.height*percentageHeight:'auto';
      autoScrollable = (autoScrollable != undefined)?autoScrollable:true;

      if (callback) {
         return Ext.create('Paranimf.view.EditModalWindow', {
            title: (title)?title:this.title,
            items: [{
               autoScroll: autoScrollable,
               xtype: xtype,
               width: percentageWidth,
               height: percentageHeight,
               buttons: [{
                  xtype: 'button',
                  text: UI.i18n.button.save,
                  action: 'save',
                  hidden: (readOnlyUser == undefined)?false:readOnlyUser,
                  handler: function() {
                     callback();
                  }
               }, {
                  xtype: 'button',
                  text: UI.i18n.button.cancel,
                  action: 'cancel',
                  handler: function() {
                     this.up('window').close();
                  }
               }]
            }]
         });
      }
      else {
         return Ext.create('Paranimf.view.EditModalWindow', {
            title: (title) ? title : this.title,
            items: [{
               autoScroll: autoScrollable,
               xtype: xtype,
               width: percentageWidth,
               height: percentageHeight
            }]
         });
      }
   },

   toJSON: function() {
      var jsonFilas = "";

      for (var i=0;i<this.store.getCount();i++) {
         if (jsonFilas != "")
            jsonFilas += ",";
         jsonFilas += Ext.JSON.encode(this.store.getRange()[i].getData());
      }
      jsonFilas = "[" + jsonFilas + "]";

      return jsonFilas;
   },

   onlyIdsToJSON: function() {
      var jsonFilas = "";

      for (var i=0;i<this.store.getCount();i++) {
         if (jsonFilas != "")
            jsonFilas += ",";
         jsonFilas += Ext.JSON.encode(this.store.getRange()[i].getData().id);
      }
      jsonFilas = "[" + jsonFilas + "]";

      return jsonFilas;
   },

   edit: function(xtype, arrayComboClearFilter, percentageWidth, percentageHeight) {
      var selectedRows = this.getSelectionModel().getSelection();
      if (selectedRows.length == 0)
         alert(UI.i18n.message.selectRow);
      else {
         var modalWindow = this.createPercentageModalWindow(xtype, percentageWidth, percentageHeight);
         var form = modalWindow.down('form').getForm();
         
         this.clearFilter(form, arrayComboClearFilter);
         modalWindow.down('form').loadRecord(selectedRows[0]);
         modalWindow.show();
      }
   },

   clearFilter: function(form, arrayComboClearFilter) {
      if (arrayComboClearFilter)
      {
         for (var i = 0; i < arrayComboClearFilter.length; i++)
         {
            var comboEspacio = form.findField(arrayComboClearFilter[i]);
            if (comboEspacio && comboEspacio.store != undefined)
            {
               comboEspacio.store.clearFilter(true);
            }
         }
      }
   },

   remove: function(callback) {
      var records = this.getSelectionModel().getSelection();
      if (records.length == 0) {
         alert(UI.i18n.message.selectRow);
         if (callback)
               callback(false);
      }
      else {
         if (confirm(UI.i18n.message.sureDelete)) {
            var st = this.store;
            if (!this.store.getProxy().hasListener('exception')) {
               this.store.getProxy().on('exception', function(proxy, resp, operation) {
                  alert(UI.i18n.error.element);
                  st.rejectChanges();
                  //st.add(st.getRemovedRecords());
               });
            }
            this.store.remove(records);
            if (callback)
               callback(true);
         }
      }
   },

   getSelectedColumnId: function()
   {
      var records = this.getSelectionModel().getSelection();

      if (records[0])
         return records[0].get("id");
      else
         return undefined;
   },
   
   getSelectedColumnIds: function()
   {
      return this.getSelectedColumnValues("id");
   },

   getSelectedColumnValues: function(column)
   {
      var records = this.getSelectionModel().getSelection();
      var values = [];
      
      for (var i=0; i<records.length; i++)
      {
        values.push(records[i].get(column));
      }

      return values;
   },

   hasRowSelected: function() {
      var records = this.getSelectionModel().getSelection();

      if (records.length == 0)
         return false;
      else
         return true;
   },

   rowsSelectedCount: function() {
      return this.getSelectionModel().getSelection().length;
   },

   addItemToStore: function(item) {
      this.store.add(item);
   },

   updateSelectedItem: function(newItem) {
      this.getSelectedRecord(this).set(newItem);
   },

   recargaStore: function() {
      this.store.clearFilter();
      this.store.load();
   },

   vaciar: function() {
      this.store.removeAll();
   },

   ocultarToolbar: function() {
      this.getDockedItems('toolbar')[0].hide();
   },

   mostrarToolbar: function() {
      this.getDockedItems('toolbar')[0].show();
   }, 

   deseleccionar: function() {
      if (this.hasRowSelected())
         this.getSelectionModel().deselectAll();
   },

   containsId: function(id) {
        for (var i=0;i<this.store.count();i++) {
            var record = this.store.getAt(i);
            if (record.data.id == id)
                return true;
        }
        return false;
    }
});