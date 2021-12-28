package dev.meyi.bn.modules.calc;

import dev.meyi.bn.BazaarNotifier;
import dev.meyi.bn.config.Configuration;
import dev.meyi.bn.utilities.Utils;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Iterator;
import java.util.Objects;
import net.minecraft.util.EnumChatFormatting;
import org.apache.commons.lang3.text.WordUtils;
import org.json.JSONException;


public class CraftingCalculator {

  private static String unlockedRecipes = "";


  public static ArrayList<ArrayList<String>> getBestEnchantRecipes() {
    ArrayList<ArrayList<String>> list = new ArrayList<>();
    Iterator<String> normalKeys = BazaarNotifier.enchantCraftingList.getJSONObject("normal").keys();

    for (int i = 0; i < BazaarNotifier.enchantCraftingList.getJSONObject("normal").length(); i++) {
      String itemName = normalKeys.next();
      if (unlockedRecipes.contains(
          BazaarNotifier.enchantCraftingList.getJSONObject("normal").getJSONObject(itemName)
              .getString("collection")) || Objects.equals(
          BazaarNotifier.enchantCraftingList.getJSONObject("normal").getJSONObject(itemName)
              .getString("collection"), "NONE") || Configuration.collectionCheckDisabled) {
        if (BazaarNotifier.bazaarDataRaw.getJSONObject(itemName).getJSONArray("sell_summary")
            .length() > 0 &&
            BazaarNotifier.bazaarDataRaw.getJSONObject(itemName).getJSONArray("buy_summary")
                .length() > 0) {
          if (!BazaarNotifier.bazaarDataRaw.getJSONObject(itemName).getJSONArray("sell_summary")
              .isEmpty()) {// I don´t know why, but it sometimes crashes with org.json.JSONException: JSONArray[0] not found.
            String material = BazaarNotifier.enchantCraftingList.getJSONObject("normal")
                .getJSONObject(itemName).getString("material");
            try {
              double price1 =
                  (BazaarNotifier.bazaarDataRaw.getJSONObject(itemName).getJSONArray("sell_summary")
                      .getJSONObject(0).getDouble("pricePerUnit")) -
                      (BazaarNotifier.bazaarDataRaw.getJSONObject(material)
                          .getJSONArray("sell_summary").getJSONObject(0).getDouble("pricePerUnit"))
                          * 160;
              double price2 =
                  BazaarNotifier.bazaarDataRaw.getJSONObject(itemName).getJSONArray("buy_summary")
                      .getJSONObject(0).getDouble("pricePerUnit") -
                      (BazaarNotifier.bazaarDataRaw.getJSONObject(material)
                          .getJSONArray("sell_summary").getJSONObject(0).getDouble("pricePerUnit"))
                          * 160;
              double profitPerMilC = 1000000 / (
                  BazaarNotifier.bazaarDataRaw.getJSONObject(material).getJSONArray("sell_summary")
                      .getJSONObject(0).getDouble("pricePerUnit") * 160);
              double profitPerMil =
                  profitPerMilC * BazaarNotifier.bazaarDataRaw.getJSONObject(itemName)
                      .getJSONArray("sell_summary").getJSONObject(0).getDouble("pricePerUnit") - (
                      BazaarNotifier.bazaarDataRaw.getJSONObject(material)
                          .getJSONArray("sell_summary").getJSONObject(0).getDouble("pricePerUnit")
                          * 160);

              list.add(new ArrayList<>(Arrays
                  .asList(Double.toString(price1), Double.toString(price2),
                      Double.toString(profitPerMil), itemName)));
            } catch (Exception ignored) {
            }
          }
        } else {
          list.add(new ArrayList<>(Arrays.asList("0", "0", "0", itemName)));
        }
      }
    }

    Iterator<String> otherKeys = BazaarNotifier.enchantCraftingList.getJSONObject("other").keys();
    for (int i = 0; i < BazaarNotifier.enchantCraftingList.getJSONObject("other").length(); i++) {

      String itemName = otherKeys.next();
      if (unlockedRecipes.contains(
          BazaarNotifier.enchantCraftingList.getJSONObject("other").getJSONObject(itemName)
              .getString("collection")) || Objects.equals(
          BazaarNotifier.enchantCraftingList.getJSONObject("other").getJSONObject(itemName)
              .getString("collection"), "NONE") || Configuration.collectionCheckDisabled) {
        if (BazaarNotifier.bazaarDataRaw.getJSONObject(itemName).getJSONArray("sell_summary")
            .length() > 0 &&
            BazaarNotifier.bazaarDataRaw.getJSONObject(itemName).getJSONArray("buy_summary")
                .length() > 0) {
          try {
            double itemSellPrice = BazaarNotifier.bazaarDataRaw.getJSONObject(itemName)
                .getJSONArray("sell_summary").getJSONObject(0).getDouble("pricePerUnit");
            double itemBuyPrice = BazaarNotifier.bazaarDataRaw.getJSONObject(itemName)
                .getJSONArray("buy_summary").getJSONObject(0).getDouble("pricePerUnit");
            double ingredientPrice = 0d;
            int ingredientCount;
            double P = 0d;

            for (int h = 0; h < BazaarNotifier.enchantCraftingList.getJSONObject("other")
                .getJSONObject(itemName).getJSONArray("material").length(); h++) {
              if (h % 2 == 0) {
                ingredientPrice = BazaarNotifier.bazaarDataRaw.getJSONObject(
                    BazaarNotifier.enchantCraftingList.getJSONObject("other")
                        .getJSONObject(itemName).getJSONArray("material").getString(h))
                    .getJSONArray("sell_summary").getJSONObject(0).getDouble("pricePerUnit");
              } else {
                ingredientCount = BazaarNotifier.enchantCraftingList.getJSONObject("other")
                    .getJSONObject(itemName).getJSONArray("material").getInt(h);
                P += (ingredientPrice * ingredientCount);
              }
            }

            double profitInstaSell = itemSellPrice - P;
            double profitSellOffer = itemBuyPrice - P;
            double countPerMil = 1000000 / P;
            double pricePerMil = countPerMil * profitInstaSell;
            list.add(new ArrayList<>(Arrays
                .asList(String.valueOf(profitInstaSell), Double.toString(profitSellOffer),
                    Double.toString(pricePerMil), itemName)));
          } catch (JSONException ignored) {
          }
        } else {
          list.add(new ArrayList<>(Arrays.asList("0", "0", "0", itemName)));
        }
      }
    }

    list.sort((o1, o2) -> {
      ArrayList<Double> list1 = new ArrayList<>();
      ArrayList<Double> list2 = new ArrayList<>();
      list1.add(Double.valueOf(o1.get(0)));
      list1.add(Double.valueOf(o1.get(1)));
      list1.add(Double.valueOf(o1.get(2)));
      list2.add(Double.valueOf(o2.get(0)));
      list2.add(Double.valueOf(o2.get(1)));
      list2.add(Double.valueOf(o2.get(2)));

      return list1.get(Configuration.craftingSortingOption).compareTo(list2.get(
          Configuration.craftingSortingOption));
    });
    Collections.reverse(list);
    return list;
  }


