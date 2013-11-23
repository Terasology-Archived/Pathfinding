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

import javax.swing.*;
import javax.swing.border.TitledBorder;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import java.awt.*;

/**
 * @author synopia
 */
public class SliderEditor<C> extends JPanel implements Editor<C, Float>, ChangeListener {
    private JSlider slider;
    private NumberProperty<C, ?> property;
    private C boundObject;

    private BorderLayout borderLayout;
    private JLabel label;


    public SliderEditor(NumberProperty<C, ?> property) {
        this.property = property;

        setBorder(new TitledBorder(property.getTitle()));

        borderLayout = new BorderLayout();
        setLayout(borderLayout);

        label = new JLabel("");

        slider = new JSlider();
        slider.setMinimum(0);
        slider.setMaximum(100);
        slider.setMinorTickSpacing(1);
        slider.setMajorTickSpacing(10);
        slider.addChangeListener(this);

        add(slider, BorderLayout.CENTER);
        add(label, BorderLayout.SOUTH);
    }

    @Override
    public void bindObject(C value) {
        boundObject = value;
        setValue(property.getAsFloat(value), property.getMinValue(), property.getMaxValue());
    }

    public void setValue(float value, float minValue, float maxValue) {
        int sliderValue = (int) (((value - minValue) / (maxValue - minValue)) * 100.0f);
        slider.setValue(sliderValue);
    }

    @Override
    public void stateChanged(ChangeEvent e) {
        float range = Math.abs(property.getMaxValue() - property.getMinValue());
        float val = (slider.getValue() / 100.0f) * range + property.getMinValue();
        property.setAsFloat(boundObject, val);
        label.setText(property.getAsString(boundObject));
    }

}
