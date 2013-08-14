/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.swe.gui;

import com.swe.transform.EditorTransformConstraint;
import com.swe.transform.EditorTransformManager;
import com.swe.entitysystem.EntityNameComponent;
import com.jme3.app.Application;
import com.jme3.app.SimpleApplication;
import com.jme3.app.state.AbstractAppState;
import com.jme3.app.state.AppStateManager;
import com.jme3.asset.AssetManager;
import com.jme3.bounding.BoundingBox;
import com.jme3.font.BitmapFont;
import com.jme3.font.BitmapText;
import com.jme3.light.AmbientLight;
import com.jme3.light.DirectionalLight;
import com.jme3.material.Material;
import com.jme3.material.RenderState;
import com.jme3.math.ColorRGBA;
import com.jme3.math.Quaternion;
import com.jme3.math.Vector3f;
import com.jme3.niftygui.NiftyJmeDisplay;
import com.jme3.renderer.ViewPort;
import com.jme3.renderer.queue.RenderQueue;
import com.jme3.scene.Geometry;
import com.jme3.scene.Node;
import com.jme3.scene.Spatial;
import com.jme3.scene.debug.Grid;
import com.jme3.scene.shape.Line;
import com.swe.EditorBaseManager;
import com.swe.selection.EditorSelectionManager;
import de.lessvoid.nifty.Nifty;
import de.lessvoid.nifty.NiftyEventSubscriber;
import de.lessvoid.nifty.controls.CheckBox;
import de.lessvoid.nifty.controls.ListBox;
import de.lessvoid.nifty.controls.RadioButtonGroupStateChangedEvent;
import de.lessvoid.nifty.controls.TextField;
import de.lessvoid.nifty.controls.scrollbar.ScrollbarControl;
import de.lessvoid.nifty.effects.EffectEventId;
import de.lessvoid.nifty.elements.Element;
import de.lessvoid.nifty.screen.Screen;
import de.lessvoid.nifty.screen.ScreenController;
import java.util.List;
import java.util.concurrent.ConcurrentHashMap;
import java.util.logging.ConsoleHandler;
import java.util.logging.Handler;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 *
 * @author mifth
 */
public class EditorGuiManager extends AbstractAppState implements ScreenController {

    private Screen screen;
    private static Nifty nifty;
    private SimpleApplication application;
    private Node gridNode, rootNode, guiNode;
    private AssetManager assetManager;
    private ViewPort guiViewPort;
    private EditorBaseManager base;
    private Element popupMoveToLayer, popupEditComponent, rightPanel;
    private ListBox entitiesListBox, sceneObjectsListBox, componentsListBox;
    private long lastIdOfComponentList, idComponentToChange;

    public EditorGuiManager(EditorBaseManager base) {
        this.base = base;
    }

    @Override
    public void initialize(AppStateManager stateManager, Application app) {

        super.initialize(stateManager, app);
        application = (SimpleApplication) app;
        rootNode = application.getRootNode();
        assetManager = app.getAssetManager();
        guiNode = application.getGuiNode();
        guiViewPort = application.getGuiViewPort();

        createGrid();
//        createSimpleGui();

        NiftyJmeDisplay niftyDisplay = new NiftyJmeDisplay(application.getAssetManager(),
                application.getInputManager(),
                application.getAudioRenderer(),
                guiViewPort);

        nifty = niftyDisplay.getNifty();
//     nifty.loadStyleFile("nifty-default-styles.xml");
//     nifty.loadControlFile("nifty-default-controls.xml");        
        nifty.fromXml("Interface/Main/basicGui.xml", "start", this);


        // attach the nifty display to the gui view port as a processor
        guiViewPort.addProcessor(niftyDisplay);
        application.getInputManager().setCursorVisible(true);

//    Element niftyElement = nifty.getCurrentScreen().findElementByName("button1");
//    niftyElement.getElementInteraction().getPrimary().setOnClickMethod(new NiftyMethodInvoker(nifty, "printGo()", this));


        // Set Logger for only warnings     
        Logger root = Logger.getLogger("");
        Handler[] handlers = root.getHandlers();
        for (int i = 0; i < handlers.length; i++) {
            if (handlers[i] instanceof ConsoleHandler) {
                ((ConsoleHandler) handlers[i]).setLevel(Level.WARNING);
            }
        }


        // set checkboxes for layers
        CheckBox lastEnabled = null;
        for (int i = 0; i < 20; i++) {
            CheckBox cb = nifty.getScreen("start").findNiftyControl("layer" + (i + 1), CheckBox.class);
            Node layer = base.getLayerManager().getLayer(i + 1);
            Object isEnabledObj = layer.getUserData("isEnabled");
            boolean isEnabled = (Boolean) isEnabledObj;
            if (isEnabled) {

                cb.check();
                lastEnabled = cb;
            } else {
                cb.uncheck();
            }
        }


        // SET THE LAYER ACTIVE (Red color)
        Node activeLayer = base.getLayerManager().getActiveLayer();
        if (activeLayer != null) {
            nifty.getScreen("start").getFocusHandler().resetFocusElements();
            Element selectImage = nifty.getScreen("start").findElementByName(base.getLayerManager().getActiveLayer().getName());
            selectImage.startEffect(EffectEventId.onFocus);
        } // SET LAST SELECTED LAYER (IF IT PARSES NOT SO GOOD)
        else if (activeLayer == null && lastEnabled != null) {

            nifty.getScreen("start").getFocusHandler().resetFocusElements();
            Element selectImage = lastEnabled.getElement();
            selectImage.startEffect(EffectEventId.onFocus);
        }

        // set popup test
        popupMoveToLayer = nifty.createPopup("popupMoveToLayer");
        popupMoveToLayer.disable();
        screen.getFocusHandler().resetFocusElements();

        // set popup test
        popupEditComponent = nifty.createPopup("popupEditComponent");
        popupEditComponent.disable();
        screen.getFocusHandler().resetFocusElements();

        // ListBoxes
        entitiesListBox = nifty.getScreen("start").findNiftyControl("entitiesListBox", ListBox.class);
        sceneObjectsListBox = nifty.getScreen("start").findNiftyControl("sceneObjectsListBox", ListBox.class);
        componentsListBox = nifty.getScreen("start").findNiftyControl("componentsListBox", ListBox.class);
        sceneObjectsListBox.changeSelectionMode(ListBox.SelectionMode.Multiple, false);

        // rightPanel
        rightPanel = nifty.getScreen("start").findElementByName("settingsRightPanel");

        //Temp
        nifty.getScreen("start").findNiftyControl("scenePath1", TextField.class).setText("/home/mifth/jMonkeyProjects/AD/ad/trunk/ADAssets/assets");

        nifty.gotoScreen("start"); // start the screen 
        screen.getFocusHandler().resetFocusElements();
    }

