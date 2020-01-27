package CustomStart.CustomRunMods;

import CustomStart.Patches.AbstractDungeonPatcher;
import basemod.BaseMod;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.math.MathUtils;
import com.megacrit.cardcrawl.cards.AbstractCard;
import com.megacrit.cardcrawl.cards.CardGroup;
import com.megacrit.cardcrawl.core.CardCrawlGame;
import com.megacrit.cardcrawl.core.Settings;
import com.megacrit.cardcrawl.daily.mods.Hoarder;
import com.megacrit.cardcrawl.dungeons.AbstractDungeon;
import com.megacrit.cardcrawl.helpers.*;
import com.megacrit.cardcrawl.helpers.controller.CInputActionSet;
import com.megacrit.cardcrawl.helpers.input.InputHelper;
import com.megacrit.cardcrawl.screens.mainMenu.ScrollBar;
import com.megacrit.cardcrawl.screens.mainMenu.ScrollBarListener;
import com.megacrit.cardcrawl.ui.buttons.GridSelectConfirmButton;

import java.util.ArrayList;
import java.util.Iterator;

public class CustomDeckScreenBase {

  public static CustomDeckScreen generateScreen() {
    return new CustomDeckScreen();
  }

  public static class CustomDeckScreen  implements ScrollBarListener {
    private static float drawStartX;
    private static float drawStartY;
    private static float padX;
    private static float padY;
    private static final float SCROLL_BAR_THRESHOLD;
    private float grabStartY = 0.0F;
    private float currentDiffY = 0.0F;
    private CardGroup targetGroup;
    private AbstractCard hoveredCard = null;
    private AbstractCard upgradePreviewCard = null;
    private float scrollLowerBound;
    private float scrollUpperBound;
    private boolean grabbedScreen;
    private boolean confirmScreenUp;
    private GridSelectConfirmButton confirmButton;
    private ScrollBar scrollBar;
    private AbstractCard controllerCard;
    private ArrayList<AbstractCard.CardColor> colorList;

    public CustomDeckScreen()  {
      this.scrollLowerBound = -Settings.DEFAULT_SCROLL_LIMIT;
      this.scrollUpperBound = Settings.DEFAULT_SCROLL_LIMIT;
      this.grabbedScreen = false;
      this.confirmScreenUp = false;
      this.confirmButton = new GridSelectConfirmButton("Selection done");
      this.controllerCard = null;
      drawStartX = (float) Settings.WIDTH;
      drawStartX -= 5.0F * AbstractCard.IMG_WIDTH * 0.75F;
      drawStartX -= 4.0F * Settings.CARD_VIEW_PAD_X;
      drawStartX /= 2.0F;
      drawStartX += AbstractCard.IMG_WIDTH * 0.75F / 2.0F;
      padX = AbstractCard.IMG_WIDTH * 0.75F + Settings.CARD_VIEW_PAD_X;
      padY = AbstractCard.IMG_HEIGHT * 0.75F + Settings.CARD_VIEW_PAD_Y;
      this.scrollBar = new ScrollBar(this);
      this.scrollBar.move(0.0F, -30.0F * Settings.scale);
      this.targetGroup = new CardGroup(CardGroup.CardGroupType.MASTER_DECK);
    }

    private void InitCardList(AbstractCard.CardColor currcolor) {
      ArrayList<AbstractCard> cardList = CardLibrary.getAllCards();
      cardList.removeIf(x -> (x.color != currcolor));
      cardList.removeIf((abstractCard -> abstractCard.color == AbstractCard.CardColor.CURSE || abstractCard.type == AbstractCard.CardType.STATUS));
      cardList.sort((AbstractCard o1, AbstractCard o2) -> o2.cost - o1.cost);
      cardList.sort((AbstractCard o1, AbstractCard o2) -> o2.rarity.compareTo(o1.rarity));
      cardList.sort((AbstractCard o1, AbstractCard o2) -> o2.type.compareTo(o1.type));
      cardList.sort((AbstractCard o1, AbstractCard o2) -> o2.color.compareTo(o1.color));
      this.targetGroup.clear();
      for (AbstractCard card : cardList) {
        card.isSeen = true;
        this.targetGroup.addToBottom(card);
      }
    }

