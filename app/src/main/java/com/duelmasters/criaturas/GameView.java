package com.duelmasters.criaturas;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.RectF;
import android.view.MotionEvent;
import android.view.View;
import java.util.Random;

/** A tile-based overworld, dialogue scenes and turn-based encounters drawn with original code sprites. */
public final class GameView extends View {
    private static final int MAP_W = 18, MAP_H = 13;
    private static final int GRASS = 0, PATH = 1, WATER = 2, TREE = 3, TALL_GRASS = 4, HOUSE = 5, FLOWER = 6;
    private final Paint p = new Paint(Paint.ANTI_ALIAS_FLAG);
    private final Random random = new Random();
    private final int[][] map = new int[MAP_H][MAP_W];
    private int tile, offsetX, offsetY, playerX = 8, playerY = 10, facing = 0;
    private int hp = 100, wildHp = 0, captures = 0, steps = 0, mission = 0;
    private boolean battle = false, dialogue = true;
    private String wild = "", message = "Lina: El Faro de Lúmina se ha apagado. Busca una criatura que pueda devolverle su luz.";
    private String battleMessage = "";
    private float downX, downY;

    public GameView(Context context) { super(context); p.setTypeface(android.graphics.Typeface.create("sans", 1)); createMap(); setFocusable(true); }

    private void createMap() {
        for (int y = 0; y < MAP_H; y++) for (int x = 0; x < MAP_W; x++) map[y][x] = GRASS;
        for (int x = 0; x < MAP_W; x++) { map[0][x] = TREE; map[1][x] = TREE; map[MAP_H - 1][x] = WATER; }
        for (int y = 2; y < MAP_H - 1; y++) { map[y][0] = TREE; map[y][MAP_W - 1] = TREE; }
        for (int x = 2; x < 16; x++) map[9][x] = PATH;
        for (int y = 3; y < 10; y++) map[y][8] = PATH;
        for (int y = 3; y < 6; y++) for (int x = 2; x < 6; x++) map[y][x] = TALL_GRASS;
        for (int y = 3; y < 8; y++) for (int x = 11; x < 16; x++) map[y][x] = TALL_GRASS;
        for (int y = 2; y < 9; y++) map[y][16] = WATER;
        map[7][3] = HOUSE; map[7][4] = HOUSE; map[8][3] = HOUSE; map[8][4] = HOUSE;
        map[5][9] = FLOWER; map[4][9] = FLOWER; map[3][9] = FLOWER;
    }

    @Override protected void onDraw(Canvas c) {
        super.onDraw(c); tile = Math.max(18, Math.min(getWidth() / MAP_W, (getHeight() - dp(116)) / MAP_H));
        offsetX = (getWidth() - tile * MAP_W) / 2; offsetY = dp(44);
        c.drawColor(Color.rgb(16, 32, 35));
        if (battle) drawBattle(c); else drawWorld(c);
    }

    private void drawWorld(Canvas c) {
        drawHeader(c, "PRADERA LÚMINA", "✦ " + captures + "  ·  Alba " + hp + "/100");
        for (int y = 0; y < MAP_H; y++) for (int x = 0; x < MAP_W; x++) drawTile(c, x, y, map[y][x]);
        drawNpc(c, 8, 7, Color.rgb(255, 148, 102));
        drawPlayer(c, playerX, playerY);
        drawDialogue(c, message + (dialogue ? "\n\nToca este cuadro para continuar." : ""));
        if (!dialogue) drawControls(c);
    }

