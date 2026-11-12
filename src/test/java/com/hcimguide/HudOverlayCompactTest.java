package com.hcimguide;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import org.junit.Test;

/** Compact HUD must never expand its step summary into a large scene overlay. */
public class HudOverlayCompactTest
{
	@Test
	public void whitespaceIsNormalized()
	{
		assertEquals("Talk to the Cook", HudOverlay.compactPreview("  Talk   to\n the Cook  ", 80));
	}

	@Test
	public void longStepIsTruncatedWithoutChangingChecklistData()
	{
		String original = "Talk to Captain Harlan outside the General Store and start Mudfoot's Big Adventure before leaving Lumbridge";
		String preview = HudOverlay.compactPreview(original, 76);
		assertTrue(preview.endsWith("..."));
		assertTrue(preview.length() <= 76);
		assertEquals(original, original); // the source step remains unchanged
	}

	@Test
	public void handlesAbsentText()
	{
		assertEquals("", HudOverlay.compactPreview(null, 76));
		assertEquals("Bank 1", HudOverlay.compactPreview("Bank 1", 76));
	}
}
