/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.swe;

import com.swe.camera.EditorCameraManager;
import com.swe.othermanagers.EditorLayerManager;
import com.swe.othermanagers.EditorDataManager;
import com.swe.selection.EditorSelectionManager;
import com.swe.history.EditorHistoryManager;
import com.swe.scene.EditorSceneManager;
import com.swe.gui.EditorGuiManager;
import com.swe.transform.EditorTransformManager;
import com.swe.entitysystem.ComponentsControl;
import com.swe.entitysystem.EntityManager;
import com.swe.entitysystem.EntityNameComponent;
import com.swe.entitysystem.EntitySpatialsSystem;
import com.swe.entitysystem.EntityTransformComponent;
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

    public static String getEditorVersion() {
        return editorVersion;
    }    
    
    public EditorCameraManager getCamManager() {
        return camManager;
    }    
    
    public EditorTransformManager getTransformManager() {
        return transformManager;
    }

    public EditorSelectionManager getSelectionManager() {
        return selectionManager;
    }    
    
    public EditorMappings getEditorMappings() {
        return mappings;
    }    
    
    public EntityManager getEntityManager() {
        return entityManager;
    }

    public EntitySpatialsSystem getSpatialSystem() {
        return spatialSystem;
    }

    public EditorLayerManager getLayerManager() {
        return layerManager;
    }    
    
    public EditorSceneManager getSceneManager() {
        return sceneManager;
    }

    public EditorGuiManager getGuiManager() {
        return gui;
    }
    
    public EditorDataManager getDataManager() {
        return dataManager;
    }

    public void setDataManager(EditorDataManager dataManager) {
        this.dataManager = dataManager;
    }    
    
    public EditorHistoryManager getHistoryManager() {
        return historyManager;
    }    

}
