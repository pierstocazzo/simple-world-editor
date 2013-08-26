/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.swe.scene;

import com.swe.entitysystem.ComponentsControl;
import com.swe.entitysystem.EntityModelPathComponent;
import com.swe.entitysystem.EntityNameComponent;
import com.swe.entitysystem.EntitySpatialsControl;
import com.jme3.app.Application;
import com.jme3.asset.AssetManager;
import com.jme3.asset.DesktopAssetManager;
import com.jme3.asset.ModelKey;
import com.jme3.asset.plugins.FileLocator;
import com.jme3.export.binary.BinaryExporter;
import com.jme3.light.AmbientLight;
import com.jme3.light.DirectionalLight;
import com.jme3.math.ColorRGBA;
import com.jme3.math.Quaternion;
import com.jme3.math.Transform;
import com.jme3.math.Vector3f;
import com.jme3.scene.AssetLinkNode;
import com.jme3.scene.Node;
import com.jme3.scene.Spatial;
import com.swe.EditorBaseManager;
import java.awt.Dimension;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ConcurrentHashMap;
import javax.swing.JFileChooser;
import javax.swing.filechooser.FileFilter;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;

/**
 *
 * @author mifth
 */
public class EditorSceneManager {

    private AssetManager assetMan;
    private Node root, guiNode;
    private Application app;
    private EditorBaseManager base;
    private final JFileChooser mFileCm;
    private FileFilter modFilter;
    private static List<String> assetsList;
    private static ConcurrentHashMap<String, EditorSceneObject> scenesList;
    private EditorSceneObject activeScene;
    private static ConcurrentHashMap<String, String> entitiesList;
    private static ConcurrentHashMap<String, Spatial> spatialsList;
    private DesktopAssetManager dsk;
    private String scenePathCache, sceneNameCache;
    private boolean savePreviewJ3o;
    private DirectionalLight dl;
    private AmbientLight al;

    public EditorSceneManager(Application app, EditorBaseManager base) {

        this.app = app;
        this.base = base;
        assetMan = this.app.getAssetManager();
        root = (Node) this.app.getViewPort().getScenes().get(0);
        guiNode = (Node) this.app.getGuiViewPort().getScenes().get(0);

        mFileCm = new JFileChooser();
        mFileCm.addChoosableFileFilter(modFilter);
        mFileCm.setAcceptAllFileFilterUsed(false);
        mFileCm.setPreferredSize(new Dimension(800, 600));
        modFilter = new EditorSceneFilter();

        scenePathCache = null;
        sceneNameCache = null;


        assetsList = new ArrayList<String>();
        scenesList = new ConcurrentHashMap<String, EditorSceneObject>();
        entitiesList = new ConcurrentHashMap<String, String>();
        spatialsList = new ConcurrentHashMap<String, Spatial>();
        dsk = (DesktopAssetManager) assetMan;

        initializeTempLighting();

        app.getViewPort().setBackgroundColor(ColorRGBA.DarkGray);

        savePreviewJ3o = false;

        // create new scene
        createSceneObject("Scene1");
        scenesList.get("Scene1").createLayersGroup("LayersGroup1");
        createSceneObject("Scene2");
        scenesList.get("Scene2").createLayersGroup("LayersGroup1");
        scenesList.get("Scene2").createLayersGroup("LayersGroup2");

    }

    public void createSceneObject(String sceneName) {
        EditorSceneObject newScene = new EditorSceneObject(root, sceneName);
        newScene.setSceneEnabled(true);
        setActiveScene(newScene);
        scenesList.put(sceneName, newScene);
    }

