/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.simpleEditor;

import com.entitysystem.ComponentsControl;
import com.entitysystem.EntityManager;
import com.entitysystem.EntityNameComponent;
import com.entitysystem.EntitySpatialsSystem;
import com.entitysystem.EntityTransformComponent;
import com.jme3.app.Application;
import com.jme3.app.FlyCamAppState;
import com.jme3.asset.AssetManager;
import com.jme3.asset.plugins.FileLocator;
import com.jme3.asset.plugins.ZipLocator;
import com.jme3.input.FlyByCamera;
import com.jme3.material.Material;
import com.jme3.math.Transform;
import com.jme3.math.Vector3f;
import com.jme3.renderer.Camera;
import com.jme3.renderer.ViewPort;
import com.jme3.scene.Geometry;
import com.jme3.scene.Node;
import com.jme3.scene.shape.Box;

/**
 *
 * @author mifth
 */
public class EditorBaseManager {

    private Application app;
    private AssetManager assetManager;
    private Camera sceneCamera;
    private ViewPort viewPort;
    private FlyByCamera flyCam;
    
    // Global Nodes
    private Node rootNode, guiNode;
    private Node selectableNode, hidedNode;
    private Node camTrackHelper;
    
    // Tools
    private EditorCameraManager camManager;
    private EditorTransformManager transformManager;
    private EditorMappings mappings;
    private EditorSelectionManager selectionManager;
    private EditorLayerManager layerManager;
    private EntityManager entityManager;
    private EntitySpatialsSystem spatialSystem;
    private EditorSceneManager sceneManager;
    private EditorGuiManager gui;
    private EditorDataManager dataManager;
    private EditorHistoryManager historyManager;

    // Version of the Editor
    private static String editorVersion;

    public EditorBaseManager(Application app) {

        this.app = app;
        sceneCamera = this.app.getCamera();
        viewPort = this.app.getViewPort();
        assetManager = this.app.getAssetManager();
        
        editorVersion = EditorVerion.editorVersion;

        flyCam = this.app.getStateManager().getState(FlyCamAppState.class).getCamera();
        flyCam.setEnabled(false);

        setGlobalNodes();
        
        camManager = new EditorCameraManager(this.app, this);        
        camManager.setCamTracker();
        mappings = new EditorMappings(this.app, this);



        // setup global tools
        historyManager = new EditorHistoryManager(this.app, this);
        dataManager = new EditorDataManager();
        layerManager = new EditorLayerManager(this.app, this);
        selectionManager = new EditorSelectionManager(this.app, this);
        selectableNode.addControl(selectionManager);
        transformManager = new EditorTransformManager(this.app, this);
        selectableNode.addControl(transformManager);   
        spatialSystem = new EntitySpatialsSystem();
        entityManager = new EntityManager();
        sceneManager = new EditorSceneManager(this.app, this);

//        setSomeEntities();

        gui = new EditorGuiManager(this);
        this.app.getStateManager().attach(gui);        
        
    }

    private void setGlobalNodes() {

        rootNode = (Node) app.getViewPort().getScenes().get(0);
        guiNode = (Node) app.getGuiViewPort().getScenes().get(0);        
        
        camTrackHelper = new Node("camTrackHelper");
        rootNode.attachChild(camTrackHelper);                
        
        selectableNode = new Node("selectableNode");
        rootNode.attachChild(selectableNode);

        hidedNode = new Node("hidedNode");
    }

    protected static String getEditorVersion() {
        return editorVersion;
    }    
    
    protected EditorCameraManager getCamManager() {
        return camManager;
    }    
    
    protected EditorTransformManager getTransformManager() {
        return transformManager;
    }

    protected EditorSelectionManager getSelectionManager() {
        return selectionManager;
    }    
    
    protected EditorMappings getEditorMappings() {
        return mappings;
    }    
    
    protected EntityManager getEntityManager() {
        return entityManager;
    }

    protected EntitySpatialsSystem getSpatialSystem() {
        return spatialSystem;
    }

    protected EditorLayerManager getLayerManager() {
        return layerManager;
    }    
    
    protected EditorSceneManager getSceneManager() {
        return sceneManager;
    }

    protected EditorGuiManager getGuiManager() {
        return gui;
    }
    
    protected EditorDataManager getDataManager() {
        return dataManager;
    }

    protected void setDataManager(EditorDataManager dataManager) {
        this.dataManager = dataManager;
    }    
    
    protected EditorHistoryManager getHistoryManager() {
        return historyManager;
    }    

}
