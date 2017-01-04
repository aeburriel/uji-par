Ext.define('Paranimf.controller.Eventos', {
   extend: 'Ext.app.Controller',

   views: ['EditModalWindow', 'EditBaseForm', 'EditBaseGrid', 'evento.GridEventos', 'evento.GridEventosMultisesion', 'evento.FormEventos', 'evento.PanelEventos', 'evento.GridSesiones', 'evento.FormSesiones', 'evento.GridPreciosSesion', 'evento.FormPreciosSesion', 'evento.FormPeliculaMultisesion'],
   stores: ['Eventos', 'EventosMultisesion', 'TiposEventosSinPaginar', 'Sesiones', 'PlantillasPrecios', 'PreciosSesion', 'Salas', 'SalasEvento', 'Nacionalidades', 'Peliculas', 'Tpvs'],
   models: ['Evento', 'Sesion', 'PrecioSesion', 'Sala', 'HoraMinuto', 'Tpv'],

   refs: [{
      ref: 'gridEventos',
      selector: 'gridEventos'
   },{
      ref: 'gridEventosMultisesion',
      selector: 'gridEventosMultisesion'
   }, {
      ref: 'formEventos',
      selector: 'formEventos'
   }, {
	   ref: 'panelEventos',
	   selector: 'panelEventos'
   }, {
	   ref: 'gridSesiones',
	   selector: 'gridSesiones'
   }, {
	   ref: 'formSesiones',
	   selector: 'formSesiones'
   }, {
      ref: 'botonDeleteImagen',
      selector: 'formEventos button[action=deleteImage]'
   }, {
      ref: 'botonDeleteImagenPubli',
      selector: 'formEventos button[action=deleteImagePubli]'
   }, {
      ref: 'checkMultisesion',
      selector: 'formEventos checkbox[name=multisesion]'
   }, {
      ref: 'icaaMultisesionGridFieldset',
      selector: 'formEventos fieldset[name=icaaGrid]'
   }, {
      ref: 'icaaFieldset',
      selector: 'formEventos fieldset[name=icaa]'
   }, {
      ref: 'comboTpv',
      selector: 'formEventos combobox[name=tpv]'
   }, {
      ref: 'gridPreciosSesion',
      selector: 'gridPreciosSesion'
   }, {
      ref: 'formPreciosSesion',
      selector: 'formPreciosSesion'
   }, {
      ref: 'sesionCine',
      selector: 'formSesiones fieldset[name=sesionCine]'
   }, {
      ref: 'comboSala',
      selector: 'formSesiones combobox[name=sala]'
   }, {
      ref: 'comboLocalizaciones',
      selector: 'formPreciosSesion combobox[name=localizacion_id]'
   }, {
      ref: 'comboTarifas',
      selector: 'formPreciosSesion combobox[name=tarifa_id]'
   }, {
      ref: 'checkboxVentaOnline',
      selector: 'formSesiones checkbox[name=canalInternet]'
   }, {
      ref: 'fechaInicioVentaOnline',
      selector: 'formSesiones datefield[name=fechaInicioVentaOnline]'
   }, {
      ref: 'fechaFinVentaOnline',
      selector: 'formSesiones datefield[name=fechaFinVentaOnline]'
   }, {
      ref: 'horaInicioVentaOnline',
      selector: 'formSesiones timefield[name=horaInicioVentaOnline]'
   }, {
      ref: 'horaFinVentaOnline',
      selector: 'formSesiones timefield[name=horaFinVentaOnline]'
   }, {
      ref: 'fieldsetReservesOnline',
      selector: 'formSesiones fieldset[name=reservesOnline]'
   }, {
      ref: 'fechaCelebracion',
      selector: 'formSesiones datefield[name=fechaCelebracion]'
   }, {
      ref: 'horaCelebracion',
      selector: 'formSesiones timefield[name=horaCelebracion]'
   }, {
      ref: 'horaApertura',
      selector: 'formSesiones timefield[name=horaApertura]'
   }, {
      ref: 'comboPlantillaPrecios',
      selector: 'formSesiones combobox[name=plantillaPrecios]'
   }, {
      ref: 'comboVersionLinguistica',
      selector: 'formSesiones combobox[name=versionLinguistica]'
   }, {
      ref: 'comboVersionLinguisticaMultisesion',
      selector: 'formPeliculaMultisesion combobox[name=versionLinguistica]'
   }, {
      ref: 'comboPeliculas',
      selector: 'formPeliculaMultisesion combobox[name=peliculas]'
   }, {
      ref: 'formPeliculaMultisesion',
      selector: 'formPeliculaMultisesion'
   }, {
      ref: 'jsonEventosMultisesion',
      selector: 'formEventos hiddenfield[name=jsonEventosMultisesion]'
   }, {
      ref: 'checkMostrarTodosEventos',
      selector: 'gridEventos checkbox[name=mostrarTodos]'
   }],

   init: function() {
      this.control({

         'gridEventos button[action=add]': {
            click: this.addEvento
         },

         'gridEventos button[action=edit]': {
            click: this.editEvento
         },

         'gridEventos button[action=del]': {
            click: this.removeEvento
         },

         'panelEventos': {
			   beforeactivate: this.recargaStore
         },

         'formEventos button[action=save]': {
            click: this.saveEventoFormData
         },

         'formEventos button[action=deleteImage]': {
            click: this.deleteImage
         },

         'formEventos button[action=deleteImagePubli]': {
            click: this.deleteImagePubli
         },
         
         'formEventos': {
        	   beforerender: this.showImagenIfExists
         },

         'formEventos': {
            afterrender: this.preparaEvento
         },

         'formEventos checkbox[name=multisesion]': {
            change: this.showMultisesion
         },
         
         'gridEventos': {
             selectionchange: this.loadSesiones
         },
         
         'gridSesiones': {
             itemdblclick: this.editSesion
         },
         
         'gridSesiones button[action=add]': {
             click: this.addSesion
         },

         'gridSesiones button[action=edit]': {
             click: this.editSesion
         },

         'gridSesiones button[action=del]': {
            click: this.removeSesion
         },

         'formSesiones': {
            afterrender: this.preparaStoresSesiones
         },
         
         'formSesiones button[action=save]': {
            click: this.saveSesionFormData
         },

         'formSesiones combobox[name=plantillaPrecios]': {
            change: this.cambiaPlantilla
         },

         'formSesiones combobox[name=sala]': {
            change: this.cambiaSala
         },

         'gridPreciosSesion button[action=add]': {
            click: this.addPrecioSesion
         },

         'gridPreciosSesion button[action=del]': {
            click: this.deletePrecioSesion
         },

         'gridPreciosSesion button[action=edit]': {
            click: this.editPrecioSesion
         },

         'formPreciosSesion': {
            afterrender: this.cargaStores
         }, 

         'formPreciosSesion button[action=save]': {
            click: this.savePrecioSesion
         }, 
         'formSesiones checkbox[name=canalInternet]': {
            change: this.enableDisableCanalInternet
         }, 

         'formSesiones timefield[name=horaCelebracion]': {
            change: this.actualizaHoraApertura
         },

         'formEventos gridEventosMultisesion button[action=add]': {
            click: this.showAddEventoMultisesion
         },

         'formEventos gridEventosMultisesion button[action=del]': {
            click: this.deleteEventoMultisesion
         },

         'formPeliculaMultisesion button[action=save]': {
            click: this.addPeliculaMultisesion
         },

         'gridEventos checkbox[name=mostrarTodos]': {
            change: this.showHideTodosEventos
         }
      });
   },

   showHideTodosEventos: function () {
      this.getGridEventos().store.proxy.url = urlPrefix + 'evento';
      this.getGridEventos().store.proxy.extraParams = {
         activos: (!this.getCheckMostrarTodosEventos().checked).toString()
      };
      this.recargaStore();
   },

   actualizaHoraApertura: function(obj, newValue, oldValue) {
      if (newValue != undefined && newValue != '') {
         var d = new Date(newValue.getTime() - 30 * 60000);
         this.getHoraApertura().setValue(d);
      }
   },

   enableDisableCanalInternet: function(obj, newValue, oldValue) {
      this.getFechaInicioVentaOnline().allowBlank = !newValue;
      this.getFechaFinVentaOnline().allowBlank = !newValue;
      this.getHoraInicioVentaOnline().allowBlank = !newValue;
      this.getHoraFinVentaOnline().allowBlank = !newValue;
      if (newValue) {
         this.getFechaInicioVentaOnline().show();
         this.getFechaFinVentaOnline().show();
         this.getHoraInicioVentaOnline().show();
         this.getHoraFinVentaOnline().show();
      } else {
         this.getFechaInicioVentaOnline().hide();
         this.getFechaFinVentaOnline().hide();
         this.getHoraInicioVentaOnline().hide();
         this.getHoraFinVentaOnline().hide();
      }
   },

   deletePrecioSesion: function(button, event, opts) {
      if (button.up('form').getForm().findField('plantillaPrecios').value == -1) {
         this.getGridPreciosSesion().remove();
      }
   },

   editPrecioSesion: function(button, event, opts) {
      if (this.getComboSala().value != undefined && this.getComboSala().value != '' && this.getComboSala().value != 0)
         this.getGridPreciosSesion().edit('formPreciosSesion');
      else
         alert(UI.i18n.error.salaObligatoria);
   },

   cambiaPlantilla: function(combo, newValue, oldValue, opts) {
      if (newValue == -1) {
         this.getGridPreciosSesion().mostrarToolbar();
         this.getGridPreciosSesion().vaciar();
         this.cargarPreciosSesion();
      } else {
         this.getGridPreciosSesion().ocultarToolbar();
         this.cargarPreciosPlantilla(newValue);
      }
   },

   cambiaSala: function(combo, newValue, oldValue, opts) {
      if (newValue != undefined && newValue != '' && newValue != 0) {
         this.getComboPlantillaPrecios().store.proxy.url = urlPrefix + 'plantillaprecios/sala/' + newValue;
      }
      else {
         this.getComboPlantillaPrecios().store.proxy.url = urlPrefix + 'plantillaprecios';
      }

      var self = this;
      this.getComboPlantillaPrecios().store.load(function(records, operation, success) {
         var plantilla = self.getComboPlantillaPrecios().value;

         if (plantilla != undefined && plantilla != '' && plantilla != 0) {
            for (var i = 0; i < records.length; i++) {
               if (plantilla == records[i].data.id)
               {
                  return;
               }
            }
         }
         self.getComboPlantillaPrecios().setValue(-1);
      });
   },

   cargarPreciosPlantilla: function(plantillaId) {
      var gridPreciosSesion = this.getGridPreciosSesion();
      Ext.Ajax.request({
        url : urlPrefix + 'plantillaprecios/' + plantillaId + '/precios',
        method: 'GET',
        success: function (response) {
            var respuesta = Ext.JSON.decode(response.responseText, true);
            gridPreciosSesion.store.loadRawData(respuesta.data);
        }, failure: function (response) {
           alert(UI.i18n.error.loadingPrecios);
        }
      });
   },
   
   cargarPreciosSesion: function(plantillaId) {
      var gridPreciosSesion = this.getGridPreciosSesion();
      
      if (this.getGridEventos().hasRowSelected() && this.getGridSesiones().hasRowSelected()) {
         var idEvento = this.getGridEventos().getSelectedRecord().data["id"];
         var idSesion = this.getGridSesiones().getSelectedRecord().data["id"];
         
         if (idEvento != undefined && idSesion != undefined) {
            console.log('idEvento:', idEvento);
            console.log('idSesion:', idSesion);
            
            gridPreciosSesion.store.proxy.url = urlPrefix + 'evento/' + idEvento + '/sesiones/' + idSesion + '/precios';
            gridPreciosSesion.store.load(function(records, operation, success) {
               if (!success)
                  alert(UI.i18n.error.loadingPrecios);
            })
         }
      }
   },   

   cargaStores: function(comp, opts) {
      this.getComboLocalizaciones().store.loadData([],false);
      this.getComboLocalizaciones().store.proxy.url = urlPrefix + 'localizacion?sala=' + this.getComboSala().value;
      this.getFormPreciosSesion().cargaComboStore('localizacion_id');
      this.getFormPreciosSesion().cargaComboStore('tarifa_id');
   },

   addPrecioSesion: function(button, event, opts) {
      if (this.getComboSala().value != undefined && this.getComboSala().value != '' && this.getComboSala().value != 0) {
         this.getGridPreciosSesion().deseleccionar();
         this.getGridPreciosSesion().showAddPrecioSesionWindow();
      } else
         alert(UI.i18n.error.salaObligatoria);
   }, 

   existeRecordConLocalizacionYTarifaElegida: function(indexFilaSeleccionada, localizacionSeleccionada, tarifaSeleccionada) {
      var encontrado = false;
      for (var i=0;i<this.getGridPreciosSesion().store.count();i++) {
         var record = this.getGridPreciosSesion().store.getAt(i);
         if (record.data.localizacion == localizacionSeleccionada && record.data.tarifa == tarifaSeleccionada)
            encontrado = true;
      }
      return encontrado;
   },

   savePrecioSesion: function(button, event, opts) {
      var indiceFilaSeleccionada = this.getGridPreciosSesion().getIndiceFilaSeleccionada();
      var localizacionSeleccionada = this.getComboLocalizaciones().value;
      var tarifaSeleccionada = this.getComboTarifas().value;

      if (!this.existeRecordConLocalizacionYTarifaElegida(indiceFilaSeleccionada, localizacionSeleccionada, tarifaSeleccionada)) {
         var precioSesion = new Ext.create('Paranimf.model.PrecioSesion', {
            plantillaPrecios :-1,
            precio : button.up('form').getForm().findField('precio').value,
            localizacion_id: localizacionSeleccionada,
            "parLocalizacione.nombreVa": this.getComboLocalizaciones().rawValue,
            "parLocalizacione.nombreEs": this.getComboLocalizaciones().rawValue,
            tarifa_id: tarifaSeleccionada,
            "parTarifa.nombre": this.getComboTarifas().rawValue
         });

         if (indiceFilaSeleccionada != -1) {
            var recordSeleccionado = this.getGridPreciosSesion().store.getAt(indiceFilaSeleccionada);
            console.log(recordSeleccionado);
            recordSeleccionado.set('precio', precioSesion.data.precio);
            recordSeleccionado.set('localizacion_id', precioSesion.data.localizacion_id);
            recordSeleccionado.set('parLocalizacione.nombreVa', precioSesion.data["parLocalizacione.nombreVa"]);
            recordSeleccionado.set('tarifa_id', precioSesion.data.tarifa_id);
            recordSeleccionado.set('parTarifa.nombre', precioSesion.data["parTarifa.nombre"]);
            recordSeleccionado.commit(true);
         }
         else
            this.getGridPreciosSesion().store.add(precioSesion);

         this.getGridPreciosSesion().getView().refresh();
         button.up('window').close();
      } else {
         alert(UI.i18n.message.registroConMismaLocalizacionExiste)
      }
   },
   
   preparaStoresSesiones: function(comp, opts) {
	   this.preparaStorePlantillaPrecios();
	   this.preparaStoreSalas(this.getGridEventos().getSelectedRecord().data.id);

      if (!this.getGridEventos().getSelectedRecord().data.parTiposEvento.exportarICAA) {
         this.getSesionCine().hide();
         this.getComboVersionLinguistica().allowBlank = true;
         this.getComboVersionLinguistica().setFieldLabel(UI.i18n.field.versionLinguistica);
      } else {
         this.getSesionCine().show();
         this.getComboVersionLinguistica().allowBlank = false;
         this.getComboVersionLinguistica().setFieldLabel(UI.i18n.field.versionLinguistica + ' <span class="req" style="color:red">*</span>');

         var isMultisesion = false;
         if (this.getGridEventos().getSelectedRecord().data.multisesion) {
            this.getComboVersionLinguistica().allowBlank = true;
            this.getComboVersionLinguistica().hide();
         }
      }
   },

   preparaEvento: function(comp, opts) {
      if (typeof multipleTpv !== "undefined") {
         if (null != multipleTpv && multipleTpv) {
            this.getComboTpv().show();
            this.getComboTpv().allowBlank = false;
            this.getComboTpv().setFieldLabel(UI.i18n.field.tpv + ' <span class="req" style="color:red">*</span>');
         }
      }
   },

   preparaStorePlantillaPrecios: function() {
      var idASeleccionar = -1;
      if (this.getGridSesiones().getSelectedColumnId() != undefined) {
         var selectedRecord = this.getGridSesiones().getSelectedRecord();
         idASeleccionar = selectedRecord.data.plantillaPrecios;
      }
      this.getFormSesiones().cargaComboStore('plantillaPrecios', idASeleccionar);
   },   
   
   preparaStoreSalas: function(eventoId) {
      var idASeleccionar = undefined;
      if (this.getGridSesiones().getSelectedColumnId() != undefined) {
         var selectedRecord = this.getGridSesiones().getSelectedRecord();
         idASeleccionar = selectedRecord.data.sala;
      }
      this.getFormSesiones().recargaComboStore('sala', idASeleccionar, urlPrefix + 'sala/evento/' + eventoId);

      this.cambiaSala(this.getComboSala(), this.getComboSala().value);
   },     
   
   showImagenIfExists: function(comp, opts) {
      var record = comp.getRecord();
      
	   if (record != undefined && record.data["imagenSrc"]) {
         var idEvento = record.data["id"];

          var imagen = comp.down("#imagenInsertada");
          imagen.html = '<a href="' + urlPrefix + 'evento/' + idEvento + '/imagen" target="blank">' + UI.i18n.field.imagenInsertada + '</a>';

          var imagenPubli = comp.down("#imagenPubliInsertada");
          imagenPubli.html = '<a href="' + urlPrefix + 'evento/' + idEvento + '/imagenPubli" target="blank">' + UI.i18n.field.imagenPubliInsertada + '</a>';

         if (!readOnlyUser) {
            this.getBotonDeleteImagen().show();
            this.getBotonDeleteImagenPubli().show();
         }

      } else {
          this.getBotonDeleteImagen().hide();
          this.getBotonDeleteImagenPubli().hide();
      }
   },

   showMultisesion: function(comp, newValue, oldValue, eOpts ) {
      if (!allowMultisession)
         newValue = false;

      if (newValue)
      {
         this.getIcaaFieldset().hide();
         this.getIcaaMultisesionGridFieldset().show();
      }
      else
      {
         this.getGridEventosMultisesion().store.removeAll();
         this.getIcaaMultisesionGridFieldset().hide();
         this.getIcaaFieldset().show();
      }
   },

   deleteImage: function(button, event, opts) {
      if (confirm(UI.i18n.message.sureDeleteImage)) {
         var record = button.up('form').getRecord();
         var idEvento = record.data["id"];
         var grid = this.getGridEventos();
         
         Ext.Ajax.request({
           url : urlPrefix + 'evento/' + idEvento + '/imagen',
           method: 'DELETE',
           success: function (response) {
               alert(UI.i18n.message.deletedImage);

               button.up('window').close();
               grid.store.load();
           }, failure: function (response) {
              alert(UI.i18n.error.deletedImage);
           }
         });
      }
   },

   deleteImagePubli: function(button, event, opts) {
      if (confirm(UI.i18n.message.sureDeleteImagePubli)) {
         var record = button.up('form').getRecord();
         var idEvento = record.data["id"];
         var grid = this.getGridEventos();

         Ext.Ajax.request({
            url : urlPrefix + 'evento/' + idEvento + '/imagenPubli',
            method: 'DELETE',
            success: function (response) {
               alert(UI.i18n.message.deletedImagePubli);

               button.up('window').close();
               grid.store.load();
            }, failure: function (response) {
               alert(UI.i18n.error.deletedImagePubli);
            }
         });
      }
   },

   recargaStore: function(comp, opts) {
      this.getGridEventos().deseleccionar();
      this.getGridSesiones().store.proxy.url = urlPrefix + 'evento/-1/sesiones';
      this.getGridSesiones().store.loadPage(1);
      this.getGridEventos().store.loadPage(1);
   },

   addEvento: function(button, event, opts) {
      this.getGridEventos().showAddEventoWindow();
      this.showMultisesion(null, false);
   },

   editEvento: function(button, event, opts) {
      var isMultisesion = false;
      if (allowMultisession) {
         if (this.getGridEventos().hasRowSelected()) {
            var rec = this.getGridEventos().getSelectedRecord();
            if (rec && rec.data && rec.data.multisesion)
               isMultisesion = (rec.data.multisesion != undefined && rec.data.multisesion == 'on')?true:false;
         }
      }
      this.getGridEventos().edit('formEventos', undefined, undefined, 0.8);
      this.showMultisesion(null, isMultisesion);

      if (isMultisesion)
         this.recargaGridMultisesion(this.getGridEventos().getSelectedColumnId());
   },

   recargaGridMultisesion: function(idEventoPadre) {
      var url = urlPrefix + 'evento/' + idEventoPadre + '/peliculas';
      this.getGridEventosMultisesion().store.removeAll();
      this.getGridEventosMultisesion().store.proxy.url = url;
      this.getGridEventosMultisesion().store.load();
   },

   removeEvento: function(button, event, opts) {
      var gridSesiones = this.getGridSesiones();
      this.getGridEventos().remove(function (borradoCorrectamente) {
         if (borradoCorrectamente)
            gridSesiones.getStore().removeAll();
      });
   },

   saveEventoFormData: function(button, event, opts) {
      var grid = this.getGridEventos();
      var form = this.getFormEventos();
      this.getJsonEventosMultisesion().setValue(this.getGridEventosMultisesion().toJSON());
      form.saveFormData(grid, urlPrefix + 'evento', undefined, 'multipart/form-data', function(form, action) {
         if (action != undefined && action.response != undefined && action.response.responseText != undefined) {
            var respuesta = Ext.JSON.decode(action.response.responseText, true);
            var key = "UI.i18n.error." + respuesta.message;
            var msg = eval(key);

            if (msg != undefined)
               alert(msg);
            else
               alert(UI.i18n.error.formSave);
         }
      });
   },
   
   loadSesiones: function(selectionModel, record) {
      if (record[0]) {
         var storeSesiones = this.getGridSesiones().getStore();
         var eventoId = record[0].get("id");
         var url = urlPrefix + 'evento/' + eventoId + '/sesiones';
         storeSesiones.getProxy().url = url;
         storeSesiones.getProxy().extraParams = {
            activos: (!this.getCheckMostrarTodosEventos().checked).toString()
         };
         storeSesiones.load();
      }
   },
   
   addSesion: function(button, event, opts) {
	   if (this.getGridEventos().getSelectedColumnId()) {
         this.getGridSesiones().deseleccionar();
		   this.getGridSesiones().showAddSesionWindow();
      }
	   else
		   alert(UI.i18n.message.event);
   },
   
   saveSesionFormData: function(button, event, opts) {
      var form = this.getFormSesiones();
      var eventoId = this.getGridEventos().getSelectedColumnId();

      if (form.isValid()) {
         var sala = this.getFormSesiones().getForm().findField('sala');
         var grid = this.getGridSesiones();

         if (this.getCheckboxVentaOnline().value) {
            var dataInicialVentaOnline = this.getFechaInicioVentaOnline().value;
            var h = this.getHoraInicioVentaOnline().rawValue.split(":");
            dataInicialVentaOnline.setHours(h[0], h[1]);
            
            var dataFinalVentaOnline = this.getFechaFinVentaOnline().value;
            h = this.getHoraFinVentaOnline().rawValue.split(":");
            dataFinalVentaOnline.setHours(h[0], h[1]);

            if (dataInicialVentaOnline > dataFinalVentaOnline) {
               alert(UI.i18n.error.dataIniciPosterior);
               return;
            }
         }
         var dataSessio = this.getFechaCelebracion().value;
         h = this.getHoraCelebracion().rawValue.split(":");
         dataSessio.setHours(h[0], h[1]);
         
         if (dataSessio < dataFinalVentaOnline) {
            alert(UI.i18n.error.dataEventAnterior);
            return;
         }
         var strDataSessio = Ext.Date.format(dataSessio, 'd/m/Y H:i');
         var me = this;
         var sesionId = '&sesionid=' + this.getFormSesiones().getForm().findField('id').value;
         Ext.Ajax.request({
           url : urlPrefix + 'evento/' + eventoId + '?sala=' + sala.getValue() + '&fecha=' + strDataSessio + sesionId,
           method: 'GET',
           success: function (response) {
               var numeroSesionesMismaHora = Ext.JSON.decode(response.responseText, true);
               var guardar = true;
               
               if (numeroSesionesMismaHora.first > 0) {
                  if (numeroSesionesMismaHora.second > 0)
                  {
                     guardar = false;
                     alert(UI.i18n.message.recordatorioDevolverEntradas)
                  }
                  else {
                     if (!confirm(UI.i18n.message.confirmReprogramacio))
                        guardar = false;
                  }
               }
               
               if (guardar) {
                  if (me.getComboPlantillaPrecios().value == -1)
                     me.getFormSesiones().getForm().findField('preciosSesion').setValue(me.getGridPreciosSesion().toJSON());
                  else
                     me.getFormSesiones().getForm().findField('preciosSesion').setValue(undefined);

                  if (sala.getValue() == '')
                     sala.setValue(null);
               
                  form.saveFormData(grid, urlPrefix + 'evento/' + eventoId + '/sesiones', undefined, undefined, function (response, action) {
                    var error = Ext.JSON.decode(response.responseText, true);
                    if (error != null && error.codi != null && error.codi == 515)
                      alert(UI.i18n.error.sesionComprasCambioFecha);
                    else
                      alert(error.descripcio);
                  });
               }
           }, failure: function (response) {
              alert(UI.i18n.error.comprobandoSesionesMismaHora);
           }
         });
      }
   },
   
   removeSesion: function(button, event, opts) {
      this.getGridSesiones().remove();
   },
   
   editSesion: function(button, event, opts) {
      if (this.getGridEventos().hasRowSelected() && this.getGridSesiones().hasRowSelected()) {
         var rec = this.getGridSesiones().getSelectedRecord();
         if (!rec.data.anulada)
            this.getGridSesiones().edit('formSesiones', undefined, undefined, 0.8);
         else
            alert(UI.i18n.error.editSesionAnulada);
      }
      else
         alert(UI.i18n.error.eventSelected);
   },

   deleteEventoMultisesion: function() {
      console.log("del");
      this.getGridEventosMultisesion().remove();
   },

   showAddEventoMultisesion: function() {
      console.log("add");
      this.getGridEventosMultisesion().showAddPelicula();
   },

   addPeliculaMultisesion: function() {
      if (this.getComboPeliculas().value != undefined) {
         if (this.getFormPeliculaMultisesion().isValid()) {
            if (!this.getGridEventosMultisesion().containsId(this.getComboPeliculas().value)) {
               var record = {
                  id: this.getComboPeliculas().value,
                  tituloEs: this.getComboPeliculas().rawValue,
                  versionLinguistica: this.getComboVersionLinguisticaMultisesion().value
               };
               this.getGridEventosMultisesion().addItemToStore(record);
               this.getFormPeliculaMultisesion().up("window").close();
            } else
               alert(UI.i18n.error.peliculaJaAfegida);
         }
      }
   }
});