    public boolean loadScene() {
        mFileCm.setDialogType(JFileChooser.OPEN_DIALOG);
        mFileCm.setDialogTitle("Load Scene");
        mFileCm.setApproveButtonToolTipText("Open");
        mFileCm.setApproveButtonText("Open");
        mFileCm.setFileFilter(modFilter);
        int returnVal = mFileCm.showOpenDialog(null);

        if (returnVal == JFileChooser.APPROVE_OPTION) {
            File selectedFile = mFileCm.getSelectedFile();

            if (selectedFile.exists() && selectedFile.isFile() && selectedFile.getName().indexOf(".") != 0 && selectedFile.getName().length() > 0) {
                String filePath = selectedFile.getParent();
                filePath = correctPath(filePath);

                String fileName = selectedFile.getName();
                if (fileName.indexOf(".") > 0) {
                    fileName = fileName.substring(0, fileName.indexOf("."));
                }
                String fullPath = filePath + fileName;

                newScene();

                // set scene paths to cache
                sceneNameCache = fileName;
                scenePathCache = filePath;

                loadSweFile(fullPath);

                return true;
            }
        }
        return false;
    }

    protected void loadSettings(String filePath, JSONObject jsScene) {

        // set new IDX
        long lastIDX = Long.valueOf((String) jsScene.get("LastIDX"));
        base.getEntityManager().setIdx(lastIDX);

        // version of the editor which the scene was saved
        // Not Used Yet
        //String SWEVersion = (String) jsScene.get("EditorVersion");

        // SevePreviewj3O
        savePreviewJ3o = (Boolean) jsScene.get("savePreviewJ3o");

        // load assets
        JSONObject jsPaths = (JSONObject) jsScene.get("AssetsPaths");
        for (Object obj : jsPaths.keySet()) {
            System.out.println("Loaded Path: " + (String) jsPaths.get(obj));
            addAsset((String) jsPaths.get(obj));
        }
        System.out.println("Settings are loaded!");
    }

    protected void loadSweFile(String filePath) {
        JSONObject loadSWEJson = loadToJsonFile(filePath + ".swe");

        loadSettings(filePath, loadSWEJson);

        // load Scenes
        JSONObject jsScenes = (JSONObject) loadSWEJson.get("Scenes");
        for (Object sceneStrObj : jsScenes.keySet()) {
            JSONObject jsScene = (JSONObject) jsScenes.get(sceneStrObj);

            // set new Scene
            String strScene = (String) sceneStrObj;
            EditorSceneObject newSceneObj = new EditorSceneObject(root, strScene);
            newSceneObj.setSceneEnabled((Boolean) jsScene.get("isEnabled"));

            // set Scene active
            Node newSceneNode = newSceneObj.getSceneNode();
            newSceneNode.setUserData("isActive", (Boolean) jsScene.get("isActive"));
            if ((Boolean) jsScene.get("isActive")) {
                setActiveScene(newSceneObj);
            }

            scenesList.put(strScene, newSceneObj); // add a scene to the list

            // Load LayersGroups of a Scene
            JSONObject jsAllLayersGroups = (JSONObject) jsScene.get("LayersGroups");
            for (Object layersGroupStrObj : jsAllLayersGroups.keySet()) {
                JSONObject jsLayersGroup = (JSONObject) jsAllLayersGroups.get(layersGroupStrObj);

                newSceneObj.createLayersGroup((String) layersGroupStrObj);
                EditorLayersGroupObject newLayersGroup = newSceneObj.getLayerGroupsList().get((String) layersGroupStrObj);
                newLayersGroup.setLayersGroupEnabled((Boolean) jsLayersGroup.get("isEnabled"));

                // set LayersGroup active
                Node newLayersGroupNode = newLayersGroup.getLayersGroupNode();
                if ((Boolean) jsLayersGroup.get("isActive")) {
                    newSceneObj.setActivelayersGroup(newLayersGroup);
                }

                // load layers
                JSONObject jsLayers = (JSONObject) jsLayersGroup.get("Layers");
                for (Object layerStrObj : jsLayers.keySet()) {
                    JSONObject jslayer = (JSONObject) jsLayers.get(layerStrObj);
                    // get layer
                    String strLayer = (String) layerStrObj;
                    strLayer = strLayer.substring(strLayer.indexOf("layer") + 5, strLayer.length());
                    Node layerNode = newLayersGroup.getLayer(Integer.valueOf(strLayer));
                    System.out.println(strLayer + "Layer number");

                    // get layer states
                    boolean isActive = (Boolean) jslayer.get("isActive");
                    if (isActive) {
                        newLayersGroup.setActiveLayer(layerNode);
//                    layerNode.setUserData("isActive", true);
                    }

                    // don't forget to parse gui layers
                    boolean isEnabled = (Boolean) jslayer.get("isEnabled");
                    if (isEnabled) {
                        newLayersGroup.getLayersGroupNode().attachChild(layerNode);
                        layerNode.setUserData("isEnabled", true);
                    }

                    // Locked Layer stats
                    boolean isLocked = (Boolean) jslayer.get("isLocked");
                    if (isLocked) {
                        layerNode.setUserData("isLocked", true);
                    }

                    // create entities
                    JSONObject jsEntities = (JSONObject) jslayer.get("Entities");
                    for (Object objID : jsEntities.keySet()) {
                        System.out.println(objID + "OBJID");
                        long ID = Long.valueOf((String) objID);
                        JSONObject jsEntity = (JSONObject) jsEntities.get(objID);

                        String idName = (String) jsEntity.get("IDName");
                        idName = idName.substring(0, idName.indexOf("_IDX"));

                        System.out.println(idName);
                        // create entity
                        long entID = createEntityModel(idName, entitiesList.get(idName), ID);
                        Node entityNode = (Node) base.getSpatialSystem().getSpatialControl(entID).getGeneralNode();
                        newLayersGroup.getLayer(Integer.valueOf(strLayer)).attachChild(entityNode);

                        //set Transform for the entity
                        JSONObject jsTransform = (JSONObject) jsEntity.get("IDTransform");
                        Transform entTransform = new Transform();
                        entTransform.setTranslation(Float.valueOf((String) jsTransform.get("translationX")), Float.valueOf((String) jsTransform.get("translationY")),
                                Float.valueOf((String) jsTransform.get("translationZ")));
                        entTransform.setRotation(new Quaternion(
                                Float.valueOf((String) jsTransform.get("rotationX")), Float.valueOf((String) jsTransform.get("rotationY")), Float.valueOf((String) jsTransform.get("rotationZ")),
                                Float.valueOf((String) jsTransform.get("rotationW"))));
                        entTransform.setScale(Float.valueOf((String) jsTransform.get("scaleX")), Float.valueOf((String) jsTransform.get("scaleY")), Float.valueOf((String) jsTransform.get("scaleZ")));
                        entityNode.setLocalTransform(entTransform);

                        System.out.println(entTransform.toString());

                        layerNode.attachChild(entityNode);

                        //set data components for the entity
                        JSONObject jsDataComponents = (JSONObject) jsEntity.get("IDDataComponents");
                        for (Object strKey : jsDataComponents.keySet()) {
                            String value = (String) jsDataComponents.get(strKey);
                            base.getDataManager().getEntityData(ID).put((String) strKey, value);
                        }
                    }

                }
            }
        }
    }

