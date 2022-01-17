package net.sourceforge.kolmafia.persistence.choiceadventures;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.TreeMap;
import java.util.function.Predicate;
import java.util.function.Supplier;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import net.sourceforge.kolmafia.AdventureResult;
import net.sourceforge.kolmafia.AdventureResult.AdventureLongCountResult;
import net.sourceforge.kolmafia.KoLAdventure;
import net.sourceforge.kolmafia.KoLCharacter;
import net.sourceforge.kolmafia.KoLConstants;
import net.sourceforge.kolmafia.KoLmafia;
import net.sourceforge.kolmafia.RequestLogger;
import net.sourceforge.kolmafia.objectpool.IntegerPool;
import net.sourceforge.kolmafia.persistence.AdventureDatabase;
import net.sourceforge.kolmafia.persistence.AdventureQueueDatabase;
import net.sourceforge.kolmafia.persistence.EffectDatabase;
import net.sourceforge.kolmafia.persistence.ItemDatabase;
import net.sourceforge.kolmafia.preferences.Preferences;
import net.sourceforge.kolmafia.request.AdventureRequest;
import net.sourceforge.kolmafia.request.GenericRequest;
import net.sourceforge.kolmafia.session.ChoiceManager;
import net.sourceforge.kolmafia.session.EquipmentManager;
import net.sourceforge.kolmafia.utilities.CharacterEntities;
import net.sourceforge.kolmafia.utilities.ChoiceUtilities;

public class ChoiceAdventureDatabase {
  // Limit the constructor's visibility to package-only
  ChoiceAdventureDatabase() {}

  private static final Map<Integer, ChoiceAdventure> database = new TreeMap<>();
  static final List<Integer> duplicates = new LinkedList<>();

  public abstract class ChoiceAdventure {
    /**
     * Indicates that none of the options from the Choice Adventure cause anything to be added to
     * the noncombat queue.<br>
     * Is equivalent to manually setting all of this choice's {@link Option}s' {@link
     * Option#entersQueue} to {@code false}.
     *
     * <p>An adventure may still end up being added if one of the options lead to another choice
     * adventure, and one of that choice adventure's {@link Option}s' {@link Option#entersQueue} is
     * {@code true}.
     *
     * <p>To mark a choiceAdventure as not being able to enter the queue <b>at all</b>
     * <i>itself</i>, see {@link #isSuperlikely}.
     */
    boolean neverEntersQueue = false;
    /**
     * Indicates that this adventure is a superlikely, meaning it will never end up in the queue if
     * it is the first thing you encounter in a choice adventure chain.
     *
     * <p>Can still cause something to end up in the queue if another noncombat leads to this choice
     * adventure. To prevent this, also set {@link #neverEntersQueue} or the {@link Option}s' {@link
     * Option#entersQueue}.
     */
    boolean isSuperlikely = false;
    /**
     * Indicates that we don't want to/can't let {@link ChoiceManager#automateChoice} handle this
     * choice. Either the user has a choice adventure script for it, or they handle it manually.
     * Period.
     */
    boolean neverAutomate = false;

    boolean hasGoalButton = false;
    /** If we can leave this choice adventure without having to submit an option. */
    boolean canWalkFromChoice = false;

    boolean option0IsManualControl = true;
    /**
     * The name used to identify this choice adventure in ChoiceOptionsPanel.
     *
     * <p>Defaults to the name of the location it's in.
     */
    String customName = "";
    /**
     * Allows choice adventures to be displayed in an arbitrary order inside their zone in
     * ChoiceOptionsPanel
     */
    int customOrder = 0;
    /**
     * The zones in which this choice adventure will appear, in ChoiceOptionsPanel.
     *
     * <p>Will, by default, contain the {@link #source}'s zone (if {@link #source} is a location)
     */
    public final List<String> customZones = new ArrayList<>();
    /**
     * Functional Interface returning which customOption should be selected based on user properties
     * (also handling invalid settings).
     *
     * <p>If null, we'll simply look at
     *
     * <pre>Preferences.getInteger( "choiceAdventure" + this.choice )</pre>
     */
    public Supplier<Integer> customLoad = null;
    /**
     * A list of unconventional (i.e. other than "choiceAdventure****") preferences to be added to
     * the list of preferences which, when modified, cause ChoiceOptionsPanel to be refreshed.
     */
    public final List<String> customPreferencesToListen = new ArrayList<>();

    /**
     * Extra custom code ran during each Choice Adventure's initialization.
     *
     * <p>Note: Please try to keep the methods in this relative order:
     *
     * <pre>
     * void setup()
     * void preChoice(String urlString)
     * boolean registerRequest(String urlString, int decision)
     * void postChoice0(GenericRequest request, int decision)
     * void registerDeferredChoice()
     * String encounterName(String urlString, String responseText)
     * void visitChoice(GenericRequest request)
     * void decorateChoice(StringBuffer buffer)
     * String specialChoiceHandling(GenericRequest request)
     * String getDecision(String responseText, String decision, int stepCount)
     * void postChoice1(GenericRequest request, int decision)
     * void postChoice2(GenericRequest request, int decision)
     * void decorateChoiceResponse(StringBuffer buffer, int option)
     * </pre>
     */
    abstract void setup();

    /**
     * Handling to be done before we register the request with RequestLogger.
     *
     * <p>Since, at this point, the request was not even sent, there is no guarantee that what the
     * URL suggests is what is going to happen. Therefore, this method must <b>only</b> be used to
     * set private fields, in preparation of handling to be done in the {@code postChoice*()}
     * methods.
     *
     * <p>Examples of actions to do in this method include saving {@link
     * RequestLogger#getLastURLString()} and/or {@link ChoiceManager#lastResponseText} before they
     * get replaced by this request's URL/response text during its registration and execution,
     * respectively.
     */
    void preChoice(final String urlString) {}

    /**
     * The registration process for {@code choice.php?whichchoice=}<i>this</i> prior to its
     * execution, which means printing, if appropriate, text in the Session Logs (or prevent the
     * automatic printing of said text).
     *
     * @param urlString the URL we are trying/about to send
     * @param decision the option the user is trying to select (may be 0 if this was actually a
     *     visit to this choice adventure)
     * @return {@code true} if the request was successfully identified. <br>
     *     If {@code false} is returned, we'll call {@link ChoiceManager#defaultRegister(int, int)}
     *     with {@code decision} and {@link RequestLogger#updateSessionLog(String)} with {@code
     *     urlString}
     */
    boolean registerRequest(final String urlString, final int decision) {
      return false;
    }

    /**
     * Edge cases that need to be handled before the encounter is registered by {@link
     * AdventureRequest#registerEncounter}.
     *
     * <p>This is done right <i>before</i> we update {@link KoLAdventure#lastVisitedLocation} with
     * {@link ChoiceAdventure#source}
     *
     * @param request the request currently used/dealt with
     * @param decision the option the user is trying to select (may be 0 if this was actually a
     *     visit to this choice adventure)
     */
    void postChoice0(final GenericRequest request, final int decision) {}

    /**
     * registration method reserved for choice adventures that follow choice adventures, fights
     * and/or other non-combats, done as a part of {@link
     * AdventureRequest#registerEncounter(GenericRequest)}.
     *
     * <p>This method should <b>only</b> be used to call {@link
     * RequestLogger#registerLastLocation()} or {@link RequestLogger#registerLocation(String)},
     * since, just like {@link #postChoice0}, this method will be used with any choice.php page, be
     * it a visit, response/choice selection, or even a page reload.
     *
     * <p>Examples:
     *
     * <blockquote>
     *
     * <pre>
     * new ChoiceAdventure(125, "No Visible Means of Support", "The Hidden Temple") {
     *   [...]
     *   void registerDeferredChoice() {
     *     RequestLogger.registerLastLocation();
     *   }
     *   [...]
     * }
     * </pre>
     *
     * </blockquote>
     *
     * or
     *
     * <blockquote>
     *
     * <pre>
     * new ChoiceAdventure(1013, "Mazel Tov!", "The Hedge Maze") {
     *   [...]
     *   void registerDeferredChoice() {
     *     RequestLogger.registerLocation("The Hedge Maze (Room 9)");
     *   }
     *   [...]
     * }
     * </pre>
     *
     * </blockquote>
     */
    void registerDeferredChoice() {}

    /**
     * Part of the registration process of {@link AdventureRequest#registerEncounter}, done right
     * after {@link #postChoice0}
     *
     * @param urlString
     * @param responseText
     * @return the name for this choice adventure. If non-null, will be set as the value of the
     *     lastEncounter preference, and will be what ends up in the queue if this adventure ends up
     *     going in it
     */
    String encounterName(final String urlString, final String responseText) {
      if (this.canWalkFromChoice) {
        return null;
      }
      return AdventureRequest.parseEncounter(responseText);
    }

    /**
     * Where we go to instead of {@link #postChoice1} when we realize that this is a visit to a
     * choice adventure, rather than a response to one.
     *
     * <p>Gives a chance to look at the raw response from KoL for this choice adventure.<br>
     * The page will be decorated afterwards.
     *
     * <p>If they are dynamic, this is the time to modify this choice's {@link Option#entersQueue},
     * {@link Option#spoilerText}, {@link Option#attachments} and/or {@link Option#leadsTo}.
     */
    void visitChoice(final GenericRequest request) {}

    /**
     * Choice-specific decorations.<br>
     * Called when we understand this request to be a <b>visit to</b> this choice adventure (if the
     * user turned {@code relayShowSpoilers} on) before adding the options' spoilers.
     *
     * @param buffer the current response text being decorated. Currently, the only modifications
     *     made to the text should pretty much only be to have passed it through {@link
     *     net.sourceforge.kolmafia.webui.StationaryButtonDecorator#decorate}
     */
    void decorateChoice(final StringBuffer buffer) {}

