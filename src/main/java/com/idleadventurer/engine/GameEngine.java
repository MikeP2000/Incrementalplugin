package com.idleadventurer.engine;

import java.util.Arrays;
import java.util.Random;

/**
 * Advances the game one tick at a time. Knows nothing about RuneLite or Swing so it can be
 * tested on its own. Persistent progress lives in {@link GameState}; the fight in progress
 * (hit points, cooldowns) lives here and is rebuilt on startup.
 */
public class GameEngine
{
	/** Length of one game tick, matching OSRS. */
	public static final int TICK_MILLIS = 600;

	static final int WALK_TICKS = 3;
	static final int RESPAWN_TICKS = 5;
	static final int PLAYER_ATTACK_TICKS = 4;
	/** Eating in a fight delays the next swing, as in OSRS. */
	static final int EAT_DELAY_TICKS = 3;
	/** How long the "zone unlocked" notice stays up (about 12 seconds). */
	static final int UNLOCK_NOTICE_TICKS = 20;
	/** One hit point is restored this often, so a long fight is not a coin flip. */
	static final int REGEN_TICKS = 10;
	/** Multiplier on OSRS XP rates; 1 matches OSRS (4 XP per damage, 1.33 to Hitpoints). */
	static final double XP_RATE = 1;
	static final double XP_PER_DAMAGE = 4 * XP_RATE;
	static final double HITPOINTS_XP_PER_DAMAGE = 1.33 * XP_RATE;
	/** Potion boosts fade by one level this often (one minute, as in OSRS). */
	static final int BOOST_DECAY_TICKS = 100;
	/** Prayer drain effect is spread over this many ticks: effect 1 costs a point every 3 minutes. */
	static final int PRAYER_DRAIN_DIVISOR = 300;
	/** Offline progress stops after 12 hours away. */
	public static final long MAX_OFFLINE_TICKS = 12L * 60 * 60 * 1000 / TICK_MILLIS;
	/** Auto-buy tops food up to this many when fewer than {@link #RESTOCK_BELOW} are left. */
	static final int RESTOCK_TO = 30;
	static final int RESTOCK_BELOW = 10;
	/** Auto-drink a Prayer potion when points fall to this share of the maximum. */
	static final double PRAYER_POTION_FRACTION = 0.25;

	private final GameState state;
	private final Random random;

	private Phase phase;
	private Monster monster;
	private int playerHp;
	private int monsterHp;
	private int phaseTicks;
	private int playerCooldown;
	private int monsterCooldown;
	private int lastPlayerHit;
	private int lastMonsterHit;
	private String unlockNotice;
	private int unlockNoticeTicks;

	public GameEngine(GameState state)
	{
		this(state, new Random());
	}

	public GameEngine(GameState state, Random random)
	{
		state.sanitise();
		this.state = state;
		this.random = random;
		this.playerHp = getPlayerMaxHp();
		startWalking();
	}

	public GameState getState()
	{
		return state;
	}

	public Phase getPhase()
	{
		return phase;
	}

	public Monster getMonster()
	{
		return monster;
	}

	public int getPlayerHp()
	{
		return playerHp;
	}

	public int getPlayerMaxHp()
	{
		return state.level(Skill.HITPOINTS);
	}

	public int getMonsterHp()
	{
		return monsterHp;
	}

	/** How far the adventurer has walked towards the monster, from 0 to 1. */
	public double getWalkProgress()
	{
		return phase == Phase.WALKING ? Math.min(1.0, phaseTicks / (double) WALK_TICKS) : 1.0;
	}

	/** Damage the player dealt this tick, 0 for a miss, or -1 if the player did not attack. */
	public int getLastPlayerHit()
	{
		return lastPlayerHit;
	}

	/** Damage the monster dealt this tick, 0 for a miss, or -1 if the monster did not attack. */
	public int getLastMonsterHit()
	{
		return lastMonsterHit;
	}

	/** The worn item in a slot, or null if nothing is worn there. */
	public GearItem getEquipped(Slot slot)
	{
		int tier = state.gearTier[slot.ordinal()];
		return tier < 0 ? null : Gear.get(slot, tier);
	}

	public int attackBonus()
	{
		GearItem weapon = getEquipped(Slot.WEAPON);
		return weapon == null ? 0 : weapon.getAttackBonus();
	}

