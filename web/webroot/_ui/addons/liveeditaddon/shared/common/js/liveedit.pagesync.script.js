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
// LIVE EDIT PAGE SYNC PLUGIN

(function ($) {

    $.extend($.fn, {
        liveEditPageSync: function (options) {
            // check if element exist
            if (!this.length) {
                if (options && options.debug && window.console) {
                    console.warn("No components found. Maybe you provided wrong component id/class");
                }
                return;
            }

            var liveEditPageSync = $.data(this, "liveEditPageSync");
            if (liveEditPageSync) {
                return this;
            }

            liveEditPageSync = new $.liveEditPageSync(options, this);
            $.data(this, "liveEditPageSync", liveEditPageSync);

            return this;
        }
    });

    // Constructor for liveEditPageSync
    $.liveEditPageSync = function (options, component) {
        this.settings = $.extend({}, $.liveEditPageSync.defaults, options);

        var isStaged = component.attr(this.settings.isCatalogStaged)
        if (isStaged=="true") {
            component.before("<div id='" + this.settings.pageSyncButton + "' class='button-SYNC_OK' style='cursor:pointer'>&nbsp;</div>");

            this.component = $("#" + this.settings.pageSyncButton);
            this.init();
        }
    };

    $.extend($.liveEditPageSync, {
        defaults: {
            urlTemplate: "/liveeditaddon/page/{pageId}/syncStatus",
            isCatalogStaged: "data-cms-content-slot-is-staged-catalog",
            notInSyncCssClass: "button-SYNC_NOT_OK",
            inSyncCssClass: "button-SYNC_OK",
            pagePreviewId: "cmsTicketId",
            pageSyncButton: "pageSyncButton"
        },

        setDefaults: function (settings) {
            $.extend($.liveEditPageSync.defaults, settings);
        },

        prototype: {
            init: function () {
                this.bindComponent();
                this.startPollingServer();
            },

            toggleSyncClass: function (isSynced) {
                if (isSynced) {
                    this.component.removeClass(this.settings.notInSyncCssClass);
                    this.component.addClass(this.settings.inSyncCssClass);
                } else {
                    this.component.removeClass(this.settings.inSyncCssClass);
                    this.component.addClass(this.settings.notInSyncCssClass);
                }
            },

            performSyncCallback: function () {
                var pageId = ACC.previewCurrentPageUid;
                var cmsTicketId = $.getUrlVar(this.settings.pagePreviewId);
                var callbackClosure = $.hitch(this, this.toggleSyncClass);

                $.ajax({
                    url: this.settings.urlTemplate.replace("{pageId}", pageId),
                    type: "POST",
                    data: {previewTicket: cmsTicketId},
                    success: function (data) {
                        callbackClosure(data.synchronisationStatus === 'SYNCHRONIZATION_OK');
                    },
                    dataType: "json",
                    timeout: 2000
                });
            },

            bindComponent: function () {
                var callbackClosure = $.hitch(this, function (event) {
                    event.preventDefault();
                    if (this.component.hasClass(this.settings.notInSyncCssClass)) {
                        this.performSyncCallback();
                    }
                    event.stopPropagation();
                });

                this.component.bind('click', function (event) {
                    callbackClosure(event);
                });
            },

            startPollingServer: function () {

                var pageId = ACC.previewCurrentPageUid;
                var cmsTicketId = $.getUrlVar(this.settings.pagePreviewId);
                var callbackClosure = $.hitch(this, this.toggleSyncClass);
                var serverURL = this.settings.urlTemplate.replace("{pageId}", pageId);

                (function poll() {

                    $.ajax({
                        url: serverURL,
                        type: "GET",
                        data: {previewTicket: cmsTicketId},
                        success: callbackClosure,
                        dataType: "json",
                        complete: setTimeout(function () {
                            poll()
                        }, 5000),
                        timeout: 2000
                    })
                })();

            }

        }
    });

    $.extend({
        hitch: function (context, func) {
            var args1 = Array.prototype.slice.call(arguments, 2);
            return function () {
                var args2 = Array.prototype.slice.call(arguments);
                return func.apply(context, Array.prototype.concat.call(args1, args2));
            };
        },
        getUrlVars: function () {
            var vars = [], hash;
            var hashes = window.location.href.slice(window.location.href.indexOf('?') + 1).split('&');
            for (var i = 0; i < hashes.length; i++) {
                hash = hashes[i].split('=');
                vars.push(hash[0]);
                vars[hash[0]] = hash[1];
            }
            return vars;
        },
        getUrlVar: function (name) {
            return $.getUrlVars()[name];
        }
    });

})(jQuery);