/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.swe.othermanagers;

import com.jme3.app.Application;
import com.jme3.asset.AssetManager;
import com.jme3.scene.Node;
import com.jme3.scene.Spatial;
import com.swe.EditorBaseManager;
import java.util.ArrayList;
import java.util.List;

/**
 *
 * @author mifth
 */
public class EditorLayerManager {

    private AssetManager assetMan;
    private Node root, guiNode;
    private Application app;
    private EditorBaseManager base;
    private Node selectableNode;


    private static List<Node> layersList;
    private Node activeLayer;

    public EditorLayerManager(Application app, EditorBaseManager base) {
        this.app = app;
        this.base = base;
        assetMan = app.getAssetManager();
        root = (Node) this.app.getViewPort().getScenes().get(0);
        guiNode = (Node) this.app.getGuiViewPort().getScenes().get(0);
        selectableNode = (Node) root.getChild("selectableNode");
        layersList = new ArrayList<Node>();
        createLayers();
    }

    private void createLayers() {
        for (int i = 0; i < 20; i++) {
            Node layerNode = new Node("layer" + (i + 1));
            layerNode.setUserData("LayerNumber", i + 1);
            layerNode.setUserData("isEnabled", false);
            layerNode.setUserData("isActive", false);
            layersList.add(layerNode);

            // set default active layer
            if (i + 1 == 1) {
                selectableNode.attachChild(layerNode);
                layerNode.setUserData("isEnabled", true);
                layerNode.setUserData("isActive", true);
                activeLayer = layerNode;
            }
        }
    }


    public Node getSelectableNode() {
        return selectableNode;
    }    
    
    public Node getLayer(int layerNumber) {
        Node nd = layersList.get(layerNumber - 1);  // compensate the list number
        return nd;
    }

    public List<Node> getLayers() {
        return layersList;
    }

    public void addToLayer(Spatial sp, int layerNumber) {
        getLayer(layerNumber).attachChild(sp);
    }

    public Node getActiveLayer() {
        return activeLayer;
    }

    public void setActiveLayer(Node activeLayer) {
        this.activeLayer = activeLayer;
    }

    public void clearLayerManager() {
        for (Node layer : layersList) {
            layer.detachAllChildren();
            layer.setUserData("isEnabled", false);
            layer.setUserData("isActive", false);
        }
        selectableNode.detachAllChildren();
        activeLayer = null;
    }
}