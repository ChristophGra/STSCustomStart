package CustomStart.CustomRunMods;

import com.megacrit.cardcrawl.daily.mods.AbstractDailyMod;
import com.megacrit.cardcrawl.core.CardCrawlGame;
import com.megacrit.cardcrawl.localization.RunModStrings;

public class RelicSelection extends AbstractDailyMod {
  public static final String ID = "CustomStart:CustomRelic";
  private static final RunModStrings modStrings;
  public static final String NAME;
  public static final String DESC;
  public RelicSelection() {
    super(ID, NAME, DESC, null, true);
  }
  static {
    modStrings = CardCrawlGame.languagePack.getRunModString("CustomStart:CustomRelic");

    NAME = modStrings.NAME;
    DESC = modStrings.DESCRIPTION;
  }
  public static void initialize() {
    new RelicSelection();
  }
}
