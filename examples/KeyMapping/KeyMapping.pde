import queasycam.*;

QueasyCam cam;

void setup(){
  size(400, 400, P3D);
  cam = new QueasyCam(this);
  cam.key_forward = 'z';
  cam.key_left = 'q';
  cam.key_backward = 's';
  cam.key_right = 'd';
  cam.key_up = 'a';
  cam.key_down = 'e';
}

void draw(){
  background(0);
  box(200);
}