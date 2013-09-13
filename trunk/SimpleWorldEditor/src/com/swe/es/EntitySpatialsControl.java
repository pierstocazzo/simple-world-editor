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
public final class EntitySpatialsControl implements EntityComponent{
    
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
//        spatial.addControl(this);
        
        
    }
    
    public static enum SpatialType {

        Node,
        LightNode,
        BatchNode,
        CameraNode,
        GuiNode
    }    
    
    public void setType(SpatialType type) {
        if (type.equals(SpatialType.Node)) {
            type = SpatialType.Node;
        } else if (type.equals(SpatialType.BatchNode)) {
            type = SpatialType.BatchNode;
        } else if (type.equals(SpatialType.CameraNode)) {
            type = SpatialType.CameraNode;
        } else if (type.equals(SpatialType.GuiNode)) {
            type = SpatialType.GuiNode;
        } else if (type.equals(SpatialType.LightNode)) {
            type = SpatialType.LightNode;
        }
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
                //System.out.println("omomomomoomomomo GEOMETRY ADDED : "+geom.getName()+" for Entity "+mObjectName);
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
//        spatial.removeControl(this);
        spatial = null;
    }
    
//    @Override
//    protected void controlUpdate(float tpf) {
//
//        // Update transforms
//        if (components.getUpdateType() == ComponentsControl.UpdateType.dynamicServerEntity) {
//            EntityTransformComponent transform = (EntityTransformComponent) components.getComponent(EntityTransformComponent.class);
//            spatial.setLocalTransform(transform.getTransform());
//        } else if (components.getUpdateType() == ComponentsControl.UpdateType.dynamicClientEntity) {
//            EntityTransformComponent transform = (EntityTransformComponent) components.getComponent(EntityTransformComponent.class);
//            transform.getTransform().set(spatial.getLocalTransform());
//        }
//        
//        if (components.isDoUpdateOnce()) {
//            components.updateOnce(false, ComponentsControl.UpdateType.staticEntity);
////            components.setUpdateType(ComponentsControl.UpdateType.staticEntity);
//        }
//    }
//    
//    @Override
//    protected void controlRender(RenderManager rm, ViewPort vp) {
//        
//    }
//    
//    public Control cloneForSpatial(Spatial spatial) {
//        throw new UnsupportedOperationException("Not supported yet.");
//    }
//    
//    @Override
//    public void read(JmeImporter im) throws IOException {
//        super.read(im);
//        InputCapsule in = im.getCapsule(this);
//        //TODO: load properties of this Control, e.g.
//        //this.value = in.readFloat("name", defaultValue);
//    }
//    
//    @Override
//    public void write(JmeExporter ex) throws IOException {
//        super.write(ex);
//        OutputCapsule out = ex.getCapsule(this);
//        //TODO: save properties of this Control, e.g.
//        //out.write(this.value, "name", defaultValue);
//    }    
}
