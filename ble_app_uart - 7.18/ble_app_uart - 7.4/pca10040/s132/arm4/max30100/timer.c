#include "timer.h"
#include "nrf_gpio.h"
unsigned long RunTime=0;

void timer_init(uint32_t arr,uint16_t psc)
{
	//NRF_TIMER2 ������ nrf51.h �У���ָ��ָ�� TIMER2 �еļĴ�����
	NRF_TIMER2->PRESCALER = psc; //2^4 16 ��Ƶ�õ� 1M timer ʱ��
	NRF_TIMER2->MODE = 0; //timer ģʽ
	NRF_TIMER2->BITMODE = 3; // ���� 32bit
	NRF_TIMER2->CC[0] = arr; //һ�� tick �� 1us�� 1000 ���� 1ms
	NRF_TIMER2->INTENSET = 1<<16;//���� compare[0]�¼�����ʱ�����ж�
	//������ʹ timer ģ���е� conter ������ cc[0]ֵʱ���Զ����㣬�Դ�����
	//�¼�����Ŀ��
	NRF_TIMER2->SHORTS = 1;
	//���� timer ģ��
	NRF_TIMER2->TASKS_START = 1;
	//���� MCU �� TIMER2 �ж�
	NVIC_SetPriority(TIMER2_IRQn, 3);
	NVIC_ClearPendingIRQ(TIMER2_IRQn);
	NVIC_EnableIRQ(TIMER2_IRQn);				 
}



void TIMER2_IRQHandler(void)   //TIM3�ж�
{
	if(NRF_TIMER2->EVENTS_COMPARE[0] == 1)
	{
     NRF_TIMER2->EVENTS_COMPARE[0] = 0; //����¼�����Ȼ�ᵼ��һ
     //ֱ�����ж�
		 RunTime++;
  }
}

unsigned long millis()
{
  if(RunTime>100000) RunTime=0;
  return RunTime;
}