    public void saveScene() {
        if (scenePathCache != null && sceneNameCache != null) {
            saveSweFile(scenePathCache + sceneNameCache);

            if (savePreviewJ3o) {
                savePreviewScene(scenePathCache, sceneNameCache);
            }
        }

    }

    public void saveAsNewScene() {
        mFileCm.setDialogType(JFileChooser.SAVE_DIALOG);
        mFileCm.setDialogTitle("Save Scene");
        mFileCm.setApproveButtonToolTipText("Save");
        String s = "Save";
        mFileCm.setApproveButtonText("Save");
        mFileCm.setFileFilter(modFilter);
        int returnVal = mFileCm.showOpenDialog(null);

        if (returnVal == JFileChooser.APPROVE_OPTION) {
            File selectedFile = mFileCm.getSelectedFile();

            if (selectedFile.getName().indexOf(".") != 0 && selectedFile.getName().length() > 0) {
                String filePath = selectedFile.getParent();
                filePath = correctPath(filePath);

                String fileName = selectedFile.getName();
                if (fileName.indexOf(".") > 0) {
                    fileName = fileName.substring(0, fileName.indexOf("."));
                }
                String fullPath = filePath + fileName;

                // set paths to cache
                scenePathCache = filePath;
                sceneNameCache = fileName;

                saveSweFile(fullPath);

                if (savePreviewJ3o) {
                    savePreviewScene(filePath, fileName); // save j3o
                }
            }
        }
    }

