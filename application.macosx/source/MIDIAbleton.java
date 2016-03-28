import processing.core.*; 
import processing.data.*; 
import processing.event.*; 
import processing.opengl.*; 

import themidibus.*; 

import java.util.HashMap; 
import java.util.ArrayList; 
import java.io.File; 
import java.io.BufferedReader; 
import java.io.PrintWriter; 
import java.io.InputStream; 
import java.io.OutputStream; 
import java.io.IOException; 

public class MIDIAbleton extends PApplet {

         //Import MIDI library

final String MIDI_BUS_CONFIGURATION_GUITAR_WING         = "Livid Guitar Wing";
final String MIDI_BUS_CONFIGURATION_ABLETON_IN          = "Bus 2";
final String MIDI_BUS_CONFIGURATION_ABLETON_OUT         = "Bus 3";
final String MIDI_BUS_CONFIGURATION_AUDIO_INTERFACE_OUT = "Fast Track Ultra 8R";

final int PROGRAM_CHANGE_STATUS_BYTE = 0xC0;

//Channel which Ableton will listen to in order to set the voice plugins - note that this channel is also used to set some guitar FX
final int CHANNEL_ABLETON_VOICEFX    = 5;
final int CHANNEL_AMP                = 3;        //For reference, the VoiceLive listens on Channel 2


//Pitches for messages coming from the Guitar Wing
final int PITCH_WING_BIG_ROUND_BUTTON_1 = 36;
final int PITCH_WING_BIG_ROUND_BUTTON_2 = 37;
final int PITCH_WING_BIG_ROUND_BUTTON_3 = 38;
final int PITCH_WING_BIG_ROUND_BUTTON_4 = 39;
final int PITCH_WING_ARROW_NEXT         = 40;
final int PITCH_WING_ARROW_PREVIOUS     = 41;
final int PITCH_WING_SMALL_RECTANGLE_1  = 42;
final int PITCH_WING_SMALL_RECTANGLE_2  = 43;
final int PITCH_WING_SMALL_RECTANGLE_3  = 44;
final int PITCH_WING_SMALL_RECTANGLE_4  = 45;
final int PITCH_WING_SMALL_SWITCH_1     = 46;
final int PITCH_WING_SMALL_SWITCH_2     = 47;
final int PITCH_WING_SMALL_SWITCH_3     = 48;
final int PITCH_WING_SMALL_SWITCH_4     = 49;
final int PITCH_TOGGLE                  = 4;
final int CC_SMALL_FADER_1              = 1;
final int CC_SMALL_FADER_2              = 2;
final int CC_BIG_FADER                  = 3;

final int PITCH_AMP_FIRST_PC_NUMBER     = 120;

//Instanciate MIDI control object
MidiBus midiBus_GuitarWing;
MidiBus midiBus_Ableton;
MidiBus midiBus_AudioInterface;

public void setup() {
  //Initialize MIDI Control object
  midiInit();
  size(1,1);
}

public void draw() {
  //Loop, no need to do anything
}

public void midiInit() {
  
  MidiBus.list(); 
  //Arguments to create the MidiBus : Parent Class, IN device, OUT device
  midiBus_GuitarWing = new MidiBus(this, MIDI_BUS_CONFIGURATION_GUITAR_WING, MIDI_BUS_CONFIGURATION_ABLETON_OUT);      //MIDI coming from the Wing
  midiBus_Ableton = new MidiBus(this, MIDI_BUS_CONFIGURATION_ABLETON_IN, MIDI_BUS_CONFIGURATION_ABLETON_OUT);          //MIDI coming from Ableton, routed back to Ableton
  midiBus_AudioInterface = new MidiBus(this, MIDI_BUS_CONFIGURATION_ABLETON_IN, MIDI_BUS_CONFIGURATION_AUDIO_INTERFACE_OUT);   //MIDI coming from Ableton, sent to external equipments
}


/////////////////////////////////////////////////
//////////////       NOTE ON       //////////////
/////////////////////////////////////////////////

public void noteOn(int channel, int pitch, int velocity, long timestamp, String bus_name) {
  // Receive a noteOn
  if (bus_name == midiBus_Ableton.getBusName()) {
    if (pitch < PITCH_AMP_FIRST_PC_NUMBER) {      // Commands to send back to Ableton in order to control Ableton FX 
      // Convert the incoming note on in a CC On message : same "pitch" (channel number), 127 intensity, voice fx channel
      midiBus_Ableton.sendControllerChange(CHANNEL_ABLETON_VOICEFX, pitch, 127);
    }
    else {                                        // Commands to send directly on the audio interface's MIDI out - for example, Program Changes for the Marshall amplifier  
      midiBus_AudioInterface.sendMessage(PROGRAM_CHANGE_STATUS_BYTE, CHANNEL_AMP, pitch + 1 - PITCH_AMP_FIRST_PC_NUMBER, 0);
    }
  }
  
  //Guitar Wing
  else if (bus_name == midiBus_GuitarWing.getBusName()) {
    switch (pitch) {
      case PITCH_WING_BIG_ROUND_BUTTON_1: sendMidiOut_On_GuitarWing_BigRoundButton1();break;
      case PITCH_WING_BIG_ROUND_BUTTON_2: sendMidiOut_On_GuitarWing_BigRoundButton2();break;
      case PITCH_WING_BIG_ROUND_BUTTON_3: sendMidiOut_On_GuitarWing_BigRoundButton3();break;
      case PITCH_WING_BIG_ROUND_BUTTON_4: sendMidiOut_On_GuitarWing_BigRoundButton4();break;
      case PITCH_WING_SMALL_RECTANGLE_1:  sendMidiOut_On_GuitarWing_SmallRectangle1();break;
      case PITCH_WING_SMALL_RECTANGLE_2:  sendMidiOut_On_GuitarWing_SmallRectangle2();break;
      case PITCH_WING_SMALL_RECTANGLE_3:  sendMidiOut_On_GuitarWing_SmallRectangle3();break;
      case PITCH_WING_SMALL_RECTANGLE_4:  sendMidiOut_On_GuitarWing_SmallRectangle4();break;
      case PITCH_TOGGLE:                  sendMidiOut_On_GuitarWing_Toggle();break;
      default: break;
    }
  }
}

//////////////////////////////////////////////////
//////////////       NOTE OFF       //////////////
//////////////////////////////////////////////////


public void noteOff(int channel, int pitch, int velocity, long timestamp, String bus_name) {
  // Receive a noteOff
  
  //Ableton Voice FX
  if (bus_name == midiBus_Ableton.getBusName()) {
    // Convert the incoming note on in a CC Off message : same "pitch" (channel number), 0 intensity, voice fx channel 
    midiBus_Ableton.sendControllerChange(CHANNEL_ABLETON_VOICEFX, pitch, 0);
  }
  
  //Guitar Wing
  else if (bus_name == midiBus_GuitarWing.getBusName()) {
    switch (pitch) {
      case PITCH_WING_BIG_ROUND_BUTTON_1: sendMidiOut_Off_GuitarWing_BigRoundButton1();break;
      case PITCH_WING_BIG_ROUND_BUTTON_2: sendMidiOut_Off_GuitarWing_BigRoundButton2();break;
      case PITCH_WING_BIG_ROUND_BUTTON_3: sendMidiOut_Off_GuitarWing_BigRoundButton3();break;
      case PITCH_WING_BIG_ROUND_BUTTON_4: sendMidiOut_Off_GuitarWing_BigRoundButton4();break;
      case PITCH_TOGGLE:                  sendMidiOut_Off_GuitarWing_Toggle();break;
      default: break;
    }
  }
}

/////////////////////////////////////////////////
//////////////  CONTROLLER CHANGE  //////////////
/////////////////////////////////////////////////


public void controllerChange(int channel, int number, int value, long timestamp, String bus_name) {
  // Receive a controllerChange
  
  //Guitar Wing
  if (bus_name == midiBus_GuitarWing.getBusName()) {
    switch (number) {
      case CC_BIG_FADER:                  sendMidiOut_CC_BigFader(value);break;
      default: break;
    }
  }
}


////////////////////////////////////

public void sendMidiOut_On_GuitarWing_BigRoundButton1() {
  midiBus_GuitarWing.sendControllerChange(4, 13, 127);
}

public void sendMidiOut_On_GuitarWing_BigRoundButton2() {
  //midiBus_GuitarWing.sendControllerChange(4, 14, 127);
  midiBus_GuitarWing.sendControllerChange(0, 16, 127);
  midiBus_GuitarWing.sendNoteOn(0, 44, 127);
}

public void sendMidiOut_On_GuitarWing_BigRoundButton3() {
  midiBus_GuitarWing.sendControllerChange(0, 16, 110);
  midiBus_GuitarWing.sendNoteOn(0, 44, 127);
}

public void sendMidiOut_On_GuitarWing_BigRoundButton4() {
  midiBus_GuitarWing.sendControllerChange(0, 52, 76);
}

public void sendMidiOut_On_GuitarWing_SmallRectangle1() {
  midiBus_GuitarWing.sendControllerChange(0, 16, 0);
}

public void sendMidiOut_On_GuitarWing_SmallRectangle2() {
  midiBus_GuitarWing.sendControllerChange(0, 16, 45);
  
}

public void sendMidiOut_On_GuitarWing_SmallRectangle3() {
  midiBus_GuitarWing.sendControllerChange(0, 16, 64);
}

public void sendMidiOut_On_GuitarWing_SmallRectangle4() {
  midiBus_GuitarWing.sendControllerChange(0, 16, 110);
}

public void sendMidiOut_On_GuitarWing_Toggle() {
  
}

////////////////////////////////////

public void sendMidiOut_Off_GuitarWing_BigRoundButton1() {
  midiBus_GuitarWing.sendControllerChange(4, 13, 0);
}

public void sendMidiOut_Off_GuitarWing_BigRoundButton2() {
  //midiBus_GuitarWing.sendControllerChange(4, 14, 0);
  midiBus_GuitarWing.sendNoteOn(0, 44, 127);
}

public void sendMidiOut_Off_GuitarWing_BigRoundButton3() {
  midiBus_GuitarWing.sendNoteOn(0, 44, 127);
}

public void sendMidiOut_Off_GuitarWing_BigRoundButton4() {
  midiBus_GuitarWing.sendControllerChange(0, 52, 0);
}

public void sendMidiOut_Off_GuitarWing_SmallRectangle1() {

}

public void sendMidiOut_Off_GuitarWing_SmallRectangle2() {
  
}

public void sendMidiOut_Off_GuitarWing_SmallRectangle3() {

}

public void sendMidiOut_Off_GuitarWing_SmallRectangle4() {

}

public void sendMidiOut_Off_GuitarWing_Toggle() {

}

////////////////////////////////////////////////////////////////////////////

public void sendMidiOut_CC_BigFader(int value) {
  midiBus_GuitarWing.sendControllerChange(0, 56, PApplet.parseInt(map(value,0,127,0,100)));
}

  static public void main(String[] passedArgs) {
    String[] appletArgs = new String[] { "MIDIAbleton" };
    if (passedArgs != null) {
      PApplet.main(concat(appletArgs, passedArgs));
    } else {
      PApplet.main(appletArgs);
    }
  }
}
