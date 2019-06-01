package CustomStart.Patches;

import CustomStart.CustomRunMods.CustomDeckScreenBase;
import CustomStart.CustomRunMods.Relicselectscreen;
import basemod.BaseMod;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.evacipated.cardcrawl.modthespire.lib.*;
import com.evacipated.cardcrawl.modthespire.patcher.PatchingException;
import com.megacrit.cardcrawl.dungeons.AbstractDungeon;
import javassist.CannotCompileException;
import javassist.CtBehavior;

import java.lang.reflect.Method;
import java.util.ArrayList;

public class AbstractDungeonPatcher {
  @SpireEnum
  public static AbstractDungeon.CurrentScreen CUSTOMSTARTDECKGRIDSCREEN;
  @SpireEnum
  public static AbstractDungeon.CurrentScreen CUSTOMSTARTDECKRELICSELECT;


  @SpirePatch(
      clz = AbstractDungeon.class,
      method = SpirePatch.CLASS
  )
  public static class CustomDeckScreenField {
    public static SpireField<CustomDeckScreenBase.CustomDeckScreen> customDeckScreen = new SpireField<CustomDeckScreenBase.CustomDeckScreen>(() -> null);
  }
  @SpirePatch(
      clz = AbstractDungeon.class,
      method = SpirePatch.CLASS
  )
  public static class CustomRelicScreenField{
    public static SpireField<Relicselectscreen> customRelicScreen = new SpireField<Relicselectscreen>(() -> null);

  }
  @SpirePatch(
      clz = AbstractDungeon.class,
      method = "render"
  )

  public static class CurrentScreenRender {
    @SpireInsertPatch(locator = RenderLocator.class)
    public static void Insert(AbstractDungeon __instance, SpriteBatch sb) {
      if (AbstractDungeon.screen == CUSTOMSTARTDECKGRIDSCREEN) {
       if (CustomDeckScreenField.customDeckScreen.get(__instance) == null)
         CustomDeckScreenField.customDeckScreen.set(__instance, new CustomDeckScreenBase.CustomDeckScreen());
        CustomDeckScreenField.customDeckScreen.get(__instance).render(sb);
      }
      if (AbstractDungeon.screen == CUSTOMSTARTDECKRELICSELECT) {
        if (CustomRelicScreenField.customRelicScreen.get(__instance) == null)
          CustomRelicScreenField.customRelicScreen.set(__instance, new Relicselectscreen());
        CustomRelicScreenField.customRelicScreen.get(__instance).render(sb);
      }
    }
  }
  @SpirePatch(
      clz = AbstractDungeon.class,
      method = "closeCurrentScreen"
  )

  public static class CurrentScreenClose {
    @SpireInsertPatch(locator = RenderLocator.class)
    public static void Insert() {
      try {
        Method overlayReset = AbstractDungeon.class.getDeclaredMethod("genericScreenOverlayReset", null);
        overlayReset.setAccessible(true);
        overlayReset.invoke(overlayReset,null);
      }
      catch (Exception e) {
        BaseMod.logger.debug(e.getMessage());
      }
    }
  }

  @SpirePatch(
      clz = AbstractDungeon.class,
      method = "update"
  )

  public static class CurrentScreenUpdate {
    @SpireInsertPatch(locator = RenderLocator.class)
    public static void Insert(AbstractDungeon __instance) {
      if (AbstractDungeon.screen == CUSTOMSTARTDECKGRIDSCREEN) {
        if (CustomDeckScreenField.customDeckScreen.get(__instance) == null)
          CustomDeckScreenField.customDeckScreen.set(__instance, new CustomDeckScreenBase.CustomDeckScreen());
        CustomDeckScreenField.customDeckScreen.get(__instance).update();
      }
      if (AbstractDungeon.screen == CUSTOMSTARTDECKRELICSELECT) {
        if (CustomRelicScreenField.customRelicScreen.get(__instance) == null)
          CustomRelicScreenField.customRelicScreen.set(__instance, new Relicselectscreen());
        CustomRelicScreenField.customRelicScreen.get(__instance).update();
      }
    }
  }

  private static class RenderLocator extends SpireInsertLocator {
    @Override
    public int[] Locate(CtBehavior ctBehavior) throws CannotCompileException, PatchingException {

      Matcher finalMatcher = new Matcher.FieldAccessMatcher(
          "com.megacrit.cardcrawl.dungeons.AbstractDungeon", "screen");

      return LineFinder.findInOrder(ctBehavior, new ArrayList<>(), finalMatcher);
    }
  }
}
