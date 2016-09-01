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

function controlWindow(A){this._form=A;this.windowType="controlWindow";this.noSuggestionSelection=FCKLang.DlgSpellNoSuggestions;this.suggestionList=this._form.sugg;this.evaluatedText=this._form.misword;this.replacementText=this._form.txtsugg;this.undoButton=this._form.btnUndo;this.addSuggestion=addSuggestion;this.clearSuggestions=clearSuggestions;this.selectDefaultSuggestion=selectDefaultSuggestion;this.resetForm=resetForm;this.setSuggestedText=setSuggestedText;this.enableUndo=enableUndo;this.disableUndo=disableUndo}function resetForm(){if(this._form){this._form.reset()}}function setSuggestedText(){var B=this.suggestionList;var A=this.replacementText;var C="";if((B.options[0].text)&&B.options[0].text!=this.noSuggestionSelection){C=B.options[B.selectedIndex].text}A.value=C}function selectDefaultSuggestion(){var B=this.suggestionList;var A=this.replacementText;if(B.options.length==0){this.addSuggestion(this.noSuggestionSelection)}else{B.options[0].selected=true}this.setSuggestedText()}function addSuggestion(D){var C=this.suggestionList;if(D){var A=C.options.length;var B=new Option(D,"sugg_text"+A);C.options[A]=B}}function clearSuggestions(){var B=this.suggestionList;for(var A=B.length-1;A>-1;A--){if(B.options[A]){B.options[A]=null}}}function enableUndo(){if(this.undoButton){if(this.undoButton.disabled==true){this.undoButton.disabled=false}}}function disableUndo(){if(this.undoButton){if(this.undoButton.disabled==false){this.undoButton.disabled=true}}};
