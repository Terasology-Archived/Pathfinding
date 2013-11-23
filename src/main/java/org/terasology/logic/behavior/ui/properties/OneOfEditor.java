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
public class OneOfEditor<C> extends JPanel implements Editor<C, String>, ActionListener {
    private OneOfProp<C, String> oneOfProp;
    private JComboBox<String> comboBox;
    private C boundObject;

    public OneOfEditor(OneOfProp<C, String> oneOfProp) {
        this.oneOfProp = oneOfProp;

        setBorder(new TitledBorder(oneOfProp.getTitle()));

        setLayout(new BorderLayout());

        comboBox = new JComboBox<>();
        for (String item : oneOfProp.getItems()) {
            comboBox.addItem(item);
        }
        comboBox.addActionListener(this);

        add(comboBox, BorderLayout.CENTER);
    }

    @Override
    public void actionPerformed(ActionEvent e) {
        oneOfProp.setValue(boundObject, (String) comboBox.getSelectedItem());
    }

    @Override
    public void bindObject(C value) {
        boundObject = value;
        comboBox.setSelectedItem(oneOfProp.getValue(value));
    }
}
