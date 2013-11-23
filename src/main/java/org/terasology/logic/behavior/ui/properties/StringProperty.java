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
public class StringProperty<C> implements Property<C, String> {
    private final FieldMetadata<C, String> field;

    public StringProperty(FieldMetadata<C, String> field) {
        this.field = field;
    }

    @Override
    public String getAsString(C target) {
        return getValue(target);
    }

    @Override
    public void setAsString(C target, String value) {
        setValue(target, value);
    }

    @Override
    public String getValue(C target) {
        return field.getValueChecked(target);
    }

    @Override
    public void setValue(C target, String value) {
        field.setValue(target, value);
    }

    @Override
    public String getTitle() {
        return field.getName();
    }
}
