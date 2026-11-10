package com.hcimguide;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import java.util.Collections;
import java.util.Map;
import net.runelite.api.coords.WorldPoint;

/**
 * A destination tile written into the guide file by its author:
 *
 * <pre>
 *   { "content": [{"text": "Talk to Cassius ..."}],
 *     "location": {"x": 3171, "y": 3277, "plane": 0, "label": "Cassius", "radius": 5} }
 * </pre>
 *
 * Text-based resolution has to guess: the spawn database knows one NPC per
 * name and nothing newer than its snapshot. An authored tile is exact, needs
 * no pin import and has no config-size limit. It replaces the planner's guess
 * for that step; a pin the player places by hand still wins over it.
 *
 * It only tells the player where to go. Whether arriving may tick the step is
 * still decided by {@link ArrivalCompletionPolicy} from the step text.
 */
final class AuthoredLocation
{
	static final int DEFAULT_RADIUS = 5;

	private final WorldPoint point;
	private final String label;
	private final int radius;

	private AuthoredLocation(WorldPoint point, String label, int radius)
	{
		this.point = point;
		this.label = label;
		this.radius = radius;
	}

	WorldPoint getPoint()
	{
		return point;
	}

	String getLabel()
	{
		return label;
	}

	int getRadius()
	{
		return radius;
	}

	/**
	 * Reads a step's "location" object. Anything malformed or out of the
	 * world's bounds yields null: a bad tile must never become an arrow.
	 */
	static AuthoredLocation parse(JsonObject step)
	{
		if (step == null)
		{
			return null;
		}
		JsonElement el = step.get("location");
		if (el == null || !el.isJsonObject())
		{
			return null;
		}
		JsonObject o = el.getAsJsonObject();
		Integer x = intOrNull(o, "x");
		Integer y = intOrNull(o, "y");
		Integer plane = o.has("plane") ? intOrNull(o, "plane") : Integer.valueOf(0);
		if (x == null || y == null || plane == null
			|| x <= 0 || x > 16383 || y <= 0 || y > 16383 || plane < 0 || plane > 3)
		{
			return null;
		}
		Integer r = o.has("radius") ? intOrNull(o, "radius") : null;
		int radius = r == null ? DEFAULT_RADIUS : Math.max(1, Math.min(20, r));
		String label = null;
		JsonElement l = o.get("label");
		if (l != null && l.isJsonPrimitive() && l.getAsJsonPrimitive().isString())
		{
			label = l.getAsString().trim();
			if (label.length() > 80)
			{
				label = label.substring(0, 80);
			}
		}
		return new AuthoredLocation(new WorldPoint(x, y, plane),
			label == null || label.isEmpty() ? "Destination" : label, radius);
	}

	private static Integer intOrNull(JsonObject o, String key)
	{
		JsonElement e = o.get(key);
		if (e == null || !e.isJsonPrimitive() || !e.getAsJsonPrimitive().isNumber())
		{
			return null;
		}
		double d = e.getAsDouble();
		return d == Math.rint(d) && Math.abs(d) < 1_000_000 ? Integer.valueOf((int) d) : null;
	}

	StepLocationPlan toPlan(String stepKey)
	{
		return new StepLocationPlan(stepKey, Collections.singletonList(
			StepLocationHint.authored("authored:" + stepKey, label, point, radius)));
	}

	/**
	 * Replaces the planner's plan with the authored tile for every step that
	 * carries one. Returns how many steps were overridden.
	 */
	static int apply(Guide guide, Map<String, StepLocationPlan> plans)
	{
		int applied = 0;
		if (guide == null || plans == null)
		{
			return applied;
		}
		for (GuideEpisode episode : guide.getEpisodes())
		{
			for (GuideBank bank : episode.getBanks())
			{
				for (GuideStep step : bank.getSteps())
				{
					AuthoredLocation authored = step.getAuthoredLocation();
					if (authored != null)
					{
						plans.put(step.getKey(), authored.toPlan(step.getKey()));
						applied++;
					}
				}
			}
		}
		return applied;
	}
}