    public void open() {
      AbstractDungeon.player.masterDeck.clear();
      colorList = new ArrayList<>();
      colorList.add(AbstractCard.CardColor.BLUE);
      colorList.add(AbstractCard.CardColor.RED);
      colorList.add(AbstractCard.CardColor.GREEN);
      colorList.add(AbstractCard.CardColor.PURPLE);
      colorList.add(AbstractCard.CardColor.COLORLESS);
      colorList.addAll(BaseMod.getCardColors());
      colorList.remove(AbstractCard.CardColor.CURSE);
      InitCardList(colorList.get(0));
      colorList.remove(colorList.get(0));
      this.callOnOpen();
      AbstractDungeon.overlayMenu.cancelButton.hide();
      this.confirmButton.isDisabled = false;
      this.confirmButton.show();
      this.calculateScrollBounds();
    }

    public void update() {
      if (this.targetGroup == null || this.targetGroup.size() == 0) {
        if (colorList == null || colorList.size() == 0)
         this.open();
      }
      this.updateControllerInput();
      if (Settings.isControllerMode && this.controllerCard != null && !CardCrawlGame.isPopupOpen && this.upgradePreviewCard == null) {
        if ((float) Gdx.input.getY() > (float) Settings.HEIGHT * 0.75F) {
          this.currentDiffY += Settings.SCROLL_SPEED;
        } else if ((float) Gdx.input.getY() < (float) Settings.HEIGHT * 0.25F) {
          this.currentDiffY -= Settings.SCROLL_SPEED;
        }
      }
      boolean isDraggingScrollBar = false;
      if (this.shouldShowScrollBar()) {
        isDraggingScrollBar = this.scrollBar.update();
      }
      if (!isDraggingScrollBar) {
        this.updateScrolling();
      }
      this.confirmButton.update();
      if (this.confirmButton.hb.clicked) {
        this.confirmButton.hb.clicked = false;
        if (colorList.size() > 0) {
          InitCardList(colorList.get(0));
          colorList.remove(0);
        } else {
          AbstractDungeon.closeCurrentScreen();
          if (CardCrawlGame.trial.dailyModIDs().contains(RelicSelection.ID))
            new Relicselectscreen().open();
        }
      } else {
        this.updateCardPositionsAndHoverLogic();
        if (this.hoveredCard != null && InputHelper.justClickedLeft) {
          this.hoveredCard.hb.clickStarted = true;
        }
        if (this.hoveredCard != null && InputHelper.justClickedRight) {
          if (this.hoveredCard.canUpgrade()) {
            this.hoveredCard.upgrade();
          }
          else if (this.hoveredCard.upgraded)
          {
            this.hoveredCard = CardLibrary.getCard(this.hoveredCard.cardID);
          }
        }
        if (this.hoveredCard != null && (this.hoveredCard.hb.clicked || CInputActionSet.select.isJustPressed())) {
          this.hoveredCard.hb.clicked = false;
          if (Gdx.input.isKeyPressed(Input.Keys.SHIFT_LEFT) || Gdx.input.isKeyPressed(Input.Keys.SHIFT_RIGHT)) {
            AbstractDungeon.player.masterDeck.removeCard(hoveredCard.cardID);
          }
          else {
            if (CardCrawlGame.trial.dailyModIDs().contains(Hoarder.ID))
              for (int i = 0; i<3; i++)
                AbstractDungeon.player.masterDeck.addToBottom(this.hoveredCard.makeStatEquivalentCopy());
            else
              AbstractDungeon.player.masterDeck.addToBottom(this.hoveredCard.makeStatEquivalentCopy());
            this.hoveredCard.targetDrawScale = 0.75F;
            this.hoveredCard.drawScale = 0.875F;
            CardCrawlGame.sound.play("CARD_SELECT");
          }

          return;
        }
        if (Settings.isControllerMode && this.controllerCard != null) {
          Gdx.input.setCursorPosition((int) this.controllerCard.hb.cX, (int) ((float) Settings.HEIGHT - this.controllerCard.hb.cY));
        }
      }
    }

