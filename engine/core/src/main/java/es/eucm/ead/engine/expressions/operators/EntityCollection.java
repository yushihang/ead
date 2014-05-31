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
package es.eucm.ead.engine.expressions.operators;

import ashley.core.Entity;
import ashley.core.Family;
import com.badlogic.gdx.utils.Array;
import com.badlogic.gdx.utils.Pools;
import es.eucm.ead.engine.GameLoop;
import es.eucm.ead.engine.expressions.ExpressionEvaluationException;
import es.eucm.ead.engine.expressions.Operation;
import es.eucm.ead.engine.variables.VarsContext;

import java.util.Iterator;

/**
 * Operation that returns a collection (array) of entities that match a
 * condition (boolean expression) given.
 * 
 * Example:
 * 
 * (collection btrue) // returns all entities (collection (eq group.x 20)) //
 * returns all entities located at position x=20 (collection sentity (not (eq
 * $entity $_this))) // returns all entities but this expression's owner.
 * 
 * Created by Javier Torrente on 29/05/14.
 */
public class EntityCollection extends Operation {

	public static final String DEFAULT_ITERATING_ENTITY_NAME = "entity";

	private GameLoop engine;

	public EntityCollection(GameLoop engine) {
		super(1, 2);
		this.engine = engine;
	}

	@Override
	public Object evaluate(VarsContext context, boolean lazy)
			throws ExpressionEvaluationException {
		if (lazy && isConstant) {
			return value;
		}

		// Create context to register entity var pointing to the variable being
		// iterated
		VarsContext tempContext = Pools.obtain(VarsContext.class);
		tempContext.setParent(context);
		String varName = DEFAULT_ITERATING_ENTITY_NAME;

		// If there are two operands, the first one should be the name of the
		// temp variable used to refer to the entity being iterated
		if (children.size() > 1) {
			Object varNameObject = first().evaluate(context, lazy);
			if (!(varNameObject instanceof String)) {
				throw new ExpressionEvaluationException(
						"Expected string first operand in " + getName(), this);
			} else {
				varName = (String) varNameObject;
			}
		}

		// Iterate through entities
		Array<Entity> entities = new Array<Entity>();
		Iterator<Entity> allEntities = engine
				.getEntitiesFor(Family.getFamilyFor()).values().iterator();
		try {
			while (allEntities.hasNext()) {
				Entity otherEntity = allEntities.next();
				// Set entity
				if (tempContext.hasVariable(varName)) {
					tempContext.setValue(varName, otherEntity);
				} else {
					tempContext.registerVariable(varName, otherEntity);
				}
				// Evaluate expression
				Object expResult = (children.size() > 1 ? second() : first())
						.evaluate(tempContext, false);
				if (!(expResult instanceof Boolean)) {
					throw new ExpressionEvaluationException(
							"Expected condition (boolean expression) operand in "
									+ getName()
									+ ". The expression did not return a boolean",
							this);
				}
				Boolean matches = (Boolean) expResult;
				if (matches) {
					entities.add(otherEntity);
				}
			}
		} catch (Exception e) {
			throw new ExpressionEvaluationException(
					"Bloody hell! The expression was not well formed and therefore it was not possible to evaluate the "
							+ getName() + " operation. ", this);
		}

		value = entities;

		// Free the temp context created to host "entity" var
		Pools.free(tempContext);

		return value;
	}
}
