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


//����IIC��ʼ�ź�
void MAX_IIC_Start(void)
{
	MAX_SDA_OUT();     //sda�����
	MAX_SDA_H;	  	  
	MAX_SCL_H;
 	MAX_SDA_L;//START:when CLK is high,DATA change form high to low 
	MAX_IIC_DELAY();
	MAX_SCL_L;//ǯסI2C���ߣ�׼�����ͻ�������� 
}	  
//����IICֹͣ�ź�
void MAX_IIC_Stop(void)
{
	MAX_SDA_OUT();//sda�����
	MAX_SCL_L;
	MAX_SDA_L;//STOP:when CLK is high DATA change form low to high
 	MAX_IIC_DELAY();
	MAX_SCL_H;  
	MAX_SDA_H;//����I2C���߽����ź�
	MAX_IIC_DELAY();							   	
}
//�ȴ�Ӧ���źŵ���
//����ֵ��1������Ӧ��ʧ��
//        0������Ӧ��ɹ�
uint8_t MAX_IIC_Wait_Ack(void)
{
	uint8_t ucErrTime=0;
	MAX_SDA_IN();      //SDA����Ϊ����  
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
	MAX_SCL_L;//ʱ�����0 	   
	return 0;  
} 
//����ACKӦ��
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
//������ACKӦ��		    
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
//IIC����һ���ֽ�
//���شӻ�����Ӧ��
//1����Ӧ��
//0����Ӧ��			  
void MAX_IIC_Send_Byte(uint8_t txd)
{                        
    uint8_t t;   
	MAX_SDA_OUT(); 	    
    MAX_SCL_L;//����ʱ�ӿ�ʼ���ݴ���
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
//��1���ֽڣ�ack=1ʱ������ACK��ack=0������nACK   
uint8_t MAX_IIC_Read_Byte(unsigned char ack)
{
	unsigned char i,receive=0;
	MAX_SDA_IN();//SDA����Ϊ����
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
        MAX_IIC_NAck();//����nACK
    else
        MAX_IIC_Ack(); //����ACK   
    return receive;
}



//IICдһ���ֽ� 
//reg:�Ĵ�����ַ
//data:����
//����ֵ:0,����
//    ����,�������
uint8_t MAX_Write_Byte(uint8_t dev_addr,uint8_t reg_addr,uint8_t data) 				 
{ 
  MAX_IIC_Start(); 
	MAX_IIC_Send_Byte((dev_addr<<1)|0);//����������ַ+д����	
	if(MAX_IIC_Wait_Ack())	//�ȴ�Ӧ��
	{
		MAX_IIC_Stop();		 
		return 1;		
	}
    MAX_IIC_Send_Byte(reg_addr);	//д�Ĵ�����ַ
    MAX_IIC_Wait_Ack();		//�ȴ�Ӧ�� 
	MAX_IIC_Send_Byte(data);//��������
	if(MAX_IIC_Wait_Ack())	//�ȴ�ACK
	{
		MAX_IIC_Stop();	
		return 1;		 
	}		 
    MAX_IIC_Stop();	 
	return 0;
}
//IIC��һ���ֽ� 
//reg:�Ĵ�����ַ 
//����ֵ:����������
uint8_t MAX_Read_Byte(uint8_t dev_addr,uint8_t reg_addr)
{
	uint8_t res;
  MAX_IIC_Start(); 
	MAX_IIC_Send_Byte((dev_addr<<1)|0);//����������ַ+д����	
	MAX_IIC_Wait_Ack();		//�ȴ�Ӧ�� 
  MAX_IIC_Send_Byte(reg_addr);	//д�Ĵ�����ַ
  MAX_IIC_Wait_Ack();		//�ȴ�Ӧ��
  MAX_IIC_Start();
	MAX_IIC_Send_Byte((dev_addr<<1)|1);//����������ַ+������	
  MAX_IIC_Wait_Ack();		//�ȴ�Ӧ�� 
	res=MAX_IIC_Read_Byte(0);//��ȡ����,����nACK 
  MAX_IIC_Stop();			//����һ��ֹͣ���� 
	return res;		
}






