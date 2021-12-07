package net.sourceforge.kolmafia.persistence.choiceadventures;

import static org.junit.jupiter.api.Assertions.fail;

import java.util.List;

import org.junit.jupiter.api.Test;

public class ChoiceAdventureDatabaseTest {
  @Test
  public void checkDuplicates() {
    List<Integer> duplicates = ChoiceAdventureDatabase.duplicates;

    if (duplicates.size() > 0) {
      StringBuilder str = new StringBuilder("Duplicate choice adventures found:");

      for (Integer duplicate : duplicates) {
        str.append(" ");
        str.append(duplicate);
      }

      fail(str.toString());
    }
  }
}