	public int strengthBonus()
	{
		GearItem weapon = getEquipped(Slot.WEAPON);
		return weapon == null ? 0 : weapon.getStrengthBonus();
	}

	public int defenceBonus()
	{
		int total = 0;
		for (Slot slot : Slot.values())
		{
			GearItem item = getEquipped(slot);
			total += item == null ? 0 : item.getDefenceBonus();
		}
		return total;
	}

	/** Why a piece of gear cannot be bought right now, or null if it can. */
	public String gearBlocker(GearItem item)
	{
		if (item.getTier() <= state.gearTier[item.getSlot().ordinal()])
		{
			return "Already owned";
		}
		if (state.level(item.getRequiredSkill()) < item.getRequiredLevel())
		{
			return "Needs " + item.getRequiredSkill().getDisplayName() + " " + item.getRequiredLevel();
		}
		if (state.coins < item.getCost())
		{
			return "Not enough coins";
		}
		return null;
	}

	/** Buys and wears the item. Returns false, changing nothing, if gearBlocker says no. */
	public boolean buyGear(GearItem item)
	{
		if (gearBlocker(item) != null)
		{
			return false;
		}
		state.coins -= item.getCost();
		state.gearTier[item.getSlot().ordinal()] = item.getTier();
		return true;
	}

	public boolean buyFood(int foodIndex, int quantity)
	{
		long price = (long) Food.ALL.get(foodIndex).getCost() * quantity;
		if (quantity <= 0 || state.coins < price)
		{
			return false;
		}
		state.coins -= price;
		state.foodCount[foodIndex] += quantity;
		return true;
	}

	/** Eats one portion if owned and there is health to restore. */
	public boolean eat(int foodIndex)
	{
		if (phase == Phase.DEAD || state.foodCount[foodIndex] <= 0 || playerHp >= getPlayerMaxHp())
		{
			return false;
		}
		state.foodCount[foodIndex]--;
		playerHp = Math.min(getPlayerMaxHp(), playerHp + Food.ALL.get(foodIndex).getHeal());
		if (phase == Phase.FIGHTING)
		{
			playerCooldown += EAT_DELAY_TICKS;
		}
		return true;
	}

	/** The level used in combat rolls: base level plus potion boost, times the active prayer's boost. */
	public int effectiveLevel(Skill skill)
	{
		int percent = 100;
		for (int i = 0; i < Prayer.ALL.size(); i++)
		{
			if (state.activePrayers[i])
			{
				Prayer prayer = Prayer.ALL.get(i);
				percent += skill == Skill.ATTACK ? prayer.getAttackPercent()
					: skill == Skill.STRENGTH ? prayer.getStrengthPercent()
					: skill == Skill.DEFENCE ? prayer.getDefencePercent() : 0;
			}
		}
		return (state.level(skill) + state.skillBoost[skill.ordinal()]) * percent / 100;
	}

	/** Levels currently added by potions (0 when none is active). */
	public int getBoost(Skill skill)
	{
		return state.skillBoost[skill.ordinal()];
	}

	public double getPrayerPoints()
	{
		return state.prayerPoints;
	}

	public int getPrayerMaxPoints()
	{
		return state.level(Skill.PRAYER);
	}

	public boolean isPrayerActive(int prayerIndex)
	{
		return state.activePrayers[prayerIndex];
	}

	/** Why a prayer cannot be switched on now, or null if it can. */
	public String prayerBlocker(int prayerIndex)
	{
		Prayer prayer = Prayer.ALL.get(prayerIndex);
		if (state.level(Skill.PRAYER) < prayer.getRequiredPrayerLevel())
		{
			return "Needs Prayer " + prayer.getRequiredPrayerLevel();
		}
		if (state.level(Skill.DEFENCE) < prayer.getRequiredDefenceLevel())
		{
			return "Needs Defence " + prayer.getRequiredDefenceLevel();
		}
		if (state.prayerPoints <= 0)
		{
			return "No prayer points";
		}
		return null;
	}

