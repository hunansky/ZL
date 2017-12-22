#include "timer.h"
#include "nrf_gpio.h"
unsigned long RunTime=0;

void timer_init(uint32_t arr,uint16_t psc)
{
	//NRF_TIMER2 定义在 nrf51.h 中，该指针指向 TIMER2 中的寄存器组
	NRF_TIMER2->PRESCALER = psc; //2^4 16 分频得到 1M timer 时钟
	NRF_TIMER2->MODE = 0; //timer 模式
	NRF_TIMER2->BITMODE = 3; // 设置 32bit
	NRF_TIMER2->CC[0] = arr; //一个 tick 是 1us， 1000 代表 1ms
	NRF_TIMER2->INTENSET = 1<<16;//设置 compare[0]事件产生时触发中断
	//该设置使 timer 模块中的 conter 计数到 cc[0]值时会自动清零，以带到重
	//新计数的目的
	NRF_TIMER2->SHORTS = 1;
	//启动 timer 模块
	NRF_TIMER2->TASKS_START = 1;
	//开启 MCU 的 TIMER2 中断
	NVIC_SetPriority(TIMER2_IRQn, 3);
	NVIC_ClearPendingIRQ(TIMER2_IRQn);
	NVIC_EnableIRQ(TIMER2_IRQn);				 
}



void TIMER2_IRQHandler(void)   //TIM3中断
{
	if(NRF_TIMER2->EVENTS_COMPARE[0] == 1)
	{
     NRF_TIMER2->EVENTS_COMPARE[0] = 0; //清除事件，不然会导致一
     //直产生中断
		 RunTime++;
  }
}

unsigned long millis()
{
  if(RunTime>100000) RunTime=0;
  return RunTime;
}









