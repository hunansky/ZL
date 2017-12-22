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

//#include <Wire.h>
#include "MAX30100.h"
#include <stdio.h>
#include "nrf_delay.h"
#include "maxiic.h"
#include <stdbool.h>
uint16_t rawIRValue;
uint16_t rawRedValue;

void max_begin()//void MAX30100::begin()
{
	  max_iic_init();
    setMode(DEFAULT_MODE);
	  printf("set mode ok...\r\n");
    setLedsPulseWidth(DEFAULT_PULSE_WIDTH);
	  printf("default pulse ok...\r\n");
    setSamplingRate(DEFAULT_SAMPLING_RATE);
	  printf("sampling rate ok...\r\n");
    setLedsCurrent(DEFAULT_IR_LED_CURRENT, DEFAULT_RED_LED_CURRENT);
	  printf("led ok...\r\n");
	  setHighresModeEnabled(1);
	  printf("enable ok...\r\n");
}

void setMode(Mode mode)
{
	   Write_One_Byte(MAX30100_REG_MODE_CONFIGURATION, mode);
}

void setLedsPulseWidth(LEDPulseWidth ledPulseWidth)
{
	  uint8_t previous;  previous=Read_One_Byte(MAX30100_REG_SPO2_CONFIGURATION);
    Write_One_Byte(MAX30100_REG_SPO2_CONFIGURATION, (previous & 0xfc) | ledPulseWidth);
}

void setSamplingRate(SamplingRate samplingRate)
{
			uint8_t previous; previous=Read_One_Byte(MAX30100_REG_SPO2_CONFIGURATION);
	    Write_One_Byte(MAX30100_REG_SPO2_CONFIGURATION, (previous & 0xe3) | (samplingRate << 2));

}

void setLedsCurrent(LEDCurrent irLedCurrent, LEDCurrent redLedCurrent)
{
			Write_One_Byte(MAX30100_REG_LED_CONFIGURATION, redLedCurrent << 4 | irLedCurrent);
}

void setHighresModeEnabled(uint8_t enabled)
{
	   uint8_t previous; 
	   previous=Read_One_Byte(MAX30100_REG_SPO2_CONFIGURATION);
	   if (enabled) {
        Write_One_Byte(MAX30100_REG_SPO2_CONFIGURATION, previous | MAX30100_SPC_SPO2_HI_RES_EN);
    } else {
        Write_One_Byte(MAX30100_REG_SPO2_CONFIGURATION, previous & ~MAX30100_SPC_SPO2_HI_RES_EN);
    }
}

float read_temp(void)
{
  uint8_t buf[2];
	float temp;
	Buff_Read(0x16,buf,2);
	temp = (float)buf[0] + (float)((buf[1]&0x0f)*0.0625f);
	return temp;
}

void update()
{
    readFifoData();
}

void readFifoData()
{
    uint8_t buffer[4];

    Buff_Read(MAX30100_REG_FIFO_DATA,buffer, 4);

    // Warning: the values are always left-aligned
    rawIRValue = (buffer[0] << 8) | buffer[1];    //get IR light data
    rawRedValue = (buffer[2] << 8) | buffer[3];   //get red light data
}

uint8_t Write_One_Byte(uint8_t addr,uint8_t data)
{
    MAX_Write_Byte(0x57,addr,data);
    return 0;
}
uint8_t Read_One_Byte(uint8_t addr)
{
    uint8_t res;
		res=MAX_Read_Byte(0x57,addr);
		return res; 
}

uint8_t Buff_Read(uint8_t address,uint8_t *buf, uint8_t len)
{
	uint8_t i;
	for(i=0;i<len;i++)
	{
	  buf[i]=MAX_Read_Byte(0x57,address);
		address++;
	}
	return 0;	
}

