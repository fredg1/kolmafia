package net.sourceforge.kolmafia.session;

import java.io.File;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import net.sourceforge.kolmafia.KoLAdventure;
import net.sourceforge.kolmafia.KoLCharacter;
import net.sourceforge.kolmafia.KoLConstants.MafiaState;
import net.sourceforge.kolmafia.KoLmafia;
import net.sourceforge.kolmafia.KoLmafiaASH;
import net.sourceforge.kolmafia.KoLmafiaCLI;
import net.sourceforge.kolmafia.RequestEditorKit;
import net.sourceforge.kolmafia.RequestLogger;
import net.sourceforge.kolmafia.objectpool.IntegerPool;
import net.sourceforge.kolmafia.persistence.AdventureSpentDatabase;
import net.sourceforge.kolmafia.persistence.choiceadventures.ChoiceAdventureDatabase;
import net.sourceforge.kolmafia.persistence.choiceadventures.ChoiceAdventureDatabase.ChoiceAdventure;
import net.sourceforge.kolmafia.persistence.choiceadventures.ChoiceAdventureDatabase.ChoiceAdventure.Option;
import net.sourceforge.kolmafia.preferences.Preferences;
import net.sourceforge.kolmafia.request.FightRequest;
import net.sourceforge.kolmafia.request.GenericRequest;
import net.sourceforge.kolmafia.request.RelayRequest;
import net.sourceforge.kolmafia.request.UseItemRequest;
import net.sourceforge.kolmafia.textui.ScriptRuntime;
import net.sourceforge.kolmafia.utilities.ChoiceUtilities;
import net.sourceforge.kolmafia.utilities.StringUtilities;

// Roadmap of how ChoiceManager methods get traversed by choice.php requests:
//
// A wild request (URL) appears! (choice.php?#####)
// (GenericRequest) request.run
// => request.execute
//	| => ChoiceManager.preChoice
//	|	| => ChoiceAdventureDatabase.preChoice (<IF> the URL has a "whichchoice" *and* an "option"
// field)
//	|	| <=
//	| <=
//	|
//	| => RequestLogger.registerRequest => RequestLogger.doRegister => ChoiceManager.registerRequest
//	|	| => ChoiceAdventureDatabase.registerRequest (<IF> the URL has a (non-zero) "whichchoice"
// field)
//	|	| <=
//	| <=
//	|
//	| => request.externalExecute => request.retrieveServerReply() (Sends the request to KoL and looks
// at response)
//	|	| => request.handleRedirect (<IF> it's a redirect)
//	|	|	| => ChoiceManager.handleWalkingAway
//	|	|	| <=
//	|	|	|
//	|	|	| => ChoiceManager.preChoice (<IF> we were redirected to a choice.php location) (won't go
// through ChoiceAdventureDatabase since there's no way the redirect URL had whichchoice + option
// fields)
//	|	|	| <=
//	|	|	| => FightRequest.preFight( fromChoice = true ) (<ELSE, IF> we were redirected to a fight.php
// or fambattle.php location)
//	|	|	| <=
//	|	|	|
//	|	|	| => ChoiceManager.registerRequest => ChoiceAdventureDatabase.registerRequest
//	|	|	| <=
//	|	|	|
//	|	|	****** <return to request.externalExecute, and retry request.retrieveServerReply() with the
// redirect location> *********
//	|	|
//	|	| => request.retrieveServerReply(stream) => request.processResponse (<IF> responseCode is 200)
//	|	|	| => ChoiceManager.postChoice0
//	|	|	|	| => ChoiceAdventureDatabase.postChoice0 (<IF> we didn't get the "not actually in a choice
// adventure" response)
//	|	|	|	| <=
//	|	|	| <=
//	|	|	|
//	|	|	| => AdventureRequest.registerEncounter
//	|	|	|	| => AdventureRequest.parseChoiceEncounter (<IF> the response contains the text
// "choice.php")
//	|	|	|	| <=
//	|	|	|	|
//	|	|	|	| => ChoiceManager.registerDeferredChoice (<IF> the response contains the text
// "choice.php")
//	|	|	|	| <=
//	|	|	|	|
//	|	|	|	| => ChoiceManager.addToQueue => ChoiceAdventureDatabase.addToQueue (<IF> the response
// contains the text "choice.php", <AND> AdventureRequest.parseChoiceEncounter did not return
// "null", <AND> we were not in a choice adventure when submitting this request)
//	|	|	|	| <=
//	|	|	| <=
//	|	|	|
//	|	|	| => ChoiceManager.postChoice1 => ChoiceManager.visitChoice (<IF> we got the "not actually in
// a choice adventure" response)
//	|	|	| <=
//	|	|	| => ChoiceManager.postChoice1 => ChoiceManager.visitChoice (<ELSE, IF> the request didn't
// have an "option" field (or it was 0))
//	|	|	|	| => ChoiceAdventureDatabase.visitChoice
//	|	|	|	| <=
//	|	|	|	|
//	|	|	|	| => RequestEditorKit.getFeatureRichHTML => RequestEditorKit.applyPageAdjustments =>
// RequestEditorKit.addChoiceSpoilers
//	|	|	|	|	| => ChoiceManager.decorateChoice => ChoiceAdventureDatabase.decorateChoice
//	|	|	|	|	| <=
//	|	|	|	| <=
//	|	|	| <=
//	|	|	| => ChoiceManager.postChoice1 => ChoiceAdventureDatabase.postChoice1 (<ELSE, IF> the request
// DID have a non-zero "option" field)
//	|	|	| <=
//	|	|	|
//	|	|	| => request.parseResults
//	|	|	| <=
//	|	|	|
//	|	|	| => request.processResults
//	|	|	| <=
//	|	|	|
//	|	|	| => ChoiceManager.postChoice2 (<IF> the request had either no "whichchoice" or no "option"
// field (i.e. it was a visit), or we got the "not actually in a choice adventure" response)
//	|	|	| <=
//	|	|	| => ChoiceManager.postChoice2 (<ELSE, IF> it had both fields)
//	|	|	|	| => ChoiceAdventureDatabase.postChoice2
//	|	|	|	| <=
//	|	|	|	|
//	|	|	|	| => ChoiceManager.visitChoice (<IF> we are STILL in a choice adventure)
//	|	|	|	| <=
//	|	|	|	| => RequestEditorKit.getFeatureRichHTML => RequestEditorKit.applyPageAdjustments =>
// RequestEditorKit.addChoiceSpoilers (<ELSE, IF> we are no longer in a choice adventure)
//	|	|	|	|	| => ChoiceManager.decorateChoice => ChoiceAdventureDatabase.decorateChoice
//	|	|	|	|	| <=
//	|	|	|	| <=
//	|	|	| <=
//	|	| <=
//	| <=
// <=

