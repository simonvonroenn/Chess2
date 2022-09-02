import processing.core.PApplet;

public class Chess2 extends PApplet {

    public void settings(){
        size(500, 500);
    }

    public void draw(){
        ellipse(mouseX, mouseY, 50, 50);
    }

    public void mousePressed(){
        background(64);
    }

    public static void main(String[] args){
        String[] processingArgs = {"MySketch"};
        Chess2 chess2 = new Chess2();
        PApplet.runSketch(processingArgs, chess2);
    }
}
