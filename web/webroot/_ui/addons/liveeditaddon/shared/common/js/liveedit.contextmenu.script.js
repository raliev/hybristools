/*
 * [y] hybris Platform
 *
 * Copyright (c) 2000-2016 SAP SE or an SAP affiliate company.
 * All rights reserved.
 *
 * This software is the confidential and proprietary information of SAP
 * ("Confidential Information"). You shall not disclose such Confidential
 * Information and shall use it only in accordance with the terms of the
 * license agreement you entered into with SAP.
 */
// LIVE EDIT CONTEXT MENU

(function($) {
	// NAMESPACE LiveEditContextMenu
	LiveEditContextMenu = function() {};
	
	// LiveEditContextMenu JSON request interface for getting menu structure
	LiveEditContextMenu.getJSON = function(options, successCallback, errorsCallback) {
		var defaults = {
			url: '/liveeditaddon/component/menu.json', // url for json call
			data: {
				previewTicket: '', 
				componentUid: '',
				slotUid: '', 
				pageUid: '',
				position: '',
				url: '',
				event: null
			} // request data
		};
		options = $.extend( true, {}, defaults, options );
		return $.getJSON(options.url, options.data)
			.done(function(json) {
				successCallback(json);
			})
			.fail(function(json) {
                if (json.status == 401){
                    document.location.href = document.location.href;
                }else {
                    errorsCallback(json);
                }
			});
	};
	
	LiveEditContextMenu.buildRequestData = function(cmsTicketId, cmsComponentUid, cmsContentSlotUid, pageUid, position) {
		return {
				data: {
					previewTicket: cmsTicketId, 
					componentUid: cmsComponentUid,
					slotUid: cmsContentSlotUid, 
					pageUid: pageUid,
					position: position,
					url: window.location.href
				}
		};
	}
	
	// LiveEditContextMenu JSON Success response
	LiveEditContextMenu.responseSuccess = function(json, componentData, event) {
		var ALIAS_PATTERN = "%parent%-%lvl%";
		var ICON_PATH = ACC.config.contextPath + "/_ui/addons/%ICON_ADDON%/shared/common/images/contextmenu_icons/%ICON%.png";
		var _componentData = componentData;
		var _menuItems = {items: []};
		var global_parent_lvl = 1;		
		
		function buildMenuStructure(items, item_lvl) {
			var parent_lvl = global_parent_lvl;
			global_parent_lvl = global_parent_lvl + 1;
			var menuItems = [];
			$.each(items, function(item_key, item) {
				var newItem = createItem(item,parent_lvl,item_key+1);
				
				if(item.items && item.items.length > 0) {
					newItem.items = buildMenuStructure(item.items, 1);
					newItem.type = 'group';
				}
				menuItems.push(newItem);
			});
			return menuItems;
		};
		
		function createItem(data, parent_lvl, item_lvl) {
			var aliasValue = ALIAS_PATTERN.replace('%parent%',parent_lvl).replace('%lvl%',item_lvl);
			var icon = ICON_PATH.replace('%ICON%', data.actionType);
			
			if(undefined!=data.addon)
				icon = icon.replace('%ICON_ADDON%',data.addon);
			else
				icon = icon.replace('%ICON_ADDON%','liveeditaddon');	
			
			return { 
				"text": data.name, 
				"icon": icon, 
				"alias": aliasValue, 
				"disable": !data.enabled,
				"action": function() {
					LiveEditContextMenu.callAction(data.actionType, _componentData, data);
				}
			};
		}
		_menuItems.items = buildMenuStructure(json.menu.items, 1);
		return _menuItems;
	}
	
	// LiveEditContextMenu JSON Error response
	// TODO: parse error messages and render liveedit popup with error message
	LiveEditContextMenu.responseError = function(json) {
		console.log('Error response for LiveEditContextMenu JSON call');
		console.log('Response: ');
		console.log(json);
	}
	
	LiveEditContextMenu.callAction = function(action, data, menuitemData) {
		
		var NAMESPACE = 'LiveEditAction';
		
		function callActionByName(functionName, context /*, args */) {
			var args = Array.prototype.slice.call(arguments).splice(2,2);
			var namespaces = functionName.split(".");
			var func = namespaces.pop();

			for(var i = 0; i < namespaces.length; i++) {
				context = context[namespaces[i]];
			}
			
			if(typeof context[func] == 'function') {
				return context[func].apply(this, args);
			} else {
				console.log('ERROR: function does not exist: ' + func);
			}
			
		}

		var ACTION_NAME = NAMESPACE + '.' + action;
		callActionByName(ACTION_NAME, window, data, menuitemData);
	}

	// Register liveEditContextMenu as a jQuery plugin
	// so you can do $('#component').liveEditContextMenu(options);
	$.extend($.fn, {
		liveEditContextMenu: function(options) {
			// check if element exist
			if ( !this.length ) {
				if ( options && options.debug && window.console ) {
					console.warn( "No components found. Maybe you provided wrong component id/class" );
				}
				return;
			}
			
			var liveEditContextMenu = $.data( this, "liveEditContextMenu" );
			if ( liveEditContextMenu ) {
				return this;
			}
			
			liveEditContextMenu = new $.liveEditContextMenu(options, this);
			$.data( this, "liveEditContextMenu", liveEditContextMenu );
			
			return this;
		}
	});
	
	// Constructor for liveEditContextMenu
	$.liveEditContextMenu = function( options, component ) {
		this.settings = $.extend( true, {}, $.liveEditContextMenu.defaults, options );
		this.component = component;
		this.element = null;
		this.init();
	};
	
	// liveEditContextMenu Plugin Implementation
	$.extend($.liveEditContextMenu, {
		defaults: {			
			rootContainerId: "#cmroot",
			componentCssClass: "yCmsComponent",
            topLevelComponentCssClass: ".yCmsContentSlot > div.sortable.ui-sortable > .yCmsComponent",
			slotCssClass: "yCmsContentSlot"
		},
		
		setDefaults: function( settings ) {
			$.extend( $.liveEditContextMenu.defaults, settings );
		},
		
		prototype: {
			// Called by constructor
			init: function() {
				this.bindRightClickEvent();
			},
		 
			// Binds mouse right click event
			bindRightClickEvent: function() {
				if(this.component != null) {
					_this = this;
					$(this.component).unbind('contextmenu');
					$(this.component).bind('contextmenu', function(event) {
						_this.rightClickCallback(this, event);
					});
					this.bindIframeCallback();
				}
			},
			
			bindIframeCallback: function() {
				var _this = this;
				var interval = setInterval(function () {
					
					window.clearInterval(interval);
					var component = _this.component;
					
					$(component).find('iframe').each(function(key, element){
						var $iframe = $(element);

						var width = $iframe.css('width');
						var height = $iframe.css('height');

						var cover = '<div style="position:absolute; z-index: 9999999; width: '+width+'; height: '+height+'"></div>';
						$iframe.css('position','relative').css('z-index','9999');
						
						$iframe.before($(cover));

						$(cover).click(function(e) {
						    e.preventDefault();
						});
					});
					
				},1000);
				
			},
		 
			// Mouse Right Click Callback
			rightClickCallback: function(element, event) {
				event.preventDefault();
				this.element = element;
				this.clearContextMenu();
				var requestData = this.setupRequestData(event);
				
				// Get menu structure by json call and then render menu
				LiveEditContextMenu.getJSON(requestData, function(json) {
					var responseFormated = LiveEditContextMenu.responseSuccess(json, requestData, event);
					_this.buildMenu(event, responseFormated);
				}, function(json) {
					// Error callback
					 LiveEditContextMenu.responseError(json); 
				});
			},
		 
			buildMenu: function(event, options) {
			
				$(this.element).contextmenu(options);
				this.showContextMenu(event);
				
				// Binds back liveEditContextMenu right click event 
				// because currently is set to contextmenu
				this.bindRightClickEvent();
			},
		 
			showContextMenu: function(event) {
				// Dummy action for showing menu by hand 
				// - because that feature isn't provided by contextmenu library
				var rootElement =  $(this.settings.rootContainerId)[0];
				rootElement.showMenu(event, this.element);
			},
		 
			clearContextMenu: function() {
				// Bugfix for contexmenu to support more menu structures then one
				var rootElement = $(this.settings.rootContainerId);
				if(rootElement) {
					rootElement.remove();
				}
			},
			
			setupRequestData: function(event) {
				var cmsComponent = this.findNearestCMSComponent(event);
				
				if (cmsComponent.length > 0)
				{					
					var cmsComponentUid = cmsComponent.data('cmsComponent');
					var cmsContentSlotUid = cmsComponent.data('cmsContentSlot');
					var pageUid = ACC.previewCurrentPageUid;
					var cmsTicketId = $.getUrlVar('cmsTicketId');		
					var position = this.getSlotPosition($(cmsComponent));
					return LiveEditContextMenu.buildRequestData(cmsTicketId, cmsComponentUid, cmsContentSlotUid, pageUid, position);
				}
				return LiveEditContextMenu.buildRequestData();
			},

            /**
             * Will return the parent component of the target that is a direct child of the slot or the component itself
             * this in order not to get contextual menu of inner component such as in the case of NavigationBarCollectionComponent.
             * where previously one would get context of a sub node leading to misunderstanding for syncing
             * @param event
             * @returns {*}
             */
			findNearestCMSComponent: function(event) {

                var component = $(event.target).parents(this.settings.topLevelComponentCssClass);
                if (component.length==0){
                    var component = $(event.target).closest(this.getComponentClassName());
                }

                return component;
			},
			
			getSlotPosition: function($component) {
				var slot_name = this.getSlotClassName();
				var slotComponent = $component.closest(slot_name);
				if(slotComponent && slotComponent.data('cmsContentSlotPosition') != undefined) {
					return slotComponent.data('cmsContentSlotPosition');
				} else {
					return "";
				}
				
			},
			
			getComponentClassName: function() {
				return '.' + this.settings.componentCssClass;
			},
			
			getSlotClassName: function() {
				return '.' + this.settings.slotCssClass;
			}
		}
	});
	
	$.extend({
		  getUrlVars: function(){
		    var vars = [], hash;
		    var hashes = window.location.href.slice(window.location.href.indexOf('?') + 1).split('&');
		    for(var i = 0; i < hashes.length; i++)
		    {
		      hash = hashes[i].split('=');
		      vars.push(hash[0]);
		      vars[hash[0]] = hash[1];
		    }
		    return vars;
		  },
		  getUrlVar: function(name){
		    return $.getUrlVars()[name];
		  }
	});
})(jQuery);