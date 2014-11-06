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
package es.eucm.ead.editor.view.builders.scene;

import com.badlogic.gdx.scenes.scene2d.Actor;
import com.badlogic.gdx.scenes.scene2d.InputEvent;
import com.badlogic.gdx.scenes.scene2d.ui.Button;
import com.badlogic.gdx.scenes.scene2d.ui.Image;
import com.badlogic.gdx.scenes.scene2d.ui.Skin;
import com.badlogic.gdx.scenes.scene2d.utils.Align;
import com.badlogic.gdx.scenes.scene2d.utils.ClickListener;

import es.eucm.ead.editor.control.Controller;
import es.eucm.ead.editor.control.Selection;
import es.eucm.ead.editor.control.actions.editor.AddLabel;
import es.eucm.ead.editor.control.actions.editor.AddPaintedElement;
import es.eucm.ead.editor.control.actions.editor.ChangeView;
import es.eucm.ead.editor.control.actions.editor.Copy;
import es.eucm.ead.editor.control.actions.editor.Paste;
import es.eucm.ead.editor.control.actions.editor.Redo;
import es.eucm.ead.editor.control.actions.editor.ShowToast;
import es.eucm.ead.editor.control.actions.editor.Undo;
import es.eucm.ead.editor.control.actions.model.AddInteractiveZone;
import es.eucm.ead.editor.control.actions.model.GroupSelection;
import es.eucm.ead.editor.control.actions.model.RemoveSelectionFromScene;
import es.eucm.ead.editor.control.actions.model.SetSelection;
import es.eucm.ead.editor.control.actions.model.TakePicture;
import es.eucm.ead.editor.control.actions.model.UngroupSelection;
import es.eucm.ead.editor.control.actions.model.scene.ReorderSelection;
import es.eucm.ead.editor.control.actions.model.scene.transform.MirrorSelection;
import es.eucm.ead.editor.model.Model.SelectionListener;
import es.eucm.ead.editor.model.events.SelectionEvent;
import es.eucm.ead.editor.view.SkinConstants;
import es.eucm.ead.editor.view.builders.ViewBuilder;
import es.eucm.ead.editor.view.builders.project.ProjectView;
import es.eucm.ead.editor.view.widgets.AbstractWidget;
import es.eucm.ead.editor.view.widgets.ContextMenu;
import es.eucm.ead.editor.view.widgets.IconButton;
import es.eucm.ead.editor.view.widgets.MultiToolbar;
import es.eucm.ead.editor.view.widgets.WidgetBuilder;
import es.eucm.ead.editor.view.widgets.baseview.BaseView;
import es.eucm.ead.editor.view.widgets.draw.BrushStrokesPicker;
import es.eucm.ead.editor.view.widgets.draw.BrushStrokesPicker.SizeEvent;
import es.eucm.ead.editor.view.widgets.draw.BrushStrokesPicker.SizeListener;
import es.eucm.ead.editor.view.widgets.draw.SlideColorPicker.ColorEvent;
import es.eucm.ead.editor.view.widgets.draw.SlideColorPicker.ColorListener;
import es.eucm.ead.editor.view.builders.scene.draw.BrushStrokes;
import es.eucm.ead.editor.view.builders.scene.draw.BrushStrokes.ModeEvent;
import es.eucm.ead.editor.view.builders.scene.draw.BrushStrokes.ModeListener;
import es.eucm.ead.editor.view.widgets.layouts.LinearLayout;
import es.eucm.ead.engine.I18N;

public class SceneView implements ViewBuilder {

	public static final int INSERT = 0, TRANSFORM = 1, PAINT = 2;

	public enum Mode {
		COMPOSE, FX, PLAY, DRAW,
	}

	public SceneView() {
	}

	private Controller controller;

	private Mode mode;

	private BaseView view;

	private MultiToolbar toolbar;

	private SceneEditor sceneEditor;

	private BrushStrokes brushStrokes;

	@Override
	public void initialize(Controller controller) {
		this.controller = controller;
		Skin skin = controller.getApplicationAssets().getSkin();
		I18N i18N = controller.getApplicationAssets().getI18N();

		AbstractWidget container = new AbstractWidget();

		sceneEditor = new SceneEditor(controller);
		sceneEditor.setFillParent(true);
		brushStrokes = new BrushStrokes(container, controller);

		view = new BaseView(skin);

		view.setToolbar(toolbar = buildToolbar(skin, i18N));
		view.setNavigation(buildNavigation(skin, i18N));

		container.addActor(sceneEditor);

		view.setContent(container);

		mode = Mode.COMPOSE;
	}