  public static String toggleCrafting() {
    if (Configuration.craftingSortingOption == 0) {
      Configuration.craftingSortingOption = 1;
      return "Crafting now sorts by sell offer";
    } else if (Configuration.craftingSortingOption == 1) {
      Configuration.craftingSortingOption = 2;
      return "Crafting now sorts by profit per million";
    } else {
      Configuration.craftingSortingOption = 0;
      return "Crafting now sorts by instant sell";
    }
  }

  public static String setCraftingLength(int length) {
    if (length > BazaarNotifier.enchantCraftingList.getJSONObject("normal").length()
        + BazaarNotifier.enchantCraftingList.getJSONObject("other").length()) {
      return length + " is too long";
    } else {
      Configuration.craftingListLength = length;
      return "Item list size set to " + length;
    }
  }

  public static String editCraftingModuleGUI(String craftingValue) {
    if (craftingValue.equalsIgnoreCase("instant_sell")) {
      Configuration.showInstantSellProfit = !Configuration.showInstantSellProfit;
      return
          (Configuration.showInstantSellProfit ? EnumChatFormatting.GREEN : EnumChatFormatting.RED)
              + "Toggled profit column (Instant Sell)";
    } else if (craftingValue.equalsIgnoreCase("sell_offer")) {
      Configuration.showSellOfferProfit = !Configuration.showSellOfferProfit;
      return (Configuration.showSellOfferProfit ? EnumChatFormatting.GREEN : EnumChatFormatting.RED)
          + "Toggled profit column (Sell Offer)";
    } else if (craftingValue.equalsIgnoreCase("ppm")) {
      Configuration.showProfitPerMil = !Configuration.showProfitPerMil;
      return (Configuration.showProfitPerMil ? EnumChatFormatting.GREEN : EnumChatFormatting.RED)
          + "Toggled profit column (Profit per 1M)";
    } else {
      return EnumChatFormatting.RED + "This value does not exist";
    }
  }