    private void saveSettings(String pathToSave, JSONObject saveSceneJson) {

        // save Last IDX
        saveSceneJson.put("LastIDX", String.valueOf(base.getEntityManager().getIdx()));

        // save assets paths
        JSONObject assetsToSave = new JSONObject();
        int assetIndex = 1;
        for (String str : assetsList) {

            assetsToSave.put("AssetPath_" + assetIndex, str);
        }
        saveSceneJson.put("AssetsPaths", assetsToSave);

        // save version of the Simple World Editor
        saveSceneJson.put("EditorVersion", base.getEditorVersion());
        saveSceneJson.put("savePreviewJ3o", savePreviewJ3o);

    }

    private void saveSweFile(String pathToSave) {
        JSONObject saveSWEJson = new JSONObject();

        saveSettings(pathToSave, saveSWEJson);

        //save scenes
        JSONObject allScenes = new JSONObject();

        for (String sceneName : scenesList.keySet()) {
            JSONObject sceneToSave = new JSONObject();

            // save scene states
            Node sceneNode = scenesList.get(sceneName).getSceneNode();
            Object isActScene = sceneNode.getUserData("isActive");
            sceneToSave.put("isActive", (Boolean) isActScene);
            Object isEnScene = sceneNode.getUserData("isEnabled");
            sceneToSave.put("isEnabled", (Boolean) isEnScene);


            //save LayerGroups
            JSONObject allLayersGroups = new JSONObject();
            for (EditorLayersGroupObject layersGroup : scenesList.get(sceneName).getLayerGroupsList().values()) {
                JSONObject layersGroupToSave = new JSONObject();

                // save layerGroup states
                Node layersGroupNode = layersGroup.getLayersGroupNode();
                Object isActLayersGroup = layersGroupNode.getUserData("isActive");
                layersGroupToSave.put("isActive", (Boolean) isActLayersGroup);
                Object isEnLayersGroup = layersGroupNode.getUserData("isEnabled");
                layersGroupToSave.put("isEnabled", (Boolean) isEnLayersGroup);

                //save layers
                JSONObject allLayers = new JSONObject();
                for (Node layerNode : layersGroup.getLayers()) {
                    JSONObject layerToSave = new JSONObject();

                    // save layer states
                    Object isActLayer = layerNode.getUserData("isActive");
                    layerToSave.put("isActive", (Boolean) isActLayer);
                    Object isEnLayer = layerNode.getUserData("isEnabled");
                    layerToSave.put("isEnabled", (Boolean) isEnLayer);
                    Object isLockedLayer = layerNode.getUserData("isLocked");
                    layerToSave.put("isLocked", (Boolean) isLockedLayer);

                    // save ID entities
                    JSONObject entitiesToSave = new JSONObject();
                    for (Spatial sp : layerNode.getChildren()) {
                        JSONObject entityJSON = new JSONObject();

                        Object idObj = sp.getUserData("EntityID");
                        long idLong = (Long) idObj;

                        // save name
                        EntityNameComponent nameComp = (EntityNameComponent) base.getEntityManager().getComponent(idLong, EntityNameComponent.class);
                        entityJSON.put("IDName", nameComp.getName());

//                        EntityModelPathComponent pathComp = (EntityModelPathComponent) base.getEntityManager().getComponent(idLong, EntityModelPathComponent.class);
                        entityJSON.put("IDModel", nameComp.getName().substring(0, nameComp.getName().indexOf("_IDX")));

                        // save transforms
                        Transform trID = sp.getWorldTransform();
//                trID.getRotation().inverseLocal();
                        JSONObject transformToSave = new JSONObject();
                        transformToSave.put("translationX", String.valueOf(trID.getTranslation().getX()));
                        transformToSave.put("translationY", String.valueOf(trID.getTranslation().getY()));
                        transformToSave.put("translationZ", String.valueOf(trID.getTranslation().getZ()));
                        transformToSave.put("rotationX", String.valueOf(trID.getRotation().getX()));
                        transformToSave.put("rotationY", String.valueOf(trID.getRotation().getY()));
                        transformToSave.put("rotationZ", String.valueOf(trID.getRotation().getZ()));
                        transformToSave.put("rotationW", String.valueOf(trID.getRotation().getW()));
                        transformToSave.put("scaleX", String.valueOf(trID.getScale().getX()));
                        transformToSave.put("scaleY", String.valueOf(trID.getScale().getY()));
                        transformToSave.put("scaleZ", String.valueOf(trID.getScale().getZ()));
                        entityJSON.put("IDTransform", transformToSave);

                        // seve data components of entity
                        ConcurrentHashMap<String, String> entityData = base.getDataManager().getEntityData(idLong);
                        JSONObject dataComponentsToSave = new JSONObject();
                        for (String strKey : entityData.keySet()) {
                            dataComponentsToSave.put(strKey, entityData.get(strKey));
                        }
                        entityJSON.put("IDDataComponents", dataComponentsToSave);

                        entitiesToSave.put(String.valueOf(idLong), entityJSON);
                    }

                    layerToSave.put("Entities", entitiesToSave);
                    allLayers.put(layerNode.getName(), layerToSave);
                }
                layersGroupToSave.put("Layers", allLayers);
                allLayersGroups.put(layersGroup.getLayersGroupName(), layersGroupToSave);
            }
            sceneToSave.put("LayersGroups", allLayersGroups);
            allScenes.put(sceneName, sceneToSave);
        }

        saveSWEJson.put("Scenes", allScenes);
        saveJsonFile(pathToSave + ".swe", saveSWEJson);
        System.out.println("File saved: " + pathToSave + ".swe");
    }

