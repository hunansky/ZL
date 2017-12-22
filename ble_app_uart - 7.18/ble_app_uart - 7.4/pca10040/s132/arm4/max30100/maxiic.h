#ifndef __MPUIIC_H
#define __MPUIIC_H
#include "nrf52.h"
	   		   

//IIC所有操作函数
void MAX_IIC_Delay(void);				//MPU IIC延时函数			 
void MAX_IIC_Start(void);				//发送IIC开始信号
void MAX_IIC_Stop(void);	  			//发送IIC停止信号
void MAX_IIC_Send_Byte(uint8_t txd);			//IIC发送一个字节
uint8_t MAX_IIC_Read_Byte(unsigned char ack);//IIC读取一个字节
uint8_t MAX_IIC_Wait_Ack(void); 				//IIC等待ACK信号
void MAX_IIC_Ack(void);					//IIC发送ACK信号
void MAX_IIC_NAck(void);				//IIC不发送ACK信号 
void max_iic_init(void);
uint8_t MAX_Write_Byte(uint8_t dev_addr,uint8_t reg_addr,uint8_t data);
uint8_t MAX_Read_Byte(uint8_t dev_addr,uint8_t reg_addr);
#endif
















