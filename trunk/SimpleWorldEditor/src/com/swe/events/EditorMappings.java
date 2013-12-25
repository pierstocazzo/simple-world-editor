/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.swe.events;

import com.jme3.app.Application;
import com.jme3.input.KeyInput;
import com.jme3.input.MouseInput;
import com.jme3.input.controls.ActionListener;
import com.jme3.input.controls.AnalogListener;
import com.jme3.input.controls.KeyTrigger;
import com.jme3.input.controls.MouseButtonTrigger;
import com.jme3.math.Transform;
import com.jme3.renderer.Camera;
import com.jme3.scene.Node;
import com.swe.EditorBaseManager;
import com.swe.camera.EditorCameraManager;
import com.swe.selection.EditorSelectionManager;
import de.lessvoid.nifty.elements.Element;

public class EditorMappings implements AnalogListener, ActionListener {

    private Node root, camHelper;
    private Application app;
    private Camera camera;
    private EditorBaseManager base;
    private EditorCameraManager camMan;
    private boolean transformResult;
    private boolean selectResult;
    private String[] mappings;

    public EditorMappings(Application app, EditorBaseManager baseParts) {

        this.app = app;
        this.base = baseParts;
        root = (Node) this.app.getViewPort().getScenes().get(0);
        camHelper = (Node) root.getChild("camTrackHelper");
        camera = app.getCamera();
        camMan = baseParts.getCamManager();

        transformResult = false;
        selectResult = false;

        setupKeys();
    }

    private void setupKeys() {
        //Set up keys and listener to read it

        mappings = new String[]{
            "MoveCameraHelper",
            "MoveCameraHelperToSelection",
            "MoveOrSelect",
            "ScaleAll",
            "History",
            "ShowHideRightPanel",
            "SelectDeselectAll",
            "LeftShiftKey",
            "LeftCtrlKey"
        };

        app.getInputManager().addMapping("MoveCameraHelper", new MouseButtonTrigger(MouseInput.BUTTON_RIGHT));
        app.getInputManager().addMapping("MoveCameraHelperToSelection", new KeyTrigger(KeyInput.KEY_C));
        app.getInputManager().addMapping("MoveOrSelect", new MouseButtonTrigger(MouseInput.BUTTON_LEFT));
        app.getInputManager().addMapping("ScaleAll", new KeyTrigger(KeyInput.KEY_S));
        app.getInputManager().addMapping("History", new KeyTrigger(KeyInput.KEY_Z));
//        app.getInputManager().addMapping("HistoryRedo", new KeyTrigger(KeyInput.KEY_X));
        app.getInputManager().addMapping("ShowHideRightPanel", new KeyTrigger(KeyInput.KEY_TAB));
        app.getInputManager().addMapping("LeftShiftKey", new KeyTrigger(KeyInput.KEY_LSHIFT));
        app.getInputManager().addMapping("LeftCtrlKey", new KeyTrigger(KeyInput.KEY_LCONTROL));

        addListener();
    }

    public void addListener() {
        app.getInputManager().addListener(this, mappings);
    }

    public void removeListener() {
        app.getInputManager().removeListener(this);
    }

    public void onAnalog(String name, float value, float tpf) {

        // Move Camera
        if (name.equals("MoveCameraHelper")) {
            camMan.setDoMoveCamera(true);
        }
    }

    public void onAction(String name, boolean isPressed, float tpf) {

        // set booleans for Ctrl and Shift
        if (name.equals("LeftCtrlKey") && isPressed) {
            base.getEventManager().setCtrlBool(true);
            base.getSelectionManager().setSelectionTool(EditorSelectionManager.SelectionToolType.Rectangle);
        } else if (name.equals("LeftCtrlKey") && !isPressed) {
            base.getEventManager().setCtrlBool(false);
            base.getSelectionManager().setSelectionTool(EditorSelectionManager.SelectionToolType.MouseClick);
        }

        if (name.equals("LeftShiftKey") && isPressed) {
            base.getEventManager().setShiftBool(true);
            base.getSelectionManager().setSelectionMode(EditorSelectionManager.SelectionMode.Additive);
        } else if (name.equals("LeftShiftKey") && !isPressed) {
            base.getEventManager().setShiftBool(false);
            base.getSelectionManager().setSelectionMode(EditorSelectionManager.SelectionMode.Normal);
        }

        // Select or transformTool an entity
        if (name.equals("MoveOrSelect") && isPressed && !name.equals("ScaleAll")) {

            if (!base.getEventManager().isActive()) {
                base.getHistoryManager().prepareNewHistory();
                transformResult = base.getTransformManager().activate();

                if (!transformResult) {
                    selectResult = base.getSelectionManager().activate();
                }
            }
            base.getEventManager().setAction(true);

        } else if (name.equals("MoveOrSelect") && !isPressed) {
            if (transformResult) {
                base.getTransformManager().deactivate();
                transformResult = false;
            }
            if (selectResult) {
                base.getSelectionManager().deactivate();
                selectResult = false;
            }

            base.getEventManager().setAction(false);
            System.out.println("transform done");
        }


        // scaleTool
        if (name.equals("ScaleAll") && isPressed && !base.getEventManager().isActive()) {
            if (base.getSelectionManager().getSelectionList().size() > 0) {
                base.getHistoryManager().prepareNewHistory();
                base.getTransformManager().scaleAll();
                transformResult = true;
                base.getEventManager().setAction(true);
            }
        } else if (name.equals("MoveCameraHelperToSelection") && isPressed && !base.getEventManager().isActive()) {
            if (!transformResult && !selectResult) {
                Transform selectionCenter = base.getSelectionManager().getSelectionCenter();
                if (selectionCenter != null) {
                    base.getCamManager().getCamTrackHelper().setLocalTranslation(selectionCenter.getTranslation().clone());
                }
                selectionCenter = null;
            }

        } else if ((name.equals("MoveCameraHelperToSelection") && isPressed && !base.getEventManager().isActive())) {
            if (base.getSelectionManager().getSelectionList().size() > 0) {
                base.getSelectionManager().clearSelectionList();
            }
        }

        // Undo/Redo
        if (name.equals("History") && isPressed
                && !base.getEventManager().isActive() && base.getEventManager().isCtrlBool()
                && !base.getEventManager().isShiftBool()) {
            base.getHistoryManager().historyUndo();

        } else if (name.equals("History") && isPressed
                && !base.getEventManager().isActive() && base.getEventManager().isCtrlBool()
                && base.getEventManager().isShiftBool()) {
            base.getHistoryManager().historyRedo();
        }

        if (name.equals("ShowHideRightPanel") && isPressed && !base.getEventManager().isActive()) {
            base.getGuiManager().getScreen().getFocusHandler().resetFocusElements();
            Element rightPanel = base.getGuiManager().getRightPanel();
            if (rightPanel.isVisible()) {
                rightPanel.hide();
            } else {
                rightPanel.show();
            }
        }
    }
}
