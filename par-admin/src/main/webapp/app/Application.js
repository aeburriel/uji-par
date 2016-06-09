Ext.Loader.setConfig({
   enabled: true/*,
   paths: {
      "Extensible": "extensible/src",
      "Extensible.example": "extensible/examples"
   }*/
});

Ext.Ajax.on('requestexception', function (conn, response, options) {
   if (response.status === 403)
      window.location = urlPrefix + 'index';
});

delete Ext.tip.Tip.prototype.minWidth;
  
if(Ext.isIE10) { 
   Ext.override(Ext.tip.Tip, {
      componentLayout: {
         type: 'fieldset',
         getDockedItems: function() {
            return [];
         }
      }
   });
}
Ext.QuickTips.init();

Ext.application({
   name: 'Paranimf',
   autoCreateViewport: false,
   appFolder: '../app',
   controllers: controllers,

   launch: function() {
      Ext.create('Ext.container.Viewport', {
         layout: 'border',
         defaults: {
        	 border: false,
             bodyPadding: 0,
             margins: '5 0 0 0',
             split: true,
             autoScroll: true
         },
         items: [{
            xtype: 'panel',
            region: 'west',
            title: UI.i18n.gridTitle.menu,
            layout: 'anchor',
            collapsible: true,
            margins: '0 0 0 0',
            padding: '0',
            width: 215,
            minWidth: 220,
            maxWidth: 220
         }, {
            layout: 'vbox',
            region: 'center',
            border: false,
            defaults: {
            	border: false
            },
            items: [{
               xtype: 'form',
               height: 25,
               region: 'north',
               width: '100%',
               items: [{
                  xtype: 'button',
                  text: UI.i18n.button.closeSession,
                  style: 'float: right;margin-right:2em',
                  action: 'logout'
               },{
                  id: 'langs',
                  xtype: 'label',
                  style: 'float: right;margin-right:1em;margin-top:6px'
               },{
                  xtype: 'label',
                  text: botonLogout,
                  style: 'float: right;margin-right:1em;font-weight:bold;margin-top:5px'
               }]
            }, {
               style : 'margin:2em;',
               xtype: 'panel',
               layout: 'card',
               region: 'south',
               width: '100%',
               autoScroll: true,
               border: false,
               flex: 1,
               items: views,
               listeners: {
                  afterlayout: function(container, layout, opts) {
                     document.getElementById("divCargador").style.display = 'none';
                  }
               }
            }]
         }]
      });

      if (typeof langsAllowed !== "undefined") {
         if (langsAllowed && langsAllowed.length > 1)
         {
           var langsHtml = "";
           for (var i = 0; i < langsAllowed.length; i++) {
             langsHtml += '<label class="idioma" style="margin-right:1em;"><a alt="' + langsAllowed[i].alias + '" title="' + langsAllowed[i].alias + '" href="javascript:cambiaIdioma(\'' + langsAllowed[i].locale + '\');">' + langsAllowed[i].alias.substring(0, 3) + '</a></label>'
           }
           Ext.getCmp('langs').update(langsHtml);
         }
      }

      Ext.History.init(this.initDispatch, this);
      Ext.History.on('change', this.historyChange, this);
      
      if (Ext.History.getToken() == null) {
         Ext.History.add('Dashboard', true);
      }

      this.loadMenuContent();
   },

   initDispatch: function() {
      this.historyChange(Ext.History.getToken());
   },

   historyChange: function(token) {
      var cardPanel = Ext.ComponentQuery.query('viewport > panel[region=center] > panel[region=south]')[0];
      var itemIndex = screens[token];

      cardPanel.getLayout().setActiveItem(itemIndex);
   },

   loadMenuContent: function() {
      var menuPanel = Ext.ComponentQuery.query('viewport > panel[region=west]')[0];
      menuPanel.update(menuHtml, false, function() {
         seleccionaMenu(menu);
      });
   }
});
