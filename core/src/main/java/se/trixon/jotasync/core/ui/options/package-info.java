/*
 * Copyright 2020 Patrik Karlström.
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
@OptionsPanelController.ContainerRegistration(id = "JotaSync", position = 0, categoryName = "#OptionsCategory_Name_JotaSync", iconBase = "se/trixon/jotasync/core/ui/options/jotasync_options.png", keywords = "#OptionsCategory_Keywords_JotaSync", keywordsCategory = "JotaSync")
@NbBundle.Messages(value = {"OptionsCategory_Name_JotaSync=JotaSync", "OptionsCategory_Keywords_JotaSync=client server"})
package se.trixon.jotasync.core.ui.options;

import org.netbeans.spi.options.OptionsPanelController;
import org.openide.util.NbBundle;