    protected void clearGui() {
        // clear gui lists
        entitiesListBox.clear();
        sceneObjectsListBox.clear();
        componentsListBox.clear();

        // clear layers
        for (int i = 0; i < 20; i++) {
            CheckBox cb = screen.findNiftyControl("layer" + (i + 1), CheckBox.class);
            cb.uncheck();
            Element selectActiveLayerImage = screen.findElementByName("layer" + (i + 1));
            selectActiveLayerImage.stopEffect(EffectEventId.onFocus);
            selectActiveLayerImage.startEffect(EffectEventId.onEnabled);
        }

        // clear assets
        for (int i = 0; i < 7; i++) {
            String strID = "scenePath" + (i + 1);
            nifty.getScreen("start").findNiftyControl(strID, TextField.class).setText("");
        }

        screen.getFocusHandler().resetFocusElements();
    }

    public static Nifty getNifty() {
        return nifty;
    }

    /**
     * This is called when the RadioButton selection has changed.
     */
    @NiftyEventSubscriber(id = "RadioGroupConstraints")
    public void RadioGroupConstraintsChanged(final String id, final RadioButtonGroupStateChangedEvent event) {

        if (event.getSelectedId().equals("constraint_none")) {
            base.getTransformManager().getConstraintTool().setConstraint(0.0f);
            screen.getFocusHandler().resetFocusElements();
        } else if (event.getSelectedId().equals("constraint_0.5")) {
            base.getTransformManager().getConstraintTool().setConstraint(0.5f);
            screen.getFocusHandler().resetFocusElements();
        } else if (event.getSelectedId().equals("constraint_1")) {
            base.getTransformManager().getConstraintTool().setConstraint(1.0f);
            screen.getFocusHandler().resetFocusElements();
        } else if (event.getSelectedId().equals("constraint_5")) {
            base.getTransformManager().getConstraintTool().setConstraint(5.0f);
            screen.getFocusHandler().resetFocusElements();
        } else if (event.getSelectedId().equals("constraint_10")) {
            base.getTransformManager().getConstraintTool().setConstraint(10.0f);
            screen.getFocusHandler().resetFocusElements();
        }
    }

    /**
     * This is called when the RadioButton selection has changed.
     */
    @NiftyEventSubscriber(id = "RadioGroupSelection")
    public void RadioGroupSelectionChanged(final String id, final RadioButtonGroupStateChangedEvent event) {

        if (event.getSelectedId().equals("mouse_sel")) {
            setMouseSelection();
            screen.getFocusHandler().resetFocusElements();
        } else if (event.getSelectedId().equals("rectangle_sel")) {
            setRectangleSelection();
            screen.getFocusHandler().resetFocusElements();
        }
    }

    /**
     * This is called when the RadioButton selection has changed.
     */
    @NiftyEventSubscriber(id = "RadioGroupSelectionAdditive")
    public void RadioGroupSelectionAdditiveChanged(final String id, final RadioButtonGroupStateChangedEvent event) {

        if (event.getSelectedId().equals("normal_sel")) {
            setNormalSelection();
            screen.getFocusHandler().resetFocusElements();
        } else if (event.getSelectedId().equals("additive_sel")) {
            setAdditiveSelection();
            screen.getFocusHandler().resetFocusElements();
        }
    }

//    // for sceneObjectsListBox manipulation
//    @NiftyEventSubscriber(id = "sceneObjectsListBox")
//    public void onListBoxSelectionChanged(final String id, final ListBoxSelectionChangedEvent changed) {
//    }
    public void setMoveManipulator() {
        System.out.println("Manipulator is changed");
        base.getTransformManager().setTransformType(EditorTransformManager.TransformToolType.MoveTool);
        screen.getFocusHandler().resetFocusElements();
    }