public abstract class ChoiceManager {
  public static final GenericRequest CHOICE_HANDLER =
      new GenericRequest("choice.php") {
        @Override
        protected boolean shouldFollowRedirect() {
          return false;
        }
      };

  public static boolean handlingChoice = false;
  public static int lastChoice = 0;
  public static int lastDecision = 0;
  public static String lastResponseText = "";
  public static String lastDecoratedResponseText = "";

  private static int skillUses = 0;
  private static boolean canWalkAway;

  public enum PostChoiceAction {
    NONE,
    INITIALIZE,
    ASCEND
  }

  public static PostChoiceAction action = PostChoiceAction.NONE;

  public static int currentChoice() {
    return ChoiceManager.handlingChoice ? ChoiceManager.lastChoice : 0;
  }

  public static int extractChoice(final String responseText) {
    int choice = ChoiceUtilities.extractChoice(responseText);

    if (choice == 0 && responseText.contains("<b>Lyle, LyleCo CEO</b>")) {
      // We still don't know the choice number, so take action here instead
      // We will either now, or in the past, have had Favored By Lyle
      Preferences.setBoolean("_lyleFavored", true);
    }

    return choice;
  }

  public static final Pattern URL_CHOICE_PATTERN = Pattern.compile("whichchoice=(\\d+)");

  public static int extractChoiceFromURL(final String urlString) {
    Matcher matcher = ChoiceManager.URL_CHOICE_PATTERN.matcher(urlString);
    return matcher.find() ? StringUtilities.parseInt(matcher.group(1)) : 0;
  }

  public static final Pattern URL_OPTION_PATTERN = Pattern.compile("(?<!force)option=(\\d+)");

  public static int extractOptionFromURL(final String urlString) {
    Matcher matcher = ChoiceManager.URL_OPTION_PATTERN.matcher(urlString);
    return matcher.find() ? StringUtilities.parseInt(matcher.group(1)) : 0;
  }

  public static final Pattern URL_IID_PATTERN = Pattern.compile("iid=(\\d+)");

  public static int extractIidFromURL(final String urlString) {
    Matcher matcher = ChoiceManager.URL_IID_PATTERN.matcher(urlString);
    return matcher.find() ? StringUtilities.parseInt(matcher.group(1)) : -1;
  }

  public static final Pattern URL_QTY_PATTERN = Pattern.compile("qty=(\\d+)");

  public static int extractQtyFromURL(final String urlString) {
    Matcher matcher = ChoiceManager.URL_QTY_PATTERN.matcher(urlString);
    return matcher.find() ? StringUtilities.parseInt(matcher.group(1)) : -1;
  }

  public static final Pattern DECISION_BUTTON_PATTERN =
      Pattern.compile(
          "<input type=hidden name=option value=(\\d+)>(?:.*?)<input +class=button type=submit value=\"(.*?)\">");

  public static void initializeAfterChoice() {
    ChoiceManager.action = PostChoiceAction.INITIALIZE;
    GenericRequest request = ChoiceManager.CHOICE_HANDLER;
    request.constructURLString("choice.php");
    request.run();
    ChoiceUtilities.printChoices(ChoiceManager.lastResponseText);
  }

  public static boolean initializingAfterChoice() {
    return ChoiceManager.action == PostChoiceAction.INITIALIZE;
  }

  public static void ascendAfterChoice() {
    ChoiceManager.action = PostChoiceAction.ASCEND;
  }

  private static final void processManuals(final int choice, final int decision) {
    Option option = ChoiceManager.choiceOptions(choice).get(decision);
    if (option == null) {
      // Shouldn't happen at this point, but just making sure
      return;
    }

    for (Option.Attachment attachment : option.attachments) {
      if (attachment.processType != ChoiceAdventureDatabase.ProcessType.MANUAL) {
        continue;
      }

      if (attachment instanceof Option.AdvAttachment) {
        int turnCost = -attachment.adventureResult.getCount();

        if (turnCost <= 0) {
          // this should have been stopped when making the
          // attachment, but just to be sure
          continue;
        }

        KoLAdventure source = ((Option.AdvAttachment) attachment).getSource();

        if (source != null) {
          AdventureSpentDatabase.addTurn(source, turnCost);
        }

        ResultProcessor.processAdventuresUsed(turnCost);

        AdventureSpentDatabase.setLastTurnUpdated(KoLCharacter.getCurrentRun());
      } else if (attachment.adventureResult.getCount() != 0 && attachment.haveEnough()) {
        ResultProcessor.processResult(attachment.adventureResult);
      }
    }
  }

