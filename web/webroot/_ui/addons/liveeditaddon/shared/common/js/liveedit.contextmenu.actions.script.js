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
// LIVE EDIT CONTEXT MENU ACTIONS

(function($) {

    // NAMESPACE LiveEditAction
    LiveEditAction = function() {};

    LiveEditAction._edit_item = function(componentData, menuItemData) {
        var pageUid = componentData.data.pageUid;
        var position = componentData.data.position;
        var pk = menuItemData.editItemPk;
        if (undefined != pk && pk != "") {
            parent.postMessage({eventName:'notifyIframeEditItem', data: [pageUid, position, pk]},'*')
        }
    }

    LiveEditAction.EDIT = function(componentData, menuItemData) {
        var cmsComponentUid = componentData.data.componentUid;
        var cmsContentSlotUid = componentData.data.slotUid;
        var pageUid = componentData.data.pageUid;
        var position = componentData.data.position;

        if (undefined != cmsComponentUid && cmsComponentUid != "") {
            parent.postMessage({eventName:'notifyIframeZkComponent', data: [pageUid, position, cmsComponentUid, cmsContentSlotUid]},'*')
        }

    }

    LiveEditAction.EDIT_POPUP = function(componentData, menuItemData) {

        var pageUid = componentData.data.pageUid;
        var slotId = componentData.data.slotUid;
        var pk = menuItemData.pk;

        if (undefined != pk && pk != "") {
            parent.postMessage({eventName:'notifyIframeEditCMSItem', data: [pageUid, slotId, pk]},'*');
        }
    }

    LiveEditAction.INSPECTOR = function(componentData, menuItemData) {

        var pageUid = componentData.data.pageUid;
        var slotUid = componentData.data.slotUid;
        var cmsComponentUid = componentData.data.componentUid;

        if (undefined != cmsComponentUid && cmsComponentUid != "") {
            parent.postMessage({eventName:'notifyIframeZkComponentInspector', data: [pageUid, slotUid, cmsComponentUid]},'*')
        }
    }

    LiveEditAction.REMOVE = function(componentData, menuItemData) {

        var cmsComponentUid = componentData.data.componentUid;
        var cmsContentSlotUid = componentData.data.slotUid;
        var pageUid = componentData.data.pageUid;
        var position = componentData.data.position;

        if (undefined != cmsComponentUid && cmsComponentUid != "") {
            parent.postMessage({eventName:'notifyIframeZkComponentRemove', data: [pageUid, position, cmsComponentUid, cmsContentSlotUid]},'*')
        }
    }

    LiveEditAction.REMOVE_CONTAINER = function(componentData, menuItemData) {

        var cmsComponentUid = componentData.data.componentUid;
        var cmsContentSlotUid = componentData.data.slotUid;
        var pageUid = componentData.data.pageUid;
        var position = componentData.data.position;

        if (undefined != cmsComponentUid && cmsComponentUid != "") {
            parent.postMessage({eventName:'notifyIframeZkRemoveContainer', data: [pageUid, position, cmsComponentUid, cmsContentSlotUid]},'*')
        }
    }

    LiveEditAction.CLONE = function(componentData, menuItemData) {

        var cmsComponentUid = componentData.data.componentUid;
        var cmsContentSlotUid = componentData.data.slotUid;
        var pageUid = componentData.data.pageUid;
        var position = componentData.data.position;

        if (undefined != cmsComponentUid && cmsComponentUid != "") {
            parent.postMessage({eventName:'notifyIframeZkComponentClone', data: [pageUid, position, cmsComponentUid, cmsContentSlotUid]},'*')
        }
    }

    LiveEditAction.ENABLE = function(componentData, menuItemData) {
        var cmsComponentUid = componentData.data.componentUid;
        var pageUid = componentData.data.pageUid;
        var slotId = componentData.data.slotUid;


        if (undefined != cmsComponentUid && cmsComponentUid != "") {
            parent.postMessage({eventName:'notifyIframeZkComponentVisibilityToggle', data: [pageUid, slotId, cmsComponentUid]},'*')
        }
    }

    LiveEditAction.HIDE = function(componentData, menuItemData) {
        var cmsComponentUid = componentData.data.componentUid;
        var pageUid = componentData.data.pageUid;
        var slotId = componentData.data.slotUid;

        if (undefined != cmsComponentUid && cmsComponentUid != "") {
            parent.postMessage({eventName:'notifyIframeZkComponentVisibilityToggle', data: [pageUid, slotId, cmsComponentUid]},'*')
        }
    }

    LiveEditAction.SYNC = function(componentData, menuItemData) {
        var cmsComponentUid = componentData.data.componentUid;
        if (undefined != cmsComponentUid && cmsComponentUid != "") {
            parent.postMessage({eventName:'notifyIframeZkItemSync', data: [cmsComponentUid]},'*')
        }
    }

    LiveEditAction.MANAGE_PRODUCT_IMAGE = function(componentData, menuItemData) {
        var cmsComponentUid = componentData.data.componentUid;
        var position = componentData.data.position;
        if (undefined != cmsComponentUid && cmsComponentUid != "") {
            parent.postMessage({eventName:'manageProductImage', data: [cmsComponentUid,position,ACC.serverPath]},'*')
        }
    }

    LiveEditAction.MANAGE_PRODUCT_FEATURE_IMAGE = function(componentData, menuItemData) {
        var cmsComponentUid = componentData.data.componentUid;
        var position = componentData.data.position;
        if (undefined != cmsComponentUid && cmsComponentUid != "") {
            parent.postMessage({eventName:'manageProductFeatureImage', data: [cmsComponentUid,position,ACC.serverPath]},'*')
        }
    }

    LiveEditAction.RESTRICTIONS = function(componentData, menuItemData) {
        var pageUid = componentData.data.pageUid;
        var slotId = componentData.data.slotUid;
        var pk = menuItemData.pk;
        if (undefined != pk && pk != "") {
            parent.postMessage({eventName:'notifyIframeEditRestrictions', data: [pageUid, slotId, pk]},'*')
        }
    }

    LiveEditAction.EDIT_PRODUCT_REFERENCES = function(componentData, menuItemData) {
        var cmsComponentUid = componentData.data.componentUid;
        if (undefined != cmsComponentUid && cmsComponentUid != "") {
            parent.postMessage({eventName:'notifyIframeEditProductReferences', data: [cmsComponentUid]},'*')
        }
    }

    LiveEditAction.MANAGE_MEDIA = function(componentData, menuItemData) {
        var cmsComponentUid = componentData.data.componentUid;
        var position = componentData.data.position;
        if (undefined != cmsComponentUid && cmsComponentUid != "") {
            parent.postMessage({eventName:'manageMedia', data: [cmsComponentUid,position,ACC.serverPath]},'*')
        }
    }

    LiveEditSlotAction.EDIT_NAVIGATION = function(slotData) {
        var pageId = slotData.pageUid;
        var position = slotData.position;
        var slotId = slotData.slotUid;

        if (pageId != "") {
            parent.postMessage({eventName:'notifyIframeZkSlotEditMenu', data: [pageId, position, slotId,'liveeditaddon',ACC.serverPath]},'*')
        }
    };

    LiveEditAction.EDIT_NAVIGATION = function(componentData, menuItemData) {
        var cmsComponentUid = componentData.data.componentUid;
        var cmsContentSlotUid = componentData.data.slotUid;
        var pageUid = componentData.data.pageUid;
        var position = componentData.data.position;

        if (undefined != cmsComponentUid && cmsComponentUid != "") {
            parent.postMessage({eventName:'notifyIframeZkOpenNavigationEditor', data: [pageUid, position, cmsComponentUid, cmsContentSlotUid,'liveeditaddon',ACC.serverPath]},'*')
        }
    };
})(jQuery);