    /**
     * Called at the start of {@link ChoiceManager#automateChoice}.
     *
     * <p>For choices with abnormal handling. Allows access to the request in order to use {@link
     * GenericRequest#addFormField(String, String, boolean)}
     *
     * @param request the request currently being processed, and that will be used for the response.
     * @return the option we are going for. If non-null, the rest of the request will immediately be
     *     assembled (adding the fields whichchoice, option and pwd), and ran. If null, we'll go
     *     back to {@link ChoiceManager#automateChoice} (though any form field added to {@code
     *     request} will still be kept)
     */
    String specialChoiceHandling(final GenericRequest request) {
      return null;
    }

    /**
     * Takes the value of the user's {@code choiceAdventure*} property after having it pass through
     * {@link ChoiceManager#pickGoalOption}, and returns the actual value we should submit KoL to
     * achieve the desired result.
     *
     * <p>Does handling such as:
     *
     * <ul>
     *   <li>Providing a contingency option if the desired option is not available.
     *   <li>Handling "custom options"; mafia-generated options displayed in ChoiceOptionsPanel
     *       which allow the user to select a "goal" rather than a "decision" (e.g. instead of
     *       "option 2", "the option that gives substats of my highest stat").
     *   <li>Handling choice adventure that are too straightforward or non-standard for it to have a
     *       {@code choiceAdventure*} property at all.
     * </ul>
     *
     * <p>Unless {@link #option0IsManualControl} is {@code false}, we don't visit this method if
     * {@code decision} is {@code "0"}.
     *
     * @param request
     * @param decision The value of the user's {@code choiceAdventure*} (up until a {@code &}, if
     *     the value included one).
     * @param stepCount When automating, contains how many choice adventures we got in a row (i.e. a
     *     choice adventure chain). Used for solvers. When not automating, has the value {@link
     *     Integer#MAX_VALUE}.
     * @return the (corrected) decision that we want/recommend the player takes.
     */
    String getDecision(final String responseText, final String decision, final int stepCount) {
      return decision;
    }

    /**
     * Handling that is to be done after the encounter is registered, but before the results are
     * processed.<br>
     * The selected option's {@link Attachment}s will be processed right after; this is the time to
     * update them.
     *
     * <p>Please avoid using
     *
     * <pre>
     * ResultProcessor.processAdventuresUsed( (int) );
     * </pre>
     *
     * or
     *
     * <pre>
     * ResultProcessor.processItem( (AdventureResult) );
     * </pre>
     *
     * Instead, use
     *
     * <pre>
     * this.options.get( decision ).turnCost( (int) );
     * </pre>
     *
     * and/or
     *
     * <pre>
     * this.options.get( decision ).attachItem( (AdventureResult), MANUAL );
     * </pre>
     *
     * <p>Will <b>only</b> be visited if the {@code whichchoice} and {@code option} fields were
     * found in the URL.<br>
     * Otherwise, this must have been a visit, so we go to {@link #visitChoice} instead.
     *
     * <p>Note that things such as modifying Preferences, Quest states, or printing text in the
     * GCLI/session logs belongs in {@link #postChoice2}
     *
     * <p>Q: But I'm already using postChoice1, do I REALLY HAVE to cut *this part* out and put it
     * in postChoice2??
     *
     * <p>A: Noooo... you don't "HAAAAAAVE" to..... Things will most likely not break if you don't
     * do it... ... <i>you lazy assh--</i>
     */
    void postChoice1(final GenericRequest request, final int decision) {}

    /** Handling that can or need to be done AFTER processing results. */
    void postChoice2(final GenericRequest request, final int decision) {}

    /**
     * Called during decoration after {@link #postChoice2} when we understand this request to be a
     * <b>response to</b> this choice adventure (if the user turned {@code relayShowSpoilers} on).
     *
     * @param option the option that the user selected for this choice adventure.
     */
    void decorateChoiceResponse(final StringBuffer buffer, final int option) {}

    /** The ChoiceAdventureNumber of this choice adventure. */
    public final int choice;
    /**
     * Not reeeaaaaaally used, since we instead currently rely on {@link
     * AdventureRequest#parseEncounter}, but hey, it's information.
     */
    public final String name;
    /** The location this choice adventure occurs in. */
    public final KoLAdventure
        source; // FIXME turn into a List, for CAs that can happen in more than 1 location?
    /**
     * If this choice adventure can happen in various locations. We won't modify {@link
     * KoLAdventure#lastVisitedLocation} when encountering it.
     */
    public final boolean undefinedSource;

    /** The options that <b>KoL</b> gives <b>us</b> */
    public final TreeMap<Integer, Option> options = new TreeMap<>();
    /**
     * The options that <b>we</b> give <b>the player</b> in {@link
     * net.sourceforge.kolmafia.swingui.panel.ChoiceOptionsPanel}, indexed by their {@link
     * CustomOption#customIndex}
     */
    public final TreeMap<Integer, CustomOption> customOptions = new TreeMap<>();

    private final List<Option> futureCustomOptions = new ArrayList<>();

    /**
     * A copy of the state of {@link #options} when exiting {@link #setup}. Loaded at the start of
     * every {@link #visitChoice}
     */
    private final TreeMap<Integer, Option> defaultOptions = new TreeMap<>();

    /**
     * @param choiceAdventureNumber
     * @param name
     * @param source the name of the location this choice adventure occurs in. On top of a location
     *     name, supports 3 special cases:
     *     <ol>
     *       <li>{@code null}: the choice adventure doesn't enter any queue, and it resulting in a
     *           turn spent isn't registered in any location.
     *       <li>{@code "undefined"}: the choice adventure can happen in more than a single
     *           location. We will trust the value of {@link KoLAdventure#lastVisitedLocation} when
     *           encountering those.
     *       <li>{@code "Item-Driven"}: same as {@code null}, but we note that this is because it
     *           comes from using an item. (NOTE/FIXME currently doesn't do anything if the CA
     *           doesn't enter ChoiceOptionsPanel, but may have been applied a bit liberally. Could
     *           be used for more?)
     *     </ol>
     *     be sure to note that items redirecting to choice adventures (or fights, FWIW) are *not*
     *     consumed based on items.txt, but rather based on {@link
     *     GenericRequest#checkItemRedirection}
     */
    ChoiceAdventure(final int choiceAdventureNumber, final String name, final String source) {
      this.choice = choiceAdventureNumber;
      this.name = name;

      if (source != null && source.equalsIgnoreCase("undefined")) {
        this.undefinedSource = true;
        this.source = null;
      } else if (source != null && source.equalsIgnoreCase("Item-Driven")) {
        this.undefinedSource = false;
        this.source = null;
        this.customZones.add("Item-Driven");
      } else {
        this.undefinedSource = false;
        this.source = AdventureDatabase.getAdventure(source);

        if (this.source != null) {
          this.customName = this.source.getAdventureName();
          this.customZones.add(this.source.getZone());
        }
      }

      if (database.containsKey(IntegerPool.get(this.choice))) {
        // Don't allow duplicates; it must have been a mistake.
        RequestLogger.printLine(
            "<font color=red>Duplicate Choice Adventure " + this.choice + "</font>");
        ChoiceAdventureDatabase.duplicates.add(this.choice);
        return;
      }

      database.put(IntegerPool.get(this.choice), this);
    }

    public final Option getOption(final Integer key) {
      Option value = this.options.get(key);
      if (value == null) {
        this.logUnknownOption(key, null);

        this.options.put(key, new Option(key));
      }
      return value;
    }

    /** Called before {@link #visitChoice} to spade new information */
    protected void scanForUnknownInformation(final GenericRequest request) {
      Map<Integer, String> choicesOffered = ChoiceUtilities.parseChoices(request.responseText);

      for (Map.Entry<Integer, String> choiceOffered : choicesOffered.entrySet()) {
        Integer index = choiceOffered.getKey();
        String text = choiceOffered.getValue();

        if (this.options.get(index) != null) {
          continue;
        }

        this.logUnknownOption(index, text);

        this.options.put(index, new Option(index));
      }
    }

    protected void logUnknownOption(final Integer index, final String text) {
      // FIXME Temporary
      String message = "PLEASE REPORT THIS unknown option " + this.choice + "/" + index;
      if (text != null) {
        message += " : " + text;
      }
      RequestLogger.printLine(message);
      RequestLogger.updateSessionLog(message);
    }

    /**
     * Called after {@link #setup} to save the options' default turn cost, queue entrance, popups,
     * etc...
     *
     * <p>They will be loaded before each {@link #visitChoice}, to avoid the troubles of having to
     * set them back.
     */
    private final void saveOptionsDefaults() {
      for (Map.Entry<Integer, Option> entry : this.options.entrySet()) {
        try {
          this.defaultOptions.put(entry.getKey(), (Option) entry.getValue().clone());
        } catch (CloneNotSupportedException e) {
          // this shouldn't happen, since they are Cloneable
          throw new InternalError(e);
        }
      }
    }

    @SuppressWarnings("unchecked")
    final void resetOptionsToDefault() {
      // can only remove from TreeMaps by index, and doing so
      // when iterating throws a ConcurrentModificationException
      // so remove after
      List<Option> toRemove = new ArrayList<>();

      for (Option option : this.options.values()) {
        Option defaultOption = this.defaultOptions.get(option.index);

        if (defaultOption == null) {
          toRemove.add(option);
          continue;
        }

        option.spoilerText = defaultOption.spoilerText;
        option.entersQueue = defaultOption.entersQueue;
        option.leadsTo = defaultOption.leadsTo;
        option.visitAhead = defaultOption.visitAhead;
        option.summaryFilter = defaultOption.summaryFilter;
        option.summarySeparator = defaultOption.summarySeparator;

        option.attachments = (ArrayList<Option.Attachment>) defaultOption.attachments.clone();
      }

      for (Option option : toRemove) {
        this.options.remove(option.index);
      }
    }

