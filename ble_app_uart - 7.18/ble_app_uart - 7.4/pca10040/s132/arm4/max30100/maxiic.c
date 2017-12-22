#include "maxiic.h"
#include "nrf_gpio.h"
#include "nrf_delay.h"
#include "stdio.h"

#if defined BOARD_3_1
#define  MAX_SCL_PIN 15
#define  MAX_SDA_PIN 14
#elif defined BOARD_3_0
#define  MAX_SCL_PIN 30
#define  MAX_SDA_PIN 29
#endif
#define MAX_SDA_OUT() {nrf_gpio_cfg_output(MAX_SDA_PIN);}
#define MAX_SDA_IN()  {nrf_gpio_cfg_input(MAX_SDA_PIN,NRF_GPIO_PIN_NOPULL);}
#define MAX_READ_SDA()  nrf_gpio_pin_read(MAX_SDA_PIN)

#define MAX_SDA_H {nrf_gpio_pin_set(MAX_SDA_PIN);}
#define MAX_SDA_L {nrf_gpio_pin_clear(MAX_SDA_PIN);}
#define MAX_SCL_H {nrf_gpio_pin_set(MAX_SCL_PIN);}
#define MAX_SCL_L {nrf_gpio_pin_clear(MAX_SCL_PIN);}

/***************************************************************************************/

void max_iic_init(void)
{	   
	nrf_gpio_cfg_output(MAX_SCL_PIN);
	nrf_gpio_cfg_output(MAX_SDA_PIN);
}

void MAX_IIC_DELAY(void)
{
   nrf_delay_us(1);
}


//产生IIC起始信号
void MAX_IIC_Start(void)
{
	MAX_SDA_OUT();     //sda线输出
	MAX_SDA_H;	  	  
	MAX_SCL_H;
 	MAX_SDA_L;//START:when CLK is high,DATA change form high to low 
	MAX_IIC_DELAY();
	MAX_SCL_L;//钳住I2C总线，准备发送或接收数据 
}	  
//产生IIC停止信号
void MAX_IIC_Stop(void)
{
	MAX_SDA_OUT();//sda线输出
	MAX_SCL_L;
	MAX_SDA_L;//STOP:when CLK is high DATA change form low to high
 	MAX_IIC_DELAY();
	MAX_SCL_H;  
	MAX_SDA_H;//发送I2C总线结束信号
	MAX_IIC_DELAY();							   	
}
//等待应答信号到来
//返回值：1，接收应答失败
//        0，接收应答成功
uint8_t MAX_IIC_Wait_Ack(void)
{
	uint8_t ucErrTime=0;
	MAX_SDA_IN();      //SDA设置为输入  
	MAX_SDA_H;MAX_IIC_DELAY();	   
	MAX_SCL_H;MAX_IIC_DELAY();	 
	while(MAX_READ_SDA())
	{
		ucErrTime++;
		if(ucErrTime>250)
		{
			MAX_IIC_Stop();
			return 1;
		}
	}
	MAX_SCL_L;//时钟输出0 	   
	return 0;  
} 
//产生ACK应答
void MAX_IIC_Ack(void)
{
	MAX_SCL_L;
	MAX_SDA_OUT();
	MAX_SDA_L;
	MAX_IIC_DELAY();
	MAX_SCL_H;
	MAX_IIC_DELAY();
	MAX_SCL_L;
}
//不产生ACK应答		    
void MAX_IIC_NAck(void)
{
	MAX_SCL_L;
	MAX_SDA_OUT();
	MAX_SDA_H;
	MAX_IIC_DELAY();
	MAX_SCL_H;
	MAX_IIC_DELAY();
	MAX_SCL_L;
}					 				     
//IIC发送一个字节
//返回从机有无应答
//1，有应答
//0，无应答			  
void MAX_IIC_Send_Byte(uint8_t txd)
{                        
    uint8_t t;   
	MAX_SDA_OUT(); 	    
    MAX_SCL_L;//拉低时钟开始数据传输
    for(t=0;t<8;t++)
    {              
        if((txd&0x80)>>7)
				{
				  MAX_SDA_H;
				}
				else MAX_SDA_L;
        txd<<=1; 	  
		MAX_SCL_H;
		MAX_IIC_DELAY(); 
		MAX_SCL_L;	
		MAX_IIC_DELAY();
    }	 
} 	    
//读1个字节，ack=1时，发送ACK，ack=0，发送nACK   
uint8_t MAX_IIC_Read_Byte(unsigned char ack)
{
	unsigned char i,receive=0;
	MAX_SDA_IN();//SDA设置为输入
    for(i=0;i<8;i++ )
	{
        MAX_SCL_L; 
        MAX_IIC_DELAY();
		    MAX_SCL_H;
        receive<<=1;
        if(MAX_READ_SDA())receive++;   
		MAX_IIC_DELAY(); 
  }					 
    if (!ack)
        MAX_IIC_NAck();//发送nACK
    else
        MAX_IIC_Ack(); //发送ACK   
    return receive;
}



//IIC写一个字节 
//reg:寄存器地址
//data:数据
//返回值:0,正常
//    其他,错误代码
uint8_t MAX_Write_Byte(uint8_t dev_addr,uint8_t reg_addr,uint8_t data) 				 
{ 
  MAX_IIC_Start(); 
	MAX_IIC_Send_Byte((dev_addr<<1)|0);//发送器件地址+写命令	
	if(MAX_IIC_Wait_Ack())	//等待应答
	{
		MAX_IIC_Stop();		 
		return 1;		
	}
    MAX_IIC_Send_Byte(reg_addr);	//写寄存器地址
    MAX_IIC_Wait_Ack();		//等待应答 
	MAX_IIC_Send_Byte(data);//发送数据
	if(MAX_IIC_Wait_Ack())	//等待ACK
	{
		MAX_IIC_Stop();	
		return 1;		 
	}		 
    MAX_IIC_Stop();	 
	return 0;
}
//IIC读一个字节 
//reg:寄存器地址 
//返回值:读到的数据
uint8_t MAX_Read_Byte(uint8_t dev_addr,uint8_t reg_addr)
{
	uint8_t res;
  MAX_IIC_Start(); 
	MAX_IIC_Send_Byte((dev_addr<<1)|0);//发送器件地址+写命令	
	MAX_IIC_Wait_Ack();		//等待应答 
  MAX_IIC_Send_Byte(reg_addr);	//写寄存器地址
  MAX_IIC_Wait_Ack();		//等待应答
  MAX_IIC_Start();
	MAX_IIC_Send_Byte((dev_addr<<1)|1);//发送器件地址+读命令	
  MAX_IIC_Wait_Ack();		//等待应答 
	res=MAX_IIC_Read_Byte(0);//读取数据,发送nACK 
  MAX_IIC_Stop();			//产生一个停止条件 
	return res;		
}






