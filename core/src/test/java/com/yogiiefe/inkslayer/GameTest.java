package com.yogiiefe.inkslayer;

import com.yogiiefe.inkslayer.world.*;
import com.yogiiefe.inkslayer.recognition.*;
import org.junit.jupiter.api.*;
import java.nio.file.*;
import java.util.*;
import static org.junit.jupiter.api.Assertions.*;

class GameTest {
    static GameContent data;
    static KanjiRecognizer recognizer;
    @BeforeAll static void load() throws Exception {
        var root=Path.of(System.getProperty("inkslayer.assets"));
        data=new GameContent(Files.readString(root.resolve("data/level.json")),Files.readString(root.resolve("data/kanji/water.json")));
        recognizer=new KanjiRecognizer(data.water,data.threshold);
    }
    @Test void canonicalWaterAccepted(){assertTrue(recognizer.recognize(data.water,true).accepted());}
    @Test void translationAndUniformScaleInvariant(){
        var changed=data.water.stream().map(s->s.stream().map(p->new InkPoint(p.x()*2.7+123,p.y()*2.7-65)).toList()).toList();
        assertTrue(recognizer.recognize(changed,true).accepted());
    }
    @Test void jitterTolerated(){
        Random r=new Random(12);
        var changed=data.water.stream().map(s->s.stream().map(p->new InkPoint(p.x()+r.nextDouble()*2-1,p.y()+r.nextDouble()*2-1)).toList()).toList();
        assertTrue(recognizer.recognize(changed,false).accepted());
    }
    @Test void wrongCountAndEmptyRejected(){assertFalse(recognizer.recognize(List.of(),false).accepted());assertFalse(recognizer.recognize(data.water.subList(0,3),false).accepted());}
    @Test void orderCoachingSeparateFromCombat(){var changed=new ArrayList<>(data.water);Collections.swap(changed,1,2);var result=recognizer.recognize(changed,false);assertTrue(result.accepted());assertFalse(result.orderCorrect());assertFalse(recognizer.recognize(changed,true).accepted());}
    @Test void reversedCurvesFailStrictDirection(){
        var changed=new ArrayList<>(data.water);var reversed=new ArrayList<>(changed.getFirst());Collections.reverse(reversed);changed.set(0,reversed);
        assertTrue(recognizer.recognize(changed,false).accepted());assertFalse(recognizer.recognize(changed,true).directionCorrect());assertFalse(recognizer.recognize(changed,true).accepted());
    }
    @Test void nonfiniteAndTapsRejected(){var changed=new ArrayList<>(data.water);changed.set(0,List.of(new InkPoint(Double.NaN,0),new InkPoint(1,2)));assertFalse(recognizer.recognize(changed,false).accepted());changed.set(0,List.of(new InkPoint(0,0),new InkPoint(0,0)));assertFalse(recognizer.recognize(changed,false).accepted());}
    @Test void unrelatedFourLinesRejected(){
        var lines=List.of(List.of(new InkPoint(0,0),new InkPoint(100,0)),List.of(new InkPoint(0,25),new InkPoint(100,25)),List.of(new InkPoint(0,50),new InkPoint(100,50)),List.of(new InkPoint(0,75),new InkPoint(100,75)));
        assertFalse(recognizer.recognize(lines,false).accepted());
    }
    @Test void randomScribbleCorpusRejected(){
        Random r=new Random(441);int accepted=0;
        for(int n=0;n<100;n++){var strokes=new ArrayList<List<InkPoint>>();for(int s=0;s<4;s++){var p=new ArrayList<InkPoint>();for(int j=0;j<18;j++)p.add(new InkPoint(r.nextDouble()*100,r.nextDouble()*100));strokes.add(p);}if(recognizer.recognize(strokes,false).accepted())accepted++;}
        assertEquals(0,accepted,"Seeded negatives must not cast");
    }
    @Test void undoClearAndCancelledStroke(){var d=new Drawing();d.begin(10,10);d.add(20,20);d.end();d.begin(40,40);d.undo();assertEquals(1,d.count());d.undo();assertEquals(0,d.count());d.begin(5,5);d.cancelActive();d.end();assertEquals(0,d.count());d.clear();assertFalse(d.isActive());}
    @Test void elementalMatrix(){assertEquals(60,data.damage("WATER","FIRE"));assertEquals(30,data.damage("FIRE","EARTH"));assertEquals(15,data.damage("FIRE","WATER"));assertThrows(IllegalArgumentException.class,()->data.damage("UNKNOWN","FIRE"));}
    @Test void tinyCharacterRejected(){var tiny=data.water.stream().map(s->s.stream().map(p->new InkPoint(p.x()*.01,p.y()*.01)).toList()).toList();assertFalse(recognizer.recognize(tiny,false).accepted());}
    private World encounter() {
        var world = new World(data);
        for (int i = 0; i < 600 && world.phase == World.Phase.WALK; i++) world.tick(1);
        assertEquals(World.Phase.PLAYER_TURN, world.phase);
        return world;
    }

    @Test void walkingTriggersEncounterAndLocksMovement() {
        var world = new World(data);
        assertFalse(world.cast());
        world.tick(-1);
        assertTrue(world.x < data.spawnX);
        world = encounter();
        double x = world.x;
        for (int i = 0; i < 120; i++) world.tick(1);
        assertEquals(x, world.x);
        assertEquals(World.MAX_HEALTH, world.health);
    }

    @Test void recognizedWaterAlternatesTurnsAndWinsWithoutFinalCounter() {
        var world = encounter();
        assertTrue(recognizer.recognize(data.water, false).accepted());
        assertTrue(world.cast());
        assertEquals(World.Phase.MONSTER_TURN, world.phase);
        assertFalse(world.cast());
        for (int i = 0; i < 44; i++) world.tick(0);
        assertEquals(3, world.health);
        world.tick(0);
        assertEquals(2, world.health);
        assertEquals(World.Phase.PLAYER_TURN, world.phase);
        assertTrue(world.cast());
        assertEquals(World.Phase.WON, world.phase);
        for (int i = 0; i < 60; i++) world.tick(0);
        assertEquals(2, world.health);
    }

    @Test void lethalCounterAndReset() {
        var world = encounter();
        world.health = 1;
        assertTrue(world.cast());
        for (int i = 0; i < 45; i++) world.tick(0);
        assertEquals(World.Phase.LOST, world.phase);
        assertFalse(world.cast());
        world.reset();
        assertEquals(World.Phase.WALK, world.phase);
        assertEquals(World.MAX_HEALTH, world.health);
        assertEquals(data.enemyHealth, world.enemyHealth);
    }
}