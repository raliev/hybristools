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
// Create IE + others compatible event handler
var eventMethod = window.addEventListener ? "addEventListener" : "attachEvent";
var eventer = window[eventMethod];
var messageEvent = eventMethod == "attachEvent" ? "onmessage" : "message";

// Listen to message from child window
eventer(messageEvent,function(e) {
    var event = e.data;
    console.log('parent received message!:  ', event.eventName, event.data);
    if (window[event.eventName]){
        window[event.eventName].apply(window, event.data);
    }
},false);

function notifyIframeZkComponent(cmp_id, slot_id){
    var iframeCollection = document.getElementsByTagName('iframe');
    var id="";
    for(index in iframeCollection){
        if(iframeCollection[index].className!=null&&iframeCollection[index].className=='liveEditBrowser'&&iframeCollection[index].offsetHeight!=0){
            id=iframeCollection[index].id;
            break;
        }
    }
    var payload = JSON.stringify({
        cmp_id:cmp_id,
        slot_id:slot_id
    });

    comm.sendUser(id,'defaultCallback',payload);
}

function notifyIframeAboutUrlChange(url, pagePk, userPk, jaloSessionId){
    var iframeCollection = document.getElementsByTagName('iframe');
    var id="";
    for(index in iframeCollection){
        if(iframeCollection[index].className!=null&&iframeCollection[index].className=='liveEditBrowser'&&iframeCollection[index].offsetHeight!=0){
            id=iframeCollection[index].id;
            break;
        }
    }
    comm.sendUser(id,'urlChange',url,pagePk,userPk,jaloSessionId)
}
function notifyIframeZkComponentCreateMedia(page_id, position, slot_id, mediaFileName){
    var iframeCollection = document.getElementsByTagName('iframe');
    var id="";
    for(index in iframeCollection){
        if(iframeCollection[index].className!=null&&iframeCollection[index].className=='liveEditBrowser'&&iframeCollection[index].offsetHeight!=0){
            id=iframeCollection[index].id;
            break;
        }
    }
    var payload = JSON.stringify({
        page_id:page_id,
        position:position,
        slot_id:slot_id,
        mediaFileName:mediaFileName
    });
    comm.sendUser(id,'createMediaComponent', payload);
}

function notifyIframeZkComponentMove(page_id, current_slot_id, new_slot_id, new_slot_position, cmp_id, cmp_index){
    var iframeCollection = document.getElementsByTagName('iframe');
    var id="";
    for(index in iframeCollection){
        if(iframeCollection[index].className!=null&&iframeCollection[index].className=='liveEditBrowser'&&iframeCollection[index].offsetHeight!=0){
            id=iframeCollection[index].id;
            break;
        }
    }
    var payload = JSON.stringify({
        page_id:page_id,
        slot_id:current_slot_id,
        new_slot_id:new_slot_id,
        new_slot_position:new_slot_position,
        cmp_id:cmp_id,
        cmp_index:cmp_index
    });
    comm.sendUser(id,'moveComponent', payload);
}

function notifyIframeZkComponentRemove(page_id, position, cmp_id, slot_id){
    var iframeCollection = document.getElementsByTagName('iframe');
    var id="";
    for(index in iframeCollection){
        if(iframeCollection[index].className!=null&&iframeCollection[index].className=='liveEditBrowser'&&iframeCollection[index].offsetHeight!=0){
            id=iframeCollection[index].id;
            break;
        }
    }
    var payload = JSON.stringify({
        page_id:page_id,
        position:position,
        cmp_id:cmp_id,
        slot_id:slot_id
    });
    comm.sendUser(id,'removeComponent', payload);
}

function notifyIframeZkRemoveContainer(page_id, position, cmp_id, slot_id){
    var iframeCollection = document.getElementsByTagName('iframe');
    var id="";
    for(index in iframeCollection){
        if(iframeCollection[index].className!=null&&iframeCollection[index].className=='liveEditBrowser'&&iframeCollection[index].offsetHeight!=0){
            id=iframeCollection[index].id;
            break;
        }
    }
    var payload = JSON.stringify({
        page_id:page_id,
        position:position,
        cmp_id:cmp_id,
        slot_id:slot_id
    });
    comm.sendUser(id,'removeContainer', payload);
}

function notifyIframeZkItemSync(item_uid){
    var iframeCollection = document.getElementsByTagName('iframe');
    var id="";
    for(index in iframeCollection){
        if(iframeCollection[index].className!=null&&iframeCollection[index].className=='liveEditBrowser'&&iframeCollection[index].offsetHeight!=0){
            id=iframeCollection[index].id;
            break;
        }
    }
    var payload = JSON.stringify({
        item_uid:item_uid
    });
    comm.sendUser(id,'syncItem',payload);
}

function notifyIframeZkComponentClone(page_id, position, cmp_id, slot_id){
    var iframeCollection = document.getElementsByTagName('iframe');
    var id="";
    for(index in iframeCollection){
        if(iframeCollection[index].className!=null&&iframeCollection[index].className=='liveEditBrowser'&&iframeCollection[index].offsetHeight!=0){
            id=iframeCollection[index].id;
            break;
        }
    }
    var payload = JSON.stringify({
        page_id:page_id,
        position:position,
        cmp_id:cmp_id,
        slot_id:slot_id
    });
    comm.sendUser(id,'cloneComponent', payload);
}