  /**
   * Choice-specific decorations.<br>
   * Called when we understand this request to be a <b>visit to</b> this choice adventure (if the
   * user turned {@code relayShowSpoilers} on) before adding the options' spoilers.
   */
  public static final void decorateChoice(final int choice, final StringBuffer buffer) {
    ChoiceAdventureDatabase.decorateChoice(choice, buffer);
  }

  /** Called from {@link RequestEditorKit#addChoiceSpoilers}, when making the options' spoilers. */
  public static final String getOptionSpoiler(final Option option) {
    if (option == null) {
      return "";
    }

    StringBuilder spoilerBuffer =
        new StringBuilder(ChoiceManager.getOptionSpoilerText(option, true));

    if (spoilerBuffer.length() > 0) {
      spoilerBuffer.insert(
          0, "<div class=mafiaOptionSpoiler id=mafiaOptionSpoiler" + option.index + ">");
      spoilerBuffer.append("</div>");
    }

    StringBuilder summaryBuffer =
        new StringBuilder(ChoiceManager.getNextChoiceSummary(option, new ArrayList<>()));

    if (summaryBuffer.length() > 0) {
      summaryBuffer.insert(
          0, "<div class=mafiaNextChoiceSummary id=mafiaNextOptionSummary" + option.index + ">");
      summaryBuffer.append("</div>");
    }

    return spoilerBuffer.toString() + summaryBuffer.toString();
  }

  public static final String getOptionSpoilerText(final Option option, final boolean wrap) {
    StringBuilder spoilerBuffer = new StringBuilder();

    if (option.spoilerText != null) {
      spoilerBuffer.append(option.spoilerText);
    }

    TreeMap<Integer, ArrayList<String>> insertedAnnotations =
        new TreeMap<>(Collections.reverseOrder());
    ArrayList<String> appendedAnnotations = new ArrayList<>();

    for (Option.Attachment attachment : option.attachments) {
      String annotation = attachment.getAttachmentAnnotation();

      if (annotation == null) {
        continue;
      }

      int targetIndex = attachment.getAnnotationTargetIndex();

      if (targetIndex == -1 || targetIndex >= spoilerBuffer.length()) {
        appendedAnnotations.add(annotation);
        continue;
      }

      insertedAnnotations.putIfAbsent(IntegerPool.get(targetIndex), new ArrayList<>());
      insertedAnnotations.get(IntegerPool.get(targetIndex)).add(annotation);
    }

    // insert insertedAnnotations
    for (Map.Entry<Integer, ArrayList<String>> entry : insertedAnnotations.entrySet()) {
      spoilerBuffer.insert(entry.getKey(), String.join(", ", entry.getValue()));
    }

    // append appendedAnnotations
    spoilerBuffer.append(String.join(", ", appendedAnnotations));

    if (wrap && option.spoilerText != null) {
      spoilerBuffer.insert(0, "(").append(")");
    }

    return spoilerBuffer.toString();
  }

  private static final String getNextChoiceSummary(final Option option, final List<Integer> chain) {
    if (option.leadsTo < 1 || option.summaryFilter == null) {
      return "";
    }

    ChoiceAdventure nextLink = option.lookAhead(); // FIXME lookAhead in visitChoice() instead
    // ChoiceAdventure nextLink = ChoiceAdventureDatabase.getChoiceAdventure(option.leadsTo);

    if (nextLink == null) {
      return "";
    }

    // infinite recursion protection
    if (chain.isEmpty()) {
      chain.add(ChoiceManager.lastChoice);
    }
    if (chain.contains(nextLink.choice)) {
      return "";
    }
    chain.add(nextLink.choice);

    List<String> nextSpoilers = new ArrayList<>();

    for (Option nextOption : nextLink.options.values()) {
      if (!option.summaryFilter.test(nextOption)) {
        continue;
      }

      String nextSpoiler = ChoiceManager.getOptionSpoilerText(nextOption, false);

      if (nextSpoiler.length() == 0) {
        nextSpoiler = ChoiceManager.getNextChoiceSummary(nextOption, chain);
      }

      if (nextSpoiler.length() > 0) {
        nextSpoilers.add(nextSpoiler);
      }
    }

    return String.join(option.summarySeparator, nextSpoilers);
  }

  /**
   * Called during decoration after {@link #postChoice2} when we understand this request to be a
   * <b>response to</b> this choice adventure (if the user turned {@code relayShowSpoilers} on).
   */
  public static final void decorateChoiceResponse(
      final String location, final StringBuffer buffer) {
    int choice = ChoiceManager.extractChoiceFromURL(location);
    if (choice == 0) {
      return;
    }

    int option = ChoiceManager.extractOptionFromURL(location);

    ChoiceAdventureDatabase.decorateChoiceResponse(choice, buffer, option);
  }

  public static final Map<Integer, Option> choiceOptions(final int choice) {
    return ChoiceAdventureDatabase.getChoiceAdventure(choice).options;
  }

  public static final void processRedirectedChoiceAdventure(final String redirectLocation) {
    ChoiceManager.processChoiceAdventure(ChoiceManager.CHOICE_HANDLER, redirectLocation, null);
  }

  public static final void processChoiceAdventure(final String responseText) {
    ChoiceManager.processChoiceAdventure(ChoiceManager.CHOICE_HANDLER, "choice.php", responseText);
  }