    private void updateControllerInput() {
      if (Settings.isControllerMode && this.upgradePreviewCard == null) {
        boolean anyHovered = false;
        int index = 0;
        for (Iterator var3 = this.targetGroup.group.iterator(); var3.hasNext(); ++index) {
          AbstractCard c = (AbstractCard) var3.next();
          if (c.hb.hovered) {
            anyHovered = true;
            break;
          }
        }
        if (!anyHovered) {
          Gdx.input.setCursorPosition((int) (this.targetGroup.group.get(0)).hb.cX, (int) (this.targetGroup.group.get(0)).hb.cY);
          this.controllerCard =this.targetGroup.group.get(0);
        } else if ((CInputActionSet.up.isJustPressed() || CInputActionSet.altUp.isJustPressed()) && this.targetGroup.size() > 5) {
          if (index < 5) {
            index = this.targetGroup.size() + 2 - (4 - index);
            if (index > this.targetGroup.size() - 1) {
              index -= 5;
            }
            if (index > this.targetGroup.size() - 1 || index < 0) {
              index = 0;
            }
          } else {
            index -= 5;
          }
          Gdx.input.setCursorPosition((int) (this.targetGroup.group.get(index)).hb.cX, Settings.HEIGHT - (int) ( this.targetGroup.group.get(index)).hb.cY);
          this.controllerCard =  this.targetGroup.group.get(index);
        } else if ((CInputActionSet.down.isJustPressed() || CInputActionSet.altDown.isJustPressed()) && this.targetGroup.size() > 5) {
          if (index < this.targetGroup.size() - 5) {
            index += 5;
          } else {
            index %= 5;
          }
          Gdx.input.setCursorPosition((int) this.targetGroup.group.get(index).hb.cX, Settings.HEIGHT - (int) this.targetGroup.group.get(index).hb.cY);
          this.controllerCard = this.targetGroup.group.get(index);
        } else if (!CInputActionSet.left.isJustPressed() && !CInputActionSet.altLeft.isJustPressed()) {
          if (CInputActionSet.right.isJustPressed() || CInputActionSet.altRight.isJustPressed()) {
            if (index % 5 < 4) {
              ++index;
              if (index > this.targetGroup.size() - 1) {
                index -= this.targetGroup.size() % 5;
              }
            } else {
              index -= 4;
              if (index < 0) {
                index = 0;
              }
            }
            Gdx.input.setCursorPosition((int) this.targetGroup.group.get(index).hb.cX, Settings.HEIGHT - (int) this.targetGroup.group.get(index).hb.cY);
            this.controllerCard = this.targetGroup.group.get(index);
          }
        } else {
          if (index % 5 > 0) {
            --index;
          } else {
            index += 4;
            if (index > this.targetGroup.size() - 1) {
              index = this.targetGroup.size() - 1;
            }
          }
          Gdx.input.setCursorPosition((int) (this.targetGroup.group.get(index)).hb.cX, Settings.HEIGHT - (int) (this.targetGroup.group.get(index)).hb.cY);
          this.controllerCard = this.targetGroup.group.get(index);
        }

      }
    }

    private void updateCardPositionsAndHoverLogic() {
      int lineNum = 0;
      ArrayList<AbstractCard> cards = this.targetGroup.group;
      for (int i = 0; i < cards.size(); ++i) {
        int mod = i % 5;
        if (mod == 0 && i != 0) {
          ++lineNum;
        }
        (cards.get(i)).target_x = drawStartX + (float) mod * padX;
        (cards.get(i)).target_y = drawStartY + this.currentDiffY - (float) lineNum * padY;
        (cards.get(i)).fadingOut = false;
        (cards.get(i)).update();
        (cards.get(i)).updateHoverLogic();
        this.hoveredCard = null;
        Iterator var5 = cards.iterator();

        while (var5.hasNext()) {
          AbstractCard c = (AbstractCard) var5.next();
          if (c.hb.hovered) {
            this.hoveredCard = c;
          }
        }
      }
    }

    private void callOnOpen() {
      if (Settings.isControllerMode) {
        Gdx.input.setCursorPosition(10, Settings.HEIGHT / 2);
        this.controllerCard = null;
      }
      this.confirmScreenUp = false;
      AbstractDungeon.overlayMenu.proceedButton.hide();
      this.controllerCard = null;
      this.hoveredCard = null;
      AbstractDungeon.topPanel.unhoverHitboxes();
      this.currentDiffY = 0.0F;
      this.grabStartY = 0.0F;
      this.grabbedScreen = false;
      this.hideCards();
      AbstractDungeon.isScreenUp = true;
      AbstractDungeon.screen = AbstractDungeonPatcher.CUSTOMSTARTDECKGRIDSCREEN;
      AbstractDungeon.overlayMenu.showBlackScreen(0.5F);
      this.confirmButton.hideInstantly();
      if (this.targetGroup.group.size() <= 5) {
        drawStartY = (float) Settings.HEIGHT * 0.5F;
      } else {
        drawStartY = (float) Settings.HEIGHT * 0.66F;
      }
    }