    public void setRotateManipulator() {
        System.out.println("Manipulator is changed");
        base.getTransformManager().setTransformType(EditorTransformManager.TransformToolType.RotateTool);
        screen.getFocusHandler().resetFocusElements();
    }

    public void setScaleManipulator() {
        System.out.println("Manipulator is changed");
        base.getTransformManager().setTransformType(EditorTransformManager.TransformToolType.ScaleTool);
        screen.getFocusHandler().resetFocusElements();
    }

    public void clearTransform(String transformType) {
        for (Long id : base.getSelectionManager().getSelectionList()) {
            Node entity = (Node) base.getSpatialSystem().getSpatialControl(id).getGeneralNode();
            if (transformType.equals("Translation")) {
                entity.setLocalTranslation(new Vector3f());
            } else if (transformType.equals("Rotation")) {
                entity.setLocalRotation(new Quaternion());
            } else if (transformType.equals("Scale")) {
                entity.setLocalScale(new Vector3f(1, 1, 1));
            }

        }
        base.getSelectionManager().calculateSelectionCenter();

        // set history
        base.getHistoryManager().prepareNewHistory();
        base.getHistoryManager().setNewSelectionHistory(base.getSelectionManager().getSelectionList());
        base.getHistoryManager().getHistoryList().get(base.getHistoryManager().getHistoryCurrentNumber()).setDoTransform(true);
        screen.getFocusHandler().resetFocusElements();
    }

    public void snapObjectsToGrid() {
        for (Long id : base.getSelectionManager().getSelectionList()) {
            Node entity = (Node) base.getSpatialSystem().getSpatialControl(id).getGeneralNode();

            float constrX = base.getTransformManager().getConstraintTool().constraintValue(entity.getLocalTranslation().getX());
            float constrY = base.getTransformManager().getConstraintTool().constraintValue(entity.getLocalTranslation().getY());
            float constrZ = base.getTransformManager().getConstraintTool().constraintValue(entity.getLocalTranslation().getZ());

            entity.setLocalTranslation(constrX, constrY, constrZ);
            base.getSelectionManager().calculateSelectionCenter();

            // set history
            base.getHistoryManager().prepareNewHistory();
            base.getHistoryManager().setNewSelectionHistory(base.getSelectionManager().getSelectionList());
            base.getHistoryManager().getHistoryList().get(base.getHistoryManager().getHistoryCurrentNumber()).setDoTransform(true);
        }
        screen.getFocusHandler().resetFocusElements();
    }

    public void setGrid() {
        int indexGrid = rootNode.getChildIndex(gridNode);
        if (indexGrid == -1) {
            rootNode.attachChild(gridNode);
        } else {
            rootNode.detachChild(gridNode);
        }
        screen.getFocusHandler().resetFocusElements();
    }

    public void setMouseSelection() {
        base.getSelectionManager().setSelectionTool(EditorSelectionManager.SelectionToolType.MouseClick);
        screen.getFocusHandler().resetFocusElements();
    }

    public void setRectangleSelection() {
        base.getSelectionManager().setSelectionTool(EditorSelectionManager.SelectionToolType.Rectangle);
        screen.getFocusHandler().resetFocusElements();
    }

    public void setLocalCoords() {
        base.getTransformManager().setTrCoordinates(EditorTransformManager.TransformCoordinates.LocalCoords);
        screen.getFocusHandler().resetFocusElements();
    }

    public void setWorldCoords() {
        base.getTransformManager().setTrCoordinates(EditorTransformManager.TransformCoordinates.WorldCoords);
        screen.getFocusHandler().resetFocusElements();
    }

    public void setViewCoords() {
        base.getTransformManager().setTrCoordinates(EditorTransformManager.TransformCoordinates.ViewCoords);
        screen.getFocusHandler().resetFocusElements();
    }

    public void setAdditiveSelection() {
        base.getSelectionManager().setSelectionMode(EditorSelectionManager.SelectionMode.Additive);
        screen.getFocusHandler().resetFocusElements();
    }

    public void setNormalSelection() {
        base.getSelectionManager().setSelectionMode(EditorSelectionManager.SelectionMode.Normal);
        screen.getFocusHandler().resetFocusElements();
    }

    public Element getRightPanel() {
        return rightPanel;
    }

    public Screen getScreen() {
        return screen;
    }

    public void newSceneButton() {
        if (!base.getEventManager().isActive()) {
            base.getSceneManager().newScene();
            clearGui();
        }
        screen.getFocusHandler().resetFocusElements();
    }