  public static final String processChoiceAdventure(
      final String decision, final String extraFields, final boolean tryToAutomate) {
    return ChoiceManager.processChoiceAdventure(
        StringUtilities.parseInt(decision), extraFields, tryToAutomate);
  }

  public static final String processChoiceAdventure(
      final int decision, final String extraFields, final boolean tryToAutomate) {
    GenericRequest request = ChoiceManager.CHOICE_HANDLER;

    request.constructURLString("choice.php");
    request.addFormField("whichchoice", String.valueOf(ChoiceManager.lastChoice));
    request.addFormField("option", String.valueOf(decision));
    if (!extraFields.equals("")) {
      String[] fields = extraFields.split("&");
      for (String field : fields) {
        int equals = field.indexOf("=");
        if (equals != -1) {
          request.addFormField(field.substring(0, equals), field.substring(equals + 1));
        }
      }
    }
    request.addFormField("pwd", GenericRequest.passwordHash);
    request.run();

    if (tryToAutomate) {
      ChoiceManager.processChoiceAdventure(request, "choice.php", request.responseText);
      return "";
    }

    return request.responseText;
  }

  public static final boolean stillInChoice() {
    return ChoiceManager.stillInChoice(ChoiceManager.lastResponseText);
  }

  private static boolean stillInChoice(final String responseText) {
    // Doing the Maths has a choice form but, somehow, does not specify choice.php

    // <form method="get" id="">
    //   <input type="hidden" name="whichchoice" value="1103" />
    //   <input type="hidden" name="pwd" value="xxxxxx" />
    //   <input type="hidden" name="option" value="1" />
    //   <input type="text" name="num" value="" maxlen="6" size="6" />
    //   <input type="submit" value="Calculate the Universe" class="button" />
    //   <div style="clear:both"></div>
    // </form>

    return responseText.contains("action=choice.php")
        || responseText.contains("href=choice.php")
        || responseText.contains("name=\"whichchoice\"")
        || responseText.contains("href=\"choice.php");
  }

  public static final void processChoiceAdventure(
      final GenericRequest request, final String initialURL, String responseText) {
    // You can no longer simply ignore a choice adventure.  One of
    // the options may have that effect, but we must at least run
    // choice.php to find out which choice it is.

    // Get rid of extra fields - like "action=auto"
    request.constructURLString(initialURL);

    if (responseText == null) {
      GoalManager.updateProgress(GoalManager.GOAL_CHOICE);
      request.run();

      if (request.responseCode == 302) {
        return;
      }

      responseText = request.responseText;
    } else {
      request.responseText = responseText;
    }

    if (GenericRequest.passwordHash.equals("")) {
      return;
    }

    for (int stepCount = 0;
        !KoLmafia.refusesContinue() && ChoiceManager.stillInChoice(responseText);
        ++stepCount) {
      int choice = ChoiceManager.extractChoice(responseText);
      if (choice == 0) {
        // choice.php did not offer us any choices.
        // This would be a bug in KoL itself.
        // Bail now and let the user finish by hand.

        KoLmafia.updateDisplay(MafiaState.ABORT, "Encountered choice adventure with no choices.");
        request.showInBrowser(true);
        return;
      }

      if (ChoiceManager.invokeChoiceAdventureScript(choice, responseText)) {
        if (FightRequest.choiceFollowsFight) {
          // The choice redirected to a fight, which was immediately lost,
          // but which leads to another choice.
          // Let the caller automate that one, if desired.
          return;
        }

        if (!ChoiceManager.handlingChoice) {
          // The choiceAdventureScript processed this choice.
          return;
        }

        // We are still handling a choice. Maybe it is a different one.
        if (ChoiceManager.lastResponseText != null
            && choice != ChoiceManager.extractChoice(ChoiceManager.lastResponseText)) {
          responseText = ChoiceManager.lastResponseText;
          continue;
        }
      }

      // Either no choiceAdventure script or it left us in the same choice.
      if (!ChoiceManager.automateChoice(choice, request, stepCount)) {
        return;
      }

      // We automated one choice. If it redirected to a
      // fight, quit automating the choice.
      if (request.redirectLocation != null) {
        return;
      }

      responseText = request.responseText;
    }
  }

  private static boolean invokeChoiceAdventureScript(final int choice, final String responseText) {
    if (responseText == null) {
      return false;
    }

    String scriptName = Preferences.getString("choiceAdventureScript").trim();
    if (scriptName.length() == 0) {
      return false;
    }

    List<File> scriptFiles = KoLmafiaCLI.findScriptFile(scriptName);
    ScriptRuntime interpreter = KoLmafiaASH.getInterpreter(scriptFiles);

    if (interpreter == null) {
      return false;
    }

    File scriptFile = scriptFiles.get(0);

    Object[] parameters = new Object[2];
    parameters[0] = Integer.valueOf(choice);
    parameters[1] = responseText;

    KoLmafiaASH.logScriptExecution(
        "Starting choice adventure script: ", scriptFile.getName(), interpreter);

    // Since we are automating, let the script execute without interruption
    KoLmafia.forceContinue();

    interpreter.execute("main", parameters);
    KoLmafiaASH.logScriptExecution(
        "Finished choice adventure script: ", scriptFile.getName(), interpreter);

    return true;
  }

