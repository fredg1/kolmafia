package net.sourceforge.kolmafia.session;

import java.util.regex.Matcher;
import java.util.regex.Pattern;
import net.sourceforge.kolmafia.KoLCharacter;
import net.sourceforge.kolmafia.preferences.Preferences;
import net.sourceforge.kolmafia.request.GenericRequest;

public class LTAManager {
  private static final Pattern CAPITAL_PATTERN = Pattern.compile("<td><b>(.*?)</b>(.*?)</td></tr>");

  private static final class BondPerk {
    private final String name;
    private final String setting;
    private boolean seen;

    private BondPerk(final String name, final String setting) {
      this.name = name;
      this.setting = setting;
    }
  }

  private static final BondPerk[] bondPerks = {
    new BondPerk("Super-Accurate Spy Watch", "Adv"),
    new BondPerk("Razor-Sharp Tie", "Wpn"),
    new BondPerk("Jet-Powered Skis", "Init"),
    new BondPerk("Kevlar-Lined Pants", "DR"),
    new BondPerk("Injected Nanobots", "HP"),
    new BondPerk("Sticky Climbing Gloves", "Item2"),
    new BondPerk("Retinal Knowledge HUD", "Stat"),
    new BondPerk("Belt-Implanted Still", "Drunk1"),
    new BondPerk("Alcohol Absorbent Underwear", "Booze"),
    new BondPerk("Universal Symbology Guide", "Symbols"),
    new BondPerk("Soberness Injection Pen", "Drunk2"),
    new BondPerk("Short-Range Jetpack", "Jetpack"),
    new BondPerk("Invisible Meat Car, the Vanish", "Stealth"),
    new BondPerk("Portable Pocket Bridge", "Bridge"),
    new BondPerk("Static-Inducing, Bug-Shorting Underpants", "MPregen"),
    new BondPerk("Exotic Bartender, Barry L. Eagle", "MartiniTurn"),
    new BondPerk("Renowned Meat Thief, Ivanna Cuddle", "Meat"),
    new BondPerk("Master Art Thief, Sly Richard", "Item1"),
    new BondPerk("Personal Trainer, Debbie Dallas", "Mus1"),
    new BondPerk("Rocket Scientist, Crimbo Jones", "Mys1"),
    new BondPerk("Licensed Masseur, Oliver Closehoff", "Mox1"),
    new BondPerk("Professional Cabbie, Rock Hardy", "Beach"),
    new BondPerk("Fellow Spy, Daisy Duke", "Beat"),
    new BondPerk("Fellow Spy, Prince O'Toole", "MartiniDelivery"),
    new BondPerk("Personal Kinesiologist, Doctor Kittie", "Mus2"),
    new BondPerk("Computer Hacker, Mitt Jobs", "Mys2"),
    new BondPerk("Spa Owner, Fatima Jiggles", "Mox2"),
    new BondPerk("Exotic Olive Procurer, Ben Dover", "MartiniPlus"),
    new BondPerk("Trained Sniper, Felicity Snuggles", "War"),
    new BondPerk("Martial Arts Trainer, Jaques Trappe", "Weapon2"),
    new BondPerk("Electromagnetic Ring", "Item3"),
    new BondPerk("Robo-Spleen", "Spleen"),
    new BondPerk("Universal GPS", "Desert"),
    new BondPerk("Mission Controller, Maeby Moneypenny", "Stealth2"),
    new BondPerk("Sage Advisor, London McBrittishman", "Stat2"),
    new BondPerk("True Love, Honey Potts", "Honey")
  };

  private LTAManager() {}

  public static final void parseLI11HQ(final GenericRequest request) {
    if (!request.responseText.contains("LI-11 HQ")) {
      return;
    }

    // reset
    for (BondPerk perk : LTAManager.bondPerks) {
      perk.seen = false;
    }

    Matcher matcher = LTAManager.CAPITAL_PATTERN.matcher(request.responseText);
    while (matcher.find()) {
      if (matcher.group(2).contains("Active") || matcher.group(2).contains("Connected")) {
        for (BondPerk perk : LTAManager.bondPerks) {
          if (!matcher.group(1).equals(perk.name)) {
            continue;
          }

          perk.seen = true;

          switch (perk.setting) {
            case "Beach":
              KoLCharacter.setDesertBeachAvailable();
              break;
            case "Desert":
              // Do something with this
              break;
            case "Bridge":
              // need to handle when this is selected
              break;
          }

          break;
        }
      }
    }

    for (BondPerk perk : LTAManager.bondPerks) {
      Preferences.setBoolean("bond" + perk.setting, perk.seen);
    }

    KoLCharacter.recalculateAdjustments();
    KoLCharacter.updateStatus();
  }
}