	/** Switches a prayer on (turning off any that boost the same stat) or off. */
	public boolean togglePrayer(int prayerIndex)
	{
		if (state.activePrayers[prayerIndex])
		{
			state.activePrayers[prayerIndex] = false;
			state.preferredPrayers[prayerIndex] = false;
			return true;
		}
		if (prayerBlocker(prayerIndex) != null)
		{
			return false;
		}
		Prayer prayer = Prayer.ALL.get(prayerIndex);
		for (int i = 0; i < Prayer.ALL.size(); i++)
		{
			if (state.activePrayers[i] && Prayer.ALL.get(i).conflictsWith(prayer))
			{
				state.activePrayers[i] = false;
				state.preferredPrayers[i] = false;
			}
		}
		state.activePrayers[prayerIndex] = true;
		state.preferredPrayers[prayerIndex] = true;
		return true;
	}

	/** Buries every bone owned for Prayer XP. Returns how many were buried. */
	public long buryBones()
	{
		long buried = 0;
		double xp = 0;
		for (int i = 0; i < BoneType.ALL.size(); i++)
		{
			buried += state.boneCount[i];
			xp += state.boneCount[i] * BoneType.ALL.get(i).getXp();
			state.boneCount[i] = 0;
		}
		if (buried > 0)
		{
			int levelBefore = state.level(Skill.PRAYER);
			state.skillXp[Skill.PRAYER.ordinal()] += xp;
			// Gaining a level also adds the new point to the pool.
			state.prayerPoints += state.level(Skill.PRAYER) - levelBefore;
		}
		return buried;
	}

	public long totalBones()
	{
		long total = 0;
		for (long count : state.boneCount)
		{
			total += count;
		}
		return total;
	}

	/** Why a potion cannot be bought right now, or null if it can. */
	public String potionBlocker(int potionIndex, int quantity)
	{
		Potion potion = Potion.ALL.get(potionIndex);
		if (state.level(potion.getRequiredSkill()) < potion.getRequiredLevel())
		{
			return "Needs " + potion.getRequiredSkill().getDisplayName() + " " + potion.getRequiredLevel();
		}
		if (state.coins < (long) potion.getCost() * quantity)
		{
			return "Not enough coins";
		}
		return null;
	}

	public boolean buyPotion(int potionIndex, int quantity)
	{
		if (quantity <= 0 || potionBlocker(potionIndex, quantity) != null)
		{
			return false;
		}
		state.coins -= (long) Potion.ALL.get(potionIndex).getCost() * quantity;
		state.potionCount[potionIndex] += quantity;
		return true;
	}

	/** True if a potion is owned and drinking it would change something (nothing is wasted). */
	public boolean canDrink(int potionIndex)
	{
		Potion potion = Potion.ALL.get(potionIndex);
		if (state.potionCount[potionIndex] <= 0)
		{
			return false;
		}
		if (potion.isPrayerRestore())
		{
			return state.prayerPoints < getPrayerMaxPoints();
		}
		Skill skill = potion.getBoostedSkill();
		return potion.boostFor(state.level(skill)) > state.skillBoost[skill.ordinal()];
	}

	/** Drinks one potion if canDrink says it helps. */
	public boolean drink(int potionIndex)
	{
		if (!canDrink(potionIndex))
		{
			return false;
		}
		Potion potion = Potion.ALL.get(potionIndex);
		if (potion.isPrayerRestore())
		{
			int max = getPrayerMaxPoints();
			state.prayerPoints = Math.min(max, state.prayerPoints + potion.restoreFor(max));
		}
		else
		{
			Skill skill = potion.getBoostedSkill();
			state.skillBoost[skill.ordinal()] = potion.boostFor(state.level(skill));
		}
		state.potionCount[potionIndex]--;
		return true;
	}

	public Zone getZone()
	{
		return Monsters.ZONES.get(Monsters.zoneIndex(monster));
	}

	/** Name of a zone unlocked in the last few ticks, or null; the panel shows it as a notice. */
	public String getUnlockNotice()
	{
		return unlockNoticeTicks > 0 ? unlockNotice : null;
	}

	public void setTargetMonster(String monsterId)
	{
		if (Monsters.zoneIndex(Monsters.byId(monsterId)) >= state.unlockedZones)
		{
			return;
		}
		if (!monsterId.equals(state.targetMonster))
		{
			state.targetMonster = monsterId;
			startWalking();
		}
	}

	public void setTrainingSkill(Skill skill)
	{
		if (skill.isTrainable())
		{
			state.trainingSkill = skill;
		}
	}

