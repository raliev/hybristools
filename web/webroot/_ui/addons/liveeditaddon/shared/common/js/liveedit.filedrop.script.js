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
$(function(){
	
	var dropbox = $('.filedrop'),
		message = $('.message', dropbox);
	
	var selectedDropBox; 
	
	dropbox.filedrop({
		// The name of the $_FILES entry:
		paramname:'pic',
		
		maxfiles: 1,
    	maxfilesize: 2,
		url: ACC.liveeditMediaUploadUrl, // Global JavaScript var defined in LiveEditResourceBeforeViewHandler
		uploadFinished:function(i,file,response){
			startCreateWizard(file, response)	
		},
		dragOver:function(e)
		{
			selectedDropBox = $(e.currentTarget);
			selectedDropBox.addClass("fileDropHoverOver");
		},
		dragLeave:function(e)
		{
			selectedDropBox.removeClass("fileDropHoverOver");
		},
		error: function(err, file) {
			switch(err) {
				case 'BrowserNotSupported':
					showMessage('Your browser does not support HTML5 file uploads!');
					break;
				case 'TooManyFiles':
					alert('Too many files! Please select 5 at most! (configurable)');
					break;
				case 'FileTooLarge':
					alert(file.name+' is too large! Please upload files up to 2mb (configurable).');
					break;
				default:
					break;
			}
		},
		
		// Called before each upload is started
		beforeEach: function(file){
			if(!file.type.match(/^image\//)){
				alert('Only images are allowed!');
				
				// Returning false will cause the
				// file to be rejected
				return false;
			}
		},
		
		uploadStarted:function(i, file, len){
			// BD disable the preview functionality, needs some work to be able to use multiple drop boxes on the
			// same page
			// createImage(file);
		},
		
		progressUpdated: function(i, file, progress) {
			// BD disable the preview functionality, needs some work to be able to use multiple drop boxes on the
			// same page
			// $.data(file).find('.progress').width(progress);
		}
    	 
	});
	
	var template = '<div class="preview">'+
						'<span class="imageHolder">'+
							'<img />'+
							'<span class="uploaded"></span>'+
						'</span>'+
						'<div class="progressHolder">'+
							'<div class="progress"></div>'+
						'</div>'+
					'</div>'; 
	
	
	function createImage(file){

		var preview = $(template), 
			image = $('img', preview);
			
		var reader = new FileReader();
		
		image.width = 50;
		image.height = 50;
		
		reader.onload = function(e){
			
			// e.target.result holds the DataURL which
			// can be used as a source of the image:
			
			image.attr('src',e.target.result);
		};
		
		// Reading the file as a DataURL. When finished,
		// this will trigger the onload function above:
		reader.readAsDataURL(file);
		
		message.hide();
		preview.appendTo(dropBox);
		
		// Associating a preview container
		// with the file, using jQuery's $.data():
		
		$.data(file,preview);
	}

	function showMessage(msg){
		message.html(msg);
	}
	
	/**
	 * Send a ZK event to start the CMS Component Wizard 
	 */
	function startCreateWizard(file, response){
		
		if (response.success)
		{
			var contentSlotId = selectedDropBox.parents(".yCmsContentSlot").attr("data-cms-content-slot");
			var position = selectedDropBox.parents(".yCmsContentSlot").attr("data-cms-content-slot-position");
            parent.postMessage({eventName:'notifyIframeZkComponentCreateMedia', data: [ACC.previewCurrentPageUid, position, contentSlotId, file.name]},'*');
		}
		else
		{
			alert("Could not upload file," + response.errorMessage);
		}
		
	}
	
});