  private static boolean automateChoice(
      final int choice, final GenericRequest request, final int stepCount) {
    // If this choice cannot be handled, don't handle it.

    if (ChoiceAdventureDatabase.neverAutomate(choice)) {
      // Should we abort?
      return false;
    }

    // If this choice has special handling that can't be
    // handled by a single preference (extra fields, for
    // example), handle it elsewhere.

    if (ChoiceManager.specialChoiceHandling(choice, request)) {
      return false;
    }

    String[] decisionGotten = ChoiceManager.getDecision(choice, request.responseText, stepCount);

    String decision = decisionGotten[0];
    String extraFields = decisionGotten[1];

    // Let user handle the choice manually, if requested

    if (decision.equals("0")) {
      KoLmafia.updateDisplay(MafiaState.ABORT, "Manual control requested for choice #" + choice);
      ChoiceUtilities.printChoices(ChoiceManager.lastResponseText);
      request.showInBrowser(true);
      return false;
    }

    if (KoLCharacter.isEd()
        && Preferences.getInteger("_edDefeats") >= Preferences.getInteger("edDefeatAbort")) {
      KoLmafia.updateDisplay(
          MafiaState.ABORT,
          "Hit Ed defeat threshold - Manual control requested for choice #" + choice);
      ChoiceUtilities.printChoices(ChoiceManager.lastResponseText);
      request.showInBrowser(true);
      return false;
    }

    // Bail if no setting determines the decision

    if (decision.equals("")) {
      KoLmafia.updateDisplay(MafiaState.ABORT, "Unsupported choice adventure #" + choice);
      ChoiceManager.logChoices();
      request.showInBrowser(true);
      return false;
    }

    // Make sure that KoL currently allows the chosen choice/decision/extraFields
    String error =
        ChoiceUtilities.validateChoiceFields(decision, extraFields, request.responseText);
    if (error != null) {
      KoLmafia.updateDisplay(MafiaState.ABORT, error);
      ChoiceUtilities.printChoices(ChoiceManager.lastResponseText);
      request.showInBrowser(true);
      return false;
    }

    request.clearDataFields();
    request.addFormField("whichchoice", String.valueOf(choice));
    request.addFormField("option", decision);
    if (!extraFields.equals("")) {
      String[] fields = extraFields.split("&");
      for (String field : fields) {
        int equals = field.indexOf("=");
        if (equals != -1) {
          request.addFormField(field.substring(0, equals), field.substring(equals + 1));
        }
      }
    }
    request.addFormField("pwd", GenericRequest.passwordHash);

    request.run();

    return true;
  }

  public static final int getDecision(int choice, String responseText) {
    String[] decisionGot = ChoiceManager.getDecision(choice, responseText, Integer.MAX_VALUE);

    String decision = decisionGot[0];
    String extraFields = decisionGot[1];

    // Currently unavailable decision, manual choice requested, or unsupported choice
    if (decision.equals("0")
        || decision.equals("")
        || ChoiceUtilities.validateChoiceFields(decision, extraFields, responseText) != null) {
      return 0;
    }

    return StringUtilities.parseInt(decision);
  }

  private static final String[] getDecision(int choice, String responseText, final int stepCount) {
    String setting = "choiceAdventure" + choice;
    String optionValue = Preferences.getString(setting);
    int amp = optionValue.indexOf("&");

    String decision = amp == -1 ? optionValue : optionValue.substring(0, amp);
    String extraFields = amp == -1 ? "" : optionValue.substring(amp + 1);

    // If one of the decisions will satisfy a goal, take it

    decision = ChoiceManager.pickGoalOption(choice, decision);

    // If this choice has special handling based on
    // character state, convert to real decision index

    decision = ChoiceAdventureDatabase.getDecision(choice, responseText, decision, stepCount);

    return new String[] {decision, extraFields};
  }

  private static String pickGoalOption(final int choice, final String decision) {
    // If the user wants manual control, let 'em have it.
    if (decision.equals("0")) {
      return decision;
    }

    ChoiceAdventure choiceAdventure = ChoiceAdventureDatabase.getChoiceAdventure(choice);
    Map<Integer, Option> options = choiceAdventure.options;

    // Choose an item in the conditions first, if it's available.
    // This allows conditions to override existing choices.

    Option bestOption = null;
    int bestOptionScore = 0;
    for (Option option : options.values()) {
      int optionScore = 0;

      for (Option.Attachment attachment : option.attachments) {
        if (attachment.adventureResult.getCount() < 0 && !attachment.haveEnough()) {
          optionScore = -1;
          break;
        }

        if (attachment.adventureResult.getCount() > 0
            && GoalManager.hasGoal(attachment.adventureResult)) {
          optionScore += attachment.adventureResult.getCount();
        }
      }

      // TODO extend the search with option.leadsTo ?

      if (optionScore > bestOptionScore) {
        bestOption = option;
      }
    }

    if (bestOption != null) {
      return String.valueOf(bestOption.index);
    }

    // Next possibility: check if the chosen option is a promise to
    // "complete the outfit"
    int index = StringUtilities.parseInt(decision);
    for (ChoiceAdventure.CustomOption customOption : choiceAdventure.customOptions.values()) {
      if (customOption.optionIndex != index
          || customOption.displayText == null
          || !customOption.displayText.equals("complete the outfit")) {
        continue;
      }

      for (Option option : options.values()) {
        for (Option.Attachment attachment : option.attachments) {
          if (attachment.adventureResult.getCount() < 0 && !attachment.haveEnough()) {
            break;
          }

          if (attachment instanceof Option.ItemAttachment
              && attachment.adventureResult.getCount() > 0
              && !InventoryManager.hasItem(attachment.adventureResult)) {
            return String.valueOf(option.index);
          }
        }
      }

      // If they have everything, then just return choice 1
      // FIXME make sure option 1 is available?
      return "1";
    }

    // ran out of ideas. Just let them have what they asked for.
    return decision;
  }

  public static final int getLastChoice() {
    return ChoiceManager.lastChoice;
  }

