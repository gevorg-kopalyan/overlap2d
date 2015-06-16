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

package com.uwsoft.editor.mvc.controller.sandbox;

import com.badlogic.ashley.core.Entity;
import com.uwsoft.editor.gdx.mediators.SceneControlMediator;
import com.uwsoft.editor.renderer.components.MainItemComponent;
import com.uwsoft.editor.renderer.legacy.data.CompositeItemVO;
import com.uwsoft.editor.utils.runtime.ComponentRetriever;
import com.uwsoft.editor.utils.runtime.EntityUtils;

import java.util.HashMap;

/**
 * Created by azakhary on 6/15/2015.
 */
public abstract class EntityModifyRevertableCommand extends RevertableCommand {

    @Override
    public void callDoAction() {
        super.callDoAction();
        postChange();
    }
    @Override
    public void callUndoAction() {
        super.callUndoAction();
        postChange();
    }

    protected void postChange() {
        Integer parentId = EntityUtils.getEntityId(sandbox.getCurrentViewingEntity());

        Entity entity = EntityUtils.getByUniqueId(parentId);

        // Update item library data if it was in library
        MainItemComponent mainItemComponent = ComponentRetriever.get(entity, MainItemComponent.class);
        SceneControlMediator sceneControl = sandbox.getSceneControl();
        HashMap<String, CompositeItemVO> libraryItems = sceneControl.getCurrentSceneVO().libraryItems;
        if(libraryItems.containsKey(mainItemComponent.itemName)) {
            CompositeItemVO itemVO = new CompositeItemVO();
            itemVO.loadFromEntity(entity);
            libraryItems.put(mainItemComponent.itemName, itemVO);
        }

        //TODO: change inners of all other entities with same library name
    }
}