  public static String[] getEnchantCraft(String itemU) {
    String itemName = BazaarNotifier.bazaarConversionsReversed
        .getString(WordUtils.capitalize(itemU.toLowerCase()));
    String[] values = new String[3];
    if (BazaarNotifier.enchantCraftingList.getJSONObject("normal").has(itemName)) {
      if (!BazaarNotifier.bazaarDataRaw.isEmpty()) {
        String material = BazaarNotifier.enchantCraftingList.getJSONObject("normal")
            .getJSONObject(itemName).getString("material");
        double price1 =
            (BazaarNotifier.bazaarDataRaw.getJSONObject(itemName).getJSONArray("sell_summary")
                .getJSONObject(0).getDouble("pricePerUnit")) -
                (BazaarNotifier.bazaarDataRaw.getJSONObject(material).getJSONArray("sell_summary")
                    .getJSONObject(1).getDouble("pricePerUnit")) * 160;
        double price2 =
            BazaarNotifier.bazaarDataRaw.getJSONObject(itemName).getJSONArray("buy_summary")
                .getJSONObject(0).getDouble("pricePerUnit") -
                (BazaarNotifier.bazaarDataRaw.getJSONObject(material).getJSONArray("sell_summary")
                    .getJSONObject(1).getDouble("pricePerUnit")) * 160;
        double profitPerMilCount = 1000000 / (
            BazaarNotifier.bazaarDataRaw.getJSONObject(material).getJSONArray("sell_summary")
                .getJSONObject(0).getDouble("pricePerUnit") * 160);
        double profitPerMil =
            profitPerMilCount * BazaarNotifier.bazaarDataRaw.getJSONObject(itemName)
                .getJSONArray("sell_summary").getJSONObject(0).getDouble("pricePerUnit") - (
                BazaarNotifier.bazaarDataRaw.getJSONObject(material).getJSONArray("sell_summary")
                    .getJSONObject(1).getDouble("pricePerUnit") * 160);

        values[0] = BazaarNotifier.df.format(price1);
        values[1] = BazaarNotifier.df.format(price2);
        values[2] = BazaarNotifier.df.format(profitPerMil);
      } else {
        values[0] = BazaarNotifier.df.format(0);
        values[1] = BazaarNotifier.df.format(0);
        values[2] = BazaarNotifier.df.format(0);
      }
    } else if (BazaarNotifier.enchantCraftingList.getJSONObject("other").has(itemName)) {
      if (!BazaarNotifier.bazaarDataRaw.isEmpty()) {
        double itemSellPrice = BazaarNotifier.bazaarDataRaw.getJSONObject(itemName)
            .getJSONArray("sell_summary").getJSONObject(0).getDouble("pricePerUnit");
        double itemBuyPrice = BazaarNotifier.bazaarDataRaw.getJSONObject(itemName)
            .getJSONArray("buy_summary").getJSONObject(0).getDouble("pricePerUnit");
        double ingredientPrice = 0d;
        int ingredientCount;
        double materialCost = 0d;

        for (int h = 0;
            h < BazaarNotifier.enchantCraftingList.getJSONObject("other").getJSONObject(itemName)
                .getJSONArray("material").length(); h++) {
          if (h % 2 == 0) {
            ingredientPrice = BazaarNotifier.bazaarDataRaw.getJSONObject(
                BazaarNotifier.enchantCraftingList.getJSONObject("other").getJSONObject(itemName)
                    .getJSONArray("material").getString(h)).getJSONArray("sell_summary")
                .getJSONObject(0).getDouble("pricePerUnit");
          } else {
            ingredientCount = BazaarNotifier.enchantCraftingList.getJSONObject("other")
                .getJSONObject(itemName).getJSONArray("material").getInt(h);
            materialCost += (ingredientPrice * ingredientCount);
          }
        }

        double profitInstaSell = itemSellPrice - materialCost;
        double profitSellOffer = itemBuyPrice - materialCost;
        double CountPerMil = 1000000 / materialCost;
        double profitPerMil = CountPerMil * profitInstaSell;
        values[0] = BazaarNotifier.df.format(profitInstaSell);
        values[1] = BazaarNotifier.df.format(profitSellOffer);
        values[2] = BazaarNotifier.df.format(profitPerMil);
      } else {
        values[0] = BazaarNotifier.df.format(0);
        values[1] = BazaarNotifier.df.format(0);
        values[2] = BazaarNotifier.df.format(0);
      }
    }

    return values;

  }

  public static void getUnlockedRecipes() {
    try {
      unlockedRecipes = Utils.unlockedRecipes().toString();
    } catch (IOException ignored) {
    }
  }

}