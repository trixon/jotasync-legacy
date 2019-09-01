/*
 * Copyright 2019 Patrik Karlström.
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

import com.dlsc.workbenchfx.model.WorkbenchModule;
import javafx.scene.Node;
import javafx.scene.control.ListView;
import javafx.scene.layout.BorderPane;
import javafx.scene.web.WebView;
import se.trixon.almond.util.Dict;
import se.trixon.almond.util.icons.material.MaterialIcon;

/**
 *
 * @author Patrik Karlström
 */
public class StartModule extends WorkbenchModule {

    private BorderPane mBorderPane;
    private ListView<String> mListView;
    private WebView mWebView;

    public StartModule() {
        super(Dict.HOME.toString(), MaterialIcon._Action.HOME.getImageView(MainApp.MODULE_ICON_SIZE).getImage());
        createUI();
    }

    @Override
    public Node activate() {
        return mBorderPane;
    }

    private void createUI() {
        mListView = new ListView<>();
        mListView.setPrefWidth(300);

        mWebView = new WebView();
        mBorderPane = new BorderPane(mWebView);
        mBorderPane.setLeft(mListView);
    }
}
