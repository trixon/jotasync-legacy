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

module se.trixon.jota {
    requires java.rmi;
    requires java.prefs;
    requires java.logging;
    requires javafx.baseEmpty;
    requires javafx.base;
//    requires javafx.controlsEmpty;

    requires javafx.controls;
//    requires javafx.graphicsEmpty;
    requires javafx.graphics;
//    requires javafx.swingEmpty;
    requires javafx.swing;
//    requires javafx.webEmpty;
    requires javafx.web;
//    requires javafx.mediaEmpty;
    requires javafx.media;
//    requires org.controlsfx.controls;
//    requires workbenchfx.core;
    requires com.dlsc.preferencesfx;
    requires com.dlsc.formsfx;
    requires javafx.fxmlEmpty;
    requires javafx.fxml;
    requires de.jensd.fx.fontawesomefx.commons;
    requires de.jensd.fx.fontawesomefx.fontawesome;
    requires org.slf4j;
    requires centerdevice.nsmenufx;
    requires commons.cli;
    requires org.apache.commons.io;
    requires almond.util;
    requires org.apache.commons.lang3;
    requires org.apache.commons.text;
    requires orange.extensions;
    requires gson;
    requires cron4j;
    requires org.slf4j.simple;
}
