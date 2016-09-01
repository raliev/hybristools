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
	
	// NAMESPACE LiveEditAction
	LiveEditSlotAction = function() {};
	
	LiveEditSlotAction.ADD = function(slotData) {

		var pageId = slotData.pageUid;
		var contentSlotId = slotData.slotUid;
		var position = slotData.position;
		
		if (pageId != "") {
            parent.postMessage({eventName:'notifyIframeZkComponentCreate', data: [pageId, position, contentSlotId]},'*');
		}
	};
	
	LiveEditSlotAction.SHOW_ALL = function(slotData) {

		var pageId = slotData.pageUid;
		var contentSlotId = slotData.slotUid;
		var position = slotData.position;
		
		if (pageId != "") {
            parent.postMessage({eventName:'notifyIframeZkComponentShowAll', data: [pageId, position, contentSlotId]},'*');
		}
	};
	
	LiveEditSlotAction.OVERRIDE = function(slotData) {
		
		var pageId = slotData.pageUid;
		var position = slotData.position;
		
		if (pageId != "") {
            parent.postMessage({eventName:'notifyIframeZkSlotCreate', data: [pageId, position, "OVERRIDE"]},'*');
		}
	};
	
	LiveEditSlotAction.OVERRIDE_REVERSE = function(slotData) {
		
		var pageId = slotData.pageUid;
		var position = slotData.position;
		var slotId = slotData.slotUid;
		
		if (pageId != "") {
            parent.postMessage({eventName:'notifyIframeZkSlotRemove', data: [pageId, position, slotId]},'*');
		}
	};
	
	LiveEditSlotAction.LOCK = function(slotData) {
		
		var pageId = slotData.pageUid;
		var position = slotData.position;
		var slotId = slotData.slotUid;
		
		if (pageId != "") {
            parent.postMessage({eventName:'notifyIframeZkSlotLock', data: [pageId, position, slotId]},'*');
		}
	};

    LiveEditSlotAction.UNLOCK = function(slotData) {

        var pageId = slotData.pageUid;
        var position = slotData.position;
        var slotId = slotData.slotUid;

        if (pageId != "") {
            parent.postMessage({eventName:'notifyIframeZkSlotLock', data: [pageId, position, slotId]},'*');
        }
    };

	LiveEditSlotAction.SYNC = function(slotData) {
		
		var pageId = slotData.pageUid;
		var position = slotData.position;
		var slotId = slotData.slotUid;
		
		if (pageId != "") {
            parent.postMessage({eventName:'notifyIframeZkItemSync', data: [slotId]},'*');
		}
	};
	
})(jQuery);