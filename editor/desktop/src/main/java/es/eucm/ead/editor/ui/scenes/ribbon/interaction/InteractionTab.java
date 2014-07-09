/**
 * eAdventure is a research project of the
 *    e-UCM research group.
 *
 *    Copyright 2005-2014 e-UCM research group.
 *
 *    You can access a list of all the contributors to eAdventure at:
 *          http://e-adventure.e-ucm.es/contributors
 *
 *    e-UCM is a research group of the Department of Software Engineering
 *          and Artificial Intelligence at the Complutense University of Madrid
 *          (School of Computer Science).
 *
 *          CL Profesor Jose Garcia Santesmases 9,
 *          28040 Madrid (Madrid), Spain.
 *
 *          For more info please visit:  <http://e-adventure.e-ucm.es> or
 *          <http://www.e-ucm.es>
 *
 * ****************************************************************************
 *
 *  This file is part of eAdventure
 *
 *      eAdventure is free software: you can redistribute it and/or modify
 *      it under the terms of the GNU Lesser General Public License as published by
 *      the Free Software Foundation, either version 3 of the License, or
 *      (at your option) any later version.
 *
 *      eAdventure is distributed in the hope that it will be useful,
 *      but WITHOUT ANY WARRANTY; without even the implied warranty of
 *      MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *      GNU Lesser General Public License for more details.
 *
 *      You should have received a copy of the GNU Lesser General Public License
 *      along with eAdventure.  If not, see <http://www.gnu.org/licenses/>.
 */
package es.eucm.ead.editor.ui.scenes.ribbon.interaction;

import com.badlogic.gdx.scenes.scene2d.ui.Skin;

import es.eucm.ead.editor.control.Controller;
import es.eucm.ead.editor.control.actions.model.scene.behaviors.AddBehavior;
import es.eucm.ead.editor.ui.WidgetsUtils;
import es.eucm.ead.editor.view.widgets.Separator;
import es.eucm.ead.editor.view.widgets.layouts.LinearLayout;
import es.eucm.ead.engine.I18N;
import es.eucm.ead.schema.components.behaviors.events.Init;
import es.eucm.ead.schema.components.behaviors.events.Key;
import es.eucm.ead.schema.components.behaviors.events.Timer;
import es.eucm.ead.schema.components.behaviors.events.Touch;

/**
 * Holds interactions buttons
 */
public class InteractionTab extends LinearLayout {

	public static final float DEFAULT_MARGIN = 5;

	public InteractionTab(Controller controller) {
		super(true);
		Skin skin = controller.getApplicationAssets().getSkin();
		I18N i18N = controller.getApplicationAssets().getI18N();
		add(WidgetsUtils.createIconWithLabel(controller, "gear48x48", skin,
				i18N.m("interaction.init"), i18N.m("interaction.init.tooltip"),
				AddBehavior.class, Init.class));
		add(WidgetsUtils.createIconWithLabel(controller, "touch48x48", skin,
				i18N.m("interaction.touch"),
				i18N.m("interaction.touch.tooltip"), AddBehavior.class,
				Touch.class));
		add(WidgetsUtils.createIconWithLabel(controller, "keyboard48x48", skin,
				i18N.m("interaction.keyboard"),
				i18N.m("interaction.keyboard.tooltip"), AddBehavior.class,
				Key.class));
		add(WidgetsUtils.createIconWithLabel(controller, "timer48x48", skin,
				i18N.m("interaction.timer"),
				i18N.m("interaction.timer.tooltip"), AddBehavior.class,
				Timer.class));

		add(new Separator(false, skin)).margin(DEFAULT_MARGIN);
		add(new BehaviorsSelector(controller));
	}
}