    public void LoadSceneButton() {
        if (!base.getEventManager().isActive()) {
            boolean isLoaded = base.getSceneManager().loadScene();

            if (isLoaded == true) {
                clearGui();

                // reload assets lists
                int guiAssetLine = 1;
                for (String obj : base.getSceneManager().getAssetsList()) {
                    // show assets at the gui
                    if (guiAssetLine <= 7) {
                        String strAssetLine = "scenePath" + guiAssetLine;
                        nifty.getScreen("start").findNiftyControl(strAssetLine, TextField.class).setText((String) obj);
                        guiAssetLine += 1;
                    }

                }

                // update list of all entities
                ConcurrentHashMap<String, String> entList = base.getSceneManager().getEntitiesListsList();
                entitiesListBox.clear();
                for (String str : entList.keySet()) {
                    entitiesListBox.addItem(str);
                }


                // update list of objects and layers visibility
                for (Node ndLayer : base.getLayerManager().getLayers()) {
                    // set layers vibiity
                    CheckBox cbLayer = screen.findNiftyControl(ndLayer.getName(), CheckBox.class);
                    boolean isEnabled = (Boolean) ndLayer.getUserData("isEnabled");
                    boolean isActive = (Boolean) ndLayer.getUserData("isActive");
                    if (isEnabled) {
                        cbLayer.check();
                    }
                    if (isActive) {
                        Element newActive = screen.findElementByName(ndLayer.getName());
                        newActive.startEffect(EffectEventId.onFocus);
                    }
                    screen.getFocusHandler().resetFocusElements();

                    // update list of objects
                    for (Spatial spEntity : ndLayer.getChildren()) {
                        Object obj = spEntity.getUserData("EntityID");
                        long idObj = (Long) obj;
                        EntityNameComponent nameComp = (EntityNameComponent) base.getEntityManager().getComponent(idObj, EntityNameComponent.class);
                        sceneObjectsListBox.addItem(nameComp.getName());
                    }
                }

                // savePreviewj3o checkbox
                CheckBox cbPreview = screen.findNiftyControl("savePreviewJ3O", CheckBox.class);
                if (base.getSceneManager().getSavePreviewJ3o()) {
                    cbPreview.check();
                } else {
                    cbPreview.uncheck();
                }

                getEntitiesListBox().sortAllItems();
                getSceneObjectsListBox().sortAllItems();

            }
        }
        screen.getFocusHandler().resetFocusElements();
    }

    public void saveSceneButton() {
        if (!base.getEventManager().isActive()) {
            base.getEventManager().setAction(true);
            base.getSceneManager().saveScene();
            base.getEventManager().setAction(false);
        }
        screen.getFocusHandler().resetFocusElements();
    }

    public void saveAsNewSceneButton() {
        if (!base.getEventManager().isActive()) {
            base.getSceneManager().saveAsNewScene();
        }
        screen.getFocusHandler().resetFocusElements();
    }

    public ListBox getEntitiesListBox() {
        return entitiesListBox;
    }

    public ListBox getSceneObjectsListBox() {
        return sceneObjectsListBox;
    }

    public void updateAssetsButton() {
        if (!base.getEventManager().isActive()) {
            // update assets
            base.getSceneManager().clearAssets();

            for (int i = 0; i < 7; i++) {
                String strID = "scenePath" + (i + 1);
                String str = nifty.getScreen("start").findNiftyControl(strID, TextField.class).getDisplayedText();

                if (str != null && str.length() > 0) {
                    base.getSceneManager().addAsset(str);
                }
            }

            // update list of all entities
            ConcurrentHashMap<String, String> entList = base.getSceneManager().getEntitiesListsList();
            entitiesListBox.clear();
            for (String str : entList.keySet()) {
                entitiesListBox.addItem(str);
            }
            entitiesListBox.sortAllItems();
        }
        screen.getFocusHandler().resetFocusElements();
    }

    public void addEntityToSceneButton() {
        // create entity
        if (entitiesListBox.getSelection().size() > 0 && base.getLayerManager().getActiveLayer() != null
                && !base.getEventManager().isActive()) {
            String selectedEntity = entitiesListBox.getSelection().get(0).toString();
            long id = base.getSceneManager().createEntityModel(selectedEntity, base.getSceneManager().getEntitiesListsList().get(selectedEntity), null);
            Spatial entitySp = base.getSpatialSystem().getSpatialControl(id).getGeneralNode();
            base.getLayerManager().getActiveLayer().attachChild(entitySp);
            EditorTransformConstraint constraintTool = base.getTransformManager().getConstraintTool();

            if (constraintTool.getConstraint() > 0) {
                float x = constraintTool.constraintValue(entitySp.getLocalTranslation().getX());
                float y = constraintTool.constraintValue(entitySp.getLocalTranslation().getY());
                float z = constraintTool.constraintValue(entitySp.getLocalTranslation().getZ());
                entitySp.setLocalTranslation(new Vector3f(x, y, z));
            }


            // clear selection
            base.getSelectionManager().clearSelectionList();
            base.getSelectionManager().selectEntity(id, base.getSelectionManager().getSelectionMode());
            base.getSelectionManager().calculateSelectionCenter();

            // add entty to sceneList
            EntityNameComponent nameComp = (EntityNameComponent) base.getEntityManager().getComponent(id, EntityNameComponent.class);
            sceneObjectsListBox.addItem(nameComp.getName());
            sceneObjectsListBox.sortAllItems();

            // set history
            base.getHistoryManager().prepareNewHistory();
            base.getHistoryManager().setNewSelectionHistory(base.getSelectionManager().getSelectionList());
        }
        screen.getFocusHandler().resetFocusElements();
    }

