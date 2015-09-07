package com.uwsoft.editor.view.ui.followers;

import com.badlogic.ashley.core.Entity;
import com.badlogic.gdx.graphics.OrthographicCamera;
import com.uwsoft.editor.view.stage.Sandbox;

public class CompositeFollower extends NormalSelectionFollower {
	public CompositeFollower(Entity entity) {
        super(entity);
    }


    @Override
    public void update() {
        super.update();
        
        Sandbox sandbox = Sandbox.getInstance();
        OrthographicCamera camera = Sandbox.getInstance().getCamera();

        int pixelPerWU = sandbox.sceneControl.sceneLoader.getRm().getProjectVO().pixelToWorld;
        
        setX(getX()+dimensionsComponent.boundBox.x);
        setY(getY()+dimensionsComponent.boundBox.y);
        setWidth(pixelPerWU * dimensionsComponent.boundBox.width * transformComponent.scaleX / camera.zoom);
        setHeight(pixelPerWU * dimensionsComponent.boundBox.height * transformComponent.scaleY / camera.zoom);
    }
}