function notifyIframeZkComponentCreate(page_id, position, slot_id){
    var iframeCollection = document.getElementsByTagName('iframe');
    var id="";
    for(index in iframeCollection){
        if(iframeCollection[index].className!=null&&iframeCollection[index].className=='liveEditBrowser'&&iframeCollection[index].offsetHeight!=0){
            id=iframeCollection[index].id;
            break;
        }
    }
    var payload = JSON.stringify({
        page_id:page_id,
        position:position,
        slot_id:slot_id
    });
    comm.sendUser(id,'createComponent', payload);
}

function notifyIframeZkComponentShowAll(page_id, position, slot_id){
    var iframeCollection = document.getElementsByTagName('iframe');
    var id="";
    for(index in iframeCollection){
        if(iframeCollection[index].className!=null&&iframeCollection[index].className=='liveEditBrowser'&&iframeCollection[index].offsetHeight!=0){
            id=iframeCollection[index].id;
            break;
        }
    }
    var payload = JSON.stringify({
        page_id:page_id,
        slot_id:slot_id
    });
    comm.sendUser(id,'showAllComponents', payload);
}

function notifyIframeZkComponentInspector(page_id, slot_id, cmp_id){
    var iframeCollection = document.getElementsByTagName('iframe');
    var id="";
    for(index in iframeCollection){
        if(iframeCollection[index].className!=null&&iframeCollection[index].className=='liveEditBrowser'&&iframeCollection[index].offsetHeight!=0){
            id=iframeCollection[index].id;
            break;
        }
    }
    var payload = JSON.stringify({
        page_id:page_id,
        slot_id:slot_id,
        cmp_id:cmp_id
    });
    comm.sendUser(id,'inspectComponent',payload);
}

function notifyIframeZkComponentVisibilityToggle(page_id, slot_id, cmp_id){
    var iframeCollection = document.getElementsByTagName('iframe');
    var id="";
    for(index in iframeCollection){
        if(iframeCollection[index].className!=null&&iframeCollection[index].className=='liveEditBrowser'&&iframeCollection[index].offsetHeight!=0){
            id=iframeCollection[index].id;
            break;
        }
    }
    var payload = JSON.stringify({
        page_id:page_id,
        slot_id:slot_id,
        cmp_id:cmp_id
    });
    comm.sendUser(id,'toggleVisibility', payload);
}

function notifyIframeZkSlotCreate(page_id, position, action){
    var iframeCollection = document.getElementsByTagName('iframe');
    var id="";
    if(action == undefined || action == '') {
        action = 'CREATE';
    }

    for(index in iframeCollection){
        if(iframeCollection[index].className!=null&&iframeCollection[index].className=='liveEditBrowser'&&iframeCollection[index].offsetHeight!=0){
            id=iframeCollection[index].id;
            break;
        }
    }
    var payload = JSON.stringify({
        page_id:page_id,
        position:position,
        action:action
    });
    comm.sendUser(id,'createSlot', payload);
}

function notifyIframeZkSlotRemove(page_id, position, slot_id){
    var iframeCollection = document.getElementsByTagName('iframe');
    var id="";
    for(index in iframeCollection){
        if(iframeCollection[index].className!=null&&iframeCollection[index].className=='liveEditBrowser'&&iframeCollection[index].offsetHeight!=0){
            id=iframeCollection[index].id;
            break;
        }
    }
    var payload = JSON.stringify({
        page_id:page_id,
        position:position,
        slot_id:slot_id
    });
    comm.sendUser(id,'removeSlot', payload);
}

function notifyIframeZkSlotLock(page_id, position, slot_id){
    var iframeCollection = document.getElementsByTagName('iframe');
    var id="";
    for(index in iframeCollection){
        if(iframeCollection[index].className!=null&&iframeCollection[index].className=='liveEditBrowser'&&iframeCollection[index].offsetHeight!=0){
            id=iframeCollection[index].id;
            break;
        }
    }
    var payload = JSON.stringify({
        page_id:page_id,
        position:position,
        slot_id:slot_id
    });
    comm.sendUser(id,'lockSlot', payload);
}

function notifyIframeZkComponent(page_id, position, cmp_id, slot_id){
    var iframeCollection = document.getElementsByTagName('iframe');
    var id="";
    for(index in iframeCollection){
        if(iframeCollection[index].className!=null&&iframeCollection[index].className=='liveEditBrowser'&&iframeCollection[index].offsetHeight!=0){
            id=iframeCollection[index].id;
            break;
        }
    }
    var payload = JSON.stringify({
        page_id:page_id,
        position:position,
        cmp_id:cmp_id,
        slot_id:slot_id
    });
    comm.sendUser(id,'callback', payload);
}

