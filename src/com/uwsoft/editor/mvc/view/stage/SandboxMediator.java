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

package com.uwsoft.editor.mvc.view.stage;

import java.awt.Cursor;
import java.util.HashMap;

import com.badlogic.ashley.core.Engine;
import com.badlogic.ashley.core.Entity;
import com.badlogic.ashley.utils.ImmutableArray;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.scenes.scene2d.Actor;
import com.badlogic.gdx.scenes.scene2d.InputEvent;
import com.badlogic.gdx.scenes.scene2d.utils.ClickListener;
import com.badlogic.gdx.utils.Align;
import com.puremvc.patterns.mediator.SimpleMediator;
import com.puremvc.patterns.observer.Notification;
import com.uwsoft.editor.Overlap2D;
import com.uwsoft.editor.gdx.sandbox.ItemFactory;
import com.uwsoft.editor.gdx.sandbox.Sandbox;
import com.uwsoft.editor.mvc.Overlap2DFacade;
import com.uwsoft.editor.mvc.proxy.SceneDataManager;
import com.uwsoft.editor.mvc.view.stage.input.EntityClickListener;
import com.uwsoft.editor.mvc.view.stage.input.InputListener;
import com.uwsoft.editor.mvc.view.stage.input.InputListenerComponent;
import com.uwsoft.editor.mvc.view.stage.tools.ConeLightTool;
import com.uwsoft.editor.mvc.view.stage.tools.PanTool;
import com.uwsoft.editor.mvc.view.stage.tools.PointLightTool;
import com.uwsoft.editor.mvc.view.stage.tools.SelectionTool;
import com.uwsoft.editor.mvc.view.stage.tools.TextTool;
import com.uwsoft.editor.mvc.view.stage.tools.Tool;
import com.uwsoft.editor.mvc.view.stage.tools.TransformTool;
import com.uwsoft.editor.mvc.view.ui.box.UIToolBoxMediator;

/**
 * Created by sargis on 4/20/15.
 */
public class SandboxMediator extends SimpleMediator<Sandbox> {
    private static final String TAG = SandboxMediator.class.getCanonicalName();
    public static final String NAME = TAG;

    private static final String PREFIX =  "com.uwsoft.editor.mvc.view.stage.SandboxStageMediator";
    public static final String SANDBOX_TOOL_CHANGED = PREFIX + ".SANDBOX_TOOL_CHANGED";

    private final Vector2 reducedMoveDirection = new Vector2(0, 0);

    private SandboxStageEventListener stageListener;

    private Tool hotSwapMemory;

    private HashMap<String, Tool> sandboxTools = new HashMap<>();
    private Tool currentSelectedTool;

    public SandboxMediator() {
        super(NAME, Sandbox.getInstance());
    }

    @Override
    public void onRegister() {
        super.onRegister();

        facade = Overlap2DFacade.getInstance();

        stageListener = new SandboxStageEventListener();

        initTools();
    }

    private void initTools() {
        sandboxTools.put(SelectionTool.NAME, new SelectionTool());
        sandboxTools.put(TransformTool.NAME, new TransformTool());
        sandboxTools.put(TextTool.NAME, new TextTool());
        sandboxTools.put(PointLightTool.NAME, new PointLightTool());
        sandboxTools.put(ConeLightTool.NAME, new ConeLightTool());
        sandboxTools.put(PanTool.NAME, new PanTool());

    }

    private void setCurrentTool(String toolName) {
        currentSelectedTool = sandboxTools.get(toolName);
        facade.sendNotification(SANDBOX_TOOL_CHANGED, currentSelectedTool);
        currentSelectedTool.initTool();
    }

    @Override
    public String[] listNotificationInterests() {
        return new String[]{
                SceneDataManager.SCENE_LOADED,
                UIToolBoxMediator.TOOL_SELECTED,
                ItemFactory.NEW_ITEM_ADDED,
                Overlap2D.OPENED_PREVIOUS_COMPOSITE
        };
    }

