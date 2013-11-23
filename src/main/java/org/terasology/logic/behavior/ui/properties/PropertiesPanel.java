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

import com.google.common.collect.Lists;
import org.terasology.classMetadata.ClassMetadata;
import org.terasology.classMetadata.DefaultClassMetadata;
import org.terasology.classMetadata.FieldMetadata;
import org.terasology.classMetadata.copying.CopyStrategyLibrary;
import org.terasology.classMetadata.reflect.ReflectFactory;
import org.terasology.classMetadata.reflect.ReflectionReflectFactory;
import org.terasology.engine.SimpleUri;
import org.terasology.logic.behavior.ui.properties.api.BooleanProperty;
import org.terasology.logic.behavior.ui.properties.api.OneOfProperty;
import org.terasology.logic.behavior.ui.properties.api.RangeProperty;
import org.terasology.logic.behavior.ui.properties.api.TextProperty;

import javax.swing.*;
import javax.swing.border.TitledBorder;
import java.lang.reflect.Field;
import java.util.Arrays;
import java.util.List;

import static com.google.common.base.Predicates.and;
import static com.google.common.base.Predicates.or;
import static org.reflections.ReflectionUtils.getAllFields;
import static org.reflections.ReflectionUtils.withAnnotation;
import static org.reflections.ReflectionUtils.withType;

/**
 * @author synopia
 */
public class PropertiesPanel<T> extends JPanel {
    private final List<Property<T, ?>> properties = Lists.newArrayList();
    private final List<Editor<T, ?>> editors = Lists.newArrayList();
    private final TitledBorder titledBorder;

    public PropertiesPanel(Class<T> type) {
        titledBorder = new TitledBorder("");
        setBorder(titledBorder);

        setLayout(new BoxLayout(this, BoxLayout.Y_AXIS));
        try {
            ReflectFactory reflectFactory = new ReflectionReflectFactory();

            CopyStrategyLibrary copyStrategies = CopyStrategyLibrary.create(reflectFactory);
            ClassMetadata<T, ?> classMetadata = new DefaultClassMetadata<>(new SimpleUri(), type, reflectFactory, copyStrategies);

            registerRange(type, classMetadata);
            registerText(type, classMetadata);
            registerOneOf(type, classMetadata);
            registerBoolean(type, classMetadata);
        } catch (NoSuchMethodException e) {
            throw new RuntimeException(e);
        }
    }

    private void registerOneOf(Class<T> type, ClassMetadata<T, ?> classMetadata) {
        for (Field field : getAllFields(type, withAnnotation(OneOfProperty.class))) {
            FieldMetadata<T, String> fieldMetadata = (FieldMetadata<T, String>) classMetadata.getField(field.getName());
            OneOfProperty oneOf = field.getAnnotation(OneOfProperty.class);
            OneOfProp.StringSelector stringSelector = new OneOfProp.StringSelector(Arrays.asList(oneOf.names()));
            OneOfProp<T, String> property = new OneOfProp<>(fieldMetadata, stringSelector);
            properties.add(property);
            OneOfEditor<T> editor = new OneOfEditor<>(property);
            editors.add(editor);
            add(editor);
        }
    }

    private void registerText(Class<T> type, ClassMetadata<T, ?> classMetadata) {
        for (Field field : getAllFields(type, and(withAnnotation(TextProperty.class), withType(String.class)))) {
            FieldMetadata<T, String> fieldMetadata = (FieldMetadata<T, String>) classMetadata.getField(field.getName());
            StringProperty<T> property = new StringProperty<>(fieldMetadata);
            properties.add(property);
            TextFieldEditor<T> editor = new TextFieldEditor<>(property);
            editors.add(editor);
            add(editor);
        }
    }

    private void registerBoolean(Class<T> type, ClassMetadata<T, ?> classMetadata) {
        for (Field field : getAllFields(type, and(withAnnotation(BooleanProperty.class), or(withType(Boolean.class), withType(Boolean.TYPE))))) {
            FieldMetadata<T, Boolean> fieldMetadata = (FieldMetadata<T, Boolean>) classMetadata.getField(field.getName());
            BooleanProp<T> property = new BooleanProp<>(fieldMetadata);
            properties.add(property);
            BooleanEditor<T> editor = new BooleanEditor<>(property);
            editors.add(editor);
            add(editor);
        }
    }

    private void registerRange(Class<T> type, ClassMetadata<T, ?> classMetadata) {
        for (Field field : getAllFields(type, and(withAnnotation(RangeProperty.class)))) {
            Class<?> fieldType = field.getType();
            NumberProperty<T, ?> property = null;
            RangeProperty rangeProperty = field.getAnnotation(RangeProperty.class);

            if (fieldType == Float.TYPE || fieldType == Float.class) {
                FieldMetadata<T, Float> fieldMetadata = (FieldMetadata<T, Float>) classMetadata.getField(field.getName());
                property = new FloatProperty<>(fieldMetadata, rangeProperty.min(), rangeProperty.max());
            } else if (fieldType == Integer.TYPE || fieldType == Integer.class) {
                FieldMetadata<T, Integer> fieldMetadata = (FieldMetadata<T, Integer>) classMetadata.getField(field.getName());
                property = new IntProperty<>(fieldMetadata, rangeProperty.min(), rangeProperty.max());
            }
            properties.add(property);
            SliderEditor<T> editor = new SliderEditor<>(property);
            editors.add(editor);
            add(editor);
        }
    }

    public void bindObject(T object) {
        titledBorder.setTitle(object.toString());
        for (Editor<T, ?> editor : editors) {
            editor.bindObject(object);
        }
    }
}
