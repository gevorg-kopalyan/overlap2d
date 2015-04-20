/*
 * ******************************************************************************
 *  * Copyright 2015 See AUTHORS file.
 *  *
 *  * Licensed under the Apache License, Version 2.0 (the "License");
 *  * you may not use this file except in compliance with the License.
 *  * You may obtain a copy of the License at
 *  *
 *  *   http://www.apache.org/licenses/LICENSE-2.0
 *  *
 *  * Unless required by applicable law or agreed to in writing, software
 *  * distributed under the License is distributed on an "AS IS" BASIS,
 *  * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  * See the License for the specific language governing permissions and
 *  * limitations under the License.
 *  *****************************************************************************
 */

package com.uwsoft.editor.mvc.view.ui.properties;

import com.puremvc.patterns.mediator.SimpleMediator;
import com.puremvc.patterns.observer.Notification;
import com.uwsoft.editor.Overlap2D;
import com.uwsoft.editor.gdx.sandbox.Sandbox;
import com.uwsoft.editor.mvc.Overlap2DFacade;

/**
 * Created by azakhary on 4/15/2015.
 */
public abstract class UIAbstractPropertiesMediator<T, V extends UIAbstractProperties> extends SimpleMediator<V> {
    private Sandbox sandbox;

    protected T observableReference;

    private boolean observableToDataViewTransactionInProgress = false;

    public UIAbstractPropertiesMediator(String mediatorName, V viewComponent) {
        super(mediatorName, viewComponent);

        sandbox = Sandbox.getInstance();
    }

    @Override
    public void onRegister() {
        facade = Overlap2DFacade.getInstance();
    }


    @Override
    public String[] listNotificationInterests() {
        return new String[]{
                Overlap2D.ITEM_DATA_UPDATED,
                UIAbstractProperties.PROPERTIES_UPDATED
        };
    }

    @Override
    public void handleNotification(Notification notification) {
        super.handleNotification(notification);

        switch (notification.getName()) {
            case UIAbstractProperties.PROPERTIES_UPDATED:
                if(!observableToDataViewTransactionInProgress) translateViewToItemData();
                break;
            case Overlap2D.ITEM_DATA_UPDATED:
                onItemDataUpdate();
                break;
            default:
                break;
        }
    }

    public void setItem(T item) {
        observableReference = item;
        observableToDataViewTransactionInProgress = true;
        translateObservableDataToView(observableReference);
        observableToDataViewTransactionInProgress = false;
    }

    public void onItemDataUpdate() {
        observableToDataViewTransactionInProgress = true;
        translateObservableDataToView(observableReference);
        observableToDataViewTransactionInProgress = false;
    }

    protected abstract void translateObservableDataToView(T item);

    protected abstract void translateViewToItemData();
}