    @Override
    public void handleNotification(Notification notification) {
        super.handleNotification(notification);
        switch (notification.getName()) {
            case SceneDataManager.SCENE_LOADED:
                handleSceneLoaded(notification);
                break;
            case UIToolBoxMediator.TOOL_SELECTED:
                setCurrentTool(notification.getBody());
                break;
            case ItemFactory.NEW_ITEM_ADDED:
            	//TODO add listener and uncomment
                //((Actor)notification.getBody()).addListener(new SandboxItemEventListener(notification.getBody()));
                break;
            case Overlap2D.OPENED_PREVIOUS_COMPOSITE:
                initItemListeners();
                break;
            default:
                break;
        }
    }

    private void handleSceneLoaded(Notification notification) {
		//TODO fix and uncomment
        //viewComponent.addListener(stageListener);

        initItemListeners();

        setCurrentTool(SelectionTool.NAME);
    }

    private void initItemListeners() {
        Engine engine = getViewComponent().getEngine();
        ImmutableArray<Entity> entities = engine.getEntities();
        for (int i = 0; i < entities.size(); i++) {
        	Entity entity = entities.get(i);
        	InputListenerComponent inputListenerComponent = entity.getComponent(InputListenerComponent.class);
        	if(inputListenerComponent == null){
        		
        		continue;
        	}
        	inputListenerComponent.removeAllListener();
        	inputListenerComponent.addListener(new SandboxItemEventListener(entity));
        }
		
    }

    public Vector2 getStageCoordinates() {
    	return new Vector2(); //temporary for not getting errors
    	//TODO fix and uncomment
        //return Sandbox.getInstance().getSandboxStage().screenToStageCoordinates(new Vector2(Gdx.input.getX(), Gdx.input.getY()));
    }

    private class SandboxItemEventListener extends EntityClickListener {

        private Entity targetEntity;

        public SandboxItemEventListener(final Entity entity) {
            this.targetEntity = entity;
        }

        public boolean touchDown(Entity entity, float x, float y, int pointer, int button) {
            super.touchDown(entity, x, y, pointer, button);
            Vector2 coords = getStageCoordinates();
            return currentSelectedTool.itemMouseDown(targetEntity, coords.x, coords.y);
        }

        public void touchUp(Entity entity, float x, float y, int pointer, int button) {
            super.touchUp(entity, x, y, pointer, button);
            Vector2 coords = getStageCoordinates();

            currentSelectedTool.itemMouseUp(targetEntity, x, y);

            if (getTapCount() == 2) {
                // this is double click
                currentSelectedTool.itemMouseDoubleClick(targetEntity, coords.x, coords.y);
            }

            if (button == Input.Buttons.RIGHT) {
                // if right clicked on an item, drop down for current selection
                Overlap2DFacade.getInstance().sendNotification(Overlap2D.ITEM_RIGHT_CLICK);
            }
        }

        public void touchDragged(InputEvent event, float x, float y, int pointer) {
            Vector2 coords = getStageCoordinates();
            currentSelectedTool.itemMouseDragged(targetEntity, coords.x, coords.y);
        }

    }

    private class SandboxStageEventListener extends ClickListener {
        public SandboxStageEventListener() {
            setTapCountInterval(.5f);
        }