	public void tick()
	{
		state.totalTicks++;
		lastPlayerHit = -1;
		lastMonsterHit = -1;
		if (unlockNoticeTicks > 0)
		{
			unlockNoticeTicks--;
		}

		if (phase != Phase.DEAD && playerHp < getPlayerMaxHp() && state.totalTicks % REGEN_TICKS == 0)
		{
			playerHp++;
		}

		if (state.totalTicks % BOOST_DECAY_TICKS == 0)
		{
			for (int i = 0; i < state.skillBoost.length; i++)
			{
				state.skillBoost[i] = Math.max(0, state.skillBoost[i] - 1);
			}
		}
		drainPrayer();
		if (phase != Phase.DEAD)
		{
			automate();
		}

		phaseTicks++;
		switch (phase)
		{
			case WALKING:
				if (phaseTicks >= WALK_TICKS)
				{
					startFight();
				}
				break;
			case FIGHTING:
				fightTick();
				break;
			case DEAD:
				if (phaseTicks >= RESPAWN_TICKS)
				{
					playerHp = getPlayerMaxHp();
					state.prayerPoints = getPrayerMaxPoints();
					startWalking();
				}
				break;
		}
	}

	/**
	 * Runs the automation the player has switched on. Eating and drinking only happen mid-fight,
	 * so nothing is wasted while walking or regenerating for free.
	 */
	private void automate()
	{
		if (state.autoBuyFood >= 0)
		{
			restockFood();
		}
		if (state.autoPrayer)
		{
			restorePrayers();
		}
		if (phase != Phase.FIGHTING)
		{
			return;
		}
		if (state.autoPotion)
		{
			drinkPotions();
		}
		if (state.autoEat && playerHp * 100 <= state.autoEatPercent * getPlayerMaxHp())
		{
			autoEat();
		}
	}

	/** Eats the biggest food that fits the missing health, or the smallest one if none does. */
	private void autoEat()
	{
		int missing = getPlayerMaxHp() - playerHp;
		int best = -1;
		for (int i = 0; i < Food.ALL.size(); i++)
		{
			if (state.foodCount[i] <= 0)
			{
				continue;
			}
			int heal = Food.ALL.get(i).getHeal();
			if (best < 0)
			{
				best = i;
			}
			else
			{
				int bestHeal = Food.ALL.get(best).getHeal();
				boolean fits = heal <= missing;
				boolean bestFits = bestHeal <= missing;
				if (fits ? (!bestFits || heal > bestHeal) : (!bestFits && heal < bestHeal))
				{
					best = i;
				}
			}
		}
		if (best >= 0)
		{
			eat(best);
		}
	}

	private void restockFood()
	{
		int index = state.autoBuyFood;
		if (state.foodCount[index] >= RESTOCK_BELOW)
		{
			return;
		}
		long affordable = state.coins / Food.ALL.get(index).getCost();
		int quantity = (int) Math.min(RESTOCK_TO - state.foodCount[index], affordable);
		if (quantity > 0)
		{
			buyFood(index, quantity);
		}
	}

	private void drinkPotions()
	{
		for (int i = Potion.ALL.size() - 1; i >= 0; i--)
		{
			Potion potion = Potion.ALL.get(i);
			if (potion.isPrayerRestore())
			{
				boolean praying = false;
				for (boolean on : state.activePrayers)
				{
					praying |= on;
				}
				if (praying && state.prayerPoints <= getPrayerMaxPoints() * PRAYER_POTION_FRACTION)
				{
					drink(i);
				}
			}
			else if (state.skillBoost[potion.getBoostedSkill().ordinal()] == 0)
			{
				// Highest tier first, so a Super potion is used before a plain one.
				drink(i);
			}
		}
	}

	private void restorePrayers()
	{
		if (state.prayerPoints <= 0)
		{
			return;
		}
		for (int i = 0; i < Prayer.ALL.size(); i++)
		{
			if (state.preferredPrayers[i] && !state.activePrayers[i] && prayerBlocker(i) == null)
			{
				togglePrayer(i);
			}
		}
	}

	/**
	 * Plays the game forward without a display, for time spent away. Returns what changed.
	 */
	public OfflineReport fastForward(long ticks)
	{
		OfflineReport.Snapshot before = OfflineReport.snapshot(state);
		long count = Math.min(ticks, MAX_OFFLINE_TICKS);
		for (long i = 0; i < count; i++)
		{
			tick();
		}
		unlockNoticeTicks = 0;
		return OfflineReport.between(before, state, count);
	}

