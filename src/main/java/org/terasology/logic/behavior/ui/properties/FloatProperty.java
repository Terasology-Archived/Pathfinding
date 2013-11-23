/*
 * Copyright 2013 MovingBlocks
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *  http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.terasology.logic.behavior.ui.properties;

import org.terasology.classMetadata.FieldMetadata;

import java.text.DecimalFormat;

/**
 * @author synopia
 */
public class FloatProperty<C> extends NumberProperty<C, Float> {
    private static final DecimalFormat DEFAULT_DECIMAL_FORMAT = new DecimalFormat("0.0000000");

    private final float min;
    private final float max;
    private final FieldMetadata<C, Float> field;

    public FloatProperty(FieldMetadata<C, Float> field, float min, float max) {
        this.min = min;
        this.max = max;
        this.field = field;
    }

    @Override
    public float getMinValue() {
        return min;
    }

    @Override
    public float getMaxValue() {
        return max;
    }

    @Override
    public String getAsString(C target) {
        return DEFAULT_DECIMAL_FORMAT.format(getValue(target));
    }

    @Override
    public void setAsString(C target, String value) {
        setValue(target, Float.parseFloat(value));
    }

    @Override
    public Float getValue(C target) {
        return field.getValueChecked(target);
    }

    @Override
    public void setValue(C target, Float value) {
        field.setValue(target, value);
    }

    @Override
    public float getAsFloat(C target) {
        return getValue(target);
    }

    @Override
    public void setAsFloat(C target, float value) {
        setValue(target, value);
    }

    @Override
    public String getTitle() {
        return field.getName();
    }
}
