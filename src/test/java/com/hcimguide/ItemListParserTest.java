package com.hcimguide;

import java.util.List;
import org.junit.Test;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

public class ItemListParserTest
{
	@Test
	public void parsesWithdrawLists()
	{
		List<ItemReq> w = ItemListParser.parse(
			"Withdraw: Coins, Air Runes, Mind Runes, Bread, Spade, Tinderbox, Air Talisman, Treasure Scroll (8 Inventory slots)");
		assertEquals(8, w.size());
		assertEquals("Coins", w.get(0).getName());
		assertEquals("Treasure Scroll", w.get(7).getName());
		assertEquals(1, w.get(0).getQuantity());

		List<ItemReq> w2 = ItemListParser.parse("Withdraw: Coins, Spade, Feather & Mysterious Orb (4 Inventory Slots)");
		assertEquals(4, w2.size());
		assertEquals("Mysterious Orb", w2.get(3).getName());
	}

	@Test
	public void parsesGatherSteps()
	{
		List<ItemReq> g = ItemListParser.parse("Collect 3x Logs [Tree Gnome Village]");
		assertEquals(3, g.get(0).getQuantity());
		assertEquals("Logs", g.get(0).getName());

		assertEquals("Beer", ItemListParser.parse("Take the Beer").get(0).getName());
		assertEquals("Leather Boots",
			ItemListParser.parse("Take Leather Boots off the table & wield").get(0).getName());
		assertEquals("Cheese",
			ItemListParser.parse("Collect Cheese from Aggie [Witch's House]").get(0).getName());
		assertEquals(5,
			ItemListParser.parse("Buy 5 Jugs of wine. Hop worlds and buy 10 in total.").get(0).getQuantity());
	}

	@Test
	public void parsesForAObtainedItem()
	{
		List<ItemReq> milk = ItemListParser.parse(
			"Milk the Dairy cow in the cow field for a Bucket of milk [Cook's Assistant]");
		assertEquals(1, milk.size());
		assertEquals("Bucket of milk", milk.get(0).getName());
		assertEquals(1, milk.get(0).getQuantity());

		List<ItemReq> groats = ItemListParser.parse(
			"Search the shelf on the south wall of the farmhouse for The Groats Principles [The Ides of Milk]");
		assertEquals(1, groats.size());
		assertEquals("Groats Principles", groats.get(0).getName());
	}

	@Test
	public void parsesKillLoot()
	{
		List<ItemReq> ranger = ItemListParser.parse(
			"Kill the Ranger Outlaw (level 6) with the Bronze dagger on stab and take his Shortbow & 80 Bronze arrows [Mudfoot's Big Adventure]");
		assertEquals(2, ranger.size());
		assertEquals("Shortbow", ranger.get(0).getName());
		assertEquals("Bronze arrows", ranger.get(1).getName());
		assertEquals(80, ranger.get(1).getQuantity());

		List<ItemReq> mage = ItemListParser.parse(
			"Wield the Shortbow and kill the Mage Outlaw south of the H.A.M Hideout, then take his Staff, Air runes & Mind runes [Mudfoot's Big Adventure]");
		assertEquals(3, mage.size());
		assertEquals("Staff", mage.get(0).getName());
		assertEquals("Air runes", mage.get(1).getName());
		assertEquals("Mind runes", mage.get(2).getName());
	}

	@Test
	public void ignoresNonItemSteps()
	{
		assertNull(ItemListParser.parse("Take Everything (Make sure you get a feather)"));
		assertNull(ItemListParser.parse("Talk to Father Aereck (3,1) [Restless Ghost]"));
		assertNull(ItemListParser.parse("Mine 10x Clay at varrock west mine"));
	}

	@Test
	public void nameEquivalenceHandlesPlurals()
	{
		assertTrue(ItemReq.namesEquivalent("Air Runes", "Air rune"));
		assertTrue(ItemReq.namesEquivalent("Coins", "Coins"));
		assertTrue(ItemReq.namesEquivalent("Jugs of wine", "Jug of wine"));
		assertFalse(ItemReq.namesEquivalent("Bread", "Bones"));
	}
	@Test
	public void bareTeleportRunesBecomeLawRuneBeforeInstructionFiltering()
	{
		List<ItemReq> bare = ItemListParser.parse("Withdraw: Teleport Runes");
		assertEquals(1, bare.size());
		assertEquals("Law rune", bare.get(0).getName());

		List<ItemReq> bank102 = ItemListParser.parse(
			"Withdraw: Teleport Runes, Tinderbox, Cat, 2 Waterskins");
		assertEquals("Law rune", bank102.get(0).getName());
		assertEquals("Tinderbox", bank102.get(1).getName());
		assertEquals(2, bank102.get(3).getQuantity());

		List<ItemReq> bank105 = ItemListParser.parse(
			"Withdraw: Coins, Teleport Runes, Hammer, 3 Planks, 90 Steel Nails, "
				+ "Rune Sword, 2 Compost, Maze Key, Ring of Charos");
		assertEquals("Coins", bank105.get(0).getName());
		assertEquals("Law rune", bank105.get(1).getName());

		List<ItemReq> bank111 = ItemListParser.parse(
			"Withdraw: Teleport Runes, Coins, Death Runes, Antipoison, "
				+ "Super Antipoison, Scrying Orb If 56/57/58 Magic; Wizard Mind Bomb, "
				+ "(9/10 Inventory Slots) + Food");
		assertEquals("Law rune", bank111.get(0).getName());
		assertEquals("Death rune", bank111.get(2).getName());

		assertNull(ItemListParser.parse("Teleport out"));
	}

	@Test
	public void inventoryFillDisplaysFullCountButUsesHalfThresholdWithOtherItems()
	{
		List<ItemReq> only = ItemListParser.parse("Withdraw: Inventory of Lobsters");
		assertEquals(1, only.size());
		assertTrue(only.get(0).isInventoryFill());
		assertEquals(28, only.get(0).getQuantity());
		assertEquals(28, only.get(0).getCompletionQuantity());

		List<ItemReq> mixed = ItemListParser.parse(
			"Withdraw: Inventory of Lobsters, Rope, Knife");
		assertEquals(3, mixed.size());
		assertEquals(28, mixed.get(0).getQuantity());
		assertEquals(14, mixed.get(0).getCompletionQuantity());
		assertEquals("28x Lobsters", mixed.get(0).toString());
		assertEquals(1, mixed.get(1).getCompletionQuantity());

		List<ItemReq> doubled = ItemListParser.parse(
			"Withdraw: 2x Inventory of Lobsters, Rope");
		assertEquals(56, doubled.get(0).getQuantity());
		assertEquals(28, doubled.get(0).getCompletionQuantity());
	}

}
