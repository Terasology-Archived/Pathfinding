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
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

/**
 * @author synopia
 */
public class BooleanEditor<C> extends JPanel implements Editor<C, Boolean>, ActionListener {
    private final BooleanProp<C> property;
    private C boundObject;
    private JCheckBox checkBox;

    public BooleanEditor(BooleanProp<C> property) {
        this.property = property;
        setBorder(new TitledBorder(property.getTitle()));

        setLayout(new BorderLayout());

        checkBox = new JCheckBox();
        checkBox.addActionListener(this);

        add(checkBox, BorderLayout.CENTER);

    }

    @Override
    public void actionPerformed(ActionEvent e) {
        property.setValue(boundObject, checkBox.isSelected());
    }

    @Override
    public void bindObject(C value) {
        boundObject = value;
        checkBox.setSelected(property.getValue(value));
    }
}
