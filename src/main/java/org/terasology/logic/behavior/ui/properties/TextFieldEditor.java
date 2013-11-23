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
public class TextFieldEditor<C> extends JPanel implements Editor<C, String>, ActionListener {
    private Property<C, ?> property;
    private JTextField textField;
    private C boundObject;

    public TextFieldEditor(Property<C, ?> property) {
        this.property = property;

        setBorder(new TitledBorder(property.getTitle()));

        setLayout(new BorderLayout());

        textField = new JTextField();
        textField.addActionListener(this);

        add(textField, BorderLayout.CENTER);
    }

    @Override
    public void actionPerformed(ActionEvent e) {
        property.setAsString(boundObject, textField.getText());
    }

    @Override
    public void bindObject(C value) {
        boundObject = value;
        textField.setText(property.getAsString(value));
    }
}
