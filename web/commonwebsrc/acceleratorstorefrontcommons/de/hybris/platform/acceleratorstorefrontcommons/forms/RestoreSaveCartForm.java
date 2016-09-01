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
package de.hybris.platform.acceleratorstorefrontcommons.forms;


public class RestoreSaveCartForm
{
    private boolean preventSaveActiveCart;
    private boolean keepRestoredCart;
    private String cartName;

    public boolean isKeepRestoredCart() {
        return keepRestoredCart;
    }

    public void setKeepRestoredCart(boolean keepRestoredCart) {
        this.keepRestoredCart = keepRestoredCart;
    }

    public boolean isPreventSaveActiveCart() {
        return preventSaveActiveCart;
    }

    public void setPreventSaveActiveCart(boolean preventSaveActiveCart) {
        this.preventSaveActiveCart = preventSaveActiveCart;
    }

    public String getCartName() {
        return cartName;
    }

    public void setCartName(String cartName) {
        this.cartName = cartName;
    }


}