    private void drawTile(Canvas c, int x, int y, int type) {
        float l = offsetX + x * tile, t = offsetY + y * tile;
        p.setStyle(Paint.Style.FILL); p.setColor(Color.rgb(112, 186, 97)); c.drawRect(l, t, l + tile, t + tile, p);
        if (type == PATH) { p.setColor(Color.rgb(218, 190, 122)); c.drawRect(l, t, l + tile, t + tile, p); p.setColor(Color.rgb(196, 166, 102)); c.drawCircle(l + tile * .25f, t + tile * .3f, tile * .06f, p); }
        if (type == WATER) { p.setColor(Color.rgb(59, 151, 205)); c.drawRect(l, t, l + tile, t + tile, p); p.setColor(Color.rgb(132, 213, 239)); p.setStrokeWidth(2); for (int i = 0; i < 2; i++) c.drawLine(l + tile * .15f, t + tile * (.3f + i * .35f), l + tile * .85f, t + tile * (.3f + i * .35f), p); }
        if (type == TREE) { p.setColor(Color.rgb(72, 104, 57)); c.drawRect(l, t, l + tile, t + tile, p); p.setColor(Color.rgb(37, 78, 45)); c.drawCircle(l + tile *.5f, t + tile *.43f, tile *.43f, p); p.setColor(Color.rgb(103, 160, 68)); c.drawCircle(l + tile *.38f, t + tile *.3f, tile *.18f, p); }
        if (type == TALL_GRASS) { p.setColor(Color.rgb(65, 141, 67)); for (int i = 1; i < 5; i++) c.drawLine(l + tile * i / 5f, t + tile, l + tile * (i / 5f - .09f), t + tile *.28f, p); }
        if (type == HOUSE) { p.setColor(Color.rgb(245, 221, 165)); c.drawRect(l + tile*.08f, t + tile*.42f, l + tile*.92f, t + tile, p); p.setColor(Color.rgb(187, 80, 65)); c.drawRect(l, t + tile*.18f, l + tile, t + tile*.48f, p); p.setColor(Color.rgb(80, 62, 49)); c.drawRect(l + tile*.4f, t + tile*.66f, l + tile*.6f, t + tile, p); }
        if (type == FLOWER) { p.setColor(Color.rgb(247, 213, 65)); c.drawCircle(l + tile*.5f, t + tile*.5f, tile*.23f, p); p.setColor(Color.WHITE); for (int i = 0; i < 4; i++) c.drawCircle(l + tile*(.5f + (i == 0 ? .2f : i == 2 ? -.2f : 0)), t + tile*(.5f + (i == 1 ? .2f : i == 3 ? -.2f : 0)), tile*.12f, p); }
        p.setStyle(Paint.Style.STROKE); p.setStrokeWidth(1); p.setColor(Color.argb(30, 0, 0, 0)); c.drawRect(l, t, l + tile, t + tile, p); p.setStyle(Paint.Style.FILL);
    }

    private void drawPlayer(Canvas c, int x, int y) { drawNpc(c, x, y, Color.rgb(68, 87, 207)); }
    private void drawNpc(Canvas c, int x, int y, int coat) {
        float l = offsetX + x * tile, t = offsetY + y * tile;
        p.setColor(Color.rgb(245, 191, 143)); c.drawCircle(l + tile*.5f, t + tile*.32f, tile*.20f, p);
        p.setColor(Color.rgb(50, 44, 56)); c.drawRect(l + tile*.28f, t + tile*.1f, l + tile*.72f, t + tile*.27f, p);
        p.setColor(coat); c.drawRoundRect(new RectF(l + tile*.26f, t + tile*.5f, l + tile*.74f, t + tile*.93f), tile*.08f, tile*.08f, p);
        p.setColor(Color.WHITE); c.drawCircle(l + tile*.43f, t + tile*.33f, tile*.025f, p); c.drawCircle(l + tile*.57f, t + tile*.33f, tile*.025f, p);
    }

    private void drawHeader(Canvas c, String title, String right) {
        p.setColor(Color.rgb(12, 62, 66)); c.drawRect(0, 0, getWidth(), dp(42), p);
        text(c, title, dp(15), dp(17), dp(14), Color.WHITE); p.setTextAlign(Paint.Align.RIGHT); text(c, right, getWidth() - dp(14), dp(17), dp(13), Color.rgb(236, 235, 178)); p.setTextAlign(Paint.Align.LEFT);
    }
    private void drawDialogue(Canvas c, String words) {
        float top = getHeight() - dp(113); p.setColor(Color.rgb(255, 250, 228)); c.drawRoundRect(new RectF(dp(10), top, getWidth()-dp(10), getHeight()-dp(10)), dp(14), dp(14), p);
        p.setColor(Color.rgb(38, 60, 55)); p.setStyle(Paint.Style.STROKE); p.setStrokeWidth(dp(2)); c.drawRoundRect(new RectF(dp(10), top, getWidth()-dp(10), getHeight()-dp(10)), dp(14), dp(14), p); p.setStyle(Paint.Style.FILL);
        drawWrapped(c, words, dp(22), top + dp(25), getWidth() - dp(44), dp(14), Color.rgb(34, 52, 48));
    }
    private void drawControls(Canvas c) {
        float cx = getWidth() - dp(74), cy = getHeight() - dp(168), s = dp(22); p.setColor(Color.argb(190, 255, 255, 255));
        c.drawCircle(cx, cy-s, s, p); c.drawCircle(cx, cy+s, s, p); c.drawCircle(cx-s, cy, s, p); c.drawCircle(cx+s, cy, s, p);
        text(c, "▲", cx, cy-s+dp(6), dp(15), Color.rgb(25, 77, 77), Paint.Align.CENTER); text(c, "▼", cx, cy+s+dp(5), dp(15), Color.rgb(25, 77, 77), Paint.Align.CENTER); text(c, "◀", cx-s, cy+dp(5), dp(15), Color.rgb(25, 77, 77), Paint.Align.CENTER); text(c, "▶", cx+s, cy+dp(5), dp(15), Color.rgb(25, 77, 77), Paint.Align.CENTER);
    }