	private void startWalking()
	{
		monster = Monsters.byId(state.targetMonster);
		phase = Phase.WALKING;
		phaseTicks = 0;
	}

	private void startFight()
	{
		phase = Phase.FIGHTING;
		phaseTicks = 0;
		monsterHp = monster.getHitpoints();
		playerCooldown = 1;
		monsterCooldown = monster.getAttackSpeedTicks();
	}

	private void fightTick()
	{
		if (--playerCooldown <= 0)
		{
			playerCooldown = PLAYER_ATTACK_TICKS;
			playerAttack();
			if (monsterHp <= 0)
			{
				kill();
				return;
			}
		}
		if (--monsterCooldown <= 0)
		{
			monsterCooldown = monster.getAttackSpeedTicks();
			monsterAttack();
			if (playerHp <= 0)
			{
				die();
			}
		}
	}

	private void playerAttack()
	{
		int damage = 0;
		double chance = CombatMath.playerHitChance(effectiveLevel(Skill.ATTACK), attackBonus(), monster.getDefenceLevel());
		if (random.nextDouble() < chance)
		{
			damage = 1 + random.nextInt(CombatMath.playerMaxHit(effectiveLevel(Skill.STRENGTH), strengthBonus()));
			damage = Math.min(damage, monsterHp);
		}
		monsterHp -= damage;
		lastPlayerHit = damage;
		gainXp(damage);
	}

	private void monsterAttack()
	{
		int damage = 0;
		double chance = CombatMath.monsterHitChance(monster.getAttackLevel(), monster.getAttackBonus(),
			effectiveLevel(Skill.DEFENCE), defenceBonus());
		if (random.nextDouble() < chance)
		{
			damage = Math.min(1 + random.nextInt(monster.getMaxHit()), playerHp);
		}
		playerHp -= damage;
		lastMonsterHit = damage;
	}

	private void gainXp(int damage)
	{
		if (damage == 0)
		{
			return;
		}
		int maxHpBefore = getPlayerMaxHp();
		state.skillXp[state.trainingSkill.ordinal()] += damage * XP_PER_DAMAGE;
		state.skillXp[Skill.HITPOINTS.ordinal()] += damage * HITPOINTS_XP_PER_DAMAGE;
		// A Hitpoints level-up adds the new point to current health, as in OSRS.
		playerHp += getPlayerMaxHp() - maxHpBefore;
	}

	private void kill()
	{
		state.kills++;
		state.boneCount[monster.getBoneType()]++;
		state.coins += monster.getMinCoins() + random.nextInt(monster.getMaxCoins() - monster.getMinCoins() + 1);
		if (state.autoBury)
		{
			buryBones();
		}
		if (monster.isBoss())
		{
			unlockNextZone();
		}
		startWalking();
	}

	private void unlockNextZone()
	{
		int next = Monsters.zoneIndex(monster) + 1;
		if (next == state.unlockedZones && next < Monsters.ZONES.size())
		{
			state.unlockedZones++;
			unlockNotice = Monsters.ZONES.get(next).getName();
			unlockNoticeTicks = UNLOCK_NOTICE_TICKS;
			if (state.autoAdvance)
			{
				state.targetMonster = Monsters.ZONES.get(next).getMonsters().get(0).getId();
			}
		}
	}

	private void drainPrayer()
	{
		int drain = 0;
		for (int i = 0; i < Prayer.ALL.size(); i++)
		{
			if (state.activePrayers[i])
			{
				drain += Prayer.ALL.get(i).getDrainEffect();
			}
		}
		if (drain == 0)
		{
			return;
		}
		state.prayerPoints -= drain / (double) PRAYER_DRAIN_DIVISOR;
		// The small tolerance absorbs floating-point error from adding up many tiny drains.
		if (state.prayerPoints <= 1e-9)
		{
			state.prayerPoints = 0;
			Arrays.fill(state.activePrayers, false);
		}
	}

	private void die()
	{
		// Dying switches prayers off and wears off potions.
		Arrays.fill(state.activePrayers, false);
		Arrays.fill(state.skillBoost, 0);
		state.deaths++;
		phase = Phase.DEAD;
		phaseTicks = 0;
	}
}
