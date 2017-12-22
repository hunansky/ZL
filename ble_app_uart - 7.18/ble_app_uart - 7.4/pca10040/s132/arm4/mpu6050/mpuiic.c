#include "mpuiic.h"
#include "nrf_gpio.h"
#include "nrf_delay.h"
#include "stdio.h"

#if defined BOARD_3_1
#define MPU_SCL_PIN 3
#define MPU_SDA_PIN 2
#elif defined BOARD_3_0
#define MPU_SCL_PIN 13
#define MPU_SDA_PIN 12
#endif
#define MPU_SDA_OUT() {nrf_gpio_cfg_output(MPU_SDA_PIN);}
#define MPU_SDA_IN()  {nrf_gpio_cfg_input(MPU_SDA_PIN,NRF_GPIO_PIN_NOPULL);}
#define MPU_READ_SDA()  nrf_gpio_pin_read(MPU_SDA_PIN)

#define MPU_SDA_H {nrf_gpio_pin_set(MPU_SDA_PIN);}
#define MPU_SDA_L {nrf_gpio_pin_clear(MPU_SDA_PIN);}
#define MPU_SCL_H {nrf_gpio_pin_set(MPU_SCL_PIN);}
#define MPU_SCL_L {nrf_gpio_pin_clear(MPU_SCL_PIN);}

/***************************************************************************************/

void mpu_iic_init(void)
{	   
	nrf_gpio_cfg_output(MPU_SCL_PIN);
	nrf_gpio_cfg_output(MPU_SDA_PIN);
	
	nrf_gpio_pin_clear(16);//MPU_INT clear
}

void MPU_IIC_DELAY(void)
{
   nrf_delay_us(1);
}


//产生IIC起始信号
void MPU_IIC_Start(void)
{
	MPU_SDA_OUT();     //sda线输出
	MPU_SDA_H;	  	  
	MPU_SCL_H;
 	MPU_SDA_L;//START:when CLK is high,DATA change form high to low 
	MPU_IIC_DELAY();
	MPU_SCL_L;//钳住I2C总线，准备发送或接收数据 
}	  
//产生IIC停止信号
void MPU_IIC_Stop(void)
{
	MPU_SDA_OUT();//sda线输出
	MPU_SCL_L;
	MPU_SDA_L;//STOP:when CLK is high DATA change form low to high
 	MPU_IIC_DELAY();
	MPU_SCL_H;  
	MPU_SDA_H;//发送I2C总线结束信号
	MPU_IIC_DELAY();							   	
}
//等待应答信号到来
//返回值：1，接收应答失败
//        0，接收应答成功
uint8_t MPU_IIC_Wait_Ack(void)
{
	uint8_t ucErrTime=0;
	MPU_SDA_IN();      //SDA设置为输入  
	MPU_SDA_H;MPU_IIC_DELAY();	   
	MPU_SCL_H;MPU_IIC_DELAY();	 
	while(MPU_READ_SDA())
	{
		ucErrTime++;
		if(ucErrTime>250)
		{
			MPU_IIC_Stop();
			return 1;
		}
	}
	MPU_SCL_L;//时钟输出0 	   
	return 0;  
} 
//产生ACK应答
void MPU_IIC_Ack(void)
{
	MPU_SCL_L;
	MPU_SDA_OUT();
	MPU_SDA_L;
	MPU_IIC_DELAY();
	MPU_SCL_H;
	MPU_IIC_DELAY();
	MPU_SCL_L;
}
//不产生ACK应答		    
void MPU_IIC_NAck(void)
{
	MPU_SCL_L;
	MPU_SDA_OUT();
	MPU_SDA_H;
	MPU_IIC_DELAY();
	MPU_SCL_H;
	MPU_IIC_DELAY();
	MPU_SCL_L;
}					 				     
//IIC发送一个字节
//返回从机有无应答
//1，有应答
//0，无应答			  
void MPU_IIC_Send_Byte(uint8_t txd)
{                        
    uint8_t t;   
	MPU_SDA_OUT(); 	    
    MPU_SCL_L;//拉低时钟开始数据传输
    for(t=0;t<8;t++)
    {              
        if((txd&0x80)>>7)
				{
				  MPU_SDA_H;
				}
				else MPU_SDA_L;
        txd<<=1; 	  
		MPU_SCL_H;
		MPU_IIC_DELAY(); 
		MPU_SCL_L;	
		MPU_IIC_DELAY();
    }	 
} 	    
//读1个字节，ack=1时，发送ACK，ack=0，发送nACK   
uint8_t MPU_IIC_Read_Byte(unsigned char ack)
{
	unsigned char i,receive=0;
	MPU_SDA_IN();//SDA设置为输入
    for(i=0;i<8;i++ )
	{
        MPU_SCL_L; 
        MPU_IIC_DELAY();
		    MPU_SCL_H;
        receive<<=1;
        if(MPU_READ_SDA())receive++;   
		MPU_IIC_DELAY(); 
  }					 
    if (!ack)
        MPU_IIC_NAck();//发送nACK
    else
        MPU_IIC_Ack(); //发送ACK   
    return receive;
}



//IIC写一个字节 
//reg:寄存器地址
//data:数据
//返回值:0,正常
//    其他,错误代码
uint8_t Write_Byte(uint8_t dev_addr,uint8_t reg_addr,uint8_t data) 				 
{ 
  MPU_IIC_Start(); 
	MPU_IIC_Send_Byte((dev_addr<<1)|0);//发送器件地址+写命令	
	if(MPU_IIC_Wait_Ack())	//等待应答
	{
		MPU_IIC_Stop();		 
		return 1;		
	}
    MPU_IIC_Send_Byte(reg_addr);	//写寄存器地址
    MPU_IIC_Wait_Ack();		//等待应答 
	MPU_IIC_Send_Byte(data);//发送数据
	if(MPU_IIC_Wait_Ack())	//等待ACK
	{
		MPU_IIC_Stop();	
		return 1;		 
	}		 
    MPU_IIC_Stop();	 
	return 0;
}
//IIC读一个字节 
//reg:寄存器地址 
//返回值:读到的数据
uint8_t Read_Byte(uint8_t dev_addr,uint8_t reg_addr)
{
	uint8_t res;
  MPU_IIC_Start(); 
	MPU_IIC_Send_Byte((dev_addr<<1)|0);//发送器件地址+写命令	
	MPU_IIC_Wait_Ack();		//等待应答 
  MPU_IIC_Send_Byte(reg_addr);	//写寄存器地址
  MPU_IIC_Wait_Ack();		//等待应答
  MPU_IIC_Start();
	MPU_IIC_Send_Byte((dev_addr<<1)|1);//发送器件地址+读命令	
  MPU_IIC_Wait_Ack();		//等待应答 
	res=MPU_IIC_Read_Byte(0);//读取数据,发送nACK 
  MPU_IIC_Stop();			//产生一个停止条件 
	return res;		
}






