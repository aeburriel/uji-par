Ext.define('Paranimf.view.compra.GridCompras', {
   extend: 'Paranimf.view.EditBaseGrid',

   alias: 'widget.gridCompras',
   store: 'Compras',
   title: UI.i18n.gridTitle.comprasReservasHechas,
    stateId: 'gridCompras',

  tbar:[{
    action: 'anular',
    text: UI.i18n.button.anular
  },{
    action: 'desanular',
    text: UI.i18n.button.desanular
  }, {
    action: 'passToCompra',
    text: UI.i18n.button.passToCompra
  }, {
    xtype: 'checkbox',
    action: 'showAnuladas',
    fieldLabel: UI.i18n.field.mostrarAnuladas,
    labelWidth: 110
  }, '-', {
    xtype: 'checkbox',
    action: 'showOnline',
    fieldLabel: UI.i18n.field.mostrarOnline,
    labelWidth: 90
  }],
   
   dockedItems: [{
     xtype: 'pagingtoolbar',
     store: 'Compras',
     dock: 'bottom',
     displayInfo: true
   }],

  viewConfig: {
    enableTextSelection: true,
    getRowClass: function(record) {
      if (record && record.data.anulada)
        return 'gridAnulada'
      else if(record && record.data.reserva)
        return 'gridReserva';
      else if (record && record.data.taquilla)
        return 'gridTaquilla';
      else
        return 'gridOnline';
    }
  },

  initComponent: function() {

    this.columns = [{
      dataIndex: 'id',
      text: UI.i18n.field.idIntern,
      hidden: true
    }, {
      dataIndex: 'uuid',
      text: UI.i18n.field.uuid,
      hidden: true
    }, {
      dataIndex: 'fecha',
      text: UI.i18n.field.date,
      format:'d/m/Y H:i',
      xtype: 'datecolumn',          
      flex: 5
    }, {
      hidden: true,
      dataIndex: 'desde',
      text: UI.i18n.field.inicioReserva,
      xtype: 'datecolumn',
      format: 'd/m/Y H:i',
      flex: 3
    }, {
      hidden: true,
      dataIndex: 'hasta',
      text: UI.i18n.field.finReserva,
      xtype: 'datecolumn',
      format: 'd/m/Y H:i',
      flex: 3
    }, {
      dataIndex: 'nombre',
      text: UI.i18n.field.nameMulti,
      flex: 5,
      hidden: true
    }, {
      dataIndex: 'apellidos',
      text: UI.i18n.field.surnameMulti,
      flex: 5,
      hidden: true
    }, {
      dataIndex: 'email',
      flex: 5,
      text: UI.i18n.field.email,
      hidden: true
    }, {
      dataIndex: 'telefono',
      flex: 2,
      text: UI.i18n.field.phone,
      hidden: true
    }, {
      align: 'center',
      dataIndex: 'importe',
      flex: 2,
      text: UI.i18n.field.importe,
      renderer: function(val) {
        return (val == 0)?'':val.toFixed(2) + '€';
      }
    },{
      align: 'center',
      dataIndex: 'taquilla',
      flex: 2,
      text: UI.i18n.field.taquilla,
      renderer: function (val, p) {
          return (val)?'<img src="../resources/images/tick.png" alt="Sí" title="Sí" />':'';
      }
    }, {
      align: 'center',
      flex: 2,
      text: UI.i18n.field.online,
      renderer: function (val, p, rec) {
        return (!rec.data.taquilla)?'<img src="../resources/images/tick.png" alt="Sí" title="Sí" />':'';
      }
    }, {
      align: 'center',
      dataIndex: 'reserva',
      flex: 2,
      text: UI.i18n.field.reserva,
      renderer: function (val, p) {
        return (val)?'<img src="../resources/images/tick.png" alt="Sí" title="Sí" />':'';
      }
    }, {
      align: 'center',
      dataIndex: 'pagada',
      flex: 2,
      text: UI.i18n.field.pagada,
      renderer: function (val, p) {
          return (val)?'<img src="../resources/images/tick.png" alt="Sí" title="Sí" />':'<img src="../resources/images/cross.png" alt="Sí" title="Sí" />';
      }
    }, {
        align: 'center',
        dataIndex: 'caducada',
        flex: 2,
        text: UI.i18n.field.caducada,
        renderer: function (val, p) {
        	return (val)?'<img src="../resources/images/tick.png" alt="Sí" title="Sí" />':'';
        }
    }, {
      dataIndex: 'observacionesReserva',
      text: UI.i18n.field.observacionesReserva,
      hidden: true,
      flex: 2
    }, {
      flex: 2,
      align: 'center',
      text: UI.i18n.message.printTaquilla,
      renderer: function(val, p, rec) {
        if (!rec.data.reserva && !rec.data.anulada)
        return '<a target="blank" href="' + urlPrefix + 'compra/' + rec.data.uuid + '/pdftaquilla">' + UI.i18n.message.print + '</a>';
        else
          return '';
      }
    }, {
      flex: 2,
      align: 'center',
      text: UI.i18n.message.printAtHome,
      renderer: function(val, p, rec) {
        if (!rec.data.reserva && !rec.data.anulada)
        return '<a target="blank" href="' + urlPrefix + 'compra/' + rec.data.uuid + '/pdf">' + UI.i18n.message.print + '</a>';
        else
          return '';
      }
    }, {
       dataIndex: 'idDevolucion',
       text: UI.i18n.field.idDevolucion,
       renderer: function (val, p) {
    	   if (val)
    		   return val;
    	   else
    		   return '';
       },
       hidden: false,
       flex: 2
    }];

    this.callParent(arguments);
  },

  showAddCompraWindow: function() {
    this.createPercentageModalWindow('formCompras').show();
  },

  showFormasDePagoWindow: function(callback) {
    this.createPercentageModalWindow('formFormasDePago', undefined, undefined, UI.i18n.formTitle.formasPago, false, callback).show();
  }
});