        @Override
        public boolean keyDown(InputEvent event, int keycode) {
            boolean isControlPressed = isControlPressed();
            Sandbox sandbox = Sandbox.getInstance();
            // the amount of pixels by which to move item if moving
            float deltaMove = 1;

            // if control is pressed then z index is getting modified
            // TODO: key pressed 0 for unckown, should be removed?
            // TODO: need to make sure OSX Command button works too.


            // Control pressed as well
            if (isControlPressed()) {
                if (keycode == Input.Keys.UP) {
                    // going to front of next item in z-index ladder
                    sandbox.itemControl.itemZIndexChange(sandbox.getSelector().getCurrentSelection(), true);
                }
                if (keycode == Input.Keys.DOWN) {
                    // going behind the next item in z-index ladder
                    sandbox.itemControl.itemZIndexChange(sandbox.getSelector().getCurrentSelection(), false);
                }
                if (keycode == Input.Keys.A) {
                    // Ctrl+A means select all
                    facade.sendNotification(Sandbox.ACTION_SET_SELECTION, sandbox.getSelector().getAllFreeItems());
                }
                // Aligning Selections
                if (keycode == Input.Keys.NUM_1 && Gdx.input.isKeyPressed(Input.Keys.CONTROL_LEFT)) {
                    sandbox.getSelector().alignSelections(Align.top);
                }
                if (keycode == Input.Keys.NUM_2 && Gdx.input.isKeyPressed(Input.Keys.CONTROL_LEFT)) {
                    sandbox.getSelector().alignSelections(Align.left);
                }
                if (keycode == Input.Keys.NUM_3 && Gdx.input.isKeyPressed(Input.Keys.CONTROL_LEFT)) {
                    sandbox.getSelector().alignSelections(Align.bottom);
                }
                if (keycode == Input.Keys.NUM_4 && Gdx.input.isKeyPressed(Input.Keys.CONTROL_LEFT)) {
                    sandbox.getSelector().alignSelections(Align.right);
                }
                if (keycode == Input.Keys.NUM_0 || keycode == Input.Keys.NUMPAD_0) {
                    sandbox.setZoomPercent(100);
                    facade.sendNotification(Overlap2D.ZOOM_CHANGED);
                }
                if (keycode == Input.Keys.X) {
                    facade.sendNotification(Sandbox.ACTION_CUT);
                }
                if (keycode == Input.Keys.C) {
                    facade.sendNotification(Sandbox.ACTION_COPY);
                }
                if (keycode == Input.Keys.V) {
                    facade.sendNotification(Sandbox.ACTION_PASTE);
                }
                if(keycode == Input.Keys.Z) {
                    if(Gdx.input.isKeyPressed(Input.Keys.SHIFT_LEFT)) {
                        sandbox.getUac().redo();
                    } else {
                        sandbox.getUac().undo();
                    }
                }

                return true;
            }

            if (Gdx.input.isKeyPressed(Input.Keys.S) && !isControlPressed()) {
                setCurrentTool(SelectionTool.NAME);
                UIToolBoxMediator toolBoxMediator = facade.retrieveMediator(UIToolBoxMediator.NAME);
                toolBoxMediator.setCurrentTool(SelectionTool.NAME);
            }

            if (Gdx.input.isKeyPressed(Input.Keys.SHIFT_LEFT)) {
                // if shift is pressed, move boxes by 20 pixels instead of one
                deltaMove = 20; //pixels
            }

            if (sandbox.getGridSize() > 1) {
                deltaMove = sandbox.getGridSize();
                if (Gdx.input.isKeyPressed(Input.Keys.SHIFT_LEFT)) {
                    // if shift is pressed, move boxes 3 times more then the grid size
                    deltaMove *= 3;
                }
            }

            if (keycode == Input.Keys.UP) {
                // moving UP
                sandbox.getSelector().moveSelectedItemsBy(0, deltaMove);
            }
            if (keycode == Input.Keys.DOWN) {
                // moving down
                sandbox.getSelector().moveSelectedItemsBy(0, -deltaMove);
            }
            if (keycode == Input.Keys.LEFT) {
                // moving left
                sandbox.getSelector().moveSelectedItemsBy(-deltaMove, 0);
            }
            if (keycode == Input.Keys.RIGHT) {
                //moving right
                sandbox.getSelector().moveSelectedItemsBy(deltaMove, 0);
            }

            // Delete
            if (keycode == Input.Keys.DEL || keycode == Input.Keys.FORWARD_DEL) {
                facade.sendNotification(Sandbox.ACTION_DELETE);
            }

            // if space is pressed, that means we are going to pan, so set cursor accordingly
            // TODO: this pan is kinda different from what happens when you press middle button, so things need to merge right
            if (keycode == Input.Keys.SPACE) {
                sandbox.setCursor(Cursor.HAND_CURSOR);
                toolHotSwap(sandboxTools.get(PanTool.NAME));
            }

            // Zoom
            if (keycode == Input.Keys.MINUS && isControlPressed) {
                sandbox.zoomDevideBy(2f);
            }
            if (keycode == Input.Keys.EQUALS && isControlPressed) {
                sandbox.zoomDevideBy(0.5f);
            }

            return true;
        }

        @Override
        public boolean keyUp(InputEvent event, int keycode) {
            Sandbox sandbox = Sandbox.getInstance();
            if (keycode == Input.Keys.DEL) {
                // delete selected item
                sandbox.getSelector().removeCurrentSelectedItems();
            }
            if (keycode == Input.Keys.SPACE) {
                // if pan mode is disabled set cursor back
                sandbox.setCursor(Cursor.DEFAULT_CURSOR);
                toolHotSwapBack();
            }

            return true;
        }


