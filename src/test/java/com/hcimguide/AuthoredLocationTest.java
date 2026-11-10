package com.hcimguide;

import java.util.HashMap;
import java.util.Map;
import net.runelite.api.coords.WorldPoint;
import org.junit.Test;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

public class AuthoredLocationTest
{
	private static String guide(String... steps)
	{
		return "{\"title\":\"t\",\"chapters\":[{\"title\":\"Episode 1\",\"sections\":[{\"title\":\"Bank 1\",\"steps\":["
			+ String.join(",", steps) + "]}]}]}";
	}

	private static String step(String text, String location)
	{
		return "{\"content\":[{\"text\":\"" + text + "\"}]" + (location == null ? "" : ",\"location\":" + location) + "}";
	}

	private static GuideBank bank(String json)
	{
		return new JsonGuideParser().parse(json).getEpisodes().get(0).getBanks().get(0);
	}

	@Test
	public void readsTheTileWithoutChangingTheStepKey()
	{
		String text = "Talk to Cassius at the pond south of the windmill and start The Ides of Milk";
		GuideStep plain = bank(guide(step(text, null))).getSteps().get(0);
		GuideStep located = bank(guide(step(text,
			"{\"x\":3171,\"y\":3277,\"plane\":0,\"label\":\"Cassius\"}"))).getSteps().get(0);

		assertNull(plain.getAuthoredLocation());
		assertNotNull(located.getAuthoredLocation());
		assertEquals(new WorldPoint(3171, 3277, 0), located.getAuthoredLocation().getPoint());
		assertEquals("Cassius", located.getAuthoredLocation().getLabel());
		assertEquals(AuthoredLocation.DEFAULT_RADIUS, located.getAuthoredLocation().getRadius());
		// saved progress is keyed on the text only
		assertEquals(plain.getKey(), located.getKey());
		assertEquals(plain.getText(), located.getText());
	}

	@Test
	public void everySentenceOfASplitParagraphGetsTheTile()
	{
		GuideBank b = bank(guide(step("Talk to Hans in the Lumbridge Castle courtyard. Check your playtime with him afterwards.",
			"{\"x\":3222,\"y\":3218,\"plane\":0}")));
		assertEquals(2, b.getSteps().size());
		for (GuideStep s : b.getSteps())
		{
			assertEquals(new WorldPoint(3222, 3218, 0), s.getAuthoredLocation().getPoint());
		}
	}

	@Test
	public void rejectsMalformedOrOutOfBoundsTiles()
	{
		String[] bad = {
			"{\"x\":0,\"y\":3277,\"plane\":0}",
			"{\"x\":3171,\"y\":99999,\"plane\":0}",
			"{\"x\":3171,\"y\":3277,\"plane\":4}",
			"{\"x\":\"3171\",\"y\":3277}",
			"{\"x\":3171.5,\"y\":3277}",
			"{\"y\":3277}",
			"[3171,3277,0]",
			"\"3171,3277,0\"",
			"null",
		};
		for (String location : bad)
		{
			assertNull(location, bank(guide(step("Go somewhere", location))).getSteps().get(0).getAuthoredLocation());
		}
		// plane defaults to 0, radius is clamped, long labels are cut
		AuthoredLocation a = bank(guide(step("Go somewhere",
			"{\"x\":3171,\"y\":3277,\"radius\":500}"))).getSteps().get(0).getAuthoredLocation();
		assertEquals(0, a.getPoint().getPlane());
		assertEquals(20, a.getRadius());
		assertEquals("Destination", a.getLabel());
	}

	@Test
	public void authoredTileReplacesThePlannerGuessOnlyForItsStep()
	{
		Guide g = new JsonGuideParser().parse(guide(
			step("Talk to Betty", "{\"x\":1504,\"y\":3415,\"plane\":0,\"label\":\"Land's End\"}"),
			step("Talk to Hans", null)));
		GuideBank b = g.getEpisodes().get(0).getBanks().get(0);
		String betty = b.getSteps().get(0).getKey();
		String hans = b.getSteps().get(1).getKey();

		Map<String, StepLocationPlan> plans = new HashMap<>();
		// what name-based lookup would produce: the other Betty, in Port Sarim
		plans.put(betty, new StepLocationPlan(betty, java.util.Collections.singletonList(
			new StepLocationHint("Betty", new WorldPoint(3012, 3259, 0), false))));
		StepLocationPlan hansPlan = new StepLocationPlan(hans, java.util.Collections.singletonList(
			new StepLocationHint("Hans", new WorldPoint(3207, 3233, 0), false)));
		plans.put(hans, hansPlan);

		assertEquals(1, AuthoredLocation.apply(g, plans));

		StepLocationPlan plan = plans.get(betty);
		assertEquals(1, plan.size());
		StepLocationHint hint = plan.get(0);
		assertEquals(new WorldPoint(1504, 3415, 0), hint.getPoint());
		assertEquals(LocationSource.AUTHORED_WAYPOINT, hint.getSource());
		assertEquals(LocationConfidence.MANUAL, hint.getConfidence());
		assertFalse(hint.isInferred());
		assertTrue(plans.get(hans) == hansPlan);
	}
}