    private final void makeFutureCustomOption() {
      for (Option option : this.futureCustomOptions) {
        // use of new Option( int, null, true )[...] means
        // we are supposed to make displayText with the
        // content of the Attachments

        // currently only supports a single item/effect attachment
        // update me as needed

        String displayText = "unknown";

        for (Option.Attachment attachment : option.attachments) {
          if (attachment instanceof Option.ItemAttachment
              && attachment.adventureResult.getCount() > 0) {
            displayText = attachment.adventureResult.getDataName();
            break;
          }
          if (attachment instanceof Option.EffectAttachment) {
            // any way to get the enchantment of an effect?
            // would be better than just the name...
            displayText = attachment.adventureResult.getDataName();
            break;
          }
        }

        new CustomOption(option.index, displayText);
      }
    }

    /**
     * Simply adds "show in browser" as CustomOption 0 for any choice adventure that doesn't use its
     * CustomIndex 0 (note: unrelated to {@link #option0IsManualControl})
     */
    private final void finalizeCustomOptions() {
      if (this.customOptions.size() == 0) {
        return;
      }

      if (this.customOptions.get(IntegerPool.get(0)) == null) {
        new CustomOption(0, "show in browser");
      }
    }

    public final String getCustomName() {
      return this.customName;
    }

    public final int getCustomOrder() {
      return this.customOrder;
    }

    private int lastDecision = Integer.MAX_VALUE;

    /**
     * Remove every attachment marked as {@link ProcessType#MANUAL} on the Option we were taking.
     *
     * @return the option in question, to allow immediately adding attachments back to it.
     */
    final Option choiceFailed() {
      Option currentOption = getOption(this.lastDecision);

      Iterator<Option.Attachment> attachments = currentOption.attachments.iterator();

      while (attachments.hasNext()) {
        Option.Attachment attachment = attachments.next();
        if (attachment.processType == ProcessType.MANUAL) {
          attachments.remove();
        }
      }

      return currentOption;
    }

    /**
     * An option the player can select during a choice adventure, all the decorations we want to add
     * to this option, and all the impacts of selecting this option.
     */
    public final class Option implements Cloneable {
      public final int index;

      public String spoilerText;

      /**
       * Whether or not selecting this option causes the choice adventure to enter the zone's NC
       * queue. (not worth bothering with if this {@link ChoiceAdventure#source} is {@code null})
       *
       * <p>If in a choice adventure chain, it can be {@code true} at any moment where we're sure
       * the initial adventure is added to the queue; no need for it to be at the "end" (mostly
       * because we can never really know when we reach that), nor to make sure it's only {@code
       * true} once per choice chain.
       */
      public boolean entersQueue = true;

      /** If this option makes you go to another choice adventure. */
      public int leadsTo = 0;
      /**
       * If it's safe (and worth it) to call that choice's {@link ChoiceAdventure#visitChoice} from
       * another choice adventure.
       */
      public boolean visitAhead = false;

      public Predicate<Option> summaryFilter = null;
      public String summarySeparator = " <span style='font-size: x-large;'>/</span> ";

      /**
       * every {@link Attachment} currently associated with this option.
       *
       * @see Attachment
       */
      public ArrayList<Attachment> attachments = new ArrayList<>();

      /**
       * Please only use this during {@link ChoiceAdventure#setup}
       *
       * @param decision this Option's index
       */
      public Option(int decision) {
        this(decision, null, false);
      }

      /**
       * Please only use this during {@link ChoiceAdventure#setup}
       *
       * @param decision this Option's index
       * @param spoilerText the text written under this option (if we find it)
       */
      public Option(final int decision, final String spoilerText) {
        this(decision, spoilerText, false);
      }

      /**
       * Please only use this during {@link ChoiceAdventure#setup}
       *
       * <p>if {@code alsoAsCustomOption} is true, equivalent of
       *
       * <pre>new Option( *an_int*, *some_text* );
       * new CustomOption( *the_same_int*, *the_same_text* );</pre>
       *
       * @param decision this Option/CustomOption's index
       * @param spoilerText the text written under this option (if we find it) and for this
       *     CustomOption (NOTE if {@code null}, we'll generate a displayText using this option's
       *     {@link #attachments}. Use this if KoL's text is straightforward/ explicit enough (and
       *     you're too lazy to manually make the {@link CustomOption}))
       * @param alsoAsCustomOption
       */
      public Option(final int index, final String spoilerText, final boolean alsoAsCustomOption) {
        this.index = index;
        this.spoilerText = spoilerText;

        ChoiceAdventure.this.options.put(this.index, this);

        if (alsoAsCustomOption) {
          if (spoilerText != null) {
            new CustomOption(index, spoilerText);
          } else {
            // make the CustomOption once we have the Attachments
            ChoiceAdventure.this.futureCustomOptions.add(this);
          }
        }
      }

      /**
       * Replaces the decorative text of this option with {@code newText}.
       *
       * @param newText the new value for this option's spoiler text. If {@code null}, the spoiler
       *     text will be removed.
       * @return itself, to allow chaining.
       */
      public final Option text(final String newText) {
        this.spoilerText = newText;
        return this;
      }

      /**
       * Whether or not selecting this option adds this choice adventure (or, if part of a choice
       * adventure chain, the choice adventure you got first) to the adventure queue.
       *
       * <p>Is not worth bothering with if this choice adventure's {@link ChoiceAdventure#source} is
       * {@code null}
       *
       * <p>If the {@link ChoiceAdventure#neverEntersQueue} field of the ChoiceAdventure this Option
       * is in is set to {@code true}, this function is useless, as said field has priority.
       *
       * @return itself, to allow chaining.
       */
      public final Option entersQueue(final boolean entersQueue) {
        this.entersQueue = entersQueue;
        return this;
      }

      /**
       * If this option leads directly to another choice adventure.
       *
       * <p><b>Also automatically sets this option's {@link #entersQueue} to {@code false}.</b> To
       * set it back to {@code true}, be sure to call {@link #entersQueue(boolean)} <b>after</b>
       * this method.
       *
       * @param choiceAdventureNumber the choiceAdventureNumber of the choice adventure this will
       *     lead us to
       * @return itself, to allow chaining.
       */
      public final Option leadsTo(final int choiceAdventureNumber) {
        return this.leadsTo(choiceAdventureNumber, false);
      }

      /**
       * If this option leads directly to another choice adventure.
       *
       * <p><b>Also automatically sets this option's {@link #entersQueue} to {@code false}.</b> To
       * set it back to {@code true}, be sure to call {@link #entersQueue(boolean)} <b>after</b>
       * this method.
       *
       * @param choiceAdventureNumber the choiceAdventureNumber of the choice adventure this will
       *     lead us to
       * @param isLookaheadSafe {@code true} if we should, <b>and can</b> call the following {@link
       *     ChoiceAdventure#visitChoice} safely.
       *     <p>A choice adventure is lookaheadSafe when its {@link ChoiceAdventure#visitChoice}
       *     method either doesn't look at its {@code request} parameter, or handles the possibility
       *     of it being {@code null} (since this will be its value when we lookahead).
       *     <p>Most visitChoice methods are technically lookaheadSafe by default, since they rarely
       *     use their {@code request} parameter. However, for safety, they will be assumed to be
       *     {@code false} by default, and should only be set to {@code true} if that choice
       *     adventure actually has options that change dynamically.
       *     <p>It is also recommended that, when marking a choice adventure as lookaheadSafe, a
       *     comment is added in the visitChoice method of that choice adventure in question, so
       *     that people remember to have it stay that way when editing the method in the future.
       * @return itself, to allow chaining.
       */
      public final Option leadsTo(final int choiceAdventureNumber, final boolean isLookaheadSafe) {
        return this.leadsTo(choiceAdventureNumber, isLookaheadSafe, null);
      }

      /**
       * If this option leads directly to another choice adventure.
       *
       * <p><b>Also automatically sets this option's {@link #entersQueue} to {@code false}.</b> To
       * set it back to {@code true}, be sure to call {@link #entersQueue(boolean)} <b>after</b>
       * this method.
       *
       * @param choiceAdventureNumber the choiceAdventureNumber of the choice adventure this will
       *     lead us to
       * @param isLookaheadSafe {@code true} if we should, <b>and can</b> call the following {@link
       *     ChoiceAdventure#visitChoice} safely.
       *     <p>A choice adventure is lookaheadSafe when its {@link ChoiceAdventure#visitChoice}
       *     method either doesn't look at its {@code request} parameter, or handles the possibility
       *     of it being {@code null} (since this will be its value when we lookahead).
       *     <p>Most visitChoice methods are technically lookaheadSafe by default, since they rarely
       *     use their {@code request} parameter. However, for safety, they will be assumed to be
       *     {@code false} by default, and should only be set to {@code true} if that choice
       *     adventure actually has options that change dynamically.
       *     <p>It is also recommended that, when marking a choice adventure as lookaheadSafe, a
       *     comment is added in the visitChoice method of that choice adventure in question, so
       *     that people remember to have it stay that way when editing the method in the future.
       * @param summaryFilter if not {@code null}, the options' spoiler text of the choice this
       *     option is leading to will be joined together into a summary, which will be placed under
       *     this option's spoiler text. This Predicate filters which option is accepted into the
       *     summary (being submitted each option as an argument, and returning if it accepted or
       *     not).
       *     <p>To accept everything, just use
       *     <pre>( Option o ) -> true</pre>
       *
       * @return itself, to allow chaining.
       */
      public final Option leadsTo(
          final int choiceAdventureNumber,
          final boolean isLookaheadSafe,
          final Predicate<Option> summaryFilter) {
        this.leadsTo = choiceAdventureNumber;
        this.entersQueue = false;
        this.visitAhead = isLookaheadSafe;
        this.summaryFilter = summaryFilter;
        return this;
      }