	@Override
	public Actor getView(Object... args) {
		sceneEditor.prepare();
		return view;
	}

	@Override
	public void release(Controller controller) {
		sceneEditor.release();
		view.invalidate();
	}

	public void setMode(Mode mode) {
		this.mode = mode;
		Object[] selection = controller.getModel().getSelection().getCurrent();
		switch (mode) {
		case COMPOSE:
			if (selection.length == 0) {
				toolbar.setSelectedToolbar(INSERT);
			} else {
				toolbar.setSelectedToolbar(TRANSFORM);
			}
			break;
		case DRAW:
			toolbar.setSelectedToolbar(PAINT);
			brushStrokes.show();
			break;
		}
	}

	private MultiToolbar buildToolbar(Skin skin, I18N i18N) {
		final MultiToolbar toolbar = new MultiToolbar(skin);
		toolbar.addToolbars(buildComposeToolbar(skin, i18N),
				buildTransformToolbar(skin, i18N), buildDrawToolbar(skin, i18N));

		controller.getModel().addSelectionListener(new SelectionListener() {
			@Override
			public boolean listenToContext(String contextId) {
				return Selection.SCENE_ELEMENT.equals(contextId);
			}

			@Override
			public void modelChanged(SelectionEvent event) {
				setMode(mode);
			}
		});
		return toolbar;
	}

	private LinearLayout buildComposeToolbar(Skin skin, I18N i18N) {
		LinearLayout compose = new LinearLayout(true);
		IconButton navigation = WidgetBuilder.toolbarIcon(skin,
				SkinConstants.IC_MENU);
		navigation.addListener(new ClickListener() {
			@Override
			public void clicked(InputEvent event, float x, float y) {
				view.toggleNavigation();
			}
		});
		compose.add(navigation);

		IconButton mode = WidgetBuilder.icon(skin, SkinConstants.IC_COMPOSE,
				SkinConstants.STYLE_DROP_DOWN);
		WidgetBuilder.launchContextMenu(mode, buildModeContextMenu(skin, i18N));

		compose.add(mode);
		compose.add(WidgetBuilder.toolbarIcon(skin, SkinConstants.IC_UNDO,
				Undo.class));
		compose.add(WidgetBuilder.toolbarIcon(skin, SkinConstants.IC_REDO,
				Redo.class));

		compose.addSpace();

		compose.add(WidgetBuilder.toolbarIcon(skin, SkinConstants.IC_PASTE,
				Paste.class));

		compose.add(WidgetBuilder.toolbarIconWithMenu(skin,
				SkinConstants.IC_ADD, buildInsertContextMenu(skin, i18N)));
		return compose;
	}

	private LinearLayout buildTransformToolbar(Skin skin, I18N i18N) {
		LinearLayout transform = new LinearLayout(true);
		transform.add(WidgetBuilder.toolbarIcon(skin, SkinConstants.IC_CHECK,
				SetSelection.class, Selection.EDITED_GROUP,
				Selection.SCENE_ELEMENT));
		transform.add(WidgetBuilder.toolbarIcon(skin, SkinConstants.IC_UNDO,
				Undo.class));
		transform.add(WidgetBuilder.toolbarIcon(skin, SkinConstants.IC_REDO,
				Redo.class));

		transform.addSpace();

		transform.add(WidgetBuilder.toolbarIcon(skin, SkinConstants.IC_GROUP,
				GroupSelection.class));
		transform.add(WidgetBuilder.toolbarIcon(skin, SkinConstants.IC_UNGROUP,
				UngroupSelection.class));

		transform.add(WidgetBuilder.toolbarIcon(skin, SkinConstants.IC_TO_BACK,
				ReorderSelection.class, ReorderSelection.Type.TO_BACK));

		transform.add(WidgetBuilder.toolbarIcon(skin,
				SkinConstants.IC_TO_FRONT, ReorderSelection.class,
				ReorderSelection.Type.TO_FRONT));

		transform.add(WidgetBuilder.toolbarIconWithMenu(skin,
				SkinConstants.IC_MORE, buildTransformContextMenu(skin, i18N)));
		return transform;
	}

