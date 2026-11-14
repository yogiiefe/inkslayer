package com.yogiiefe.inkslayer.world;

import com.badlogic.gdx.utils.JsonReader;
import com.badlogic.gdx.utils.JsonValue;
import com.yogiiefe.inkslayer.recognition.InkPoint;
import java.util.ArrayList;
import java.util.List;

/** JSON adapter; no OpenGL or Gdx globals needed. */
public final class GameContent {
    public final List<List<InkPoint>> water = new ArrayList<>();
    public final double width, spawnX, enemyX, threshold;
    public final int damage, enemyHealth;
    private final double[][] matrix;
    public GameContent(String levelJson,String kanjiJson) {
        var level=new JsonReader().parse(levelJson); var kanji=new JsonReader().parse(kanjiJson);
        width=level.getDouble("width"); spawnX=level.getDouble("spawnX"); enemyX=level.getDouble("enemyX");
        damage=level.get("spell").getInt("damage"); enemyHealth=level.get("enemy").getInt("health");
        threshold=kanji.getDouble("threshold");
        for(var stroke:kanji.get("strokes")) {
            var points=new ArrayList<InkPoint>();
            for(var c:stroke) {
                if(c.size!=8) throw new IllegalArgumentException("Invalid cubic template");
                for(int i=0;i<=16;i++) {
                    double t=i/16.0,u=1-t;
                    points.add(new InkPoint(u*u*u*c.getDouble(0)+3*u*u*t*c.getDouble(2)+3*u*t*t*c.getDouble(4)+t*t*t*c.getDouble(6),u*u*u*c.getDouble(1)+3*u*u*t*c.getDouble(3)+3*u*t*t*c.getDouble(5)+t*t*t*c.getDouble(7)));
                }
            }
            water.add(List.copyOf(points));
        }
        matrix=new double[4][4]; JsonValue rows=level.get("damageMatrix");
        if(rows.size!=4) throw new IllegalArgumentException("Invalid element matrix");
        for(int i=0;i<4;i++) { if(rows.get(i).size!=4) throw new IllegalArgumentException("Invalid row");
            for(int j=0;j<4;j++) { matrix[i][j]=rows.get(i).getDouble(j); if(!Double.isFinite(matrix[i][j])||matrix[i][j]<0) throw new IllegalArgumentException("Invalid multiplier"); }
        }
        if(width<=640||spawnX<0||enemyX<=spawnX+180||enemyX>=width||damage<=0||enemyHealth<=0) throw new IllegalArgumentException("Invalid level");
    }
    public int damage(String attack,String defense) {
        var ids=List.of("FIRE","WATER","WOOD","EARTH");
        int a=ids.indexOf(attack),d=ids.indexOf(defense);
        if(a<0||d<0) throw new IllegalArgumentException("Unknown element");
        return (int)Math.round(damage*matrix[a][d]);
    }
}
