Ext.define('Paranimf.view.evento.GridEventos', {
   extend: 'Paranimf.view.EditBaseGrid',

   alias: 'widget.gridEventos',
   store: 'Eventos',
    stateId: 'gridEventos',

   title: UI.i18n.gridTitle.eventos,

   tbar: [{
     xtype: 'checkbox',
     fieldLabel: UI.i18n.field.eventosAcabados,
     name: 'mostrarTodos',
     labelWidth: 180,
     labelAlign: 'right'
   }],

   dockedItems: [{
     xtype: 'pagingtoolbar',
     store: 'Eventos',
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
        flex: 2,
        dataIndex: 'fechaPrimeraSesion',
        text: UI.i18n.field.fechaPrimeraSesion,
        renderer: function(val) {
          if (val != '' && val != undefined) {
            var dt = new Date(val);
            return Ext.Date.format(dt, 'd/m/Y H:i');
          }
          return '';
        }
    }, {
        dataIndex: 'parTiposEvento',
        text: UI.i18n.field.type,
        flex: 2,
        renderer: function (val, p) {
          return val["nombreEs"];
        }
      }, {
        dataIndex: 'tituloEs',
        text: UI.i18n.field.title,
        flex: 5
      }, {
        dataIndex: 'tituloVa',
        text: UI.i18n.field.titolNeutreLabel,
        hidden: langsAllowed && langsAllowed.length > 1 ? false : true,
        flex: 5
      }, {
        dataIndex: 'imagenSrc',
        text: UI.i18n.field.imagen,
        flex: 3,
        renderer: function (val, record, p) {
          if (val != undefined) {
            return '<a href="' + urlPrefix + 'evento/' + p.data.id + '/imagen" target="blank">' + UI.i18n.field.imagenInsertada + '</a>'
          }
        }
      }, {
          dataIndex: 'imagenPubliSrc',
          text: UI.i18n.field.imagenPubli,
          flex: 3,
          renderer: function (val, record, p) {
              if (val != undefined) {
                  return '<a href="' + urlPrefix + 'evento/' + p.data.id + '/imagenPubli" target="blank">' + UI.i18n.field.imagenPubliInsertada + '</a>'
              }
          }
      },{
          dataIndex: 'rssId',
          text: UI.i18n.field.rssId,
          hidden: true
      },{
        text: UI.i18n.field.urlPublica,
        dataIndex: 'id',
        flex: 4,
        renderer: function(value) {
          return urlPublic + '/rest/evento/id/' + value;
        }
      }];

      this.callParent(arguments);
   },

   showAddEventoWindow: function() {
      this.createPercentageModalWindow('formEventos', undefined, 0.8).show();
   }
});