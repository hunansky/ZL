#ifndef __MPUIIC_H
#define __MPUIIC_H
#include "nrf52.h"
	   		   

//IIC���в�������
void MAX_IIC_Delay(void);				//MPU IIC��ʱ����			 
void MAX_IIC_Start(void);				//����IIC��ʼ�ź�
void MAX_IIC_Stop(void);	  			//����IICֹͣ�ź�
void MAX_IIC_Send_Byte(uint8_t txd);			//IIC����һ���ֽ�
uint8_t MAX_IIC_Read_Byte(unsigned char ack);//IIC��ȡһ���ֽ�
uint8_t MAX_IIC_Wait_Ack(void); 				//IIC�ȴ�ACK�ź�
void MAX_IIC_Ack(void);					//IIC����ACK�ź�
void MAX_IIC_NAck(void);				//IIC������ACK�ź� 
void max_iic_init(void);
uint8_t MAX_Write_Byte(uint8_t dev_addr,uint8_t reg_addr,uint8_t data);
uint8_t MAX_Read_Byte(uint8_t dev_addr,uint8_t reg_addr);
#endif
















