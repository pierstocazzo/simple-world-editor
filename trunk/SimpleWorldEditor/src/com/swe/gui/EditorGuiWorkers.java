/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.swe.gui;

import com.jme3.scene.Node;
import com.jme3.scene.Spatial;
import com.swe.EditorBaseManager;
import com.swe.scene.EditorLayersGroupObject;
import com.swe.scene.EditorSceneObject;
import de.lessvoid.nifty.controls.CheckBox;
import de.lessvoid.nifty.controls.ListBox;
import de.lessvoid.nifty.controls.nullobjects.CheckBoxNull;
import de.lessvoid.nifty.effects.EffectEventId;
import de.lessvoid.nifty.elements.Element;
import de.lessvoid.nifty.screen.Screen;
import java.util.ArrayList;
import java.util.List;

/**
 *
 * @author mifth
 */
public class EditorGuiWorkers extends EditorGuiAbstractChild {

    public EditorGuiWorkers(EditorBaseManager base) {
        super(base);
    }

    protected void updateSceneGUI(boolean updateScenesListbox, boolean updateLayersGroupsListbox) {
        EditorSceneObject activeScene = base.getSceneManager().getActiveSceneObject();
        EditorLayersGroupObject activeLayersGroup = base.getSceneManager().getActiveSceneObject().getActivelayersGroup();

        // savePreviewj3o checkbox
        CheckBox cbPreview = screen.findNiftyControl("savePreviewJ3O", CheckBox.class);
        updateCheckbox(base.getSceneManager().getSavePreviewJ3o(), "savePreviewJ3O");

        // set Scenes ListBox
        if (updateScenesListbox) {
            scenesListbox.clear();
            String activeSceneName = activeScene.getSceneName();
            for (String sceneName : base.getSceneManager().getScenesList().keySet()) {
                scenesListbox.addItem(sceneName);
                if (sceneName.equals(activeSceneName)) {
                    scenesListbox.selectItem(sceneName);
                }
            }
            scenesListbox.sortAllItems();
            scenesListbox.refresh();
        }

        // set checkbox isEnabled scene
        boolean isSceneEnabled = (Boolean) activeScene.getSceneNode().getUserData("isEnabled");
        updateCheckbox(isSceneEnabled, "isSceneEnabled");

        // set LayersGroups ListBox
        if (updateLayersGroupsListbox) {
            layersGroupsListbox.clear();
            String activeLayerGroupName = activeLayersGroup.getLayersGroupName();
            for (String layerGroupName : base.getSceneManager().getActiveSceneObject().getLayersGroupsList().keySet()) {
                layersGroupsListbox.addItem(layerGroupName);
                if (layerGroupName.equals(activeLayerGroupName)) {
                    layersGroupsListbox.selectItem(layerGroupName);
                }
            }
            layersGroupsListbox.sortAllItems();
            layersGroupsListbox.refresh();
        }

        // set checkbox LayersGroup 
        boolean isLayersGroupEnabled = (Boolean) activeLayersGroup.getLayersGroupNode().getUserData("isEnabled");
        updateCheckbox(isLayersGroupEnabled, "isLayersGroupEnabled");

        updateLayersGUI();

        // update list of layers group
        layersGroupsObjectsListBox.clear();
        for (Node layers : base.getSceneManager().getActiveSceneObject().getActivelayersGroup().getLayers()) {
            for (Spatial sp : layers.getChildren()) {
                layersGroupsObjectsListBox.addItem(sp.getName());
            }
        }
        layersGroupsObjectsListBox.sortAllItems();
        layersGroupsObjectsListBox.refresh();

    }

    protected void updateLayersGUI() {
        // set checkboxes for layers
        for (int i = 0; i < 20; i++) {
            Node layer = base.getSceneManager().getActiveSceneObject().getActivelayersGroup().getLayer(i + 1);

            // LAYERS VISIBILITY
            CheckBox cbVisible = screen.findNiftyControl("layerVisibility" + (i + 1), CheckBox.class);
            Object isEnabledObj = layer.getUserData("isEnabled");
            boolean isEnabled = (Boolean) isEnabledObj;

            if (isEnabled) {
                cbVisible.check();
            } else {
                cbVisible.uncheck();
            }


            // LAYERS LOCK
            CheckBox cbLocked = screen.findNiftyControl("layerLock" + (i + 1), CheckBox.class);
            Object isLockedObj = layer.getUserData("isLocked");
            boolean isLocked = (Boolean) isLockedObj;

            if (isLocked) {
                cbLocked.check();
            } else {
                cbLocked.uncheck();
            }

            // deselect Red Color of all LayersVisibility
            Element deselectImageVisibility = screen.findElementByName("layerVisibility" + (i + 1));
            deselectImageVisibility.stopEffect(EffectEventId.onFocus);
            Element deselectImageLock = screen.findElementByName("layerLock" + (i + 1));
            deselectImageLock.stopEffect(EffectEventId.onFocus);

        }

        // SET THE LAYER ACTIVE (Red color)
        Node activeLayer = base.getSceneManager().getActiveSceneObject().getActivelayersGroup().getActiveLayer();
        if (activeLayer != null) {
            screen.getFocusHandler().resetFocusElements();
            int activeLayerNumb = (Integer) base.getSceneManager().getActiveSceneObject().getActivelayersGroup().getActiveLayer().getUserData("LayerNumber");
            Element selectImage = screen.findElementByName("layerVisibility" + activeLayerNumb);
            selectImage.startEffect(EffectEventId.onFocus);
        }
        screen.getFocusHandler().resetFocusElements();
    }

