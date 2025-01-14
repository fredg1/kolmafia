package net.sourceforge.kolmafia;

import static org.junit.Assert.*;

import net.sourceforge.kolmafia.maximizer.Boost;
import net.sourceforge.kolmafia.maximizer.Maximizer;
import net.sourceforge.kolmafia.objectpool.EffectPool;
import net.sourceforge.kolmafia.session.InventoryManager;
import net.sourceforge.kolmafia.session.EquipmentManager;

import org.json.JSONException;
import org.json.JSONObject;
import org.junit.After;
import org.junit.Ignore;
import org.junit.Test;

import java.util.Optional;

public class MaximizerTest
{
	@After
	public void after() {
		KoLCharacter.reset(false);
	}

	@Test
	public void changesGear()
	{
		// 1 helmet turtle.
		loadInventory( "{\"3\": \"1\"}" );
		assertTrue( maximize("mus") );
		assertEquals(1, modFor("Buffed Muscle"), 0.01 );
	}

	@Test
	public void nothingBetterThanSomething()
	{
		// 1 helmet turtle.
		loadInventory( "{\"3\": \"1\"}" );
		assertTrue( maximize("-mus") );
		assertEquals(0, modFor("Buffed Muscle"), 0.01 );
	}

	@Test
	public void clubModifierDoesntAffectOffhand()
	{
		KoLCharacter.addAvailableSkill( "Double-Fisted Skull Smashing" );
		// 15 base + buffed mus.
		KoLCharacter.setStatPoints( 15, 225, 0, 0, 0, 0 );
		// 2 flaming crutch, 2 white sword, 1 dense meat sword.
		// Max required muscle to equip any of these is 15.
		loadInventory( "{\"473\": \"2\", \"269\": \"2\", \"1728\": \"1\"}" );
		assertTrue( "Can equip white sword", EquipmentManager.canEquip(269) );
		assertTrue( "Can equip flaming crutch", EquipmentManager.canEquip(473) );
		assertTrue( maximize("mus, club") );
		// Should equip 1 flaming crutch, 1 white sword.
		assertEquals( "Muscle as expected.", 2, modFor("Muscle"), 0.01 );
		assertEquals( "Hot damage as expected.", 3, modFor("Hot Damage"), 0.01 );
	}

	@Test
	public void maximizeGiveBestScoreWithEffectsAtNoncombatLimit()
	{
		// space trip safety headphones, Krampus horn
		loadInventory("{\"4639\": \"1\", \"9274\": \"1\"}");
		// get ourselves to -25 combat
		KoLConstants.activeEffects.clear();
		KoLConstants.activeEffects.add(EffectPool.get(1798)); // Shelter of Shed
		KoLConstants.activeEffects.add(EffectPool.get(165)); // Smooth Movements
		// check we can equip everything
		KoLCharacter.setStatPoints( 0, 0, 40, 1600, 125, 15625 );
		KoLCharacter.recalculateAdjustments();
		assertTrue( "Cannot equip space trip safety headphones", EquipmentManager.canEquip(4639) );
		assertTrue( "Cannot equip Krampus Horn", EquipmentManager.canEquip(9274) );
		assertTrue( maximize( "cold res,-combat -hat -weapon -offhand -back -shirt -pants -familiar -acc1 -acc2 -acc3") );
		assertEquals( "Base score is 25",
				25, modFor("Cold Resistance" )	- modFor( "Combat Rate" ), 0.01 );
		assertTrue( maximize( "cold res,-combat -acc2 -acc3" ) );
		assertEquals( "Maximizing one slot should reach 27",
				27, modFor("Cold Resistance" )	- modFor( "Combat Rate" ), 0.01 );
		Optional<AdventureResult> acc1 = Maximizer.boosts.stream()
				.filter(Boost::isEquipment)
				.filter(b -> b.getSlot() == EquipmentManager.ACCESSORY1)
				.map(Boost::getItem)
				.findAny();
		assertTrue(acc1.isPresent());
		assertEquals(acc1.get().id, 9274);
	}

	@Test
	public void maximizeShouldNotRemoveEquipmentThatCanNoLongerBeEquipped()
	{
		// slippers have a Moxie requirement of 125
		EquipmentManager.setEquipment(EquipmentManager.ACCESSORY1, AdventureResult.parseResult("Fuzzy Slippers of Hatred"));
		// get our Moxie below 125 (e.g. basic hot dogs, stat limiting effects)
		KoLCharacter.setStatPoints( 0, 0, 0, 0, 0, 0 );
		KoLCharacter.recalculateAdjustments();
		assertFalse( "Can still equip Fuzzy Slippers of Hatred", EquipmentManager.canEquip(4307) );
		assertTrue( maximize( "-combat -hat -weapon -offhand -back -shirt -pants -familiar -acc1 -acc2 -acc3" ) );
		assertEquals( "Base score is 5",
				5, -modFor( "Combat Rate" ), 0.01 );
		assertTrue( maximize( "-combat" ) );
		assertEquals( "Maximizing should not reduce score",
				5, -modFor( "Combat Rate" ), 0.01 );
	}

	@Test
	public void freshCharacterShouldNotRecommendEverythingWithCurrentScore()
	{
		KoLCharacter.setSign("Platypus");
		assertTrue( maximize( "familiar weight" ) );
		assertEquals( "Base score is 5",
				5, modFor( "Familiar Weight" ), 0.01 );
		// monorail buff should always be available, but should not improve familiar weight.
		// so are friars, but I don't know why and that might be a bug
		assertEquals( 1, Maximizer.boosts.size() );
		Boost ar = Maximizer.boosts.get(0);
		assertEquals("", ar.getCmd());
	}

	// Sample test for https://kolmafia.us/showthread.php?23648&p=151903#post151903.
	@Test
	public void noTieCanLeaveSlotsEmpty()
	{
		// 1 helmet turtle.
		loadInventory( "{\"3\": \"1\"}" );
		assertTrue( maximize( "mys -tie" ) );
		assertEquals(
			0, modFor( "Buffed Muscle" ), 0.01 );
	}

	private void loadInventory(String jsonInventory)
	{
		try
		{
			InventoryManager.parseInventory( new JSONObject( jsonInventory ) );
		}
		catch ( JSONException e )
		{
			fail( "Inventory parsing failed." );
		}
	}

	private boolean maximize(String maximizerString) {
		return Maximizer.maximize( maximizerString, 0, 0, true );
	}

	private double modFor(String modifier) {
		return Modifiers.getNumericModifier( "Generated", "_spec", modifier );
	}
}
