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

    float constraint;

    public EditorTransformConstraint() {
        constraint = 0.0f;
    }
    
    public float getConstraint() {
        return constraint;
    }

    public void setConstraint(float constraint) {
        this.constraint = constraint;
    }

    public float constraintValue(float value) {

        float valueToConstrait = value;

        // Make Constraint
        if (constraint > 0.0f) {
            float distanceTest = valueToConstrait + (constraint);
            String strDistance = String.valueOf(valueToConstrait);
            if (constraint == 0.5f) {
                strDistance = strDistance.substring(0, strDistance.indexOf(".") + 1);
            } else if (constraint == 1.0f) {
                strDistance = strDistance.substring(0, strDistance.indexOf("."));
            } else if (constraint == 10.0f || constraint == 5.0f) {
                strDistance = strDistance.substring(0, strDistance.indexOf(".") - 1);
                strDistance = strDistance + "0";
            }

            float lowValue = Float.valueOf(strDistance);
            float hightValue = lowValue + constraint;

            if (valueToConstrait >= hightValue) {
                valueToConstrait = hightValue;
            } else {
                valueToConstrait = lowValue;
            }
        }

        return valueToConstrait;
    }
}
