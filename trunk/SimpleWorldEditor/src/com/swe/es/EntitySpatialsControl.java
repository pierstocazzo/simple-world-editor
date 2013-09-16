/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.swe.es;

import com.jme3.scene.Geometry;
import com.jme3.scene.Node;
import com.jme3.scene.Spatial;
import java.util.ArrayList;
import java.util.List;

/**
 *
 * @author mifth
 */
public final class EntitySpatialsControl {
    
    private Spatial spatial;
    private static List<Geometry> mapChildMeshes = new ArrayList<Geometry>(); //Collection of meshes
    private SpatialType type;
//    private static EntityManager entManager;
    private long ID;
    private ComponentsControl components;
    
    public EntitySpatialsControl(Spatial sp, long ID, ComponentsControl components) {
        
        this.ID = ID;
        this.components = components;
        spatial = sp;
    }
    
    public static enum SpatialType {

        Node,
        LightNode,
        BatchNode,
        CameraNode,
        GuiNode
    }    
    
    public void setType(SpatialType type) {
        this.type = type;
    }
    
    public SpatialType getType() {
        return type;
    }
    
    public Spatial setGeneralNode(Spatial sp) {
        return spatial = sp;
    }
    
    public Spatial getGeneralNode() {
        return spatial;
    }

    //Read the node child to find geomtry and stored it to the map for later access as submesh
    public void recurseNodeID(Node generalNodde) {
        Node nd_temp = (Node) generalNodde;
        nd_temp.setUserData("EntityID", ID);
        
        for (int i = 0; i < nd_temp.getChildren().size(); i++) {
            
            if (nd_temp.getChildren().get(i) instanceof Node) {
                nd_temp.getChildren().get(i).setUserData("EntityID", ID);
                recurseNodeID((Node)nd_temp.getChildren().get(i));
            } else if (nd_temp.getChildren().get(i) instanceof Geometry) {
                Geometry geom = (Geometry) nd_temp.getChildren().get(i);
                geom.setUserData("EntityID", ID);
                mapChildMeshes.add(geom);
            }
        }
    }
    
    public Geometry getChildMesh(String name) {
        for (Geometry mc : mapChildMeshes) {
            if (name.equals(mc.getName())) {
                return mc;
            }
        }
        return null;
    }
    
    public List<Geometry> getChildMeshes() {
        return mapChildMeshes;
    }
    
    public void destroy() {
        mapChildMeshes.clear();
        spatial.removeFromParent();
        spatial = null;
    }
   
}