    // This is just visual representation of selected objects
    protected void setSelectedObjectsList() {

        List<Long> selList = base.getSelectionManager().getSelectionList();

        for (Object indexDeselect : sceneObjectsListBox.getSelection()) {
            sceneObjectsListBox.deselectItem(indexDeselect);
        }

        for (Long id : selList) {
            EntityNameComponent nameComp = (EntityNameComponent) base.getEntityManager().getComponent(id, EntityNameComponent.class);
            String objectString = nameComp.getName();
            sceneObjectsListBox.selectItem(objectString);
        }
        screen.getFocusHandler().resetFocusElements();
    }

    public void removeClonesButton() {
        if (entitiesListBox.getSelection().size() > 0 && !base.getEventManager().isActive()) {
            base.getSceneManager().removeClones(entitiesListBox.getSelection().get(0).toString());
            base.getGuiManager().getSceneObjectsListBox().sortAllItems();
        }
        screen.getFocusHandler().resetFocusElements();
    }

    // select entities from the list of seceneObjectsList
    public void selectEntitiesButton() {
//        List<Long> lectlList = base.getSelectionManager().getSelectionList();

        if (sceneObjectsListBox.getSelection().size() > 0) {

            base.getSelectionManager().clearSelectionList();
            for (Object obj : sceneObjectsListBox.getSelection()) {
                String objStr = (String) obj;
                long id = Long.valueOf(objStr.substring(objStr.indexOf("_IDX") + 4, objStr.length()));
                Node entNode = (Node) base.getSpatialSystem().getSpatialControl(id).getGeneralNode();
                System.out.println(objStr.substring(objStr.indexOf("_IDX") + 4, objStr.length()));

                // check if entity is in selected layer
                Node possibleLayer = (Node) entNode.getParent();
                if (possibleLayer != null) {
                    Object isEnabledObj = possibleLayer.getUserData("isEnabled");
                    if (isEnabledObj != null) {
                        boolean isEnabled = (Boolean) isEnabledObj;
                        if (isEnabled == true) {
                            base.getSelectionManager().selectEntity(id, EditorSelectionManager.SelectionMode.Additive);
                        }
                    }
                }
            }
            base.getSelectionManager().calculateSelectionCenter();
            setSelectedObjectsList();

            // set history
            base.getHistoryManager().prepareNewHistory();
            base.getHistoryManager().setNewSelectionHistory(base.getSelectionManager().getSelectionList());
        }
        screen.getFocusHandler().resetFocusElements();
    }

    public void showSelectedEntitiesButton() {
        setSelectedObjectsList();
        screen.getFocusHandler().resetFocusElements();
    }

    public void clearSelectedEntitiesButton() {
        for (Object indexDeselect : sceneObjectsListBox.getSelection()) {
            sceneObjectsListBox.deselectItem(indexDeselect);
        }
        screen.getFocusHandler().resetFocusElements();
    }

    public void removeSelectedButton() {
        for (long id : base.getSelectionManager().getSelectionList()) {
            EntityNameComponent nameComponent = (EntityNameComponent) base.getEntityManager().getComponent(id, EntityNameComponent.class);
            sceneObjectsListBox.removeItem(nameComponent.getName());

            base.getSceneManager().removeEntityObject(id);
        }

        base.getSelectionManager().getSelectionList().clear();
        base.getSelectionManager().calculateSelectionCenter();
        screen.getFocusHandler().resetFocusElements();
    }

    public void selectAllButton() {
        if (!base.getEventManager().isActive()) {

            if (base.getSelectionManager().getSelectionList().size() > 0) {
                base.getSelectionManager().clearSelectionList();
            } else {
                for (Spatial spLayer : base.getLayerManager().getSelectableNode().getChildren()) {
                    Node layerNode = (Node) spLayer;
                    for (Spatial spEntity : layerNode.getChildren()) {
                        Node entityNode = (Node) spEntity;
                        long ID = (Long) entityNode.getUserData("EntityID");
                        base.getSelectionManager().selectEntity(ID, EditorSelectionManager.SelectionMode.Additive);
                    }
                }
            }

            base.getSelectionManager().calculateSelectionCenter();

            // set history
            base.getHistoryManager().prepareNewHistory();
            base.getHistoryManager().setNewSelectionHistory(base.getSelectionManager().getSelectionList());

        }
        screen.getFocusHandler().resetFocusElements();
    }

    public void cloneSelectedButton() {
        if (base.getSelectionManager().getSelectionList().size() > 0) {
            List<Long> list = base.getSceneManager().cloneSelectedEntities();
            for (Long id : list) {
                EntityNameComponent newRealName = (EntityNameComponent) base.getEntityManager().getComponent(id, EntityNameComponent.class);
                base.getGuiManager().getSceneObjectsListBox().addItem(newRealName.getName());
            }
        }
        screen.getFocusHandler().resetFocusElements();
    }

