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
public class Debugger extends JToolBar implements Interpreter.PauseListener {
    private Interpreter interpreter;

    public Debugger() {
        super(HORIZONTAL);
        ButtonGroup group = new ButtonGroup();
        JToggleButton pause = new JToggleButton(new AbstractAction("||") {
            @Override
            public void actionPerformed(ActionEvent e) {
                interpreter.getTree().setEditable(true);
                interpreter.reset();
            }
        });
        JToggleButton step = new JToggleButton(new AbstractAction("|>") {
            @Override
            public void actionPerformed(ActionEvent e) {
                interpreter.step(0.2f);
            }
        });
        JToggleButton play = new JToggleButton(new AbstractAction(">>") {
            @Override
            public void actionPerformed(ActionEvent e) {
                interpreter.getTree().setEditable(false);
                interpreter.reset();
            }
        });
        group.add(pause);
        group.add(step);
        group.add(play);

        add(pause);
        add(step);
        add(play);
    }

    public Interpreter getInterpreter() {
        return interpreter;
    }

    public void setInterpreter(Interpreter interpreter) {
        this.interpreter = interpreter;
        interpreter.addListener(this);
    }

    @Override
    public void pauseChanged(boolean pause) {

    }
}
