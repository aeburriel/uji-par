Ext.define('Paranimf.view.taquilla.PanelSeleccionarNoNumeradas', {
   	extend: 'Ext.panel.Panel',
   	alias: 'widget.panelSeleccionarNoNumeradas',
   	autoScroll: true,
   	border: false,
   	defaults: {
      anchor: '100%'
   	},
   	items: [{
		name: 'localizaciones',
		xtype: 'panel',
		border: 0,
		layout: 'column',
	    defaults: [{
	    	xtype: 'fieldset'
	    }],
		items:[]
	},{
		xtype: 'label',
		text: UI.i18n.field.totalCompra,
		name: 'totalPrecios',
		style: {
	    	fontSize: '13px',
	    	fontWeight: 'bold'
	    }
	}]
});