  public static final int getLastDecision() {
    return ChoiceManager.lastDecision;
  }

  /**
   * Handling to be done before we register the request.
   *
   * <p><b>Nothing has been sent yet</b> <br>
   * This method should mainly be used to get a backup of {@code RequestLogger.lastURLString} and/or
   * {@code ChoiceManager.lastResponseText} before they are re-assigned, if needed.
   */
  public static final void preChoice(final GenericRequest request) {
    FightRequest.choiceFollowsFight = false;
    ChoiceManager.handlingChoice = true;
    FightRequest.currentRound = 0;

    String choice = request.getFormField("whichchoice");
    String option = request.getFormField("option");

    if (choice == null || option == null) {
      // Visiting a choice page but not yet making a decision
      ChoiceManager.lastChoice = 0;
      ChoiceManager.lastDecision = 0;
      ChoiceManager.lastResponseText = null;
      ChoiceManager.lastDecoratedResponseText = null;
      return;
    }

    // We are about to take a choice option
    ChoiceManager.lastChoice = StringUtilities.parseInt(choice);
    ChoiceManager.lastDecision = StringUtilities.parseInt(option);

    ChoiceAdventureDatabase.preChoice(ChoiceManager.lastChoice, request.getURLString());
  }

  /**
   * Edge cases that need to be handled ASAP, before the encounter is registered by AdventureRequest
   *
   * <p>Also use this as an occasion to correct our location
   */
  public static void postChoice0(final String urlString, final GenericRequest request) {
    if (ChoiceManager.nonInterruptingRequest(urlString, request)) {
      return;
    }

    // If this is not actually a choice page, nothing to do here.
    if (!urlString.startsWith("choice.php")) {
      return;
    }

    // Things that have to be done before we register the encounter.

    String text = request.responseText;
    int choice =
        ChoiceManager.lastChoice == 0
            ? ChoiceManager.extractChoice(text)
            : ChoiceManager.lastChoice;

    if (choice == 0) {
      // choice.php did not offer us any choices.
      // This would be a bug in KoL itself.
      return;
    }

    // We don't get redirected when we get the "not in a choice adventure" response, so handle it
    // manually.
    if (ChoiceManager.lastChoice != 0
        && ChoiceManager.lastDecision != 0
        && text.contains("Whoops!  You're not actually in a choice adventure.")) {
      ChoiceManager.lastChoice = 0;
      ChoiceManager.lastDecision = 0;
      return;
    }

    ChoiceAdventureDatabase.postChoice0(choice, request);
  }

  /**
   * Certain requests do not interrupt a choice (i.e. are accessible and do not walk away from the
   * choice)
   */
  public static boolean nonInterruptingRequest(
      final String urlString, final GenericRequest request) {
    return request.isExternalRequest
        || request.isRootsetRequest
        || request.isTopmenuRequest
        || request.isChatRequest
        || request.isChatLaunchRequest
        || request.isDescRequest
        || request.isStaticRequest
        || request.isQuestLogRequest
        // Daily Reminders
        || urlString.startsWith("main.php?checkbfast")
        // Choice 1414 uses Lock Picking
        || urlString.equals("skillz.php?oneskillz=195")
        // Choice 1399 uses Seek out a Bird
        || urlString.equals("skillz.php?oneskillz=7323");
  }

  /**
   * Common handling. Handling that is to be done after the encounter is registered, but before the
   * results are processed.
   *
   * <p>If we see that this is a <i>visit</i> to a choice adventure, rather than a response to, we
   * go to {@link #visitChoice(GenericRequest)} instead
   */
  public static void postChoice1(final String urlString, final GenericRequest request) {
    if (ChoiceManager.nonInterruptingRequest(urlString, request)) {
      return;
    }

    // If you walked away from the choice, this is not the result of a choice.
    if (ChoiceManager.canWalkAway
        && !urlString.startsWith("choice.php")
        && !urlString.startsWith("fight.php")) {
      return;
    }

    // Things that can or need to be done BEFORE processing results.
    // Remove spent items or meat here.

    if (ChoiceManager.lastChoice == 0) {
      // We are viewing the choice page for the first time.
      ChoiceManager.visitChoice(request);
      return;
    }

    String text = request.responseText;

    // If this is not actually a choice page, we were redirected.
    // Do not save this responseText
    if (urlString.startsWith("choice.php")) {
      ChoiceManager.lastResponseText = text;
    }

    ChoiceAdventureDatabase.postChoice1(ChoiceManager.lastChoice, request);

    // Process the changes that KoL doesn't openly disclose
    ChoiceManager.processManuals(ChoiceManager.lastChoice, ChoiceManager.lastDecision);
  }

