/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package mtg_json;

/**
 *
 * @author E
 */
public class Card {
    
    String name;
                    String cost;
                    int cmc;
                    String type;
                    String subtype;
                    String rarity;
                    String card_text;
                    String flavor;
                    String artist;
                    int power;
                    int toughness;
                    String imageName;
    
    public Card(String name, String cost, int cmc, String type, String subtype, String rarity, String card_text, String flavor, String artist, int power, int toughness, String imageName)
    {
        this.name = name;
        this.cost = cost;
        this.cmc = cmc;
        this.type = type;
        this.subtype = subtype;
        this.rarity = rarity;
        this.card_text = card_text;
        this.flavor = flavor;
        this.artist = artist;
        this.power = power;
        this.toughness = toughness;
        this.imageName = imageName;
    }

    public String getName() {
        return name;
    }

    public String getCost() {
        return cost;
    }

    public int getCmc() {
        return cmc;
    }

    public String getType() {
        return type;
    }

    public String getRarity() {
        return rarity;
    }

    public String getCard_text() {
        return card_text;
    }

    public String getFlavor() {
        return flavor;
    }

    public String getArtist() {
        return artist;
    }

    public int getPower() {
        return power;
    }

    public int getToughness() {
        return toughness;
    }

    public String getImageName() {
        return imageName;
    }
    
    
}