    private void drawBattle(Canvas c) {
        p.setColor(Color.rgb(141, 213, 222)); c.drawRect(0, 0, getWidth(), getHeight(), p); drawHeader(c, "ENCUENTRO SALVAJE", "Esferas: " + (3 + captures));
        drawStatus(c, dp(18), dp(62), wild, wildHp, Color.rgb(243, 109, 95)); drawCreature(c, getWidth()* .70f, dp(142), wild.hashCode(), false);
        drawStatus(c, dp(18), getHeight()-dp(274), "ALBA", hp, Color.rgb(72, 185, 132)); drawCreature(c, getWidth()*.28f, getHeight()-dp(192), 4, true);
        p.setColor(Color.rgb(31, 52, 63)); c.drawRect(0, getHeight()-dp(132), getWidth(), getHeight(), p);
        text(c, battleMessage.length() == 0 ? "¿Qué hará Alba?" : battleMessage, dp(20), getHeight()-dp(108), dp(15), Color.WHITE);
        battleButton(c, "CHISPA\nSOLAR", dp(14), getHeight()-dp(82), Color.rgb(249, 195, 74)); battleButton(c, "NIEBLA\nLUNAR", getWidth()/2 + dp(7), getHeight()-dp(82), Color.rgb(157, 192, 255)); battleButton(c, "ESFERA", dp(14), getHeight()-dp(38), Color.rgb(238, 135, 114)); battleButton(c, "HUIR", getWidth()/2 + dp(7), getHeight()-dp(38), Color.rgb(214, 226, 222));
    }
    private void drawStatus(Canvas c, float x, float y, String name, int life, int color) { p.setColor(Color.argb(230, 255, 255, 239)); c.drawRoundRect(new RectF(x, y, x+dp(145), y+dp(54)), dp(9), dp(9), p); text(c, name, x+dp(10), y+dp(19), dp(14), Color.rgb(35, 48, 52)); text(c, "Energía " + life + "/100", x+dp(10), y+dp(39), dp(11), Color.DKGRAY); p.setColor(Color.rgb(50, 73, 70)); c.drawRoundRect(new RectF(x+dp(92), y+dp(10), x+dp(135), y+dp(16)), dp(3), dp(3), p); p.setColor(color); c.drawRoundRect(new RectF(x+dp(92), y+dp(10), x+dp(92)+dp(43)*life/100f, y+dp(16)), dp(3), dp(3), p); }
    private void drawCreature(Canvas c, float x, float y, int seed, boolean ally) { float s = dp(38); p.setColor(ally ? Color.rgb(255, 218, 88) : Color.rgb(157, 93, 214)); c.drawOval(new RectF(x-s, y-s*.65f, x+s, y+s*.65f), p); p.setColor(ally ? Color.rgb(234, 158, 55) : Color.rgb(101, 52, 154)); c.drawCircle(x-s*.42f, y-s*.62f, s*.25f, p); c.drawCircle(x+s*.42f, y-s*.62f, s*.25f, p); p.setColor(Color.WHITE); c.drawCircle(x-s*.26f, y-s*.08f, s*.13f, p); c.drawCircle(x+s*.26f, y-s*.08f, s*.13f, p); p.setColor(Color.rgb(35, 40, 57)); c.drawCircle(x-s*.26f, y-s*.08f, s*.06f, p); c.drawCircle(x+s*.26f, y-s*.08f, s*.06f, p); }
    private void battleButton(Canvas c, String label, float x, float y, int color) { float w = getWidth()/2f-dp(21), h = dp(38); p.setColor(color); c.drawRoundRect(new RectF(x,y,x+w,y+h),dp(8),dp(8),p); String[] lines=label.split("\\n"); for(int i=0;i<lines.length;i++) text(c,lines[i],x+w/2,y+dp(15)+i*dp(12),dp(11),Color.rgb(28,43,47),Paint.Align.CENTER); }