    public void addComponentButton() {
        // if entity is selected
        if (base.getSelectionManager().getSelectionList().contains(lastIdOfComponentList)) {
            idComponentToChange = lastIdOfComponentList; // set emp id to change

            popupEditComponent.enable();
            nifty.showPopup(nifty.getCurrentScreen(), popupEditComponent.getId(), null);

            popupEditComponent.findNiftyControl("entityDataName", TextField.class).setText("");
            popupEditComponent.findNiftyControl("entityData", TextField.class).setText("");

            popupEditComponent.getFocusHandler().resetFocusElements();
            base.getEditorMappings().removeListener();

        }

    }

    public void removeSelectedComponentButton() {
        if (componentsListBox.getSelection().size() > 0
                && base.getSelectionManager().getSelectionList().contains(lastIdOfComponentList)) {
            String strName = (String) componentsListBox.getSelection().get(0);
            base.getDataManager().getEntityData(lastIdOfComponentList).remove(strName);

            componentsListBox.removeItem(strName);
        }
        screen.getFocusHandler().resetFocusElements();

    }

    public void editComponent() {

        if (componentsListBox.getSelection().size() > 0) {
            // textFields
            String dataComponentName = (String) componentsListBox.getSelection().get(0);

            // if entity is selected
            if (base.getSelectionManager().getSelectionList().contains(lastIdOfComponentList)) {
                idComponentToChange = lastIdOfComponentList; // set emp id to change

                popupEditComponent.enable();
                nifty.showPopup(nifty.getCurrentScreen(), popupEditComponent.getId(), null);

                ConcurrentHashMap<String, String> data = base.getDataManager().getEntityData(idComponentToChange);
                popupEditComponent.findNiftyControl("entityDataName", TextField.class).setText(dataComponentName);
                popupEditComponent.findNiftyControl("entityData", TextField.class).setText(data.get(dataComponentName));

                popupEditComponent.getFocusHandler().resetFocusElements();
                base.getEditorMappings().removeListener();
            }
        }
        screen.getFocusHandler().resetFocusElements();
    }

    public void copyComponentToSelectedEntityButton() {
        if (componentsListBox.getSelection().size() > 0
                && base.getSelectionManager().getSelectionList().contains(lastIdOfComponentList)) {
            String strDataName = (String) componentsListBox.getSelection().get(0);
            String strData = base.getDataManager().getEntityData(lastIdOfComponentList).get(strDataName);
            List<Long> list = base.getSelectionManager().getSelectionList();
            for (long id : list) {
                if (id != lastIdOfComponentList) {
                    base.getDataManager().getEntityData(id).put(strDataName, strData);
                }
            }
        }
        screen.getFocusHandler().resetFocusElements();
    }

    public void removeComponentFromSelectedEntityButton() {
        if (componentsListBox.getSelection().size() > 0
                && base.getSelectionManager().getSelectionList().contains(lastIdOfComponentList)) {
            String strDataName = (String) componentsListBox.getSelection().get(0);
            List<Long> list = base.getSelectionManager().getSelectionList();
            for (long id : list) {
                if (id != lastIdOfComponentList
                        && base.getDataManager().getEntityData(id).containsKey(strDataName) == true) {
                    base.getDataManager().getEntityData(id).remove(strDataName);
                }
            }
        }
        screen.getFocusHandler().resetFocusElements();
    }

    public void finishEditComponent(String bool) {
        boolean boo = Boolean.valueOf(bool);
        // if entity is selected

        if (boo) {
            ConcurrentHashMap<String, String> data = base.getDataManager().getEntityData(idComponentToChange);

            String newDataName = popupEditComponent.findNiftyControl("entityDataName", TextField.class).getDisplayedText();
            String newData = popupEditComponent.findNiftyControl("entityData", TextField.class).getDisplayedText();
            data.put(newDataName, newData);

            if (base.getSelectionManager().getSelectionList().size() > 0
                    && base.getSelectionManager().getSelectionList().get(base.getSelectionManager().getSelectionList().size() - 1) == idComponentToChange) {
                if (componentsListBox.getItems().contains(newDataName) == false) {
                    componentsListBox.addItem(newDataName);
                }
            }
        }

        nifty.closePopup(popupEditComponent.getId());
        popupEditComponent.disable();
        popupEditComponent.getFocusHandler().resetFocusElements();
        base.getEditorMappings().addListener();
    }

    public void savePreviewJ3O() {
        CheckBox cb = screen.findNiftyControl("savePreviewJ3O", CheckBox.class);
        boolean savePreview = base.getSceneManager().getSavePreviewJ3o();
        if (savePreview) {
            base.getSceneManager().setSavePreviewJ3o(false);
            cb.uncheck();
        } else {
            base.getSceneManager().setSavePreviewJ3o(true);
            cb.check();
        }
        screen.getFocusHandler().resetFocusElements();
    }

