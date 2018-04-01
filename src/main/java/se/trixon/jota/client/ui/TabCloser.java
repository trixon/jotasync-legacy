/* 
 * Copyright 2018 Patrik Karlström.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package se.trixon.jota.client.ui;

import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.plaf.basic.BasicButtonUI;
import org.apache.commons.lang3.SystemUtils;
import se.trixon.almond.util.AlmondOptions;
import se.trixon.almond.util.Dict;
import se.trixon.almond.util.icons.material.MaterialIcon;

/**
 *
 * @author Patrik Karlström
 */
public class TabCloser extends JPanel {

    private static final int ICON_SIZE = 16;
    private final TabButton mButton;
    private final AlmondOptions mAlmondOptions = AlmondOptions.getInstance();

    public TabCloser(final TabHolder pane) {
        super(new FlowLayout(FlowLayout.LEFT, 0, 0));
        setOpaque(false);

        JLabel label = new JLabel() {
            @Override
            public String getText() {
                int i = pane.indexOfTabComponent(TabCloser.this);
                if (i != -1) {
                    setText(pane.getTitleAt(i));
                    return super.getText();
                }
                return null;
            }
        };

        add(label);
        label.setBorder(BorderFactory.createEmptyBorder(0, 0, 0, 5));
        mButton = new TabButton();
        int buttonPos = SystemUtils.IS_OS_WINDOWS ? 1 : 0;
        add(mButton, buttonPos);
        setBorder(BorderFactory.createEmptyBorder(2, 0, 0, 0));
    }

    public TabButton getButton() {
        return mButton;
    }

    void postSetAction() {
        mButton.setText(null);
        mButton.setIcon(MaterialIcon._Navigation.CLOSE.get(ICON_SIZE, mAlmondOptions.getIconColor()));
    }

    class TabButton extends JButton {

        public TabButton() {
            init();
            setEnabled(false);
        }

        @Override
        public void updateUI() {
            setIcon(MaterialIcon._Navigation.CLOSE.get(ICON_SIZE, mAlmondOptions.getIconColor()));
        }

        private void init() {
            int size = 17;
            setPreferredSize(new Dimension(size, size));
            setToolTipText(Dict.TAB_CLOSE.toString());
            setUI(new BasicButtonUI());
            setContentAreaFilled(false);
            setFocusable(false);
            setBorder(BorderFactory.createEtchedBorder());
            setBorderPainted(false);
            setRolloverEnabled(true);
            setIcon(MaterialIcon._Navigation.CLOSE.get(ICON_SIZE, mAlmondOptions.getIconColor()));

            addMouseListener(new MouseListener() {
                @Override
                public void mouseClicked(MouseEvent e) {
                }

                @Override
                public void mouseEntered(MouseEvent e) {
                    setIcon(MaterialIcon._Navigation.CANCEL.get(ICON_SIZE, mAlmondOptions.getIconColor()));
                }

                @Override
                public void mouseExited(MouseEvent e) {
                    setIcon(MaterialIcon._Navigation.CLOSE.get(ICON_SIZE, mAlmondOptions.getIconColor()));
                }

                @Override
                public void mousePressed(MouseEvent e) {
                    setIcon(MaterialIcon._Navigation.CANCEL.get(ICON_SIZE - 1, mAlmondOptions.getIconColor()));
                }

                @Override
                public void mouseReleased(MouseEvent e) {
                }
            });
        }
    }
}