      /**
       * If this option leads directly to another choice adventure.
       *
       * <p><b>Also automatically sets this option's {@link #entersQueue} to {@code false}.</b> To
       * set it back to {@code true}, be sure to call {@link #entersQueue(boolean)} <b>after</b>
       * this method.
       *
       * @param choiceAdventureNumber the choiceAdventureNumber of the choice adventure this will
       *     lead us to
       * @param isLookaheadSafe {@code true} if we should, <b>and can</b> call the following {@link
       *     ChoiceAdventure#visitChoice} safely.
       *     <p>A choice adventure is lookaheadSafe when its {@link ChoiceAdventure#visitChoice}
       *     method either doesn't look at its {@code request} parameter, or handles the possibility
       *     of it being {@code null} (since this will be its value when we lookahead).
       *     <p>Most visitChoice methods are technically lookaheadSafe by default, since they rarely
       *     use their {@code request} parameter. However, for safety, they will be assumed to be
       *     {@code false} by default, and should only be set to {@code true} if that choice
       *     adventure actually has options that change dynamically.
       *     <p>It is also recommended that, when marking a choice adventure as lookaheadSafe, a
       *     comment is added in the visitChoice method of that choice adventure in question, so
       *     that people remember to have it stay that way when editing the method in the future.
       * @param summaryFilter if not {@code null}, the options' spoiler text of the choice this
       *     option is leading to will be joined together into a summary, which will be placed under
       *     this option's spoiler text. This Predicate filters which option is accepted into the
       *     summary (being submitted each option as an argument, and returning if it accepted or
       *     not).
       *     <p>To accept everything, just use
       *     <pre>( Option o ) -> true</pre>
       *
       * @param separator what should separate the spoilerTexts, if there ends up being more than
       *     one.
       * @return itself, to allow chaining.
       */
      public final Option leadsTo(
          final int choiceAdventureNumber,
          final boolean isLookaheadSafe,
          final Predicate<Option> summaryFilter,
          final String separator) {
        this.summarySeparator = Objects.requireNonNull(separator);
        return this.leadsTo(choiceAdventureNumber, isLookaheadSafe, summaryFilter);
      }

      /**
       * Resets the <b>display-related</b> information of this Option.
       *
       * <p>This method is intended to be used during {@link ChoiceAdventure#visitChoice} <i>look
       * aheads</i> (the only moment when {@code request} is {@code null});
       *
       * @return itself, to allow chaining.
       */
      public final Option reset() {
        return this.reset(null);
      }

      /**
       * Resets the <b>display-related</b> information of this Option, and sets {@link #spoilerText}
       * to the given value.
       *
       * <p>This method is intended to be used during {@link ChoiceAdventure#visitChoice} <i>look
       * aheads</i> (the only moment when {@code request} is {@code null});
       *
       * @param newText the new value of {@link #spoilerText}
       * @return itself, to allow chaining.
       */
      public final Option reset(final String newText) {
        this.spoilerText = newText;
        this.leadsTo = 0;
        this.visitAhead = false;
        this.summaryFilter = null;
        this.summarySeparator = " <span style='font-size: x-large;'>/</span> ";
        this.attachments.clear();
        return this;
      }

      @Override
      public Object clone() throws CloneNotSupportedException {
        return super.clone();
      }

      /** Called from {@link ChoiceManager#getNextChoiceSummary} */
      public final ChoiceAdventure lookAhead() {
        ChoiceAdventure nextChoiceAdventure =
            ChoiceAdventureDatabase.getChoiceAdventure(this.leadsTo);

        if (nextChoiceAdventure instanceof UnknownChoiceAdventure) {
          return null;
        }

        nextChoiceAdventure.resetOptionsToDefault();

        if (this.visitAhead) {
          nextChoiceAdventure.visitChoice(null);
        }

        return nextChoiceAdventure;
      }

      /**
       * An {@link AdventureResult} attached to this option, along with information about how to
       * display and process it.
       *
       * <p>They are currently used for 3 purposes:
       *
       * <ul>
       *   <li><b>Decoration</b>: if the user enabled {@code relayShowSpoilers}, attachments can be
       *       displayed along the spoiler texts, providing links to descriptions and information
       *       about their possessions.
       *   <li><b>Decision choosing</b>: during {@link ChoiceManager#pickGoalOption}, we use
       *       attachments to find options that contain goals that the user currently has, and avoid
       *       options that cost things that they lack
       *   <li><b>Manual result processing</b>: {@link ProcessType#MANUAL} attachments are used to
       *       process AdventureResults that don't get picked up by ResultProcessor
       * </ul>
       */
      public abstract class Attachment {
        public final AdventureResult adventureResult;
        public final ProcessType processType;
        public final DisplayType displayType;

        private Attachment(
            final AdventureResult adventureResult,
            final ProcessType processType,
            final DisplayType displayType) {
          this.adventureResult = adventureResult;
          this.processType = processType;
          this.displayType = displayType;
        }

        public abstract boolean haveEnough();

        public final String getAttachmentAnnotation() {
          if (this.displayType instanceof NoDisplay) {
            return null;
          }

          StringBuilder result = new StringBuilder("<img height=30 valign=middle ");

          for (String attribute : new String[] {"alt", "title"}) {
            result.append(attribute);
            result.append("=\"");
            result.append(this.adventureResult.getName());
            result.append("\" ");
          }

          if (this.adventureResult.isItem()) {
            String imagePath = ItemDatabase.getImage(this.adventureResult.getItemId());

            result.append("src=\"");
            if (imagePath == null) {
              result.append("/images/debug.gif");
            } else {
              result.append(KoLmafia.imageServerPath());
              if (!imagePath.contains("/")) {
                result.append("itemimages/");
              }
              result.append(imagePath);
            }
            result.append("\" onclick=\"descitem('");
            result.append(ItemDatabase.getDescriptionId(this.adventureResult.getItemId()));
            result.append("');\"");
          } else if (this.adventureResult.isStatusEffect()) {
            result.append("src=\"");
            result.append(EffectDatabase.getImage(this.adventureResult.getEffectId()));
            result.append("\" onclick=\"eff('");
            result.append(EffectDatabase.getDescriptionId(this.adventureResult.getEffectId()));
            result.append("');\"");
          }

          result.append(">");

          if (this.displayType instanceof DisplayAll) {
            if (this instanceof ItemAttachment) {
              result.append("(");
              result.append(((DisplayAll) this.displayType).getAnnotation(this.adventureResult));
              result.append(")");
            } else {
              // No other attachment currently supports this
            }
          }

          return result.toString();
        }

        /**
         * @return the index of where the return value of {@link #getAttachmentAnnotation} should
         *     go, in this {@link Option}'s {@link Option#spoilerText}.
         */
        public final int getAnnotationTargetIndex() {
          if (this.displayType instanceof NoDisplay || Option.this.spoilerText == null) {
            return -1;
          }

          Matcher matcher = this.displayType.pattern.matcher(Option.this.spoilerText);

          if (!matcher.find()) {
            return -1;
          }

          return matcher.start();
        }
      }

      public final class AdvAttachment extends Attachment {
        private AdvAttachment(final int amount, final ProcessType processType) {
          super(new AdventureResult(AdventureResult.ADV, amount), processType, new NoDisplay());
        }

        public KoLAdventure getSource() {
          if (ChoiceAdventure.this.undefinedSource) {
            return KoLAdventure.lastVisitedLocation();
          }

          return ChoiceAdventure.this.source;
        }

        @Override
        public boolean haveEnough() {
          // we can't even enter most choice adventures without any adventures
          // left, and those that cost more than 1 adventure don't always
          // stop you if you have less than required, so assume you always can
          return true;
        }
      }

      /**
       * The amount of turns clicking this option makes you *spend*, incrementing the amount of
       * turns played for the day/total (e.g. ticks down ronin counter), and incrementing the amount
       * of turns spent in the zone (if any), as opposed to {@link #attachAdv(int)}, which are
       * adventure gains/losses that only affect your remaining adventures for the day.
       *
       * <p>Always* {@link ProcessType#MANUAL}. *Some very very rare exceptions exist.
       *
       * @param cost the (<i>positive</i>) amount of turns selecting this option makes you spend.
       * @return itself, to allow chaining.
       */
      public final Option turnCost(final int cost) {
        return this.turnCost(cost, ProcessType.MANUAL);
      }

      /**
       * You should only directly call this method to supply it {@link ProcessType#AUTO}, which
       * should be next to never.
       */
      public final Option turnCost(final int cost, final ProcessType processType) {
        if (cost < 0) {
          RequestLogger.printLine(
              "<font color=red>A negative cost is a gain. You didn't discover an adventure that consistently gives free adventures, did you?</font>");
          RequestLogger.printLine("@ Option.turnCost(int) ");
          RequestLogger.printLine("@ Option " + this.index);
          RequestLogger.printLine("@ ChoiceAdventure " + ChoiceAdventure.this.choice);
          return this;
        }
        if (cost != 0) {
          this.attachments.add(new AdvAttachment(-cost, processType));
        }

        return this;
      }

      /**
       * An amount of adventures gained or lost.
       *
       * <p>Differs from {@link #turnCost(int)} in that those don't count as "turns"; they don't
       * increment the amount of "turns played" in your character pane (if negative).
       *
       * <p>Always {@link ProcessType#AUTO}, as KoL always discloses these.
       *
       * @return itself, to allow chaining.
       */
      public final Option attachAdv(final int amount) {
        if (amount != 0) {
          this.attachments.add(new AdvAttachment(amount, ProcessType.AUTO));
        }

        return this;
      }