    // not implemented as Lights are not implemented
//    public void switchDefaultLighting() {
//        CheckBox cb = screen.findNiftyControl("switchDefaultLighting", CheckBox.class);
//        if(base.getSceneManager().getTempLighting()) {
//            base.getSceneManager().setTempLighting(false);
//            cb.uncheck();
//        } else {
//            base.getSceneManager().setTempLighting(true);
//            cb.check();
//        }
//    }
    public void switchLayer(String srtinG) {
        if (!base.getEventManager().isActive()) {
            CheckBox cb = screen.findNiftyControl("layer" + srtinG, CheckBox.class);

            int iInt = Integer.valueOf(srtinG);
            Node activeLayer = base.getLayerManager().getActiveLayer(); // active layer
            Node layerToSwitch = base.getLayerManager().getLayer(iInt); // layer to switch on/off
            Node selectableNode = (Node) rootNode.getChild("selectableNode");

            Object isEnabledObj = layerToSwitch.getUserData("isEnabled");
            boolean isEnabled = (Boolean) isEnabledObj;

            // Switching off
            if (isEnabled == true) {
                //set checkbox effect off
                cb.uncheck();

                // detach layer
                selectableNode.detachChild(layerToSwitch);
                layerToSwitch.setUserData("isEnabled", false);

                // remove layer from selection
                List<Long> selectionList = base.getSelectionManager().getSelectionList();
                for (Spatial sp : layerToSwitch.getChildren()) {
                    Object idObj = sp.getUserData("EntityID");
                    long id = (Long) idObj;
                    if (selectionList.indexOf(id) > -1) {
                        Node removeSelBox = (Node) base.getSpatialSystem().getSpatialControl(id).getGeneralNode();
                        base.getSelectionManager().removeSelectionBox(removeSelBox);
                        selectionList.remove(id);
                    }
                }
                base.getSelectionManager().calculateSelectionCenter();

                // if selected layer is active
                if (activeLayer.equals(layerToSwitch)) {
                    // deactivate active and slected layer
                    layerToSwitch.setUserData("isActive", false);
                    screen.findElementByName(layerToSwitch.getName()).stopEffect(EffectEventId.onFocus);
                    screen.getFocusHandler().resetFocusElements();

                    // set new active layer
                    if (selectableNode.getChildren().size() > 0) {
                        Node nd = (Node) selectableNode.getChild(selectableNode.getChildren().size() - 1);
                        nd.setUserData("isActive", true);
                        base.getLayerManager().setActiveLayer(nd);
                        Element newActive = screen.findElementByName(nd.getName());
                        newActive.startEffect(EffectEventId.onFocus);
                        screen.getFocusHandler().resetFocusElements();
                    } else {
                        base.getLayerManager().setActiveLayer(null);
                    }
                }
            } // switching on
            else {
                //set checkbox effect on
                cb.check();

                if (activeLayer != null) {

                    Element selectActiveLayerImage = screen.findElementByName(activeLayer.getName());
                    selectActiveLayerImage.stopEffect(EffectEventId.onFocus);
                    selectActiveLayerImage.startEffect(EffectEventId.onEnabled);
                    screen.getFocusHandler().resetFocusElements();
                    activeLayer.setUserData("isActive", false);
                }


                // SET THE LAYER ACTIVE (Red color)
//            CheckBox cb = screen.findNiftyControl("layer" + (iInt), CheckBox.class);
                Element selectImage = screen.findElementByName(layerToSwitch.getName());
                selectImage.startEffect(EffectEventId.onFocus);
                base.getLayerManager().setActiveLayer(layerToSwitch);

                selectableNode.attachChild(layerToSwitch);
                layerToSwitch.setUserData("isActive", true);
                layerToSwitch.setUserData("isEnabled", true);
            }
        }
        screen.getFocusHandler().resetFocusElements();
    }

    public void moveToLayerEnable(String bool) {
        boolean boolValue = Boolean.valueOf(bool);
        if (boolValue) {
            screen.getFocusHandler().resetFocusElements();
            popupMoveToLayer.enable();
            nifty.showPopup(nifty.getCurrentScreen(), popupMoveToLayer.getId(), null);
            popupMoveToLayer.getFocusHandler().resetFocusElements();
            base.getEditorMappings().removeListener();
        } else {
            nifty.closePopup(popupMoveToLayer.getId());
            popupMoveToLayer.disable();
            popupMoveToLayer.getFocusHandler().resetFocusElements();
            base.getEditorMappings().addListener();
        }

    }

    public void moveToLayer(String srtinG) {
        // move to layer
        int iInt = Integer.valueOf(srtinG);
        List<Long> lst = base.getSelectionManager().getSelectionList();
        for (Long lng : lst) {
            Node moveNode = (Node) base.getSpatialSystem().getSpatialControl(lng).getGeneralNode();
            base.getLayerManager().addToLayer(moveNode, iInt);
        }

        // clear selection if layer is inactive
        Object boolObj = base.getLayerManager().getLayer(iInt).getUserData("isEnabled");
        boolean bool = (Boolean) boolObj;
        if (bool == false) {
            // remove selection boxes
//            for (Long idToRemove : lst) {
//                base.getSelectionManager().removeSelectionBox((Node) base.getSpatialSystem().getSpatialControl(idToRemove).getGeneralNode());
//            }
            base.getSelectionManager().clearSelectionList();
            base.getSelectionManager().calculateSelectionCenter();
        }

        nifty.closePopup(popupMoveToLayer.getId());
        popupMoveToLayer.disable();
        screen.getFocusHandler().resetFocusElements();
        base.getEditorMappings().addListener();

    }