    @Override public boolean onTouchEvent(MotionEvent e) {
        if (e.getAction() == MotionEvent.ACTION_DOWN) { downX=e.getX(); downY=e.getY(); return true; }
        if (e.getAction() != MotionEvent.ACTION_UP) return true;
        if (battle) { handleBattle(e.getX(), e.getY()); return true; }
        if (dialogue) { dialogue=false; message="Explora la pradera. El césped alto esconde criaturas; habla con Lina junto al sendero."; invalidate(); return true; }
        float dx=e.getX()-downX, dy=e.getY()-downY;
        if (Math.abs(dx)>dp(18) || Math.abs(dy)>dp(18)) move(Math.abs(dx)>Math.abs(dy) ? (dx>0?1:-1) : (dy>0?2:-2));
        else { float cx=getWidth()-dp(74), cy=getHeight()-dp(168); if (Math.abs(e.getX()-cx)>dp(6) || Math.abs(e.getY()-cy)>dp(6)) move(Math.abs(e.getX()-cx)>Math.abs(e.getY()-cy) ? (e.getX()>cx?1:-1) : (e.getY()>cy?2:-2)); else interact(); }
        return true;
    }
    private void move(int direction) { int nx=playerX,ny=playerY; if(direction==1)nx++;if(direction==-1)nx--;if(direction==2)ny++;if(direction==-2)ny--; facing=direction; if(nx<0||ny<0||nx>=MAP_W||ny>=MAP_H||map[ny][nx]==TREE||map[ny][nx]==WATER||map[ny][nx]==HOUSE){message="El camino está bloqueado.";invalidate();return;} playerX=nx;playerY=ny;steps++; message=""; if(Math.abs(playerX-8)+Math.abs(playerY-7)==1) interact(); else if(map[playerY][playerX]==TALL_GRASS && steps%3==0 && random.nextInt(100)<43) startBattle(); else if(playerY==3&&playerX==9&&mission==0){mission=1;message="Encontraste una Flor de Estrella. Llévasela a Lina.";} invalidate(); }
    private void interact() { if(Math.abs(playerX-8)+Math.abs(playerY-7)<=1){ if(mission==0)message="Lina: Las Flores de Estrella crecen al norte. Una de ellas puede despertar el Faro."; else if(mission==1){mission=2;captures++;message="Lina: ¡La encontraste! Alba absorbe su brillo. Tienes una Esfera de Luz.";} else message="Lina: El Faro volverá a brillar gracias a vuestra aventura.";} else message="No hay nada que investigar aquí.";invalidate(); }
    private void startBattle(){battle=true;wildHp=55+random.nextInt(40);String[] names={"Brizna","Nubal","Coralín","Voltín"};wild=names[random.nextInt(names.length)];battleMessage="¡Un " + wild + " salvaje aparece!";invalidate();}
    private void handleBattle(float x,float y){float top=getHeight()-dp(86);if(y<top)return;boolean right=x>getWidth()/2f;if(y<getHeight()-dp(43)){if(right)attack(25,"Niebla lunar");else attack(18,"Chispa solar");}else{if(right){battle=false;message="Alba vuelve a la pradera.";}else capture();}invalidate();}
    private void attack(int power,String name){int hit=power+random.nextInt(11);wildHp=Math.max(0,wildHp-hit);if(wildHp==0){battle=false;message=wild+" se esconde entre las hojas. Ganaste experiencia.";return;}int hitBack=7+random.nextInt(13);hp=Math.max(0,hp-hitBack);battleMessage=name+" causa "+hit+". ¡"+wild+" responde con "+hitBack+"!";if(hp==0){battle=false;hp=55;message="Alba se recuperó en el campamento.";}}
    private void capture(){int chance=20+(100-wildHp)/2;if(random.nextInt(100)<chance){captures++;battle=false;message="¡Captura conseguida! "+wild+" se une a tu equipo.";}else battleMessage="La esfera se abre... ¡"+wild+" sigue luchando!";}
    private void drawWrapped(Canvas c,String s,float x,float y,float width,float size,int color){p.setTextSize(size);p.setColor(color);p.setTextAlign(Paint.Align.LEFT);String[] words=s.split(" ");String line="";int row=0;for(String word:words){if(word.equals("\n")){row++;line="";continue;}String next=line.length()==0?word:line+" "+word;if(p.measureText(next)>width){c.drawText(line,x,y+row*size*1.35f,p);row++;line=word;}else line=next;}if(line.length()>0)c.drawText(line,x,y+row*size*1.35f,p);}
    private void text(Canvas c,String s,float x,float y,float size,int color){text(c,s,x,y,size,color,Paint.Align.LEFT);} private void text(Canvas c,String s,float x,float y,float size,int color,Paint.Align align){p.setTextSize(size);p.setColor(color);p.setTextAlign(align);c.drawText(s,x,y,p);}
    private int dp(int n){return (int)(n*getResources().getDisplayMetrics().density+.5f);}
}
