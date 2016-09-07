Ext.define('Paranimf.controller.Informes', {
   extend: 'Ext.app.Controller',

   views: ['EditModalWindow', 'EditBaseForm', 'EditBaseGrid', 'informes.PanelInformes', 'informes.GridSesionesInformes'],
   stores: ['SesionesInformes', 'TipoInformes', 'TiposInformesGenerales'],
   models: ['Sesion', 'Informe'],

   refs: [{
      ref: 'fechaInicio',
      selector: 'panelInformes datefield[name=fechaInicio]'
   }, {
      ref: 'fechaFin',
      selector: 'panelInformes datefield[name=fechaFin]'
   }, {
      ref: 'gridSesionesInformes',
      selector: 'gridSesionesInformes'
   }, {
      ref: 'panelInformesCombo',
      selector: 'panelInformes combo[name=comboInformesSesiones]'
   }, {
      ref: 'comboInformesGenerales',
      selector: 'panelInformes combo[name=comboInformesGenerales]'
   }],

   init: function() {
      this.control({

         'panelInformes button[action=generateExcelTaquilla]': {
           click: this.generateExcelTaquilla
         },

         'panelInformes button[action=generatePdfTaquilla]': {
           click: this.generatePdfTaquilla
         },   
          
         'panelInformes button[action=generatePdfEfectivo]': {
           click: this.generatePdfEfectivo
         },      
         
         'panelInformes button[action=generatePdfTpv]': {
           click: this.generatePdfTpv
         },

         'panelInformes button[action=generatePdfTpvOnline]': {
            click: this.generatePdfTpvOnline
         },

         'panelInformes button[action=generatePdfEventos]': {
           click: this.generatePdfEventos
         },          

         'panelInformes button[action=generateExcelEvento]': {
           click: this.generateExcelEvento
         }, 

         'panelInformes button[action=filtrarSessions]': {
            click: this.filtraSesiones
         },

         'panelInformes button[action=generarInforme]': {
            click: this.generarInforme
         },

         'panelInformes button[action=generarInformeGeneral]': {
            click: this.generarInformeGeneral
         }
      });
   },

   generarInformeGeneral: function() {
      if (!this.sonFechasValidas(this.getFechaInicio().value, this.getFechaFin().value))
         return;

      if (this.getComboInformesGenerales().value) {
         var rec = this.getComboInformesGenerales().store.getById(this.getComboInformesGenerales().value);
         var suffix = (rec.data.suffix != undefined && rec.data.suffix != '')?rec.data.suffix:'';
         var prefix = (rec.data.prefix != undefined && rec.data.prefix != '')?rec.data.prefix:'';
         var url = urlPrefix + 'report/' + prefix + this.getStrFecha(this.getFechaInicio().value) + '/' + 
            this.getStrFecha(this.getFechaFin().value) + suffix;
         this.generateInformeGeneral(url);
      } else
         console.log("no entra");
   },

   generarInformeSesion: function(anuladas) {
      var idsSelected = this.getGridSesionesInformes().getSelectedColumnValues("id");
      if (idsSelected && idsSelected.length == 1) {
         this.generatePdfSesion(idsSelected[0], anuladas);
      } else
         alert(UI.i18n.message.selectRow);
   },

   generarInformeEvento: function(anuladas) {
      var idsSelected = this.getGridSesionesInformes().getSelectedColumnValues("evento");
      if (idsSelected && idsSelected.length == 1) {
         this.generatePdfEvento(idsSelected[0].id, anuladas);
      } else
         alert(UI.i18n.message.selectRow);
   },

   generarInformeTipo: function(idSelected) {
      var sesionSelected = this.getGridSesionesInformes().getSelectedColumnValues("id");
      var eventoSelected = this.getGridSesionesInformes().getSelectedColumnValues("evento");
      if (sesionSelected && sesionSelected.length == 1 && eventoSelected && eventoSelected.length == 1) {
         this.generatePdf(eventoSelected[0].id, sesionSelected[0], idSelected);
      } else
         alert(UI.i18n.message.selectRow);
   },

   generarInforme: function() {
      var idSelected = this.getPanelInformesCombo().getValue();
      if (idSelected) {
         if (idSelected === 'informeSesion') {
            this.generarInformeSesion(true);
         }
         else if (idSelected === 'informeSesionNoAnuladas') {
            this.generarInformeSesion(false);
         }
         else if (idSelected === 'informeEventos') {
            this.generarInformeEvento(true);
         }
         else if (idSelected === 'informeEventosNoAnuladas') {
            this.generarInformeEvento(false);
         }
         else {
            this.generarInformeTipo(idSelected);
         }
      } else
         alert(UI.i18n.message.selectInforme);
   },

   filtraSesiones: function() {
      if (!this.sonFechasValidas(this.getFechaInicio().value, this.getFechaFin().value))
         return;

      var storeSesiones = this.getGridSesionesInformes().getStore();
      storeSesiones.getProxy().url = urlPrefix + 'evento/sesionesficheros/todo';
      storeSesiones.getProxy().extraParams = {
         'fechaInicio': this.getFechaInicio().rawValue,
         'fechaFin': this.getFechaFin().rawValue
      };
      storeSesiones.load();
   },

   generateExcelTaquilla: function(button, event, opts) {
      console.log(this.getFechaInicio(), this.getFechaFin());
      
      if (!this.sonFechasValidas(this.getFechaInicio().value, this.getFechaFin().value))
         return;

      var strFechaInicio = this.getStrFecha(this.getFechaInicio().value);
      var strFechaFin = this.getStrFecha(this.getFechaFin().value);

      var form = document.createElement("form");
      form.setAttribute("method", "post");
      form.setAttribute("action", urlPrefix + 'report/taquilla/' + strFechaInicio + '/' + strFechaFin);
      form.setAttribute("target", "_blank");

      document.body.appendChild(form);
      form.submit();
   }, 

   generatePdfSesion: function(idSesion, anuladas) {
      var form = document.createElement("form");
      form.setAttribute("method", "post");
      form.setAttribute("action", urlPrefix + 'report/sesion/' + idSesion + '/pdf' + (anuladas ? '' : '/noanuladas'));
      form.setAttribute("target", "_blank");

      document.body.appendChild(form);
      form.submit();      
   },

   generatePdfEvento: function(idEvento, anuladas) {
      var form = document.createElement("form");
      form.setAttribute("method", "post");
      form.setAttribute("action", urlPrefix + 'report/evento/' + idEvento + '/pdf' + (anuladas ? '' : '/noanuladas'));
      form.setAttribute("target", "_blank");

      document.body.appendChild(form);
      form.submit();      
   },

   generateInformeGeneral: function(url) {
      var form = document.createElement("form");
      form.setAttribute("method", "post");
      form.setAttribute("action", url);
      form.setAttribute("target", "_blank");

      document.body.appendChild(form);
      form.submit();
   },

   generatePdf: function(idEvento, idSesion, idSelected) {
      var form = document.createElement("form");
      form.setAttribute("method", "post");
      form.setAttribute("action", urlPrefix + 'report/evento/' + idEvento + '/sesion/' + idSesion + '/pdf/' + idSelected);
      form.setAttribute("target", "_blank");

      document.body.appendChild(form);
      form.submit();      
   },
   
   generatePdfTaquilla: function(button, event, opts) {
      console.log(this.getFechaInicio(), this.getFechaFin());
      
      if (!this.sonFechasValidas(this.getFechaInicio().value, this.getFechaFin().value))
         return;

      var strFechaInicio = this.getStrFecha(this.getFechaInicio().value);
      var strFechaFin = this.getStrFecha(this.getFechaFin().value);

      var form = document.createElement("form");
      form.setAttribute("method", "post");
      form.setAttribute("action", urlPrefix + 'report/taquilla/' + strFechaInicio + '/' + strFechaFin + '/pdf');
      form.setAttribute("target", "_blank");

      document.body.appendChild(form);
      form.submit();
   },    
   
   generatePdfEfectivo: function(button, event, opts) {
      console.log(this.getFechaInicio(), this.getFechaFin());
      
      if (!this.sonFechasValidas(this.getFechaInicio().value, this.getFechaFin().value))
         return;

      var strFechaInicio = this.getStrFecha(this.getFechaInicio().value);
      var strFechaFin = this.getStrFecha(this.getFechaFin().value);

      var form = document.createElement("form");
      form.setAttribute("method", "post");
      form.setAttribute("action", urlPrefix + 'report/taquilla/' + strFechaInicio + '/' + strFechaFin + '/efectivo/pdf');
      form.setAttribute("target", "_blank");

      document.body.appendChild(form);
      form.submit();
   },    
   
   generatePdfTpv: function(button, event, opts) {
      console.log(this.getFechaInicio(), this.getFechaFin());
      
      if (!this.sonFechasValidas(this.getFechaInicio().value, this.getFechaFin().value))
         return;

      var strFechaInicio = this.getStrFecha(this.getFechaInicio().value);
      var strFechaFin = this.getStrFecha(this.getFechaFin().value);

      var form = document.createElement("form");
      form.setAttribute("method", "post");
      form.setAttribute("action", urlPrefix + 'report/taquilla/' + strFechaInicio + '/' + strFechaFin + '/tpv/pdf');
      form.setAttribute("target", "_blank");

      document.body.appendChild(form);
      form.submit();
   },

   generatePdfTpvOnline: function(button, event, opts) {
      console.log(this.getFechaInicio(), this.getFechaFin());

      if (!this.sonFechasValidas(this.getFechaInicio().value, this.getFechaFin().value))
         return;

      var strFechaInicio = this.getStrFecha(this.getFechaInicio().value);
      var strFechaFin = this.getStrFecha(this.getFechaFin().value);

      var form = document.createElement("form");
      form.setAttribute("method", "post");
      form.setAttribute("action", urlPrefix + 'report/taquilla/' + strFechaInicio + '/' + strFechaFin + '/tpv/online/pdf');
      form.setAttribute("target", "_blank");

      document.body.appendChild(form);
      form.submit();
   },

   generatePdfEventos: function(button, event, opts) {
      console.log(this.getFechaInicio(), this.getFechaFin());
      
      if (!this.sonFechasValidas(this.getFechaInicio().value, this.getFechaFin().value))
         return;

      var strFechaInicio = this.getStrFecha(this.getFechaInicio().value);
      var strFechaFin = this.getStrFecha(this.getFechaFin().value);

      var form = document.createElement("form");
      form.setAttribute("method", "post");
      form.setAttribute("action", urlPrefix + 'report/taquilla/' + strFechaInicio + '/' + strFechaFin + '/eventos/pdf');
      form.setAttribute("target", "_blank");

      document.body.appendChild(form);
      form.submit();
   },    

   sonFechasValidas: function(fechaInicio, fechaFin) {
      if (!fechaInicio || fechaInicio == '' || !fechaFin || fechaFin == '') {
         alert(UI.i18n.error.fechasObligatorias);
         return false;
      }
      return true;
   },

   getStrFecha: function(fecha) {
      var dt = fecha;
      var mes = dt.getMonth() + 1;
      mes = (mes>9)?mes:'0' + mes;
      var dia = (dt.getDate() > 9)?dt.getDate():'0' + dt.getDate();
      return dt.getFullYear() + '-' + mes + '-' + dia;
   },

   generateExcelEvento: function(button, event, opts) {
      console.log(this.getFechaInicio(), this.getFechaFin());
      
      if (!this.sonFechasValidas(this.getFechaInicio().value, this.getFechaFin().value))
         return;

      var strFechaInicio = this.getStrFecha(this.getFechaInicio().value);
      var strFechaFin = this.getStrFecha(this.getFechaFin().value);

      var form = document.createElement("form");
      form.setAttribute("method", "post");
      form.setAttribute("action", urlPrefix + 'report/eventos/' + strFechaInicio + '/' + strFechaFin);
      form.setAttribute("target", "_blank");

      document.body.appendChild(form);
      form.submit();
   }
});