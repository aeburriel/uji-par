Ext.define('Paranimf.view.taquilla.GridSesionesTaquilla', {
   extend: 'Paranimf.view.EditBaseGrid',

   alias: 'widget.gridSesionesTaquilla',
   store: 'SesionesTaquilla',
    stateId: 'gridSesionesTaquilla',

   title: UI.i18n.gridTitle.sesionesCompras,

   dockedItems: [{
     xtype: 'pagingtoolbar',
     store: 'SesionesTaquilla',
     dock: 'bottom',
     displayInfo: true
   }],
   
   tbar: [{
	         xtype: 'button',
	         text: UI.i18n.button.comprar,
	         action: 'comprar'
   	  	  },
   	  	  {
 	         xtype: 'button',
 	         text: UI.i18n.button.reservar,
 	         action: 'reservar'
    	  }/*,
   	  	  {
  	         xtype: 'button',
  	         text: UI.i18n.button.verCompras,
  	         action: 'verCompras'
     	  }*/
   ],   
   
   comprar: function() {
	   console.log('COMPRAR ENTRADA');  
   },

   initComponent: function() {

    this.columns = [{
      dataIndex: 'id',
      hidden: true,
      text: UI.i18n.field.idIntern
    }, {
      hidden: true,
      dataIndex: 'horaCelebracion',
      text: UI.i18n.field.sessionTime
    }, {
      dataIndex: 'fechaCelebracion',
      text: UI.i18n.field.eventDate,
      format:'d/m/Y H:i',
      xtype: 'datecolumn',
      flex: 1
    }, {
      dataIndex: 'horaApertura',
      text: UI.i18n.field.opening,
      flex: 1
    }, {
      dataIndex: 'fechaInicioVentaOnline',
      text: UI.i18n.field.startOnlineSelling,
      xtype: 'datecolumn',
      flex: 1,
      renderer: function (val, p, rec) {
        return (!rec.data.canalInternet)?'-':Ext.util.Format.date(val, 'd/m/Y');
      }
    }, {
      dataIndex: 'fechaFinVentaOnline',
      text: UI.i18n.field.endOnlineSelling,
      xtype: 'datecolumn',
      flex: 1,
      renderer: function (val, p, rec) {
        return (!rec.data.canalInternet)?'-':Ext.util.Format.date(val, 'd/m/Y');
      }
    }, {
      dataIndex: 'butacasVendidas',
      text: UI.i18n.field.butacasVendidas,
      flex: 1
    }];

    this.callParent(arguments);
  },

  showComprarWindow: function(idSesion, asientosNumerados, title, modoReserva) {
  	//console.log("showComprarWindow: ", idSesion);  
  	var window = this.createPercentageModalWindow('formComprar', 0.95, 0.95, title).show();
  	var cardLayout = Ext.getCmp('pasoSeleccionar').getLayout();
  	
  	if (asientosNumerados)
  	{
  		cardLayout.setActiveItem(0);
  		var url = urlPublic + "/rest/entrada/butacasFragment/" + idSesion + "?if=true";
  		
  		if (modoReserva)
  			url += '&reserva=true';
  		
  		Ext.getDom('iframeButacas').src = url;
  	}
  	else
  		cardLayout.setActiveItem(1);
  }
});