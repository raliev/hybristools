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
(function($) {
    $.fn.changeElementType = function(newType) {
        var attrs = {};
        $.each(this[0].attributes, function(idx, attr) {
            attrs[attr.nodeName] = attr.nodeValue;
        });

        this.replaceWith(function() {
            return $("<" + newType + "/>", attrs).append($(this).contents());
        });
    };
})(jQuery);

LiveEditSortable = {
	
		SELECTOR_CONTENT_SLOT: ".yCmsContentSlot",
		SELECTOR_CONTENT_SLOT_MENU: ".yContentSlotMenu",
		SELECTOR_CMS_COMPONENT: ".yCmsComponent",
        SELECTOR_NAVIGATION_COLLECTION: ".navigationbarcollectioncomponent",
		SELECTOR_FILEDROP: ".filedrop",
		SELECTOR_SORTABLE: ".sortable",
		
		init: function() {
			var _this = this;
            // WARNING: this code should not exist but we need it to convert live edit to proper html code
            $(this.SELECTOR_CONTENT_SLOT).each(function (key, slot) {
                var $slot = $(slot);
                if ($slot.is("ul")) {
                    var $sortable_container = $('<ul class="sortable clearfix"></ul>');
                    var slot_css_class = $slot.attr('class');
                    var $contentSlot = $slot.children(_this.SELECTOR_CMS_COMPONENT);
                    _this.addSortableContainerToSlot($slot, $sortable_container);

                    $sortable_container.addClass(slot_css_class).removeClass("yCmsContentSlot");
                    $contentSlot.appendTo($sortable_container);

                    $contentSlot.each(function(key,item) {
                        var $item = $(item);
                        var $children = $item.children();

                        if($children.length == 1) {
                            var $itemLi = $children.first();
                            if($itemLi.is('li')) {
                                $item.addClass($itemLi.attr('class'));
                                $item.append($itemLi.children());
                                $item.changeElementType("li");
                                $children.remove();
                            }
                        }
                    });

                } else if ($slot.parent().is('ul')) {
                    var $sortable_container = $('<ul class="sortable"></ul>');
                    _this.addSortableContainerToSlot($slot, $sortable_container);
                    $slot.children(_this.SELECTOR_CMS_COMPONENT).appendTo($sortable_container);
                    $slot.changeElementType('li');
                } else {
                    var $sortable_container = $('<div class="sortable"></div>');
                    _this.addSortableContainerToSlot($slot, $sortable_container);
                    $slot.children(_this.SELECTOR_CMS_COMPONENT).appendTo($sortable_container);
                }

                // Convert Navigation Bar Collection markup to proper html markup
                _this.transformNavigationBarCollection($slot);

            });
			this.bindSortable();			
		},

        transformNavigationBarCollection: function($slot) {
            var collectionContainer = $slot.find(this.SELECTOR_NAVIGATION_COLLECTION);
            if(collectionContainer.length > 0) {
                collectionContainer.each(function(containerIndex, containerElement) {
                    var children = $(containerElement).children("ul").children();
                    children.each(function(childIndex, childElement) {
                        var $childElement = $(childElement);
                        if($childElement.is("div")) {
                            var $sortable_container = $('<ul class="sortable"></ul>');
                            $childElement.children().appendTo($sortable_container);
                            $sortable_container.appendTo($childElement);
                            $childElement.changeElementType('li');
                        }
                    });
                });
            }
        },
		
		addSortableContainerToSlot: function($slot, $sortable) {
			if($slot.has(this.SELECTOR_FILEDROP).length > 0) {
				var $filedrop_container =  $slot.children(this.SELECTOR_FILEDROP);
				$filedrop_container.before($sortable);
			} else {
				$slot.append($sortable);
			}
		},
		
		bindSortable: function() {
			if($.fn.sortable != undefined) {
				var _this = this;
				$(this.SELECTOR_SORTABLE).sortable(
				{
					 cancel: _this.SELECTOR_CONTENT_SLOT_MENU,
					 connectWith: _this.SELECTOR_SORTABLE,
                     items:'> '+this.SELECTOR_CMS_COMPONENT,
                    start: function(event, ui) {
                        wscrolltop = $(window).scrollTop();
                    },
                    sort: function(event, ui) {
                        ui.helper.css({
                            'width' : '50px',
                            'left' : event.clientX + 'px',
                            'top' : event.clientY + wscrolltop + 'px'

                        });
                    },
                    helper: function(e, elt) {
                        return "<div>"+elt.html()+"</div>";
                    },
					 update: function(event, ui) {
						var newIndex = ui.item.index();
						var componentId = ui.item.attr("data-cms-component");
						//console.log("*** item = " + ui.item);
	
						var fromContentSlotId = ui.item.attr("data-cms-content-slot");
						//console.log("*** content slotId = " + fromContentSlotId);
						
						var toContentSlotId = ui.item.parents(_this.SELECTOR_CONTENT_SLOT).attr("data-cms-content-slot");
						var toPosition = ui.item.parents(_this.SELECTOR_CONTENT_SLOT).attr("data-cms-content-slot-position");
						
						//console.log("***** move = " + newIndex  + "," + fromContentSlotId + "," + toContentSlotId + "," + toPosition + "," + componentId);
						
                        parent.postMessage({eventName:'notifyIframeZkComponentMove', data: [ACC.previewCurrentPageUid, fromContentSlotId, toContentSlotId, toPosition, componentId, newIndex]},'*');

                     }
				});				
				$(this.SELECTOR_SORTABLE).disableSelection();
			}
		}
		
};

$(document).ready(function () {
	LiveEditSortable.init();
});