        @Override
        public boolean touchDown(InputEvent event, float x, float y, int pointer, int button) {
            super.touchDown(event, x, y, pointer, button);

            Sandbox sandbox = Sandbox.getInstance();

            // setting key and scroll focus on main area
            sandbox.getUIStage().setKeyboardFocus();
            sandbox.getUIStage().setScrollFocus(sandbox.mainBox);
            sandbox.setKeyboardFocus();

            // if there was a drop down remove it
            // TODO: this is job for front UI to figure out
            //sandbox.getUIStage().mainDropDown.hide();

            switch (button) {
                case Input.Buttons.MIDDLE:
                    // if middle button is pressed - PAN the scene
                    toolHotSwap(sandboxTools.get(PanTool.NAME));
                    break;
            }

            currentSelectedTool.stageMouseDown(x, y);

            return true;
        }

        @Override
        public void touchUp(InputEvent event, float x, float y, int pointer, int button) {
            super.touchUp(event, x, y, pointer, button);

            currentSelectedTool.stageMouseUp(x, y);

            Sandbox sandbox = Sandbox.getInstance();
            if (button == Input.Buttons.RIGHT) {
                // if clicked on empty space, selections need to be cleared
                sandbox.getSelector().clearSelections();

                // show default dropdown
                facade.sendNotification(Overlap2D.SCENE_RIGHT_CLICK, new Vector2(event.getStageX(), event.getStageY()));

                return;
            }

            if (button == Input.Buttons.MIDDLE) {
                toolHotSwapBack();
            }

            if (getTapCount() == 2 && button == Input.Buttons.LEFT) {
                doubleClick(event, x, y);
            }

        }

        private void doubleClick(InputEvent event, float x, float y) {
            Sandbox sandbox = Sandbox.getInstance();
            currentSelectedTool.stageMouseDoubleClick(x, y);
        }

        @Override
        public void touchDragged(InputEvent event, float x, float y, int pointer) {
            Sandbox sandbox = Sandbox.getInstance();

            currentSelectedTool.stageMouseDragged(x, y);
        }


        @Override
        public boolean scrolled(InputEvent event, float x, float y, int amount) {
            Sandbox sandbox = Sandbox.getInstance();
            // well, duh
            if (amount == 0) return false;

            // if item is currently being held with mouse (touched in but not touched out)
            // mouse scroll should rotate the selection around it's origin
            /*
            if (sandbox.isItemTouched) {
                for (SelectionRectangle value : sandbox.getSelector().getCurrentSelection().values()) {
                    float degreeAmount = 1;
                    if (amount < 0) degreeAmount = -1;
                    // And if shift is pressed, the rotation amount is bigger
                    if (Gdx.input.isKeyPressed(Input.Keys.SHIFT_LEFT)) {
                        degreeAmount = degreeAmount * 30;
                    }
                    value.getHostAsActor().rotateBy(degreeAmount);
                    value.update();
                }
                facade.sendNotification(Overlap2D.ITEM_DATA_UPDATED);
                sandbox.dirty = true;
            } else if (Gdx.input.isKeyPressed(Input.Keys.ALT_LEFT)) {
                // if not item is touched then we can use this for zoom
                sandbox.zoomBy(amount);
            }
            */

            return false;
        }

        private boolean isControlPressed() {
            return Gdx.input.isKeyPressed(Input.Keys.SYM)
                    || Gdx.input.isKeyPressed(Input.Keys.CONTROL_LEFT)
                    || Gdx.input.isKeyPressed(Input.Keys.CONTROL_RIGHT);
        }

        private boolean isShiftKey(int keycode) {
            return keycode == Input.Keys.SHIFT_LEFT
                    || keycode == Input.Keys.SHIFT_RIGHT;
        }

        private boolean isShiftPressed() {
            return Gdx.input.isKeyPressed(Input.Keys.SHIFT_LEFT)
                    || Gdx.input.isKeyPressed(Input.Keys.SHIFT_RIGHT);
        }
    }

    private void toolHotSwap(Tool tool) {
        hotSwapMemory = currentSelectedTool;
        currentSelectedTool = tool;
    }

    private void toolHotSwapBack() {
        currentSelectedTool = hotSwapMemory;
        hotSwapMemory = null;
    }
}