    protected void clearScene() {
        // clear selection
        base.getSelectionManager().clearSelectionList();
        base.getSelectionManager().calculateSelectionCenter();

        // clear all scenes, layersGoups, layers
        for (EditorSceneObject scene : scenesList.values()) {
            scene.clearScene();
        }
        scenesList.clear();
        activeScene = null;


        // clear history
        base.getHistoryManager().clearHistory();

        // clear data components
        base.getDataManager().clearEntityData();

        // clear entities
        ConcurrentHashMap<Long, ComponentsControl> allControls = base.getEntityManager().getAllControls();
        for (Long ID : allControls.keySet()) {
            base.getEntityManager().removeEntity(ID);
            base.getSpatialSystem().removeSpatialControl(ID);
        }
        allControls.clear();

        // clear paths cache
        sceneNameCache = null;
        scenePathCache = null;

        // clear assets list
        clearAssets();

        //clear prevew boolean
        savePreviewJ3o = false;
    }

    public void clearAssets() {
        for (String str : spatialsList.keySet()) {
            dsk.deleteFromCache(new ModelKey(str));
        }
        spatialsList.clear();

        for (String str : assetsList) {
            dsk.unregisterLocator(str, FileLocator.class);
        }
        assetsList.clear();

        dsk.clearCache(); // clear all loaded models        
//        dsk.clearAssetEventListeners();
    }

    public void addAsset(String path) {
        String thePath = correctPath(path);
        File fl = new File(thePath);

        // registerLocator
//        System.out.println(fl.exists());
        if (!assetsList.contains(thePath)) {
            if (fl.exists()) {
                dsk.registerLocator(thePath, FileLocator.class);
                findFiles(thePath, thePath, "j3o");
            }
            assetsList.add(thePath);
        }

    }

