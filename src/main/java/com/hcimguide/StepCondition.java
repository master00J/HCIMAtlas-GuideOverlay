package com.hcimguide;

import java.util.List;
import net.runelite.api.Quest;
import net.runelite.api.Skill;

/**
 * A game-state condition that can auto-complete a step.
 */
public class StepCondition
{
	public enum Type
	{
		QUEST_STARTED,
		QUEST_FINISHED,
		SKILL_LEVEL,
		ITEMS_IN_INVENTORY,
		TALK_TO_NPC
	}

	private final Type type;
	private final Quest quest;
	private final Skill skill;
	private final int level;
	private final List<ItemReq> items;
	private final String npcName;

	private StepCondition(Type type, Quest quest, Skill skill, int level, List<ItemReq> items, String npcName)
	{
		this.type = type;
		this.quest = quest;
		this.skill = skill;
		this.level = level;
		this.items = items;
		this.npcName = npcName;
	}

	static StepCondition questStarted(Quest quest)
	{
		return new StepCondition(Type.QUEST_STARTED, quest, null, 0, null, null);
	}

	static StepCondition questFinished(Quest quest)
	{
		return new StepCondition(Type.QUEST_FINISHED, quest, null, 0, null, null);
	}

	static StepCondition skillLevel(Skill skill, int level)
	{
		return new StepCondition(Type.SKILL_LEVEL, null, skill, level, null, null);
	}

	static StepCondition itemsInInventory(List<ItemReq> items)
	{
		return new StepCondition(Type.ITEMS_IN_INVENTORY, null, null, 0, items, null);
	}

	static StepCondition talkToNpc(String npcName)
	{
		return new StepCondition(Type.TALK_TO_NPC, null, null, 0, null, npcName);
	}

	public Type getType()
	{
		return type;
	}

	public Quest getQuest()
	{
		return quest;
	}

	public Skill getSkill()
	{
		return skill;
	}

	public int getLevel()
	{
		return level;
	}

	public List<ItemReq> getItems()
	{
		return items;
	}

	public String getNpcName()
	{
		return npcName;
	}

	public String describe()
	{
		switch (type)
		{
			case QUEST_STARTED:
				return "Auto-completes when \"" + quest.getName() + "\" is started";
			case QUEST_FINISHED:
				return "Auto-completes when \"" + quest.getName() + "\" is finished";
			case SKILL_LEVEL:
				return "Auto-completes at " + level + " " + skill.getName();
			case ITEMS_IN_INVENTORY:
				return "Auto-completes when all listed items are in your inventory";
			case TALK_TO_NPC:
				return "Auto-completes when you talk to " + npcName;
			default:
				return "";
		}
	}
}