	private ContextMenu buildInsertContextMenu(Skin skin, I18N i18n) {
		String style = SkinConstants.STYLE_CONTEXT;

		Button picture = WidgetBuilder.button(skin, SkinConstants.IC_CAMERA,
				i18n.m("picture"), style, TakePicture.class);

		Button text = WidgetBuilder.button(skin, SkinConstants.IC_TEXT,
				i18n.m("text"), style, AddLabel.class);

		Button zone = WidgetBuilder.button(skin, SkinConstants.IC_ZONE,
				i18n.m("interactive.zone"), style, AddInteractiveZone.class);

		Button paint = WidgetBuilder.button(skin, SkinConstants.IC_BRUSH,
				i18n.m("drawing"), style);
		paint.addListener(new ClickListener() {

			@Override
			public void clicked(InputEvent event, float x, float y) {
				setMode(Mode.DRAW);
			}
		});

		ContextMenu contextMenu = WidgetBuilder.iconLabelContextPanel(skin,
				picture, text, zone, paint);

		contextMenu.pack();
		contextMenu.setOriginX(contextMenu.getWidth());
		contextMenu.setOriginY(contextMenu.getHeight());
		return contextMenu;
	}

	private ContextMenu buildTransformContextMenu(Skin skin, I18N i18n) {
		String style = SkinConstants.STYLE_CONTEXT;

		Button copy = WidgetBuilder.button(skin, SkinConstants.IC_COPY,
				i18n.m("copy"), style, Copy.class);
		WidgetBuilder.actionOnClick(copy, ShowToast.class, i18n.m("copied"));
		WidgetBuilder.actionOnClick(copy, SetSelection.class,
				Selection.EDITED_GROUP, Selection.SCENE_ELEMENT);

		Button front = WidgetBuilder.button(skin,
				SkinConstants.IC_BRING_TO_FRONT, i18n.m("bring.to.front"),
				style, ReorderSelection.class,
				ReorderSelection.Type.BRING_TO_FRONT);

		Button back = WidgetBuilder.button(skin, SkinConstants.IC_SEND_TO_BACK,
				i18n.m("send.to.back"), style, ReorderSelection.class,
				ReorderSelection.Type.SEND_TO_BACK);

		Button vertical = WidgetBuilder.button(skin,
				SkinConstants.IC_MIRROR_VERTICAL, i18n.m("mirror.vertical"),
				style, MirrorSelection.class, MirrorSelection.Type.HORIZONTAL);

		Button horizontal = WidgetBuilder.button(skin,
				SkinConstants.IC_MIRROR_HORIZONTAL,
				i18n.m("mirror.horizontal"), style, MirrorSelection.class,
				MirrorSelection.Type.VERTICAL);

		Button delete = WidgetBuilder.button(skin, SkinConstants.IC_DELETE,
				i18n.m("delete"), style, RemoveSelectionFromScene.class);

		ContextMenu contextMenu = WidgetBuilder.iconLabelContextPanel(skin,
				copy, front, back, vertical, horizontal, delete);

		contextMenu.pack();
		contextMenu.setOriginX(contextMenu.getWidth());
		contextMenu.setOriginY(contextMenu.getHeight());
		return contextMenu;
	}

	private Actor buildModeContextMenu(Skin skin, I18N i18N) {
		ContextMenu contextMenu = WidgetBuilder.iconLabelContextPanel(skin,
				SkinConstants.IC_COMPOSE, i18N.m("compose"),
				SkinConstants.IC_FX, i18N.m("fx"), SkinConstants.IC_PLAY,
				i18N.m("test"));
		contextMenu.pack();
		contextMenu.setOriginY(contextMenu.getHeight());
		contextMenu.addListener(new ClickListener() {
			@Override
			public void clicked(InputEvent event, float x, float y) {
				String name = event.getTarget().getName();
				if (SkinConstants.IC_COMPOSE.equals(name)) {
					setMode(Mode.COMPOSE);
				} else if (SkinConstants.IC_FX.equals(name)) {
					setMode(Mode.FX);
				} else if (SkinConstants.IC_PLAY.equals(name)) {
					setMode(Mode.PLAY);
				}
			}
		});
		return contextMenu;
	}

	private LinearLayout buildNavigation(Skin skin, I18N i18N) {
		LinearLayout navigation = new LinearLayout(false,
				skin.getDrawable(SkinConstants.DRAWABLE_PAGE_LEFT));
		navigation.add(WidgetBuilder.button(skin, SkinConstants.IC_HOME,
				i18N.m("project"), SkinConstants.STYLE_CONTEXT,
				ChangeView.class, ProjectView.class));
		navigation.addSpace();
		return navigation;
	}

