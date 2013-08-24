package com.swe.scene;

import com.jme3.scene.Node;
import com.jme3.scene.Spatial;
import java.util.concurrent.ConcurrentHashMap;

/**
 *
 * @author mifth
 */
public class EditorSceneObject {

    private Node rootNode, allGroupsNode;
    private String sceneName;
    private ConcurrentHashMap<String, EditorLayersGroup> layersGroupsList;
    private Node sceneNode;
    private EditorLayersGroup activelayerGroup;

    public EditorSceneObject(Node rootNode, String sceneName) {
        this.rootNode = rootNode;
        this.sceneName = sceneName;
        
        layersGroupsList = new ConcurrentHashMap<String, EditorLayersGroup>();
        
        createScenNode(sceneName);
    }

    private void createScenNode(String sceneName) {
        //scene
        sceneNode = new Node(sceneName);
        sceneNode.setUserData("isEnabled", true);
        sceneNode.setUserData("isActive", false);

        // general Node of all layerGroups
        allGroupsNode = new Node("LayerGroups");
        sceneNode.attachChild(allGroupsNode);

    }

    public void setSceneEnabled(boolean enable) {
        sceneNode.setUserData("isEnabled", enable);

        if (enable) {
            if (!rootNode.hasChild(sceneNode)) {
                rootNode.attachChild(sceneNode);
            }
        } else {
            if (rootNode.hasChild(sceneNode)) {
                rootNode.detachChild(sceneNode);
            }
        }
    }

    public void createLayersGroup(String layersGroupName) {
        // First Layer Group
        EditorLayersGroup layersGroup = new EditorLayersGroup(allGroupsNode, layersGroupName, sceneName);
        layersGroupsList.put(layersGroupName, layersGroup);
        setActivelayersGroup(layersGroup);
    }

    public void removeLayersGroup(String layersGroupName) {
        layersGroupsList.get(layersGroupName).clearLayersGroup();
        layersGroupsList.remove(layersGroupName);
    }

    public Node getSceneNode() {
        return sceneNode;
    }

    public EditorLayersGroup getActivelayersGroup() {
        return activelayerGroup;
    }

    public void setActivelayersGroup(EditorLayersGroup activelayerGroup) {

        if (this.activelayerGroup != null) {
            this.activelayerGroup.getLayersGroupNode().setUserData("isActive", false); // old active
        }

        if (activelayerGroup != null) {
            this.activelayerGroup = activelayerGroup;
            this.activelayerGroup.getLayersGroupNode().setUserData("isActive", true);  // new active
        } else {
            this.activelayerGroup = null;
        }

    }

    public Node getAllGroupsNode() {
        return allGroupsNode;
    }

    public ConcurrentHashMap<String, EditorLayersGroup> getLayerGroupsList() {
        return layersGroupsList;
    }

    public String getSceneName() {
        return sceneName;
    }

    public void clearScene() {
        for (EditorLayersGroup layersGroup : layersGroupsList.values()) {
            layersGroup.clearLayersGroup();
        }
        layersGroupsList.clear();
        sceneNode.detachAllChildren();
    }
}
