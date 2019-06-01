package CustomStart;
import CustomStart.CustomRunMods.RelicSelection;
import basemod.BaseMod;
import basemod.interfaces.PostDungeonInitializeSubscriber;
import com.badlogic.gdx.Gdx;
import com.evacipated.cardcrawl.modthespire.lib.*;
import com.megacrit.cardcrawl.core.CardCrawlGame;
import org.apache.logging.log4j.LogManager;
import CustomStart.CustomRunMods.CustomDeck;
import basemod.interfaces.AddCustomModeModsSubscriber;
import basemod.interfaces.EditStringsSubscriber;
import com.megacrit.cardcrawl.localization.RunModStrings;
import com.megacrit.cardcrawl.screens.custom.CustomMod;
import org.apache.logging.log4j.Logger;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;


@SpireInitializer
public class CustomStart implements AddCustomModeModsSubscriber, EditStringsSubscriber {
  public static final Logger logger = LogManager.getLogger(CustomStart.class.getName());

  public CustomStart() {
    BaseMod.subscribe(this);
  }
  @Override
  public void receiveCustomModeMods(List<CustomMod> list) {
    list.add(new CustomMod(CustomDeck.ID, "b", true));
    list.add(new CustomMod(RelicSelection.ID, "b", true));
  }

  public void receiveEditStrings() {
    String modStrings = Gdx.files.internal("localization/CustomStart-DailyModStrings.json").readString(String.valueOf(StandardCharsets.UTF_8));
    BaseMod.loadCustomStrings(RunModStrings.class, modStrings);
  }

  public static void initialize() {
    new CustomStart();
  }


}