	private LinearLayout buildDrawToolbar(final Skin skin, I18N i18N) {
		LinearLayout draw = new LinearLayout(true);
		IconButton save = WidgetBuilder.toolbarIcon(skin,
				SkinConstants.IC_CHECK);

		draw.add(save);

		IconButton mode = WidgetBuilder.icon(skin, SkinConstants.IC_BRUSH,
				SkinConstants.STYLE_DROP_DOWN);
		final Image modeIcon = mode.getIcon();
		WidgetBuilder.launchContextMenu(mode,
				buildDrawModeContextMenu(skin, i18N));

		draw.add(mode);

		IconButton picker = WidgetBuilder.icon(skin, SkinConstants.IC_CIRCLE,
				SkinConstants.STYLE_DROP_DOWN);
		WidgetBuilder.launchContextMenu(picker,
				buildBrushStrokesColorPicker(skin, picker.getIcon()));
		brushStrokes.addListener(new ModeListener() {
			@Override
			public void modeChanged(ModeEvent event) {
				boolean drawing = event.getMode() == BrushStrokes.Mode.DRAW;
				modeIcon.setDrawable(skin, drawing ? SkinConstants.IC_BRUSH
						: SkinConstants.IC_RUBBER);
			}
		});

		draw.add(picker);

		draw.add(WidgetBuilder.toolbarIcon(skin, SkinConstants.IC_UNDO,
				Undo.class));
		draw.add(WidgetBuilder.toolbarIcon(skin, SkinConstants.IC_REDO,
				Redo.class));

		draw.addSpace();

		IconButton close = WidgetBuilder.toolbarIcon(skin,
				SkinConstants.IC_CLOSE);

		draw.add(close);

		draw.addListener(new ClickListener() {
			@Override
			public void clicked(InputEvent event, float x, float y) {
				String name = event.getTarget().getName();
				if (SkinConstants.IC_CHECK.equals(name)) {
					setMode(Mode.COMPOSE);
					controller.action(AddPaintedElement.class, brushStrokes);
					brushStrokes.hide(false);
				} else if (SkinConstants.IC_CLOSE.equals(name)) {
					setMode(Mode.COMPOSE);
					brushStrokes.hide(true);
				}
			}
		});
		return draw;
	}

	private Actor buildBrushStrokesColorPicker(Skin skin, final Image pickerIcon) {
		BrushStrokesPicker colorPickerPanel = new BrushStrokesPicker(skin);
		colorPickerPanel.pack();
		colorPickerPanel.setOriginY(colorPickerPanel.getHeight());
		colorPickerPanel.addListener(new ColorListener() {
			@Override
			public void colorChanged(ColorEvent event) {
				brushStrokes.setColor(event.getColor());
			}
		});
		colorPickerPanel.addListener(new SizeListener() {
			@Override
			public void sizeChanged(SizeEvent event) {
				brushStrokes.setRadius(event.getCompletion());
			}
		});
		pickerIcon.setOrigin(Align.center);
		colorPickerPanel.addListener(new ColorListener() {
			@Override
			public void colorChanged(ColorEvent event) {
				pickerIcon.setColor(event.getColor());
				brushStrokes.setMode(BrushStrokes.Mode.DRAW);
			}
		});
		colorPickerPanel.addListener(new SizeListener() {
			@Override
			public void sizeChanged(SizeEvent event) {
				pickerIcon.setScale(event.getCompletion());
			}
		});
		colorPickerPanel.setPickedColor(brushStrokes.getInitialColor());
		colorPickerPanel.setSizeValue(brushStrokes.getInitialRadius()
				/ brushStrokes.getMaxRadius());
		return colorPickerPanel;
	}

	private Actor buildDrawModeContextMenu(Skin skin, I18N i18N) {
		ContextMenu contextMenu = WidgetBuilder.iconLabelContextPanel(skin,
				SkinConstants.IC_BRUSH, i18N.m("paint"),
				SkinConstants.IC_RUBBER, i18N.m("erase"));
		contextMenu.pack();
		contextMenu.setOriginY(contextMenu.getHeight());
		contextMenu.addListener(new ClickListener() {
			@Override
			public void clicked(InputEvent event, float x, float y) {
				String name = event.getTarget().getName();
				if (SkinConstants.IC_BRUSH.equals(name)) {
					brushStrokes.setMode(BrushStrokes.Mode.DRAW);
				} else if (SkinConstants.IC_RUBBER.equals(name)) {
					brushStrokes.setMode(BrushStrokes.Mode.ERASE);
				}
			}
		});
		return contextMenu;
	}
}