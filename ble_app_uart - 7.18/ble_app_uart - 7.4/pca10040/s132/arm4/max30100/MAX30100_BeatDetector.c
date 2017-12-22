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


/*检测心率节拍程序*/
#include "MAX30100_BeatDetector.h"
#include "timer.h"
#define min(a,b)  (a>b)?b:a

BeatDetectorState Beatstate;

float threshold;    //检测阈值
float beatPeriod;   //两次起跳间隔  时间毫秒
float lastMaxValue; 
uint32_t tsLastBeat;//最后一次起跳时的系统时间

void BeatDetector(){
    Beatstate=BEATDETECTOR_STATE_INIT;
    threshold=BEATDETECTOR_MIN_THRESHOLD;
    beatPeriod=0;
    lastMaxValue=0;
    tsLastBeat=0;
}

uint8_t addSample(float sample){
    return checkForBeat(sample);
}

float getRate(){
    if (beatPeriod != 0) {
        return 1 / beatPeriod * 1000 * 60;
    } else {
        return 0;
    }
}

float getCurrentThreshold(){//获取电流阈值
    return threshold;
}

uint8_t checkForBeat(float sample){
    uint8_t beatDetected = 0;
	  float delta;
    switch (Beatstate) {
        case BEATDETECTOR_STATE_INIT:
            if (millis() > BEATDETECTOR_INIT_HOLDOFF) {
							Beatstate = BEATDETECTOR_STATE_WAITING;
            }
            break;

        case BEATDETECTOR_STATE_WAITING:
            if (sample > threshold) {
                threshold = min(sample, BEATDETECTOR_MAX_THRESHOLD);
									Beatstate = BEATDETECTOR_STATE_FOLLOWING_SLOPE;
						}
            // Tracking lost, resetting
            if (millis() - tsLastBeat > BEATDETECTOR_INVALID_READOUT_DELAY) {
                beatPeriod = 0;
                lastMaxValue = 0;
            }

            decreaseThreshold();
            break;

        case BEATDETECTOR_STATE_FOLLOWING_SLOPE:
            if (sample < threshold) {
									Beatstate = BEATDETECTOR_STATE_MAYBE_DETECTED;
						} else {
                threshold = min(sample, BEATDETECTOR_MAX_THRESHOLD);
            }
            break;

        case BEATDETECTOR_STATE_MAYBE_DETECTED:
            if (sample + BEATDETECTOR_STEP_RESILIENCY < threshold) {
							beatDetected = 1;
                lastMaxValue = sample;
              Beatstate = BEATDETECTOR_STATE_MASKING;

							 delta = millis() - tsLastBeat;
                if (delta) {
                    beatPeriod = BEATDETECTOR_BPFILTER_ALPHA * delta +
                            (1 - BEATDETECTOR_BPFILTER_ALPHA) * beatPeriod;
                }

                tsLastBeat = millis();
            } else {
									Beatstate = BEATDETECTOR_STATE_FOLLOWING_SLOPE;
						}
            break;

        case BEATDETECTOR_STATE_MASKING:
            if (millis() - tsLastBeat > BEATDETECTOR_MASKING_HOLDOFF) {
							   Beatstate = BEATDETECTOR_STATE_WAITING;
            }
            decreaseThreshold();
            break;
    }

    return beatDetected;
}

void decreaseThreshold(){//减小阈值
    // When a valid beat rate readout is present, target the
    if (lastMaxValue > 0 && beatPeriod > 0) {
        threshold -= lastMaxValue * (1 - BEATDETECTOR_THRESHOLD_FALLOFF_TARGET) /
                (beatPeriod / BEATDETECTOR_SAMPLES_PERIOD);
    } else {
        // Asymptotic decay
        threshold *= BEATDETECTOR_THRESHOLD_DECAY_FACTOR;
    }

    if (threshold < BEATDETECTOR_MIN_THRESHOLD) {
        threshold = BEATDETECTOR_MIN_THRESHOLD;
    }
}