    private void updateScrolling() {

        int y = InputHelper.mY;
        boolean isDraggingScrollBar = this.scrollBar.update();
        if (!isDraggingScrollBar) {
          if (!this.grabbedScreen) {
            if (InputHelper.scrolledDown) {
              this.currentDiffY += Settings.SCROLL_SPEED;
            } else if (InputHelper.scrolledUp) {
              this.currentDiffY -= Settings.SCROLL_SPEED;
            }

            if (InputHelper.justClickedLeft) {
              this.grabbedScreen = true;
              this.grabStartY = (float) y - this.currentDiffY;
            }
          } else if (InputHelper.isMouseDown) {
            this.currentDiffY = (float) y - this.grabStartY;
          } else {
            this.grabbedScreen = false;
          }
        }

        if (0 != this.targetGroup.size()) {
          this.calculateScrollBounds();
        }

        this.resetScrolling();
        this.updateBarPosition();

    }

    private void calculateScrollBounds() {
      int scrollTmp;
      if (this.targetGroup.size() > 10) {
        scrollTmp = this.targetGroup.size() / 5 - 2;
        if (this.targetGroup.size() % 5 != 0) {
          ++scrollTmp;
        }

        this.scrollUpperBound = Settings.DEFAULT_SCROLL_LIMIT + (float) scrollTmp * padY;
      } else {
        this.scrollUpperBound = Settings.DEFAULT_SCROLL_LIMIT;
      }
    }

    private void resetScrolling() {
      if (this.currentDiffY < this.scrollLowerBound) {
        this.currentDiffY = MathHelper.scrollSnapLerpSpeed(this.currentDiffY, this.scrollLowerBound);
      } else if (this.currentDiffY > this.scrollUpperBound) {
        this.currentDiffY = MathHelper.scrollSnapLerpSpeed(this.currentDiffY, this.scrollUpperBound);
      }

    }

    private void hideCards() {
      int lineNum = 0;
      ArrayList<AbstractCard> cards = this.targetGroup.group;

      for (int i = 0; i < cards.size(); ++i) {
        (cards.get(i)).setAngle(0.0F, true);
        int mod = i % 5;
        if (mod == 0 && i != 0) {
          ++lineNum;
        }

        (cards.get(i)).lighten(true);
        (cards.get(i)).current_x = drawStartX + (float) mod * padX;
        (cards.get(i)).current_y = drawStartY + this.currentDiffY - (float) lineNum * padY - MathUtils.random(100.0F * Settings.scale, 200.0F * Settings.scale);
        (cards.get(i)).targetDrawScale = 0.75F;
        (cards.get(i)).drawScale = 0.75F;
      }
    }

    public void render(SpriteBatch sb) {
      if (this.shouldShowScrollBar()) {
        this.scrollBar.render(sb);
      }

      if (this.hoveredCard != null) {
        this.hoveredCard.renderHoverShadow(sb);
        this.hoveredCard.render(sb);
        this.hoveredCard.renderCardTip(sb);
      }

      if (this.confirmScreenUp) {
        sb.setColor(new Color(0.0F, 0.0F, 0.0F, 0.8F));
        sb.draw(ImageMaster.WHITE_SQUARE_IMG, 0.0F, 0.0F, (float) Settings.WIDTH, (float) Settings.HEIGHT - 64.0F * Settings.scale);

        this.hoveredCard.current_x = (float) Settings.WIDTH / 2.0F;
        this.hoveredCard.current_y = (float) Settings.HEIGHT / 2.0F;
        this.hoveredCard.render(sb);
        this.hoveredCard.updateHoverLogic();

      }
      this.targetGroup.render(sb);
      this.confirmButton.render(sb);
      FontHelper.renderDeckViewTip(sb, "Select all cards you want in your deck at the start of the run.", 96.0F * Settings.scale, Settings.CREAM_COLOR);

    }

    public void scrolledUsingBar(float newPercent) {
      this.currentDiffY = MathHelper.valueFromPercentBetween(this.scrollLowerBound, this.scrollUpperBound, newPercent);
      this.updateBarPosition();
    }

    private void updateBarPosition() {
      float percent = MathHelper.percentFromValueBetween(this.scrollLowerBound, this.scrollUpperBound, this.currentDiffY);
      this.scrollBar.parentScrolledToPercent(percent);
    }

    private boolean shouldShowScrollBar() {
      return !this.confirmScreenUp && this.scrollUpperBound > SCROLL_BAR_THRESHOLD;
    }

    static {
      SCROLL_BAR_THRESHOLD = 500.0F * Settings.scale;
    }
  }
}