    protected void savePreviewScene(String pathToSave, String sceneSWEName) {
        // Saving scene with linked Nodes to j3o (for scene viewing)
        Node sceneSavePreview = new Node(sceneSWEName);

        for (EditorSceneObject sceneObj : scenesList.values()) {
            Node sceneToSave = new Node(sceneObj.getSceneName());
            Node sceneNode = sceneObj.getSceneNode();

            // save Data of a Scene
            sceneToSave.setUserData("isActive", sceneNode.getUserData("isActive"));
            sceneToSave.setUserData("isEnabled", sceneNode.getUserData("isEnabled"));
            if ((Boolean) sceneNode.getUserData("isEnabled") == false) {
                sceneToSave.setCullHint(Spatial.CullHint.Always);
            }

            sceneSavePreview.attachChild(sceneToSave);

            // get layersGroups of a Scene
            for (EditorLayersGroupObject layersGroupObj : sceneObj.getLayerGroupsList().values()) {
                Node layersGroupToSave = new Node(layersGroupObj.getLayersGroupName());
                Node layersGroupNode = layersGroupObj.getLayersGroupNode();

                // save Data of a layerGroup
                layersGroupToSave.setUserData("isActive", layersGroupNode.getUserData("isActive"));
                layersGroupToSave.setUserData("isEnabled", layersGroupNode.getUserData("isEnabled"));
                if ((Boolean) layersGroupNode.getUserData("isEnabled") == false) {
                    layersGroupToSave.setCullHint(Spatial.CullHint.Always);
                }

                sceneToSave.attachChild(layersGroupToSave);

                // save Layers
                for (Node layerNode : layersGroupObj.getLayers()) {

                    if (layerNode.getChildren().size() > 0) {
                        Node layerToSave = new Node(layerNode.getName());

                        // save Data of a Layer
                        layerToSave.setUserData("isActive", layerNode.getUserData("isActive"));
                        layerToSave.setUserData("isLocked", layerNode.getUserData("isLocked"));
                        layerToSave.setUserData("isEnabled", layerNode.getUserData("isEnabled"));
                        if ((Boolean) layerNode.getUserData("isEnabled") == false) {
                            layerToSave.setCullHint(Spatial.CullHint.Always);
                        }

                        for (Spatial spEntity : layerNode.getChildren()) {


                            // load entity
                            Object IDObj = spEntity.getUserData("EntityID");
                            Object pathComponent = base.getEntityManager().getComponent((Long) IDObj, EntityModelPathComponent.class);
                            EntityModelPathComponent modelPath = (EntityModelPathComponent) pathComponent;
                            ModelKey mkLinkToScene = new ModelKey(modelPath.getModelPath());
                            AssetLinkNode linkedEntity = new AssetLinkNode(mkLinkToScene);

                            // set name
                            Object modelNameObj = base.getEntityManager().getComponent((Long) IDObj, EntityNameComponent.class);
                            EntityNameComponent modmodelName = (EntityNameComponent) modelNameObj;
                            linkedEntity.setName(modmodelName.getName());
                            linkedEntity.setLocalTransform(spEntity.getWorldTransform());

                            // set components
                            ConcurrentHashMap<String, String> dataComponents = base.getDataManager().getEntityData((Long) IDObj);
                            for (String key : dataComponents.keySet()) {
                                linkedEntity.setUserData(key, dataComponents.get(key));
                            }
                            // add entity to a layer
                            layerToSave.attachChild(linkedEntity);
                        }

                        layersGroupToSave.attachChild(layerToSave);
                    }
                }
            }
        }




        // save node
        binaryExport(pathToSave + sceneSWEName + "_preview", sceneSavePreview);

        // clear node
        sceneSavePreview.detachAllChildren();
        sceneSavePreview = null;
    }

    private void binaryExport(String fullPath, Node saveNode) {

        File MaFile = new File(fullPath + ".j3o");
        MaFile.setWritable(true);
        MaFile.canWrite();
        MaFile.canRead();


        try {
            BinaryExporter exporter = BinaryExporter.getInstance();
            exporter.save(saveNode, MaFile);
//            BinaryExporter.getInstance().save(saveNode, MaFile);
        } catch (IOException ex) {
            System.out.println("Baddddd Saveee");

        }

    }

    public Long createEntityModel(String name, String path, Long existedID) {

        // setup Entity
        Node model = null;
        if (spatialsList.contains(path) == false) {
            Node loadedModel = (Node) dsk.loadModel(path);
            spatialsList.put(path, loadedModel);
            model = loadedModel.clone(false);
        } else {
            model = (Node) spatialsList.get(path).clone(false);
        }

        Vector3f camHelperPosition = base.getCamManager().getCamTrackHelper().getWorldTranslation();
        model.setLocalTranslation(camHelperPosition);


        long ent;
        if (existedID == null) {
            ent = base.getEntityManager().createEntity();
        } else {
            ent = existedID;
        }

        base.getDataManager().setEntityData(ent, new ConcurrentHashMap<String, String>());
        ComponentsControl components = base.getEntityManager().addComponentControl(ent);

        EntityModelPathComponent modelPath = new EntityModelPathComponent(path);
        components.setComponent(modelPath);

        EntityNameComponent nameComponent = new EntityNameComponent(name + "_IDX" + ent);
        components.setComponent(nameComponent);
        model.setName(nameComponent.getName());

        EntitySpatialsControl spatialControl = base.getSpatialSystem().addSpatialControl(model, ent, base.getEntityManager().getComponentControl(ent));
        spatialControl.setType(EntitySpatialsControl.SpatialType.Node);
        spatialControl.recurseNodeID(model);

        return ent;
    }

