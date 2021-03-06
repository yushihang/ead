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
package es.eucm.ead.editor.ui.scenes.map;

import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

import com.badlogic.gdx.scenes.scene2d.ui.Button;
import com.badlogic.gdx.scenes.scene2d.ui.ButtonGroup;
import com.badlogic.gdx.scenes.scene2d.ui.CheckBox;
import com.badlogic.gdx.scenes.scene2d.ui.Skin;
import com.badlogic.gdx.scenes.scene2d.ui.Table;
import com.badlogic.gdx.utils.Array;
import com.badlogic.gdx.utils.Pools;

import es.eucm.ead.editor.control.Controller;
import es.eucm.ead.editor.control.Selection;
import es.eucm.ead.editor.control.actions.model.SetSelection;
import es.eucm.ead.editor.model.Model;
import es.eucm.ead.editor.model.Model.ModelListener;
import es.eucm.ead.editor.model.Model.Resource;
import es.eucm.ead.editor.model.Model.SelectionListener;
import es.eucm.ead.editor.model.Q;
import es.eucm.ead.editor.model.events.ResourceEvent;
import es.eucm.ead.editor.model.events.SelectionEvent;
import es.eucm.ead.editor.view.listeners.SceneDocumentationListener;
import es.eucm.ead.editor.view.widgets.dragndrop.focus.FocusItemList.FocusEvent;
import es.eucm.ead.editor.view.widgets.dragndrop.focus.FocusItemList.FocusListener;
import es.eucm.ead.engine.assets.Assets;
import es.eucm.ead.schema.entities.ModelEntity;
import es.eucm.ead.schemax.entities.ResourceCategory;

/**
 * A widget used to display all the available scenes in the {@link Model} in a
 * list.
 * 
 */
public class SceneList extends Table {

	private Skin skin;

	private Controller controller;

	private ButtonGroup buttonGroup;

	private SceneDocumentationListener nameListener;

	private ModelListener<ResourceEvent> scenesChangedListener;

	public SceneList(Controller control) {
		this.controller = control;
		buttonGroup = new ButtonGroup();
		Assets assets = controller.getApplicationAssets();
		skin = assets.getSkin();

		addListener(new FocusListener() {

			@Override
			public void focusChanged(FocusEvent event) {
				controller
						.action(SetSelection.class, Selection.SCENE_MAP,
								Selection.SCENE,
								((SceneButton) event.getActor()).scene);
			}
		});

		nameListener = new SceneDocumentationListener(controller) {

			@Override
			public void nameChanged(String name) {
				((CheckBox) buttonGroup.getChecked()).setText(name);
			}
		};

		controller.getModel().addSelectionListener(new SelectionListener() {

			@Override
			public void modelChanged(SelectionEvent event) {
				if (event.getType() == SelectionEvent.Type.FOCUSED) {
					Object object = event.getSelection()[0];
					if (object instanceof ModelEntity) {
						ModelEntity scene = (ModelEntity) object;
						Array<Button> buttons = buttonGroup.getButtons();
						for (Button button : buttons) {
							if (((SceneButton) button).scene == scene) {
								button.setChecked(true);
								break;
							}
						}
						nameListener.setUp(scene);
					}
				}
			}

			@Override
			public boolean listenToContext(String contextId) {
				return Selection.SCENE.equals(contextId);
			}
		});

		scenesChangedListener = new ScenesChangesListener();
	}

	public void prepare() {

		Model model = controller.getModel();

		Map<String, Resource> resources = model
				.getResources(ResourceCategory.SCENE);
		model.addResourceListener(scenesChangedListener);

		Set<Entry<String, Resource>> entrySet = resources.entrySet();
		buttonGroup.setMinCheckCount(0);

		for (java.util.Map.Entry<String, Resource> entry : resources.entrySet()) {
			addSceneBox((ModelEntity) entry.getValue().getObject(),
					entry.getKey());
		}
		buttonGroup.setMinCheckCount(1);
	}

	private void addSceneBox(ModelEntity scene, String sceneId) {
		CheckBox sceneBox = new SceneButton(controller, scene, sceneId, skin);
		add(sceneBox).left().expandX();
		buttonGroup.add(sceneBox);
		row();
	}

	public void release() {
		clear();
		nameListener.remove();
		buttonGroup.getButtons().clear();
		buttonGroup.getAllChecked().clear();
		controller.getModel().removeResourceListener(scenesChangedListener);
	}

	private static class SceneButton extends CheckBox {

		private ModelEntity scene;

		public SceneButton(Controller controller, ModelEntity scene, String id,
				Skin skin) {
			super(Q.getTitle(scene), skin);

			this.scene = scene;
			Object modelEntity = controller.getModel().getSelection()
					.getSingle(Selection.SCENE);
			setChecked(modelEntity == scene);
		}

		@Override
		public void setChecked(boolean isChecked) {
			boolean wasChecked = isChecked();
			super.setChecked(isChecked);
			if (!wasChecked && isChecked) {
				fireFocus();
			}
		}

		/**
		 * Fires that this has gained focus
		 */
		private void fireFocus() {
			FocusEvent dropEvent = Pools.obtain(FocusEvent.class);
			dropEvent.setActor(this);
			fire(dropEvent);
			Pools.free(dropEvent);
		}
	}

	private class ScenesChangesListener implements ModelListener<ResourceEvent> {

		@Override
		public void modelChanged(ResourceEvent event) {
			ResourceCategory category = event.getCategory();
			if (category == ResourceCategory.SCENE) {
				Object resource = event.getResource();
				if (resource instanceof ModelEntity) {
					String id = event.getId();
					switch (event.getType()) {
					case ADDED:
						addSceneBox((ModelEntity) resource, id);
						break;
					case REMOVED:
						Array<Button> buttons = buttonGroup.getButtons();
						for (Button button : buttons) {
							if (((SceneButton) button).scene == resource) {
								buttonGroup.remove(button);
								removeActor(button);
								break;
							}
						}
						break;
					}
				}
			}
		}
	}
}
