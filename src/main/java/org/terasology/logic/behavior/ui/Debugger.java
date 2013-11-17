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
package org.terasology.logic.behavior.ui;

import org.terasology.logic.behavior.tree.Interpreter;

import javax.swing.*;
import java.awt.event.ActionEvent;

/**
 * @author synopia
 */
public class Debugger extends JToolBar {
    private Interpreter interpreter;

    public Debugger() {
        super(HORIZONTAL);
        add(new AbstractAction("||") {
            @Override
            public void actionPerformed(ActionEvent e) {
                interpreter.setPause(true);
                interpreter.reset();
            }
        });
        add(new AbstractAction("|>") {
            @Override
            public void actionPerformed(ActionEvent e) {
                interpreter.step(0.2f);
            }
        });
        add(new AbstractAction(">>") {
            @Override
            public void actionPerformed(ActionEvent e) {
                interpreter.reset();
                interpreter.setPause(false);
            }
        });
    }

    public Interpreter getInterpreter() {
        return interpreter;
    }

    public void setInterpreter(Interpreter interpreter) {
        this.interpreter = interpreter;
    }
}
