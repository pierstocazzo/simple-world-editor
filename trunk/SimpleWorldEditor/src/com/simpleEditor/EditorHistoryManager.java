/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.simpleEditor;

import com.jme3.app.Application;
import com.jme3.asset.AssetManager;
import com.jme3.math.Transform;
import com.jme3.scene.Node;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ConcurrentHashMap;

/**
 *
 * @author mifth
 */
public class EditorHistoryManager {

    private AssetManager assetMan;
    private Node root, guiNode;
    private Application app;
    private EditorBaseManager base;
//    private static ConcurrentHashMap<Integer, EditorHistoryObject> historyList = new ConcurrentHashMap<Integer, EditorHistoryObject>();
    private static ArrayList<EditorHistoryObject> historyList;
    private int historycurrentNumber;
    private int historyMaximumnumber;

    public EditorHistoryManager(Application app, EditorBaseManager base) {
        this.app = app;
        this.base = base;
        assetMan = this.app.getAssetManager();
        root = (Node) this.app.getViewPort().getScenes().get(0);
        guiNode = (Node) this.app.getGuiViewPort().getScenes().get(0);

        historyList = new ArrayList<EditorHistoryObject>();
        historycurrentNumber = 0;
        historyMaximumnumber = 60;

        setDefaultHistoryObject();

    }

    protected static ArrayList<EditorHistoryObject> getHistoryList() {
        return historyList;
    }

    protected void setDefaultHistoryObject() {
        // set default history object
        EditorHistoryObject hObj = new EditorHistoryObject();
        hObj.createSelectDeselectEntitiesList();
        historyList.add(0, hObj);
    }

    protected void historyUndo() {
        if (historycurrentNumber > 0) {
            int prevHistoryNumber = historycurrentNumber - 1;
            EditorHistoryObject historyPreviousObj = historyList.get(prevHistoryNumber);

            // selection/transform changes
            if (historyPreviousObj.getSelectDeselectEntitiesList() != null) {
                base.getSelectionManager().clearSelectionList();
                System.out.println("historycurrentNumber" + prevHistoryNumber);

                ConcurrentHashMap<Long, Transform> previousSelectionlist = historyPreviousObj.getSelectDeselectEntitiesList();
                boolean doTransform = historyList.get(historycurrentNumber).isDoTransform();

                for (Long id : previousSelectionlist.keySet()) {
                    if (base.getEntityManager().containsID(id)) {
                        Node layer = base.getSpatialSystem().getSpatialControl(id).getGeneralNode().getParent();
                        boolean isEnabledLayer = (Boolean)layer.getUserData("isEnabled");
                        if (isEnabledLayer) {
                            if (doTransform) {
                                base.getSpatialSystem().getSpatialControl(id).getGeneralNode().setLocalTransform(previousSelectionlist.get(id));
                            }
                            base.getSelectionManager().selectEntity(id, EditorSelectionManager.SelectionMode.Additive);
                        }

                    }
                }
                base.getSelectionManager().calculateSelectionCenter();
//                System.out.println("SelListUndo2" + PreviousSelectionlist.size());
//                System.out.println("SelListUndo2" + base.getSelectionManager().getSelectionList().size());
            }

            historycurrentNumber = prevHistoryNumber;

        }
    }

    protected void historyRedo() {
        if (historycurrentNumber < historyMaximumnumber
                && historyList.size() > historycurrentNumber + 1) {
            historycurrentNumber = historycurrentNumber + 1;
            EditorHistoryObject historyPreviousList = historyList.get(historycurrentNumber);

            // selection/transform changes
            if (historyPreviousList.getSelectDeselectEntitiesList() != null) {
                base.getSelectionManager().clearSelectionList();
                ConcurrentHashMap<Long, Transform> reversedSelectionlist = historyPreviousList.getSelectDeselectEntitiesList();
                boolean doTransform = historyList.get(historycurrentNumber).isDoTransform();
                for (Long id : reversedSelectionlist.keySet()) {
                    if (base.getEntityManager().containsID(id)) {
                        Node layer = base.getSpatialSystem().getSpatialControl(id).getGeneralNode().getParent();
                        boolean isEnabledLayer = (Boolean)layer.getUserData("isEnabled");
                        if (isEnabledLayer) {
                            if (doTransform) {
                                base.getSpatialSystem().getSpatialControl(id).getGeneralNode().setLocalTransform(reversedSelectionlist.get(id));
                            }
                            base.getSelectionManager().selectEntity(id, EditorSelectionManager.SelectionMode.Additive);
                        }
                    }
                }
            }
            base.getSelectionManager().calculateSelectionCenter();
        }
    }

    protected void prepareNewHistory() {
//        System.out.println("selSize" + base.getSelectionManager().getSelectionList().size());

        if (historycurrentNumber + 1 < historyList.size()) {
            for (int i = historyList.size() - 1; i > historycurrentNumber; i--) {
                System.out.println("DELETE RED" + i);
                historyList.get(i).clearHistoryObject();
                historyList.remove(i);
            }
        }

//        System.out.println("histListSize1" + historyList.size());
        if (historycurrentNumber < historyMaximumnumber) {
            historycurrentNumber = historycurrentNumber + 1;
        } else {

            historyList.get(0).clearHistoryObject();
            historyList.remove(0); // remove 0 history Object
        }


        System.out.println("numb" + historycurrentNumber);

        EditorHistoryObject newHistory = new EditorHistoryObject();
        setHistoryObject(historycurrentNumber, newHistory);

        System.out.println("histListSize2" + historyList.size());
    }

    protected void setNewSelectionHistory(List<Long> selectionIDList) {
        EditorHistoryObject historyObj = historyList.get(historycurrentNumber);
//        System.out.println("historyObject_SelectList_Hash" + historyObj.getSelectDeselectEntitiesList().size());
        historyObj.createSelectDeselectEntitiesList();


        for (int i = 0; i < selectionIDList.size(); i++) {
            Transform trID = base.getSpatialSystem().getSpatialControl(base.getSelectionManager().getSelectionList().get(i)).getGeneralNode().getWorldTransform().clone();
            historyObj.getSelectDeselectEntitiesList().put(selectionIDList.get(i), trID);
        }
    }

    protected void clearHistory() {
        for (EditorHistoryObject hObj : historyList) {
            hObj.clearHistoryObject();
            hObj = null;
        }
        historyList.clear();
        historycurrentNumber = 0;
        setDefaultHistoryObject();
    }

    protected static void setHistoryObject(int historyNumber, EditorHistoryObject historyObject) {
        historyList.add(historyNumber, historyObject);
    }

    protected static EditorHistoryObject getHistoryObject(int historyNumber) {
        return historyList.get(historyNumber);
    }

    protected int getHistoryCurrentNumber() {
        return historycurrentNumber;
    }

    protected void setHistoryNumber(int historyNumber) {
        historycurrentNumber = historyNumber;
    }

    protected int getHistoryMaximumnumber() {
        return historyMaximumnumber;
    }
}
