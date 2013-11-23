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

import java.util.Arrays;
import java.util.List;

/**
 * @author synopia
 */
public class OneOfProp<C, T> implements Property<C, T> {
    private final Selector<T> selector;
    private final FieldMetadata<C, T> field;

    public OneOfProp(FieldMetadata<C, T> field, Selector<T> selector) {
        this.field = field;
        this.selector = selector;
    }

    public List<T> getItems() {
        return selector.getItems();
    }

    @Override
    public T getValue(C target) {
        return field.getValueChecked(target);
    }

    @Override
    public String getAsString(C target) {
        return getValue(target).toString();
    }

    @Override
    public void setValue(C target, T value) {
        field.setValue(target, value);
    }

    @Override
    public void setAsString(C target, String value) {

    }

    @Override
    public String getTitle() {
        return field.getName();
    }

    public static class StringSelector implements Selector<String> {
        private List<String> items;

        public StringSelector(List<String> items) {
            this.items = items;
        }

        @Override
        public List<String> getItems() {
            return items;
        }
    }

    public static class EnumSelector implements Selector<Enum> {
        private Class<Enum> enumClass;

        public EnumSelector(Class<Enum> enumClass) {
            this.enumClass = enumClass;
        }

        @Override
        public List<Enum> getItems() {
            Enum[] enums = enumClass.getEnumConstants();
            return Arrays.asList(enums);
        }
    }

    public interface Selector<T> {
        List<T> getItems();
    }
}