    public void removeClones(String name) {
        String nameToRemove = name + "_IDX";
        List<Long> selList = base.getSelectionManager().getSelectionList();
        List<Long> idsToRemove = new ArrayList<Long>();
        for (Long id : selList) {

            // remove objects from the scene
            EntityNameComponent nameComp = (EntityNameComponent) base.getEntityManager().getComponent(id, EntityNameComponent.class);
            if (nameComp.getName().indexOf(nameToRemove) == 0) {
                idsToRemove.add(id);
            }
        }

        for (Long removeID : idsToRemove) {
            EntityNameComponent nameToRemoveReal = (EntityNameComponent) base.getEntityManager().getComponent(removeID, EntityNameComponent.class);
            base.getGuiManager().getSceneObjectsListBox().removeItem(nameToRemoveReal.getName());
            removeEntityObject(removeID);
            selList.remove(removeID);
        }
        idsToRemove.clear();
        idsToRemove = null;
        base.getSelectionManager().calculateSelectionCenter();
    }

    public List<Long> cloneSelectedEntities() {
        List<Long> selectionList = base.getSelectionManager().getSelectionList();
        List<Long> tempList = new ArrayList<Long>();
        for (Long idOfSelected : selectionList) {
            // selected entity's components
            ComponentsControl compControlSelected = base.getEntityManager().getComponentControl(idOfSelected);
            EntityModelPathComponent modelPathSelected = (EntityModelPathComponent) compControlSelected.getComponent(EntityModelPathComponent.class);
            Node selectedModel = (Node) base.getSpatialSystem().getSpatialControl(idOfSelected).getGeneralNode();
            Node layerToClone = selectedModel.getParent();
            EntityNameComponent modelNameSelected = (EntityNameComponent) compControlSelected.getComponent(EntityNameComponent.class);

            // new entity
            String selectedName = modelNameSelected.getName().substring(0, modelNameSelected.getName().indexOf("_IDX"));
            long newID = createEntityModel(selectedName, modelPathSelected.getModelPath(), null);
            Node newModel = (Node) base.getSpatialSystem().getSpatialControl(newID).getGeneralNode();
            newModel.setLocalTransform(selectedModel.getWorldTransform());

            // Clone data
            ConcurrentHashMap<String, String> dataOfSelected = base.getDataManager().getEntityData(idOfSelected);
            ConcurrentHashMap<String, String> dataNew = base.getDataManager().getEntityData(newID);
            for (String key : dataOfSelected.keySet()) {
                dataNew.put(key, dataOfSelected.get(key));
            }

            tempList.add(newID);
            layerToClone.attachChild(newModel);
        }

        return tempList;
    }

    public void removeEntityObject(long id) {
        //remove item from selection
        List<Long> selList = base.getSelectionManager().getSelectionList();
        if (selList.contains(id)) {
            Node nd = (Node) base.getSpatialSystem().getSpatialControl(id).getGeneralNode();
            base.getSelectionManager().removeSelectionBox(nd);
            nd = null;
        }

        // destroy entity
        base.getEntityManager().removeEntity(id);
        base.getSpatialSystem().removeSpatialControl(id);
        base.getDataManager().removeEntityData(id);
    }

    // Correct path for Windows OS
    protected String correctPath(String path) {
        String pathCorrected = path;

        if (File.separatorChar == '\\') {
            pathCorrected = pathCorrected.replace('\\', '/');
        }
        if (!path.endsWith("/") && path.indexOf(".") < 0) {
            pathCorrected += "/";
        }

        return pathCorrected;
    }

