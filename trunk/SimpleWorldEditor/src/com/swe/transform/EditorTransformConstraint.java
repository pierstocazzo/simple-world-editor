/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.swe.transform;

/**
 *
 * @author mifth
 */
public class EditorTransformConstraint {

    private float moveConstraint, rotateConstraint, scaleConstraint;

    public EditorTransformConstraint() {
        moveConstraint = 0f;
        rotateConstraint = 0f;
        scaleConstraint = 0f;
    }
    
    public float constraintValue(float value, float constaintValue) {

        float valueToConstrait = value;

        // Make Constraint
        if (constaintValue > 0.0f) {
            float distanceTest = valueToConstrait + (constaintValue);
            String strDistance = String.valueOf(valueToConstrait);
            if (constaintValue == 0.5f) {
                strDistance = strDistance.substring(0, strDistance.indexOf(".") + 1);
            } else if (constaintValue == 1.0f) {
                strDistance = strDistance.substring(0, strDistance.indexOf("."));
            } else if ((constaintValue == 10.0f || constaintValue == 5.0f) || constaintValue > 10f) {
                strDistance = strDistance.substring(0, strDistance.indexOf(".") - 1);
                strDistance = strDistance + "0";
            }

            float lowValue = Float.valueOf(strDistance);
            float hightValue = lowValue + constaintValue;

            if (valueToConstrait >= hightValue) {
                valueToConstrait = hightValue;
            } else {
                valueToConstrait = lowValue;
            }
        }

        return valueToConstrait;
    }
    
    public float getMoveConstraint() {
        return moveConstraint;
    }

    public void setMoveConstraint(float constraint) {
        this.moveConstraint = constraint;
    }

    public float getRotateConstraint() {
        return rotateConstraint;
    }

    public void setRotateConstraint(float rotateConstraint) {
        this.rotateConstraint = rotateConstraint;
    }

    public float getScaleConstraint() {
        return scaleConstraint;
    }

    public void setScaleConstraint(float scaleConstraint) {
        this.scaleConstraint = scaleConstraint;
    }
    
}
