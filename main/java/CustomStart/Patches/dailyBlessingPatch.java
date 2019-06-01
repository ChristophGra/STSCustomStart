package CustomStart.Patches;

import CustomStart.CustomRunMods.CustomDeck;
import CustomStart.CustomRunMods.CustomDeckScreenBase;
import CustomStart.CustomRunMods.RelicSelection;
import CustomStart.CustomRunMods.Relicselectscreen;
import basemod.BaseMod;
import com.evacipated.cardcrawl.modthespire.lib.SpirePatch;
import com.evacipated.cardcrawl.modthespire.lib.SpirePostfixPatch;
import com.megacrit.cardcrawl.core.CardCrawlGame;
import com.megacrit.cardcrawl.helpers.ModHelper;
import com.megacrit.cardcrawl.neow.NeowEvent;

public class dailyBlessingPatch {


  @SpirePatch(
      clz = NeowEvent.class,
      method = "dailyBlessing"
  )
  public static class dailyBlessing {
    @SpirePostfixPatch(
    )
    public static void Postfix(com.megacrit.cardcrawl.neow.NeowEvent __instance) {
     if (CardCrawlGame.trial.dailyModIDs().contains(CustomDeck.ID))
       CustomDeckScreenBase.generateScreen().open();
     else if (CardCrawlGame.trial.dailyModIDs().contains(RelicSelection.ID))
       (new Relicselectscreen()).open();
    }
  }
}


