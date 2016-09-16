Ext.define('Paranimf.view.generarficheros.GridSesionesCompleto', {
   extend: 'Paranimf.view.EditBaseGrid',

   alias: 'widget.gridSesionesCompleto',
   store: 'SesionesFicheros',
    stateId: 'gridSesionesCompleto',

   title: UI.i18n.gridTitle.sesionesICAA,
   tbar: [{
      xtype: 'button',
      text: UI.i18n.button.saveFileICAA,
      action: 'saveFileICAA',
      hidden: (readOnlyUser == undefined)?false:readOnlyUser
   }, {
    xtype: 'button',
    text: UI.i18n.button.marcarComoEnviados,
    action: 'markAsSent',
    hidden: (readOnlyUser == undefined)?false:readOnlyUser
   }, {
    xtype: 'button',
    text: UI.i18n.button.setIncidencia,
    action: 'setIncidencia',
    //hidden: (readOnlyUser == undefined)?false:readOnlyUser
    hidden: true
   }],

   dockedItems: [],

   selModel: {
    mode: 'MULTI'
   },
   selType: 'checkboxmodel',

   initComponent: function() {

    this.columns = [{
      dataIndex: 'id',
      hidden: true,
      text: UI.i18n.field.idIntern
    }, {
      dataIndex: 'idEnvioFichero',
      hidden: true,
      text: UI.i18n.field.idEnvioFichero
    }, {
      dataIndex: 'incidenciaId',
      hidden: true,
      text: UI.i18n.field.incidenciaCodigo
    }, {
      dataIndex: 'tituloEs',
      flex: 3,
      text: UI.i18n.field.tituloMulti
    }, {
      dataIndex: 'fechaCelebracion',
      text: UI.i18n.field.eventDate,
      format:'d/m/Y H:i',
      xtype: 'datecolumn',
      flex: 1
    }, {
      dataIndex: 'horaApertura',
      text: UI.i18n.field.opening,
      flex: 1,
      hidden: true
    }, {
      dataIndex: 'fechaGeneracionFichero',
      text: UI.i18n.field.ficheroICAAgenerado,
      format:'d/m/Y H:i',
      xtype: 'datecolumn',
      flex: 1
    }, {
      dataIndex: 'fechaEnvioFichero',
      text: UI.i18n.field.ficheroICAAenviado,
      format:'d/m/Y H:i',
      xtype: 'datecolumn',
      flex: 1
    }, {
      dataIndex: 'tipoEnvio',
      text: UI.i18n.field.tipoEnvio,
      flex: 1,
      renderer: function(value) {
        if (value == 'FL')
          return UI.i18n.field.envioHabitual
        else if (value == 'AT')
          return UI.i18n.field.envioRetrasado
      }
    }, {
      dataIndex: 'incidenciaNombre',
      text: UI.i18n.field.incidencia,
      flex: 1,
      renderer: function(value, p)  {
        var txt = eval("UI.i18n.incidencias.i" + p.record.data.incidenciaId);
        p.tdAttr = 'data-qtip="' + txt + '"';
        return txt;
      }
    }];

    this.callParent(arguments);
  },

  showDatosFicheroForm: function() {
    this.createPercentageModalWindow('formDatosFichero', undefined, undefined, UI.i18n.formTitle.datosFichero).show();
  },

  showIncidenciasForm: function() {
    this.createPercentageModalWindow('formIncidencias', undefined, undefined, UI.i18n.formTitle.incidencias).show();
  }
});