      public final class EffectAttachment extends Attachment {
        private EffectAttachment(final AdventureResult effect, final DisplayType displayType) {
          super(effect, ProcessType.AUTO, displayType);
        }

        @Override
        public boolean haveEnough() {
          // we currently don't support effects being required
          return true;
        }
      }

      /**
       * Note: Please avoid this method when outside of {@link ChoiceAdventure#setup}, as it means
       * wasting time doing fuzzy matching every time we visit that choice adventure.<br>
       * Save the effect as a local variable using {@code new AdventureResult( effectName, 0, true
       * )} yourself, and use {@link #attachEffect(AdventureResult)} instead.
       *
       * <p>An effect merely mentioned or required for this option. Adds a way to quickly see its
       * description with no other processing.
       *
       * @param effectName the name of the effect to add.
       * @return itself, to allow chaining.
       */
      public final Option attachEffect(final String effectName) {
        return this.attachEffect(effectName, new ImageOnly());
      }

      /**
       * An effect merely mentioned or required for this option. Adds a way to quickly see its
       * description with no other processing.
       *
       * @param effectID the ID of the effect to add.
       * @return itself, to allow chaining.
       */
      public final Option attachEffect(final int effectID) {
        return this.attachEffect(effectID, new ImageOnly());
      }

      /**
       * An effect merely mentioned or required for this option. Adds a way to quickly see its
       * description with no other processing.
       *
       * @param effect the effect to add.
       * @return itself, to allow chaining.
       */
      public final Option attachEffect(final AdventureResult effect) {
        return this.attachEffect(effect, new ImageOnly());
      }

      /**
       * Note: Please avoid this method when outside of {@link ChoiceAdventure#setup}, as it means
       * wasting time doing fuzzy matching every time we visit that choice adventure.<br>
       * Save the effect as a local variable using {@code new AdventureResult( effectName, 0, true
       * )} yourself, and use {@link #attachEffect(AdventureResult, ImageOnly)} instead.
       *
       * <p>An effect merely mentioned or required for this option. Adds a way to quickly see its
       * description with no other processing.
       *
       * @param effectName the name of the effect to add.
       * @param displayType an {@link ImageOnly} indicating where the popup-opening image should go.
       * @return itself, to allow chaining.
       */
      public final Option attachEffect(final String effectName, final ImageOnly displayType) {
        return this.attachEffect(new AdventureResult(effectName, 0, true), displayType);
      }

      /**
       * An effect merely mentioned or required for this option. Adds a way to quickly see its
       * description with no other processing.
       *
       * @param effectID the ID of the effect to add.
       * @param displayType an {@link ImageOnly} indicating where the popup-opening image should go.
       * @return itself, to allow chaining.
       */
      public final Option attachEffect(final int effectID, final ImageOnly displayType) {
        return this.attachEffect(new AdventureResult(effectID, 0, true), displayType);
      }

      /**
       * An effect merely mentioned or required for this option. Adds a way to quickly see its
       * description with no other processing.
       *
       * @param effect the effect to add.
       * @param displayType an {@link ImageOnly} indicating where the popup-opening image should go.
       * @return itself, to allow chaining.
       */
      public final Option attachEffect(final AdventureResult effect, final ImageOnly displayType) {
        if (!effect.isStatusEffect()) {
          RequestLogger.printLine(
              "<font color=red>Submitted AdventureResult "
                  + effect.getDisambiguatedName()
                  + " is not an effect</font>");
          RequestLogger.printLine("@ Option.attachEffect(AdventureResult, [...]) ");
          RequestLogger.printLine("@ Option " + this.index);
          RequestLogger.printLine("@ ChoiceAdventure " + ChoiceAdventure.this.choice);
        } else if (effect.getEffectId() == -1) {
          RequestLogger.printLine(
              "<font color=red>Bad effect name " + effect.getName() + "</font>");
          RequestLogger.printLine("@ Option.attachEffect([...]) ");
          RequestLogger.printLine("@ Option " + this.index);
          RequestLogger.printLine("@ ChoiceAdventure " + ChoiceAdventure.this.choice);
        } else {
          this.attachments.add(new EffectAttachment(effect, displayType));
        }

        return this;
      }

      public final class ItemAttachment extends Attachment {
        private ItemAttachment(
            final AdventureResult item,
            final ProcessType processType,
            final DisplayType displayType) {
          super(item, processType, displayType);
        }

        @Override
        public boolean haveEnough() {
          return this.adventureResult.getCount()
                  + this.adventureResult.getCount(KoLConstants.inventory)
              >= 0;
        }
      }

      /**
       * An item merely mentioned or required for this option. Adds a way to quickly see its
       * description with no other processing.
       *
       * <p>Will add a link to the item's description, followed by how many the player has in
       * inventory, at the end of the line.
       *
       * @param itemName the name of the item to add.
       * @return itself, to allow chaining.
       */
      public final Option attachItem(final String itemName) {
        return this.attachItem(itemName, 0, ProcessType.AUTO, new DisplayAll());
      }

      /**
       * An item merely mentioned or required for this option. Adds a way to quickly see its
       * description with no other processing.
       *
       * <p>Will add a link to the item's description, followed by how many the player has in
       * inventory, at the end of the line.
       *
       * @param itemID the ID of the item to add.
       * @return itself, to allow chaining.
       */
      public final Option attachItem(final int itemID) {
        return this.attachItem(itemID, 0, ProcessType.AUTO, new DisplayAll());
      }

      /**
       * An item merely mentioned or required for this option. Adds a way to quickly see its
       * description with no other processing.
       *
       * <p>Will add a link to the item's description, followed by how many the player has in
       * inventory, at the end of the line.
       *
       * @param item the item to add.
       * @return itself, to allow chaining.
       */
      public final Option attachItem(final AdventureResult item) {
        return this.attachItem(item.getInstance(0), ProcessType.AUTO, new DisplayAll());
      }

      /**
       * An item merely mentioned or required for this option. Adds a way to quickly see its
       * description with no other processing.
       *
       * @param itemName the name of the item to add.
       * @param displayType a {@link DisplayType} indicating how and where this attachment's text
       *     and/or image should go.
       * @return itself, to allow chaining.
       */
      public final Option attachItem(final String itemName, final DisplayType displayType) {
        return this.attachItem(itemName, 0, ProcessType.AUTO, displayType);
      }

      /**
       * An item merely mentioned or required for this option. Adds a way to quickly see its
       * description with no other processing.
       *
       * @param itemID the ID of the item to add.
       * @param displayType a {@link DisplayType} indicating how and where this attachment's text
       *     and/or image should go.
       * @return itself, to allow chaining.
       */
      public final Option attachItem(final int itemID, final DisplayType displayType) {
        return this.attachItem(itemID, 0, ProcessType.AUTO, displayType);
      }

      /**
       * An item merely mentioned or required for this option. Adds a way to quickly see its
       * description with no other processing.
       *
       * @param item the item to add.
       * @param displayType a {@link DisplayType} indicating how and where this attachment's text
       *     and/or image should go.
       * @return itself, to allow chaining.
       */
      public final Option attachItem(final AdventureResult item, final DisplayType displayType) {
        return this.attachItem(item.getInstance(0), ProcessType.AUTO, displayType);
      }

      /**
       * Note: Please avoid this method when outside of {@link ChoiceAdventure#setup}, as it means
       * wasting time doing fuzzy matching every time we visit that choice adventure.<br>
       * Save the item as a local variable using {@code new AdventureResult( itemName, amount, false
       * )} yourself, and use {@link #attachItem(AdventureResult, ProcessType)} instead.
       *
       * <p>An item that we will (should) gain (if {@code amount} is positive) / lose (if {@code
       * amount} is negative) as a result of taking this choice.
       *
       * <p>Will add a link to the item's description, followed by how many the player has in
       * inventory, at the end of the line.
       *
       * @param itemName the name of the item
       * @param amount how many of that item will be gained (if positive)/lost (if negative)
       * @param processType {@link ProcessType#AUTO} if KoL will show a clear "you acquire an item:
       *     *item*" when this attachment happens. {@link ProcessType#MANUAL} otherwise.
       * @return itself, to allow chaining.
       */
      public final Option attachItem(
          final String itemName, final int amount, final ProcessType processType) {
        return this.attachItem(itemName, amount, processType, new DisplayAll());
      }

      /**
       * An item that we will (should) gain (if {@code amount} is positive) / lose (if {@code
       * amount} is negative) as a result of taking this choice.
       *
       * <p>Will add a link to the item's description, followed by how many the player has in
       * inventory, at the end of the line.
       *
       * @param itemID the ID of the item
       * @param amount how many of that item will be gained (if positive)/lost (if negative)
       * @param processType {@link ProcessType#AUTO} if KoL will show a clear "you acquire an item:
       *     *item*" when this attachment happens. {@link ProcessType#MANUAL} otherwise.
       * @return itself, to allow chaining.
       */
      public final Option attachItem(
          final int itemID, final int amount, final ProcessType processType) {
        return this.attachItem(itemID, amount, processType, new DisplayAll());
      }

      /**
       * An item that we mention / will (should) gain (if {@code item.count} is positive) / will
       * (should) lose (if {@code item.count} is negative) as a result of taking this choice.
       *
       * <p>Will add a link to the item's description, followed by how many the player has in
       * inventory, at the end of the line.
       *
       * @param item the item in question
       * @param processType {@link ProcessType#AUTO} if KoL will show a clear "you acquire an item:
       *     *item*" when this attachment happens. {@link ProcessType#MANUAL} otherwise.
       * @return itself, to allow chaining.
       */
      public final Option attachItem(final AdventureResult item, final ProcessType processType) {
        return this.attachItem(item, processType, new DisplayAll());
      }