  /**
   * The handling to be done once the results of the adventure were processed.
   *
   * <p>Once this is done, we also check if we can add the adventure to the NC queue.
   */
  public static void postChoice2(final String urlString, final GenericRequest request) {
    if (ChoiceManager.nonInterruptingRequest(urlString, request)) {
      return;
    }

    // The following are requests that may or may not be allowed at
    // any time, but we do them in automation during result
    // processing and they do not count as "walking away"
    if (urlString.startsWith("diary.php")) {
      return;
    }

    // Things that can or need to be done AFTER processing results.
    String text = request.responseText;

    // If you walked away from the choice (or we automated during
    // result processing), this is not a choice page
    if (ChoiceManager.canWalkAway
        && !urlString.startsWith("choice.php")
        && !urlString.startsWith("fight.php")) {
      // I removed the following line, but it caused issues.
      ChoiceManager.handlingChoice = false;
      return;
    }

    ChoiceManager.handlingChoice = ChoiceManager.stillInChoice(text);

    if (ChoiceManager.lastChoice == 0 || ChoiceManager.lastDecision == 0) {
      // This was a visit
      return;
    }

    ChoiceAdventureDatabase.postChoice2(ChoiceManager.lastChoice, request);
    // Addition to the queue was done in ChoiceAdventureDatabase

    SpadingManager.processChoice(urlString, text);

    if (ChoiceManager.handlingChoice) {
      ChoiceManager.visitChoice(request);
      return;
    }

    if (text.contains("charpane.php")) {
      // Since a charpane refresh was requested, a turn might have been spent
      AdventureSpentDatabase.setNoncombatEncountered(true);
    }

    PostChoiceAction action = ChoiceManager.action;
    if (action != PostChoiceAction.NONE) {
      ChoiceManager.action = PostChoiceAction.NONE;
      switch (action) {
        case INITIALIZE:
          LoginManager.login(KoLCharacter.getUserName());
          break;
        case ASCEND:
          ValhallaManager.postAscension();
          break;
        case NONE: // only done to suppress a warning
          break;
      }
    }

    // visitChoice() gets the decorated response text, but this is not a visit.
    // If this is not actually a choice page, we were redirected.
    // Do not save this responseText
    if (urlString.startsWith("choice.php")) {
      ChoiceManager.lastDecoratedResponseText =
          RequestEditorKit.getFeatureRichHTML(request.getURLString(), text);
    }
  }

  public static void handleWalkingAway(final String urlString) {
    // If we are not handling a choice, nothing to do
    if (!ChoiceManager.handlingChoice) {
      return;
    }

    // If the choice doesn't let you walk away, normal redirect
    // processing will take care of it
    if (!ChoiceManager.canWalkAway) {
      return;
    }

    // If you walked away from the choice, we're done with the choice
    if (!urlString.startsWith("choice.php")) {
      ChoiceManager.handlingChoice = false;
      return;
    }
  }

  /**
   * Where we go to instead of {@link #postChoice1(String,GenericRequest)} when we realize that this
   * is a visit to a choice adventure, rather than a response to one.
   *
   * <p>Gives a chance to look at the raw response from KoL for this choice adventure.<br>
   * The page will be decorated afterwards.
   */
  public static void visitChoice(final GenericRequest request) {
    String text = request.responseText;
    ChoiceManager.lastChoice = ChoiceManager.extractChoice(text);

    if (ChoiceManager.lastChoice == 0) {
      // choice.php did not offer us any choices and we couldn't work out which choice it was.
      // This happens if taking a choice gives a response with a "next" link to choice.php.
      ChoiceManager.lastDecoratedResponseText =
          RequestEditorKit.getFeatureRichHTML(request.getURLString(), text);
      return;
    }

    SpadingManager.processChoiceVisit(ChoiceManager.lastChoice, text);

    // Must do this BEFORE we decorate the response text
    ChoiceManager.setCanWalkAway(ChoiceManager.lastChoice);

    ChoiceManager.lastResponseText = text;

    // Clear lastItemUsed, to prevent the item being "processed"
    // next time we simply visit the inventory.
    UseItemRequest.clearLastItemUsed();

    ChoiceAdventureDatabase.visitChoice(ChoiceManager.lastChoice, request);

    // Do this after special classes (like WumpusManager) have a
    // chance to update state in their visitChoice methods.
    ChoiceManager.lastDecoratedResponseText =
        RequestEditorKit.getFeatureRichHTML(request.getURLString(), text);
  }

  private static boolean specialChoiceHandling(final int choice, final GenericRequest request) {
    String decision = ChoiceAdventureDatabase.specialChoiceHandling(choice, request);

    if (decision == null) {
      return false;
    }

    request.addFormField("whichchoice", String.valueOf(choice));
    request.addFormField("option", decision);
    request.addFormField("pwd", GenericRequest.passwordHash);
    request.run();

    ChoiceManager.lastResponseText = request.responseText;
    ChoiceManager.lastDecoratedResponseText =
        RequestEditorKit.getFeatureRichHTML(request.getURLString(), request.responseText);

    return true;
  }

  public static final boolean hasGoalButton(final int choice) {
    return ChoiceAdventureDatabase.hasGoalButton(choice);
  }

  public static final void addGoalButton(final StringBuffer buffer, final String goal) {
    // Insert a "Goal" button in-line
    int index = buffer.lastIndexOf("name=choiceform1");
    if (index == -1) {
      return;
    }
    index = buffer.lastIndexOf("<form", index);
    if (index == -1) {
      return;
    }

    // Build a "Goal" button
    StringBuffer button = new StringBuffer();
    String url = "/KoLmafia/specialCommand?cmd=choice-goal&pwd=" + GenericRequest.passwordHash;
    button.append("<form name=goalform action='").append(url).append("' method=post>");
    button.append("<input class=button type=submit value=\"Go To Goal\">");

    // Add the goal
    button.append("<br><font size=-1>(");
    button.append(goal);
    button.append(")</font></form>");

    // Insert it into the page
    buffer.insert(index, button);
  }

  public static final String gotoGoal() {
    String responseText = ChoiceManager.lastResponseText;
    GenericRequest request = ChoiceManager.CHOICE_HANDLER;
    ChoiceManager.processChoiceAdventure(request, "choice.php", responseText);
    RelayRequest.specialCommandResponse = ChoiceManager.lastDecoratedResponseText;
    RelayRequest.specialCommandIsAdventure = true;
    return request.responseText;
  }

