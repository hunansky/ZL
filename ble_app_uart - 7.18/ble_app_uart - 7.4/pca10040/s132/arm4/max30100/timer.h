#ifndef __TIMER_H
#define __TIMER_H
#include "nrf52.h"
extern unsigned long RunTime;

void timer_init(uint32_t arr,uint16_t psc);
unsigned long millis(void);
#endif
