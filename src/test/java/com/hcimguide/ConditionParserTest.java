package com.hcimguide;

import net.runelite.api.Quest;
import net.runelite.api.Skill;
import org.junit.Test;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;

public class ConditionParserTest
{
	@Test
	public void detectsQuestStart()
	{
		StepCondition c = ConditionParser.parse("Talk to Morgan and start Vampyre Slayer (1)");
		assertEquals(StepCondition.Type.QUEST_STARTED, c.getType());
		assertEquals(Quest.VAMPYRE_SLAYER, c.getQuest());

		c = ConditionParser.parse("Head to King Roald and start Priest in Peril (1,1,3)");
		assertEquals(StepCondition.Type.QUEST_STARTED, c.getType());
		assertEquals(Quest.PRIEST_IN_PERIL, c.getQuest());

		c = ConditionParser.parse("Start A Porcine of Interest");
		assertEquals(StepCondition.Type.QUEST_STARTED, c.getType());
		assertEquals(Quest.A_PORCINE_OF_INTEREST, c.getQuest());

		c = ConditionParser.parse("Talk to the Cook in the Lumbridge Castle kitchen and start Cook's Assistant (keep the Pot and Bucket from Tutorial Island)");
		assertEquals(StepCondition.Type.QUEST_STARTED, c.getType());
		assertEquals(Quest.COOKS_ASSISTANT, c.getQuest());

		c = ConditionParser.parse("Talk to Captain Harlan outside the General Store north of the castle and start Mudfoot's Big Adventure (1)");
		assertEquals(StepCondition.Type.QUEST_STARTED, c.getType());
		assertEquals(Quest.MUDFOOTS_BIG_ADVENTURE, c.getQuest());

		c = ConditionParser.parse("Talk to Cassius at the pond south of the windmill and start The Ides of Milk");
		assertEquals(StepCondition.Type.QUEST_STARTED, c.getType());
		assertEquals(Quest.THE_IDES_OF_MILK, c.getQuest());
	}

	@Test
	public void detectsQuestCompletion()
	{
		StepCondition c = ConditionParser.parse("Head West to Port Sarim and talk to Veos to complete X Marks The Spot.");
		assertEquals(StepCondition.Type.QUEST_FINISHED, c.getType());
		assertEquals(Quest.X_MARKS_THE_SPOT, c.getQuest());
	}

	@Test
	public void ignoresPartialQuestSteps()
	{
		// "Complete 2nd Step of ..." must NOT auto-complete on quest finish
		assertNull(ConditionParser.parse("Complete 2nd Step of X Marks The Spot"));
		// not a quest at all
		assertNull(ConditionParser.parse("Complete 3 Floors of Stronghold of Security."));
	}

	@Test
	public void detectsSkillTargets()
	{
		StepCondition c = ConditionParser.parse("Train Draynor Agility to 5 Agility [Lumbridge Easy Diary]");
		assertEquals(StepCondition.Type.SKILL_LEVEL, c.getType());
		assertEquals(Skill.AGILITY, c.getSkill());
		assertEquals(5, c.getLevel());
	}

	@Test
	public void detectsItemSteps()
	{
		StepCondition c = ConditionParser.parse("Withdraw: Coins, Spade, Tinderbox (3 Inventory slots)");
		assertEquals(StepCondition.Type.ITEMS_IN_INVENTORY, c.getType());
		assertEquals(3, c.getItems().size());

		c = ConditionParser.parse("Milk the Dairy cow in the cow field for a Bucket of milk [Cook's Assistant]");
		assertEquals(StepCondition.Type.ITEMS_IN_INVENTORY, c.getType());
		assertEquals("Bucket of milk", c.getItems().get(0).getName());
	}

	@Test
	public void detectsTalkToNpc()
	{
		StepCondition c = ConditionParser.parse("Talk to Father Aereck (3,1) [Restless Ghost]");
		assertEquals(StepCondition.Type.TALK_TO_NPC, c.getType());
		assertEquals("Father Aereck", c.getNpcName());

		c = ConditionParser.parse("Talk to Mudfoot near the building south of the cow field, without a pet following you [Mudfoot's Big Adventure]");
		assertEquals(StepCondition.Type.TALK_TO_NPC, c.getType());
		assertEquals("Mudfoot", c.getNpcName());

		c = ConditionParser.parse("Talk to Mudfoot again at the chicken field south of the Lumbridge windmill [Mudfoot's Big Adventure]");
		assertEquals(StepCondition.Type.TALK_TO_NPC, c.getType());
		assertEquals("Mudfoot", c.getNpcName());

		c = ConditionParser.parse("Cross the River Lum and talk to Gillie Groats in the cow field just north of the gate [The Ides of Milk]");
		assertEquals(StepCondition.Type.TALK_TO_NPC, c.getType());
		assertEquals("Gillie Groats", c.getNpcName());

		c = ConditionParser.parse("Talk to Seth Groats in the farmhouse next to the chicken coop [The Ides of Milk]");
		assertEquals(StepCondition.Type.TALK_TO_NPC, c.getType());
		assertEquals("Seth Groats", c.getNpcName());

		c = ConditionParser.parse("Talk to Cassius to hand over The Groats Principles and get a Milk sample [The Ides of Milk]");
		assertEquals(StepCondition.Type.TALK_TO_NPC, c.getType());
		assertEquals("Cassius", c.getNpcName());

		c = ConditionParser.parse("Talk to Duke Horacio again and give him the Milk sample [The Ides of Milk]");
		assertEquals(StepCondition.Type.TALK_TO_NPC, c.getType());
		assertEquals("Duke Horacio", c.getNpcName());
	}

	@Test
	public void plainStepsHaveNoCondition()
	{
		assertNull(ConditionParser.parse("Head West towards Draynor hugging the fence to avoid the Jail Guards"));
		assertNull(ConditionParser.parse("Confront the outlaw walking outside the Lumbridge General Store [Mudfoot's Big Adventure]"));
		assertNull(ConditionParser.parse("Check playtime on Hans [Lumbridge Easy Diary]"));
	}
}
