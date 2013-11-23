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

/**
 * @author synopia
 */
public class BooleanProp<C> implements Property<C, Boolean> {
    private final FieldMetadata<C, Boolean> field;

    public BooleanProp(FieldMetadata<C, Boolean> field) {
        this.field = field;
    }

    @Override
    public Boolean getValue(C target) {
        return field.getValueChecked(target);
    }

    @Override
    public String getAsString(C target) {
        return getValue(target).toString();
    }

    @Override
    public void setValue(C target, Boolean value) {
        field.setValue(target, value);
    }

    @Override
    public void setAsString(C target, String value) {
        setValue(target, Boolean.parseBoolean(value));
    }

    @Override
    public String getTitle() {
        return field.getName();
    }
}