      /**
       * Note: Please avoid this method when outside of {@link ChoiceAdventure#setup}, as it means
       * wasting time doing fuzzy matching every time we visit that choice adventure.<br>
       * Save the item as a local variable using {@code new AdventureResult( itemName, amount, false
       * )} yourself, and use {@link #attachItem(AdventureResult, ProcessType, DisplayType)}
       * instead.
       *
       * <p>An item that we will (should) gain (if {@code amount} is positive) / lose (if {@code
       * amount} is negative) as a result of taking this choice.
       *
       * @param itemName the name of the item
       * @param amount how many of that item will be gained (if positive)/lost (if negative)
       * @param processType {@link ProcessType#AUTO} if KoL will show a clear "you acquire an item:
       *     *item*" when this attachment happens. {@link ProcessType#MANUAL} otherwise.
       * @param displayType a {@link DisplayType} indicating if, where and how this attachment's
       *     text and/or image should go.
       * @return itself, to allow chaining.
       */
      public final Option attachItem(
          final String itemName,
          final int amount,
          final ProcessType processType,
          final DisplayType displayType) {
        return this.attachItem(
            new AdventureResult(itemName, amount, false), processType, displayType);
      }

      /**
       * An item that we will (should) gain (if {@code amount} is positive) / lose (if {@code
       * amount} is negative) as a result of taking this choice.
       *
       * @param itemID the ID of the item
       * @param amount how many of that item will be gained (if positive)/lost (if negative)
       * @param processType {@link ProcessType#AUTO} if KoL will show a clear "you acquire an item:
       *     *item*" when this attachment happens. {@link ProcessType#MANUAL} otherwise.
       * @param displayType a {@link DisplayType} indicating if, where and how this attachment's
       *     text and/or image should go.
       * @return itself, to allow chaining.
       */
      public final Option attachItem(
          final int itemID,
          final int amount,
          final ProcessType processType,
          final DisplayType displayType) {
        return this.attachItem(
            new AdventureResult(itemID, amount, false), processType, displayType);
      }

      /**
       * An item that we mention / will gain / will lose as a result of taking this choice.
       *
       * @param item the item in question
       * @param processType {@link ProcessType#AUTO} if KoL will show a clear "you acquire an item:
       *     *item*" when this attachment happens. {@link ProcessType#MANUAL} otherwise.
       * @param displayType a {@link DisplayType} indicating if, where and how this attachment's
       *     text and/or image should go.
       * @return itself, to allow chaining.
       */
      public final Option attachItem(
          final AdventureResult item,
          final ProcessType processType,
          final DisplayType displayType) {
        if (!item.isItem()) {
          RequestLogger.printLine(
              "<font color=red>Submitted AdventureResult "
                  + item.getDisambiguatedName()
                  + " is not an item</font>");
          RequestLogger.printLine("@ Option.attachItem(AdventureResult, [...]) ");
          RequestLogger.printLine("@ Option " + this.index);
          RequestLogger.printLine("@ ChoiceAdventure " + ChoiceAdventure.this.choice);
        } else if (item.getItemId() == -1) {
          RequestLogger.printLine("<font color=red>Bad item name " + item.getName() + "</font>");
          RequestLogger.printLine("@ Option.attachItem([...]) ");
          RequestLogger.printLine("@ Option " + this.index);
          RequestLogger.printLine("@ ChoiceAdventure " + ChoiceAdventure.this.choice);
        } else {
          if (!(displayType instanceof NoDisplay && item.getCount() == 0)) {
            this.attachments.add(new ItemAttachment(item, processType, displayType));
          }
        }

        return this;
      }

      public final class MeatAttachment extends Attachment {
        private MeatAttachment(final long amount, final ProcessType processType) {
          super(
              new AdventureLongCountResult(AdventureResult.MEAT, amount),
              processType,
              new NoDisplay());
        }

        @Override
        public boolean haveEnough() {
          return this.adventureResult.getLongCount() + KoLCharacter.getAvailableMeat() >= 0;
        }
      }

      /**
       * A change in meat as a result of selecting this option.
       *
       * @param amount how much meat will be gained (if {@code amount} is positive)/ lost (if {@code
       *     amount} is negative)
       * @param processType {@link ProcessType#AUTO} if KoL will show a clear "you gain/lose/spend #
       *     meat" when this attachment happens. {@link ProcessType#MANUAL} otherwise.
       * @return itself, to allow chaining.
       */
      public final Option attachMeat(final long amount, final ProcessType processType) {
        if (amount != 0) {
          this.attachments.add(new MeatAttachment(amount, processType));
        }

        return this;
      }

      public final class MPAttachment extends Attachment {
        private MPAttachment(final long amount) {
          super(
              new AdventureLongCountResult(AdventureResult.MP, amount),
              ProcessType.AUTO,
              new NoDisplay());
        }

        @Override
        public boolean haveEnough() {
          return this.adventureResult.getLongCount() + KoLCharacter.getCurrentMP() >= 0;
        }
      }

      /**
       * A change in MP as a result of selecting this option.
       *
       * <p>Always {@link ProcessType#AUTO}.
       *
       * @param amount how much MP will be gained/lost
       * @return itself, to allow chaining.
       */
      public final Option attachMP(final long amount) {
        if (amount != 0) {
          this.attachments.add(new MPAttachment(amount));
        }

        return this;
      }
    }

    /**
     * An option the player can select in {@link
     * net.sourceforge.kolmafia.swingui.panel.ChoiceOptionsPanel}
     */
    public final class CustomOption {
      /**
       * Represents this customOption's index/order in its comboBox.
       *
       * <p>This number doesn't have to be related to {@link #optionIndex} in any way; it could be
       * {@code -500}, {@code 0}, {@code 69} or {@code 420} for all we care, as long as this {@link
       * ChoiceAdventure#customLoad} and {@link #selectionHandler} properly act as the bridge
       * between Preference value and {@link #customIndex}.
       *
       * <p>The only thing to note is that if a {@link ChoiceAdventure} doesn't have a {@link
       * CustomOption} with a {@link #customIndex} of {@code 0}, one will be created with the value
       * {@code "show in browser"}.
       */
      public final int customIndex;
      /**
       * The {@link Option#index} of the {@link Option} this customOption is tied to (unless its
       * {@link ChoiceAdventure} uses a {@link ChoiceAdventure#customLoad})
       */
      public final int optionIndex;
      /**
       * Text visible in the comboBox. Note that {@code "complete the outfit"} carries a special
       * meaning: during {@link ChoiceManager#pickGoalOption(int, String)}, if {@code decision}
       * (turned as an integer) is equal to this customOption's {@link #optionIndex}, we'll pick the
       * first {@link Option} we see that gives an item we don't have.
       */
      public final String displayText;
      /**
       * A Functional Interface that gets called whenever the user selects this {@link CustomOption}
       * in it's comboBox, in {@link net.sourceforge.kolmafia.swingui.panel.ChoiceOptionsPanel}.
       *
       * <p>Handles setting Preferences according to this selection. Defaults to
       *
       * <pre>() -> {
       * 	Preferences.setString(
       * 		"choiceAdventure" + ChoiceAdventure.this.choice,
       * 		String.valueOf( this.optionIndex ) );
       * }</pre>
       *
       * <p>NOTE: if changed to anything but the default, be sure to also set a {@link
       * ChoiceAdventure#customLoad}
       */
      public final Runnable selectionHandler;

      /**
       * @param index the {@link #customIndex} and {@link #optionIndex} of this customOption. Note
       *     that unless {@code displayText} is {@code "complete the outfit"}, this number must
       *     equal the {@link Option#index} of an existing {@link Option}
       * @param displayText the text visible in the comboBox. Note that {@code "complete the
       *     outfit"} has special handling in {@link ChoiceManager#pickGoalOption(int, String)} (see
       *     {@link #displayText})
       */
      CustomOption(final int index, final String displayText) {
        this(index, displayText, index);
      }

      /**
       * @param customIndex the index of this customOption in its comboBox
       * @param displayText the text visible in the comboBox. Note that {@code "complete the
       *     outfit"} has special handling in {@link ChoiceManager#pickGoalOption(int, String)} (see
       *     {@link #displayText})
       * @param optionIndex the {@link Option#index} of the {@link Option} corresponding to this
       *     customOption (unless {@code displayText} is {@code "complete the outfit"}; then this
       *     should be anything BUT the {@link Option#index} of an existing {@link Option})
       */
      CustomOption(final int customIndex, final String displayText, final int optionIndex) {
        this.customIndex = customIndex;
        this.optionIndex = optionIndex;
        this.displayText = displayText;
        this.selectionHandler =
            () ->
                Preferences.setString(
                    "choiceAdventure" + ChoiceAdventure.this.choice,
                    String.valueOf(this.optionIndex));

        this.addToCustomOptionList();
      }

      /**
       * @param customIndex the index of this customOption in its comboBox
       * @param displayText the text visible in the comboBox.
       * @param selectionHandler the function in charge of updating (a) preference(s) when this
       *     customOption is selected, in its comboBox.
       * @see ChoiceAdventure#customLoad
       * @see ChoiceAdventure#customPreferencesToListen
       */
      CustomOption(
          final int customIndex, final String displayText, final Runnable selectionHandler) {
        this.customIndex = customIndex;
        // if they made their own selectionHandler, they'll have to
        // also make their own customLoad, which handles the
        // transition between optionIndex (preference value) and customIndex
        this.optionIndex = customIndex;
        this.displayText = displayText;
        this.selectionHandler = selectionHandler;

        this.addToCustomOptionList();
      }