    protected void updateCheckbox(boolean setBoolean, String checkboxID) {
        CheckBox cbScene = screen.findNiftyControl(checkboxID, CheckBox.class);

        if (setBoolean) {
            cbScene.check();
        } else {
            cbScene.uncheck();
        }
    }

    protected void clearGui() {
        // clear gui lists
        scenesListbox.clear();
        layersGroupsListbox.clear();
        entitiesListBox.clear();
        layersGroupsObjectsListBox.clear();
        componentsListBox.clear();

        // clear layers
        for (int i = 0; i < 20; i++) {
            CheckBox cb = screen.findNiftyControl("layerVisibility" + (i + 1), CheckBox.class);
            cb.uncheck();
            Element selectActiveLayerImage = screen.findElementByName("layerVisibility" + (i + 1));
            selectActiveLayerImage.stopEffect(EffectEventId.onFocus);
            selectActiveLayerImage.startEffect(EffectEventId.onEnabled);
        }
        // clear layers locks
        for (int i = 0; i < 20; i++) {
            CheckBox cb = screen.findNiftyControl("layerLock" + (i + 1), CheckBox.class);
            if (cb.isChecked()) {
                cb.uncheck();
            }
        }


        // clear assets
        assetsListBox.clear();

        screen.getFocusHandler().resetFocusElements();
    }

    // This function is for removing of entities from the selectionList
    // if scene settings were changed
    protected void checkSelectionList() {
        List<Long> selList = base.getSelectionManager().getSelectionList();
        List<Long> tempSelList = new ArrayList<Long>();

        for (Long id : selList) {
            Node selectedModel = (Node) base.getSpatialSystem().getSpatialControl(id).getGeneralNode();
            String isActiveSceneOfEntity = (String) selectedModel.getParent().getUserData("SceneName");
            String isActiveLayersGroupOfEntity = (String) selectedModel.getParent().getUserData("LayersGroupName");
            // if entity is on active scene and layersGroup - check entity's layer
            EditorLayersGroupObject activeLayerGroup = base.getSceneManager().getActiveSceneObject().getActivelayersGroup();
            EditorSceneObject activeScene = base.getSceneManager().getActiveSceneObject();

            if (activeScene.getSceneName().equals(isActiveSceneOfEntity)
                    && activeLayerGroup.getLayersGroupName().equals(isActiveLayersGroupOfEntity)) {

                boolean isActiveSceneEnabled = (Boolean) activeScene.getSceneNode().getUserData("isEnabled");
                boolean isActiveLayersGroupEnabled = (Boolean) activeLayerGroup.getLayersGroupNode().getUserData("isEnabled");
                boolean isLayerOfEntityLocked = (Boolean) selectedModel.getParent().getUserData("isLocked");
                boolean isLayerOfEntityEnabled = (Boolean) selectedModel.getParent().getUserData("isEnabled");

                // remove entity from selection if scene or LayersGroup disabled and if layer is disabled or locked
                if (!isActiveSceneEnabled || !isActiveLayersGroupEnabled
                        || isLayerOfEntityLocked || !isLayerOfEntityEnabled) {
                    tempSelList.add(id);
                }
            } // remove entity from selection if scene or layersGroup not active
            else {
                tempSelList.add(id);
            }

        }

        // do remove entities from selection
        for (Long idToRemove : tempSelList) {
            selList.remove(idToRemove);
            base.getSelectionManager().removeSelectionBox((Node) base.getSpatialSystem().getSpatialControl(idToRemove).getGeneralNode());
        }
        base.getSelectionManager().calculateSelectionCenter();
        tempSelList.clear();
        tempSelList = null;
    }

    @Deprecated
    protected void updateCheckBoxes(Element mainElement) {
        for (Element childElement : mainElement.getElements()) {

            if (!childElement.getNiftyControl(CheckBox.class).getClass().equals(CheckBoxNull.class)) {
                // partial fix for checkboxes
                CheckBox cb = childElement.getNiftyControl(CheckBox.class);
                cb.getElement().resetAllEffects();
                if (cb.isChecked()) {
                    cb.uncheck();
                    cb.check();
                }
                System.out.println("Found!" + cb);
            }

            // recourse function
            if (childElement.getElements().size() > 0) {
                updateCheckBoxes(childElement);
            }
        }
    }
}
