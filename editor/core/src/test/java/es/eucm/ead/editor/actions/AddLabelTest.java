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
package es.eucm.ead.editor.actions;

import static org.junit.Assert.assertEquals;

import org.junit.Test;

import es.eucm.ead.editor.control.Selection;
import es.eucm.ead.editor.control.actions.model.AddLabel;
import es.eucm.ead.editor.control.actions.model.SetSelection;
import es.eucm.ead.schema.components.controls.Label;
import es.eucm.ead.schema.entities.ModelEntity;

public class AddLabelTest extends ActionTest {

	@Test
	public void testAddLabelText() {
		// Add new label in the edited scene
		ModelEntity scene = new ModelEntity();
		controller.action(SetSelection.class, null, Selection.SCENE, scene);
		int size = scene.getChildren().size;
		controller.action(AddLabel.class);
		assertEquals(scene.getChildren().size, size + 1);

		// Add a label to the entity
		ModelEntity element = new ModelEntity();
		Label label = new Label();
		controller.action(SetSelection.class, null, Selection.SCENE_ELEMENT,
				element);
		int sizeElement = element.getChildren().size;
		controller.action(AddLabel.class, label);
		assertEquals(element.getChildren().size, sizeElement + 1);
	}
}