      /**
       * Only exists because you can't call {@code this} when calling another constructor, so {@link
       * #CustomOption(int, String, int)} and {@link #CustomOption(int, String, Runnable)} had to be
       * two different constructors...
       */
      private final void addToCustomOptionList() {
        CustomOption previous = customOptions.putIfAbsent(IntegerPool.get(this.customIndex), this);

        if (previous != null) {
          // Don't allow duplicates; it must have been a mistake.
          RequestLogger.printLine(
              "<font color=red>Duplicate CustomOption " + this.customIndex + "</font>");
          RequestLogger.printLine(
              "<font color=red>"
                  + previous.displayText
                  + " (old) vs "
                  + this.displayText
                  + " (new)</font>");
          RequestLogger.printLine("@ ChoiceAdventure " + ChoiceAdventure.this.choice);
        }
      }

      public int getChoice() {
        return ChoiceAdventure.this.choice;
      }

      @Override
      public String toString() {
        return CharacterEntities.unescape(this.displayText);
      }
    }

    // the rest of this class (ChoiceAdventure) is meant for
    // Option.Attachment, but is put here to make it accessible
    // from ChoiceAdventure anonymous constructors

    /**
     * Class containing everything related to how the {@link Option.Attachment}s are integrated to
     * their {@link Option}'s spoiler text.
     */
    public abstract class DisplayType {
      protected Pattern pattern;
    }

    public final class NoDisplay extends DisplayType {
      /** Not displayed at all. */
      NoDisplay() {}
    }

    public final class ImageOnly extends DisplayType {
      /** Only displays the description- opening image, at the end of the line. */
      ImageOnly() {
        this.pattern = Pattern.compile("(?<=$)");
      }

      /**
       * Only displays the description- opening image, at the end of the match made with the current
       * spoiler text and the supplied pattern.
       *
       * @param pattern a String to be converted into a lookbehind pattern. Note that THIS IS A
       *     PATTERN, so be sure to remember to escape characters such as {@code ()[].\?*+}
       */
      ImageOnly(final String pattern) {
        this.pattern = Pattern.compile("(?<=" + pattern + ")");
      }
    }

    public final class DisplayAll extends DisplayType {
      private final GoalImportance goalImportance;
      private final GoalOperator goalOperator;
      private final int goalAmount;

      /**
       * Displays, at the end of the line, the description-opening image, followed by {@code (Have
       * #)}.
       */
      public DisplayAll() {
        this("$", null, null, 0);
      }

      /**
       * Displays, at the end of the line, the description-opening image, followed by {@code (Need
       * #. Have #)}.
       *
       * <p>The names of the required parameters should make their purpose obvious enough.
       *
       * @param goalImportance see {@link GoalImportance}
       * @param goalOperator see {@link GoalOperator}
       * @param goalAmount
       */
      DisplayAll(
          final GoalImportance goalImportance,
          final GoalOperator goalOperator,
          final int goalAmount) {
        this("$", goalImportance, goalOperator, goalAmount);
      }

      /**
       * Displays, at the end of the match made with the current spoiler text and the supplied
       * pattern, the description-opening image, followed by {@code (Have #)}.
       *
       * @param pattern a String to be converted into a lookbehind pattern. Note that THIS IS A
       *     PATTERN, so be sure to remember to escape characters such as {@code ()[].\?*+}
       */
      DisplayAll(final String pattern) {
        this(pattern, null, null, 0);
      }

      /**
       * Displays, at the end of the match made with the current spoiler text and the supplied
       * pattern, the description-opening image, followed by {@code (Need #. Have #)}.
       *
       * <p>The names of the required parameters should make their purpose obvious enough.
       *
       * @param pattern a String to be converted into a lookbehind pattern. Note that THIS IS A
       *     PATTERN, so be sure to remember to escape characters such as {@code ()[].\?*+}
       * @param goalImportance see {@link GoalImportance}
       * @param goalOperator see {@link GoalOperator}
       * @param goalAmount
       */
      DisplayAll(
          final String pattern,
          final GoalImportance goalImportance,
          final GoalOperator goalOperator,
          final int goalAmount) {
        this.pattern = Pattern.compile("(?<=" + pattern + ")");

        this.goalImportance = goalImportance;
        this.goalOperator = goalOperator;
        this.goalAmount = goalAmount;
      }

      private final String getAnnotation(final AdventureResult adventureResult) {
        StringBuffer result = new StringBuffer();

        if (adventureResult.isItem()) {
          itemAnnotation(result, adventureResult);
        } else {
          // only type supported so far
        }

        return result.toString();
      }

      private final void itemAnnotation(final StringBuffer buffer, final AdventureResult item) {
        if (this.goalImportance != null) {
          buffer.append("Need" + this.goalOperator.goalToString(this.goalAmount) + ". ");
        }

        itemPossessionAnnotation(buffer, item);
      }

      private final void itemPossessionAnnotation(
          final StringBuffer result, final AdventureResult item) {
        int equipped = EquipmentManager.equippedCount(item);
        int inInventory = item.getCount(KoLConstants.inventory);

        int totalInHand = equipped + inInventory;

        result.append("Have " + totalInHand);

        if (equipped > 0) {
          if (equipped == totalInHand) {
            result.append(" (equipped) ");
          } else {
            result.append(" (" + equipped + " equipped) ");
          }
        }

        // before returning the result, check if we
        // have an important requirement not met.
        if (this.goalImportance == GoalImportance.NEED
            && !this.goalOperator.possessionSatisfiesGoal(this.goalAmount, equipped, inInventory)) {
          // highlight in red
          result.insert(0, "<font color=\"red\">");
          result.append("</font>");
        }
      }
    }
  }

  public abstract class RetiredChoiceAdventure extends ChoiceAdventure {
    private boolean notifiedUser = false;

    RetiredChoiceAdventure(
        final int choiceAdventureNumber, final String name, final String source) {
      super(choiceAdventureNumber, name, source);
    }

    @Override
    protected void scanForUnknownInformation(final GenericRequest request) {
      // TODO hook to a spading manager
      if (!notifiedUser) {
        String message =
            "PLEASE REPORT THIS encountered choice "
                + this.choice
                + ", which we thought was retired";
        RequestLogger.printLine(message);
        RequestLogger.updateSessionLog(message);

        notifiedUser = true;
      }

      super.scanForUnknownInformation(request);
    }
  }

  public final class UnknownChoiceAdventure extends ChoiceAdventure {
    private boolean notifiedUser = false;
    private final List<Integer> optionsSeen = new ArrayList<>();

    UnknownChoiceAdventure(final int choiceAdventureNumber) {
      super(choiceAdventureNumber, null, "undefined");
    }

    @Override
    void setup() {}

    @Override
    protected void scanForUnknownInformation(final GenericRequest request) {
      // TODO hook to a spading manager
      if (!notifiedUser) {
        String message =
            "PLEASE REPORT THIS encountered choice "
                + this.choice
                + ", which we thought didn't exist";
        RequestLogger.printLine(message);
        RequestLogger.updateSessionLog(message);

        notifiedUser = true;
      }

      super.scanForUnknownInformation(request);
    }

    @Override
    protected void logUnknownOption(final Integer index, final String text) {
      // note/warn about the new option, but make sure we don't spam
      // (they maybe just have an outdated mafia version)
      if (this.optionsSeen.contains(index)) {
        return;
      }

      this.optionsSeen.add(index);
      super.logUnknownOption(index, text);
    }
  }

  private UnknownChoiceAdventure makeUnknownChoiceAdventure(final int id) {
    return new UnknownChoiceAdventure(id);
  }

  /** For creating unknown choice adventures on the go */
  private static final ChoiceAdventureDatabase INSTANCE = new ChoiceAdventureDatabase();

  /** Bridge between {@link ChoiceManager#preChoice} and {@link ChoiceAdventure#preChoice} */
  public static final void preChoice(final int choice, final String urlString) {
    getChoiceAdventure(choice).preChoice(urlString);
  }

  /**
   * Bridge between {@link ChoiceManager#registerRequest} and {@link
   * ChoiceAdventure#registerRequest}
   */
  public static final boolean registerRequest(
      final int choice, final String urlString, final int decision) {
    return getChoiceAdventure(choice).registerRequest(urlString, decision);
  }

  /**
   * Bridge between {@link ChoiceManager#postChoice0} and {@link ChoiceAdventure#postChoice0}
   *
   * <p>Also updates our location afterwards, since we know where each choice adventure happens
   */
  public static final void postChoice0(final int choice, final GenericRequest request) {
    ChoiceAdventure choiceAdventure = getChoiceAdventure(choice);

    choiceAdventure.postChoice0(request, ChoiceManager.getLastDecision());

    if (choiceAdventure.undefinedSource) {
      // undefinedSource means the choice adventure can appear
      // in more than 1 zone, so trust the current value of KoLAdventure.lastVisitedLocation
      return;
    }

    KoLAdventure source = choiceAdventure.source;

    if (source == null) {
      KoLAdventure.lastVisitedLocation = null;
      KoLAdventure.lastLocationName = null;
      KoLAdventure.lastLocationURL = null;
      KoLCharacter.updateSelectedLocation(null);
    } else {
      KoLAdventure.setLastAdventure(source);
    }
  }

  /**
   * Bridge between {@link ChoiceManager#registerDeferredChoice} and {@link
   * ChoiceAdventure#registerDeferredChoice}
   */
  public static final void registerDeferredChoice(final int choice) {
    getChoiceAdventure(choice).registerDeferredChoice();
  }

  /**
   * Bridge between {@link ChoiceManager#encounterName} and {@link ChoiceAdventure#encounterName}
   */
  public static final String encounterName(
      final int choice, final String urlString, final String responseText) {
    return getChoiceAdventure(choice).encounterName(urlString, responseText);
  }

