/*
Arduino-MAX30100 oximetry / heart rate integrated sensor library
Copyright (C) 2016  OXullo Intersecans <x@brainrapers.org>

This program is free software: you can redistribute it and/or modify
it under the terms of the GNU General Public License as published by
the Free Software Foundation, either version 3 of the License, or
(at your option) any later version.

This program is distributed in the hope that it will be useful,
but WITHOUT ANY WARRANTY; without even the implied warranty of
MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
GNU General Public License for more details.

You should have received a copy of the GNU General Public License
along with this program.  If not, see <http://www.gnu.org/licenses/>.
*/

//#include <Arduino.h>

#include "MAX30100_PulseOximeter.h"
#include "timer.h"
#include "stdlib.h"
#include <stdio.h>

float onBeatDetected;
PulseOximeterState state;
PulseOximeterDebuggingMode debuggingMode;
uint32_t tsFirstBeatDetected;   //第一次起跳时系统时间
uint32_t tsLastBeatDetected;    //最后一次起跳时的系统时间
uint32_t tsLastSample;          //最后一次采样
uint32_t tsLastBiasCheck;
uint32_t tsLastCurrentAdjustment;
uint8_t redLedPower;


DCRemover irDCRemover;
DCRemover redDCRemover;

float onBeatDetected;
void PulseOximeter(){ 
    state=PULSEOXIMETER_STATE_INIT;
    tsFirstBeatDetected=0;
    tsLastBeatDetected=0;
    tsLastSample=0;
    tsLastBiasCheck=0;
    tsLastCurrentAdjustment=0;
    redLedPower=((uint8_t)RED_LED_CURRENT_START);
    onBeatDetected=NULL;
}

void begin(){
    debuggingMode = PULSEOXIMETER_DEBUGGINGMODE_NONE;

    max_begin();//hrm.begin();
	  printf("max_begin ok...\r\n");
    setMode(MAX30100_MODE_SPO2_HR);
    setLedsCurrent(IR_LED_CURRENT, RED_LED_CURRENT_START);

			DCRemover1(DC_REMOVER_ALPHA,&irDCRemover.alpha,&irDCRemover.dcw);
			DCRemover1(DC_REMOVER_ALPHA,&redDCRemover.alpha,&redDCRemover.dcw);
    state = PULSEOXIMETER_STATE_IDLE;
}

void POupdate(){
    checkSample();
    checkCurrentBias();
}

float getHeartRate(){
    return getRate();
}

uint8_t POgetSpO2(){
    return getSpO2();
}

uint8_t getRedLedCurrentBias(){
    return redLedPower;
}

void setOnBeatDetectedCallback(float *cb){
    onBeatDetected = *cb;
}

void checkSample(){
	      uint8_t beatDetected;
	      float filteredPulseValue;
	      float irACValue;
	      float redACValue;
    if (millis() - tsLastSample > 1.0 / SAMPLING_FREQUENCY * 1000.0) {
        tsLastSample = millis();
			  update();
				irACValue = step(rawIRValue,&irDCRemover.alpha,&irDCRemover.dcw);
			  redACValue = step(rawRedValue,&redDCRemover.alpha,&redDCRemover.dcw);
        // The signal fed to the beat detector is mirrored since the cleanest monotonic spike is below zero
			    filteredPulseValue = FBstep(-irACValue);
			  beatDetected = addSample(filteredPulseValue);
        if (getRate() > 0) {
            state = PULSEOXIMETER_STATE_DETECTING;
            SPO2update(irACValue, redACValue, beatDetected);
        } else if (state == PULSEOXIMETER_STATE_DETECTING) {
            state = PULSEOXIMETER_STATE_IDLE;
					    reset();
        }

        switch (debuggingMode) {
            case PULSEOXIMETER_DEBUGGINGMODE_RAW_VALUES:
						    printf("R:");
                printf("%d",rawIRValue);
                printf(",");
                printf("%d\r\n",rawRedValue);
                break;

            case PULSEOXIMETER_DEBUGGINGMODE_AC_VALUES:
						    printf("R:");
                printf("%lf",irACValue);
                printf(",");
                printf("%lf\r\n",redACValue);
                break;

            case PULSEOXIMETER_DEBUGGINGMODE_PULSEDETECT:
						    printf("R:");
                printf("%lf",filteredPulseValue);
                printf(",");
                printf("%lf\r\n",getCurrentThreshold());
                break;

            default:
                break;
        }

        if (beatDetected && onBeatDetected) {
            onBeatDetected1();
        }
    }
}

void onBeatDetected1(void)
{
  
}
extern uint8_t  finger_leave;
void checkCurrentBias(){
    // Follower that adjusts the red led current in order to have comparable DC baselines between
    // red and IR leds. The numbers are really magic: the less possible to avoid oscillations
    if (millis() - tsLastBiasCheck > CURRENT_ADJUSTMENT_PERIOD_MS) {
			  uint8_t changed = 0;
        if (getDCW(&irDCRemover.dcw)-getDCW(&redDCRemover.dcw)> 70000 && redLedPower < MAX30100_LED_CURR_50MA) {
            ++redLedPower;
					changed = 1;
        } else if (getDCW(&redDCRemover.dcw) - getDCW(&irDCRemover.dcw) > 70000 && redLedPower > 0) {
            --redLedPower;
					changed = 1;
        }
        if (changed) {
				   	setLedsCurrent(IR_LED_CURRENT, (LEDCurrent)redLedPower);
            tsLastCurrentAdjustment = millis();
        }
				if(redLedPower==MAX30100_LED_CURR_0MA){
						  finger_leave = 1;
				}else finger_leave = 0;
        tsLastBiasCheck = millis();
    }
}