    protected void saveJsonFile(String pathToSave, JSONObject saveJson) {
        try {
            File saveFile = new File(pathToSave);
            saveFile.setReadable(true);
            saveFile.setWritable(true);

            FileWriter fileToSave = new FileWriter(saveFile);
            fileToSave.write(saveJson.toJSONString());
            fileToSave.flush();
            fileToSave.close();

        } catch (IOException e) {
            e.printStackTrace();
        }

    }

    private void initializeTempLighting() {

        dl = new DirectionalLight();
        dl.setDirection(new Vector3f(-0.8f, -0.6f, -0.08f).normalizeLocal());
        dl.setColor(new ColorRGBA(1.1f, 1, 0.95f, 1));
        root.addLight(dl);

        al = new AmbientLight();
        al.setColor(new ColorRGBA(1, 1, 2, 1));
        root.addLight(al);
    }

    protected JSONObject loadToJsonFile(String path) {
        // Load JSON script
        JSONParser json = new JSONParser();

        FileReader fileRead = null;
        JSONObject jsObj = null;

        try {
            fileRead = new FileReader(new File(path));
        } catch (FileNotFoundException ex) {
            System.out.println("bad JSON file");
        }

        try {
            jsObj = (JSONObject) json.parse(fileRead);
            fileRead.close();
        } catch (IOException ex) {
            System.out.println("bad JSON file");
        } catch (org.json.simple.parser.ParseException ex) {
            System.out.println("bad JSON file Parser");
        }

        return jsObj;
    }

//    protected Long addEntityToScene(String name) {
//        return createEntityModel(name, entitiesList.get(name), null);
//    }
    // Recursive search of files
    protected void findFiles(String dirEntity, String dirEntityRecursive, String fileExtension) {
        System.out.println("ooooooooo LOAD entity Dir : " + dirEntity);
        File dir = new File(dirEntityRecursive);
        File[] a = dir.listFiles();

        for (File f : a) {
            if (f.isDirectory() && f.getName().indexOf("svn") < 0) {
                // Recursive search
                System.out.println("****** CHECKing Dir : " + f.getName());
                String recursDir = dirEntityRecursive + f.getName() + "/";
                findFiles(dirEntity, recursDir, fileExtension);
            } else if (f.getName().endsWith("." + fileExtension)) {

                String strF = f.getName();
                String modelName = f.getName().substring(0, f.getName().indexOf("." + fileExtension));
                String modelRelativePath = f.getAbsolutePath().substring(dirEntity.length(), f.getAbsolutePath().length());

                // Add found models
                entitiesList.put(modelName, modelRelativePath);
                System.out.println("========>>FOUND ENTITY :: " + modelRelativePath);
            }
        }
    }

    public static List<String> getAssetsList() {
        return assetsList;
    }

    public static ConcurrentHashMap<String, String> getEntitiesListsList() {
        return entitiesList;
    }

    protected String getScenePath() {
        return scenePathCache;
    }

    protected void setScenePath(String scenePath) {
        scenePath = scenePath;
    }

    protected String getSceneName() {
        return sceneNameCache;
    }

    protected void setSceneName(String sceneName) {
        sceneName = sceneName;
    }

    public void newScene() {
        clearScene();
    }

    public boolean getSavePreviewJ3o() {
        return savePreviewJ3o;
    }

    public void setSavePreviewJ3o(boolean savePreviewJ3o) {
        this.savePreviewJ3o = savePreviewJ3o;
        System.out.println(savePreviewJ3o);
    }

    public static ConcurrentHashMap<String, EditorSceneObject> getScenesList() {
        return scenesList;
    }

    public EditorSceneObject getActiveScene() {
        return activeScene;
    }

    public void setActiveScene(EditorSceneObject activeScene) {
        if (this.activeScene != null) {
            this.activeScene.getSceneNode().setUserData("isActive", false); // old active
        }

        if (activeScene != null) {
            this.activeScene = activeScene;
            this.activeScene.getSceneNode().setUserData("isActive", true);  // new active            
        } else {
            this.activeScene = null;
        }

    }
}