  /**
   * Bridge between {@link ChoiceManager#visitChoice} and {@link ChoiceAdventure#visitChoice}
   *
   * <p>Resets the {@link ChoiceAdventure.Option}s to their default, first.
   */
  public static final void visitChoice(final int choice, final GenericRequest request) {
    ChoiceAdventure choiceAdventure = getChoiceAdventure(choice);

    choiceAdventure.resetOptionsToDefault();

    choiceAdventure.scanForUnknownInformation(request);

    choiceAdventure.visitChoice(request);
  }

  /**
   * Bridge between {@link ChoiceManager#decorateChoice} and {@link ChoiceAdventure#decorateChoice}
   */
  public static final void decorateChoice(final int choice, final StringBuffer buffer) {
    getChoiceAdventure(choice).decorateChoice(buffer);
  }

  /**
   * Bridge between {@link ChoiceManager#specialChoiceHandling} and {@link
   * ChoiceAdventure#specialChoiceHandling}
   */
  public static final String specialChoiceHandling(final int choice, final GenericRequest request) {
    return getChoiceAdventure(choice).specialChoiceHandling(request);
  }

  /**
   * Bridge between {@link ChoiceManager#getDecision(int, String, int)} and {@link
   * ChoiceAdventure#getDecision}
   *
   * <p>If {@code decision} is "0" and {@link ChoiceAdventure#option0IsManualControl} is true, will
   * not call the method.
   */
  public static final String getDecision(
      final int choice, final String responseText, final String decision, final int stepCount) {
    ChoiceAdventure choiceAdventure = getChoiceAdventure(choice);

    if (decision.equals("0") && choiceAdventure.option0IsManualControl) {
      return decision;
    }

    return choiceAdventure.getDecision(responseText, decision, stepCount);
  }

  /** Bridge between {@link ChoiceManager#postChoice1} and {@link ChoiceAdventure#postChoice1} */
  public static final void postChoice1(final int choice, final GenericRequest request) {
    ChoiceAdventure choiceAdventure = getChoiceAdventure(choice);

    choiceAdventure.lastDecision = ChoiceManager.getLastDecision();

    choiceAdventure.postChoice1(request, choiceAdventure.lastDecision);
  }

  /**
   * Bridge between {@link ChoiceManager#postChoice2} and {@link ChoiceAdventure#postChoice2}
   *
   * <p>Calls {@link #enqueueStartOfChain(int, int)} afterwards.
   */
  public static final void postChoice2(final int choice, final GenericRequest request) {
    int decision = ChoiceManager.getLastDecision();
    getChoiceAdventure(choice).postChoice2(request, decision);

    // Check if we add the adventure to the NC queue
    enqueueStartOfChain(choice, decision);
  }

  /**
   * Bridge between {@link ChoiceManager#decorateChoiceResponse} and {@link
   * ChoiceAdventure#decorateChoiceResponse}
   */
  public static final void decorateChoiceResponse(
      final int choice, final StringBuffer buffer, final int option) {
    getChoiceAdventure(choice).decorateChoiceResponse(buffer, option);
  }

  public static final boolean neverAutomate(final int choice) {
    return getChoiceAdventure(choice).neverAutomate;
  }

  public static final boolean hasGoalButton(final int choice) {
    return getChoiceAdventure(choice).hasGoalButton;
  }

  public static final boolean canWalkFromChoice(final int choice) {
    return getChoiceAdventure(choice).canWalkFromChoice;
  }

  private static String choiceChainStartChoice;
  private static KoLAdventure choiceChainStartLocation;

  /**
   * The method used to add a Choice Adventure to a location's noncombat queue.
   *
   * <p>Some Choice Adventures don't get added to the queue when selecting certain options, such as
   * those allowing players to leave the choice adventure for free.<br>
   * Sometimes, it's not even known on <i>this</i> choice adventure; only later, at the end of a
   * choice adventure chain.
   *
   * <p>This method stores what the first adventure of the chain was.<br>
   * We'll try to add this adventure to the queue during {@link #postChoice2(int, GenericRequest)},
   * once we know the player's decision.
   *
   * @param choice the current choice's number
   * @param location the location we were in when we got the choice. May get overriden by this
   *     {@link ChoiceAdventure#source}
   * @param encounter the name for this encounter, that we'll want (possibly) in the queue
   */
  public static final void addToQueue(final int choice, KoLAdventure location, String encounter) {
    ChoiceAdventure choiceAdventure = getChoiceAdventure(choice);

    if (choiceAdventure.isSuperlikely) {
      // cannot enter the queue
      choiceChainStartChoice = null;
      return;
    }

    choiceChainStartChoice = encounter;

    if (choiceAdventure.undefinedSource) {
      choiceChainStartLocation = location;
    } else {
      choiceChainStartLocation = choiceAdventure.source;
    }
  }

  /**
   * Check, based on the decision on the previous choice, if we add the value of {@link
   * #choiceChainStartChoice} to the appropriate location's noncombat queue. (if {@link
   * #choiceChainStartChoice} is non-null)
   */
  private static final void enqueueStartOfChain(final int choice, final int decision) {
    if (choiceChainStartChoice == null || choiceChainStartLocation == null) {
      return;
    }

    ChoiceAdventure choiceAdventure = getChoiceAdventure(choice);

    if (choiceAdventure.neverEntersQueue) {
      return;
    }

    ChoiceAdventure.Option option = choiceAdventure.options.get(decision);

    if (option != null && option.entersQueue) {
      AdventureQueueDatabase.enqueueNoncombat(choiceChainStartLocation, choiceChainStartChoice);

      // Prevent the same choice adventure from being logged multiple times.
      choiceChainStartChoice = null;
    }
  }

  public static final ChoiceAdventure getChoiceAdventure(final int id) {
    ChoiceAdventure choiceAdventure = database.get(IntegerPool.get(id));

    if (choiceAdventure == null) {
      return ChoiceAdventureDatabase.INSTANCE.makeUnknownChoiceAdventure(id);
    }

    return choiceAdventure;
  }

  public static final Iterator<ChoiceAdventure> getDatabaseIterator() {
    return database.values().iterator();
  }

  static {
    new CADatabase1to99().add1to99();
    new CADatabase100to199().add100to199();
    new CADatabase200to299().add200to299();
    new CADatabase300to399().add300to399();
    new CADatabase400to499().add400to499();
    new CADatabase500to599().add500to599();
    new CADatabase600to699().add600to699();
    new CADatabase700to799().add700to799();
    new CADatabase800to899().add800to899();
    new CADatabase900to999().add900to999();
    new CADatabase1000to1099().add1000to1099();
    new CADatabase1100to1199().add1100to1199();
    new CADatabase1200to1299().add1200to1299();
    new CADatabase1300to1399().add1300to1399();
    new CADatabase1400to1499().add1400to1499();

    for (ChoiceAdventure choiceAdventure : database.values()) {
      // was initially in their... initialization, but doing
      // so prevented local fields from being used in setup()
      choiceAdventure.setup();

      choiceAdventure.saveOptionsDefaults();
      choiceAdventure.makeFutureCustomOption();
      choiceAdventure.finalizeCustomOptions();
    }
  }

  // stupid enums that can't be declared inside non-static classes...
  // These are meant for Option.Attachment

  /** Either {@link #AUTO} or {@link #MANUAL} */
  public enum ProcessType {
    /**
     * Attachment type that is disclaimed by KoL, and doesn't need handling because it will be
     * picked up by {@link GenericRequest#processResults}
     */
    AUTO,
    /**
     * Attachment type that, when selecting its option, gets processed after {@link #postChoice1}.
     *
     * <p>Calling {@link #choiceFailed()} removes every {@link ProcessType#MANUAL} attachment
     * currently attached to this option (until we reset them to their default).
     */
    MANUAL
  }

  /** Either {@link #WANT} or {@link #NEED} */
  public enum GoalImportance {
    /**
     * We "would like" {@code x}. Nothing special happens if we don't have {@code x} (the text
     * displayed will still say "Need", because that looks better)
     *
     * <p>In other words: "need for later"
     */
    WANT,
    /**
     * We <b>need</b> {@code x}. If we don't have {@code x}, the amount in inventory will be
     * displayed in red.
     *
     * <p>In other words: "need NOW, or the choice will fail/you'll get a bad outcome"
     */
    NEED
  }

  public enum GoalOperator {
    // looks in inventory (and equipment, if equipment item)
    EXACTLY("", ""),
    AT_LEAST("+", ""),
    AT_BEST("-", ""),

    // looks at equipment
    EQUIPPED("", " equipped"),
    EQUIPPED_AT_LEAST("+", " equipped"),
    EQUIPPED_AT_BEST("-", " equipped"),

    // looks in inventory *only*
    INV_ONLY("", " in inventory"),
    INV_ONLY_AT_LEAST("+", " in inventory"),
    INV_ONLY_AT_BEST("-", " in inventory");

    private final String bound;
    private final String extra; // there HAS to be a better name...

    GoalOperator(final String bound, final String extra) {
      this.bound = bound;
      this.extra = extra;
    }

    public String goalToString(final int goalAmount) {
      if (this.bound.equals("+") && goalAmount == 1) {
        // need "any"/"any amount at all"
        return this.extra;
      }
      return " " + goalAmount + this.bound + this.extra;
    }

    public boolean possessionSatisfiesGoal(
        final int goalAmount, final int equipped, final int inInventory) {
      int x =
          this.extra.endsWith("equipped")
              ? equipped
              : this.extra.endsWith("inventory") ? inInventory : equipped + inInventory;

      switch (this.bound) {
        case "+":
          return x >= goalAmount;
        case "-":
          return x <= goalAmount;
        default:
          return x == goalAmount;
      }
    }
  }
}
