package CustomStart.CustomRunMods;

import com.megacrit.cardcrawl.daily.mods.AbstractDailyMod;
import com.megacrit.cardcrawl.core.CardCrawlGame;
import com.megacrit.cardcrawl.localization.RunModStrings;

import java.util.List;

public class CustomDeck extends AbstractDailyMod
{
  public static final String ID = "CustomStart:CustomDeck";
  private static final RunModStrings modStrings;
  public static final String NAME;
  public static final String DESC;
  public CustomDeck() {
    super(ID, NAME, DESC, null, true);
  }
  static {
    modStrings = CardCrawlGame.languagePack.getRunModString("CustomStart:CustomDeck");

    NAME = modStrings.NAME;
    DESC = modStrings.DESCRIPTION;
  }
  public static void initialize() {
    new CustomDeck();
  }
}