  public static void defaultRegister(final int choice, final int decision) {
    if (decision == 0) {
      return;
    }

    // Figure out which decision we took
    String desc = ChoiceManager.choiceDescription(choice, decision);
    RequestLogger.updateSessionLog("Took choice " + choice + "/" + decision + ": " + desc);
  }

  public static String choiceDescription(final int choice, final int decision) {
    String description;

    Map<Integer, String> choices = ChoiceUtilities.parseChoices(ChoiceManager.lastResponseText);
    String KoLDescription = choices.get(decision);

    description = KoLDescription == null ? "unknown" : KoLDescription;

    // If we have spoilers for this choice, add it
    Option option = ChoiceManager.choiceOptions(choice).get(IntegerPool.get(decision));
    if (option != null && option.spoilerText != null) {
      description += " ( " + option.spoilerText + " )";
    }

    return description;
  }

  /**
   * Sent here from {@code RequestLogger.registerRequest} if the url of the request starts with
   * {@code choice.php}.
   *
   * <p>The request <b>isn't sent yet</b>; we just log that there is going to be an attempt.
   *
   * @return {@code true} if the request was successfully registered by going through this function
   *     <br>
   *     (i.e. recognized this request, and logged appropriate information in the session logs to
   *     document having gone through it).
   */
  public static final boolean registerRequest(final String urlString) {
    if (!urlString.startsWith("choice.php")) {
      return false;
    }

    if (urlString.equals("choice.php")) {
      // Continuing after a multi-fight.
      // Handle those when the real choice comes up.
      return true;
    }

    GenericRequest.itemMonster = null;

    int choice = ChoiceManager.extractChoiceFromURL(urlString);
    int decision = ChoiceManager.extractOptionFromURL(urlString);
    if (choice != 0) {
      if (ChoiceAdventureDatabase.registerRequest(choice, urlString, decision)) {
        return true;
      }

      ChoiceManager.defaultRegister(choice, decision);
    } else if (decision == 0) {
      // forceoption=0 will redirect to the real choice.
      // Don't bother logging it.
      return true;
    }

    // By default, we log the url of any choice we take
    RequestLogger.updateSessionLog(urlString);

    return true;
  }

  /**
   * The registration process for noteworthy choice adventures that occur immediately after another
   * encounter, for example, the naughty sorceress's hedge maze, the three adventures to get the
   * beehive in the black forest, or the multiple successive fights/choices of the L.O.V. tunnels.
   */
  public static final void registerDeferredChoice(final int choice) {
    ChoiceAdventureDatabase.registerDeferredChoice(choice);
  }

  public static final String encounterName(
      final int choice, final String urlString, final String responseText) {
    return ChoiceAdventureDatabase.encounterName(choice, urlString, responseText);
  }

  /**
   * The method used to add a Choice Adventure to a location's noncombat queue.
   *
   * <p>Some Choice Adventures don't get added to the queue when selecting certain options, such as
   * those allowing players to leave the choice adventure for free.<br>
   * Sometimes, it's not even known on <i>this</i> choice adventure; only later, at the end of a
   * choice adventure chain.
   *
   * <p>This method stores what the first adventure of the chain was.<br>
   * We'll try to add this adventure to the queue during {@link #postChoice2(String,
   * GenericRequest)}, once we know the player's decision.
   *
   * @param choice the current choice's number
   * @param location the location we were in when we got the choice. May get overridden by this
   *     ChoiceAdventure's {@code source}
   * @param encounter the name for this encounter, that we'll want (possibly) in the queue
   */
  public static final void addToQueue(int choice, KoLAdventure location, String encounter) {
    ChoiceAdventureDatabase.addToQueue(choice, location, encounter);
  }

  public static final String findChoiceDecisionIndex(final String text, final String responseText) {
    Matcher matcher = ChoiceManager.DECISION_BUTTON_PATTERN.matcher(responseText);
    while (matcher.find()) {
      String decisionText = matcher.group(2);

      if (decisionText.contains(text)) {
        return StringUtilities.getEntityDecode(matcher.group(1));
      }
    }

    return "0";
  }

  public static final String findChoiceDecisionText(final int index, final String responseText) {
    Matcher matcher = ChoiceManager.DECISION_BUTTON_PATTERN.matcher(responseText);
    while (matcher.find()) {
      int decisionIndex = Integer.parseInt(matcher.group(1));

      if (decisionIndex == index) {
        return matcher.group(2);
      }
    }

    return null;
  }

  public static final void setSkillUses(final int uses) {
    // Used for casting skills that lead to a choice adventure
    ChoiceManager.skillUses = uses;
  }

  public static final int getSkillUses() {
    return ChoiceManager.skillUses;
  }

  public static boolean canWalkAway() {
    return ChoiceManager.canWalkAway;
  }

  private static void setCanWalkAway(final int choice) {
    ChoiceManager.canWalkAway = ChoiceManager.canWalkFromChoice(choice);
  }

  public static boolean canWalkFromChoice(int choice) {
    return ChoiceAdventureDatabase.canWalkFromChoice(choice);
  }

  public static void logChoices() {
    // Log choice options to the session log
    int choice = ChoiceManager.currentChoice();
    Map<Integer, String> choices =
        ChoiceUtilities.parseChoicesWithSpoilers(ChoiceManager.lastResponseText);
    for (Map.Entry<Integer, String> entry : choices.entrySet()) {
      RequestLogger.updateSessionLog(
          "choice " + choice + "/" + entry.getKey() + ": " + entry.getValue());
    }
    // Give prettier and more verbose output to the gCLI
    ChoiceUtilities.printChoices(ChoiceManager.lastResponseText);
  }
}