    private void createGrid() {
        gridNode = new Node("gridNode");

        //Create a grid plane
        Geometry g = new Geometry("GRID", new Grid(201, 201, 10f));
        Material floor_mat = new Material(assetManager, "Common/MatDefs/Misc/Unshaded.j3md");
        floor_mat.getAdditionalRenderState().setWireframe(true);
        floor_mat.setColor("Color", new ColorRGBA(0.4f, 0.4f, 0.4f, 0.15f));
        floor_mat.getAdditionalRenderState().setBlendMode(RenderState.BlendMode.Alpha);
        g.setCullHint(Spatial.CullHint.Never);
        g.setShadowMode(RenderQueue.ShadowMode.Off);
        g.setQueueBucket(RenderQueue.Bucket.Transparent);
        g.setMaterial(floor_mat);
        g.center().move(new Vector3f(0f, 0f, 0f));
        gridNode.attachChild(g);

        // Red line for X axis
        final Line xAxis = new Line(new Vector3f(-1000f, 0f, 0f), new Vector3f(1000f, 0f, 0f));
        xAxis.setLineWidth(2f);
        Geometry gxAxis = new Geometry("XAxis", xAxis);
        gxAxis.setModelBound(new BoundingBox());
        Material mat = new Material(assetManager, "Common/MatDefs/Misc/Unshaded.j3md");
        mat.setColor("Color", new ColorRGBA(1.0f, 0.2f, 0.5f, 0.2f));
        mat.getAdditionalRenderState().setBlendMode(RenderState.BlendMode.Alpha);
        gxAxis.setCullHint(Spatial.CullHint.Never);
        gxAxis.setQueueBucket(RenderQueue.Bucket.Transparent);
        gxAxis.setShadowMode(RenderQueue.ShadowMode.Off);
        gxAxis.setMaterial(mat);
        gxAxis.setCullHint(Spatial.CullHint.Never);

        gridNode.attachChild(gxAxis);

        // Blue line for Z axis
        final Line zAxis = new Line(new Vector3f(0f, 0f, -1000f), new Vector3f(0f, 0f, 1000f));
        zAxis.setLineWidth(2f);
        Geometry gzAxis = new Geometry("ZAxis", zAxis);
        gzAxis.setModelBound(new BoundingBox());
        mat = new Material(assetManager, "Common/MatDefs/Misc/Unshaded.j3md");
        mat.setColor("Color", new ColorRGBA(0.2f, 1.0f, 0.2f, 0.2f));
        mat.getAdditionalRenderState().setBlendMode(RenderState.BlendMode.Alpha);
        gzAxis.setCullHint(Spatial.CullHint.Never);
        gzAxis.setQueueBucket(RenderQueue.Bucket.Transparent);
        gzAxis.setShadowMode(RenderQueue.ShadowMode.Off);
        gzAxis.setMaterial(mat);
        gzAxis.setCullHint(Spatial.CullHint.Never);
        gridNode.attachChild(gzAxis);

        rootNode.attachChild(gridNode);

    }

    public Node getGridNode() {
        return gridNode;
    }

//    private void createSimpleGui() {
//
//        BitmapFont guiFont = assetManager.loadFont("Interface/Fonts/Default.fnt");
//        BitmapText ch = new BitmapText(guiFont, false);
//        ch.setSize(guiFont.getCharSet().getRenderedSize());
//        ch.setText("W,A,S,D,Q,Z, MiddleMouseButton, RightMouseButton, Scroll"); // crosshairs
//        ch.setColor(new ColorRGBA(1f, 0.8f, 0.1f, 0.3f));
//        ch.setLocalTranslation(application.getCamera().getWidth() * 0.1f, application.getCamera().getHeight() * 0.1f, 0);
//        guiNode.attachChild(ch);
//
//    }
    @Override
    public void update(float tpf) {
        // This is for componentsList!
        List<Long> selList = base.getSelectionManager().getSelectionList();
        if (selList.size() == 0) {
            componentsListBox.clear();
            lastIdOfComponentList = -1; // just for the case if user will select the same entity
        } else if (selList.get(selList.size() - 1) != lastIdOfComponentList) {
            componentsListBox.clear();
            lastIdOfComponentList = selList.get(selList.size() - 1);
            ConcurrentHashMap<String, String> data = base.getDataManager().getEntityData(lastIdOfComponentList);
            for (String key : data.keySet()) {
                componentsListBox.addItem(key);
            }
        }
    }

    @Override
    public void cleanup() {
        super.cleanup();
        //TODO: clean up what you initialized in the initialize method,
        //e.g. remove all spatials from rootNode
        //this is called on the OpenGL thread after the AppState has been detached
    }

    public void bind(Nifty nifty, Screen screen) {
        this.nifty = nifty;
        this.screen = screen;
    }

    public void onStartScreen() {
    }

    public void onEndScreen() {
    }
}