function manageProductImage(item_uid,position,serverPath){
    var iframeCollection = document.getElementsByTagName('iframe');
    var id="";
    for(index in iframeCollection){
        if(iframeCollection[index].className!=null&&iframeCollection[index].className=='liveEditBrowser'&&iframeCollection[index].offsetHeight!=0){
            id=iframeCollection[index].id;
            break;
        }
    }
    var payload = JSON.stringify({
        serverPath:serverPath
    });
    comm.sendUser(id,'manageProductImage', payload);
}

function manageProductFeatureImage(item_uid,position,serverPath){
    var iframeCollection = document.getElementsByTagName('iframe');
    var id="";
    for(index in iframeCollection){
        if(iframeCollection[index].className!=null&&iframeCollection[index].className=='liveEditBrowser'&&iframeCollection[index].offsetHeight!=0){
            id=iframeCollection[index].id;
            break;
        }
    }
    var payload = JSON.stringify({
        item_uid:item_uid,
        position:position,
        serverPath:serverPath
    });
    comm.sendUser(id,'manageProductFeatureImage', payload);
}

function notifyIframeEditProductReferences(cmp_id){
    var iframeCollection = document.getElementsByTagName('iframe');
    var id="";
    for(index in iframeCollection){
        if(iframeCollection[index].className!=null&&iframeCollection[index].className=='liveEditBrowser'&&iframeCollection[index].offsetHeight!=0){
            id=iframeCollection[index].id;
            break;
        }
    }
    var payload = JSON.stringify({
        cmp_id:cmp_id
    });
    comm.sendUser(id,'editProductReferences', payload);
}

function notifyIframeEditRestrictions(page_id, slot_id, component_pk){
    var iframeCollection = document.getElementsByTagName('iframe');
    var id="";
    for(index in iframeCollection){
        if(iframeCollection[index].className!=null&&iframeCollection[index].className=='liveEditBrowser'&&iframeCollection[index].offsetHeight!=0){
            id=iframeCollection[index].id;
            break;
        }
    }
    var payload = JSON.stringify({
        page_id:page_id,
        slot_id:slot_id,
        component_pk:component_pk
    });
    comm.sendUser(id,'editRestrictions', payload);
}

function notifyIframeEditCMSItem(page_id, slot_id, component_pk){
    var iframeCollection = document.getElementsByTagName('iframe');
    var id="";
    for(index in iframeCollection){
        if(iframeCollection[index].className!=null&&iframeCollection[index].className=='liveEditBrowser'&&iframeCollection[index].offsetHeight!=0){
            id=iframeCollection[index].id;
            break;
        }
    }
    var payload = JSON.stringify({
        page_id:page_id,
        slot_id:slot_id,
        component_pk:component_pk
    });
    comm.sendUser(id,'editCMSItem', payload);
}

function notifyIframeEditItem(page_id, position, item_pk){
    var iframeCollection = document.getElementsByTagName('iframe');
    var id="";
    for(index in iframeCollection){
        if(iframeCollection[index].className!=null&&iframeCollection[index].className=='liveEditBrowser'&&iframeCollection[index].offsetHeight!=0){
            id=iframeCollection[index].id;
            break;
        }
    }
    var payload = JSON.stringify({
        item_pk:item_pk
    });
    comm.sendUser(id,'editItem', payload);
}

function manageMedia(item_uid,position,serverPath){
    var iframeCollection = document.getElementsByTagName('iframe');
    var id="";
    for(index in iframeCollection){
        if(iframeCollection[index].className!=null&&iframeCollection[index].className=='liveEditBrowser'&&iframeCollection[index].offsetHeight!=0){
            id=iframeCollection[index].id;
            break;
        }
    }
    var payload = JSON.stringify({
        item_uid:item_uid,
        position:position,
        serverPath:serverPath
    });
    comm.sendUser(id, 'manageMedia', payload);
}

function notifyIframeZkSlotEditMenu(page_id, position, slot_id,addon,serverPath) {
    var iframeCollection = document.getElementsByTagName('iframe');
    var id="";
    for(index in iframeCollection) {
        if(iframeCollection[index].className!=null&&iframeCollection[index].className=='liveEditBrowser'&&iframeCollection[index].offsetHeight!=0) {
            id=iframeCollection[index].id;
            break;
        }
    }
    var payload = JSON.stringify({
        page_id:page_id,
        position:position,
        slot_id:slot_id,
        addon:addon,
        serverPath:serverPath
    });
    comm.sendUser(id,'editMenu', payload);
}

function notifyIframeZkOpenNavigationEditor(page_id, position, cmp_id, slot_id,addon,serverPath){
    var iframeCollection = document.getElementsByTagName('iframe');
    var id="";
    for(index in iframeCollection){
        if(iframeCollection[index].className!=null&&iframeCollection[index].className=='liveEditBrowser'&&iframeCollection[index].offsetHeight!=0){
            id=iframeCollection[index].id;
            break;
        }
    }
    var payload = JSON.stringify({
        page_id:page_id,
        position:position,
        cmp_id:cmp_id,
        slot_id:slot_id,
        addon:addon,
        serverPath:serverPath
    });
    comm.sendUser(id,'openNavigationEditor', payload);
}


