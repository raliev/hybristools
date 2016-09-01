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
// LIVE EDIT CONTENT SLOT MENU

(function($) {
	
	// NAMESPACE LiveEditSlotMenu
	LiveEditSlotMenu = function() {};
	
	LiveEditSlotMenu.tooltip = function(action) {
		var message_key = 'liveeditaddon.message.slot.tooltip.action.'+action.toLowerCase();
		if(ACC.addons.liveeditaddon[message_key] != undefined)
			return ACC.addons.liveeditaddon[message_key];
		else 
			return '';
	};
	
	LiveEditSlotMenu.getJSON = function(options, successCallback, errorsCallback) {
		var defaults = {
			url: '/liveeditaddon/slot/menu.json', // url for json call
			data: {
				previewTicket: '',
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
	
	LiveEditSlotMenu.buildRequestData = function(cmsTicketId, cmsContentSlotUid, pageUid, position) {
		return {
				data: {
					previewTicket: cmsTicketId,
					slotUid: cmsContentSlotUid, 
					pageUid: pageUid,
					position: position,
					url: window.location.href
				}
		};
	}
	
	LiveEditSlotMenu.responseError = function(json) {
		console.log('Error response for LiveEditContextMenu JSON call');
		console.log('Response: ');
		console.log(json);
	}
	
	LiveEditSlotMenu.callAction = function(action, data) {
		
		var NAMESPACE = 'LiveEditSlotAction';
		
		function callActionByName(functionName, context /*, args */) {
			var args = Array.prototype.slice.call(arguments).splice(2);
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
		callActionByName(ACTION_NAME, window, data);
	};
	
	LiveEditSlotMenu.slotData = function(slot) {
		
		var pageId = ACC.previewCurrentPageUid;
		var slotId = slot.data('cmsContentSlot');
		var position = slot.data('cmsContentSlotPosition');
		var cmsTicketId = $.getUrlVar('cmsTicketId');
		
		var data = {
				"pageUid": pageId,
				"slotUid": slotId,
				"position": position,
				"previewTicket": cmsTicketId
		};
		
		return data;
	}
	
	$.extend($.fn, {
		liveEditSlotMenu: function(options) {
			// check if element exist
			if ( !this.length ) {
				if ( options && options.debug && window.console ) {
					console.warn( "No components found. Maybe you provided wrong component id/class" );
				}
				return;
			}
			
			var liveEditSlotMenu = $.data( this, "liveEditSlotMenu" );
			if ( liveEditSlotMenu ) {
				return this;
			}
			
			liveEditSlotMenu = new $.liveEditSlotMenu(options, this);
			$.data( this, "liveEditSlotMenu", liveEditSlotMenu );
			
			return this;
		}
	});
	
	// Constructor for liveEditContextMenu
	$.liveEditSlotMenu = function( options, component ) {
		this.settings = $.extend( {}, $.liveEditSlotMenu.defaults, options );
		this.component = component;
		this.element = null;
		this.init();
	};
	
	$.extend($.liveEditSlotMenu, {
		defaults: {			
			enableSlotLock: true,
			hidePseudo: "BOTH",
			hiddenPseudoAfterClass: "hiddenPseudoAfter",
			hiddenPseudoBeforeClass: "hiddenPseudoBefore",
			tabClass: "yContentSlotMenu",
			dataSlotExistEmpty: "cmsContentSlotEmpty",
			dataSlotMainName: "cmsContentSlotFromMaster",
			tabStructure: '<div class="%TAB_COMPONENT_CSS%"><div class="border-container"><div class="ycsm-left"></div><div class="ycsm-center"></div><div class="ycsm-right"></div><div style="clear:both"></div></div></div>',
			boxRedClass: 'box-red',
			boxGreenClass: 'box-green',
			tabLockClass: 'ycsm-left',
			tabTitleClass: 'ycsm-center',
			tabMenuClass: 'ycsm-right',
			baseActionList: [
			                 {name: 'ADD', actionType: 'ADD', enabled: true, render: 'all'},
			                 {name: 'MENU', actionType: 'MENU', enabled: true, render: 'slot'},
			                 {name: 'CREATE', actionType: 'CREATE', enabled: true, render: 'empty'}
			]
		},
		
		setDefaults: function( settings ) {
			$.extend( $.liveEditContextMenu.defaults, settings );
		},
		
		prototype: {
			init: function() {
				this.hidePseudoElements();
				this.buildMenuTab();
			},
			
			getSlotType: function($slot) {
				
				if($slot.data(this.settings.dataSlotExistEmpty) == true) {
					return 'EMPTY';
				} else if($slot.data(this.settings.dataSlotMainName) == true) {
					return 'MAIN'
				} else {
					return 'SLOT';
				}
				
			},
			
			buildMenuTab: function() {
				var _this = this;
				var divElement = this.settings.tabStructure.replace('%TAB_COMPONENT_CSS%', this.settings.tabClass);
				this.component.each(function(key, val) {
					var element = divElement;

					var _component = val;
					var $_component = $(_component);
					_this.renderTitleSlot(element, $_component);

					_this.renderMenuSlot($_component, _this.settings.baseActionList);
					
					var slotTabMenuClassName = '.'+_this.settings.tabClass;
					if(_this.getSlotType($_component) == 'MAIN') {
						_this.renderLockUnlockIcon($_component, $(element));
						$_component.find(slotTabMenuClassName).addClass(_this.settings.boxRedClass);
					}
				});
			},

			renderMenuSlot: function($component, $actionList) {
				var elements = this.renderBaseLvl($component, $actionList);
				var rightContainer = $component.children('.'+this.settings.tabClass).find('.'+this.settings.tabMenuClass);
				rightContainer.append(elements);
			},
			
			renderLockUnlockIcon: function($slot, $element) {
				if(this.settings.enableSlotLock && this.getSlotType($slot) == 'MAIN') {

                    var isLocked  = $slot.attr('data-cms-content-slot-locked');
					var item = {
							name: 'LOCK', actionType: (isLocked=="true"?'LOCK':'UNLOCK'), enabled: true, render: 'main'
					}
					
					var link_element = this.builder.linkElement($slot, item);
					$slot.children($element).find('.'+this.settings.tabLockClass).html(link_element);
				}
			},
			
			renderTitleSlot: function(element, $component) {
				switch(this.settings.hidePseudo) {
					case 'AFTER':
						var dataContent = this.getElementPseudoContent($component,'after');
						$component.append(element).children($(element)).find('.'+this.settings.tabTitleClass).html(dataContent);;
						break;
					case 'BEFORE':
						var dataContent = this.getElementPseudoContent($component,'before');						
						$component.prepend(element).children($(element)).find('.'+this.settings.tabTitleClass).html(dataContent);
						break;
					case 'BOTH':
						var beforeElement = element;
						var beforeDataContent = this.getElementPseudoContent($component,'before');
						
						var afterElement = element;
						var afterDataContent = this.getElementPseudoContent($component,'after');
						
						$component.append(afterElement).prepend(beforeElement);
						$component.children($(beforeElement)).find('.'+this.settings.tabTitleClass).html(beforeDataContent);
						$component.children($(afterElement)).find('.'+this.settings.tabTitleClass).html(afterDataContent);
				}
			},
			
			hidePseudoElements: function() {
				switch(this.settings.hidePseudo) {
					case 'AFTER':
						this.component.addClass(this.settings.hiddenPseudoAfterClass);
						break;
					case 'BEFORE':
						this.component.addClass(this.settings.hiddenPseudoBeforeClass);
						break;
					case 'BOTH':
						this.component.addClass(this.settings.hiddenPseudoBeforeClass).addClass(this.settings.hiddenPseudoAfterClass);
					default:
				}
			},
			
			getElementPseudoContent: function($component, type) {
				return $component.data('cmsContentSlotPosition');
			},
			
			renderBaseLvl: function($slot, list) {
				var content = this.builder.ulElement(0);
				var _this = this;
				
				var slot_type = _this.getSlotType($slot);
;
				$.each(list, function(key, item) {
					var item_type = item.render.toUpperCase();
					if(slot_type == item_type || item_type == 'ALL') {
				
						var li_element = _this.builder.liElement();
						var link_element = _this.builder.linkElement($slot, item);
						
						li_element.append(link_element);
						
						if(item.actionType == "MENU") {
							_this.bindEvent(link_element, $slot);
						}
						
						content.append(li_element);
					}
				});
				return content;
			},
			
			renderMenu: function(slot, list) {
				return this.builder.menuElement(slot, list, 1);
			},
			
			bindEvent: function(linkElement, slot) {
				var _this = this;
				linkElement.bind('click', function(event) {
					event.preventDefault();
					
					var slotData = LiveEditSlotMenu.slotData(slot);					
					var requestData = {
						data: slotData
					};
					
					LiveEditSlotMenu.getJSON(requestData, function(json) {
						if($('body').children('#ycms-menu-element').length > 0) {
							$('body').children('#ycms-menu-element').remove();
						}
						
						var menuElement = _this.renderMenu(slot, json.menu.items);
						menuElement.attr('id','ycms-menu-element');
						
						var parentLi = linkElement.parent();
						
						parentLi.children().not(linkElement[0]).remove();
						
						$('body').append(menuElement);
						var offset = linkElement.offset();
						var el_height = linkElement.height();
						menuElement.css('top', offset.top + el_height);
						menuElement.css('left', offset.left - 175);
						//parentLi.append(menuElement);
						
						menuElement.show();	
						
						$('body').one('click',function() {
							menuElement.hide();
						});
						
					}, function(json) {
						LiveEditSlotMenu.responseError(json);
					});
					
					event.stopPropagation();
				});
			},
			
			builder: {
				BUTTON_CLASS: 'ycsm-button-',
				
				ulElement: function(lvl) {
					return $('<ul class="ycms-ul ycms-ul-lvl-'+lvl+'"></ul>');
				},
				
				liElement: function() {
					return $('<li class="ycms-li"></li>');
				},
				
				linkElement: function(slot, item, showName) {
					var className = this.BUTTON_CLASS + item.actionType;
					var name = '';
					if(showName) {
						name = item.name;
					}
					var link_element = $('<a href="#" class="'+className+'">' + name + '</a>');
					
					var tooltip = LiveEditSlotMenu.tooltip(item.actionType);
					
					if(tooltip != '') {
						link_element.attr('title', tooltip);
					}
					
					if((!item.items || item.items.length == 0) && item.enabled && item.actionType != "MENU") {
						link_element.click(function(event) {
							event.preventDefault();
							var slotData = LiveEditSlotMenu.slotData(slot);
							LiveEditSlotMenu.callAction(item.actionType, slotData);
						});
					}
					
					if(!item.enabled) {
						link_element.addClass('disabled');
					}
					return link_element;
				},
				
				menuElement: function(slot, list, lvl) {
					var content = this.ulElement(lvl);
					var _this = this;
					$.each(list, function(key, item) {
						
						var li_element = _this.liElement();
						var link_element = _this.linkElement(slot, item, lvl == 0 ? false : true);
						
						li_element.append(link_element);
						
						if(item.items && item.items.length > 0) {
							var menu_element = _this.menuElement(slot, item.items, lvl + 1);
							_this.showStrategy.showHover(li_element, menu_element);
							li_element.append(menu_element);
						}
						
						content.append(li_element);
						
					});
					return content;
				},
				
				showHover: function(element, target) {
					element.hover(function(event) {
						target.show();
					}, function() {
						target.hide();
					});
				}
				
			}
		},
		
		
		
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